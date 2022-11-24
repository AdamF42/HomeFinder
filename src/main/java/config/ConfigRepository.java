package config;

import com.mongodb.client.MongoCollection;
import config.pojo.Config;

public class ConfigRepository {

    MongoCollection<Config> collection;

    public ConfigRepository(MongoCollection<Config> collection) {
        this.collection = collection;
    }

    public Config getConfig() {
        return collection.find().first();
    }

}
