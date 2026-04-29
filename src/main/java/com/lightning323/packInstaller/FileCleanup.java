package com.lightning323.packInstaller;

import com.lightning323.packInstaller.fileTypes.FileEntry;
import com.lightning323.packInstaller.fileTypes.IndexFile;
import com.lightning323.packInstaller.fileTypes.ModFile;
import com.lightning323.packInstaller.utils.ModDownloader;

import java.io.IOException;
import java.nio.file.Path;
import java.io.File;
import java.util.HashSet;

import static com.lightning323.packInstaller.utils.ModDownloader.MOD_TOML_FILE_EXT;

public class FileCleanup {
    /**
     * Preventing added files from being deleted by the program
     * Each jar that is supposed to be here has a .pw.toml file
     * If a jar was added that does NOT have a .pw.toml file, it will be skipped
     */
    static HashSet<Path> downloadedDirectories = new HashSet<>();
//    static HashSet<Path> directoriesToSpare = new HashSet<>();
//
//    static {
//        directoriesToSpare.add(Path.of("mods/index"));
//        directoriesToSpare.add(Path.of("mods/connector"));
//    }

    public static void deleteUnIncludedFiles(File saveDir, IndexFile indexData, boolean fullCleanup) throws IOException {
        System.out.println("\n--- Deleting Unincluded Files ---");

        HashSet<Path> filesThatShouldExist = new HashSet<>();
        for (FileEntry fe : indexData.files) {
            if (fe.file().endsWith(MOD_TOML_FILE_EXT)) {
                //Add the jar file to the entry
                File pwTomlFile = new File(saveDir, fe.file());
                ModFile modFile = ModDownloader.getFileEntry(pwTomlFile);

                File jarFile = Path.of(fe.file())
                        .resolveSibling(modFile.filename)
                        .toFile();
                filesThatShouldExist.add(jarFile.toPath());
            }
            filesThatShouldExist.add(Path.of(fe.file()));
        }

        //Populate our jars with toml files
        HashSet<Path> jarsWithTomlFiles = new HashSet<>();
        for (File file : new File(saveDir, "mods").listFiles()) {
            if (file.getName().endsWith(MOD_TOML_FILE_EXT)) {
                ModFile modFile = ModDownloader.getFileEntry(file);
                Path jarFile = Path.of(file.getPath()).resolveSibling(modFile.filename);

                Path base = saveDir.toPath().toAbsolutePath().normalize();
                Path full = jarFile.toAbsolutePath().normalize();
                jarFile = base.relativize(full);
                jarsWithTomlFiles.add(jarFile);
            }
        }


        //Now delete files within downloaded directories that arent on the list
        FileCleanup.getDownloadedDirectories().forEach(path -> {
            //IMPORTANT SAFETY CHECK, make sure the path is inside the save directory
            if (!FileCleanup.isInsideOrEqual(path, saveDir.toPath()))
                throw new RuntimeException("Path " + path + " is not inside the save directory");
//            if (!fullCleanup) {
//                for (Path spareDir : directoriesToSpare) {
//                    if (FileCleanup.isInsideOrEqual(path, spareDir)) {
//                        System.out.println("Skipping directory: " + path);
//                        return;
//                    }
//                }
//            }
            fileLoop:
            for (File file : path.toFile().listFiles()) {
                if (file.exists() && !file.isDirectory()) {
                    Path base = saveDir.toPath().toAbsolutePath().normalize();
                    Path full = file.toPath().toAbsolutePath().normalize();
                    Path fileRelativePath = base.relativize(full);
                    //Check if the file is in the index
                    if (!filesThatShouldExist.contains(fileRelativePath)) {

                        //Spare jarfiles that dont have a toml file, because they were likely added manually
                        if (fileRelativePath.getFileName().toString().endsWith(".jar")
                                && !jarsWithTomlFiles.contains(fileRelativePath)
                                && !fullCleanup) {
                            System.out.println("Sparing jarfile " + fileRelativePath);
                            continue;
                        }
                        System.out.println("Deleting file: " + fileRelativePath);
                        file.delete();
                    }
                }
            }
        });
    }

    public static boolean isInsideOrEqual(Path child, Path parent) {
        // 1. Normalize and get absolute paths to handle "." or ".." or relative vs absolute
        Path absChild = child.toAbsolutePath().normalize();
        Path absParent = parent.toAbsolutePath().normalize();

        // 2. startsWith handles both "inside" and "equal to"
        return absChild.startsWith(absParent);
    }

    public static HashSet<Path> getDownloadedDirectories() {
        return downloadedDirectories;
    }

    public static synchronized void add(Path newPath) {
        downloadedDirectories.add(newPath);
    }
}