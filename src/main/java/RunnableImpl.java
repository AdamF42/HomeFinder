import io.vavr.control.Try;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pages.Page;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class RunnableImpl implements Runnable {

    private static final String dataFile = Paths.get("./data.txt").toString();

    private String chatId;

    private Page page;

    private TelegramLongPollingBot bot;

    private boolean shouldRun = true;

    private static volatile RunnableImpl instance;

    private long millis;

    public static RunnableImpl getInstance(TelegramLongPollingBot bot, String chatId, Page page, long millis) {
        RunnableImpl result = instance;
        if (result == null) {
            synchronized (RunnableImpl.class) {
                result = instance;
                if (result == null) {
                    instance = result = new RunnableImpl(bot, chatId, page, millis);
                }
            }
        }
        return result;
    }

    public RunnableImpl(TelegramLongPollingBot bot, String chatId, Page page, long millis) {
        this.chatId = chatId;
        this.bot = bot;
        this.page = page;
        this.millis = millis;
    }

    public void setShouldRun(boolean val) {
        this.shouldRun = val;
    }

    public void run() {
        while (shouldRun) {
//            System.out.println("loop on: " + page.getStartUrl());
            try {
                getAllLinks().stream()
                        .filter(RunnableImpl::isNew)
                        .map(RunnableImpl::save)
                        .filter(e -> !e.isEmpty())
                        .forEach(msg -> sendMsg(this.chatId, msg));
            } catch (Exception e) {
                System.out.println("ROTTO");
                e.printStackTrace();
            }
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static String save(String str) {
        return Try.ofCallable(writeString(str)).getOrElse("");
    }

    private static Callable<String> writeString(String str) {
        return () -> {
            FileWriter writer = new FileWriter(dataFile, true);
            writer.write(str + System.lineSeparator());
            writer.close();
            return str;
        };
    }

    private static boolean isNew(String s) {

        List<String> result = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get(dataFile))) {
            result = lines.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return !result.contains(s);
    }

    private void sendMsg(String chatId, String msg) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        try {
            this.bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            // gestione errore in invio
        }
    }

    private List<String> getAllLinks() {
        Page cp = page.clone();
        List<String> links = cp.getLinks();
        while (cp.hasNextPage()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cp = cp.getNextPage();
            links.addAll(cp.getLinks());
        }
        System.out.println("LIST SIZE: " + links.size());
        return links;
    }
}