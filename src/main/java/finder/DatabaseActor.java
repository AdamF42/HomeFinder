package finder;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import config.ConfigRepository;
import config.pojo.Config;
import data.HouseRepository;
import data.HouseRepositoryMongo;
import data.pojo.House;
import io.vavr.control.Try;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.Serializable;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class DatabaseActor extends AbstractBehavior<DatabaseActor.Command> {


    public interface Command extends Serializable {
    }

    public static class StartCommand implements DatabaseActor.Command {

        private static final long serialVersionUID = 1L;
        private final String connString;
        private final String database;
        private final ActorRef<ManagerActor.Command> manager;

        public StartCommand(String connString, String database, ActorRef<ManagerActor.Command> manager) {
            this.connString = connString;
            this.database = database;
            this.manager = manager;
        }

        public String getConnString() {
            return connString;
        }

        public String getDatabase() {
            return database;
        }

        public ActorRef<ManagerActor.Command> getManager() {
            return manager;
        }
    }

    public static class GetHousesCommand implements DatabaseActor.Command {

        private static final long serialVersionUID = 1L;

        private final String webSiteName;

        public GetHousesCommand(String webSiteName) {
            this.webSiteName = webSiteName;
        }
    }


    public static class SaveHousesCommand implements DatabaseActor.Command {

        private static final long serialVersionUID = 1L;

        private final List<House> houses;

        public SaveHousesCommand(List<House> houses) {
            this.houses = houses;
        }
    }

    public DatabaseActor(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<DatabaseActor.Command> create() {
        return Behaviors.setup(DatabaseActor::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, msg -> {
                    ConnectionString connectionString = new ConnectionString(msg.connString);
                    CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
                    CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
                    MongoClientSettings clientSettings = MongoClientSettings.builder()
                            .applyConnectionString(connectionString)
                            .codecRegistry(codecRegistry)
                            .build();
                    MongoDatabase database = Try.of(() -> MongoClients.create(clientSettings))
                            .map(mc -> mc.getDatabase(msg.database))
                            .get();
                    MongoCollection<Config> configCollection = database.getCollection("config", Config.class);
                    ConfigRepository configRepository = new ConfigRepository(configCollection);
                    Config newConf = configRepository.getConfig();
                    msg.manager.tell(new ManagerActor.ConfigResultCommand(newConf));
                    MongoCollection<House> collection = database.getCollection("links", House.class);
                    HouseRepository houseRepository = new HouseRepositoryMongo(collection);
                    return running(msg.manager, houseRepository, configRepository);
                })
                .build();
    }

    private Receive<Command> running(ActorRef<ManagerActor.Command> manager, HouseRepository houseRepository, ConfigRepository configRepository) {
        return newReceiveBuilder()
                .onMessage(GetHousesCommand.class, msg -> {
                    List<House> houses = houseRepository.getHouses();
                    manager.tell(new ManagerActor.HousesCommand(houses));
                    return running(manager, houseRepository, configRepository);
                })
                .onMessage(SaveHousesCommand.class, msg -> {
                    houseRepository.saveHouses(msg.houses);
                    return running(manager, houseRepository, configRepository);
                })
                .build();
    }

}
