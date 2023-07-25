package it.adamf42.app.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import it.adamf42.app.util.RandomInterval;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;

public class WebSiteActor extends AbstractBehavior<WebSiteActor.Command> {

    private Queue<RequestCommand> currentRequests = new LinkedList<>();
    private Object TIMER_KEY;

    private WebSiteActor(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<WebSiteActor.Command> create() {
        return Behaviors.setup(WebSiteActor::new);
    }


    public interface Command extends Serializable {
    }

    public static class StartCommand implements Command {
        private static final long serialVersionUID = 1L;
        private final Integer minPageNavigationInterval;
        private final Integer maxPageNavigationInterval;


        public StartCommand(Integer minPageNavigationInterval, Integer maxPageNavigationInterval) {
            this.minPageNavigationInterval = minPageNavigationInterval;
            this.maxPageNavigationInterval = maxPageNavigationInterval;
        }
    }

    public static class ProcessRequestCommand implements Command {
        private static final long serialVersionUID = 1L;
    }

    public static class RequestCommand implements Command {
        private static final long serialVersionUID = 1L;
        private final ActorRef<ScraperActor.Command> actor;
        private final String req;

        public RequestCommand(ActorRef<ScraperActor.Command> actor, String req) {
            this.actor = actor;
            this.req = req;
        }

        public ActorRef<ScraperActor.Command> getActor() {
            return actor;
        }

        public String getReq() {
            return req;
        }
    }


    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, msg -> Behaviors.withTimers(timer -> {
                            timer.cancel(TIMER_KEY);
                            timer.startTimerAtFixedRate(TIMER_KEY, new ProcessRequestCommand(), Duration.ofSeconds(1));
                            return processRequests(msg);
                        })
                )
                .build();
    }

    private Receive<Command> processRequests(StartCommand startMsg) {
        getContext().getLog().debug("processRequests");
        RandomInterval navigationInterval = new RandomInterval(startMsg.minPageNavigationInterval, startMsg.maxPageNavigationInterval);
        return newReceiveBuilder()
                .onMessage(ProcessRequestCommand.class, msg -> Behaviors.withTimers(timer -> {
                        timer.cancel(TIMER_KEY);
                        if (currentRequests.isEmpty()) {
                            return idle(startMsg);
                        }
                        RequestCommand req = currentRequests.remove();
                        getContext().getLog().debug("ProcessRequestCommand: " + req.getReq());
                        Document doc = getDocument(req.getReq());
                        req.getActor().tell(new ScraperActor.HtmlCommand(doc.html()));
                        long newInterval = navigationInterval.getInterval();
                        getContext().getLog().debug(newInterval + " interval for " + getContext().getSelf().path().name());
                        timer.startTimerAtFixedRate(TIMER_KEY, new ProcessRequestCommand(), Duration.ofSeconds(newInterval));
                        return Behaviors.same();
                    })
                )
                .onMessage(RequestCommand.class, msg -> {
                    currentRequests.add(msg);
                    return Behaviors.same();
                })
                .build();
    }

    private Receive<Command> idle(StartCommand startMsg) {
        getContext().getLog().debug("idle");
        return newReceiveBuilder()
                .onMessage(RequestCommand.class, msg -> {
                    currentRequests.add(msg);
                    getContext().getSelf().tell(new ProcessRequestCommand());
                    return processRequests(startMsg);
                })
                .build();
    }

    private Connection getConnection(String url) {
        return Jsoup.connect(url)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3")
                .referrer("http://www.google.com")
                .timeout(30000)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.114 Safari/537.36");
    }

    private Document getDocument(String url) throws IOException {
        return getConnection(url).get();
    }


}
