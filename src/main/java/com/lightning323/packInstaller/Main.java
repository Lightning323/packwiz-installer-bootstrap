package com.lightning323.packInstaller;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.lightning323.packInstaller.fileTypes.FileEntry;
import com.lightning323.packInstaller.fileTypes.IndexFile;
import com.lightning323.packInstaller.fileTypes.PackConfig;
import com.lightning323.packInstaller.utils.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import static com.lightning323.packInstaller.utils.IOUtils.fetchString;
import static com.lightning323.packInstaller.utils.IOUtils.getRelativeUrl;

public class Main {
    public static void main(String[] args) throws MalformedURLException {
        URL packTomlURL = new URL("https://raw.githubusercontent.com/Lightning323/MC-Terranova/refs/heads/main/pack/pack.toml");

        // Setup Mapper
        TomlMapper mapper = new TomlMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

        try {
            System.out.println("Fetching pack configuration...");
            String packContent = fetchString(packTomlURL);

            // Deserialize PackConfig
            PackConfig config = mapper.readValue(packContent, PackConfig.class);

            System.out.println("--- Pack Info ---");
            System.out.println("Name: " + config.name);
            if (config.versions != null) {
                System.out.println("Minecraft Version: " + config.versions.get("minecraft"));
            }

            if (config.index != null) {
                System.out.println("\n--- Index ---");
                System.out.println("Index File Path: " + config.index.file);
                System.out.println("Hash Format: " + config.index.hashFormat);
                System.out.println("Hash: " + config.index.hash);

                //Get the index.toml
                URL indexURL = getRelativeUrl(packTomlURL, config.index.file);
                String indexContent = fetchString(indexURL);
                IndexFile indexData = mapper.readValue(indexContent, IndexFile.class);

                File saveDir = new File("./test_save/");
                saveDir.mkdirs();

                int i = 0;
                for (FileEntry entry : indexData.files) {
                    i++;
                    processFile(indexURL, saveDir, entry, i, indexData.files.size());
                }
            } else {
                System.err.println("No index found!");
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processFile(URL baseURL, File saveDir, FileEntry entry, int index, int totalFiles) throws Exception {
        System.out.println("File (" + index + " of " + totalFiles + "): " + entry.file());
        IOUtils.downloadFile(baseURL, saveDir, entry);
    }


}