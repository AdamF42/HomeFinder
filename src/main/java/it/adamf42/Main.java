package it.adamf42;

import akka.actor.typed.ActorSystem;
import it.adamf42.application.actors.ManagerActor;

public class Main
{
    ActorSystem<ManagerActor.Command> actorSystem;
    public void start() {
        actorSystem = ActorSystem.create(ManagerActor.create(), "Manager");
        actorSystem.tell(new ManagerActor.BootCommand());
    }

    public static void main(String[] args) {

        Main finder = new Main();
        finder.start();

    }

}
