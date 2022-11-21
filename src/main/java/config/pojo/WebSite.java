package config.pojo;

import org.bson.codecs.pojo.annotations.BsonProperty;

public class WebSite {

    @BsonProperty(value = "name")
    private String name;

    @BsonProperty(value = "url")
    private String url;

    @BsonProperty(value = "baseUrl")
    private String baseUrl;

    @BsonProperty(value = "maxPrice")
    private Integer maxPrice;

    @BsonProperty(value = "minPrice")
    private Integer minPrice;

    @BsonProperty(value = "roomNumber")
    private Integer roomNumber;

    @BsonProperty(value = "minParsingInterval")
    private Integer minParsingInterval;

    @BsonProperty(value = "maxParsingInterval")
    private Integer maxParsingInterval;

    @BsonProperty(value = "minPageNavigationInterval")
    private Integer minPageNavigationInterval;

    @BsonProperty(value = "maxPageNavigationInterval")
    private Integer maxPageNavigationInterval;

    // String linksSelector, String nextPageSelector,
    @BsonProperty(value = "linksSelector")
    private String linksSelector;

    @BsonProperty(value = "nextPageSelector")
    private String nextPageSelector;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Integer getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getUrl() {
        return url.replaceFirst("minPrice", String.valueOf(minPrice)).replaceFirst("roomNumber", String.valueOf(roomNumber)).replaceFirst("maxPrice", String.valueOf(maxPrice));
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Integer getMinParsingInterval() {
        return minParsingInterval;
    }

    public void setMinParsingInterval(Integer minParsingInterval) {
        this.minParsingInterval = minParsingInterval;
    }

    public Integer getMaxParsingInterval() {
        return maxParsingInterval;
    }

    public void setMaxParsingInterval(Integer maxParsingInterval) {
        this.maxParsingInterval = maxParsingInterval;
    }

    public Integer getMinPageNavigationInterval() {
        return minPageNavigationInterval;
    }

    public void setMinPageNavigationInterval(Integer minPageNavigationInterval) {
        this.minPageNavigationInterval = minPageNavigationInterval;
    }

    public Integer getMaxPageNavigationInterval() {
        return maxPageNavigationInterval;
    }

    public void setMaxPageNavigationInterval(Integer maxPageNavigationInterval) {
        this.maxPageNavigationInterval = maxPageNavigationInterval;
    }

    public String getLinksSelector() {
        return linksSelector;
    }

    public void setLinksSelector(String linksSelector) {
        this.linksSelector = linksSelector;
    }

    public String getNextPageSelector() {
        return nextPageSelector;
    }

    public void setNextPageSelector(String nextPageSelector) {
        this.nextPageSelector = nextPageSelector;
    }
}
