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
        IMMOBILIARE("immobiliare"),
        SUBITO("subito"),
        CASA("casa"),
        IDEALISTA("idealista");

        private String s;

        PageType(String text) {
            this.s = text;
        }

        public static PageType fromString(String s) {
            for (PageType b : PageType.values()) {
                if (b.s.equalsIgnoreCase(s)) {
                    return b;
                }
            }
            return null;
        }
    }
}
