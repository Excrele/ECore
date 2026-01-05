package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MarketplaceCommand implements CommandExecutor {
    private final Ecore plugin;
    
    public MarketplaceCommand(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "Only players can use marketplace commands!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            plugin.getMarketplaceGUIManager().openMarketplaceGUI(player, 1);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                if (args.length < 5) {
                    player.sendMessage(org.bukkit.ChatColor.RED + "Usage: /marketplace create <type> <category> <title> <price> [description]");
                    return true;
                }
                String type = args[1];
                String category = args[2];
                String title = args[3];
                try {
                    double price = Double.parseDouble(args[4]);
                    String description = args.length > 5 ? String.join(" ", java.util.Arrays.copyOfRange(args, 5, args.length)) : "";
                    int listingId = plugin.getMarketplaceManager().createListing(player, type, category, title, description, price);
                    player.sendMessage(org.bukkit.ChatColor.GREEN + "Listing created! ID: " + listingId);
                } catch (NumberFormatException e) {
                    player.sendMessage(org.bukkit.ChatColor.RED + "Invalid price!");
                }
                break;
                
            default:
                plugin.getMarketplaceGUIManager().openMarketplaceGUI(player, 1);
                break;
        }
        
        return true;
    }
}

