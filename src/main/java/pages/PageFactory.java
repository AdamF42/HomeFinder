package pages;

public class PageFactory {

    public static Page get(PageType type, String url) {
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

    public enum PageType {
        IMMOBILIARE,
        SUBITO,
        CASA,
        IDEALISTA
    }
}
