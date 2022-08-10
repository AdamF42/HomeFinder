package pages;

import core.WebSiteType;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.interval.RandomInterval;

import java.util.Arrays;

public class Bakeca extends Page {

    private static final Logger logger = LoggerFactory.getLogger(Bakeca.class);

    public Bakeca(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval, String linksSelector, String nextPageSelector) {
        super(url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected Bakeca createNew(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval, String linksSelector, String nextPageSelector) {
        return new Bakeca(url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }
//    @Override
//    public Bakeca getNextPage() {
//        System.out.println(document.location());
//        Elements elements = document.select(nextPageSelector);
//        String url = elements.stream()
//                .map(e -> e.attributes().get("href")) //
//                .findFirst().orElseThrow();
//        if ("".equals(url)) {
//            url = elements.stream().flatMap(e -> Arrays.stream(e.attributes().get("onclick").split("\"")))
//                    .filter(e -> e.contains("http"))
//                    .findFirst().orElseThrow();
//        }
//        return createNew(baseUrl + url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
//    }


    @Override
    public String getName() {
        return WebSiteType.BAKECA.toString();
    }

    @Override
    public Bakeca clone() {
        return new Bakeca(document.location(), baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }

}
