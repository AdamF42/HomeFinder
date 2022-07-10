package runnable;

import ch.qos.logback.classic.Logger;
import io.vavr.control.Try;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pages.Page;
import utils.RandomInterval;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RunnableImpl implements Runnable {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(RunnableImpl.class);
    private static final String dataFile = Paths.get("./data.txt").toString();
    private final RandomInterval interval;
    private final RandomInterval navigationInterval;
    private final String chatId;
    private Page page;
    private final TelegramLongPollingBot bot;
    private boolean shouldRun = true;

    public RunnableImpl(TelegramLongPollingBot bot, String chatId, Page page, RandomInterval interval, RandomInterval navigationInterval) {
        this.chatId = chatId;
        this.bot = bot;
        this.page = page;
        this.interval = interval;
        this.navigationInterval = navigationInterval;
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

    public Set<String> listFiles(String dir) {
        return Stream.of(Objects.requireNonNull(new File(dir).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getAbsolutePath)
                .collect(Collectors.toSet());
    }

    private static boolean isNew(String s) {

        List<String> result = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get(dataFile))) {
            result = lines.collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Unable to read file", e);
        }

        return !result.contains(s);
    }

    public void reloadPage() {
        page = page.clone();
    }

    public void setShouldRun(boolean val) {
        this.shouldRun = val;
    }

    public void run() {
        logger.debug("[FILES] {}", listFiles("./").toString());
        while (shouldRun) {
            try {
                getAllLinks().stream()
                        .filter(RunnableImpl::isNew)
                        .peek(e -> logger.debug("New link: {}", e))
                        .map(RunnableImpl::save)
                        .filter(e -> !e.isEmpty())
                        .forEach(msg -> sendMsg(this.chatId, msg));
            } catch (Exception e) {
                logger.error("Generic error", e);
            }

            try {
                Thread.sleep(interval.getInterval());
            } catch (InterruptedException e) {
                logger.error("Unable to sleep", e);
            }
        }
    }

    private void sendMsg(String chatId, String msg) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        try {
            this.bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Unable to send msg {}", msg, e);
        }
    }

    private List<String> getAllLinks() {
        Page cp = page.clone();
        List<String> links = cp.getLinks();
        while (cp.hasNextPage()) {
            try {
                Thread.sleep(navigationInterval.getInterval());
            } catch (InterruptedException e) {
                logger.error("Unable to sleep", e);
            }
            cp = cp.getNextPage();
            links.addAll(cp.getLinks());
        }
        logger.debug("[PAGE] {} [LIST]: {}", page.getStartUrl(), links.size());
        return links;
    }
}