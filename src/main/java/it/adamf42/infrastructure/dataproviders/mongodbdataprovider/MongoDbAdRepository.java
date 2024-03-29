package it.adamf42.infrastructure.dataproviders.mongodbdataprovider;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import it.adamf42.core.usecases.ad.repositories.AdRepository;
import org.bson.Document;

import java.util.regex.Pattern;

public class MongoDbAdRepository implements AdRepository {
    private final MongoCollection<Document> adCollection;

    public MongoDbAdRepository(MongoCollection<Document> adCollection) {
        this.adCollection = adCollection;
    }

    @Override
    public DbAd save(DbAd ad) {
        Document adDocument = new Document().append("city", ad.getCity()).append("area", ad.getArea())
                .append("street", ad.getStreet()).append("title", ad.getTitle()).append("price", ad.getPrice())
                .append("squareMeters", ad.getSquareMeters()).append("floor", ad.getFloor())
                .append("condominiumFees", ad.getCondominiumFees()).append("energyRating", ad.getEnergyRating())
                .append("rooms", ad.getRooms()).append("bail", ad.getBail()).append("url", ad.getUrl())
                .append("publisher", ad.getPublisher());

        adCollection.insertOne(adDocument);
        return ad;
    }

    @Override
    public boolean isPresent(DbAd dbAd) {
        Document existingDocument = adCollection.find(
                Filters.and(
                        Filters.regex("city", Pattern.compile(dbAd.getCity(), Pattern.CASE_INSENSITIVE)),
                        Filters.regex("title", Pattern.compile(dbAd.getTitle(), Pattern.CASE_INSENSITIVE)),
                        Filters.eq("price", dbAd.getPrice())
                )
        ).first();
        return existingDocument != null;
    }
}
