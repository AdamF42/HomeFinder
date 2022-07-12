package data;

import com.mongodb.client.MongoCollection;
import config.models.House;

import java.util.ArrayList;
import java.util.List;

public class HouseRepositoryImpl implements HouseRepository {

    MongoCollection<House> collection;

    public HouseRepositoryImpl(MongoCollection<House> collection) {
        this.collection = collection;
    }


    @Override
    public List<House> getHouses() {
        return collection.find().into(new ArrayList<>());
    }

    @Override
    public void saveHouse(House house) {
        collection.insertOne(house);
    }

    @Override
    public void saveHouses(List<House> houses) {
        collection.insertMany(houses);
    }
}
