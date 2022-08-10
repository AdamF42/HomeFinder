package pages;

import org.slf4j.Logger;
import utils.interval.RandomInterval;

public class NotSupported extends Page {
    public NotSupported(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval, String linksSelector, String nextPageSelector) {
        super(url, baseUrl, interval, navigationInterval, linksSelector, nextPageSelector);
    }

    @Override
    protected Logger getLogger() {
        return null;
    }

    @Override
    protected Page createNew(String url, String baseUrl, RandomInterval interval, RandomInterval navigationInterval, String linksSelector, String nextPageSelector) {
        return null;
    }


    @Override
    public String getName() {
        return null;
    }

    @Override
    public Page clone() {
        return null;
    }


}
