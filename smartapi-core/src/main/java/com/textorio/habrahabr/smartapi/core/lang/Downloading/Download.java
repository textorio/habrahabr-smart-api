package com.textorio.habrahabr.smartapi.core.lang.Downloading;

import com.textorio.habrahabr.smartapi.core.lang.FS;
import com.textorio.habrahabr.smartapi.core.lang.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class Download {
    private static Logger logger = LoggerFactory.getLogger(Download.class);

    private URL url;
    private int sumCount;
    private int size;
    private String destDir;
    private String destName;
    private boolean logProgress;

    public Download() {
    }

    public static Thing<Download, ?> create(String source, String destDir, boolean logProgress) {
        try {
            Download download = new Download();
            URL url = new URL(source);
            download.setUrl(url);
            URLConnection conn = url.openConnection();
            int size = conn.getContentLength();
            download.setSize(size);
            if (size < 0) {
                //In principle we can handle unbounded files, but that's not tested
                logger.warn("Can't determine download size");
            } else {
                logger.info(String.format("File size: %d", size));
            }
            download.setSumCount(0);
            download.setDestDir(destDir);
            download.setDestName(getFileName(url));
            download.setLogProgress(logProgress);
            return Thing.of(download, String.format("Download for URL: %s", source));
        } catch (Exception e) {
            return Thing.ofError("Can't initialize download");
        }
    }

    public String getDestFilePath() {
        return destDir + (destDir.endsWith(File.separator) ? "" : File.separator) + destName;
    }

    public static String getFileName(URL url) {
        String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }

    public static Thing<List<Download>, ?> createList(List<String> sources, String destDir, boolean logProgress) {
        List<Download> downloads = new ArrayList<>();
        for (String source: sources) {
            downloads.add(create(source, destDir, logProgress).raiseIfInvalid("One of downloads in the list failed").get());
        }
        return Thing.of(downloads);
    }

    public static void run(List<Download> downloads, boolean replaceIfExists) {
        run(downloads, replaceIfExists, null);
    }

    public static int calculateListSize(List<Download> downloads) {
        int listSize = 0;
        for (Download download: downloads) {
            listSize += download.getSize();
        }
        return listSize;
    }

    public static void run(List<Download> downloads, boolean replaceIfExists, BiConsumer<Download, Integer> updater) {
        int curListSize = 0;

        for (Download download : downloads) {
            if (!replaceIfExists && FS.fileExists(download.getDestFilePath())) {
                continue;
            }
            FS.ensureFileRemoved(download.getDestFilePath(), replaceIfExists);

            try (BufferedInputStream in = new BufferedInputStream(download.getUrl().openStream());
                 FileOutputStream out = new FileOutputStream(download.getDestFilePath());
            ) {
                FS.ensureDirectoryExists(download.getDestDir());


                byte data[] = new byte[1024];
                int count;

                while ((count = in.read(data, 0, 1024)) != -1) {
                    out.write(data, 0, count);
                    download.setSumCount(download.getSumCount() + count);
                    curListSize += count;
                    if (download.getSize() > 0) {
                        System.out.println("Percentace: " + (download.getSumCount() * 1.0 / download.getSize() * 100.0) + "%");
                        if (null != updater) {
                            updater.accept(download, curListSize);
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Can't download file", ex);
            }
        }
    }

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public int getSumCount() {
        return sumCount;
    }

    public void setSumCount(int sumCount) {
        this.sumCount = sumCount;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isLogProgress() {
        return logProgress;
    }

    public void setLogProgress(boolean logProgress) {
        this.logProgress = logProgress;
    }

    public String getDestName() {
        return destName;
    }

    public void setDestName(String destName) {
        this.destName = destName;
    }
}
