package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * NPC System Command Handler
 */
public class NPCCommand implements CommandExecutor {
    private final Ecore plugin;
    
    public NPCCommand(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecore.npc.use")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /npc <create|delete|list> [args]");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /npc create <id> <name> [dialogue...]");
                    return true;
                }
                String id = args[1];
                String name = args[2];
                List<String> dialogue = args.length > 3 ? 
                    Arrays.asList(Arrays.copyOfRange(args, 3, args.length)) : 
                    Arrays.asList("Hello, " + name + "!");
                Player player = (Player) sender;
                plugin.getNPCManager().createNPC(
                    id,
                    player.getLocation(),
                    name,
                    "villager",
                    dialogue,
                    null, // questId
                    null, // shopId
                    null, // command
                    null  // permission
                );
                sender.sendMessage(ChatColor.GREEN + "NPC created!");
                break;
                
            case "delete":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /npc delete <id>");
                    return true;
                }
                plugin.getNPCManager().deleteNPC(args[1]);
                sender.sendMessage(ChatColor.GREEN + "NPC deleted!");
                break;
                
            case "list":
                sender.sendMessage(ChatColor.YELLOW + "NPCs:");
                for (com.excrele.ecore.managers.NPCManager.NPC npc : plugin.getNPCManager().getNPCs()) {
                    sender.sendMessage(ChatColor.GRAY + "- " + npc.getId() + " (" + npc.getName() + ") at " + 
                        npc.getLocation().getWorld().getName() + " " +
                        npc.getLocation().getBlockX() + "," +
                        npc.getLocation().getBlockY() + "," +
                        npc.getLocation().getBlockZ());
                }
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                break;
        }
        
        return true;
    }
}

