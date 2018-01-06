package com.textorio.habrahabr.smartapi.core.lang.Downloading;

import com.textorio.habrahabr.smartapi.core.lang.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Downloader {
    private static Logger logger = LoggerFactory.getLogger(Downloader.class);

    public static void download(List<String> sources, String dest, boolean replaceIfExists, String message, boolean useGUI, boolean logProgress, boolean blockThread) {
        FS.ensureDirectoryExists(dest);

        if (useGUI) {
            downloadWithGUI(sources, dest, replaceIfExists, message, logProgress, blockThread);
        } else {
            downloadSimple(sources, dest, replaceIfExists, message, logProgress);
        }
    }

    public static void downloadSimple(List<String> sources, String dest, boolean replaceIfExists, String message, boolean logProgress) {
        if (null == sources || sources.size() <= 0) {
            return;
        }

        logger.info(String.format("Downloader: %s", message));
        List<Download> downloads = Download.createList(sources, dest, logProgress).crashIfInvalid("Really need to create list of downloads").get();
        Download.run(downloads, replaceIfExists);
    }

    public static void downloadWithGUI(List<String> sources, String dest, boolean replaceIfExists,String message, boolean logProgress, boolean blockThread) {
        if (null == sources || sources.size() <= 0) {
            return;
        }

        boolean entendedLayout = sources.size() > 1;

        AtomicBoolean done = new AtomicBoolean(false);
        try {
            List<Download> downloads = Download.createList(sources, dest, logProgress).crashIfInvalid("Really need to create Download object").get();

            final JProgressBar currentProgressBar = new JProgressBar();
            currentProgressBar.setValue(0);
            currentProgressBar.setMaximum(100);
            currentProgressBar.setStringPainted(true);
            currentProgressBar.setBorder(BorderFactory.createTitledBorder("Progress of the current file"));

            final JProgressBar fullProgressBar = new JProgressBar();
            fullProgressBar.setValue(0);
            fullProgressBar.setMaximum(Download.calculateListSize(downloads));
            fullProgressBar.setStringPainted(true);
            fullProgressBar.setBorder(BorderFactory.createTitledBorder("Progress of everything"));

            JFrame mainFrame = new JFrame("Downloader");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Container contentPane = mainFrame.getContentPane();

            final JButton btnDownload = new JButton(message);
            contentPane.add(btnDownload);

            JPanel progressPanel = new JPanel( new GridLayout(0, 1) );

            progressPanel.add(currentProgressBar, BorderLayout.SOUTH);

            if (entendedLayout) {
                progressPanel.add(fullProgressBar, BorderLayout.SOUTH);
            }

            contentPane.add(progressPanel, BorderLayout.SOUTH);

            btnDownload.addActionListener(e -> new Thread(() -> {
                Download.run(downloads, replaceIfExists, (Download download, Integer fullProgress) -> {
                    currentProgressBar.setMaximum(download.getSize());
                    currentProgressBar.setValue(download.getSumCount());
                    fullProgressBar.setValue(fullProgress);
                });
                done.set(true);
            }).start());

            mainFrame.setSize(300, entendedLayout ? 200 : 150);
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
