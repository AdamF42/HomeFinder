package pages;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Immobiliare implements Page {

    private final Document document;
    private final String startUrl;
    private final static String baseUrl = "https://www.immobiliare.it";

    private static final Logger logger = LoggerFactory.getLogger(Immobiliare.class);

    public Immobiliare(String url) {
        document = getDocument(url);
        startUrl = url;
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
        return document.select("div > div.nd-mediaObject__content.in-card__content.in-realEstateListCard__content > a").stream()
                .map(e -> e.attributes().get("href")) //
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasNextPage() {
        return document.select("#__next > section > div.in-main.in-searchList__main > div.in-pagination.in-searchList__pagination > div:nth-child(3) > a:nth-child(1)").stream().findFirst().isPresent();
    }

    @Override
    public Page getNextPage() {
        Elements elements = document.select("#__next > section > div.in-main.in-searchList__main > div.in-pagination.in-searchList__pagination > div:nth-child(3) > a:nth-child(1)");
        String url = elements.stream()
                .map(e -> e.attributes().get("href")) //
                .findFirst().orElseThrow();
        return new Immobiliare(url);
    }

    @Override
    public String getStartUrl() {
        return startUrl;
    }

    @Override
    public Immobiliare clone() {
        return new Immobiliare(document.location());
    }
}
