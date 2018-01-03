package com.textorio.habrahabr.smartapi.core.webdriver;

import java.util.Optional;

public class WebSettings {
    private Optional<String> profileDirName;
    private Optional<Boolean> visible;

    public WebSettings() {
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
}
