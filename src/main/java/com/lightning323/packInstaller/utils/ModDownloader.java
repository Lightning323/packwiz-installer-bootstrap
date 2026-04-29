package com.lightning323.packInstaller.utils;

import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.lightning323.packInstaller.fileTypes.FileEntry;
import com.lightning323.packInstaller.fileTypes.ModFile;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModDownloader {

    private static final String CURSEFORGE_BASE_URL = "https://www.curseforge.com/api/v1/mods/%d/files/%d/download";
    private static final String MODRINTH_BASE_URL = "https://cdn.modrinth.com/data/%s/versions/%s/%s";
    public static final String MOD_TOML_FILE_EXT = ".pw.toml";

    private static final TomlMapper mapper = new TomlMapper();

    public static ModFile getFileEntry(File pwTomlFile) throws IOException {
        return mapper.readValue(pwTomlFile, ModFile.class);
    }

    public static void checkAndDownloadMod(FileEntry entry, File destinationDir) throws IOException, InterruptedException {
        if (entry.file().endsWith(MOD_TOML_FILE_EXT)) {

            File pwTomlFile = new File(destinationDir, entry.file());
            ModFile modToml = getFileEntry(pwTomlFile);

            Path jarOutputPath = new File(pwTomlFile.getParentFile(), modToml.filename).toPath();

            if (Files.exists(jarOutputPath) //If the jar already exists and its hash matches
                    && HashUtils.getHash(modToml.download.hashFormat, Files.readAllBytes(jarOutputPath)).equals(modToml.download.hash)) {
                return;
            }


            // 1. Construct the download URL using the IDs from the config
            String url = null;

            if (modToml.update.curseforge != null) {
                url = String.format(CURSEFORGE_BASE_URL,
                        modToml.update.curseforge.projectId,
                        modToml.update.curseforge.fileId);
            } else if (modToml.update.modrinth != null) {
                url = String.format(MODRINTH_BASE_URL,
                        modToml.update.modrinth.modId,
                        modToml.update.modrinth.version,
                        modToml.filename);
            } else {
                throw new RuntimeException("Invalid mod update URL");
            }
            System.out.println("Downloading mod: " + modToml.filename + " \t (" + url + ")..");

            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            // 2. Send the request
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() == 200) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (InputStream is = response.body()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                }
                IOUtils.writeFile(modToml.download.hashFormat, baos.toByteArray(), jarOutputPath.toFile(), modToml.download.hash);
            } else {
                throw new RuntimeException("Failed to download mod. HTTP Status: " + response.statusCode());
            }
        }
    }
}