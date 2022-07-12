package data;

import data.pojo.House;

import java.util.List;

public interface HouseRepository {

    List<House> getHouses();

    void saveHouse(House house);

    void saveHouses(List<House> houses);
}
