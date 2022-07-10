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

public class Casa implements Page {

    private static final String baseUrl = "https://www.casa.it";
    private final Document document;
    private final String startUrl;

    private static final Logger logger = LoggerFactory.getLogger(Casa.class);


    public Casa(String url) {
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
        return document.select("#app > div > section > .list > div > article > div.csa_gallery.med > div.csa_gallery__slider > div:nth-child(1) > figure > a").stream()
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
        return new Casa(baseUrl + url);
    }

    @Override
    public String getStartUrl() {
        return startUrl;
    }

    @Override
    public Casa clone() {
        return new Casa(document.location());
    }
}
