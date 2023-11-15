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
import java.util.Map;

public class ChatManagerActor extends AbstractBehavior<ChatManagerActor.Command> {
    public interface Command extends Serializable {
    }

    public static class StartCommand implements Command {
        private static final long serialVersionUID = 1L;

        @Getter
        private final ActorRef<BotActor.Command> bot;

        public StartCommand(ActorRef<BotActor.Command> bot) {
            this.bot = bot;
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


    private ChatManagerActor(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<ChatManagerActor.Command> create() {
        return Behaviors.setup(ChatManagerActor::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, msg -> running(msg.bot, new HashMap<>()))
                .build();
    }

    private Receive<Command> running(ActorRef<BotActor.Command> bot, Map<Long, ActorRef<ChatActor.Command>> mem) {
        return newReceiveBuilder()
                .onMessage(NewChatCommand.class, msg -> {
                    mem.computeIfAbsent(msg.getChat().getChatId(), k -> {
                        Behavior<ChatActor.Command> dbBehavior =
                                Behaviors.supervise(ChatActor.create(bot, msg.getChat())).onFailure(SupervisorStrategy.resume());
                        ActorRef<ChatActor.Command> chat = getContext().spawn(dbBehavior, "chat" + msg.getChat().getChatId());
                        getContext().watch(chat);
                        return chat;
                    });
                    return Behaviors.same();
                })
                .onMessage(UpdateChatCommand.class, msg -> {
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

