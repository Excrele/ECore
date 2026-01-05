package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DimensionCommand implements CommandExecutor {
    private final Ecore plugin;
    
    public DimensionCommand(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "Only players can use dimension commands!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            plugin.getDimensionGUIManager().openDimensionGUI(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                if (plugin.getDimensionManager().createPlayerDimension(player) != null) {
                    player.sendMessage(org.bukkit.ChatColor.GREEN + "Dimension created!");
                } else {
                    player.sendMessage(org.bukkit.ChatColor.RED + "Failed to create dimension!");
                }
                break;
                
            case "tp":
            case "teleport":
                com.excrele.ecore.managers.DimensionManager.Dimension dim = 
                    plugin.getDimensionManager().getPlayerDimension(player.getUniqueId());
                if (dim != null) {
                    plugin.getDimensionManager().teleportToDimension(player, dim.getId());
                } else {
                    player.sendMessage(org.bukkit.ChatColor.RED + "You don't have a dimension!");
                }
                break;
                
            default:
                plugin.getDimensionGUIManager().openDimensionGUI(player);
                break;
        }
        
        return true;
    }
}

