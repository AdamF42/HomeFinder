package it.adamf42.app.repo.data;

import com.mongodb.client.MongoCollection;
import it.adamf42.app.repo.data.pojo.House;
import it.adamf42.core.repo.data.HouseRepository;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class HouseRepositoryMongo implements HouseRepository {

    MongoCollection<House> collection;

    public HouseRepositoryMongo(MongoCollection<House> collection) {
        this.collection = collection;
    }


    @Override
    public List<House> getHousesByWebsite(String website) {
        return collection.find(eq("website", website)).into(new ArrayList<>());
    }

    @Override
    public void saveHouse(House house) {
        collection.insertOne(house);
    }

    @Override
    public void saveHouses(List<House> houses) {
        if (houses != null && !houses.isEmpty()) {
            collection.insertMany(houses);
        }
    }
}
