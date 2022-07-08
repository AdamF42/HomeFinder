import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Subito implements Page {

    private static final String baseUrl = "https://www.subito.it";

    private Document document;

    private int pageNum = 1;

    public Subito(String url) {
        document = getDocument(url);
    }

    private Subito(String url, int pageNum) {
        document = getDocument(url);
    }

    @Override
    public List<String> getLinks() {
        return getLinks(document);
    }

    @Override
    public boolean hasNextPage() {
        return document.select("div > div:nth-child(2) > nav > a:last-child").stream().findFirst().isPresent();
    }

    @Override
    public Page getNextPage() {
        Elements elements = document.select("div:nth-child(2) > nav > a");
        List<String> arrowLinks = elements.stream().map(e -> e.attributes().get("href")).toList();
        String url = arrowLinks.get(arrowLinks.size() - 1);

        return new Subito(baseUrl + url, pageNum +1);
    }


    private static Document getDocument(String url) {
        System.out.println(url);
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

    private List<String> getLinks(Document document) {
        return document.select(".item-card > a").stream().map(e -> e.attributes().get("href")).collect(Collectors.toList());
    }

}
