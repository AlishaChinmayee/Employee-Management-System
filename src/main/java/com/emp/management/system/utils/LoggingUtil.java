package com.emp.management.system.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingUtil {
    private static final Logger log = LoggerFactory.getLogger(LoggingUtil.class);

    public static void logInfo(String message) {
        log.info(message);
    }

   
}