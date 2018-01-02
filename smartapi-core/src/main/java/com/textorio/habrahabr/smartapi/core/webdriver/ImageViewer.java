package com.textorio.habrahabr.smartapi.core.webdriver;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageViewer {
    public static final int MARGIN = 50;
    private JPanel panel;
    private JLabel label;

    public void useImage(Image image) {
        label.setIcon(new ImageIcon(image));
    }

    public void initComponents(int width, int height) {
        if (panel ==null) {
            panel = new JPanel(new BorderLayout());
            panel.setBorder(new EmptyBorder(5,5,5,5));
            label = new JLabel();

            JPanel centeredImage = new JPanel(new GridBagLayout());
            centeredImage.add(label);
            JScrollPane scrolledImage = new JScrollPane(centeredImage);
            scrolledImage.setPreferredSize(new Dimension(width+MARGIN, height+MARGIN));
            panel.add(scrolledImage, BorderLayout.CENTER);
        }
    }

    public Container createPanels(int width, int height) {
        initComponents(width, height);
        return panel;
    }

    public static void run(Image img, int width, int height) {
        Runnable r = () -> {
            JFrame f = new JFrame("Image Viewer");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            final ImageViewer viewer = new ImageViewer();
            f.setContentPane(viewer.createPanels(width, height));

            f.pack();
            f.setLocationByPlatform(true);
            f.setVisible(true);

            viewer.useImage(img);
        };
        SwingUtilities.invokeLater(r);
    }

    public static Image generateTestImage(int width, int height) {
        return new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
    }
}