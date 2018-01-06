package com.textorio.habrahabr.smartapi.core;

import com.textorio.habrahabr.smartapi.core.lang.SevenZipArchiver;
import com.textorio.habrahabr.smartapi.core.lang.DirLocator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class Un7zipTest {
    @Test
    public void downloadTest() throws IOException {
        String archiveFilePath = "Z:\\git\\textorio-chromium-bin\\stable\\chrome-win32.test.7z";
        String sourceFilePath = "Z:\\git\\textorio-chromium-bin\\stable\\chrome-win32";
        SevenZipArchiver.compress(archiveFilePath, new File(sourceFilePath));
        SevenZipArchiver.decompress(archiveFilePath,  new File(DirLocator.getTemp() + File.separator + "chromium-temp"));
    }
}
