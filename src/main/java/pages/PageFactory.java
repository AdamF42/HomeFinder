package pages;

import core.WebSiteType;

public class PageFactory {

    public static Page get(WebSiteType type, String url) {
        switch (type) {
            case IMMOBILIARE:
                return new Immobiliare(url);
            case IDEALISTA:
                return new Idealista(url);
            case CASA:
                return new Casa(url);
            case SUBITO:
                return new Subito(url);
            default:
                return new NotSupported();
        }
    }

}
