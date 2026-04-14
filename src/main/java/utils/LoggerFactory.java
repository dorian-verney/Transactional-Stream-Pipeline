package utils;

import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerFactory {


    public static Logger create(Class<?> clazz, String filename) {
        Logger logger = Logger.getLogger(clazz.getName());
        try {
            logger.setUseParentHandlers(false);
            String path = System.getProperty("user.dir") + "/monitoring/" + filename;
            FileHandler fileHandler = new FileHandler(path, true);
            fileHandler.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    String blank = " ".repeat(7 - record.getLevel().getName().length());
                    long millis = record.getMillis() % 1000;
                    return String.format("[%s] %s,%03d %s"+ blank + " - %s - %s\n",
                            new java.text.SimpleDateFormat("yy-MM-dd").format(new java.util.Date(record.getMillis())),
                            new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(record.getMillis())),
                            millis,
                            record.getLevel(),
                            record.getSourceClassName(),
//                            record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf('.') + 1),
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