package com.emp.management.system.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingUtil {
    private static final Logger log = LoggerFactory.getLogger(LoggingUtil.class);

    public static void logInfo(String message, Object... args) {
        log.info(message, args);
    }

    public static void logError(String message) {
        log.error(message);
    }

    public static void logError(String message, Object... args) {
        log.error(message, args);
    }


}
