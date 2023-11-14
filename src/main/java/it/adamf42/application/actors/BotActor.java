package it.adamf42.application.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.japi.function.Function;
import it.adamf42.core.domain.ad.Ad;
import it.adamf42.core.domain.user.User;
import lombok.Data;
import lombok.Getter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.Serializable;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;

public class BotActor extends AbstractBehavior<BotActor.Command> {

    // TODO should consider https://core.telegram.org/bots/faq#:~:text=If%20you%27re%20sending%20bulk,minute%20to%20the%20same%20group.
    private static final long MSG_INTERVAL = 5;

    private static class TelegramBot extends TelegramLongPollingBot {

        private final ActorRef<BotActor.Command> actor;
        private final String token;

        public TelegramBot(ActorRef<BotActor.Command> actor, String token) {
            this.actor = actor;
            this.token = token;
        }

        @Override
        public String getBotUsername() {
            return "TelegramBot";
        }

        @Override
        public String getBotToken() {
            return token;
        }

        @Override
        public void onUpdateReceived(Update update) {
            if (update == null || update.getMessage() == null || update.getMessage().getText() == null || update.getMessage().getChatId() == null) {
                return;
            }
            String msgtext = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (UserMsgCommand.CommandType.isValidCommand(msgtext)) {
                this.actor.tell(new UserMsgCommand(UserMsgCommand.CommandType.fromString(msgtext), chatId));
            }
        }

        public void sendMsg(String chatId, Ad msg) throws TelegramApiException {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(msg.getUrl());
            this.execute(sendMessage);
        }

        @Override
        public void onClosing() {
            this.exe.shutdownNow();
        }
    }

    private Queue<SendMsgCommand> currentRequests = new LinkedList<>();

    private final ActorRef<DatabaseActor.Command> databaseActor;

    private Object TIMER_KEY;

    public interface Command extends Serializable {
    }

    @Data
    public static class StartCommand implements Command {

        private static final long serialVersionUID = 1L;
        private final String token;

        public StartCommand(String token) {
            this.token = token;
        }
    }

    @Data
    public static class SendMsgCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final Ad text;

        private final String chatId;

        public SendMsgCommand(Ad text, String chatId) {
            this.text = text;
            this.chatId = chatId;
        }

    }

    @Data
    public static class UserMsgCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final CommandType cmd;

        private final Long chatId;

        @Getter
        public enum CommandType {
            START("/start"),
            MAX("/max"),
            MIN("/min");

            private final String commandString;

            CommandType(String commandString) {
                this.commandString = commandString;
            }

            // Function to verify if a string matches one of the command patterns
            public static boolean isValidCommand(String input) {
                for (CommandType commandType : CommandType.values()) {
                    if (input.equals(commandType.getCommandString())) {
                        return true;
                    }
                }
                return false;
            }

            // Function to create CommandType from a string
            public static CommandType fromString(String input) {
                for (CommandType commandType : CommandType.values()) {
                    if (input.equals(commandType.getCommandString())) {
                        return commandType;
                    }
                }
                throw new IllegalArgumentException("Unknown command: " + input);
            }
        }

        public UserMsgCommand(CommandType cmd, Long chatId) {
            this.cmd = cmd;
            this.chatId = chatId;
        }

    }


    public static class ProcessRequestCommand implements Command {
        private static final long serialVersionUID = 1L;
    }

    public BotActor(ActorContext<Command> context, ActorRef<DatabaseActor.Command> databaseActor) {
        super(context);
        this.databaseActor = databaseActor;
    }

    public static Behavior<BotActor.Command> create(ActorRef<DatabaseActor.Command> databaseActor) {
        return Behaviors.setup(context -> new BotActor(context, databaseActor));
    }


    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, onStartCommand())
                .build();
    }

    private Receive<Command> running(TelegramBot bot) {
        getContext().getLog().debug("running");
        return newReceiveBuilder()
                .onMessage(SendMsgCommand.class, onSendMsgCommandWhileRunning())
                .onMessage(ProcessRequestCommand.class, onProcessRequestCommand(bot))
                .onMessage(UserMsgCommand.class, onUserMsgCommand())
                .build();
    }

    private Receive<Command> idle(TelegramBot bot) {
        getContext().getLog().debug("idle");
        return newReceiveBuilder()
                .onMessage(SendMsgCommand.class, onSendMsgCommandWhileIdle(bot))
                .onMessage(UserMsgCommand.class, onUserMsgCommand())
                .build();
    }

    private Function<StartCommand, Behavior<Command>> onStartCommand() {
        getContext().getLog().info("start");
        return msg -> Behaviors.withTimers(timer -> {
            timer.cancel(TIMER_KEY);
            timer.startTimerAtFixedRate(TIMER_KEY, new ProcessRequestCommand(), Duration.ofSeconds(MSG_INTERVAL));
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            TelegramBot bot = new TelegramBot(this.getContext().getSelf(), msg.getToken());
            api.registerBot(bot);
            return running(bot);
        });
    }

    private Function<ProcessRequestCommand, Behavior<Command>> onProcessRequestCommand(TelegramBot bot) {
        return msg -> {
            if (currentRequests.isEmpty()) {
                return idle(bot);
            }
            SendMsgCommand req = currentRequests.remove();
            bot.sendMsg(req.getChatId(), req.getText());
            return Behaviors.same();
        };
    }

    private Function<SendMsgCommand, Behavior<Command>> onSendMsgCommandWhileRunning() {
        return msg -> {
            currentRequests.add(msg);
            return Behaviors.same();
        };
    }

    private Function<UserMsgCommand, Behavior<Command>> onUserMsgCommand() {
        return msg -> {
            switch ( msg.getCmd())
            {
                case START:
                    User usr = new User();
                    usr.setChatId(msg.getChatId());
                    this.databaseActor.tell(new DatabaseActor.SaveUserCommand(usr));
                    break;
                case MIN:
                case MAX:
                    break;
                default:
                    break;
            }

            return Behaviors.same();
        };
    }

    private Function<SendMsgCommand, Behavior<Command>> onSendMsgCommandWhileIdle(TelegramBot bot) {
        return msg -> Behaviors.withTimers(timer -> {
            timer.cancel(TIMER_KEY);
            timer.startTimerAtFixedRate(TIMER_KEY, new ProcessRequestCommand(), Duration.ofSeconds(MSG_INTERVAL));
            currentRequests.add(msg);
            return running(bot);
        });
    }
}
