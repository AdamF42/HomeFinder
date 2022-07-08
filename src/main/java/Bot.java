import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Bot extends TelegramLongPollingBot {

    RunnableImpl imm;
    RunnableImpl sub;

    // private static final long millis = 5*60*1000;
    private static final long millis = 10 * 1000;
    private static final String immUrl = "https://www.immobiliare.it/affitto-case/bologna/?criterio=rilevanza&prezzoMinimo=200&prezzoMassimo=900&superficieMinima=60&localiMinimo=3&idMZona[]=17&idMZona[]=23&idMZona[]=31";
    private static final String subitoUrl = "https://www.subito.it/annunci-emilia-romagna/affitto/appartamenti/bologna/bologna/?ps=300&pe=800&rs=3";

    static ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    public String getBotUsername() {
        return "Bot";
    }

    @Override
    public String getBotToken() {
        return "";
    }

    public void onUpdateReceived(Update update) {

        Page immobiliare = new Immobiliare(immUrl);
        Page subito = new Subito(subitoUrl);

        String chatId = update.getMessage().getChatId().toString();
        switch (update.getMessage().getText()) {
            case "start" -> {
                imm = RunnableImpl.getInstance(this, chatId, immobiliare, millis);
                imm.setShouldRun(true);
                executor.submit(imm);
                sub = new RunnableImpl(this, chatId, subito, millis);
                sub.setShouldRun(true);
                executor.submit(sub);
            }
            case "stop" -> {
                imm.setShouldRun(false);
                sub.setShouldRun(false);
            }
            default -> {
            }
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

