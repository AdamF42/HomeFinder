package model;

import config.pojo.Config;
import org.slf4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Consumer;

public class TelegramBot extends TelegramLongPollingBot {

    private final Config config;
    private final Logger logger;
    private final Consumer<Update> forwardMsg;

    public TelegramBot(Config config, Logger logger, Consumer<Update> forwardMsg) {
        this.config = config;
        this.logger = logger;
        this.forwardMsg = forwardMsg;
    }


    @Override
    public String getBotUsername() {
        return "TelegramBot";
    }

    @Override
    public String getBotToken() {
        return this.config.getTelegramToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        forwardMsg.accept(update);
    }

    public void sendMsg(String chatId, String msg) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        this.execute(sendMessage);
    }

}
