package com.textorio.habrahabr.smartapi.core.api.pages;

import com.textorio.habrahabr.smartapi.core.webdriver.Web;

public class ProfilePage {
    private static final String XPATH_EXIT = ".//*[@class='exit']";
    public static final String HABR_SETTINGS_PAGE = "https://id.tmtm.ru/settings/";


    public static void activateFast(Web web) {
        activate(web, 0);
    }

    public static void activateSlow(Web web) {
        activate(web, web.getSettings().getPageTimeout());
    }

    public static void activate(Web web, long mills) {
        web.driver().get(HABR_SETTINGS_PAGE);
    }

    public static boolean activated(Web web) {
        return web.elementExists(XPATH_EXIT);
    }
}
