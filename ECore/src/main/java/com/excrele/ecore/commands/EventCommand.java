package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventCommand implements CommandExecutor {
    private final Ecore plugin;
    
    public EventCommand(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                plugin.getEventGUIManager().openEventGUI((Player) sender);
            } else {
                sender.sendMessage(org.bukkit.ChatColor.RED + "Usage: /event <create|start|end|join|list> [args]");
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                if (!sender.hasPermission("ecore.event.create")) {
                    sender.sendMessage(org.bukkit.ChatColor.RED + "You don't have permission!");
                    return true;
                }
                if (args.length < 5) {
                    sender.sendMessage(org.bukkit.ChatColor.RED + "Usage: /event create <id> <name> <type> <description>");
                    return true;
                }
                String eventId = args[1];
                String eventName = args[2];
                String eventType = args[3];
                String description = String.join(" ", java.util.Arrays.copyOfRange(args, 4, args.length));
                plugin.getEventManager().createEvent(eventId, eventName, eventType, description, 
                    System.currentTimeMillis(), 3600000L);
                sender.sendMessage(org.bukkit.ChatColor.GREEN + "Event created!");
                break;
                
            case "join":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(org.bukkit.ChatColor.RED + "Only players can join events!");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(org.bukkit.ChatColor.RED + "Usage: /event join <event-id>");
                    return true;
                }
                if (plugin.getEventManager().joinEvent((Player) sender, args[1])) {
                    sender.sendMessage(org.bukkit.ChatColor.GREEN + "Joined event!");
                }
                break;
                
            case "list":
                sender.sendMessage(org.bukkit.ChatColor.GOLD + "Active Events:");
                for (com.excrele.ecore.managers.EventManager.Event event : plugin.getEventManager().getActiveEvents()) {
                    sender.sendMessage(org.bukkit.ChatColor.YELLOW + "- " + event.getName() + " (" + event.getType() + ")");
                }
                break;
                
            default:
                if (sender instanceof Player) {
                    plugin.getEventGUIManager().openEventGUI((Player) sender);
                }
                break;
        }
        
        return true;
    }
}

