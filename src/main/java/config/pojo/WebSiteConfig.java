package config.pojo;

import org.bson.codecs.pojo.annotations.BsonProperty;

public class WebSiteConfig {

    @BsonProperty(value = "name")
    private String name;

    @BsonProperty(value = "minPageNavigationInterval")
    private Integer minPageNavigationInterval;

    @BsonProperty(value = "maxPageNavigationInterval")
    private Integer maxPageNavigationInterval;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
