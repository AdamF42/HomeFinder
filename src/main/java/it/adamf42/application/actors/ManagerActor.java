package it.adamf42.application.actors;

import java.io.Serializable;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ManagerActor extends AbstractBehavior<ManagerActor.Command>
{
	private static final String MONGO_CONN_STR = "MONGO_CONN_STR";
	private static final String MONGO_DATABASE = "MONGO_DATABASE";

	public ManagerActor(ActorContext<Command> context)
	{
		super(context);
	}

	public interface Command extends Serializable
	{
	}

	public static class BootCommand implements ManagerActor.Command
	{
		private static final long serialVersionUID = 1L;
	}

	@Override
	public Receive<Command> createReceive()
	{
		Behavior<DatabaseActor.Command> dbBehavior =
		Behaviors.supervise(DatabaseActor.create()).onFailure(SupervisorStrategy.resume());  // resume = ignore the crash
		ActorRef<DatabaseActor.Command> db = getContext().spawn(dbBehavior, "database");
		getContext().watch(db);

		Behavior<KafkaActor.Command> kafkaBehavior =
		Behaviors.supervise(KafkaActor.create(db)).onFailure(SupervisorStrategy.resume());  // resume = ignore the crash
		ActorRef<KafkaActor.Command> kafka = getContext().spawn(kafkaBehavior, "kafka");

		return newReceiveBuilder()
		.onMessage(ManagerActor.BootCommand.class, msg -> {
			db.tell(new DatabaseActor.BootCommand(System.getenv(MONGO_CONN_STR), System.getenv(MONGO_DATABASE), getContext().getSelf()));
			kafka.tell(new KafkaActor.BootCommand());
			return Behaviors.same();
		})
		.build();
	}

	public static Behavior<
	ManagerActor.Command> create() {
		return Behaviors.setup(ManagerActor::new);
	}


}
