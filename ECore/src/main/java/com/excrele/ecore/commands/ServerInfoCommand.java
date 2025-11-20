package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ServerInfoCommand implements CommandExecutor {
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

        // Display server information
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
        
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        
        return true;
    }
}

