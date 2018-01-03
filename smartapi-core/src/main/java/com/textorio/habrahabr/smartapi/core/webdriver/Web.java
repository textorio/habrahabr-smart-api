package com.textorio.habrahabr.smartapi.core.webdriver;

import com.assertthat.selenium_shutterbug.core.PageSnapshot;
import com.assertthat.selenium_shutterbug.core.Shutterbug;
import com.textorio.habrahabr.smartapi.core.lang.Thing;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.List;

import static com.assertthat.selenium_shutterbug.utils.web.ScrollStrategy.BOTH_DIRECTIONS;

/**
 * https://sites.google.com/a/chromium.org/chromedriver
 * https://sites.google.com/a/chromium.org/chromedriver/downloads
 */
public class Web {
    private static Logger logger = LoggerFactory.getLogger(Web.class);
    //@see https://sites.google.com/a/chromium.org/chromedriver/getting-started
    public static final String WEBDRIVER_BIN_PROPERTY_NAME = "webdriver.chrome.driver";
    public static final String WEBDRIVER_VERSION = "2.34";
    public static final String WEBDRIVER_SUFFIX_LINUX = "linux64";
    public static final String WEBDRIVER_SUFFIX_MAC = "mac64";
    public static final String WEBDRIVER_SUFFIX_WIN = "win32.exe";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36";
    public static final String CHROME_PROFILE_DIR_NAME = "smartapi-chrome-profile";
    public static final int BROWSER_WIDTH = 800;
    public static final int BROWSER_HEIGHT = 800;
    public static final int DEBUG_SCREENSHOT_SLEEP_INTERVAL = 100;

    public static List<String> DEFAULT_CHROME_OPTS = new ArrayList<>() {{
        add(String.format("--user-agent=%s", USER_AGENT));
        addAll(Arrays.asList("--disable-gpu", "--no-sandbox"));
        add("--incognito");
    }};

    private ChromeDriver driver;
    private WebSettings settings;

    public Web init() {
        return init(new WebSettings());
    }

    public void stop() {
        driver.quit();
    }

    public Web init(WebSettings settings) {
        this.settings = settings;
        enableDriverBinary();
        initializeChromeDriver();
        return this;
    }

    public void restartVisible() {
        logger.info("Restarting in visible (non-headless) mode.");
        stop();
        settings.setVisible(Optional.of(true));
        initializeChromeDriver();
        logger.info("Restarting in visible (non-headless) mode - finiashed.");
    }

    public void restartInvisible() {
        logger.info("Restarting in invisible (headless) mode.");
        stop();
        settings.setVisible(Optional.of(false));
        initializeChromeDriver();
        logger.info("Restarting in invisible (headless) mode - finished.");
    }

    public Thing<ChromeDriver, ?> createChromeDriver(Optional<String> profileDirName, Optional<Boolean> visible) {
        try {
            ChromeOptions chromeOptions = prepareChromeOptions(profileDirName, visible).raiseIfInvalid("Chrome options should be prepared OK").get();
            ChromeDriver chromeDriver = new ChromeDriver(chromeOptions);
            return Thing.of(chromeDriver, "Default Chrome driver");
        } catch (Exception e) {
            return Thing.ofError("Can't create Chrome Driver with desired capabilities");
        }
    }

    public void initializeChromeDriver() {
        driver = createChromeDriver(settings.getProfileDirName(), settings.getVisible()).raiseIfInvalid("Chrome driver should be prepared").get();
        final Dimension windowSize = new Dimension(BROWSER_WIDTH, BROWSER_HEIGHT); //1080p just for my streaming perversions :
        driver.manage().window().setSize(windowSize);
    }

    public Thing<ChromeOptions, ?> prepareChromeOptions(Optional<String> profileDirName, Optional<Boolean> visible) {
        try {
            final ChromeOptions chromeOptions = new ChromeOptions();
            //@see https://sites.google.com/a/chromium.org/chromedriver/capabilities#TOC-Using-a-Chrome-executable-in-a-non-standard-location
            chromeOptions.setBinary(findChromeExecutable().raiseIfInvalid("Propertly installed Chrome is required").get());
            chromeOptions.addArguments(DEFAULT_CHROME_OPTS);

            if (! (null != visible && visible.isPresent() && visible.get()) ) {
                chromeOptions.addArguments("--headless");
            }

            String chromeProfileDir = findProfileDirectory(profileDirName).raiseIfInvalid("Really need Chrome profile dir").get();
            chromeOptions.addArguments(String.format("user-data-dir=%s", chromeProfileDir));

            return Thing.of(chromeOptions);
        } catch (Exception e) {
            return Thing.ofError("Can't prepare chrome options");
        }
    }

    public void enableDriverBinary() {
        String driverPath = prepareDriver().raiseIfInvalid("nothing to do without temporary driver").get();
        System.setProperty(WEBDRIVER_BIN_PROPERTY_NAME, driverPath);
    }

    public Thing<String, ?> prepareDriver() {
        return findDriverInResources().raiseIfInvalid("nothing to do without a driver").extract((String driverResource) -> {
            logger.info(String.format("Driver resource: %s",driverResource));
            File tempDirectory = FileUtils.getTempDirectory();
            final File driverFile = new File(tempDirectory, driverResource);
            logger.info(String.format("Temp directory: %s",tempDirectory));

            if (!driverFile.exists()) {
                // All this variants are not working, including Guava. Deal with it.
                //URL resource = Thread.currentThread().getContextClassLoader().getResource(driverResource);
                //URL resource = this.getClass().getClassLoader().getResource(driverResource);
                //URL resource = Resources.getResource(this.getClass(), driverResource);

                URL resource = getClass().getClassLoader().getResource(driverResource);
                try (InputStream source = resource.openStream()) {
                    FileUtils.copyInputStreamToFile(source, driverFile);
                    driverFile.setExecutable(true);
                    logger.info(String.format("Created a new driver file: %s",driverFile.getAbsolutePath()));
                } catch (Exception ex) {
                    return Thing.ofError(ex, "Can't copy stream with a driver");
                }
            }
            return Thing.of(driverFile.getPath());
        });
    }

    /**
     * OMG WHAT A MONKEY CODE! Where's your hashmap magic dude?
     * @return
     */
    public static Thing<String, ?> findDriverInResources() {
        String suffix;
        if (SystemUtils.IS_OS_LINUX) {
            suffix = WEBDRIVER_SUFFIX_LINUX;
        } else if (SystemUtils.IS_OS_MAC) {
            suffix = WEBDRIVER_SUFFIX_MAC;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            suffix = WEBDRIVER_SUFFIX_WIN;
        } else {
            return Thing.ofError("Unknown operating system");
        }

        String resultString = String.format("textorio-chromedriver-bin/release/%s/chromedriver_%s", WEBDRIVER_VERSION, suffix);
        return Thing.of(resultString);
    }

    /**
     * OMG WHAT A MONKEY CODE!
     * @return
     */
    public static Thing<String, ?> findChromeExecutable() {
        String path;
        if (SystemUtils.IS_OS_LINUX) {
            path = "/usr/bin/google-chrome"; //or /usr/bin/google-chrome-unstable
        } else if (SystemUtils.IS_OS_MAC) {
            path = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
        } else if (SystemUtils.IS_OS_WINDOWS) {
            path = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
        } else {
            return Thing.ofError("Unknown operating system");
        }

        return Thing.of(path);
    }

    public Thing<String, ?> findProfileDirectory(Optional<String> profileDirName) {
        String profileDir = FileUtils.getTempDirectoryPath() + (profileDirName.orElse(CHROME_PROFILE_DIR_NAME));
        try {
            FileUtils.forceMkdir(new File(profileDir));
            logger.info(String.format("Embedded Chrome profile directory is: %s", profileDir));
        } catch (IOException e) {
            return Thing.ofError("Can't create Embedded Chrome profile directory");
        }
        return Thing.of(profileDir);
    }

    public ChromeDriver driver() {
        return driver;
    }

    public void setDriver(ChromeDriver driver) {
        this.driver = driver;
    }

    public void setAttribute(WebElement element, String attName, String attValue) {
        driver.executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);", element, attName, attValue);
    }

    public Image screenshot() {
        return Shutterbug.shootPage(driver, BOTH_DIRECTIONS).getImage();
    }

    public void debugScreenshot() {
        ImageViewer.run(screenshot(), BROWSER_WIDTH, BROWSER_HEIGHT);
        while(true) {
            try {
                Thread.sleep(DEBUG_SCREENSHOT_SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                logger.error("Swing GUI main cycle was interrupted", e);
            }
        }
    }
}
