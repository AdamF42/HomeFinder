package it.adamf42.core.repo.data;

import it.adamf42.app.repo.data.pojo.House;

import java.util.List;

public interface HouseRepository {

    List<House> getHouses();

    void saveHouse(House house);

    void saveHouses(List<House> houses);
}
