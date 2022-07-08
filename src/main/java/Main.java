import java.util.List;

public class Main {

    private static final String url = "https://www.subito.it/annunci-emilia-romagna/affitto/appartamenti/bologna/bologna/";

    public static void main(String[] args) {

        List<String> links = getAllLinks();

        System.out.println(links.size());
    }

    private static List<String> getAllLinks() {
        Page page = new Subito(url);
        List<String> links = page.getLinks();

        while (page.hasNextPage()) {
            page = page.getNextPage();
            List<String> test = page.getLinks();
            links.addAll(test);
        }
        return links;
    }


}
