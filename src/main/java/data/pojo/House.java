package data.pojo;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Objects;

public class House {
    private ObjectId id;

    @BsonProperty(value = "link")
    private String link;

    @BsonProperty(value = "website")
    private String website;

    @BsonProperty(value = "timestamp")
    private LocalDateTime timestamp;


    // getters and setters with builder pattern
    // toString()
    // equals()
    // hashCode()

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof House)) return false;
        House house = (House) o;
        return getId().equals(house.getId()) && getLink().equals(house.getLink()) && getTimestamp().equals(house.getTimestamp());
    }


    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLink(), getTimestamp());
    }
}