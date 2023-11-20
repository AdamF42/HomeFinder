package it.adamf42.application.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.typesafe.config.ConfigException;

import java.io.Serializable;

public class ManagerActor extends AbstractBehavior<ManagerActor.Command> {
    private static final String MONGO_CONN_STR = System.getenv("MONGO_CONN_STR");
    private static final String MONGO_DATABASE = System.getenv("MONGO_DATABASE");
    private static final String TG_TOKEN = System.getenv("TG_TOKEN");
    private static final String KAKFA_BOOTSTRAP_SERVERS = System.getenv("KAKFA_BOOTSTRAP_SERVERS");

    public ManagerActor(ActorContext<Command> context) {
        super(context);
    }

    public interface Command extends Serializable {
    }

    public static class BootCommand implements ManagerActor.Command {
        private static final long serialVersionUID = 1L;
    }

    @Override
    public Receive<Command> createReceive() {

        Behavior<ChatManagerActor.Command> chatManagerBehavior =
                Behaviors.supervise(ChatManagerActor.create()).onFailure(SupervisorStrategy.resume());  // resume = ignore the crash
        ActorRef<ChatManagerActor.Command> chatManager = getContext().spawn(chatManagerBehavior, "chatman");

        Behavior<DatabaseActor.Command> dbBehavior =
                Behaviors.supervise(DatabaseActor.create()).onFailure(SupervisorStrategy.resume());  // resume = ignore the crash
        ActorRef<DatabaseActor.Command> db = getContext().spawn(dbBehavior, "database");
        getContext().watch(db);

        Behavior<KafkaActor.Command> kafkaBehavior =
                Behaviors.supervise(KafkaActor.create(db, chatManager, KAKFA_BOOTSTRAP_SERVERS)).onFailure(SupervisorStrategy.resume());  // resume = ignore the crash
        ActorRef<KafkaActor.Command> kafka = getContext().spawn(kafkaBehavior, "kafka");

        Behavior<BotActor.Command> botBehavior =
                Behaviors.supervise(BotActor.create(db, chatManager)).onFailure(SupervisorStrategy.resume());  // resume = ignore the crash
        ActorRef<BotActor.Command> bot = getContext().spawn(botBehavior, "bot");


        return newReceiveBuilder()
                .onMessage(ManagerActor.BootCommand.class, msg -> {
                    db.tell(new DatabaseActor.BootCommand(MONGO_CONN_STR, MONGO_DATABASE, getContext().getSelf()));
                    kafka.tell(new KafkaActor.BootCommand());
                    bot.tell(new BotActor.StartCommand(TG_TOKEN));
                    chatManager.tell(new ChatManagerActor.StartCommand(bot, db));
                    return Behaviors.same();
                })
                .build();
    }

    public static Behavior<ManagerActor.Command> create() {

        if (isNullOrEmpty(MONGO_CONN_STR)) {
            throw new ConfigException.Missing("MONGO_CONN_STR");
        }
        if (isNullOrEmpty(MONGO_DATABASE)) {
            throw new ConfigException.Missing("MONGO_DATABASE");
        }
        if (isNullOrEmpty(TG_TOKEN)) {
            throw new ConfigException.Missing("TG_TOKEN");
        }
        if (isNullOrEmpty(KAKFA_BOOTSTRAP_SERVERS)) {
            throw new ConfigException.Missing("KAKFA_BOOTSTRAP_SERVERS");
        }

        return Behaviors.setup(ManagerActor::new);
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty() || value.isBlank();
    }

}
