package finder;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.time.Duration;
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

        private final String url;

        protected final String baseUrl;
        protected final String linksSelector;
        protected final String nextPageSelector;

        private final ActorRef<WebSiteActor.Command> website;

        public StartCommand(String url, String baseUrl, String linksSelector, String nextPageSelector, ActorRef<WebSiteActor.Command> website) {
            this.url = url;
            this.baseUrl = baseUrl;
            this.linksSelector = linksSelector;
            this.nextPageSelector = nextPageSelector;
            this.website = website;
        }

        public String getUrl() {
            return url;
        }

        public ActorRef<WebSiteActor.Command> getWebsite() {
            return this.website;
        }
    }

    public static class ScrapeCommand implements Command {
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
                            timer.startTimerAtFixedRate(TIMER_KEY, new ScrapeCommand(), Duration.ofSeconds(1));
                            return startIntervalScraping(msg.getUrl(),msg.nextPageSelector, msg.linksSelector, msg.baseUrl, msg.getWebsite());
                        })
                )
                .build();
    }

    private Receive<ScraperActor.Command> startIntervalScraping(String url, String nextPageSelector, String linksSelector, String baseUrl, ActorRef<WebSiteActor.Command> website) {
        return newReceiveBuilder()
                .onMessage(ScrapeCommand.class, msg -> {
                    website.tell(new WebSiteActor.RequestCommand(getContext().getSelf(), url));
                    return scrapeWebSite(url, nextPageSelector, linksSelector, baseUrl, website, new HashSet<>());
                })
                .build();
    }


    private Receive<ScraperActor.Command> scrapeWebSite(String url, String nextPageSelector, String linksSelector, String baseUrl, ActorRef<WebSiteActor.Command> website, Set<String> links) {
        return newReceiveBuilder()
                .onMessage(HtmlCommand.class, msg -> {
                    Document doc = Jsoup.parse(msg.getHtml());
                    links.addAll(getLinks(doc, linksSelector, baseUrl));
                    if (hasNextPage(doc, nextPageSelector)) {
                        website.tell(new WebSiteActor.RequestCommand(getContext().getSelf(), getNextPageUrl(doc, nextPageSelector)));
                        return scrapeWebSite(url, nextPageSelector, linksSelector, baseUrl, website, links);
                    } else {
                        getContext().getLog().debug("[LINKS] " +  links.size() + " [URL]: " + url);
                        return startIntervalScraping(url, nextPageSelector, linksSelector, baseUrl, website);
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

    private Connection getConnection(String url) {
        return Jsoup.connect(url)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3")
                .referrer("http://www.google.com")
                .timeout(30000)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.114 Safari/537.36");
    }

}
