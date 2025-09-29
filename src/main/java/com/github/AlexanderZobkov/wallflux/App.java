package com.github.AlexanderZobkov.wallflux;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;

public class App {

    private final AtomicReference<WallpaperDescriptor> wallpaperRef = new AtomicReference<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final BingWallpaperDownloader wallpaperDownloader = new BingWallpaperDownloader();
    private TrayIcon trayIcon;

    public App() {
        try {
            initUI();
            scheduleAutoUpdate();
        } catch (Exception e) {
            ((ThrowingActionListener) e1 -> {
            }).handleException(e);
        }
    }

    private void scheduleAutoUpdate() {
        scheduler.scheduleAtFixedRate(() -> {
            EventQueue.invokeLater(() -> {
                try {
                    downloadAndChange();
                } catch (Exception e) {
                    ((ThrowingActionListener) e1 -> {
                    }).handleException(e);
                }
            });
        }, 0, 1, TimeUnit.HOURS);
    }

    private Image generateIcon() {
        final Image image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(128, 155, 206)); // blue
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        final FontMetrics fm = g2.getFontMetrics();
        final String text = "B";
        final int size = 16; // tray icons are usually 16x16
        final int x = (size - fm.stringWidth(text)) / 2;
        final int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, x, y);
        g2.dispose();
        return image;
    }

    private void initUI() throws AWTException {
        // Declare trayIcon first so we can use it in listeners
        trayIcon = new TrayIcon(generateIcon(), "WallFlux");
        trayIcon.setImageAutoSize(true);

        final PopupMenu popupMenu = new PopupMenu();

        // Refresh item
        final MenuItem refreshItem = new MenuItem("Refresh");
        refreshItem.addActionListener((ThrowingActionListener) e -> downloadAndChange());
        popupMenu.add(refreshItem);

        // Info item
        final MenuItem infoItem = new MenuItem("Show info");
        infoItem.addActionListener((ThrowingActionListener) e -> {
                    if (wallpaperRef.get() != null) {
                        Desktop.getDesktop().browse(wallpaperRef.get().infoLink().toURI());
                    }
                }
        );
        popupMenu.add(infoItem);

        // Exit item
        final MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            SystemTray.getSystemTray().remove(trayIcon);
            System.exit(0);
        });
        popupMenu.add(exitItem);

        trayIcon.setPopupMenu(popupMenu);

        // Add tray icon
        SystemTray.getSystemTray().add(trayIcon);
    }

    private void downloadAndChange() throws IOException {
        final WallpaperDescriptor newWallpaperDescr = wallpaperDownloader.getPictureOfTheDayDescriptor();
        final WallpaperDescriptor currentWallpaperDescr = wallpaperRef.get();

        if (currentWallpaperDescr == null
                || (!currentWallpaperDescr.imageUrl().getQuery().equals(newWallpaperDescr.imageUrl().getQuery())) ) {
            wallpaperRef.set(newWallpaperDescr);
            final File imageFile = wallpaperDownloader.download(newWallpaperDescr.imageUrl());
            WindowsWallpaperChanger.change(imageFile.getAbsolutePath());
            trayIcon.setToolTip(wallpaperRef.get().info());
        } else {
            System.out.println("Keep wallpaper as no a new image from Bing");
        }
    }

    public static void main(String[] args) {
        // Run with javaw.exe to avoid console window

        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(
                    null,
                    "System tray not supported on this platform.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }

        EventQueue.invokeLater(App::new);
    }

}
