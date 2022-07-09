package pages;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Immobiliare implements Page {

    private Document document;
    private String baseUrl;

    public Immobiliare(String url) {
        document = getDocument(url);
        baseUrl = url;
    }

    @Override
    public List<String> getLinks() {
        return getLinks(document);
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
                .findFirst().get();
        return new Immobiliare(url);
    }

    @Override
    public String getStartUrl() {
        return baseUrl;
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
        return document.select("div > div.nd-mediaObject__content.in-card__content.in-realEstateListCard__content > a").stream()
                .map(e -> e.attributes().get("href")) //
                .collect(Collectors.toList());
    }

    @Override
    public Immobiliare clone() {
        return new Immobiliare(document.location());
    }
}
