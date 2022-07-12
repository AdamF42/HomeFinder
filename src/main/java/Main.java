import pages.Immobiliare;
import pages.Page;
import utils.sleep.SleepUtil;

import java.util.ArrayList;
import java.util.List;

public class Main {

    //    private static final String url = "https://www.subito.it/annunci-emilia-romagna/affitto/appartamenti/bologna/bologna/?ps=300&pe=900&rs=3";
    private static final String url = "https://www.immobiliare.it/affitto-case/bologna/?criterio=rilevanza&prezzoMinimo=200&prezzoMassimo=2000&superficieMinima=60&localiMinimo=3&idMZona[]=17&idMZona[]=23&idMZona[]=31";
//    private static final String url = "https://www.idealista.it/affitto-case/bologna-bologna/con-prezzo_900,prezzo-min_200,trilocali-3,quadrilocali-4,5-locali-o-piu/?ordine=pubblicazione-desc";

//    private static final String url = "https://www.casa.it/srp/?tr=affitti&numRoomsMin=3&priceMax=5000&propertyTypeGroup=case&q=12d65861,171c3cab,035cb1f0,853e62a4,d2a360ef,d80cd525";


//    public static void main(String[] args) {
//
//        List<String> links = getAllLinks();
//
//        System.out.println(links.size());
////        System.out.println(getAllLinksR().size());
//    }

//    private static List<String> getAllLinks() {

//        Page page = new Casa(url);
//        Page page = new Subito(url);
//        Page page = new Immobiliare(url);
//        List<String> links = page.getLinks();
//        int num = 0;
//        while (page.hasNextPage()) {
//            System.out.println(num);
//            SleepUtil.sleep(1000);
//            page = page.getNextPage();
//            List<String> test = page.getLinks();
//            links.addAll(test);
//            num = num + 1;
//        }
//        return links;
//    }

//    private static List<String> getAllLinksR() {
//
////        Page page = new Casa(url);
////        Page page = new Subito(url);
//        Page page = new Immobiliare(url);
//        return getAllLinks(new ArrayList<>(), page, 0);
//    }


//    private static List<String> getAllLinks(List<String> links, final Page page, final int num) {
//        SleepUtil.sleep(1000);
//        links.addAll(page.getLinks());
//        if (!page.hasNextPage()) {
//            return links;
//        }
//        return getAllLinks(links, page.getNextPage(), num + 1);
//    }


}
