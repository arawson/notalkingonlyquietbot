
package net.notalkingonlyquiet.bot;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * NO MORE COMPLICATED LOGGING
 * @author arawson
 */
public class LogUtil {
    private static final String LOGGER_NAME = "NOTALKINGONLYQUIET";
    private static final Logger LOGGER;
    
    static {
        LOGGER = Logger.getLogger(LOGGER_NAME);
        
        StaticLoggerBinder.getSingleton();
    }
    
    public static Logger getLogger() {
        return LOGGER;
    }
    
    public static void logError(String message) {
        LOGGER.log(Level.SEVERE, message);
    }
    
    public static void logInfo(String message) {
        LOGGER.log(Level.INFO, message);
    }

    public static void logTrace(String msg) {
        LOGGER.log(Level.FINE, msg);
    }
    
    /**
     * Inconvenience method for GoodLogger; use the other methods as they are
     * easier.
     * @param l
     * @param format
     * @param arguments 
     */
    public static void log(Level l, String format, Object[] arguments) {
        LOGGER.log(l, format, arguments);
    }
    
    /**
     * Inconvenience method for GoodLogger; use the other methods as they are
     * easier.
     * @param l
     * @param t
     * @param provider 
     */
    public static void log(Level l, Throwable t, String provider) {
        LOGGER.log(l, t, () -> provider);
    }

    public static void logWarning(String msg) {
        LOGGER.log(Level.WARNING, msg);
    }
}