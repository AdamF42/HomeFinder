package it.adamf42.application.actors;

import java.io.Serializable;

import akka.actor.typed.Behavior;
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
		return null;
	}

	public static Behavior<
	ManagerActor.Command> create() {
		return Behaviors.setup(ManagerActor::new);
	}


}
