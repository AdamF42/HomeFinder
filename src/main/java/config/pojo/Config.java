package config.pojo;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    @BsonProperty(value = "telegramToken")
    private String telegramToken;

    @BsonProperty(value = "userIds")
    private List<Long> userId = new ArrayList<>();

    @BsonProperty(value = "websites")
    private List<WebSite> websites = new ArrayList<>();

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

    public List<WebSite> getWebsites() {
        return websites;
    }

    public void setWebsites(List<WebSite> websites) {
        this.websites = websites;
    }
}
