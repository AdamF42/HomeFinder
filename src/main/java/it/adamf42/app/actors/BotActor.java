package it.adamf42.app.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import it.adamf42.app.repo.config.pojo.Config;
import org.slf4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.Serializable;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class BotActor extends AbstractBehavior<BotActor.Command> {

    private static class TelegramBot extends TelegramLongPollingBot {

        private final Config config;
        private final Consumer<Update> forwardMsg;

        public TelegramBot(Config config, Consumer<Update> forwardMsg) {
            this.config = config;
            this.forwardMsg = forwardMsg;
        }

        @Override
        public String getBotUsername() {
            return "TelegramBot";
        }

        @Override
        public String getBotToken() {
            return this.config.getTelegramToken();
        }

        @Override
        public void onUpdateReceived(Update update) {
            forwardMsg.accept(update);
        }

        public void sendMsg(String chatId, String msg) throws TelegramApiException {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(msg);
            this.execute(sendMessage);
        }

        @Override
        public void onClosing() {
            this.exe.shutdownNow();
        }
    }


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
                            TelegramBot bot = new TelegramBot(msg.config, u -> msg.manager.tell(new ManagerActor.ChatCommand(u)));
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
