package com.excrele.ecore.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.PerformanceManager;

public class ServerInfoCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public ServerInfoCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecore.serverinfo")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        // Handle subcommands
        if (args.length > 0) {
            String subCommand = args[0].toLowerCase();
            
            return switch (subCommand) {
                case "clear", "cleanup" -> {
                    handleCleanup(sender);
                    yield true;
                }
                case "lag", "stats" -> {
                    handleStats(sender);
                    yield true;
                }
                case "merge" -> {
                    handleMerge(sender, args);
                    yield true;
                }
                case "chunks" -> {
                    handleChunks(sender);
                    yield true;
                }
                case "help" -> {
                    sendHelp(sender);
                    yield true;
                }
                default -> false;
            };
        }

        // Display server information
        displayServerInfo(sender);
        return true;
    }

    private void displayServerInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        sender.sendMessage(ChatColor.YELLOW + "          Server Information");
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        
        // Basic Info
        sender.sendMessage(ChatColor.GRAY + "Version: " + ChatColor.WHITE + plugin.getServerInfoManager().getServerVersion());
        sender.sendMessage(ChatColor.GRAY + "Bukkit: " + ChatColor.WHITE + plugin.getServerInfoManager().getBukkitVersion());
        sender.sendMessage(ChatColor.GRAY + "Java: " + ChatColor.WHITE + plugin.getServerInfoManager().getJavaVersion());
        sender.sendMessage(ChatColor.GRAY + "OS: " + ChatColor.WHITE + plugin.getServerInfoManager().getOSInfo());
        
        sender.sendMessage("");
        
        // Performance
        sender.sendMessage(ChatColor.GRAY + "TPS: " + plugin.getServerInfoManager().getTPSFormatted());
        sender.sendMessage(ChatColor.GRAY + "Memory: " + plugin.getServerInfoManager().getMemoryFormatted());
        sender.sendMessage(ChatColor.GRAY + "Uptime: " + ChatColor.WHITE + plugin.getServerInfoManager().getUptime());
        
        sender.sendMessage("");
        
        // Players
        int online = plugin.getServerInfoManager().getOnlinePlayers();
        int max = plugin.getServerInfoManager().getMaxPlayers();
        String playerColor = online >= max * 0.9 ? ChatColor.RED.toString() : 
                            online >= max * 0.7 ? ChatColor.YELLOW.toString() : ChatColor.GREEN.toString();
        sender.sendMessage(ChatColor.GRAY + "Players: " + playerColor + online + ChatColor.GRAY + " / " + ChatColor.WHITE + max);
        
        sender.sendMessage("");
        
        // World Info
        sender.sendMessage(ChatColor.GRAY + "Worlds: " + ChatColor.WHITE + String.join(", ", plugin.getServerInfoManager().getWorldNames()));
        sender.sendMessage(ChatColor.GRAY + "Loaded Chunks: " + ChatColor.WHITE + plugin.getServerInfoManager().getTotalChunks());
        sender.sendMessage(ChatColor.GRAY + "Total Entities: " + ChatColor.WHITE + plugin.getServerInfoManager().getTotalEntities());
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.WHITE + "/serverinfo help" + ChatColor.YELLOW + " for performance commands");
        
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
    }

    private void handleCleanup(CommandSender sender) {
        if (!sender.hasPermission("ecore.serverinfo.cleanup")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to perform cleanup!");
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + "Performing cleanup...");
        PerformanceManager.CleanupResult result = plugin.getPerformanceManager().performCleanup();
        
        sender.sendMessage(ChatColor.GREEN + "Cleanup complete!");
        sender.sendMessage(ChatColor.GRAY + "Items removed: " + ChatColor.WHITE + result.itemsRemoved);
        sender.sendMessage(ChatColor.GRAY + "Mobs removed: " + ChatColor.WHITE + result.mobsRemoved);
        sender.sendMessage(ChatColor.GRAY + "Projectiles removed: " + ChatColor.WHITE + result.projectilesRemoved);
        sender.sendMessage(ChatColor.GRAY + "Total removed: " + ChatColor.WHITE + result.getTotalRemoved());
    }

    private void handleStats(CommandSender sender) {
        if (!sender.hasPermission("ecore.serverinfo.stats")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to view stats!");
            return;
        }

        PerformanceManager.PerformanceStats stats = plugin.getPerformanceManager().getPerformanceStats();
        
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        sender.sendMessage(ChatColor.YELLOW + "       Performance Statistics");
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        
        sender.sendMessage(ChatColor.GRAY + "TPS: " + plugin.getServerInfoManager().getTPSFormatted());
        
        if (stats.memoryInfo != null) {
            double usedMB = stats.memoryInfo.getUsedMemory() / (1024.0 * 1024.0);
            double maxMB = stats.memoryInfo.getMaxMemory() / (1024.0 * 1024.0);
            double percent = (stats.memoryInfo.getUsedMemory() * 100.0) / stats.memoryInfo.getMaxMemory();
            
            String color = percent < 50 ? ChatColor.GREEN.toString() : 
                          percent < 75 ? ChatColor.YELLOW.toString() : 
                          percent < 90 ? ChatColor.GOLD.toString() : ChatColor.RED.toString();
            
            sender.sendMessage(ChatColor.GRAY + "Memory: " + color + String.format("%.2f", usedMB) + "MB / " + 
                             String.format("%.2f", maxMB) + "MB (" + String.format("%.1f", percent) + "%)");
        }
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "Entities:");
        sender.sendMessage(ChatColor.GRAY + "  Total: " + ChatColor.WHITE + stats.totalEntities);
        sender.sendMessage(ChatColor.GRAY + "  Items: " + ChatColor.WHITE + stats.items);
        sender.sendMessage(ChatColor.GRAY + "  Mobs: " + ChatColor.WHITE + stats.mobs);
        sender.sendMessage(ChatColor.GRAY + "  Other: " + ChatColor.WHITE + stats.other);
        sender.sendMessage(ChatColor.GRAY + "  Players: " + ChatColor.WHITE + stats.onlinePlayers + " / " + stats.maxPlayers);
        sender.sendMessage(ChatColor.GRAY + "Loaded Chunks: " + ChatColor.WHITE + stats.totalChunks);
        
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
    }

    private void handleMerge(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.serverinfo.merge")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to merge items!");
            return;
        }

        double radius = 5.0;
        if (args.length > 1) {
            try {
                radius = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid radius! Using default: 5.0");
            }
        }

        sender.sendMessage(ChatColor.YELLOW + "Merging items within " + radius + " blocks...");
        int merged = plugin.getPerformanceManager().mergeItems(radius);
        sender.sendMessage(ChatColor.GREEN + "Merged " + merged + " item stacks!");
    }

    private void handleChunks(CommandSender sender) {
        if (!sender.hasPermission("ecore.serverinfo.chunks")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to optimize chunks!");
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + "Optimizing chunks...");
        int unloaded = plugin.getPerformanceManager().optimizeChunks();
        sender.sendMessage(ChatColor.GREEN + "Unloaded " + unloaded + " unused chunks!");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        sender.sendMessage(ChatColor.YELLOW + "     Server Info Commands");
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        sender.sendMessage(ChatColor.YELLOW + "/serverinfo" + ChatColor.GRAY + " - Show server information");
        sender.sendMessage(ChatColor.YELLOW + "/serverinfo clear" + ChatColor.GRAY + " - Clean up entities");
        sender.sendMessage(ChatColor.YELLOW + "/serverinfo stats" + ChatColor.GRAY + " - Show performance stats");
        sender.sendMessage(ChatColor.YELLOW + "/serverinfo merge [radius]" + ChatColor.GRAY + " - Merge nearby items");
        sender.sendMessage(ChatColor.YELLOW + "/serverinfo chunks" + ChatColor.GRAY + " - Optimize chunks");
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("clear", "stats", "merge", "chunks", "help")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}

