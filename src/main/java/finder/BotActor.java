package finder;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import config.pojo.Config;
import model.TelegramBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.Serializable;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;

public class BotActor extends AbstractBehavior<BotActor.Command> {

    private Queue<SendMsgCommand> currentRequests = new LinkedList<>();

    private Object TIMER_KEY;

    public interface Command extends Serializable {
    }

    public static class StartCommand implements Command {

        private static final long serialVersionUID = 1L;
        private final Config config;

        private final ActorRef<ManagerActor.Command> manager;

        public StartCommand(Config config, ActorRef<ManagerActor.Command> manager) {
            this.config = config;
            this.manager = manager;
        }
    }

    public static class SendMsgCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final String text;

        private final String chatId;


        public SendMsgCommand(String text, String chatId) {
            this.text = text;
            this.chatId = chatId;
        }

        public String getText() {
            return text;
        }

        public String getChatId() {
            return chatId;
        }
    }

    public static class ProcessRequestCommand implements Command {
        private static final long serialVersionUID = 1L;
    }

    public BotActor(ActorContext<Command> context) {
        super(context);
    }


    public static Behavior<BotActor.Command> create() {
        return Behaviors.setup(BotActor::new);
    }


    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, msg -> Behaviors.withTimers(timer -> {
                            timer.cancel(TIMER_KEY);
                            timer.startTimerAtFixedRate(TIMER_KEY, new ProcessRequestCommand(), Duration.ofSeconds(2)); // TODO: should be configurable
                            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
                            TelegramBot bot = new TelegramBot(msg.config, getContext().getLog(), u -> msg.manager.tell(new ManagerActor.ChatCommand(u)));
                            BotSession session = api.registerBot(bot);
                            getContext().getLog().info("Bot started");
                            return running(bot, session);
                        })
                )
                .build();
    }

    private Receive<Command> running(TelegramBot bot, BotSession session) {
        return newReceiveBuilder()
                .onMessage(SendMsgCommand.class, msg -> {
                    currentRequests.add(msg);
                    return Behaviors.same();
                })
                .onMessage(ProcessRequestCommand.class, msg -> {
                    if (!currentRequests.isEmpty()) {
                        SendMsgCommand req = currentRequests.remove();
                        bot.sendMsg(req.getChatId(), req.getText());
                    } else {
                        //TODO: create an idle behaviour
                    }
                    return Behaviors.same();
                })
                .build();
    }
}
