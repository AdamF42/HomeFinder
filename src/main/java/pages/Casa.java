package pages;

import core.WebSiteType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.interval.RandomInterval;

public class Casa extends Page {

    private static final Logger logger = LoggerFactory.getLogger(Casa.class);

    public Casa(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval, String linksSelector, String nextPageSelector) {
        super(url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected Page createNew(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval, String linksSelector, String nextPageSelector) {
        return new Casa(baseUrl + url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }

//    @Override
//    public Page getNextPage() {
//        Elements elements = document.select(".next");
//        String url = elements.stream()
//                .map(e -> e.attributes().get("href")) //
//                .findFirst().orElseThrow();
//        return new Casa(baseUrl + url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
//    }

    @Override
    public String getName() {
        return WebSiteType.CASA.toString();
    }

    @Override
    public Casa clone() {
        return new Casa(document.location(), baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }

}
