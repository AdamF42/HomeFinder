package it.adamf42.infrastructure.dataproviders.mongodbdataprovider;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import it.adamf42.core.usecases.chat.repositories.ChatRepository;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDbChatRepository implements ChatRepository {

    private final MongoCollection<Document> userCollection;

    public MongoDbChatRepository(MongoCollection<Document> userCollection) {
        this.userCollection = userCollection;
    }

    @Override
    public DbChat save(DbChat dbChat) {
        Document userDocument = new Document("chatId", dbChat.getChatId()).append("maxPrice", dbChat.getMaxPrice())
                .append("minPrice", dbChat.getMinPrice()).append("city", dbChat.getCity())
                .append("isActive", dbChat.getIsActive());

        userCollection.insertOne(userDocument);

        return findByChatId(dbChat.getChatId());
    }

    @Override
    public DbChat findByChatId(Long chatId) {
        Document query = new Document("chatId", chatId);

        try (MongoCursor<Document> cursor = userCollection.find(query).iterator()) {
            if (cursor.hasNext()) {
                Document userDocument = cursor.next();
                return documentToDbUser(userDocument);
            }
        } catch (Exception e) {
            // TODO: log
        }

        return null; // User not found or an exception occurred
    }

    @Override
    public boolean existsByChatId(Long chatId) {
        Document query = new Document("chatId", chatId);
        return userCollection.countDocuments(query) > 0;
    }

    @Override
    public DbChat update(DbChat dbChat) {
        Document query = new Document("chatId", dbChat.getChatId());
        Document update = new Document();

        // Update only non-null fields
        if (dbChat.getMaxPrice() != null) {
            update.append("maxPrice", dbChat.getMaxPrice());
        }

        if (dbChat.getMinPrice() != null) {
            update.append("minPrice", dbChat.getMinPrice());
        }

        if (dbChat.getCity() != null && !dbChat.getCity().isEmpty()) {
            update.append("city", dbChat.getCity());
        }

        if (dbChat.getIsActive() != null) {
            update.append("isActive", dbChat.getIsActive());
        }

        if (!update.isEmpty()) {
            Document updateQuery = new Document("$set", update);
            userCollection.updateOne(query, updateQuery);
        }

        return findByChatId(dbChat.getChatId());
    }

    @Override
    public List<DbChat> getAll() {
        List<DbChat> chatList = new ArrayList<>();

        try (MongoCursor<Document> cursor = userCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Document userDocument = cursor.next();
                chatList.add(documentToDbUser(userDocument));
            }
        } catch (Exception e) {
            // TODO: log
        }

        return chatList;
    }

    private DbChat documentToDbUser(Document document) {
        return new DbChat(document.getLong("chatId"), document.getInteger("maxPrice"),
                document.getInteger("minPrice"), document.getString("city"), document.getBoolean("isActive", false));
    }
}
