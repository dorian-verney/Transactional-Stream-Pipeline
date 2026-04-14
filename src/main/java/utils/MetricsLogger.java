package utils;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MetricsLogger {

    private final Logger logger;

    public MetricsLogger(Logger logger) {
        this.logger = logger;
    }

    public void logIf(boolean predicate, Level level, String message) {
        if (predicate) logger.log(level, message);
    }

    public void logEvery(long count, long every, Supplier<String> message) {
        if (count % every == 0) logger.info(message.get());
    }
}