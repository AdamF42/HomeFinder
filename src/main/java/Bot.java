import ch.qos.logback.classic.Logger;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import config.ConfigHandler;
import config.ConfigRepository;
import config.models.ConfigYaml;
import config.pojo.Config;
import data.pojo.House;
import data.HouseRepository;
import data.HouseRepositoryMongo;
import io.vavr.control.Try;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import pages.Page;
import pages.PageFactory;
import runnable.RunnableImpl;
import utils.interval.RandomInterval;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static core.WebSiteType.fromString;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

class Bot extends TelegramLongPollingBot {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Bot.class);

    private final ConfigYaml config;
    private final List<Page> pages;
    private final ExecutorService executor;
    private final HouseRepository houseRepository;

    private List<RunnableImpl> runnables = new ArrayList<>();

    public Bot(ConfigYaml config, List<Page> pages, HouseRepository houseRepository, ExecutorService executor) {
        this.config = config;
        this.pages = pages;
        this.executor = executor;
        this.houseRepository = houseRepository;
    }

    @Override
    public String getBotUsername() {
        return "Bot";
    }

    @Override
    public String getBotToken() {
        return config.getTelegramToken();
    }

    public void onUpdateReceived(Update update) {
        if (!isValid(update)) {
            return;
        }
        User user = update.getMessage().getFrom();
        String msg = update.getMessage().getText();
        String chatId = String.valueOf(update.getMessage().getChatId());

        logger.info("[MSG] {} [FROM] {}", msg, user.toString());

        if (!config.getUserId().contains(user.getId())) {
            return;
        }

        switch (msg) {
            case "start":
                handleStart(chatId);
                break;
            case "stop":
                handleStop();
                break;
            case "ping":
                handlePing(chatId);
                break;
            default:
        }
    }

    private void handlePing(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("pong");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Unable to send msg {}", "pong", e);
        }
    }

    private boolean isValid(Update update) {
        return !Objects.isNull(update.getMessage()) && !Objects.isNull(update.getMessage().getFrom());
    }

    private void handleStop() {
        if (!runnables.isEmpty()) {
            runnables.forEach(runnable -> runnable.setShouldRun(false));
        }
    }

    private void handleStart(String chatId) {
        if (runnables.isEmpty()) {
            RandomInterval parsingInterval = new RandomInterval(config.getMinParsingInterval(), config.getMaxParsingInterval());
            RandomInterval navigationInterval = new RandomInterval(config.getMinpageNavigationInterval(), config.getMaxpageNavigationInterval());
            runnables = pages.stream()
                    .map(page -> new RunnableImpl(this, chatId, page, houseRepository, parsingInterval, navigationInterval))
                    .collect(Collectors.toList());
            runnables.forEach(executor::submit);
        } else {
            runnables.forEach(RunnableImpl::reloadPage);
        }
        runnables.forEach(runnable -> runnable.setShouldRun(true));
    }

    public static void main(String[] args) {

        logger.info("Getting configuration");
        ConfigYaml config = Try.of(ConfigHandler::getInstance).map(ConfigHandler::getConfig)
                .onFailure(e -> logger.error("Unable to get config", e))
                .get();
        logger.info("Getting mongoDb");
        String string = "mongodb+srv://" + config.getMongoDbDatabase() + ":" + config.getMongoDBPass() + "@" + config.getMongoDBCluster() + ".3vyhn.mongodb.net/?retryWrites=true&w=majority";
        ConnectionString connectionString = new ConnectionString(string);
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();


        MongoDatabase database = Try.of(() -> MongoClients.create(clientSettings))
                .map(mc -> mc.getDatabase(config.getMongoDbDatabase()))
                .onFailure(e -> logger.error("Unable to get database", e))
                .get();

        MongoCollection<House> collection = database.getCollection("links", House.class);
        MongoCollection<Config> configCollection = database.getCollection("config", Config.class);

        HouseRepository houseRepository = new HouseRepositoryMongo(collection);
        ConfigRepository configRepository = new ConfigRepository(configCollection);

        logger.info("Getting starting pages");

        Config newConf = configRepository.getConfig();
        List<Page> pages = newConf.getWebsites().stream()
                .map(w -> PageFactory.get(Objects.requireNonNull(fromString(w.getName())), w.getUrl()))
                .collect(Collectors.toList());

//        List<Page> pages = config.getWebsites().keySet().stream()
//                .map(k -> PageFactory.get(Objects.requireNonNull(fromString(k)), config.getWebsites().get(k).getUrl()))
//                .collect(Collectors.toList());

        logger.info("Getting executor");
        ExecutorService executor = Executors.newFixedThreadPool(pages.size());

        logger.info("Getting Telegram Bot");


        Try.of(() -> new TelegramBotsApi(DefaultBotSession.class))
                .onFailure(e -> logger.error("Unable to get telegram api", e))
                .andThenTry(api -> api.registerBot(new Bot(config, pages, houseRepository, executor)))
                .onFailure(e -> logger.error("Unable to register telegram bot", e))
                .onSuccess(res -> logger.info("Bot started"));
    }

}

