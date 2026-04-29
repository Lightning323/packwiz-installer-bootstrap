package com.lightning323.packInstaller.utils;

public class UIUtils {
    public static void detachedAlert(String title, String message) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Minimalist PowerShell msg box
                new ProcessBuilder("msg", "*", message).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("osascript", "-e",
                        String.format("display notification \"%s\" with title \"%s\"", message, title)).start();
            } else {
                // Linux (requires libnotify/zenity)
                new ProcessBuilder("notify-send", title, message).start();
            }
        } catch (Throwable t) {
            System.out.println("Could not trigger popup: " + message);
        }
    }
}
