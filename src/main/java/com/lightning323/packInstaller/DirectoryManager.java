package com.lightning323.packInstaller;

import com.lightning323.packInstaller.fileTypes.FileEntry;
import com.lightning323.packInstaller.fileTypes.IndexFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;

public class DirectoryManager {
    static HashSet<Path> downloadedDirectories = new HashSet<>();

    public static void deleteUnIncludedFiles(File saveDir, IndexFile indexData) {
        System.out.println("\n--- Deleting Unincluded Files ---");
        HashSet<Path> indexFiles = new HashSet<>();
        for (FileEntry fe : indexData.files) {
            indexFiles.add(Path.of(fe.file()));
        }

        //Now delete files within downloaded directories that arent on the list
        DirectoryManager.getDownloadedDirectories().forEach(path -> {
            //IMPORTANT SAFETY CHECK, make sure the path is inside the save directory
            if (!DirectoryManager.isInsideOrEqual(path, saveDir.toPath()))
                throw new RuntimeException("Path " + path + " is not inside the save directory");

            fileLoop:
            for (File file : path.toFile().listFiles()) {
                if (file.exists() && !file.isDirectory()) {
                    Path base = saveDir.toPath().toAbsolutePath().normalize();
                    Path full = file.toPath().toAbsolutePath().normalize();
                    Path fileRelativePath = base.relativize(full);
                    //Check if the file is in the index
                    if (!indexFiles.contains(fileRelativePath)) {
                        System.out.println("Deleting file: " + file);
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