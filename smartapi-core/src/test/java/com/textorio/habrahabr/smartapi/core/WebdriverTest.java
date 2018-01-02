package com.textorio.habrahabr.smartapi.core;

import com.textorio.habrahabr.smartapi.core.webdriver.Webdriver;
import org.junit.jupiter.api.Test;

public class WebdriverTest {
    @Test
    public void test () {
        new Webdriver().enableDriver();
    }
}
