import com.mongodb.Block;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import config.ConfigHandler;
import config.models.Config;
import config.models.House;
import io.vavr.control.Try;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static java.util.Collections.singletonList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MappingPOJO {

    public static void main(String[] args) {
        Config config = Try.of(ConfigHandler::getInstance).map(ConfigHandler::getConfig)
                .get();

        String string = "mongodb+srv://" + config.getMongoDbDatabase() + ":" + config.getMongoDBPass() + "@housefindercluster.3vyhn.mongodb.net/?retryWrites=true&w=majority";
        ConnectionString connectionString = new ConnectionString(string);
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();
        try (MongoClient mongoClient = MongoClients.create(clientSettings)) {
            MongoDatabase sampleTrainingDB = mongoClient.getDatabase(config.getMongoDbDatabase());
            MongoCollection<House> collection = sampleTrainingDB.getCollection("links", House.class);

            // create a new grade.
           // House newGrade = new House().setStudent_id(10003d)
             //       .setClass_id(10d);
//                    .setScores(singletonList(new Score().setType("homework").setScore(50d)));
            //grades.insertOne(newGrade);

            // find this grade.
           // Grade grade = grades.find(eq("student_id", 10003d)).first();
            //System.out.println("Grade found:\t" + grade);

            // update this grade: adding an exam grade
            //List<Score> newScores = new ArrayList<>(grade.getScores());
//            newScores.add(new Score().setType("exam").setScore(42d));
//            grade.setScores(newScores);
//            Document filterByGradeId = new Document("_id", grade.getId());
//            FindOneAndReplaceOptions returnDocAfterReplace = new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER);
//            Grade updatedGrade = grades.findOneAndReplace(filterByGradeId, grade, returnDocAfterReplace);
//            System.out.println("Grade replaced:\t" + updatedGrade);

            // delete this grade
            List<House> test = collection.find().into(new ArrayList<>());
            test.stream().map(House::getLink).forEach(System.out::println);
        }
    }
}