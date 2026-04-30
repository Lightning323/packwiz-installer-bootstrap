package com.lightning323.packInstaller.utils;

import com.lightning323.packInstaller.FileCleanup;
import com.lightning323.packInstaller.PackInstaller;
import com.lightning323.packInstaller.fileTypes.FileEntry;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static com.lightning323.packInstaller.PackInstaller.PATHS_TO_SPARE;

public class IOUtils {


    public static void checkAndDownloadFile(URL baseUrl, File baseSaveDir, String hashFormat,
                                            FileEntry entry)
            throws IOException, SecurityException, URISyntaxException, InterruptedException {

        URL fileURL = getRelativeUrl(baseUrl, entry.file());

        HttpURLConnection conn = (HttpURLConnection) fileURL.openConnection();
        File outFile = new File(baseSaveDir, entry.file());
        File dir = outFile.getParentFile();
        dir.mkdirs();
        //Add the directory to the list of downloaded directories
        FileCleanup.add(dir.toPath());
        if (outFile.exists()) {
            //If a file already exist, check if they are the same
            byte[] existingFile = Files.readAllBytes(outFile.toPath());
            String existingFileHash = HashUtils.getHash(hashFormat, existingFile);
            if (existingFileHash.equals(entry.hash())) {
                return; //The files are the same
            }

            //Check if the file is in the DONT_OVERWRITE list
            if (!PackInstaller.FULL_RESET && PATHS_TO_SPARE.contains(Paths.get(entry.file()))) {
                System.out.println("Skipping: " + entry.file());
                return;
            }
        }

        //Overwrite/write the file
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        System.out.println("Downloading: " + entry.file());
        try (var inputStream = conn.getInputStream()) {
            writer.write(inputStream.readAllBytes());
        }
        writeFile(hashFormat, writer.toByteArray(), outFile, entry.hash());
    }

    public static void writeFile(String hashFormat, byte[] bytes, File outFile, String hash) throws IOException {
        //Verify the hash
        if (!HashUtils.getHash(hashFormat, bytes).equals(hash)) {
            throw new IOException("Hash for \"" + outFile.toPath() + "\" does not match!");
        }
        Files.write(outFile.toPath(), bytes);
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
