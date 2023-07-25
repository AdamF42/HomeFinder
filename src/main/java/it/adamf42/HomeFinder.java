package it.adamf42;

import akka.actor.typed.ActorSystem;
import it.adamf42.app.actors.ManagerActor;

public class HomeFinder {
    ActorSystem<ManagerActor.Command> actorSystem;
    public void start() {
        actorSystem = ActorSystem.create(ManagerActor.create(), "HouseFinder");
        actorSystem.tell(new ManagerActor.BootCommand());
    }

    public static void main(String[] args) {

        HomeFinder finder = new HomeFinder();
        finder.start();

    }

}
