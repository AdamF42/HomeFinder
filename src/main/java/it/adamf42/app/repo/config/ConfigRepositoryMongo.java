package it.adamf42.app.repo.config;

import com.mongodb.client.MongoCollection;
import it.adamf42.app.repo.config.pojo.Config;
import it.adamf42.core.repo.config.ConfigRepository;

public class ConfigRepositoryMongo implements ConfigRepository {

    MongoCollection<Config> collection;

    public ConfigRepositoryMongo(MongoCollection<Config> collection) {
        this.collection = collection;
    }

    @Override
    public Config getConfig() {
        return collection.find().first();
    }

}
