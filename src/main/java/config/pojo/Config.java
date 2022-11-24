package config.pojo;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Config {

    @BsonProperty(value = "telegramToken")
    private String telegramToken;

    @BsonProperty(value = "userIds")
    private List<Long> userId = new ArrayList<>();

    @BsonProperty(value = "chatIds")
    private List<String> chatIds = new ArrayList<>();

    @BsonProperty(value = "websites")
    private List<WebSiteConfig> webSiteConfigs = new ArrayList<>();

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

    public List<String> getChatIds() {
        return chatIds;
    }

    public void setChatIds(List<String> chatIds) {
        this.chatIds = chatIds;
    }

    public List<WebSiteConfig> getWebSiteConfigs() {
        return webSiteConfigs;
    }

    public void setWebSiteConfigs(List<WebSiteConfig> webSiteConfigs) {
        this.webSiteConfigs = webSiteConfigs;
    }
}
