package com.textorio.habrahabr.smartapi.core.api;

import com.textorio.habrahabr.smartapi.core.lang.Sys;

import java.util.Properties;

public class UserSettings {
    private String username;
    private String email;
    private String password;
    private boolean firstRun;

    //https://sites.google.com/a/chromium.org/chromedriver/
    private boolean chromedriverHackingEnabled;
    private String chromedriverHackingExe;

    public UserSettings(String username) {
        this(Sys.getUserSettings(username));
    }

    public UserSettings(Properties userSettings) {
        username = userSettings.getProperty("username");
        email = userSettings.getProperty("email");
        password = userSettings.getProperty("password");
        chromedriverHackingEnabled = Boolean.valueOf(userSettings.getProperty("chromedriver_hacking_enabled"));
        chromedriverHackingExe = userSettings.getProperty("chromedriver_hacking_exe");
        firstRun = Boolean.valueOf(userSettings.getProperty("first_run"));
    }

    public synchronized boolean firstRunCompleted() {
        return Sys.updateBooleanSetting(username, "first_run", false);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getFirstRun() {
        return firstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }

    public boolean getChromedriverHackingEnabled() {
        return chromedriverHackingEnabled;
    }

    public void setChromedriverHackingEnabled(boolean chromedriverHackingEnabled) {
        this.chromedriverHackingEnabled = chromedriverHackingEnabled;
    }

    public String getChromedriverHackingExe() {
        return chromedriverHackingExe;
    }

    public void setChromedriverHackingExe(String chromedriverHackingExe) {
        this.chromedriverHackingExe = chromedriverHackingExe;
    }
}
