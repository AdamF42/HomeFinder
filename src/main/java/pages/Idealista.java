package pages;

import ch.qos.logback.classic.Logger;
import core.WebSiteType;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.LoggerFactory;
import utils.interval.RandomInterval;

public class Idealista extends Page {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Idealista.class);

    public Idealista(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval, String linksSelector, String nextPageSelector) {
        super(url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }

    @Override
    protected Connection getConnection(String url) {
        return Jsoup.connect(url)
                .header("Host", "www.idealista.it")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Connection", "keep-alive")
                .header("DNT", "1")
                .header("upgrade-insecure-requests", "1")
                .header("sec-fetch-site", "none")
                .header("sec-fetch-mode", "navigate")
                .timeout(30000)
                .referrer("http://www.google.com")
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.114 Safari/537.36");
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected Page createNew(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval, String linksSelector, String nextPageSelector) {
        return new Idealista(baseUrl + url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }

//    @Override
//    public Page getNextPage() {
//        Elements elements = document.select("#main-content > section > div > ul > li.next > a");
//        String url = elements.stream()
//                .map(e -> e.attributes().get("href")) //
//                .findFirst().orElseThrow();
//        return new Idealista(baseUrl + url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
//    }

    @Override
    public String getName() {
        return WebSiteType.IDEALISTA.toString();
    }

    public Idealista clone() {
        return new Idealista(document.location(), baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }
}
