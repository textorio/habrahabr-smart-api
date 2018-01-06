package com.textorio.habrahabr.smartapi.core.lang;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class FS {
    private static Logger logger = LoggerFactory.getLogger(FS.class);

    public static boolean fileExists(String file) {
        return new File(file).exists();
    }

    public static void ensureFileRemoved(String dest) {
        ensureFileRemoved(dest, true);
    }

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

    public static File ensureDirectoryExists(String dest) {
        try {
            File directory = new File(dest);
            if (! directory.isDirectory()) {
                FileUtils.forceDelete(directory);
                logger.warn(String.format("There was a file with name %s instead of a directory. Removed it completely. Sorry.", dest));
            }
            FileUtils.forceMkdir(directory);
            return directory;
        } catch (IOException e) {
            logger.error(String.format("Directory %s should be created, but I can't do this", dest), e);
        }
        return null;
    }
}
