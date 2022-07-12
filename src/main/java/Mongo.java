import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertManyOptions;
import config.ConfigHandler;
import config.models.Config;
import io.vavr.control.Try;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Mongo {


    public static void main(String[] args) {

        Config config = Try.of(ConfigHandler::getInstance).map(ConfigHandler::getConfig)
                .get();

        String connectionString = "mongodb+srv://" + config.getMongoDbDatabase() + ":" + config.getMongoDBPass() + "@housefindercluster.3vyhn.mongodb.net/?retryWrites=true&w=majority";
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase sampleTrainingDB = mongoClient.getDatabase(config.getMongoDbDatabase());
            MongoCollection<Document> gradesCollection = sampleTrainingDB.getCollection("links");
            System.out.println(gradesCollection.countDocuments());
            Document link = new Document("_id", new ObjectId());

            link.append("link", "10000d")
                    .append("timestamp", LocalDateTime.now());
            gradesCollection.insertMany(List.of(link), new InsertManyOptions().ordered(false));

            Document link1 = gradesCollection.find(new Document("link", "10000d")).first();
            System.out.println("link1 1: " + link1.get("link"));

            List<Document> linkList = gradesCollection.find().into(new ArrayList<>());

            linkList.forEach(System.out::println);




//            gradesCollection.drop();

        }
    }

}
