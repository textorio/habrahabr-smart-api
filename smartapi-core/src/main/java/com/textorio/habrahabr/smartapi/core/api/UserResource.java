package com.textorio.habrahabr.smartapi.core.api;

import com.textorio.habrahabr.smartapi.core.api.pages.LoginPage;
import com.textorio.habrahabr.smartapi.core.api.pages.ProfilePage;
import com.textorio.habrahabr.smartapi.core.lang.Concurrent;
import com.textorio.habrahabr.smartapi.core.lang.SubimageSize;
import com.textorio.habrahabr.smartapi.core.lang.Thing;
import com.textorio.habrahabr.smartapi.core.webdriver.Web;
import com.textorio.habrahabr.smartapi.core.webdriver.WebSettings;
import org.apache.commons.codec.binary.Base64;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.textorio.habrahabr.smartapi.core.webdriver.Web.CONDITION_REACTION_TIMEOUT_SECONDS;

public class UserResource {
    private static Logger staticLogger = LoggerFactory.getLogger(UserResource.class);
    private SLogger logger;

    public static final boolean DEBUG_SIMULATE_AUTOMATIC_ERROR = false;

    private String email;
    private String password;
    private String username;
    public Web web;

    public HashMap<String, String> downloadScreenshots(String ytid, SubimageSize size, String destDir, int[] times) throws IOException, InterruptedException {
        HashMap<String, String> result = new HashMap<>();
        boolean firstTime = true;
        for (int i=0; i< times.length; i++) {
            int time = times[i];
            String destFile = destDir + File.separator + Integer.toString(i) + ".jpg";
            downloadScreenshot(ytid, time, size, destFile, !firstTime);
            result.put(Integer.toString(time), destFile);
            if (firstTime) {
                firstTime = false;
            }
        }
        return result;
    }

    public String downloadScreenshot(String ytid, int time, SubimageSize size, String destFile, boolean followUp) throws InterruptedException, IOException {
        String ytUrl = String.format("https://www.youtube.com/watch?v=%s", ytid);
        if (!followUp) {
            web.debugShowBrowser(ytUrl);
            Thread.sleep(10000);
        }

        String screenshotMaker = getResourceFileAsString("screenshot-maker.js");
        web.driver().executeScript(screenshotMaker,time);
        WebDriverWait wait3 = new WebDriverWait(web.driver(), 50);

        Thread.sleep(5000);

        wait3.until(driver -> ((JavascriptExecutor)driver).executeScript("return window.screenshotFired != undefined;").equals(true));

        Thread.sleep(3000);

        String summoner = getResourceFileAsString("lastresult-summoner.js");
        web.driver().executeScript(summoner);
        WebDriverWait wait = new WebDriverWait(web.driver(), 20);
        wait.until(driver -> ((JavascriptExecutor)driver).executeScript("return window.lastResult != undefined;").equals(true));

        Object result = driver().executeScript("return window.lastResult;");
        saveImageFromBase64String((String)result, size, destFile);

        System.out.println(result);

        analyzeLog();

        return "";
    }

    public void saveImageFromBase64String(String image, SubimageSize size, String destination) throws IOException {
        byte[] data = Base64.decodeBase64((String)image);
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        BufferedImage bImage2 = ImageIO.read(bis);
        BufferedImage bImage3 = bImage2.getSubimage(size.x,size.y, size.width, size.height);
        ImageIO.write(bImage3, "jpg", new File(destination) );
    }

    public String getResourceFileAsString(String fileName) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        return null;
    }

    public void analyzeLog() {
        LogEntries logEntries = web.driver().manage().logs().get(LogType.BROWSER);
        for (LogEntry entry : logEntries) {
            System.out.println(new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage());
            //do something useful with the data
        }
    }

    public UserResource(String username, String email, String password, Web web) {
        this.logger = new SLogger(username, UserResource.class);

        this.username = username;
        this.email = email;
        this.password = password;
        this.web = web;
    }

    public static Thing<UserResource, ?> create(String username, String email, String password) {
        return create(Optional.empty(), username, email, password);
    }

    public static Thing<UserResource, ?> create(Optional<WebSettings> webSettings, String username, String email, String password) {
        WebSettings ws = webSettings.orElse(new WebSettings());
        ws.setId(username);
        ws.setProfileDirName(Optional.of(String.format("%s-%s", Web.CHROME_PROFILE_DIR_NAME, username)));

        Web web;
        try {
            web = new Web().create(ws);
        } catch (Exception e) {
            return Thing.ofError("Can't create Web Token for the user");
        }

        return Thing.of(new UserResource(username, email, password, web));
    }

    public ChromeDriver driver() {
        return web.driver();
    }

    public boolean requireLogin(boolean firstRun) {
        boolean result = false;
        ProfilePage.activateFast(web);
        if (!ProfilePage.activated(web) && LoginPage.activated(web)) {
            logger.info("We're logged out, trying to log in using automation");
            if (firstRun || !login(true)) {
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
        } else if (ProfilePage.activated(web)){
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
        //web.debugShowBrowser(ProfilePage.HABR_SETTINGS_PAGE);
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
