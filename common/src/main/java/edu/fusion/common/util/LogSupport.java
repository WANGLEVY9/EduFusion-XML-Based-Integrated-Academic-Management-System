package edu.fusion.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

final class LogSupport {

    private LogSupport() {
    }

    static Logger createFileLogger(String name, String fileName) {
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        if (logger.getHandlers().length == 0) {
            try {
                Path logDir = Paths.get("logs");
                Files.createDirectories(logDir);
                FileHandler handler = new FileHandler(logDir.resolve(fileName).toString(), true);
                handler.setFormatter(new SimpleFormatter());
                logger.addHandler(handler);
                logger.setLevel(Level.INFO);
            } catch (IOException ex) {
                logger.setUseParentHandlers(true);
                logger.log(Level.WARNING, "Failed to initialize file logger: " + fileName, ex);
            }
        }
        return logger;
    }

    static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\n', ' ').replace('\r', ' ').trim();
    }
}
