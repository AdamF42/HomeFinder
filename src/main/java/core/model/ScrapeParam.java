package core.model;

import java.io.Serializable;

public class ScrapeParam implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;

    private final String url;

    private final String baseUrl;

    private final Integer maxPrice;

    private final Integer minPrice;

    private final Integer roomNumber;

    private final Integer minParsingInterval;

    private final Integer maxParsingInterval;

    private final String linksSelector;

    private final String nextPageSelector;


    public ScrapeParam(String name, String url, String baseUrl, Integer maxPrice, Integer minPrice, Integer roomNumber, Integer minParsingInterval, Integer maxParsingInterval, String linksSelector, String nextPageSelector) {
        this.name = name;
        this.url = url;
        this.baseUrl = baseUrl;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
        this.roomNumber = roomNumber;
        this.minParsingInterval = minParsingInterval;
        this.maxParsingInterval = maxParsingInterval;
        this.linksSelector = linksSelector;
        this.nextPageSelector = nextPageSelector;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public Integer getMinPrice() {
        return minPrice;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public Integer getMinParsingInterval() {
        return minParsingInterval;
    }

    public Integer getMaxParsingInterval() {
        return maxParsingInterval;
    }

    public String getLinksSelector() {
        return linksSelector;
    }

    public String getNextPageSelector() {
        return nextPageSelector;
    }
}
