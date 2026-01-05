package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillCommand implements CommandExecutor {
    private final Ecore plugin;
    
    public SkillCommand(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "Only players can use skill commands!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            plugin.getSkillGUIManager().openSkillsGUI(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "top":
            case "leaderboard":
                if (args.length < 2) {
                    player.sendMessage(org.bukkit.ChatColor.RED + "Usage: /skill top <skill-id>");
                    return true;
                }
                player.sendMessage(org.bukkit.ChatColor.YELLOW + "Leaderboard coming soon!");
                break;
                
            default:
                plugin.getSkillGUIManager().openSkillsGUI(player);
                break;
        }
        
        return true;
    }
}

