package com.textorio.habrahabr.smartapi.core.api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLogger {
    public static final String PRINT_FORMAT = "[@%s] %s";

    private Logger logger;
    private String username;

    public SLogger(String username, Logger logger) {
        this.logger = logger;
        this.username = username;
    }

    public SLogger(String username, Class<?> clazz) {
        this(username, LoggerFactory.getLogger(clazz));
    }

    public void info(String message) {
        logger.info(String.format(PRINT_FORMAT, username));
    }

    public void warn(String message) {
        logger.warn(String.format(PRINT_FORMAT, username));
    }

    public void debug(String message) {
        logger.debug(String.format(PRINT_FORMAT, username));
    }

    public void error(String message, Exception ex) {
        logger.error(String.format(PRINT_FORMAT, username), ex);
    }

}