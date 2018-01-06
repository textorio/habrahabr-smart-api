package com.textorio.habrahabr.smartapi.core;

import com.textorio.habrahabr.smartapi.core.lang.Downloading.Downloader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class DownloadTest {
    @Test
    public void downloadTest() {
        String file = "https://github.com/textorio/textorio-chromedriver-bin/raw/master/release/70e554ab5501db7d935af951dea987919642292b/chromedriver_win32.exe";
        Downloader.download(
                Arrays.asList(file,file),
                "C:/temp/something.exe",
                true,
                "Download ChromeDriver",
                true,
                true,
                true);
    }
}
