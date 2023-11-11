package it.adamf42.application.actors;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.io.Serializable;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import it.adamf42.core.domain.ad.Ad;
import it.adamf42.core.usecases.DefaultCreateAdUseCase;
import it.adamf42.core.usecases.CreateAdUseCase;
import it.adamf42.core.usecases.repositories.AdRepository;
import it.adamf42.infrastructure.dataproviders.mongodbdataprovider.MongoDbAdRepository;
import lombok.Getter;

public class DatabaseActor extends AbstractBehavior<DatabaseActor.Command>
{

	private CreateAdUseCase createAd;
	public interface Command extends Serializable
	{
	}

	public static class StartCommand implements DatabaseActor.Command
	{

		private static final long serialVersionUID = 1L;
		@Getter
		private final String connString;
		@Getter
		private final String database;
		@Getter
		private final ActorRef<ManagerActor.Command> manager;

		public StartCommand(String connString, String database, ActorRef<ManagerActor.Command> manager)
		{
			this.connString = connString;
			this.database = database;
			this.manager = manager;
		}

	}

	public static class SaveAdCommand implements DatabaseActor.Command
	{

		private static final long serialVersionUID = 1L;

		@Getter
		private final  Ad ad;

		public SaveAdCommand(Ad ad) {
			this.ad = ad;
		}


	}

	public DatabaseActor(ActorContext<DatabaseActor.Command> context)
	{
		super(context);
	}

	public static Behavior<DatabaseActor.Command> create()
	{
		return Behaviors.setup(DatabaseActor::new);
	}

	@Override
	public Receive<DatabaseActor.Command> createReceive()
	{
		return newReceiveBuilder()
		.onMessage(StartCommand.class, msg ->
		{
			ConnectionString connectionString = new ConnectionString(msg.getConnString());
			CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
			CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
			pojoCodecRegistry);
			MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
			.codecRegistry(codecRegistry).build();
			MongoClient client = MongoClients.create(clientSettings);
			MongoDatabase database = client.getDatabase(msg.getDatabase());
			MongoCollection<Document> collection = database.getCollection("ads");
			AdRepository adRepository = new MongoDbAdRepository(collection);
			this.createAd = new DefaultCreateAdUseCase(adRepository);

			return Behaviors.same();
		})
		.onMessage(SaveAdCommand.class, msg -> {
			this.createAd.execute(adToRequest(msg.getAd()));
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
