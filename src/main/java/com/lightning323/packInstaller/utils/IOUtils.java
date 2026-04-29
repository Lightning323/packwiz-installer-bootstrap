package com.lightning323.packInstaller.utils;

import com.lightning323.packInstaller.DirectoryManager;
import com.lightning323.packInstaller.fileTypes.FileEntry;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.stream.Collectors;

public class IOUtils {


    public static void checkAndDownloadFile(URL baseUrl, File baseSaveDir, String hashFormat,
                                            FileEntry entry)
            throws IOException, SecurityException, URISyntaxException {

        URL fileURL = getRelativeUrl(baseUrl, entry.file());

        HttpURLConnection conn = (HttpURLConnection) fileURL.openConnection();
        File outFile = new File(baseSaveDir, entry.file());
        File dir = outFile.getParentFile();
        dir.mkdirs();
        //Add the directory to the list of downloaded directories
        DirectoryManager.add(dir.toPath());
        if (outFile.exists()) {
            //If a file already exist, check if they are the same
            byte[] existingFile = Files.readAllBytes(outFile.toPath());
            String existingFileHash = HashUtils.getHash(hashFormat, existingFile);
            if (existingFileHash.equals(entry.hash())) {
                return; //The files are the same
            }
        }
        //Overwrite/write the file
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        System.out.println("Downloading File: " + entry.file());
        try (var inputStream = conn.getInputStream()) {
            writer.write(inputStream.readAllBytes());
        }
        //Verify the hash
        String hash = HashUtils.getHash(hashFormat, writer.toByteArray());
        if (!hash.equals(entry.hash())) {
            throw new SecurityException("Hash for " + entry.file() + " does not match!");
        }
        Files.write(outFile.toPath(), writer.toByteArray());
    }

    public static URL getRelativeUrl(URL baseUrl, String relativePath) throws URISyntaxException, MalformedURLException {
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
