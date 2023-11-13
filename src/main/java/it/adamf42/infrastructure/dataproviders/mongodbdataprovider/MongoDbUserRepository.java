package it.adamf42.infrastructure.dataproviders.mongodbdataprovider;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import it.adamf42.core.usecases.user.repositories.UserRepository;

public class MongoDbUserRepository implements UserRepository
{

	private final MongoCollection<Document> userCollection;

	public MongoDbUserRepository(MongoCollection<Document> userCollection)
	{
		this.userCollection = userCollection;
	}

	@Override
	public DbUser save(DbUser dbUser)
	{
		Document userDocument = new Document("chatId", dbUser.getChatId()).append("maxPrice", dbUser.getMaxPrice())
		.append("minPrice", dbUser.getMinPrice()).append("city", dbUser.getCity());

		userCollection.insertOne(userDocument);

		return dbUser;
	}

	@Override
	public DbUser findByChatId(String chatId)
	{
		Document query = new Document("chatId", chatId);

		try (MongoCursor<Document> cursor = userCollection.find(query).iterator())
		{
			if (cursor.hasNext())
			{
				Document userDocument = cursor.next();
				return documentToDbUser(userDocument);
			}
		}
		catch (Exception e)
		{
			// TODO: log
		}

		return null; // User not found or an exception occurred
	}

	@Override
	public boolean existsByChatId(Long chatId)
	{
		Document query = new Document("chatId", chatId);
		return userCollection.countDocuments(query) > 0;
	}

	@Override
	public DbUser update(DbUser dbUser)
	{
		Document query = new Document("chatId", dbUser.getChatId());
		Document update = new Document("$set",
		new Document().append("maxPrice", dbUser.getMaxPrice()).append("minPrice", dbUser.getMinPrice())
		.append("city", dbUser.getCity()));

		userCollection.updateOne(query, update);

		return dbUser;
	}

	private DbUser documentToDbUser(Document document)
	{
		return new DbUser(document.getLong("chatId"), document.getInteger("maxPrice"),
		document.getInteger("minPrice"), document.getString("city"));
	}
}
