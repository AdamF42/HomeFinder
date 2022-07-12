package pages;

import config.pojo.WebSite;
import core.WebSiteType;
import utils.interval.RandomInterval;

public class PageFactory {

    public static Page get(WebSiteType type, WebSite webSite) {
        RandomInterval parsingInterval = new RandomInterval(webSite.getMinParsingInterval(), webSite.getMaxParsingInterval());
        RandomInterval navigationInterval = new RandomInterval(webSite.getMinPageNavigationInterval(), webSite.getMaxPageNavigationInterval());
        switch (type) {
            case IMMOBILIARE:
                return new Immobiliare(webSite.getUrl(), webSite.getBaseUrl(), parsingInterval, navigationInterval);
            case IDEALISTA:
                return new Idealista(webSite.getUrl(), webSite.getBaseUrl(), parsingInterval, navigationInterval);
            case CASA:
                return new Casa(webSite.getUrl(), webSite.getBaseUrl(), parsingInterval, navigationInterval);
            case SUBITO:
                return new Subito(webSite.getUrl(), webSite.getBaseUrl(), parsingInterval, navigationInterval);
            default:
                return new NotSupported();
        }
    }

}
