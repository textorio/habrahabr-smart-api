package com.textorio.habrahabr.smartapi.core.api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLogger {
    public static final String PRINT_FORMAT = "[@%s] %s";

    private Logger logger;
    private String id;

    public SLogger(String id, Logger logger) {
        this.logger = logger;
        this.id = id;
    }

    public SLogger(String id, Class<?> clazz) {
        this(id, LoggerFactory.getLogger(clazz));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void info(String message) {
        logger.info(String.format(PRINT_FORMAT, id, message));
    }

    public void warn(String message) {
        logger.warn(String.format(PRINT_FORMAT, id, message));
    }

    public void debug(String message) {
        logger.debug(String.format(PRINT_FORMAT, id, message));
    }

    public void error(String message, Exception ex) {
        logger.error(String.format(PRINT_FORMAT, id, message), ex);
    }

    public void error(String message) {
        logger.error(String.format(PRINT_FORMAT, id, message));
    }


}