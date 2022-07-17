import pages.Casa;
import pages.Page;
import pages.Subito;
import pages.Subito2;
import utils.interval.RandomInterval;
import utils.sleep.SleepUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class test {
//       static String url = "https://www.casa.it/srp/?tr=affitti&numRoomsMin=3&mqMin=50&priceMax=900&propertyTypeGroup=case&q=12d65861%2C171c3cab%2C035cb1f0%2C853e62a4%2Cd2a360ef%2Cd80cd525";
       static String url = "https://www.subito.it/annunci-emilia-romagna/affitto/appartamenti/bologna/bologna/?ps=200&pe=3000&rs=3";
       static String baseUrl = "https://www.subito.it";



    public static void main(String[] args) {
        Page p = new Subito(url, baseUrl, new RandomInterval(1,2), new RandomInterval(1,2));
        Page p2 = new Subito2(url, baseUrl, new RandomInterval(1,2), new RandomInterval(1,2));

        Set<String> tes = getAllLinks(p);
        Set<String> tes2 = getAllLinks(p2);

//        List<String> n = p.getNextPage().getLinks();
//
//        List<String> n2 = p2.getNextPage().getLinks();


        System.out.println(tes.equals(tes2));
//        System.out.println(n.equals(n2));

    }

    private static Set<String> getAllLinks(final Page page) {
        Page cp = page.clone();
        return getAllLinks(new HashSet<>(), cp);
    }

    private static Set<String> getAllLinks(Set<String> links, final Page page) {
        Long navigationInterval = page.getNavigationInterval();
        SleepUtil.sleep(navigationInterval);
        links.addAll(page.getLinks());
        System.out.println("page: " + page.getStartUrl());
        System.out.println("links: " + links.size());

        if (!page.hasNextPage()) {
            return links;
        }
        return getAllLinks(links, page.getNextPage());
    }


}
