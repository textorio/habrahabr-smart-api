package com.textorio.habrahabr.smartapi.core.api;

import com.textorio.habrahabr.smartapi.core.lang.Thing;
import com.textorio.habrahabr.smartapi.core.webdriver.Web;
import com.textorio.habrahabr.smartapi.core.webdriver.WebSettings;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserResource {
    private static Logger logger = LoggerFactory.getLogger(UserResource.class);

    public static final String HABR_LOGIN_PAGE = "https://id.tmtm.ru/requireLogin/";

    private String email;
    private String password;
    public Web web;

    public static Thing<UserResource, ?> create(String email, String password) {
        WebSettings ws = new WebSettings();
        Web web = null;
        try {
            ws.setProfileDirName(Optional.of(String.format("%s-%s", Web.CHROME_PROFILE_DIR_NAME, email)));
            web = new Web().init(ws);
        } catch (Exception e) {
            return Thing.ofError("Can't create Web Token for the user");
        }

        UserResource user = new UserResource();
        user.setWeb(web);
        user.setEmail(email);
        user.setPassword(password);

        return Thing.of(user);
    }

    public void requireLogin() {
        ChromeDriver driver = web.driver();
        driver.get(HABR_LOGIN_PAGE);

        List<WebElement> goButtons = driver.findElementsByXPath(".//*[@name='go']");
        List<WebElement> exitButtons = driver.findElementsByXPath(".//*[@class='exit']");
        if (goButtons.size() > 0) {
            logger.info(String.format("Email %s logged out, trying to log in", email));
            login();
        } else if (exitButtons.size() > 0){
            logger.info(String.format("Email %s logged in, we must log out it before anything", email));
        } else {
            logger.error("Some strange unknown page encountered while processing a new request.");
        }
    }

    public void login() {
        ChromeDriver driver = web.driver();
        WebElement emailField = driver.findElementByXPath(".//*[@type='email']");
        web.setAttribute(emailField, "value", email);

        WebElement passwordField = driver.findElementByXPath(".//*[@type='password']");
        web.setAttribute(passwordField, "value", password);

        WebElement submitButton = driver.findElementByXPath(".//*[@type='submit']");
        submitButton.click();

        List<WebElement> exitButtons = driver.findElementsByXPath(".//*[@class='exit']");
        if (exitButtons.size() > 0) {
            logger.info(String.format("Email %s successfully logged in", email));
        } else {
            logger.info(String.format("Email %s logging procedure failed", email));
        }
    }

    public Web getWeb() {
        return web;
    }

    public void setWeb(Web web) {
        this.web = web;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
