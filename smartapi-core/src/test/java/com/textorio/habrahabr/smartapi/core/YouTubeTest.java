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

            String result = user.processText(
                    "wiCKL2SrDM0",
                    "/Users/olegchir/tmp/img",
                    "transcript.txt",
                    "/Users/olegchir/tmp/img/result.html");
            System.out.println(result);

//            for (Map.Entry<String, String> item: result.entrySet()) {
//                System.out.println(String.format("%s = %s", item.getKey(), item.getValue()));
//            }

        } catch (Exception e) {
          e.printStackTrace();
        } finally {
            user.getWeb().stop();
        }
    }
}
