package pages;

import core.WebSiteType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.interval.RandomInterval;

public class Immobiliare extends Page {

    private static final Logger logger = LoggerFactory.getLogger(Immobiliare.class);

    public Immobiliare(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval, String linksSelector, String nextPageSelector) {
        super(url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }

//    protected Connection getConnection(String url) {
//        return Jsoup.connect(url)
//                .timeout(30000)
//                .referrer("http://www.google.com")
//                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.114 Safari/537.36");
//    }

    @Override//    @Override
//    public Page getNextPage() {
//        Elements elements = document.select("#__next > section > div.in-main.in-searchList__main > div.in-pagination.in-searchList__pagination > div:nth-child(3) > a:nth-child(1)");
//        String url = elements.stream()
//                .map(e -> e.attributes().get("href")) //
//                .findFirst().orElseThrow();
//        return new Immobiliare(url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
//    }
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected Page createNew(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval, String linksSelector, String nextPageSelector) {
        return new Immobiliare(url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }

//    @Override
//    public Page getNextPage() {
//        Elements elements = document.select("#__next > section > div.in-main.in-searchList__main > div.in-pagination.in-searchList__pagination > div:nth-child(3) > a:nth-child(1)");
//        String url = elements.stream()
//                .map(e -> e.attributes().get("href")) //
//                .findFirst().orElseThrow();
//        return new Immobiliare(url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
//    }

    @Override
    public String getName() {
        return WebSiteType.IMMOBILIARE.toString();
    }

    @Override
    public Immobiliare clone() {
        return new Immobiliare(document.location(), baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }

}
