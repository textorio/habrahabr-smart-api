package com.textorio.habrahabr.smartapi.core.api;

import com.textorio.habrahabr.smartapi.core.lang.Sys;
import java.util.Properties;

public class UserSettings {
    private String username;
    private String email;
    private String password;
    private Boolean firstRun;
    private Boolean chromedriverHackingEnabled;
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
    }

    public synchronized boolean getAndUpdateFirstRun() {
        Properties userSettings = Sys.getUserSettings(username);
        boolean oldValue = Boolean.valueOf(userSettings.getProperty("first_run"));
        userSettings.setProperty("first_run", "false");
        Sys.saveUserSettings(userSettings);
        return oldValue;
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

    public Boolean getFirstRun() {
        return firstRun;
    }

    public void setFirstRun(Boolean firstRun) {
        this.firstRun = firstRun;
    }

    public Boolean getChromedriverHackingEnabled() {
        return chromedriverHackingEnabled;
    }

    public void setChromedriverHackingEnabled(Boolean chromedriverHackingEnabled) {
        this.chromedriverHackingEnabled = chromedriverHackingEnabled;
    }

    public String getChromedriverHackingExe() {
        return chromedriverHackingExe;
    }

    public void setChromedriverHackingExe(String chromedriverHackingExe) {
        this.chromedriverHackingExe = chromedriverHackingExe;
    }
}
