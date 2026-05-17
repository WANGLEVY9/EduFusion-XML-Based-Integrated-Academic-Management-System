package edu.fusion.common.util;

import java.util.logging.Logger;

public final class AuditLogger {

    private static final Logger LOGGER = LogSupport.createFileLogger("edu.fusion.audit", "audit.log");

    private AuditLogger() {
    }

    public static void log(String action, String actor, String target, boolean success, String message) {
        String entry = "action=" + LogSupport.sanitize(action)
                + " actor=" + LogSupport.sanitize(actor)
                + " target=" + LogSupport.sanitize(target)
                + " success=" + success
                + " message=" + LogSupport.sanitize(message);
        LOGGER.info(entry);
    }
}
