package pages;

import java.util.ArrayList;
import java.util.List;

public class NotSupported implements Page {
    @Override
    public List<String> getLinks() {
        return new ArrayList<>();
    }

    @Override
    public boolean hasNextPage() {
        return false;
    }

    @Override
    public Page getNextPage() {
        return this;
    }

    @Override
    public String getStartUrl() {
        return "";
    }

    @Override
    public Page clone() {
        return this;
    }

    @Override
    public Long getParsingInterval() {
        return 0L;
    }

    @Override
    public Long getNavigationInterval() {
        return 0L;
    }
}
