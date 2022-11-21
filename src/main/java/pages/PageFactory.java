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
                return new Immobiliare(
                        webSite.getUrl(),
                        webSite.getBaseUrl(),
                        parsingInterval,
                        navigationInterval,
                        "div > div.nd-mediaObject__content.in-card__content.in-realEstateListCard__content > a",
                        "#__next > section > div.in-main.in-searchList__main > div.in-pagination.in-searchList__pagination > div:nth-child(3) > a:nth-child(1)");
            case IDEALISTA:
                return new Idealista(webSite.getUrl(), webSite.getBaseUrl(), parsingInterval, navigationInterval,
                        "#main-content > section > .item > .item-info-container > a",
                        "#main-content > section > div > ul > li.next > a");
            case CASA:
                return new Casa(webSite.getUrl(), webSite.getBaseUrl(), parsingInterval, navigationInterval,
                        ".csa-gallery__imga",
                        ".next");
            case SUBITO:
                return new Subito(webSite.getUrl(), webSite.getBaseUrl(), parsingInterval, navigationInterval,
                        ".item-card > a",
                        "div > div:nth-child(2) > nav > a:last-child");
            case BAKECA:
                return new Bakeca(webSite.getUrl(), webSite.getBaseUrl(), parsingInterval, navigationInterval,
                        "body > div.main-wrapper.relative > main > article > * > div.annuncio-elenco > section > a",
                        "body > div.main-wrapper.relative > main > article > * > div > a.ml-auto");
            default:
                return new NotSupported(null, null, null, null, null, null);
        }
    }

}
