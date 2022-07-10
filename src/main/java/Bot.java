import ch.qos.logback.classic.Logger;
import config.models.Config;
import config.ConfigHandler;
import io.vavr.control.Try;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import pages.Page;
import pages.PageFactory;
import runnable.RunnableImpl;
import utils.interval.RandomInterval;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static pages.PageFactory.PageType.*;

class Bot extends TelegramLongPollingBot {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Bot.class);

    // TODO: really bad...
    private static final Config config = Try.of(ConfigHandler::getInstance).map(ConfigHandler::getConfig).get();

    private static final List<Page> pages = List.of(
            PageFactory.get(IMMOBILIARE, config.getWebsites().get("immobiliare").getUrl()),
            PageFactory.get(SUBITO, config.getWebsites().get("subito").getUrl()),
            PageFactory.get(IDEALISTA, config.getWebsites().get("idealista").getUrl()),
            PageFactory.get(CASA, config.getWebsites().get("casa").getUrl())
    );

    private static final ExecutorService executor = Executors.newFixedThreadPool(pages.size());

    private List<RunnableImpl> runnables = new ArrayList<>();

    public static void main(String[] args) {
        logger.info("Bot started");
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            logger.error("Unable to register bot.", e);
        }
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
        if (!config.getUserId().contains(update.getMessage().getFrom().getId())) {
            return;
        }
        String chatId = update.getMessage().getChatId().toString();
        String msg = update.getMessage().getText();
        logger.info("Received: {}", msg);
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

}

