package com.textorio.habrahabr.smartapi.core;

import com.textorio.habrahabr.smartapi.core.api.UserResource;
import com.textorio.habrahabr.smartapi.core.api.UserSettings;
import com.textorio.habrahabr.smartapi.core.api.pages.ProfilePage;
import com.textorio.habrahabr.smartapi.core.lang.SubimageSize;
import com.textorio.habrahabr.smartapi.core.webdriver.WebSettings;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class YouTubeTest {
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

            SubimageSize size = new SubimageSize(0,0, 1920, 1080);
            user.downloadScreenshot("EgsA9cMVf9Q",1000, size, "/Users/olegchir/tmp/a1.jpg",false);
            user.downloadScreenshot("EgsA9cMVf9Q",500, size, "/Users/olegchir/tmp/a2.jpg",true);
            user.downloadScreenshot("EgsA9cMVf9Q",1000, size, "/Users/olegchir/tmp/a3.jpg",true);
            user.downloadScreenshot("EgsA9cMVf9Q",500, size, "/Users/olegchir/tmp/a4.jpg",true);
            user.downloadScreenshot("EgsA9cMVf9Q",1000, size, "/Users/olegchir/tmp/a5.jpg",true);


        } catch (Exception e) {
          e.printStackTrace();
        } finally {
            user.getWeb().stop();
        }
    }
}
