package finder;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import config.pojo.Config;
import config.pojo.WebSite;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {
    private static final String MONGO_CONN_STR = "MONGO_CONN_STR";
    private static final String MONGO_DATABASE = "MONGO_DATABASE";
    public interface Command extends Serializable {
    }
    private ManagerBehavior(ActorContext<Command> context) {
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

    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(BootCommand.class, msg -> {
                    ActorRef<DatabaseBehavior.Command> db = getContext().spawn(DatabaseBehavior.create(), "database");
                    db.tell(new DatabaseBehavior.StartCommand(System.getenv(MONGO_CONN_STR), System.getenv(MONGO_DATABASE), getContext().getSelf()));
                    return Behaviors.same();
                })
                .onMessage(ConfigResultCommand.class, msg -> {
                    ActorRef<BotBehavior.Command> bot = getContext().spawn(BotBehavior.create(), "bot");
                    bot.tell(new BotBehavior.StartCommand(msg.config, getContext().getSelf()));
                    return this.running(msg.config, bot);
                })
                .build();
    }

    private Receive<Command> running(Config config, ActorRef<BotBehavior.Command> bot) {
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
                .build();
    }

    private boolean isValid(Update update) {
        return !Objects.isNull(update.getMessage()) && !Objects.isNull(update.getMessage().getFrom());
    }

    private void handleStop(ActorRef<BotBehavior.Command> bot) {


    }

    private void handleStart(ActorRef<BotBehavior.Command> bot, String chatId, Config config) {

        for (WebSite site: config.getWebsites()) {
            if (site.getName().equalsIgnoreCase("immobiliare")) {
                ActorRef<WebSiteActor.Command> website = getContext().spawn(WebSiteActor.create(), site.getName()+"_website");
                website.tell(new WebSiteActor.StartCommand());
                ActorRef<ScraperActor.Command> scraper = getContext().spawn(ScraperActor.create(), site.getName()+"_scraper");
                scraper.tell(new ScraperActor.StartCommand(
                        site.getUrl(),
                        site.getBaseUrl(),
                        "div > div.nd-mediaObject__content.in-card__content.in-realEstateListCard__content > a",
                        "#__next > section > div.in-main.in-searchList__main > div.in-pagination.in-searchList__pagination > div:nth-child(3) > a:nth-child(1)",
                        website));
            }
        }

    }

    private void handlePing(String chatId, ActorRef<BotBehavior.Command> bot) {
        bot.tell(new BotBehavior.SendMsgCommand("pong", chatId));
    }


}
