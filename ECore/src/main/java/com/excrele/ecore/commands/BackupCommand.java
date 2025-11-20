package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.BackupManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Handles backup management commands.
 */
public class BackupCommand implements CommandExecutor {
    private final Ecore plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public BackupCommand(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
            case "backup":
                return handleCreate(sender);
            case "list":
                return handleList(sender);
            case "restore":
                return handleRestore(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Backup Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/backup create - Create a new backup");
        sender.sendMessage(ChatColor.YELLOW + "/backup list - List all backups");
        sender.sendMessage(ChatColor.YELLOW + "/backup restore <number> - Restore a backup");
        sender.sendMessage(ChatColor.YELLOW + "/backup reload - Reload backup configuration");
    }
    
    private boolean handleCreate(CommandSender sender) {
        if (!sender.hasPermission("ecore.backup.create")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to create backups!");
            return true;
        }
        
        BackupManager.BackupResult result = plugin.getBackupManager().createBackup(true);
        
        if (result.isSuccess()) {
            sender.sendMessage(ChatColor.GREEN + result.getMessage());
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to create backup: " + result.getMessage());
        }
        
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("ecore.backup.list")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to list backups!");
            return true;
        }
        
        List<File> backups = plugin.getBackupManager().listBackups();
        
        if (backups.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No backups found.");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== Available Backups ===");
        for (int i = 0; i < backups.size(); i++) {
            File backup = backups.get(i);
            String size = formatFileSize(backup.length());
            String date = dateFormat.format(new java.util.Date(backup.lastModified()));
            sender.sendMessage(ChatColor.YELLOW + String.valueOf(i + 1) + ". " + ChatColor.WHITE + backup.getName() + 
                ChatColor.GRAY + " (" + size + ", " + date + ")");
        }
        
        return true;
    }
    
    private boolean handleRestore(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.backup.restore")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to restore backups!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /backup restore <number>");
            return true;
        }
        
        try {
            int index = Integer.parseInt(args[1]) - 1;
            List<File> backups = plugin.getBackupManager().listBackups();
            
            if (index < 0 || index >= backups.size()) {
                sender.sendMessage(ChatColor.RED + "Invalid backup number!");
                return true;
            }
            
            File backup = backups.get(index);
            BackupManager.RestoreResult result = plugin.getBackupManager().restoreBackup(backup);
            
            if (result.isSuccess()) {
                sender.sendMessage(ChatColor.GREEN + result.getMessage());
                sender.sendMessage(ChatColor.YELLOW + "Restoring backup: " + backup.getName());
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to restore backup: " + result.getMessage());
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid backup number!");
        }
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("ecore.backup.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to reload backup configuration!");
            return true;
        }
        
        plugin.getConfigManager().reloadConfig();
        plugin.getBackupManager().stopScheduledBackups();
        if (plugin.getBackupManager().isEnabled()) {
            // Re-schedule backups with new config
            plugin.getLogger().info("Backup configuration reloaded");
        }
        
        sender.sendMessage(ChatColor.GREEN + "Backup configuration reloaded!");
        return true;
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}

