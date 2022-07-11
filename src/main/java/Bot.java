import ch.qos.logback.classic.Logger;
import config.ConfigHandler;
import config.models.Config;
import io.vavr.control.Try;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
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

import static pages.PageFactory.PageType.fromString;

class Bot extends TelegramLongPollingBot {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Bot.class);

    private final Config config;
    private final List<Page> pages;
    private final ExecutorService executor;

    private List<RunnableImpl> runnables = new ArrayList<>();

    public Bot(Config config, List<Page> pages, ExecutorService executor) {
        this.config = config;
        this.pages = pages;
        this.executor = executor;
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

        if(!config.getUserId().contains(user.getId())) {
            return;
        }

        switch (msg) {
            case "start":
                handleStart(chatId);
                break;
            case "stop":
                handleStop();
                break;
            default:
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
                    .map(page -> new RunnableImpl(this, chatId, page, parsingInterval, navigationInterval))
                    .collect(Collectors.toList());
            runnables.forEach(executor::submit);
        } else {
            runnables.forEach(RunnableImpl::reloadPage);
        }
        runnables.forEach(runnable -> runnable.setShouldRun(true));
    }

    public static void main(String[] args) {
        logger.info("Getting configuration");
        Config config = Try.of(ConfigHandler::getInstance).map(ConfigHandler::getConfig)
                .onFailure(e -> logger.error("Unable to get config", e))
                .get();

        logger.info("Getting starting pages");
        List<Page> pages = config.getWebsites().keySet().stream()
                .map(k -> PageFactory.get(Objects.requireNonNull(fromString(k)), config.getWebsites().get(k).getUrl()))
                .collect(Collectors.toList());

        logger.info("Getting executor");
        ExecutorService executor = Executors.newFixedThreadPool(pages.size());

        logger.info("Getting Telegram Bot");


        Try.of(() -> new TelegramBotsApi(DefaultBotSession.class))
                .onFailure(e -> logger.error("Unable to get telegram api", e))
                .andThenTry(api -> api.registerBot(new Bot(config, pages, executor)))
                .onFailure(e -> logger.error("Unable to register telegram bot", e))
                .onSuccess(res -> logger.info("Bot started"));
    }

}

