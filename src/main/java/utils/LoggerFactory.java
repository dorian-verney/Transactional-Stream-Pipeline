package utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerFactory {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public static Logger create(Class<?> clazz, String filename) {
        Logger logger = Logger.getLogger(clazz.getName());
        try {
            logger.setUseParentHandlers(false);
            String path = System.getProperty("user.dir") + "/monitoring/" + filename;
            FileHandler fileHandler = new FileHandler(path, true);
            fileHandler.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    Instant instant = Instant.ofEpochMilli(record.getMillis());
                    String timestamp = DATE_FMT.format(instant);
                    long millis = record.getMillis() % 1000;
                    String blank = " ".repeat(7 - record.getLevel().getName().length());
                    String simpleClass = record.getSourceClassName();
//                            .substring(record.getSourceClassName().lastIndexOf('.') + 1);
                    return String.format("[%s,%03d] %s%s - %s - %s\n",
                            timestamp, millis,
                            record.getLevel(), blank,
                            simpleClass,
                            record.getMessage()
                    );
                }
            });
            logger.addHandler(fileHandler);
        } catch (Exception e) {
            logger.severe("Failed to set up file handler for " + clazz.getSimpleName());
        }
        return logger;
    }
}