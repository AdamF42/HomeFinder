package pages;

import core.WebSiteType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.interval.RandomInterval;

public class Subito extends Page {

    private static final Logger logger = LoggerFactory.getLogger(Subito.class);

    public Subito(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval, String linksSelector, String nextPageSelector) {
        super(url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected Page createNew(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval, String linksSelector, String nextPageSelector) {
        return new Subito(baseUrl + url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }


//    @Override
//    public Page getNextPage() {
//        Elements elements = document.select("div > div:nth-child(2) > nav > a:last-child");
//        String url = elements.stream()
//                .map(e -> e.attributes().get("href")) //
//                .findFirst().orElseThrow();
//        return new Subito(baseUrl + url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
//    }

    @Override
    public String getName() {
        return WebSiteType.SUBITO.toString();
    }

    @Override
    public Subito clone() {
        return new Subito(document.location(), baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }

}
