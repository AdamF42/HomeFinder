package core;

public enum WebSiteType {
    IMMOBILIARE("immobiliare"),
    SUBITO("subito"),
    CASA("casa"),
    IDEALISTA("idealista"),
    BAKECA("bakeca");

    private final String s;

    WebSiteType(String text) {
        this.s = text;
    }

    public static WebSiteType fromString(String s) {
        for (WebSiteType b : WebSiteType.values()) {
            if (b.s.equalsIgnoreCase(s)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return s;
    }
}
