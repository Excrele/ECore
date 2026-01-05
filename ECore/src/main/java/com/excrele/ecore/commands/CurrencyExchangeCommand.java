package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CurrencyExchangeCommand implements CommandExecutor {
    private final Ecore plugin;
    
    public CurrencyExchangeCommand(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "Only players can use currency exchange commands!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 3) {
            player.sendMessage(org.bukkit.ChatColor.RED + "Usage: /currency exchange <from> <to> <amount>");
            return true;
        }
        
        String fromCurrency = args[0];
        String toCurrency = args[1];
        try {
            double amount = Double.parseDouble(args[2]);
            if (plugin.getCurrencyExchangeManager().exchangeCurrency(player, fromCurrency, toCurrency, amount)) {
                player.sendMessage(org.bukkit.ChatColor.GREEN + "Currency exchanged!");
            } else {
                player.sendMessage(org.bukkit.ChatColor.RED + "Failed to exchange currency!");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(org.bukkit.ChatColor.RED + "Invalid amount!");
        }
        
        return true;
    }
}

