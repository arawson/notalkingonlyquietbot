
package net.notalkingonlyquiet.bot;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * NO MORE COMPLICATED LOGGING
 * @author arawson
 */
public class LogUtil {
    private static final String LOGGER_NAME = "SPACE_MAZE";
    private static Logger logger;
    private static FileHandler handler;
    private static SimpleFormatter formatter;
    
    static void initLogging() throws IOException {
        logger = Logger.getLogger(LOGGER_NAME);
        handler = new FileHandler("./log");
        formatter = new SimpleFormatter();
        handler.setFormatter(formatter);
        logger.addHandler(handler);
    }
    
    public static Logger getLogger() {
        return logger;
    }
    
    public static void logError(String message) {
        logger.log(Level.SEVERE, message);
        handler.flush();
    }
    
    public static void logInfo(String message) {
        logger.log(Level.INFO, message);
        handler.flush();
    }
}