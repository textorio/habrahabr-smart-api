package com.textorio.habrahabr.smartapi.core.api;

import com.textorio.habrahabr.smartapi.core.api.pages.LoginPage;
import com.textorio.habrahabr.smartapi.core.api.pages.ProfilePage;
import com.textorio.habrahabr.smartapi.core.lang.Concurrent;
import com.textorio.habrahabr.smartapi.core.lang.Thing;
import com.textorio.habrahabr.smartapi.core.webdriver.Web;
import com.textorio.habrahabr.smartapi.core.webdriver.WebSettings;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserResource {
    private static Logger staticLogger = LoggerFactory.getLogger(UserResource.class);
    private SLogger logger;

    public static final boolean DEBUG_SIMULATE_AUTOMATIC_ERROR = true;

    private String email;
    private String password;
    private String username;
    public Web web;

    public UserResource(String username, String email, String password, Web web) {
        this.logger = new SLogger(username, UserResource.class);

        this.username = username;
        this.email = email;
        this.password = password;
        this.web = web;
    }

    public static Thing<UserResource, ?> create(String username, String email, String password) {
        WebSettings ws = new WebSettings();
        ws.setId(username);
        ws.setProfileDirName(Optional.of(String.format("%s-%s", Web.CHROME_PROFILE_DIR_NAME, username)));

        Web web;
        try {
            web = new Web().init(ws);
        } catch (Exception e) {
            return Thing.ofError("Can't create Web Token for the user");
        }

        return Thing.of(new UserResource(username, email, password, web));
    }

    public ChromeDriver driver() {
        return web.driver();
    }

    public boolean requireLogin() {
        boolean result = false;
        ProfilePage.activateFast(web);
        if (!ProfilePage.activated(web) && LoginPage.activated(web)) {
            logger.info("We're logged out, trying to log in using automation");
            if (!login(true)) {
                logger.info("Automatic logging failed, using manual mode");
                web.restartVisible();
                if (!login(false)) {
                    logger.error("Manual logging failed. No further attempts to call API result in valid data.");
                } else {
                    result = true;
                    logger.info("Manual logging in OK");
                    web.restartInvisible();
                }
            } else {
                result = true;
                logger.info("Logged in successfully. Let's celebrate, bitches!");
            }
        } else if (!ProfilePage.activated(web) && !LoginPage.activated(web)) {
            logger.error("Some strange unknown page encountered while processing a new request.");
        } if (ProfilePage.activated(web)){
            result = true;
            logger.info("You don't need to log in. Lucky bitch!");
        }
        return result;
    }

    public boolean login(boolean automatic) {
        //This is not duplication of what's written in requireLogin!
        //This happens when automated log-in procedure was too fast to detect success
        LoginPage.activateSlow(web); //may cause redirect to settings page
        if (ProfilePage.activated(web)) {
            logger.info("Previous attempts to log in succeeded, no further actions required.");
            return true;
        }

        LoginPage loginPage = LoginPage.goSlow(web).raiseIfInvalid("We should be on the login page now").get();
        loginPage.fill(email, password);

        if (automatic) {
            if (DEBUG_SIMULATE_AUTOMATIC_ERROR) {
                logger.info("Simulation of automatic logging error");
                return false;
            }

            logger.info("Trying to log in");
            loginPage.submit();
            //now we should be at this page: https://id.tmtm.ru/settings/?consumer=default

//            TODO: very ugly! I'll try to change it to a result detection later
            Concurrent.sleep(web.getSettings().getPageTimeout());

            if (ProfilePage.activated(web)) {
                logger.info("Logged in");
                return true;
            } else {
                logger.info("Logging in failed");
                return false;
            }
        } else {
            final AtomicBoolean success = new AtomicBoolean(true);
            web.closeWindowAndContinueWhen("Browser should switch to a settings page",
                    condition -> {

                        // Window should not be closed until user get to the settings page
                        try {
                            driver().getTitle();
                        } catch (Exception ex) {
                            logger.info("Browser closed, and that's very bad.");
                            success.set(false);
                            return true;
                        }

                        return ProfilePage.activated(web);
                    });
            return success.get();
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
