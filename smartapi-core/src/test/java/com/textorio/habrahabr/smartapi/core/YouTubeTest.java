package com.textorio.habrahabr.smartapi.core;

import com.textorio.habrahabr.smartapi.core.api.UserResource;
import com.textorio.habrahabr.smartapi.core.api.UserSettings;
import com.textorio.habrahabr.smartapi.core.api.pages.ProfilePage;
import com.textorio.habrahabr.smartapi.core.lang.SubimageSize;
import com.textorio.habrahabr.smartapi.core.webdriver.WebSettings;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
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
            String path = "/Users/olegchir/tmp/img";

            HashMap<String, String> result = user.downloadScreenshots("EgsA9cMVf9Q", size, path, new int[]{1000, 500, 1000, 500, 1000});
            for (Map.Entry<String, String> item: result.entrySet()) {
                System.out.println(String.format("%s = %s", item.getKey(), item.getValue()));
            }

        } catch (Exception e) {
          e.printStackTrace();
        } finally {
            user.getWeb().stop();
        }
    }
}
