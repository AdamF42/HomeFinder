package finder;

import akka.actor.typed.ActorSystem;

public class HomeFinder {
    ActorSystem<ManagerActor.Command> actorSystem;
    public void start() {
        actorSystem = ActorSystem.create(ManagerActor.create(), "HouseFinder");
        actorSystem.tell(new ManagerActor.BootCommand());
    }
}
