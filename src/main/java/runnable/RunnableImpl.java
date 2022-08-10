package runnable;

import ch.qos.logback.classic.Logger;
import data.HouseRepository;
import data.pojo.House;
import org.slf4j.LoggerFactory;
import pages.Page;
import utils.sleep.SleepUtil;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RunnableImpl implements Runnable {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(RunnableImpl.class);

    private final Consumer<String> sendMsg;
    private final HouseRepository houseRepository;
    private Page page;
    private boolean shouldRun = true;

    public RunnableImpl(Consumer<String> sendMsg, Page page, HouseRepository houseRepository) {
        this.sendMsg = sendMsg;
        this.page = page;
        this.houseRepository = houseRepository;
    }

    private House toHouse(String str) {
        House house = new House();
        house.setLink(str);
        house.setWebsite(page.getName());
        house.setTimestamp(LocalDateTime.now());
        return house;
    }

    public void reloadPage() {
        page = page.clone();
    }

    public void setShouldRun(boolean val) {
        this.shouldRun = val;
    }

    public void run() {
        while (shouldRun) {
            List<String> houses = houseRepository.getHouses().stream().map(House::getLink).collect(Collectors.toList());
            logger.debug("[WEBSITE] {} [HOUSES] {}", page.getBaseUrl(), houses.size());
            try {
                List<House> newHouses = getAllLinks().stream()
                        .filter(link -> !houses.contains(link))
                        .peek(e -> logger.info("[NEW LINK] {}", e))
                        .map(this::toHouse)
                        .collect(Collectors.toList());

                newHouses.forEach(house -> sendMsg.accept(house.getLink()));
                houseRepository.saveHouses(newHouses);
            } catch (Exception e) {
                logger.error("Generic error", e);
            }
            Long parsingInterval = page.getParsingInterval();
            logger.debug("[WEBSITE] {} [SLEEP] {}", page.getBaseUrl(), parsingInterval);
            SleepUtil.sleep(parsingInterval);
        }
    }

    private Set<String> getAllLinks() {
        Page cp = page.clone();
        Set<String> links = getAllLinks(new HashSet<>(), cp);
        logger.debug("[PAGE] {} [LIST]: {}", page.getStartUrl(), links.size());
        return links;
    }

    private Set<String> getAllLinks(Set<String> links, final Page page) {
        Long navigationInterval = page.getNavigationInterval();
        logger.debug("[WEBSITE] {} [NAV INTERVAL] {}", page.getBaseUrl(), navigationInterval);
        SleepUtil.sleep(navigationInterval);
        links.addAll(page.getLinks());
        if (!page.hasNextPage()) {
            return links;
        }
        return getAllLinks(links, page.getNextPage());
    }
}