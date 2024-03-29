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
import it.adamf42.core.domain.chat.Chat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class BotActor extends AbstractBehavior<BotActor.Command> {

    // TODO should consider https://core.telegram.org/bots/faq#:~:text=If%20you%27re%20sending%20bulk,minute%20to%20the%20same%20group.
    private static final long MSG_INTERVAL = 50;
    private final Map<Long, ChatStatus> chatStatusMap = new HashMap<>();
    private final Queue<SendMsgChatCommand> currentRequests = new LinkedList<>();
    private final ActorRef<DatabaseActor.Command> databaseActor;
    private final ActorRef<ChatManagerActor.Command> chatManagerActor;
    private Object TIMER_KEY;
    private enum ChatStatus {
        FREE,
        UPDATE_MIN,
        UPDATE_MAX
    }

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

            if (MsgFromChatCommand.CommandType.isValidCommand(msgtext)) {
                this.actor.tell(new MsgFromChatCommand(MsgFromChatCommand.CommandType.fromString(getStringBeforeAt(msgtext)), chatId));
            } else {
                this.actor.tell(new UserInputMsgCommand(msgtext, chatId));
            }
        }

        private static String getStringBeforeAt(String input) {
            int atIndex = input.indexOf('@');
            return atIndex >= 0 ? input.substring(0, atIndex) : input;
        }

        public void sendMsg(Long chatId, String msg) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(msg);
            sendMessage.enableHtml(true);
            try {
                this.execute(sendMessage);
            } catch (TelegramApiRequestException e) {
                if (Integer.valueOf(403).equals(e.getErrorCode())){
                    logger.debug("Received {} from chat {} removing...", e.getErrorCode(), chatId);
                    this.actor.tell(new DisableChatCommand(chatId));

                } else  {
                    logger.error("Unable to send msg {} to chat {}", msg, chatId, e);
                }
            } catch (TelegramApiException e) {
                logger.error("Unable to send msg {} to chat {}", msg, chatId, e);
            }
        }

        @Override
        public void onClosing() {
            this.exe.shutdownNow();
        }
    }

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
    public static class SendAdToChatCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final Ad ad;

        private final Long chatId;

        public SendAdToChatCommand(Ad ad, Long chatId) {
            this.ad = ad;
            this.chatId = chatId;
        }

    }

    @Data
    public static class SendMsgChatCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final String text;

        private final Long chatId;

        public SendMsgChatCommand(String text, Long chatId) {
            this.text = text;
            this.chatId = chatId;
        }

    }

    @Data
    public static class GetChatResponseCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final Chat chat;

        private final Long chatId;

        public GetChatResponseCommand(Chat chat, Long chatId) {
            this.chat = chat;
            this.chatId = chatId;
        }

    }


    @Data
    public static class MsgFromChatCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final CommandType cmd;

        private final Long chatId;

        @Getter
        public enum CommandType {
            START("/start"),
            MAX("/max"),
            MIN("/min"),
            INFO("/info");

            private final String commandString;

            CommandType(String commandString) {
                this.commandString = commandString;
            }

            // Function to verify if a string matches one of the command patterns
            public static boolean isValidCommand(String input) {
                for (CommandType commandType : CommandType.values()) {
                    if (input.equals(commandType.getCommandString()) || input.startsWith(commandType.getCommandString() + "@" )) {
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

        public MsgFromChatCommand(CommandType cmd, Long chatId) {
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

    @Data
    @AllArgsConstructor
    public static class DisableChatCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final Long chatId;
    }

    public static class ProcessRequestCommand implements Command {
        private static final long serialVersionUID = 1L;
    }

    public BotActor(ActorContext<Command> context, ActorRef<DatabaseActor.Command> databaseActor, ActorRef<ChatManagerActor.Command> chatManagerActor) {
        super(context);
        this.databaseActor = databaseActor;
        this.chatManagerActor = chatManagerActor;
    }

    public static Behavior<BotActor.Command> create(ActorRef<DatabaseActor.Command> databaseActor, ActorRef<ChatManagerActor.Command> chatManagerActor) {
        return Behaviors.setup(context -> new BotActor(context, databaseActor, chatManagerActor));
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
                .onMessage(SendAdToChatCommand.class, onSendAdToChatCommand())
                .onMessage(SendMsgChatCommand.class, onSendMsgChatCommand())
                .onMessage(GetChatResponseCommand.class, onGetChatResponseCommand())
                .onMessage(ProcessRequestCommand.class, onProcessRequestCommand(bot))
                .onMessage(MsgFromChatCommand.class, onUserMsgCommand())
                .onMessage(UserInputMsgCommand.class, onUserInputMsgCommand(bot))
                .onMessage(DisableChatCommand.class, msg -> {
                    Chat toBeUpdatedChat = new Chat();
                    toBeUpdatedChat.setIsActive(false);
                    toBeUpdatedChat.setChatId(msg.getChatId());
                    this.chatManagerActor.tell(new ChatManagerActor.UpdateChatCommand(toBeUpdatedChat)); // TODO: understand how
                    this.databaseActor.tell(new DatabaseActor.UpdateChatCommand(toBeUpdatedChat));
                    return running(bot);
                })
                .build();
    }

    private Function<StartCommand, Behavior<Command>> onStartCommand() {
        getContext().getLog().info("start");
        return msg -> Behaviors.withTimers(timer -> {
            timer.cancel(TIMER_KEY);
            timer.startTimerAtFixedRate(TIMER_KEY, new ProcessRequestCommand(), Duration.ofMillis(MSG_INTERVAL));
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            TelegramBot bot = new TelegramBot(msg.getToken(), this.getContext().getSelf(), this.getContext().getLog());
            api.registerBot(bot);
            return running(bot);
        });
    }

    private Function<ProcessRequestCommand, Behavior<Command>> onProcessRequestCommand(TelegramBot bot) {
        return msg -> {
            if (currentRequests.isEmpty()) {
                return Behaviors.same();
            }
            SendMsgChatCommand req = currentRequests.remove();
            bot.sendMsg(req.getChatId(), req.getText());
            return Behaviors.same();
        };
    }

    private Function<SendAdToChatCommand, Behavior<Command>> onSendAdToChatCommand() {
        return msg -> {
            currentRequests.add(new SendMsgChatCommand(convertAdToHtml(msg.getAd()), msg.getChatId()));
            return Behaviors.same();
        };
    }

    private Function<SendMsgChatCommand, Behavior<Command>> onSendMsgChatCommand() {
        return msg -> {
            currentRequests.add(msg);
            return Behaviors.same();
        };
    }

    private Function<GetChatResponseCommand, Behavior<Command>> onGetChatResponseCommand() {
        return msg -> {
            Chat chat = msg.getChat();
            String newLine = System.getProperty("line.separator");
            String text = "<b>ChatID:</b> " + chat.getChatId() +
                    newLine +
                    "<b>Max price:</b> " + chat.getMaxPrice() +
                    newLine +
                    "<b>Min price:</b> " + chat.getMinPrice() +
                    newLine;
            currentRequests.add(new SendMsgChatCommand(text, msg.getChatId()));
            return Behaviors.same();
        };
    }

    private Function<MsgFromChatCommand, Behavior<Command>> onUserMsgCommand() {
        return msg -> {
            Chat chat = new Chat();
            chat.setChatId(msg.getChatId());
            getContext().getLog().debug("[onUserMsgCommand] Received {} on chatId {}", msg.getCmd(), msg.chatId);
            switch (msg.getCmd()) {
                case START:
                    chat.setIsActive(true);
                    this.databaseActor.tell(new DatabaseActor.SaveChatCommand(chat));
                    this.chatManagerActor.tell(new ChatManagerActor.NewChatCommand(chat));
                    this.chatStatusMap.put(chat.getChatId(), ChatStatus.FREE);
                    break;
                case INFO:
                    this.databaseActor.tell(new DatabaseActor.GetChatCommand(chat.getChatId(), this.getContext().getSelf()));
                    this.chatStatusMap.put(chat.getChatId(), ChatStatus.FREE);
                    break;
                case MIN:
                    this.chatStatusMap.put(chat.getChatId(), ChatStatus.UPDATE_MIN);
                    break;
                case MAX:
                    this.chatStatusMap.put(chat.getChatId(), ChatStatus.UPDATE_MAX);
                    break;
                default:
                    break;
            }

            return Behaviors.same();
        };
    }

    private Function<UserInputMsgCommand, Behavior<Command>> onUserInputMsgCommand(TelegramBot bot) {
        return msg -> {
            Chat chat = new Chat();
            chat.setChatId(msg.getChatId());
            getContext().getLog().debug("[onUserInputMsgCommand] Received {} on chatId {}", msg.getText(), msg.chatId);

            switch (this.chatStatusMap.getOrDefault(msg.getChatId(), ChatStatus.FREE)) {
                case UPDATE_MAX:
                    Try.of(() -> Integer.valueOf(msg.getText()))
                            .onFailure(e -> bot.sendMsg(chat.getChatId(), "Value not valid"))
                            .andThen(chat::setMaxPrice)
                            .andThen(() -> {
                                this.chatManagerActor.tell(new ChatManagerActor.UpdateChatCommand(chat)); // TODO: understand how
                                this.databaseActor.tell(new DatabaseActor.UpdateChatCommand(chat));
                            });
                    break;
                case UPDATE_MIN:
                    Try.of(() -> Integer.valueOf(msg.getText()))
                            .onFailure(e -> bot.sendMsg(chat.getChatId(), "Value not valid"))
                            .andThen(chat::setMinPrice)
                            .andThen(() -> {
                                this.chatManagerActor.tell(new ChatManagerActor.UpdateChatCommand(chat)); // TODO: understand how
                                this.databaseActor.tell(new DatabaseActor.UpdateChatCommand(chat));
                            });
                    break;
                default:
                    break;
            }
            this.chatStatusMap.put(chat.getChatId(), ChatStatus.FREE);

            return Behaviors.same();
        };
    }

    private static String convertAdToHtml(Ad ad) {
        StringBuilder htmlBuilder = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        if (ad.getTitle() != null) {
            htmlBuilder.append("<b>\uD83C\uDFE0 ").append(ad.getTitle()).append("</b>").append(newLine).append(newLine);
        }

        appendIfNotNull(htmlBuilder, "\uD83D\uDCCD", ad.getCity(), "");
        appendIfNotNull(htmlBuilder, ", ", ad.getArea(), "");
        appendIfNotNull(htmlBuilder, ", ", ad.getStreet(), "");
        htmlBuilder.append(newLine);
        htmlBuilder.append(newLine);

        appendIfNotNull(htmlBuilder, "Superficie:", ad.getSquareMeters(), "m² ");
        appendIfNotNull(htmlBuilder, "| Piano:", ad.getFloor(), "°" + newLine);
        appendIfNotNull(htmlBuilder, "| Camere:", ad.getRooms(), " ");
        appendIfNotNull(htmlBuilder, "| Classificazione energetica:", ad.getEnergyRating(), "");
        htmlBuilder.append(newLine);
        htmlBuilder.append(newLine);

        appendIfNotNull(htmlBuilder, "\uD83D\uDCB0 Prezzo:", ad.getPrice(), "€ ");
        appendIfNotNull(htmlBuilder, "| Condominio:", ad.getCondominiumFees(), "€ ");
        appendIfNotNull(htmlBuilder, "| Caparra:", ad.getBail(), "€ ");
        htmlBuilder.append(newLine);
        htmlBuilder.append(newLine);

        appendIfNotNull(htmlBuilder, " \uD83D\uDD11 Agenzia:", ad.getPublisher(), " ");
        htmlBuilder.append(newLine);
        htmlBuilder.append(newLine);

        if (ad.getUrl() != null) {
            htmlBuilder.append("\uD83D\uDC49  <a href=\"").append(ad.getUrl()).append("\"><u>Link Dettagli</u></a>");
        }

        return htmlBuilder.toString();
    }

    private static void appendIfNotNull(StringBuilder builder, String label, Object value, String newLine) {
        if (value != null) {
            builder.append("<b>").append(label).append("</b> ").append(value).append(newLine);
        }
    }

}
