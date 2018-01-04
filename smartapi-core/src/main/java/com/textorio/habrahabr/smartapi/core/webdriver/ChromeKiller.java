package com.textorio.habrahabr.smartapi.core.webdriver;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;

public class ChromeKiller {
    public static void main(String[] args) throws IOException {
        killChrome();
    }

    public static void killChrome() throws IOException {
        if (SystemUtils.IS_OS_WINDOWS) {
            Runtime.getRuntime().exec("TASKKILL /IM chrome.exe /F");
            Runtime.getRuntime().exec("TASKKILL /IM chromedriver_win32.exe /F");
        } else if (SystemUtils.IS_OS_LINUX) {
            Runtime.getRuntime().exec("killall google-chrome");
            Runtime.getRuntime().exec("killall chromedriver_linux64");
        } else if (SystemUtils.IS_OS_MAC) {
            throw new NotImplementedException("Don't know how to do thin on Macs properly");
        } else {
            throw new NotImplementedException("Only Windows, Mac and GNU/Linux are supported");
        }
    }
}
