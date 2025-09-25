package com.github.AlexanderZobkov.wallflux;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import static com.github.AlexanderZobkov.wallflux.WindowsWallpaperChanger.User32.SPIF_SENDWININICHANGE;
import static com.github.AlexanderZobkov.wallflux.WindowsWallpaperChanger.User32.SPIF_UPDATEINIFILE;

public class WindowsWallpaperChanger {

    public interface User32 extends StdCallLibrary {

        int SPI_SETDESKWALLPAPER = 20;
        int SPIF_UPDATEINIFILE = 0x01;
        int SPIF_SENDWININICHANGE = 0x02;

        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

        // https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-systemparametersinfow
        boolean SystemParametersInfoW(
                int uiAction,
                int uiParam,
                String pvParam,
                int fWinIni
        );

    }

    /**
     * Changes wallpaper.
     *
     * @param filePath full path the imageUrl.
     */
    public static void change(final String filePath) {
        final boolean ok = User32.INSTANCE.SystemParametersInfoW(
                User32.SPI_SETDESKWALLPAPER, 0, filePath,
                SPIF_UPDATEINIFILE | SPIF_SENDWININICHANGE
        );
        System.out.println("Wallpaper set: " + ok);
    }

}
