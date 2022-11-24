package it.adamf42.app.repo.data;

import ch.qos.logback.classic.Logger;
import it.adamf42.app.repo.data.pojo.House;
import it.adamf42.core.repo.data.HouseRepository;
import io.vavr.control.Try;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class HouseRepositoryFile implements HouseRepository {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(HouseRepositoryFile.class);

    private final String dataFile;

    public HouseRepositoryFile(String dataFile) {
        this.dataFile = dataFile;
    }

    private static House toHouse(String str) {
        House house = new House();
        house.setLink(str);
        house.setTimestamp(LocalDateTime.now());
        return house;
    }

    @Override
    public List<House> getHouses() {
        List<String> links = Try.of(() -> Files.lines(Paths.get(dataFile)))
                .onFailure(e -> logger.error("Unable to read file", e))
                .map(e -> e.collect(Collectors.toList())).getOrElse(ArrayList::new);
        return links.stream().map(HouseRepositoryFile::toHouse).collect(Collectors.toList());
    }

    @Override
    public void saveHouse(House house) {
        Try.ofCallable(writeString(house.getLink()))
                .onFailure(e -> logger.error("Unable to write file", e));
    }

    @Override
    public void saveHouses(List<House> houses) {
        List<String> links = houses.stream().map(House::getLink).collect(Collectors.toList());
        Try.of(() -> writeString(links))
                .onFailure(e -> logger.error("Unable to write file", e));
    }

    private Callable<String> writeString(String str) {
        return () -> {
            FileWriter writer = new FileWriter(dataFile, true);
            writer.write(str + System.lineSeparator());
            writer.close();
            return str;
        };
    }

    private Callable<List<String>> writeString(List<String> str) {
        String toWrite = str.stream().collect(Collectors.joining("", "", System.lineSeparator()));
        return () -> {
            FileWriter writer = new FileWriter(dataFile, true);
            writer.write(toWrite);
            writer.close();
            return str;
        };
    }
}
