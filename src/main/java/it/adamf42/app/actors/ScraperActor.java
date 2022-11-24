package it.adamf42.app.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import it.adamf42.core.domain.ScrapeParam;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import it.adamf42.app.util.RandomInterval;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ScraperActor extends AbstractBehavior<ScraperActor.Command> {

    private Object TIMER_KEY;

    private ScraperActor(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ScraperActor::new);
    }

    public interface Command extends Serializable {
    }

    public static class StartCommand implements Command {

        private static final long serialVersionUID = 1L;
        private final ActorRef<ManagerActor.Command> manager;

        private final ActorRef<WebSiteActor.Command> website;
        private final ScrapeParam config;

        private final String chatId;

        public StartCommand(ActorRef<ManagerActor.Command> manager, ScrapeParam config, ActorRef<WebSiteActor.Command> website, String chatId) {
            this.manager = manager;
            this.config = config;
            this.chatId = chatId;
            this.website = website;
        }

        public ActorRef<WebSiteActor.Command> getWebsite() {
            return this.website;
        }
    }

    public static class ScrapeCommand implements Command {
        private static final long serialVersionUID = 1L;
    }

    public static class StopCommand implements Command {
        private static final long serialVersionUID = 1L;
    }


    public static class HtmlCommand implements Command {
        private static final long serialVersionUID = 1L;
        private final String html;

        public HtmlCommand(String html) {
            this.html = html;
        }

        public String getHtml() {
            return html;
        }
    }


    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, msg -> Behaviors.withTimers(timer -> {
                            timer.cancel(TIMER_KEY);
                            timer.startTimerAtFixedRate(TIMER_KEY, new ScrapeCommand(), Duration.ofSeconds(1));
                            return startIntervalScraping(msg);
                        })
                )
                .build();
    }

    private Receive<ScraperActor.Command> startIntervalScraping(StartCommand startMsg) {
        RandomInterval scrapingInterval = new RandomInterval(startMsg.config.getMinParsingInterval(), startMsg.config.getMaxParsingInterval());
        return newReceiveBuilder()
                .onMessage(StopCommand.class, msg -> {
                    getContext().getLog().info("Received StopCommand");
                    return Behaviors.stopped();
                })
                .onMessage(ScrapeCommand.class, msg -> Behaviors.withTimers(timer -> {
                            long newInterval = scrapingInterval.getInterval();
                            getContext().getLog().debug(newInterval + " interval for " + startMsg.config.getUrl());
                            timer.cancel(TIMER_KEY);
                            timer.startTimerAtFixedRate(TIMER_KEY, new ScrapeCommand(), Duration.ofSeconds(newInterval));
                            startMsg.website.tell(new WebSiteActor.RequestCommand(getContext().getSelf(), startMsg.config.getUrl()));
                            return scrapeWebSite(startMsg, new HashSet<>());
                        })
                )
                .build();
    }

    private Receive<ScraperActor.Command> scrapeWebSite(StartCommand startMsg, Set<String> links) {
        return newReceiveBuilder()
                .onMessage(HtmlCommand.class, msg -> {
                    Document doc = Jsoup.parse(msg.getHtml());
                    links.addAll(getLinks(doc, startMsg.config.getLinksSelector(), startMsg.config.getBaseUrl()));
                    if (hasNextPage(doc, startMsg.config.getNextPageSelector())) {
                        startMsg.website.tell(new WebSiteActor.RequestCommand(getContext().getSelf(), getNextPageUrl(doc, startMsg.config.getNextPageSelector())));
                        return scrapeWebSite(startMsg, links);
                    } else {
                        getContext().getLog().debug("[LINKS] " + links.size() + " [URL]: " + startMsg.config.getUrl());
                        startMsg.manager.tell(new ManagerActor.LinksCommand(new ArrayList<>(links), startMsg.config.getName(), startMsg.chatId));
                        return startIntervalScraping(startMsg);
                    }
                })
                .build();
    }


    private List<String> getLinks(Document document, String linksSelector, String baseUrl) {
        return document.select(linksSelector).stream()
                .map(e -> baseUrl + e.attributes().get("href")) //
                .collect(Collectors.toList());
    }

    public boolean hasNextPage(Document document, String nextPageSelector) {
        return document.select(nextPageSelector).stream().findFirst().isPresent();
    }

    public String getNextPageUrl(Document document, String nextPageSelector) {
        Elements elements = document.select(nextPageSelector);
        return elements.stream()
                .map(e -> e.attributes().get("href")) //
                .findFirst().orElseThrow();
    }

}
