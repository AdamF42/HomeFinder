import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import pages.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Bot extends TelegramLongPollingBot {

    RunnableImpl imm;
    RunnableImpl sub;
    RunnableImpl idea;
    RunnableImpl cs;

    // private static final long millis = 5*60*1000;
    private static final long millis = 30 * 60 * 1000;
    private static final int maxPrice = 900;
    private static final int minPrice = 200;
    private static final int roomNumber = 3;

    private static final String immUrl = "https://www.immobiliare.it/affitto-case/bologna/?criterio=dataModifica&ordine=desc&prezzoMinimo=" + minPrice + "&prezzoMassimo=" + maxPrice + "&superficieMinima=60&localiMinimo=" + roomNumber + "&idMZona[]=17&idMZona[]=23&idMZona[]=31";
    private static final String subitoUrl = "https://www.subito.it/annunci-emilia-romagna/affitto/appartamenti/bologna/bologna/?ps=" + minPrice + "&pe=" + maxPrice + "&rs=" + roomNumber;
    private static final String idealistaUrl = "https://www.idealista.it/affitto-case/bologna-bologna/con-prezzo_" + maxPrice + ",prezzo-min_" + minPrice + ",trilocali-3,quadrilocali-4,5-locali-o-piu/?ordine=pubblicazione-desc";
    private static final String casaUrl = "https://www.casa.it/srp/?tr=affitti&numRoomsMin=" + roomNumber + "&priceMin=" + minPrice + "&priceMax=" + maxPrice + "&propertyTypeGroup=case&q=12d65861%2C171c3cab%2C035cb1f0%2C853e62a4%2Cd2a360ef%2Cd80cd525";

    static ExecutorService executor = Executors.newFixedThreadPool(4);

    @Override
    public String getBotUsername() {
        return "Bot";
    }

    @Override
    public String getBotToken() {
        return "565399062:AAHd-_CapBje_0Bq6sa_CGoLOYC8RcFDJVM";
    }

    public void onUpdateReceived(Update update) {

        Page immobiliare = new Immobiliare(immUrl);
        Page subito = new Subito(subitoUrl);
        Page idealista = new Idealista(idealistaUrl);
        Page casa = new Casa(casaUrl);

        String chatId = update.getMessage().getChatId().toString();
        switch (update.getMessage().getText()) {
            case "start":
                imm = new RunnableImpl(this, chatId, immobiliare, millis);
                imm.setShouldRun(true);
                executor.submit(imm);

                sub = new RunnableImpl(this, chatId, subito, millis);
                sub.setShouldRun(true);
                executor.submit(sub);

                idea = new RunnableImpl(this, chatId, idealista, millis);
                idea.setShouldRun(true);
                executor.submit(idea);

                cs = new RunnableImpl(this, chatId, casa, millis);
                cs.setShouldRun(true);
                executor.submit(cs);

                break;
            case "stop":
                imm.setShouldRun(false);
                sub.setShouldRun(false);
                idea.setShouldRun(false);
                cs.setShouldRun(false);
                break;
            default:

        }
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}

