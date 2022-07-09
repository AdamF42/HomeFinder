package pages;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Casa implements Page {

    private Document document;
    private String startUrl;
    private static final String baseUrl = "https://www.casa.it";

    public Casa(String url) {
        document = getDocument(url);
        startUrl = url;
    }

    @Override
    public List<String> getLinks() {
        return getLinks(document);
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
                .findFirst().get();
        return new Casa(baseUrl + url);
    }

    @Override
    public String getStartUrl() {
        return startUrl;
    }

    private static Document getDocument(String url) {
        Connection conn = Jsoup.connect(url);
        Document document = null;
        try {
            document = conn.get();
        } catch (IOException e) {
            e.printStackTrace();
            // handle error
        }
        return document;
    }

    private static List<String> getLinks(Document document) {
        return document.select("#app > div > section > .list > div > article > div.csa_gallery.med > div.csa_gallery__slider > div:nth-child(1) > figure > a").stream()
                .map(e -> baseUrl + e.attributes().get("href")) //
                .collect(Collectors.toList());
    }

    @Override
    public Casa clone() {
        return new Casa(document.location());
    }
}
