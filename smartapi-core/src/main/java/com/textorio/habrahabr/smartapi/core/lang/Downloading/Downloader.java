package com.textorio.habrahabr.smartapi.core.lang.Downloading;

import com.textorio.habrahabr.smartapi.core.lang.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * http://commons.apache.org/proper/commons-compress/examples.html
 * https://stackoverflow.com/questions/14069848/download-a-file-while-also-updating-a-jprogressbar
 * http://www.codejava.net/coding/swing-application-to-download-files-from-http-server-with-progress-bar
 */
public class Downloader {
    private static Logger logger = LoggerFactory.getLogger(Downloader.class);

    public static void download(String source, String dest, boolean replaceIfExists, String message, boolean useGUI, boolean logProgress, boolean blockThread) {
        FS.ensureFileRemoved(dest, replaceIfExists);

        if (useGUI) {
            downloadWithGUI(source, dest, message, logProgress, blockThread);
        } else {
            downloadSimple(source, dest, message, logProgress);
        }
    }

    public static void downloadSimple(String source, String dest, String message, boolean logProgress) {
        logger.info(String.format("Downloader: %s", message));
        Download download = Download.create(source, dest, logProgress).crashIfInvalid("Really need to create Download object").get();
        Download.run(download);
    }

    public static void downloadWithGUI(String source, String dest,String message, boolean logProgress, boolean blockThread) {
        AtomicBoolean done = new AtomicBoolean(false);
        try {
            final JProgressBar progressBar = new JProgressBar();
            progressBar.setValue(0);
            progressBar.setMaximum(100);
            progressBar.setStringPainted(true);
            progressBar.setBorder(BorderFactory.createTitledBorder("Progress"));

            JFrame mainFrame = new JFrame("Downloader");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Container contentPane = mainFrame.getContentPane();
            contentPane.add(progressBar, BorderLayout.SOUTH);
            final JButton btnDownload = new JButton(message);
            contentPane.add(btnDownload);

            Download download = Download.create(source, dest, logProgress).crashIfInvalid("Really need to create Download object").get();
            progressBar.setMaximum(download.getSize());

            btnDownload.addActionListener(e -> new Thread(() -> {
                Download.run(download, progressBar::setValue);
                done.set(true);
            }).start());

            mainFrame.setSize(300, 150);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (blockThread) {
            while (!done.get()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
