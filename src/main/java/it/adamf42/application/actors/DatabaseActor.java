package it.adamf42.application.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.vavr.control.Try;
import it.adamf42.core.domain.ad.Ad;
import it.adamf42.core.domain.chat.Chat;
import it.adamf42.core.usecases.ad.CreateAdUseCase;
import it.adamf42.core.usecases.ad.DefaultCreateAdUseCase;
import it.adamf42.core.usecases.ad.repositories.AdRepository;
import it.adamf42.core.usecases.chat.*;
import it.adamf42.core.usecases.chat.repositories.ChatRepository;
import it.adamf42.infrastructure.dataproviders.mongodbdataprovider.MongoDbAdRepository;
import it.adamf42.infrastructure.dataproviders.mongodbdataprovider.MongoDbChatRepository;
import lombok.Getter;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.Serializable;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class DatabaseActor extends AbstractBehavior<DatabaseActor.Command> {

    private CreateAdUseCase createAd;
    private CreateChatUseCase createUser;
    private UpdateChatUseCase updateUser;
    private GetChatUseCase getChat;

    public interface Command extends Serializable {
    }

    public static class BootCommand implements DatabaseActor.Command {

        private static final long serialVersionUID = 1L;
        @Getter
        private final String connString;
        @Getter
        private final String database;
        @Getter
        private final ActorRef<ManagerActor.Command> manager;

        public BootCommand(String connString, String database, ActorRef<ManagerActor.Command> manager) {
            this.connString = connString;
            this.database = database;
            this.manager = manager;
        }

    }

    public static class SaveAdCommand implements DatabaseActor.Command {
        private static final long serialVersionUID = 1L;

        @Getter
        private final Ad ad;

        public SaveAdCommand(Ad ad) {
            this.ad = ad;
        }
    }

    public static class SaveChatCommand implements DatabaseActor.Command {
        private static final long serialVersionUID = 1L;

        @Getter
        private final Chat chat;

        public SaveChatCommand(Chat chat) {
            this.chat = chat;
        }
    }

    public static class UpdateChatCommand implements DatabaseActor.Command {
        private static final long serialVersionUID = 1L;

        @Getter
        private final Chat chat;

        public UpdateChatCommand(Chat chat) {
            this.chat = chat;
        }
    }

    public static class GetChatCommand implements DatabaseActor.Command {
        private static final long serialVersionUID = 1L;

        @Getter
        private final Long chatId;

        @Getter
        private final ActorRef<BotActor.Command> bot;

        public GetChatCommand(Long chatId, ActorRef<BotActor.Command> bot) {
            this.chatId = chatId;
            this.bot = bot;
        }
    }

    public DatabaseActor(ActorContext<DatabaseActor.Command> context) {
        super(context);
    }

    public static Behavior<DatabaseActor.Command> create() {
        return Behaviors.setup(DatabaseActor::new);
    }

    @Override
    public Receive<DatabaseActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(BootCommand.class, msg ->
                {
                    ConnectionString connectionString = new ConnectionString(msg.getConnString());
                    CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
                    CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                            pojoCodecRegistry);
                    MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
                            .codecRegistry(codecRegistry).build();
                    MongoClient client = MongoClients.create(clientSettings);
                    MongoDatabase database = client.getDatabase(msg.getDatabase());
                    MongoCollection<Document> adsCollection = database.getCollection("ads");
                    AdRepository adRepository = new MongoDbAdRepository(adsCollection);
                    this.createAd = new DefaultCreateAdUseCase(adRepository);
                    MongoCollection<Document> chatsCollection = database.getCollection("chats");
                    ChatRepository chatRepository = new MongoDbChatRepository(chatsCollection);
                    this.createUser = new DefaultCreateChatUseCase(chatRepository);
                    this.updateUser = new DefaultUpdateChatUseCase(chatRepository);
                    this.getChat = new DefaultGetChatUseCase(chatRepository);
                    return Behaviors.same();
                })
                .onMessage(SaveAdCommand.class, msg -> {
                    Try.of(() -> this.createAd.execute(adToRequest(msg.getAd())))
                            .onFailure(CreateAdUseCase.AlreadyPresentException.class, e -> getContext().getLog().debug("Already present"))
                            .onSuccess(ad -> getContext().getLog().debug("Successfully saved Ad: {}", ad));
                    return Behaviors.same();
                })
                .onMessage(SaveChatCommand.class, msg -> {
                    CreateChatUseCase.Request req = new CreateChatUseCase.Request();
                    req.setChat(msg.getChat());
                    Try.of(() -> this.createUser.execute(req))
                            .onFailure(CreateChatUseCase.AlreadyPresentException.class, e -> getContext().getLog().debug("Already present"))
                            .onSuccess(chat -> getContext().getLog().debug("Successfully saved chat: {}", chat));
                    return Behaviors.same();
                })
                .onMessage(UpdateChatCommand.class, msg -> {
                    UpdateChatUseCase.Request req = new UpdateChatUseCase.Request();
                    req.setChat(msg.getChat());
                    Try.of(() -> this.updateUser.execute(req))
                            .onFailure(UpdateChatUseCase.NotPresentException.class, e -> getContext().getLog().debug("Chat {} Not present", msg.getChat().getChatId()))
                            .onSuccess(chat -> getContext().getLog().debug("Successfully updated chat: {}", chat));
                    return Behaviors.same();
                })
                .onMessage(GetChatCommand.class, msg -> {
                    GetChatUseCase.Request req = new GetChatUseCase.Request();
                    req.setChatId(msg.getChatId());
                    Try.of(() -> this.getChat.execute(req))
                            .onFailure(GetChatUseCase.NotPresentException.class, e -> getContext().getLog().debug("Chat {} Not present", msg.getChatId()))
                            .onSuccess(chat -> getContext().getLog().debug("Successfully retrieved chat: {}", chat))
                            .andThen(chat -> msg.getBot().tell(new BotActor.GetChatResponseCommand(chat.getChat(), chat.getChat().getChatId())));
                    return Behaviors.same();
                })
                .build();
    }

    private static CreateAdUseCase.Request adToRequest(Ad ad) {
        CreateAdUseCase.Request request = new CreateAdUseCase.Request();
        request.setCity(ad.getCity());
        request.setArea(ad.getArea());
        request.setStreet(ad.getStreet());
        request.setTitle(ad.getTitle());
        request.setPrice(ad.getPrice());
        request.setSquareMeters(ad.getSquareMeters());
        request.setFloor(ad.getFloor());
        request.setCondominiumFees(ad.getCondominiumFees());
        request.setEnergyRating(ad.getEnergyRating());
        request.setRooms(ad.getRooms());
        request.setBail(ad.getBail());
        request.setUrl(ad.getUrl());
        request.setPublisher(ad.getPublisher());
        return request;
    }

}
