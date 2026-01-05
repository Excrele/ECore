package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.MarriageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class MarriageCommand implements CommandExecutor {
    private final Ecore plugin;
    
    public MarriageCommand(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "Only players can use marriage commands!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            plugin.getMarriageGUIManager().openMarriageGUI(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "propose":
                if (args.length < 2) {
                    player.sendMessage(org.bukkit.ChatColor.RED + "Usage: /marry propose <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(org.bukkit.ChatColor.RED + "Player not found!");
                    return true;
                }
                plugin.getMarriageManager().propose(player, target);
                break;
                
            case "accept":
                if (plugin.getMarriageManager().acceptProposal(player)) {
                    player.sendMessage(org.bukkit.ChatColor.GREEN + "Marriage proposal accepted!");
                }
                break;
                
            case "deny":
                plugin.getMarriageManager().denyProposal(player);
                break;
                
            case "divorce":
                if (plugin.getMarriageManager().divorce(player)) {
                    player.sendMessage(org.bukkit.ChatColor.RED + "You have divorced.");
                }
                break;
                
            case "tp":
            case "teleport":
                plugin.getMarriageManager().teleportToSpouse(player);
                break;
                
            case "chat":
            case "c":
                if (args.length < 2) {
                    player.sendMessage(org.bukkit.ChatColor.RED + "Usage: /marry chat <message>");
                    return true;
                }
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                plugin.getMarriageManager().sendMarriageChat(player, message);
                break;
                
            case "stats":
            case "info":
                MarriageManager.MarriageStats stats = plugin.getMarriageManager().getMarriageStats(player.getUniqueId());
                if (stats == null) {
                    player.sendMessage(org.bukkit.ChatColor.RED + "You are not married!");
                } else {
                    player.sendMessage(org.bukkit.ChatColor.GOLD + "Marriage Statistics:");
                    player.sendMessage(org.bukkit.ChatColor.GRAY + "Days Married: " + stats.getDaysMarried());
                    player.sendMessage(org.bukkit.ChatColor.GRAY + "Married: " + new java.util.Date(stats.getMarriedAt()).toString());
                }
                break;
                
            default:
                plugin.getMarriageGUIManager().openMarriageGUI(player);
                break;
        }
        
        return true;
    }
}

