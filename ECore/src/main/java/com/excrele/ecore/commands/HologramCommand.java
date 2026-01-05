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
 * Hologram System Command Handler
 */
public class HologramCommand implements CommandExecutor {
    private final Ecore plugin;
    
    public HologramCommand(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecore.hologram.use")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /hologram <create|delete|list> [args]");
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
                    sender.sendMessage(ChatColor.RED + "Usage: /hologram create <id> <line1> [line2] [line3]...");
                    return true;
                }
                String id = args[1];
                List<String> lines = Arrays.asList(Arrays.copyOfRange(args, 2, args.length));
                Player player = (Player) sender;
                plugin.getHologramManager().createHologram(
                    id,
                    player.getLocation(),
                    lines,
                    false, // dynamic
                    false, // interactive
                    null,  // permission
                    null   // command
                );
                sender.sendMessage(ChatColor.GREEN + "Hologram created!");
                break;
                
            case "delete":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /hologram delete <id>");
                    return true;
                }
                plugin.getHologramManager().deleteHologram(args[1]);
                sender.sendMessage(ChatColor.GREEN + "Hologram deleted!");
                break;
                
            case "list":
                sender.sendMessage(ChatColor.YELLOW + "Holograms:");
                for (com.excrele.ecore.managers.HologramManager.Hologram hologram : plugin.getHologramManager().getHolograms()) {
                    sender.sendMessage(ChatColor.GRAY + "- " + hologram.getId() + " at " + 
                        hologram.getLocation().getWorld().getName() + " " +
                        hologram.getLocation().getBlockX() + "," +
                        hologram.getLocation().getBlockY() + "," +
                        hologram.getLocation().getBlockZ());
                }
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                break;
        }
        
        return true;
    }
}

