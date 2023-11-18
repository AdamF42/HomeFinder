package it.adamf42.application.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import it.adamf42.core.domain.ad.Ad;
import it.adamf42.core.domain.chat.Chat;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatManagerActor extends AbstractBehavior<ChatManagerActor.Command> {
    public interface Command extends Serializable {
    }

    public static class StartCommand implements Command {
        private static final long serialVersionUID = 1L;

        @Getter
        private final ActorRef<BotActor.Command> bot;

        @Getter
        private final ActorRef<DatabaseActor.Command> db;

        public StartCommand(ActorRef<BotActor.Command> bot, ActorRef<DatabaseActor.Command> db) {
            this.bot = bot;
            this.db = db;
        }
    }

    public static class NewAdCommand implements Command {
        private static final long serialVersionUID = 1L;

        @Getter
        private final Ad ad;

        public NewAdCommand(Ad ad) {
            this.ad = ad;
        }
    }

    public static class NewChatCommand implements Command {
        private static final long serialVersionUID = 1L;

        @Getter
        private final Chat chat;

        public NewChatCommand(Chat chat) {
            this.chat = chat;
        }
    }

    public static class UpdateChatCommand implements Command {
        private static final long serialVersionUID = 1L;

        @Getter
        private final Chat chat;

        public UpdateChatCommand(Chat chat) {
            this.chat = chat;
        }
    }


    public static class AllChatsCommand implements Command {
        private static final long serialVersionUID = 1L;

        @Getter
        private final List<Chat> chats;

        public AllChatsCommand(List<Chat> chats) {
            this.chats = chats;
        }
    }


    private ChatManagerActor(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<ChatManagerActor.Command> create() {
        return Behaviors.setup(ChatManagerActor::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, msg -> {
                    msg.db.tell(new DatabaseActor.GetAllChatCommand(this.getContext().getSelf()));
                    return setup(msg.getBot());
                })
                .build();
    }

    private Receive<Command> setup(ActorRef<BotActor.Command> bot) {
        return newReceiveBuilder()
                .onMessage(AllChatsCommand.class, msg -> {
                    Map<Long, ActorRef<ChatActor.Command>> mem = new HashMap<>();
                    msg.getChats().stream().filter(c -> Boolean.TRUE.equals(c.getIsActive())).forEach(c -> {
                        Behavior<ChatActor.Command> dbBehavior =
                                Behaviors.supervise(ChatActor.create(bot, c)).onFailure(SupervisorStrategy.resume());
                        ActorRef<ChatActor.Command> chat = getContext().spawn(dbBehavior, "chat" + c.getChatId());
                        getContext().watch(chat);
                        mem.put(c.getChatId(), chat);
                    });
                    return running(bot, mem);
                })
                .build();
    }


    private Receive<Command> running(ActorRef<BotActor.Command> bot, Map<Long, ActorRef<ChatActor.Command>> mem) {
        return newReceiveBuilder()
                .onMessage(NewChatCommand.class, msg -> {
                    mem.computeIfAbsent(msg.getChat().getChatId(), k -> {
                        Behavior<ChatActor.Command> chatBehavior =
                                Behaviors.supervise(ChatActor.create(bot, msg.getChat())).onFailure(SupervisorStrategy.resume());
                        ActorRef<ChatActor.Command> chat = getContext().spawn(chatBehavior, "chat" + msg.getChat().getChatId());
                        getContext().watch(chat);
                        return chat;
                    });
                    return Behaviors.same();
                })
                .onMessage(UpdateChatCommand.class, msg -> {
                    if (Boolean.FALSE.equals(msg.getChat().getIsActive())){
                        mem.remove(msg.getChat().getChatId());
                    }
                    mem.computeIfPresent(msg.getChat().getChatId(), (k, a) -> {
                        a.tell(new ChatActor.UpdateChatCommand(msg.getChat()));
                        return a;
                    });
                    return Behaviors.same();
                })
                .onMessage(NewAdCommand.class, msg -> {
                    mem.keySet().forEach(k -> mem.get(k).tell(new ChatActor.NewAdCommand(msg.getAd())));
                    return Behaviors.same();
                })
                .build();
    }

}

