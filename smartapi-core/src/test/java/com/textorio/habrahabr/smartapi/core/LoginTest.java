package com.textorio.habrahabr.smartapi.core;

import com.textorio.habrahabr.smartapi.core.api.UserResource;
import com.textorio.habrahabr.smartapi.core.api.UserSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {
    @Test
    public void test () {
        boolean loginOK = false;
        UserResource user = null;
        try {
            UserSettings us = new UserSettings();
            user = UserResource.create(us.getUsername(), us.getEmail(), us.getPassword()).raiseIfInvalid("user is required").get();
            loginOK = user.requireLogin();
        } finally {
            user.getWeb().stop();
        }
        assertTrue(loginOK, "Logging in should be successful");
    }
}
