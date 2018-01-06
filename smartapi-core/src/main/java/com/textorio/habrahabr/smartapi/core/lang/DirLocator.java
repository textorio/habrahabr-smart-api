package com.textorio.habrahabr.smartapi.core.lang;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class DirLocator {
    public static final String TEXTORIO_SETTINGS_DIR = ".textorio";
    public static final String TEXTORIO_HABR_SETTINGS_DIR = "habrahabr";

    /**
     * https://stackoverflow.com/questions/585534/what-is-the-best-way-to-find-the-users-home-directory-in-java
     * @return
     */
    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    public static File getTemp() {
        return FileUtils.getTempDirectory();
    }

    public static String getSettingsFilePath(String username) {
        return getUserHome() + File.separator + TEXTORIO_SETTINGS_DIR + File.separator + TEXTORIO_HABR_SETTINGS_DIR + File.separator + username;
    }
}
