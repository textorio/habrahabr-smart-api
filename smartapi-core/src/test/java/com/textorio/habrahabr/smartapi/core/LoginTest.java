package com.textorio.habrahabr.smartapi.core;

import com.textorio.habrahabr.smartapi.core.api.UserResource;
import com.textorio.habrahabr.smartapi.core.api.UserSettings;
import org.junit.jupiter.api.Test;

public class LoginTest {
    @Test
    public void test () {
        UserSettings us = new UserSettings();
        UserResource user = UserResource.create(us.getEmail(), us.getPassword()).raiseIfInvalid("user is required").get();
        user.requireLogin();
        user.getWeb().debugScreenshot();
    }
}
