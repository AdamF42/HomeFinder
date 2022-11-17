package finder;

import akka.actor.typed.ActorSystem;

public class HomeFinder {
    ActorSystem<ManagerBehavior.Command> actorSystem;
    public void start() {
        actorSystem = ActorSystem.create(ManagerBehavior.create(), "HouseFinder");
        actorSystem.tell(new ManagerBehavior.BootCommand());
    }
}
