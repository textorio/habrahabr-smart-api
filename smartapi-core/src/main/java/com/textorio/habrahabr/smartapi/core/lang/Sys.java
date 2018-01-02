package com.textorio.habrahabr.smartapi.core.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Sys {
    private static Logger logger = LoggerFactory.getLogger(Sys.class);
    public static final String SETTINGS_FILE_NAME = ".habr";
    /**
     * https://stackoverflow.com/questions/585534/what-is-the-best-way-to-find-the-users-home-directory-in-java
     * @return
     */
    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    public static Properties getUserSettings() {
        Properties result = new Properties();
        String settingsFilePath = getUserHome() + File.separator + SETTINGS_FILE_NAME;
        try (FileInputStream input = new FileInputStream(settingsFilePath)) {
            result.load(input);
        } catch (Exception e) {
            logger.warn(String.format("Can't find user settings in the %s", settingsFilePath));
        }
        return result;
    }
}
