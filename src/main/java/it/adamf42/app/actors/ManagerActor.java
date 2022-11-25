package it.adamf42.app.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import it.adamf42.app.repo.config.pojo.Config;
import it.adamf42.app.repo.data.pojo.House;
import it.adamf42.core.domain.ScrapeParam;
import it.adamf42.core.domain.ChatScrapingConfig;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ManagerActor extends AbstractBehavior<ManagerActor.Command> {
    private static final String MONGO_CONN_STR = "MONGO_CONN_STR";
    private static final String MONGO_DATABASE = "MONGO_DATABASE";

    private Map<String, List<ActorRef<ScraperActor.Command>>> currentChatScrapers = new HashMap<>();
    private Map<String, Integer> currentChatScrapersCounter = new HashMap<>();

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

        private final List<ChatScrapingConfig> chatScrapingConfigs;

        public ConfigResultCommand(Config config, List<ChatScrapingConfig> chatScrapingConfigs) {
            this.config = config;
            this.chatScrapingConfigs = chatScrapingConfigs;
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

        Behavior<DatabaseActor.Command> dbBehavior =
                Behaviors.supervise(DatabaseActor.create()).onFailure(SupervisorStrategy.resume());  // resume = ignore the crash

        ActorRef<DatabaseActor.Command> db = getContext().spawn(dbBehavior, "database");

        Behavior<BotActor.Command> botBehavior =
                Behaviors.supervise(BotActor.create()).onFailure(SupervisorStrategy.resume());  // resume = ignore the crash

        ActorRef<BotActor.Command> bot = getContext().spawn(botBehavior, "bot");




        return newReceiveBuilder()
                .onMessage(BootCommand.class, msg -> {
                    db.tell(new DatabaseActor.StartCommand(System.getenv(MONGO_CONN_STR), System.getenv(MONGO_DATABASE), getContext().getSelf()));
                    return Behaviors.same();
                })
                .onMessage(ConfigResultCommand.class, msg -> {
                    bot.tell(new BotActor.StartCommand(msg.config, getContext().getSelf()));

                    Map<String, List<ScrapeParam>> chatScrapingConfigs = msg.chatScrapingConfigs.stream()
                            .collect(Collectors.toMap(ChatScrapingConfig::getChatId, ChatScrapingConfig::getScrapingParams));

                    Map<String, ActorRef<WebSiteActor.Command>> websites = msg.getConfig().getWebSiteConfigs().stream()
                            .map(site -> {
                                Behavior<WebSiteActor.Command> websiteBehavior = Behaviors.supervise(WebSiteActor.create()).onFailure(SupervisorStrategy.restart());  // resume = ignore the crash
                                ActorRef<WebSiteActor.Command> website =  getContext().spawn(websiteBehavior, site.getName());
                                website.tell(new WebSiteActor.StartCommand(site.getMinPageNavigationInterval(), site.getMaxPageNavigationInterval()));
                                return website;
                            })
                            .collect(Collectors.toMap(e->e.path().name(), e -> e));

                    return this.running(msg.config, chatScrapingConfigs, websites, bot, db);
                })
                .build();
    }

    private Receive<Command> running(Config config, Map<String, List<ScrapeParam>> chatScrapingConfigs, Map<String, ActorRef<WebSiteActor.Command>> websites, ActorRef<BotActor.Command> bot, ActorRef<DatabaseActor.Command> db) {
        return newReceiveBuilder()
                .onSignal(Terminated.class, handler -> {
                    getContext().getLog().error("Actor terminated: " + handler.getRef().path().name());
                    String chatId =  handler.getRef().path().name().split("_")[1];
                    Integer actualScrapersCounter = currentChatScrapersCounter.getOrDefault(chatId, 0);
                    if (actualScrapersCounter > 0) {
                        actualScrapersCounter = actualScrapersCounter - 1;
                        currentChatScrapersCounter.put(chatId, actualScrapersCounter);
                    }
                    if (actualScrapersCounter == 0) {
                        currentChatScrapers.remove(chatId);
                    }
                    return Behaviors.same();
                })
                .onMessage(ChatCommand.class, msg -> {
                    if (!isValid(msg.update)) {
                        return Behaviors.same();
                    }
                    User user = msg.update.getMessage().getFrom();
                    String msgtext = msg.update.getMessage().getText();
                    String chatId = String.valueOf(msg.update.getMessage().getChatId());
                    Long chatId2 = msg.update.getMessage().getChat().getId();

                    getContext().getLog().info("[MSG] {} [CHAT_ID] {}", msgtext, chatId);

                    if (!config.getUserId().contains(user.getId())) {
                        return Behaviors.same();
                    }
                    if (msgtext == null) {
                        return Behaviors.same();
                    }
                    switch (msgtext) {
                        case "start":
                            handleStart(chatId, chatScrapingConfigs, websites, bot);
                            break;
                        case "stop":
                            handleStop(chatId, bot);
                            break;
                        case "ping":
                            handlePing(chatId, bot);
                            break;
                        default:
                    }
                    return Behaviors.same();
                })
                .onMessage(LinksCommand.class, msg -> {
                    db.tell(new DatabaseActor.GetHousesCommand(msg.webSiteName));
                    return checkNewHouses(config, chatScrapingConfigs, websites, bot, db, msg);
                })
                .build();
    }

    private Receive<Command> checkNewHouses(Config config, Map<String, List<ScrapeParam>> chatScrapingConfigs, Map<String, ActorRef<WebSiteActor.Command>> websites, ActorRef<BotActor.Command> bot, ActorRef<DatabaseActor.Command> db, LinksCommand linksMsg) {
        return newReceiveBuilder()
                .onMessage(HousesCommand.class, msg -> {
                    List<String> houses = msg.houses.stream()
                            .filter(house -> house.getChatId().equals(linksMsg.chatId))
                            .map(House::getLink).collect(Collectors.toList());

                    List<House> newHouses = linksMsg.links.stream()
                            .filter(link -> !houses.contains(link))
                            .peek(e -> getContext().getLog().info("[NEW LINK] {}", e))
                            .map(link -> this.toHouse(link, linksMsg.webSiteName, linksMsg.chatId))
                            .collect(Collectors.toList());
                    newHouses.forEach(h -> bot.tell(new BotActor.SendMsgCommand(h.getLink(), linksMsg.chatId)));
                    db.tell(new DatabaseActor.SaveHousesCommand(newHouses));
                    return this.running(config, chatScrapingConfigs, websites, bot, db);
                })
                .build();
    }

    private boolean isValid(Update update) {
        return !Objects.isNull(update.getMessage()) && !Objects.isNull(update.getMessage().getFrom());
    }

    private void handleStop(String chatId, ActorRef<BotActor.Command> bot) {

        if (currentChatScrapers.getOrDefault(chatId, new ArrayList<>()).isEmpty()){
            String msg = "Scrapers not running for chatId: " + chatId;
            getContext().getLog().debug(msg);
            bot.tell(new BotActor.SendMsgCommand(msg, chatId));
        }
        currentChatScrapers.getOrDefault(chatId, new ArrayList<>()).forEach(a -> {
            a.tell(new ScraperActor.StopCommand());
        });
    }

    private void handleStart(String chatId, Map<String, List<ScrapeParam>> config, Map<String, ActorRef<WebSiteActor.Command>> websites, ActorRef<BotActor.Command> bot) {

        if (config.getOrDefault(chatId, new ArrayList<>()).isEmpty()){
            String msg = "No config found for chatId: " + chatId;
            getContext().getLog().debug(msg);
            bot.tell(new BotActor.SendMsgCommand(msg, chatId));
            return;
        }

        if (!currentChatScrapers.getOrDefault(chatId, new ArrayList<>()).isEmpty()){

            String msg = currentChatScrapersCounter.get(chatId)+" scrapers running for chatId: " + chatId;
            getContext().getLog().debug(msg);
            bot.tell(new BotActor.SendMsgCommand(msg, chatId));
            return;
        }

        List<ActorRef<ScraperActor.Command>> chatScrapers = new ArrayList<>();

        for (ScrapeParam site : config.get(chatId)) {
            Behavior<ScraperActor.Command> scraperBehavior = Behaviors.supervise(ScraperActor.create())
                    .onFailure(SupervisorStrategy.restart());  // resume = ignore the crash
            ActorRef<ScraperActor.Command> scraper = getContext().spawn(scraperBehavior, site.getName() +"_" + chatId + "_scraper");
            getContext().watch(scraper); // setup supervision for every worker
            chatScrapers.add(scraper);
            currentChatScrapersCounter.put(chatId, currentChatScrapersCounter.getOrDefault(chatId, 0) + 1 );
            ActorRef<WebSiteActor.Command> website = websites.get(site.getName());
            scraper.tell(new ScraperActor.StartCommand(
                    getContext().getSelf(),
                    site,
                    website,
                    chatId));
        }
        currentChatScrapers.put(chatId, chatScrapers);

    }

    private void handlePing(String chatId, ActorRef<BotActor.Command> bot) {
        bot.tell(new BotActor.SendMsgCommand("pong", chatId));
    }

    private House toHouse(String link, String webSiteName, String chatId) {
        House house = new House();
        house.setLink(link);
        house.setWebsite(webSiteName);
        house.setChatId(chatId);
        house.setTimestamp(LocalDateTime.now());
        return house;
    }
}
