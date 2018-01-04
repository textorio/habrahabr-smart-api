package com.textorio.habrahabr.smartapi.core.api;

import com.textorio.habrahabr.smartapi.core.lang.Sys;
import java.util.Properties;

public class UserSettings {
    private String username;
    private String email;
    private String password;

    public UserSettings() {
        this(Sys.getUserSettings());
    }

    public UserSettings(Properties userSettings) {
        username = userSettings.getProperty("username");
        email = userSettings.getProperty("email");
        password = userSettings.getProperty("password");
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
}
