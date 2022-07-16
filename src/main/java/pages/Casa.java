package pages;

import core.WebSiteType;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.interval.RandomInterval;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Casa implements Page {

    private static final Logger logger = LoggerFactory.getLogger(Casa.class);

    private final Document document;
    private final String startUrl;
    private final RandomInterval interval;
    private final RandomInterval navigationInterval;
    private final String baseUrl;

    public Casa(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval) {
        this.interval = interval;
        this.navigationInterval = navigationInterval;
        this.document = getDocument(url);
        this.startUrl = url;
        this.baseUrl = baseUrl;
    }

    private static Document getDocument(String url) {
        Connection conn = Jsoup.connect(url)
                .timeout(30000)
                .referrer("http://www.google.com")
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.114 Safari/537.36");
        Document document = null;
        try {
            document = conn.get();
        } catch (IOException e) {
            logger.error("Unable to get document.", e);
        }
        return document;
    }

    @Override
    public List<String> getLinks() {
        return document.select(".csa-gallery__imga").stream()
                .map(e -> baseUrl + e.attributes().get("href")) //
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasNextPage() {
        return document.select(".next").stream().findFirst().isPresent();
    }

    @Override
    public Page getNextPage() {
        Elements elements = document.select(".next");
        String url = elements.stream()
                .map(e -> e.attributes().get("href")) //
                .findFirst().orElseThrow();
        return new Casa(baseUrl + url, baseUrl, interval, navigationInterval);
    }

    @Override
    public String getStartUrl() {
        return startUrl;
    }

    @Override
    public String getBaseUrl() {
        return this.baseUrl;
    }

    @Override
    public String getName() {
        return WebSiteType.CASA.toString();
    }

    @Override
    public Casa clone() {
        return new Casa(document.location(), baseUrl, interval, navigationInterval);
    }

    @Override
    public Long getParsingInterval() {
        return this.interval.getInterval();
    }

    @Override
    public Long getNavigationInterval() {
        return this.navigationInterval.getInterval();
    }
}
