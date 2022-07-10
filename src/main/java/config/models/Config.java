package config.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    private String telegramToken;
    private List<Long> userId = new ArrayList<>();
    private Integer minParsingInterval;
    private Integer maxParsingInterval;
    private Integer minpageNavigationInterval;
    private Integer  maxpageNavigationInterval;

    private Map<String, WebSite> websites = new HashMap<>();

    public String getTelegramToken() {
        return telegramToken;
    }

    public void setTelegramToken(String telegramToken) {
        this.telegramToken = telegramToken;
    }

    public List<Long> getUserId() {
        return userId;
    }

    public void setUserId(List<Long> userId) {
        this.userId = userId;
    }

    public Integer getMinParsingInterval() {
        return minParsingInterval;
    }

    public void setMinParsingInterval(Integer minParsingInterval) {
        this.minParsingInterval = minParsingInterval;
    }

    public Integer getMaxParsingInterval() {
        return maxParsingInterval;
    }

    public void setMaxParsingInterval(Integer maxParsingInterval) {
        this.maxParsingInterval = maxParsingInterval;
    }

    public Integer getMinpageNavigationInterval() {
        return minpageNavigationInterval;
    }

    public void setMinpageNavigationInterval(Integer minpageNavigationInterval) {
        this.minpageNavigationInterval = minpageNavigationInterval;
    }

    public Integer getMaxpageNavigationInterval() {
        return maxpageNavigationInterval;
    }

    public void setMaxpageNavigationInterval(Integer maxpageNavigationInterval) {
        this.maxpageNavigationInterval = maxpageNavigationInterval;
    }

    public Map<String, WebSite> getWebsites() {
        return websites;
    }

    public void setWebsites(Map<String, WebSite> websites) {
        this.websites = websites;
    }
}
