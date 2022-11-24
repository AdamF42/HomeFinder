package it.adamf42.app.repo.config;

import com.mongodb.client.MongoCollection;
import it.adamf42.app.repo.config.pojo.Config;

public class ConfigRepository {

    MongoCollection<Config> collection;

    public ConfigRepository(MongoCollection<Config> collection) {
        this.collection = collection;
    }

    public Config getConfig() {
        return collection.find().first();
    }

}
