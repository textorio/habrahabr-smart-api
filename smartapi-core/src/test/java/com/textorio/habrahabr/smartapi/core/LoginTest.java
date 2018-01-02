package com.textorio.habrahabr.smartapi.core;

import com.textorio.habrahabr.smartapi.core.api.UserResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginTest {
    @Test
    public void test () {
        UserResource.create("olegchir", "unknown")
            .raiseIfInvalid("user is required").get()
            .requireLogin();
    }
}
