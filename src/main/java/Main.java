import pages.Casa;
import pages.Page;

import java.util.List;

public class Main {

//    private static final String url = "https://www.subito.it/annunci-emilia-romagna/affitto/appartamenti/bologna/bologna/?ps=300&pe=900&rs=3";
//    private static final String url = "https://www.immobiliare.it/affitto-case/bologna/?criterio=rilevanza&prezzoMinimo=200&prezzoMassimo=900&superficieMinima=60&localiMinimo=3&idMZona[]=17&idMZona[]=23&idMZona[]=31";
//    private static final String url = "https://www.idealista.it/affitto-case/bologna-bologna/con-prezzo_900,prezzo-min_200,trilocali-3,quadrilocali-4,5-locali-o-piu/?ordine=pubblicazione-desc";

    private static final String url = "https://www.casa.it/srp/?tr=affitti&numRoomsMin=3&priceMax=5000&propertyTypeGroup=case&q=12d65861,171c3cab,035cb1f0,853e62a4,d2a360ef,d80cd525";


    public static void main(String[] args) {

        List<String> links = getAllLinks();

        System.out.println(links.size());
    }

    private static List<String> getAllLinks() {
        Page page = new Casa(url);
//        Page page = new Subito(url);
//        Page page = new Immobiliare(url);
        List<String> links = page.getLinks();

        while (page.hasNextPage()) {
            page = page.getNextPage();
            List<String> test = page.getLinks();
            links.addAll(test);
        }
        return links;
    }


}
