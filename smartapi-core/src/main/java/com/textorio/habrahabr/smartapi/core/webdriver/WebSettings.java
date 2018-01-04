package com.textorio.habrahabr.smartapi.core.webdriver;

import java.util.Optional;

public class WebSettings {
    private String id;
    private Optional<String> profileDirName;
    private Optional<Boolean> visible;
    private long pageTimeout = 2000;

    public WebSettings() {
    }

    public WebSettings(String id) {
        this.id = id;
    }

    public Optional<String> getProfileDirName() {
        return profileDirName;
    }

    public void setProfileDirName(Optional<String> profileDirName) {
        this.profileDirName = profileDirName;
    }

    public Optional<Boolean> getVisible() {
        return visible;
    }

    public void setVisible(Optional<Boolean> visible) {
        this.visible = visible;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getPageTimeout() {
        return pageTimeout;
    }

    public void setPageTimeout(long pageTimeout) {
        this.pageTimeout = pageTimeout;
    }
}
