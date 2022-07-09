package pages;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Idealista implements Page{

    private Document document;
    private String startUrl;
    private static final String baseUrl = "https://www.idealista.it";

    public Idealista(String url) {
        document = getDocument(url);
        startUrl = url;
    }

    @Override
    public List<String> getLinks() {
        return getLinks(document);
    }

    @Override
    public boolean hasNextPage() {
        return document.select("#main-content > section > div > ul > li.next > a").stream().findFirst().isPresent();
    }

    @Override
    public Page getNextPage() {
        Elements elements = document.select("#main-content > section > div > ul > li.next > a");
        String url = elements.stream()
                .map(e -> e.attributes().get("href")) //
                .findFirst().orElseThrow();
        return new Idealista(baseUrl + url);
    }

    @Override
    public String getStartUrl() {
        return startUrl;
    }

    private static Document getDocument(String url) {
        Connection conn = Jsoup.connect(url)
                .header("Host", "www.idealista.it")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Connection", "keep-alive")
                .header("DNT", "1")
                .header("upgrade-insecure-requests", "1")
                .header("sec-fetch-site", "none")
                .header("sec-fetch-mode", "navigate")
                .timeout(5000)
                .referrer("http://www.google.com")
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.114 Safari/537.36");
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
        return document.select("#main-content > section > .item > .item-info-container > a").stream()
                .map(e -> baseUrl + e.attributes().get("href")) //
                .collect(Collectors.toList());
    }

    @Override
    public Idealista clone() {
        return new Idealista(document.location());
    }
}
