package com.textorio.habrahabr.smartapi.core.webdriver;

import com.textorio.habrahabr.smartapi.core.lang.Thing;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

/**
 * https://sites.google.com/a/chromium.org/chromedriver
 * https://sites.google.com/a/chromium.org/chromedriver/downloads
 */
public class Webdriver {
    Logger logger = LoggerFactory.getLogger(Webdriver.class);

    //@see https://sites.google.com/a/chromium.org/chromedriver/capabilities#TOC-Using-a-Chrome-executable-in-a-non-standard-location
    //@see https://sites.google.com/a/chromium.org/chromedriver/getting-started
    public static final String WEBDRIVER_BIN_PROPERTY_NAME = "webdriver.chrome.driver";
    public static final String WEBDRIVER_VERSION = "2.34";
    public static final String WEBDRIVER_SUFFIX_LINUX = "linux64";
    public static final String WEBDRIVER_SUFFIX_MAC = "mac64";
    public static final String WEBDRIVER_SUFFIX_WIN = "win32.exe";

    public void enableDriver() {
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
}
