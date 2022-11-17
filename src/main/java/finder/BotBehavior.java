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
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.Serializable;
import java.util.Objects;

public class BotBehavior extends AbstractBehavior<BotBehavior.Command> {


    private boolean isValid(Update update) {
        return !Objects.isNull(update.getMessage()) && !Objects.isNull(update.getMessage().getFrom());
    }

    private void handleStop() {

    }

    private void handleStart() {

    }

    private void handlePing(String chatId) {

    }

    public interface Command extends Serializable {
    }

    public static class StartCommand implements BotBehavior.Command {

        private static final long serialVersionUID = 1L;
        private final Config config;

        private final ActorRef<ManagerBehavior.Command> manager;

        public StartCommand(Config config, ActorRef<ManagerBehavior.Command> manager) {
            this.config = config;
            this.manager = manager;
        }
    }

    public static class SendMsgCommand implements BotBehavior.Command {

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

    public BotBehavior(ActorContext<Command> context) {
        super(context);
    }


    public static Behavior<BotBehavior.Command> create() {
        return Behaviors.setup(BotBehavior::new);
    }


    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, msg -> {
                    TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
                    TelegramBot bot = new TelegramBot(msg.config, getContext().getLog(), u -> msg.manager.tell(new ManagerBehavior.ChatCommand(u)));
                    api.registerBot(bot);
                    getContext().getLog().info("Bot started");
                    return running(bot);
                })
                .build();
    }

    private Receive<Command> running(TelegramBot bot) {
        return newReceiveBuilder()
                .onMessage(SendMsgCommand.class, msg -> {
                    bot.sendMsg(msg.getChatId(), msg.getText());
                    return Behaviors.same();
                })
                .build();
    }
}
