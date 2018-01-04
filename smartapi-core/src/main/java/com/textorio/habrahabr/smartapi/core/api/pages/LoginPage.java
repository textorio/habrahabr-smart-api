package com.textorio.habrahabr.smartapi.core.api.pages;

import com.textorio.habrahabr.smartapi.core.lang.Thing;
import com.textorio.habrahabr.smartapi.core.webdriver.Web;
import org.openqa.selenium.WebElement;

public class LoginPage {
    public static final String HABR_LOGIN_PAGE = "https://id.tmtm.ru/login/";
    private static final String XPATH_EMAIL = ".//*[@type='email']";
    private static final String XPATH_PASSWORD = ".//*[@type='password']";
    private static final String XPATH_SUBMIT = ".//*[@type='submit']";

    private Web web;
    private WebElement emailField;
    private WebElement passwordField;
    private WebElement submitButton;

    public LoginPage(Web web) {
        this.web = web;
    }

    public void update() {
        WebElement emailField =  web.driver().findElementByXPath(XPATH_EMAIL);
        WebElement passwordField =  web.driver().findElementByXPath(XPATH_PASSWORD);
        WebElement submitButton =  web.driver().findElementByXPath(XPATH_SUBMIT);

        this.emailField = emailField;
        this.passwordField = passwordField;
        this.submitButton = submitButton;
    }

    public static Thing<LoginPage, ?> create(Web web) {
        LoginPage loginPage = new LoginPage(web);
        try {
            loginPage.update();
        } catch (Exception e) {
            Thing.ofError(e, "Can't create Habrahabr Login Page");
        }
        return Thing.of(loginPage);
    }

    public static boolean activated(Web web) {
        return web.elementsExists(XPATH_EMAIL, XPATH_PASSWORD, XPATH_SUBMIT);
    }

    public static void activateFast(Web web) {
        activate(web, 0);
    }

    public static void activateSlow(Web web) {
        activate(web, web.getSettings().getPageTimeout());
    }

    public static void activate(Web web, long mills) {
        web.driver().get(HABR_LOGIN_PAGE);
    }

    public static Thing<LoginPage, ?> goFast(Web web) {
        return go(web, 0);
    }

    public static Thing<LoginPage, ?> goSlow(Web web) {
        return go(web, web.getSettings().getPageTimeout());
    }

    public static Thing<LoginPage, ?> go(Web web, long mills) {
        activate(web, mills);
        return create(web);
    }

    public void fill(String email, String password) {
        update();
        web.setAttribute(emailField, "value", email);
        web.setAttribute(passwordField, "value", password);
    }

    public void submit() {
        submitButton.click();
    }

}
