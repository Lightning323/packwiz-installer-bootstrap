package com.lightning323.packInstaller;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.lightning323.packInstaller.fileTypes.FileEntry;
import com.lightning323.packInstaller.fileTypes.IndexFile;
import com.lightning323.packInstaller.fileTypes.PackConfig;
import com.lightning323.packInstaller.utils.IOUtils;
import com.lightning323.packInstaller.utils.ModDownloader;
import com.lightning323.packInstaller.utils.UIUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.lightning323.packInstaller.utils.IOUtils.fetchString;
import static com.lightning323.packInstaller.utils.IOUtils.getRelativeUrl;

public class Main {
    static private final ExecutorService workerPool = Executors.newFixedThreadPool(8);

    public static void main(String[] args) throws MalformedURLException {
        URL PACK_TOML_URL = new URL("https://raw.githubusercontent.com/Lightning323/MC-Terranova/refs/heads/main/pack/pack.toml");
        File SAVE_DIR = new File("./test_save/");

        // Setup Mapper
        TomlMapper mapper = new TomlMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

        try {
            AtomicBoolean popup = new AtomicBoolean(true);
            long startTime = System.currentTimeMillis();
            System.out.println("Fetching pack configuration...");
            String packContent = fetchString(PACK_TOML_URL);

            // Deserialize PackConfig
            PackConfig config = mapper.readValue(packContent, PackConfig.class);

            System.out.println("--- Pack Info ---");
            System.out.println("Name: " + config.name);
            if (config.versions != null) {
                System.out.println("Minecraft Version: " + config.versions.get("minecraft"));
            }


            if (config.index != null) {
                System.out.println("\n--- Index ---");
                if (config.index.file == null) throw new IllegalArgumentException("Index file cannot be null");
                System.out.println("Index File Path: " + config.index.file);
                if (config.index.hashFormat == null) throw new IllegalArgumentException("Hash type cannot be null");
                System.out.println("Hash Format: " + config.index.hashFormat);
                if (config.index.hash == null) throw new IllegalArgumentException("Hash cannot be null");
                System.out.println("Hash: " + config.index.hash);

                //Get the index.toml
                URL indexURL = getRelativeUrl(PACK_TOML_URL, config.index.file);
                String indexContent = fetchString(indexURL);
                IndexFile indexData = mapper.readValue(indexContent, IndexFile.class);
                SAVE_DIR.mkdirs();
                AtomicBoolean stop = new AtomicBoolean(false);

                for (FileEntry entry : indexData.files) {
                    if (!stop.get()) workerPool.submit(() -> {
                        try {
                            IOUtils.checkAndDownloadFile(indexURL, SAVE_DIR, config.index.hashFormat, entry);
                            ModDownloader.checkAndDownloadMod(entry, SAVE_DIR);

                            if (System.currentTimeMillis() - startTime > 3000 && popup.get()) {
                                popup.set(false);
                                UIUtils.detachedAlert("Beginning download", "Starting download for " + config.name);
                            }

                        } catch (Exception e) {
                            System.err.println("Failed to download " + entry.file());
                            stop.set(true);
                            e.printStackTrace();
                        }
                    });
                }

                //Wait for all tasks to complete
                workerPool.shutdown();
                if (!workerPool.awaitTermination(10, TimeUnit.MINUTES)) {
                    workerPool.shutdownNow();
                }
                System.out.println("\n--- Download Complete ---");
                FileCleanup.deleteUnIncludedFiles(SAVE_DIR, indexData,false);
                System.out.println("\n--- Cleanup Complete ---");

                if (System.currentTimeMillis() - startTime > 3000) {
                    UIUtils.detachedAlert("Download complete", "Download complete for " + config.name);
                }
            } else {
                System.err.println("No index found!");
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            Runtime.getRuntime().exit(1);
        }
    }


}