package pages;

import config.pojo.ScrapeParam;
import core.WebSiteType;
import utils.interval.RandomInterval;

public class PageFactory {

    public static Page get(WebSiteType type, ScrapeParam scrapeParam) {
        RandomInterval parsingInterval = new RandomInterval(scrapeParam.getMinParsingInterval(), scrapeParam.getMaxParsingInterval());
        RandomInterval navigationInterval = new RandomInterval(scrapeParam.getMinPageNavigationInterval(), scrapeParam.getMaxPageNavigationInterval());
        switch (type) {
            case IMMOBILIARE:
                return new Immobiliare(
                        scrapeParam.getUrl(),
                        scrapeParam.getBaseUrl(),
                        parsingInterval,
                        navigationInterval,
                        "div > div.nd-mediaObject__content.in-card__content.in-realEstateListCard__content > a",
                        "#__next > section > div.in-main.in-searchList__main > div.in-pagination.in-searchList__pagination > div:nth-child(3) > a:nth-child(1)");
            case IDEALISTA:
                return new Idealista(scrapeParam.getUrl(), scrapeParam.getBaseUrl(), parsingInterval, navigationInterval,
                        "#main-content > section > .item > .item-info-container > a",
                        "#main-content > section > div > ul > li.next > a");
            case CASA:
                return new Casa(scrapeParam.getUrl(), scrapeParam.getBaseUrl(), parsingInterval, navigationInterval,
                        ".csa-gallery__imga",
                        ".next");
            case SUBITO:
                return new Subito(scrapeParam.getUrl(), scrapeParam.getBaseUrl(), parsingInterval, navigationInterval,
                        ".item-card > a",
                        "div > div:nth-child(2) > nav > a:last-child");
            case BAKECA:
                return new Bakeca(scrapeParam.getUrl(), scrapeParam.getBaseUrl(), parsingInterval, navigationInterval,
                        "body > div.main-wrapper.relative > main > article > * > div.annuncio-elenco > section > a",
                        "body > div.main-wrapper.relative > main > article > * > div > a.ml-auto");
            default:
                return new NotSupported(null, null, null, null, null, null);
        }
    }

}
