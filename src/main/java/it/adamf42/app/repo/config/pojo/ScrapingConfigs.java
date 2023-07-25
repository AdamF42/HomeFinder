package it.adamf42.app.repo.config.pojo;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class ScrapingConfigs {

    private ObjectId id;

    @BsonProperty(value = "website_config")
    private List<ScrapeParam> websites = new ArrayList<>();

    public List<ScrapeParam> getWebsites() {
        return websites;
    }

    public void setWebsites(List<ScrapeParam> websites) {
        this.websites = websites;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
