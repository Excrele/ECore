package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles chunk pregeneration commands.
 * 
 * @author Excrele
 * @version 1.0
 */
public class ChunksCommand implements CommandExecutor {
    private final Ecore plugin;
    
    public ChunksCommand(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecore.chunks.generate")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "generate":
            case "gen":
                return handleGenerate(sender, args);
            case "cancel":
                return handleCancel(sender);
            case "status":
                return handleStatus(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleGenerate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /chunks generate <radius>");
            player.sendMessage(ChatColor.GRAY + "Example: /chunks generate 50");
            return true;
        }
        
        int radius;
        try {
            radius = Integer.parseInt(args[1]);
            if (radius < 1) {
                player.sendMessage(ChatColor.RED + "Radius must be at least 1!");
                return true;
            }
            if (radius > 1000) {
                player.sendMessage(ChatColor.RED + "Radius cannot exceed 1000 chunks!");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid radius! Must be a number.");
            return true;
        }
        
        World world = player.getWorld();
        
        // Check if already generating
        if (plugin.getChunkManager().isGenerating(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You already have a chunk generation in progress!");
            player.sendMessage(ChatColor.YELLOW + "Use /chunks cancel to cancel it first.");
            return true;
        }
        
        // Calculate estimated chunks
        double estimatedChunks = Math.PI * radius * radius;
        player.sendMessage(ChatColor.GREEN + "Starting chunk generation...");
        player.sendMessage(ChatColor.YELLOW + "World: " + world.getName());
        player.sendMessage(ChatColor.YELLOW + "Radius: " + radius + " chunks");
        player.sendMessage(ChatColor.YELLOW + "Estimated chunks: ~" + (int) estimatedChunks);
        player.sendMessage(ChatColor.GRAY + "This may take a while. Progress updates will be sent periodically.");
        
        // Start generation
        if (plugin.getChunkManager().generateChunks(world, radius, player.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "Chunk generation started!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to start chunk generation!");
        }
        
        return true;
    }
    
    private boolean handleCancel(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (plugin.getChunkManager().cancelGeneration(player.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "Chunk generation cancelled!");
        } else {
            player.sendMessage(ChatColor.RED + "You don't have any active chunk generation!");
        }
        
        return true;
    }
    
    private boolean handleStatus(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (plugin.getChunkManager().isGenerating(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "You have an active chunk generation in progress.");
            player.sendMessage(ChatColor.GRAY + "Use /chunks cancel to cancel it.");
        } else {
            player.sendMessage(ChatColor.GREEN + "No active chunk generation.");
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Chunk Generation Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/chunks generate <radius> - Generate chunks in radius from spawn");
        sender.sendMessage(ChatColor.YELLOW + "/chunks cancel - Cancel your active chunk generation");
        sender.sendMessage(ChatColor.YELLOW + "/chunks status - Check if you have active chunk generation");
        sender.sendMessage(ChatColor.GRAY + "Example: /chunks generate 50");
    }
}

