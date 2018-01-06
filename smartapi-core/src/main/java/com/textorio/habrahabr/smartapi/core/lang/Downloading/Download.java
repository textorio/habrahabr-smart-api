package com.textorio.habrahabr.smartapi.core.lang.Downloading;

import com.textorio.habrahabr.smartapi.core.lang.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

public class Download {
    private static Logger logger = LoggerFactory.getLogger(Download.class);

    private URL url;
    private int sumCount;
    private int size;
    private String dest;
    private boolean logProgress;

    public Download() {
    }

    public static Thing<Download, ?> create(String source, String dest, boolean logProgress) {
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
            download.setDest(dest);
            download.setLogProgress(logProgress);
            return Thing.of(download, String.format("Download for URL: %s", source));
        } catch (Exception e) {
            return Thing.ofError("Can't initialize download");
        }
    }

    public static void run(Download download) {
        run(download, null);
    }

    public static void run(Download download, Consumer<Integer> updater) {
        try (BufferedInputStream in = new BufferedInputStream(download.getUrl().openStream());
             FileOutputStream out = new FileOutputStream(download.getDest());
        ) {
            byte data[] = new byte[1024];
            int count;

            while ((count = in.read(data, 0, 1024)) != -1) {
                out.write(data, 0, count);
                download.setSumCount(download.getSumCount() + count);
                if (download.getSize() > 0) {
                    System.out.println("Percentace: " + ( download.getSumCount() * 1.0 / download.getSize() * 100.0) + "%");
                    if (null != updater) {
                        updater.accept(download.getSumCount());
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Can't download file", ex);
        }
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
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
}
