package config;

import com.mongodb.client.MongoCollection;
import config.pojo.Config;
import config.pojo.WebSite;

public class ConfigRepository {

    MongoCollection<Config> collection;

    public ConfigRepository(MongoCollection<Config> collection) {
        this.collection = collection;
    }

    public Config getConfig() {
        return collection.find().first();
    }

    public WebSite getWebsite(WebSiteType webSiteType){
        return getConfig().getWebsites().stream()
                .filter(e -> webSiteType.name().equalsIgnoreCase(e.getName()))
                .findFirst().orElseGet(WebSite::new);
    }

    public enum WebSiteType {
        IMMOBILIARE,
        SUBITO,
        IDEALISTA,
        CASA
    }

}
