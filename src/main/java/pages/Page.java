package pages;

import java.util.List;

public interface Page extends Cloneable {
    List<String> getLinks();

//    void setMaxPrice(Integer price);
//
//    void setMinPrice(Integer price);
//
//    void setRoomNumber(Integer roomNumber);

    boolean hasNextPage();

    Page getNextPage();

    String getStartUrl();

    Page clone();

}
