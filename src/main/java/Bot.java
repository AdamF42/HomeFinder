import ch.qos.logback.classic.Logger;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import config.ConfigRepository;
import config.pojo.Config;
import data.HouseRepository;
import data.HouseRepositoryMongo;
import data.pojo.House;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static core.WebSiteType.fromString;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

class Bot extends TelegramLongPollingBot {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Bot.class);
    private static final String MONGO_CONN_STR = "MONGO_CONN_STR";
    private static final String MONGO_DATABASE = "MONGO_DATABASE";
    private final Config config;
    private final List<Page> pages;
    private final ExecutorService executor;
    private final HouseRepository houseRepository;
    private List<RunnableImpl> runnables = new ArrayList<>();

    public Bot(Config config, List<Page> pages, HouseRepository houseRepository, ExecutorService executor) {
        this.config = config;
        this.pages = pages;
        this.executor = executor;
        this.houseRepository = houseRepository;
    }

    public static void main(String[] args) {

        logger.info("Connecting to MongoDB");
        ConnectionString connectionString = new ConnectionString(System.getenv(MONGO_CONN_STR));
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();

        String mongoDataBase = System.getenv(MONGO_DATABASE);
        logger.info("Getting MongoDB database {}", mongoDataBase);
        MongoDatabase database = Try.of(() -> MongoClients.create(clientSettings))
                .map(mc -> mc.getDatabase(mongoDataBase))
                .onFailure(e -> logger.error("Unable to get database", e))
                .get();

        logger.info("Getting House collection");
        MongoCollection<House> collection = database.getCollection("links", House.class);
        HouseRepository houseRepository = new HouseRepositoryMongo(collection);

        logger.info("Getting Config collection");
        MongoCollection<Config> configCollection = database.getCollection("config", Config.class);
        ConfigRepository configRepository = new ConfigRepository(configCollection);

        logger.info("Getting starting pages");
        Config newConf = configRepository.getConfig();
        List<Page> pages = new ArrayList<>();
//                newConf.getWebsites().stream()
//                .map(w -> PageFactory.get(Objects.requireNonNull(fromString(w.getName())), w))
//                .collect(Collectors.toList());

        logger.info("Getting executors");
        ExecutorService executor = Executors.newFixedThreadPool(pages.size());

        logger.info("Getting Telegram Bot");
        Try.of(() -> new TelegramBotsApi(DefaultBotSession.class))
                .onFailure(e -> logger.error("Unable to get telegram api", e))
                .andThenTry(api -> api.registerBot(new Bot(newConf, pages, houseRepository, executor)))
                .onFailure(e -> logger.error("Unable to register telegram bot", e))
                .onSuccess(res -> logger.info("Bot started"));
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

        logger.info("[MSG] {} [CHAT_ID] {}", msg, chatId);

        if (!config.getUserId().contains(user.getId())) {
            return;
        }

        switch (msg) {
            case "start":
                handleStart();
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
        sendMsg(chatId).accept("pong");
    }


    private Consumer<String> sendMsg(String chatId) {
        return msg -> {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(msg);
            try {
                this.execute(sendMessage);
            } catch (TelegramApiException e) {
                logger.error("Unable to send msg {}", msg, e);
            }
        };
    }

    private boolean isValid(Update update) {
        return !Objects.isNull(update.getMessage()) && !Objects.isNull(update.getMessage().getFrom());
    }

    private void handleStop() {
        if (!runnables.isEmpty()) {
            runnables.forEach(runnable -> runnable.setShouldRun(false));
        }
    }

    private void handleStart() {
        if (runnables.isEmpty()) {
            for (Page page : pages) {
                for (String chatId : config.getChatIds()) {
                    RunnableImpl runnable = new RunnableImpl(sendMsg(chatId), page, houseRepository);
                    runnables.add(runnable);
                }
            }
            runnables.forEach(executor::submit);
        } else {
            runnables.forEach(RunnableImpl::reloadPage);
        }
        runnables.forEach(runnable -> runnable.setShouldRun(true));
    }

}

