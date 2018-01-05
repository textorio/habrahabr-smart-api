package com.textorio.habrahabr.smartapi.core;

import com.textorio.habrahabr.smartapi.core.api.UserResource;
import com.textorio.habrahabr.smartapi.core.api.UserSettings;
import com.textorio.habrahabr.smartapi.core.api.pages.ProfilePage;
import com.textorio.habrahabr.smartapi.core.webdriver.WebSettings;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {
    @Test
    public void test () {
        boolean loginOK = false;
        UserResource user = null;
        try {
            UserSettings us = new UserSettings("olegchir");
            boolean firstRun = us.getFirstRun();

            WebSettings ws = new WebSettings("olegchir");
            ws.setChromedriverHackingEnabled(us.getChromedriverHackingEnabled());
            ws.setChromedriverHackingExe(us.getChromedriverHackingExe());
            ws.setRemoveProfileDirBeforeStart(firstRun);
            ws.setEnsuringCleanSession(firstRun);

            user = UserResource.create(Optional.of(ws), us.getUsername(), us.getEmail(), us.getPassword()).raiseIfInvalid("user is required").get();
//            user.getWeb().debugShowBrowser(ProfilePage.HABR_SETTINGS_PAGE); loginOK = true;

            loginOK = user.requireLogin(firstRun);
            assertTrue(loginOK, "Logging in should be successful");

            if (loginOK && firstRun) {
                us.firstRunCompleted();
            }

        } catch (Exception e) {
          e.printStackTrace();
        } finally {
            user.getWeb().stop();
        }
    }
}
