package it.adamf42.app.repo.config.pojo;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

public class ChatConfig {

    @BsonProperty(value = "chatId")
    private String chatId;

    @BsonProperty(value = "website_config_id")
    private ObjectId websiteConfigId;

    public ObjectId getWebsiteConfigId() {
        return websiteConfigId;
    }

    public void setWebsiteConfigId(ObjectId websiteConfigId) {
        this.websiteConfigId = websiteConfigId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
