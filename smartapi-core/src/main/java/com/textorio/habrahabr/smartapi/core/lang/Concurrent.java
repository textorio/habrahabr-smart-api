package com.textorio.habrahabr.smartapi.core.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Concurrent {
    private static Logger staticLogger = LoggerFactory.getLogger(Concurrent.class);

    public static void sleep(long mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            staticLogger.error("Sleeping interrupted", e);
        }
    }
}
