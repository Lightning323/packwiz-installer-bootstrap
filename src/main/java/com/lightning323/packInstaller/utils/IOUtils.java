package com.lightning323.packInstaller.utils;

import com.lightning323.packInstaller.fileTypes.FileEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.stream.Collectors;

public class IOUtils {

    static HashSet<File> downloadedDirectories = new HashSet<>();

    public static void downloadFile(URL baseUrl, File baseSaveDir, FileEntry entry) throws Exception {
        URL fileURL = getRelativeUrl(baseUrl, entry.file());

        HttpURLConnection conn = (HttpURLConnection) fileURL.openConnection();
        File outFile = new File(baseSaveDir, entry.file());
        File dir = outFile.getParentFile();
        downloadedDirectories.add(dir);
        dir.mkdirs();
        try (var inputStream = conn.getInputStream();
             FileWriter writer = new FileWriter(outFile)) {
            writer.write(new String(inputStream.readAllBytes()));
        }
    }

    public static URL getRelativeUrl(URL baseUrl, String relativePath) throws Exception {
        URI resolvedUri = baseUrl.toURI().resolve(relativePath);
        return new URL(resolvedUri.toString());
    }

    /**
     * Helper to fetch String content from a URL
     */
    public static String fetchString(URL url) throws Exception {

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
