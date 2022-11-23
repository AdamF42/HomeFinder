package finder;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import config.pojo.Config;
import config.pojo.WebSite;
import data.pojo.House;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ManagerActor extends AbstractBehavior<ManagerActor.Command> {
    private static final String MONGO_CONN_STR = "MONGO_CONN_STR";
    private static final String MONGO_DATABASE = "MONGO_DATABASE";

    public interface Command extends Serializable {
    }

    private ManagerActor(ActorContext<Command> context) {
        super(context);
    }

    public static class BootCommand implements Command {
        private static final long serialVersionUID = 1L;
    }

    public static class ConfigResultCommand implements Command {
        private static final long serialVersionUID = 1L;
        private final Config config;

        public ConfigResultCommand(Config config) {
            this.config = config;
        }

        public Config getConfig() {
            return config;
        }
    }

    public static class ChatCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final Update update;


        public ChatCommand(Update update) {
            this.update = update;
        }

    }

    public static class LinksCommand implements Command {
        private static final long serialVersionUID = 1L;

        private final List<String> links;
        private final String webSiteName;

        private final String chatId;


        public LinksCommand(List<String> links, String webSiteName, String chatId) {
            this.links = links;
            this.webSiteName = webSiteName;
            this.chatId = chatId;
        }
    }

    public static class HousesCommand implements Command {
        private static final long serialVersionUID = 1L;

        private final List<House> houses;


        public HousesCommand(List<House> houses) {
            this.houses = houses;
        }
    }


    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerActor::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(BootCommand.class, msg -> {
                    ActorRef<DatabaseActor.Command> db = getContext().spawn(DatabaseActor.create(), "database");
                    db.tell(new DatabaseActor.StartCommand(System.getenv(MONGO_CONN_STR), System.getenv(MONGO_DATABASE), getContext().getSelf()));
                    return retrieveConfig(db);
                })
                .build();
    }

    private Receive<Command> retrieveConfig(ActorRef<DatabaseActor.Command> db) {
        return newReceiveBuilder()
                .onMessage(ConfigResultCommand.class, msg -> {
                    ActorRef<BotActor.Command> bot = getContext().spawn(BotActor.create(), "bot");
                    bot.tell(new BotActor.StartCommand(msg.config, getContext().getSelf()));
                    return this.running(msg.config, bot, db);
                })
                .build();
    }

    private Receive<Command> running(Config config, ActorRef<BotActor.Command> bot, ActorRef<DatabaseActor.Command> db) {
        return newReceiveBuilder()
                .onMessage(ChatCommand.class, msg -> {
                    if (!isValid(msg.update)) {
                        return Behaviors.same();
                    }
                    User user = msg.update.getMessage().getFrom();
                    String msgtext = msg.update.getMessage().getText();
                    String chatId = String.valueOf(msg.update.getMessage().getChatId());

                    getContext().getLog().info("[MSG] {} [CHAT_ID] {}", msgtext, chatId);

                    if (!config.getUserId().contains(user.getId())) {
                        return Behaviors.same();
                    }

                    switch (msgtext) {
                        case "start":
                            handleStart(bot, chatId, config);
                            break;
                        case "stop":
                            handleStop(bot);
                            break;
                        case "ping":
                            handlePing(chatId, bot);
                            break;
                        default:
                            // TODO: handle default
                    }
                    return Behaviors.same();
                })
                .onMessage(LinksCommand.class, msg -> {
                    db.tell(new DatabaseActor.GetHousesCommand(""));
                    return checkNewHouses(config, bot, db, msg);
                })
                .build();
    }

    private Receive<Command> checkNewHouses(Config config, ActorRef<BotActor.Command> bot, ActorRef<DatabaseActor.Command> db, LinksCommand linksMsg) {
        return newReceiveBuilder()
                .onMessage(HousesCommand.class, msg -> {
                    List<String> houses = msg.houses.stream().map(House::getLink).collect(Collectors.toList());
                    List<House> newHouses = linksMsg.links.stream()
                            .filter(link -> !houses.contains(link))
                            .peek(e -> getContext().getLog().info("[NEW LINK] {}", e))
                            .map(l -> this.toHouse(l, linksMsg.webSiteName))
                            .collect(Collectors.toList());
                    newHouses.forEach(h -> bot.tell(new BotActor.SendMsgCommand(h.getLink(), linksMsg.chatId)));
                    db.tell(new DatabaseActor.SaveHousesCommand(newHouses));
                    return this.running(config, bot, db);
                })
                .build();
    }



        private boolean isValid(Update update) {
        return !Objects.isNull(update.getMessage()) && !Objects.isNull(update.getMessage().getFrom());
    }

    private void handleStop(ActorRef<BotActor.Command> bot) {


    }

    private void handleStart(ActorRef<BotActor.Command> bot, String chatId, Config config) {

        for (WebSite site : config.getWebsites()) {
            ActorRef<WebSiteActor.Command> website = getContext().spawn(WebSiteActor.create(), site.getName() + "_website");
            website.tell(new WebSiteActor.StartCommand());
            ActorRef<ScraperActor.Command> scraper = getContext().spawn(ScraperActor.create(), site.getName() + "_scraper");
            scraper.tell(new ScraperActor.StartCommand(
                    getContext().getSelf(),
                    site,
                    website,
                    chatId));
        }

    }

    private void handlePing(String chatId, ActorRef<BotActor.Command> bot) {
        bot.tell(new BotActor.SendMsgCommand("pong", chatId));
    }

    private House toHouse(String str, String webSiteName) {
        House house = new House();
        house.setLink(str);
        house.setWebsite(webSiteName);
        house.setTimestamp(LocalDateTime.now());
        return house;
    }
}
