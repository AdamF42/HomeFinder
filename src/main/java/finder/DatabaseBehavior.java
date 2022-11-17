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
import io.vavr.control.Try;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.Serializable;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class DatabaseBehavior extends AbstractBehavior<DatabaseBehavior.Command> {


    public interface Command extends Serializable {
    }

    public static class StartCommand implements DatabaseBehavior.Command {

        private static final long serialVersionUID = 1L;
        private final String connString;
        private final String database;
        private final ActorRef<ManagerBehavior.Command> manager;

        public StartCommand(String connString, String database, ActorRef<ManagerBehavior.Command> manager) {
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

        public ActorRef<ManagerBehavior.Command> getManager() {
            return manager;
        }
    }

    public DatabaseBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<DatabaseBehavior.Command> create() {
        return Behaviors.setup(DatabaseBehavior::new);
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
                    msg.manager.tell(new ManagerBehavior.ConfigResultCommand(newConf));
                    return running(msg.manager, database);
                })
                .build();
    }

    private Receive<Command> running(ActorRef<ManagerBehavior.Command> manager, MongoDatabase database) {
        return newReceiveBuilder()

                .build();
    }

}
