package script;

import ch.qos.logback.classic.Logger;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import it.adamf42.app.repo.config.ConfigRepositoryMongo;
import it.adamf42.app.repo.config.pojo.ChatConfig;
import it.adamf42.app.repo.config.pojo.Config;
import it.adamf42.app.repo.config.pojo.ScrapingConfigs;
import it.adamf42.core.repo.data.HouseRepository;
import it.adamf42.app.repo.data.HouseRepositoryMongo;
import it.adamf42.app.repo.data.pojo.House;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.LoggerFactory;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

class FirstConfig {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(FirstConfig.class);
    private static final String MONGO_CONN_STR = "MONGO_CONN_STR";
    private static final String MONGO_DATABASE = "MONGO_DATABASE";

    public static void main(String[] args) {

        logger.info("Connecting to MongoDB");
        ConnectionString connectionString = new ConnectionString(System.getenv(MONGO_CONN_STR));
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString).codecRegistry(codecRegistry).build();

        String mongoDataBase = System.getenv(MONGO_DATABASE);
        logger.info("Getting MongoDB database {}", mongoDataBase);
        MongoClient client = MongoClients.create(clientSettings);
        MongoDatabase database = client.getDatabase(mongoDataBase);

        logger.info("Getting House collection");
        MongoCollection<House> collection = database.getCollection("links", House.class);
        HouseRepository houseRepository = new HouseRepositoryMongo(collection);

        logger.info("Getting Config collection");
        MongoCollection<Config> configCollection = database.getCollection("it/adamf42/app/repo/config", Config.class);
        ConfigRepositoryMongo configRepository = new ConfigRepositoryMongo(configCollection);
        Config conf = configRepository.getConfig();


        logger.info("Create config for websites");
        MongoCollection<ScrapingConfigs> websiteConfigCollection = database.getCollection("website_config", ScrapingConfigs.class);
        // List<WebSite> websites = conf.getWebsites();
//
//        WebSiteConfig websiteConfig = new WebSiteConfig();
//        websiteConfig.setWebsites(websites);
//        websiteConfigCollection.insertOne(websiteConfig);
//
//        websiteConfig = websiteConfigCollection.find().first();

        MongoCollection<ChatConfig> chatConfigCollection = database.getCollection("chat_config", ChatConfig.class);

//        ChatConfig newChatConfig = new ChatConfig();
//        newChatConfig.setChatId("-674090628");
//        newChatConfig.setWebsiteConfigId(websiteConfig.getId());
//        chatConfigCollection.insertOne(newChatConfig);



    }


}

