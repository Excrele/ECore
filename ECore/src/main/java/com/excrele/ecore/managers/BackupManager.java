package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Manages automatic backups of server data.
 * Supports scheduled backups, backup compression, and restoration.
 */
public class BackupManager {
    private final Ecore plugin;
    private int backupTaskId = -1;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    public BackupManager(Ecore plugin) {
        this.plugin = plugin;
        if (isEnabled()) {
            scheduleBackups();
        }
    }
    
    /**
     * Checks if backup system is enabled.
     */
    public boolean isEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("backup.enabled", false);
    }
    
    /**
     * Creates a backup of specified data.
     */
    public BackupResult createBackup(boolean manual) {
        if (!isEnabled() && !manual) {
            return new BackupResult(false, "Backup system is disabled");
        }
        
        FileConfiguration config = plugin.getConfigManager().getConfig();
        String backupDir = config.getString("backup.directory", "backups");
        File backupFolder = new File(plugin.getDataFolder().getParentFile().getParentFile(), backupDir);
        
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }
        
        String timestamp = dateFormat.format(new Date());
        String backupName = "backup_" + timestamp;
        File backupFile = new File(backupFolder, backupName + ".zip");
        
        try {
            List<String> filesToBackup = config.getStringList("backup.files");
            List<String> worldsToBackup = config.getStringList("backup.worlds");
            
            if (filesToBackup.isEmpty() && worldsToBackup.isEmpty()) {
                return new BackupResult(false, "No files or worlds configured for backup");
            }
            
            plugin.getLogger().info("Creating backup: " + backupName);
            
            // Create backup asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    createZipBackup(backupFile, filesToBackup, worldsToBackup);
                    
                    // Clean old backups
                    cleanOldBackups(backupFolder, config.getInt("backup.max-backups", 10));
                    
                    plugin.getLogger().info("Backup created successfully: " + backupName);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to create backup", e);
                }
            });
            
            return new BackupResult(true, "Backup started: " + backupName);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create backup", e);
            return new BackupResult(false, "Failed to create backup: " + e.getMessage());
        }
    }
    
    /**
     * Creates a ZIP backup of files and worlds.
     */
    private void createZipBackup(File backupFile, List<String> filesToBackup, List<String> worldsToBackup) throws IOException {
        File serverFolder = plugin.getDataFolder().getParentFile().getParentFile();
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupFile))) {
            // Backup plugin data files
            for (String filePath : filesToBackup) {
                File file = new File(serverFolder, filePath);
                if (file.exists()) {
                    addFileToZip(zos, file, filePath);
                }
            }
            
            // Backup world folders
            for (String worldName : worldsToBackup) {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    File worldFolder = world.getWorldFolder();
                    addDirectoryToZip(zos, worldFolder, "worlds/" + worldName);
                } else {
                    // Try to find world folder even if not loaded
                    File worldFolder = new File(serverFolder, worldName);
                    if (worldFolder.exists()) {
                        addDirectoryToZip(zos, worldFolder, "worlds/" + worldName);
                    }
                }
            }
            
            // Backup ECore plugin data
            File ecoreDataFolder = plugin.getDataFolder();
            if (ecoreDataFolder.exists()) {
                addDirectoryToZip(zos, ecoreDataFolder, "plugins/ECore");
            }
        }
    }
    
    /**
     * Adds a file to the ZIP archive.
     */
    private void addFileToZip(ZipOutputStream zos, File file, String entryName) throws IOException {
        if (file.isDirectory()) {
            addDirectoryToZip(zos, file, entryName);
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(entryName);
            zipEntry.setTime(file.lastModified());
            zos.putNextEntry(zipEntry);
            
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            
            zos.closeEntry();
        }
    }
    
    /**
     * Adds a directory to the ZIP archive recursively.
     */
    private void addDirectoryToZip(ZipOutputStream zos, File directory, String basePath) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            String entryName = basePath + "/" + file.getName();
            
            if (file.isDirectory()) {
                addDirectoryToZip(zos, file, entryName);
            } else {
                addFileToZip(zos, file, entryName);
            }
        }
    }
    
    /**
     * Cleans old backups, keeping only the most recent N backups.
     */
    private void cleanOldBackups(File backupFolder, int maxBackups) {
        File[] backups = backupFolder.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".zip"));
        
        if (backups == null || backups.length <= maxBackups) {
            return;
        }
        
        // Sort by last modified date (oldest first)
        Arrays.sort(backups, Comparator.comparingLong(File::lastModified));
        
        // Delete oldest backups
        int toDelete = backups.length - maxBackups;
        for (int i = 0; i < toDelete; i++) {
            if (backups[i].delete()) {
                plugin.getLogger().info("Deleted old backup: " + backups[i].getName());
            }
        }
    }
    
    /**
     * Lists all available backups.
     */
    public List<File> listBackups() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        String backupDir = config.getString("backup.directory", "backups");
        File backupFolder = new File(plugin.getDataFolder().getParentFile().getParentFile(), backupDir);
        
        if (!backupFolder.exists()) {
            return new ArrayList<>();
        }
        
        File[] backups = backupFolder.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".zip"));
        if (backups == null) {
            return new ArrayList<>();
        }
        
        // Sort by last modified date (newest first)
        Arrays.sort(backups, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        return Arrays.asList(backups);
    }
    
    /**
     * Restores a backup from a ZIP file.
     */
    public RestoreResult restoreBackup(File backupFile) {
        if (!backupFile.exists()) {
            return new RestoreResult(false, "Backup file does not exist");
        }
        
        plugin.getLogger().warning("Starting backup restoration from: " + backupFile.getName());
        plugin.getLogger().warning("This will overwrite existing files!");
        
        // Run restoration asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                restoreFromZip(backupFile);
                plugin.getLogger().info("Backup restoration completed successfully!");
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.broadcastMessage("§aBackup restoration completed! Server restart recommended.");
                });
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to restore backup", e);
            }
        });
        
        return new RestoreResult(true, "Backup restoration started");
    }
    
    /**
     * Restores files from a ZIP backup.
     */
    private void restoreFromZip(File backupFile) throws IOException {
        File serverFolder = plugin.getDataFolder().getParentFile().getParentFile();
        
        // Extract ZIP file to temporary location first
        File tempFolder = new File(plugin.getDataFolder(), "temp_restore_" + System.currentTimeMillis());
        tempFolder.mkdirs();
        
        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new FileInputStream(backupFile))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(tempFolder, entry.getName());
                
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
                
                zis.closeEntry();
            }
        }
        
        // Move files from temp folder to actual locations
        moveFiles(tempFolder, serverFolder);
        
        // Clean up temp folder
        deleteDirectory(tempFolder);
    }
    
    /**
     * Moves files from source to destination.
     */
    private void moveFiles(File source, File destination) throws IOException {
        File[] files = source.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            File destFile = new File(destination, file.getName());
            
            if (file.isDirectory()) {
                if (destFile.exists()) {
                    deleteDirectory(destFile);
                }
                destFile.mkdirs();
                moveFiles(file, destFile);
            } else {
                if (destFile.exists()) {
                    destFile.delete();
                }
                Files.move(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
    
    /**
     * Deletes a directory recursively.
     */
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
    
    /**
     * Schedules automatic backups.
     */
    private void scheduleBackups() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        int interval = config.getInt("backup.interval", 3600); // Default: 1 hour (in seconds)
        
        backupTaskId = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            createBackup(false);
        }, 20L * 60L * 5L, 20L * interval).getTaskId(); // Start after 5 minutes, then every interval
    }
    
    /**
     * Stops scheduled backups.
     */
    public void stopScheduledBackups() {
        if (backupTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(backupTaskId);
            backupTaskId = -1;
        }
    }
    
    /**
     * Result of a backup operation.
     */
    public static class BackupResult {
        private final boolean success;
        private final String message;
        
        public BackupResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * Result of a restore operation.
     */
    public static class RestoreResult {
        private final boolean success;
        private final String message;
        
        public RestoreResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
}

