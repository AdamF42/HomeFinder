package pages;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import utils.interval.RandomInterval;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Page implements Cloneable {

    protected final Document document;
    protected final String startUrl;
    protected final RandomInterval interval;
    protected final RandomInterval navigationInterval;
    protected final String baseUrl;
    protected final String linksSelector;
    protected final String nextPageSelector;

    public Page(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval, String linksSelector, String nextPageSelector) {
        this.interval = interval;
        this.navigationInterval = navigationInterval;
        this.document = getDocument(url);
        this.startUrl = url;
        this.baseUrl = baseUrl;
        this.linksSelector = linksSelector;
        this.nextPageSelector = nextPageSelector;
    }

    protected abstract Logger getLogger();

    protected abstract Page createNew(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval, String linksSelector, String nextPageSelector);

    public abstract String getName();

    public abstract Page clone();

    protected Connection getConnection(String url) {
        return Jsoup.connect(url)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3")
                .referrer("http://www.google.com")
                .timeout(30000)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.114 Safari/537.36");
    }

    public final List<String> getLinks() {
        return document.select(linksSelector).stream()
                .map(e -> baseUrl + e.attributes().get("href")) //
                .collect(Collectors.toList());
    }

    public Page getNextPage() {
        Elements elements = document.select(nextPageSelector);
        String url = elements.stream()
                .map(e -> e.attributes().get("href")) //
                .findFirst().orElseThrow();
        return createNew(baseUrl + url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }

    public boolean hasNextPage() {
        return document.select(nextPageSelector).stream().findFirst().isPresent();
    }

    public String getStartUrl() {
        return startUrl;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public Long getParsingInterval() {
        return this.interval.getInterval();
    }

    public Long getNavigationInterval() {
        return this.navigationInterval.getInterval();
    }

    private Document getDocument(String url) {
        Connection conn = getConnection(url);
        Document document = null;
        try {
            document = conn.get();
        } catch (IOException e) {
            getLogger().error("Unable to get document.", e);
        }
        return document;
    }
}
