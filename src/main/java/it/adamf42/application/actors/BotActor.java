package it.adamf42.application.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.japi.function.Function;
import io.vavr.control.Try;
import it.adamf42.core.domain.ad.Ad;
import it.adamf42.core.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class BotActor extends AbstractBehavior<BotActor.Command> {

    // TODO should consider https://core.telegram.org/bots/faq#:~:text=If%20you%27re%20sending%20bulk,minute%20to%20the%20same%20group.
    private static final long MSG_INTERVAL = 5;

    private enum ChatStatus {
        FREE,
        UPDATE_MIN,

        UPDATE_MAX
    }

    private Map<Long, ChatStatus> chatStatusMap = new HashMap<>();

    private static class TelegramBot extends TelegramLongPollingBot {

        private final ActorRef<BotActor.Command> actor;
        private final Logger logger;

        public TelegramBot(String token, ActorRef<BotActor.Command> actor, Logger logger) {
            super(token);
            this.actor = actor;
            this.logger = logger;
        }

        @Override
        public String getBotUsername() {
            return "TelegramBot";
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
            } else {
                this.actor.tell(new UserInputMsgCommand(msgtext, chatId));
            }
        }

        public void sendMsg(Long chatId, String msg) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(msg);
            try {
                this.execute(sendMessage);
            } catch (TelegramApiException e) {
                logger.error("Unable to send msg {} to chat {}", msg, chatId, e);
            }
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

        private final Ad ad;

        private final Long chatId;

        public SendMsgCommand(Ad ad, Long chatId) {
            this.ad = ad;
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

    @Data
    @AllArgsConstructor
    public static class UserInputMsgCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final String text;

        private final Long chatId;
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
                .onMessage(UserInputMsgCommand.class, onUserInputMsgCommand(bot))
                .build();
    }

    private Receive<Command> idle(TelegramBot bot) {
        getContext().getLog().debug("idle");
        return newReceiveBuilder()
                .onMessage(SendMsgCommand.class, onSendMsgCommandWhileIdle(bot))
                .onMessage(UserMsgCommand.class, onUserMsgCommand())
                .onMessage(UserInputMsgCommand.class, onUserInputMsgCommand(bot))
                .build();
    }

    private Function<StartCommand, Behavior<Command>> onStartCommand() {
        getContext().getLog().info("start");
        return msg -> Behaviors.withTimers(timer -> {
            timer.cancel(TIMER_KEY);
            timer.startTimerAtFixedRate(TIMER_KEY, new ProcessRequestCommand(), Duration.ofSeconds(MSG_INTERVAL));
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            TelegramBot bot = new TelegramBot(msg.getToken(), this.getContext().getSelf(), this.getContext().getLog());
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
            bot.sendMsg(req.getChatId(), req.getAd().getUrl());
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
            User usr = new User();
            usr.setChatId(msg.getChatId());
            getContext().getLog().debug("[onUserMsgCommand] Received {} on chatId {}", msg.getCmd(), msg.chatId);
            switch (msg.getCmd()) {
                case START:
                    this.databaseActor.tell(new DatabaseActor.SaveUserCommand(usr));
                    this.chatStatusMap.put(usr.getChatId(), ChatStatus.FREE);
                    break;
                case MIN:
                    this.chatStatusMap.put(usr.getChatId(), ChatStatus.UPDATE_MIN);
                    break;
                case MAX:
                    this.chatStatusMap.put(usr.getChatId(), ChatStatus.UPDATE_MAX);
                    break;
                default:
                    break;
            }

            return Behaviors.same();
        };
    }

    private Function<UserInputMsgCommand, Behavior<Command>> onUserInputMsgCommand(TelegramBot bot) {
        return msg -> {
            User usr = new User();
            usr.setChatId(msg.getChatId());
            getContext().getLog().debug("[onUserInputMsgCommand] Received {} on chatId {}", msg.getText(), msg.chatId);

            switch (this.chatStatusMap.getOrDefault(msg.getChatId(), ChatStatus.FREE)) {
                case UPDATE_MAX:
                    Try.of(() -> Integer.valueOf(msg.getText()))
                            .onFailure(e -> bot.sendMsg(usr.getChatId(), "Value not valid"))
                            .andThen(usr::setMaxPrice)
                            .andThen(() -> this.databaseActor.tell(new DatabaseActor.UpdateUserCommand(usr)));
                    break;
                case UPDATE_MIN:
                    Try.of(() -> Integer.valueOf(msg.getText()))
                            .onFailure(e -> bot.sendMsg(usr.getChatId(), "Value not valid"))
                            .andThen(usr::setMinPrice)
                            .andThen(() -> this.databaseActor.tell(new DatabaseActor.UpdateUserCommand(usr)));
                    break;
                default:
                    break;
            }
            this.chatStatusMap.put(usr.getChatId(), ChatStatus.FREE);

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