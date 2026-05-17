package edu.fusion.common.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class ErrorLogger {

    private static final Logger LOGGER = LogSupport.createFileLogger("edu.fusion.error", "error.log");

    private ErrorLogger() {
    }

    public static void log(String context, Throwable error) {
        LOGGER.log(Level.SEVERE, LogSupport.sanitize(context), error);
    }

    public static void log(String context, String message, Throwable error) {
        String entry = LogSupport.sanitize(context) + " - " + LogSupport.sanitize(message);
        LOGGER.log(Level.SEVERE, entry, error);
    }
}
