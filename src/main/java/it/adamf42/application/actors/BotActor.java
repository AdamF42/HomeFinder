package it.adamf42.application.actors;

import java.io.Serializable;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import it.adamf42.core.domain.ad.Ad;
import it.adamf42.core.domain.user.User;

import lombok.Data;

public class BotActor extends AbstractBehavior<BotActor.Command> {

    // TODO should consider https://core.telegram.org/bots/faq#:~:text=If%20you%27re%20sending%20bulk,minute%20to%20the%20same%20group.
    private static final long MSG_INTERVAL = 5;

    private static class TelegramBot extends TelegramLongPollingBot {

        private final ActorRef<BotActor.Command> actor;

        public TelegramBot(ActorRef<BotActor.Command> actor) {
            this.actor = actor;
        }

        @Override
        public String getBotUsername() {
            return "TelegramBot";
        }

        @Override
        public String getBotToken() {
            return ""; // TODO: inject token
        }

        @Override
        public void onUpdateReceived(Update update) {
            String msgtext = update.getMessage().getText(); //TODO check null
            Long chatId = update.getMessage().getChatId(); //TODO check null

            if(UserMsgCommand.CommandType.isValidCommand(msgtext)) {
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

    public static class StartCommand implements Command {

        private static final long serialVersionUID = 1L;

        public StartCommand() {
        }
    }

    @Data
    public static class SendMsgCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final Ad text;

        private final String chatId;

        public SendMsgCommand(Ad text, String chatId)
        {
            this.text = text;
            this.chatId = chatId;
        }

    }

    @Data
    public static class UserMsgCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final CommandType cmd;

        private final Long chatId;

        public enum CommandType {
            START("/start"),
            MAX("/max"),
            MIN("/min");

            private final String commandString;

            CommandType(String commandString) {
                this.commandString = commandString;
            }

            public String getCommandString() {
                return commandString;
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

        public UserMsgCommand(CommandType cmd, Long chatId)
        {
            this.cmd = cmd;
            this.chatId = chatId;
        }

    }


    public static class ProcessRequestCommand implements Command {
        private static final long serialVersionUID = 1L;
    }

	// TODO: maybe it is better if the actors does not have any dep from their siblings
    public BotActor(ActorContext<Command> context, ActorRef<DatabaseActor.Command> databaseActor) {
        super(context);
		this.databaseActor = databaseActor;
    }

	public static Behavior<BotActor.Command> create(ActorRef<DatabaseActor.Command> databaseActor)
	{
		return Behaviors.setup(context -> new BotActor(context, databaseActor));
	}


    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, msg -> Behaviors.withTimers(timer -> {
                            timer.cancel(TIMER_KEY);
                            timer.startTimerAtFixedRate(TIMER_KEY, new ProcessRequestCommand(), Duration.ofSeconds(MSG_INTERVAL));
                            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
                            TelegramBot bot = new TelegramBot(this.getContext().getSelf());
                            api.registerBot(bot);
                            getContext().getLog().info("Bot started");
                            return running(bot);
                        })
                )
                .build();
    }

    private Receive<Command> running(TelegramBot bot) {
        getContext().getLog().info("running");
        return newReceiveBuilder()
                .onMessage(SendMsgCommand.class, msg -> {
                    currentRequests.add(msg);
                    return Behaviors.same();
                })
                .onMessage(ProcessRequestCommand.class, msg -> {
                    if (currentRequests.isEmpty()) {
                        return idle(bot);
                    }
                    SendMsgCommand req = currentRequests.remove();
                    bot.sendMsg(req.getChatId(), req.getText());
                    return Behaviors.same();
                })
				.onMessage(UserMsgCommand.class, msg -> {
					// TODO: check othe commands
					User usr = new User();
					usr.setChatId(msg.getChatId());
					this.databaseActor.tell(new DatabaseActor.SaveUserCommand(usr));
					return Behaviors.same();
				})
                .build();
    }

    private Receive<Command> idle(TelegramBot bot) {
        getContext().getLog().debug("idle");
        return newReceiveBuilder()
                .onMessage(SendMsgCommand.class, msg -> Behaviors.withTimers(timer -> {
                            timer.cancel(TIMER_KEY);
                            timer.startTimerAtFixedRate(TIMER_KEY, new ProcessRequestCommand(), Duration.ofSeconds(MSG_INTERVAL));
                            currentRequests.add(msg);
                            return running(bot);
                        })
                )
				.onMessage(UserMsgCommand.class, msg -> {
					User usr = new User();
					usr.setChatId(msg.getChatId());
					this.databaseActor.tell(new DatabaseActor.SaveUserCommand(usr));
					return Behaviors.same();
				})
                .build();
    }
}
