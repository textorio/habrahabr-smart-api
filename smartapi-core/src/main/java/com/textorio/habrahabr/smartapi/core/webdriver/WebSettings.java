package com.textorio.habrahabr.smartapi.core.webdriver;

import java.util.Optional;

public class WebSettings {
    private Optional<String> profileDirName;

    public WebSettings() {
    }

    public Optional<String> getProfileDirName() {
        return profileDirName;
    }

    public void setProfileDirName(Optional<String> profileDirName) {
        this.profileDirName = profileDirName;
    }
}
