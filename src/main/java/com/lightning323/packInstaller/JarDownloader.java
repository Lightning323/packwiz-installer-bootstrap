package com.lightning323.packInstaller;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JarDownloader {

    public static void downloadJar(String downloadUrl, String outputFolder, String fileName) throws IOException {
        // 1. Ensure the directory exists
        Path path = Paths.get(outputFolder);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        System.out.println("Downloading: " + fileName + "...");

        URL url = new URL(downloadUrl);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // 2. Check for success (200 OK)
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedInputStream in = new BufferedInputStream(httpConn.getInputStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(outputFolder + "/" + fileName)) {

                byte[] dataBuffer = new byte[8192]; // 8KB buffer
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
                System.out.println("Finished: " + fileName);
            }
        } else {
            System.err.println("Failed to download file. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }
}
