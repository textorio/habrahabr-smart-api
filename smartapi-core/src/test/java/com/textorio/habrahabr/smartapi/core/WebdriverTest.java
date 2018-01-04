package com.textorio.habrahabr.smartapi.core;

import com.textorio.habrahabr.smartapi.core.webdriver.Web;
import com.textorio.habrahabr.smartapi.core.webdriver.WebSettings;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WebdriverTest {
    @Test
    public void test () {
        ChromeDriver driver = new Web().init(new WebSettings("olegchir")).driver();
        driver.get("http://textor.io");
        final String text = driver.findElementByXPath(".//*[@id='textorio-title']").getText();
        assertEquals("Textorio", text);
    }
}
