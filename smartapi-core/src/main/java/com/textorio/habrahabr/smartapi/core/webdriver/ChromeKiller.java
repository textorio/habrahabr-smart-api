package com.textorio.habrahabr.smartapi.core.webdriver;

import com.textorio.habrahabr.smartapi.core.api.UserResource;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChromeKiller {
    private static Logger staticLogger = LoggerFactory.getLogger(UserResource.class);

    public static void main(String[] args) {
        killChrome();
    }

    public static void killChrome()  {
        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                //Runtime.getRuntime().exec("TASKKILL /IM chrome.exe /F");
                Runtime.getRuntime().exec("TASKKILL /IM textorio-chrome.exe /F");
                Runtime.getRuntime().exec("TASKKILL /IM chromedriver_win32.exe /F");
                Runtime.getRuntime().exec("TASKKILL /IM chromedriver.exe /F");
            } else if (SystemUtils.IS_OS_LINUX) {
                //Runtime.getRuntime().exec("killall google-chrome");
                Runtime.getRuntime().exec("killall textorio-chrome");
                Runtime.getRuntime().exec("killall chromedriver_linux64");
                Runtime.getRuntime().exec("killall chromedriver");
            } else if (SystemUtils.IS_OS_MAC) {
                Runtime.getRuntime().exec("killall textorio-chrome");
                Runtime.getRuntime().exec("killall chromedriver_mac64");
                Runtime.getRuntime().exec("killall chromedriver");
            } else {
                throw new NotImplementedException("Only Windows, Mac and GNU/Linux are supported");
            }
        } catch (Exception e) {
            staticLogger.error("Can't kill Chrome using ChromeKiller", e);
        }
    }
}
