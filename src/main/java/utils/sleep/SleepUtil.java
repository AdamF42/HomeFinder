package utils.sleep;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class SleepUtil {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(SleepUtil.class);

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.error("Unable to sleep", e);
        }
    }

}
