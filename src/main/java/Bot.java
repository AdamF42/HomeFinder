import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import pages.Page;
import pages.PageFactory;
import runnable.RunnableImpl;
import utils.RandomInterval;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static pages.PageFactory.PageType.*;

class Bot extends TelegramLongPollingBot {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Bot.class);

    private static final List<Long> userIds = List.of(55585460L);
    private static final RandomInterval interval = new RandomInterval(20, 30);
    private static final RandomInterval pageNavigationInterval = new RandomInterval(5, 10);
    private static final int maxPrice = 900;
    private static final int minPrice = 200;
    private static final int roomNumber = 3;

    private static final String immUrl = "https://www.immobiliare.it/affitto-case/bologna/?criterio=dataModifica&ordine=desc&prezzoMinimo=" + minPrice + "&prezzoMassimo=" + maxPrice + "&superficieMinima=60&localiMinimo=" + roomNumber + "&idMZona[]=17&idMZona[]=23&idMZona[]=31";
    private static final String subitoUrl = "https://www.subito.it/annunci-emilia-romagna/affitto/appartamenti/bologna/bologna/?ps=" + minPrice + "&pe=" + maxPrice + "&rs=" + roomNumber;
    private static final String idealistaUrl = "https://www.idealista.it/affitto-case/bologna-bologna/con-prezzo_" + maxPrice + ",prezzo-min_" + minPrice + ",trilocali-3,quadrilocali-4,5-locali-o-piu/?ordine=pubblicazione-desc";
    private static final String casaUrl = "https://www.casa.it/srp/?tr=affitti&numRoomsMin=" + roomNumber + "&priceMin=" + minPrice + "&priceMax=" + maxPrice + "&propertyTypeGroup=case&q=12d65861%2C171c3cab%2C035cb1f0%2C853e62a4%2Cd2a360ef%2Cd80cd525";

    private static final List<Page> pages = List.of(PageFactory.get(IMMOBILIARE, immUrl), PageFactory.get(SUBITO, subitoUrl), PageFactory.get(IDEALISTA, idealistaUrl), PageFactory.get(CASA, casaUrl));
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
        return "565399062:AAHd-_CapBje_0Bq6sa_CGoLOYC8RcFDJVM";
    }

    public void onUpdateReceived(Update update) {
        if (!userIds.contains(update.getMessage().getFrom().getId())) {
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
            runnables = pages.stream().map(page -> new RunnableImpl(this, chatId, page, interval, pageNavigationInterval)).collect(Collectors.toList());
            runnables.forEach(executor::submit);
        } else {
            runnables.forEach(RunnableImpl::reloadPage);
        }
        runnables.forEach(runnable -> runnable.setShouldRun(true));
    }

}

