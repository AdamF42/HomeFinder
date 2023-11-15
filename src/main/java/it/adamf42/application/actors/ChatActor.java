package it.adamf42.application.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import it.adamf42.core.domain.ad.Ad;
import it.adamf42.core.domain.chat.Chat;
import lombok.Getter;

import java.io.Serializable;
import java.util.function.BiFunction;

import static it.adamf42.application.actors.ChatActor.AdChatValidator.ValidationResult.*;
import static it.adamf42.application.actors.ChatActor.AdChatValidator.isMaxPriceRespected;
import static it.adamf42.application.actors.ChatActor.AdChatValidator.isMinPriceRespected;

public class ChatActor extends AbstractBehavior<ChatActor.Command> {
    public interface Command extends Serializable {
    }

    public static class NewAdCommand implements Command {
        private static final long serialVersionUID = 1L;
        @Getter
        private final Ad ad;

        public NewAdCommand(Ad ad) {
            this.ad = ad;
        }
    }

    private final ActorRef<BotActor.Command> bot;
    private final Chat chat;

    private ChatActor(ActorContext<Command> context, ActorRef<BotActor.Command> bot, Chat chat) {
        super(context);
        this.bot = bot;
        this.chat = chat;
    }

    public static Behavior<ChatActor.Command> create(ActorRef<BotActor.Command> bot, Chat chat) {
        return Behaviors.setup(context -> new ChatActor(context, bot, chat));
    }


    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().onMessage(NewAdCommand.class, msg -> {
            Ad ad = msg.getAd();
            AdChatValidator.ValidationResult res = isMinPriceRespected()
                    .and(isMaxPriceRespected())
                    .apply(ad, chat);

            if (SUCCESS.equals(res)) {
                bot.tell(new BotActor.SendAdToChatCommand(ad, chat.getChatId()));
            }
            return Behaviors.same();
        }).build();
    }

    interface AdChatValidator extends BiFunction<Ad, Chat, AdChatValidator.ValidationResult> {

        static AdChatValidator isMinPriceRespected() {
            return (ad, chat) -> {
                if (chat.getMinPrice() == null || ad.getPrice() == null) {
                    return SUCCESS;
                }
                return ad.getPrice() >= chat.getMinPrice() ? SUCCESS : MIN_PRICE_NOT_RESPECTED;
            };
        }

        static AdChatValidator isMaxPriceRespected() {
            return (ad, chat) -> {
                if (chat.getMaxPrice() == null || ad.getPrice() == null) {
                    return SUCCESS;
                }
                return ad.getPrice() >= chat.getMaxPrice() ? SUCCESS : MAN_PRICE_NOT_RESPECTED;
            };
        }

        default AdChatValidator and(AdChatValidator other) {
            return (ad, chat) -> {
                ValidationResult result = this.apply(ad, chat);
                return result.equals(SUCCESS) ? other.apply(ad, chat) : result;
            };
        }

        enum ValidationResult {
            SUCCESS, MIN_PRICE_NOT_RESPECTED, MAN_PRICE_NOT_RESPECTED
        }

    }
}
