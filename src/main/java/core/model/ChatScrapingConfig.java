package core.model;

import java.io.Serializable;
import java.util.List;


public class ChatScrapingConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String chatId;

    private final List<ScrapeParam> scrapingParams;


    public ChatScrapingConfig(String chatId, List<ScrapeParam> scrapingParams) {
        this.chatId = chatId;
        this.scrapingParams = scrapingParams;
    }

    public String getChatId() {
        return chatId;
    }

    public List<ScrapeParam> getScrapingParams() {
        return scrapingParams;
    }
}
