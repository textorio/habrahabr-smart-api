package com.textorio.habrahabr.smartapi.core.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Sys {
    private static Logger logger = LoggerFactory.getLogger(Sys.class);

    public static synchronized Properties getUserSettings(String username) {
        Properties result = new Properties();
        String settingsFilePath = DirLocator.getSettingsFilePath(username);
        try (FileInputStream input = new FileInputStream(settingsFilePath)) {
            result.load(input);
        } catch (Exception e) {
            logger.warn(String.format("Can't find user settings in the %s", settingsFilePath));
        }
        return result;
    }

    public static synchronized void saveUserSettings(Properties props) {
        String username = props.getProperty("username");
        String settingsFilePath = DirLocator.getSettingsFilePath(username);
        File settingsFile = new File(settingsFilePath);
        try (OutputStream out = new FileOutputStream( settingsFile )) {
            props.store(out, "Textorio settings for Habrahabr");
        } catch (Exception e) {
            logger.error(String.format("Can't save user settings to the %s", settingsFilePath));
        }
    }

    public static synchronized boolean updateBooleanSetting(String username, String fieldName, boolean newValue) {
        Properties userSettings = Sys.getUserSettings(username);
        boolean oldValue = Boolean.valueOf(userSettings.getProperty(fieldName));
        userSettings.setProperty(fieldName, Boolean.toString(newValue));
        Sys.saveUserSettings(userSettings);
        return oldValue;
    }

    public static synchronized String updateStringSetting(String username, String fieldName, String newValue) {
        Properties userSettings = Sys.getUserSettings(username);
        String oldValue = userSettings.getProperty(fieldName);
        userSettings.setProperty(fieldName,newValue);
        Sys.saveUserSettings(userSettings);
        return oldValue;
    }

    public static synchronized int updateIntSetting(String username, String fieldName, int newValue) {
        Properties userSettings = Sys.getUserSettings(username);
        int oldValue = Integer.parseInt(userSettings.getProperty(fieldName));
        userSettings.setProperty(fieldName,Integer.toString(newValue));
        Sys.saveUserSettings(userSettings);
        return oldValue;
    }
}
