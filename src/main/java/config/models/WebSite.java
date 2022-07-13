package config.models;

public class WebSite {
    private String url;
    private Integer maxPrice;
    private Integer minPrice;
    private Integer roomNumber;

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
}
