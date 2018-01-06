package com.textorio.habrahabr.smartapi.core.lang;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class FS {
    private static Logger logger = LoggerFactory.getLogger(FS.class);

    public static void ensureFileRemoved(String dest, boolean reallyNeedToDoThis) {
        File outFile = new File(dest);
        if (outFile.exists()) {
            if (reallyNeedToDoThis) {
                try {
                    FileUtils.forceDelete(outFile);
                } catch (IOException e) {
                    logger.error(String.format("File %s should be removed, but I can't do this", dest), e);
                }
            } else {
                return;
            }
        }
    }
}
