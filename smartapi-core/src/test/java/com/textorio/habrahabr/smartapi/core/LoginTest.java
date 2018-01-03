package com.textorio.habrahabr.smartapi.core;

import com.textorio.habrahabr.smartapi.core.api.UserResource;
import com.textorio.habrahabr.smartapi.core.api.UserSettings;
import org.junit.jupiter.api.Test;

public class LoginTest {
    @Test
    public void test () {
        UserResource user = null;
        try {
            UserSettings us = new UserSettings();
            user = UserResource.create(us.getEmail(), us.getPassword()).raiseIfInvalid("user is required").get();
            user.requireLogin();
        } finally {
            user.getWeb().stop();
        }
    }
}
