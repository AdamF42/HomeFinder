import java.util.List;

public interface Page {
    List<String> getLinks();

    boolean hasNextPage();

    Page getNextPage();

}
