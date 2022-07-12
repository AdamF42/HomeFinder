package data;

import com.mongodb.client.MongoCollection;
import data.pojo.House;

import java.util.ArrayList;
import java.util.List;

public class HouseRepositoryMongo implements HouseRepository {

    MongoCollection<House> collection;

    public HouseRepositoryMongo(MongoCollection<House> collection) {
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
