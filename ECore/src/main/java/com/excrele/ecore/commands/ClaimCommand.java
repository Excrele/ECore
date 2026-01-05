package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimCommand implements CommandExecutor {
    private final Ecore plugin;
    
    public ClaimCommand(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "Only players can use claim commands!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getClaimManager().isEnabled()) {
            player.sendMessage(org.bukkit.ChatColor.RED + "The claim system is disabled!");
            return true;
        }
        
        if (args.length == 0) {
            plugin.getClaimGUIManager().openClaimGUI(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        Chunk chunk = player.getLocation().getChunk();
        
        switch (subCommand) {
            case "claim":
                if (plugin.getClaimManager().claimChunk(player, chunk)) {
                    player.sendMessage(org.bukkit.ChatColor.GREEN + "Chunk claimed!");
                }
                break;
                
            case "unclaim":
                if (plugin.getClaimManager().unclaimChunk(player, chunk)) {
                    player.sendMessage(org.bukkit.ChatColor.GREEN + "Chunk unclaimed!");
                }
                break;
                
            case "visualize":
            case "viz":
                com.excrele.ecore.managers.ClaimManager.Claim claim = plugin.getClaimManager().getClaim(chunk);
                if (claim != null) {
                    plugin.getClaimManager().visualizeClaim(player, claim);
                } else {
                    player.sendMessage(org.bukkit.ChatColor.RED + "This chunk is not claimed!");
                }
                break;
                
            case "sell":
                if (args.length < 2) {
                    player.sendMessage(org.bukkit.ChatColor.RED + "Usage: /claim sell <price>");
                    return true;
                }
                try {
                    double price = Double.parseDouble(args[1]);
                    if (plugin.getClaimManager().setClaimForSale(player, chunk, price)) {
                        player.sendMessage(org.bukkit.ChatColor.GREEN + "Claim put up for sale!");
                    } else {
                        player.sendMessage(org.bukkit.ChatColor.RED + "Failed to set claim for sale!");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(org.bukkit.ChatColor.RED + "Invalid price!");
                }
                break;
                
            default:
                plugin.getClaimGUIManager().openClaimGUI(player);
                break;
        }
        
        return true;
    }
}

