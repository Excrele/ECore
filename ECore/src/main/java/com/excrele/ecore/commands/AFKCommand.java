package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AFKCommand implements CommandExecutor {
    private final Ecore plugin;

    public AFKCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: /afk [player]");
                return true;
            }

            Player player = (Player) sender;
            boolean isAFK = plugin.getAFKManager().isAFK(player);
            plugin.getAFKManager().setAFK(player, !isAFK, true);
            return true;
        } else {
            if (!sender.hasPermission("ecore.afk.check")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission!");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }

            boolean isAFK = plugin.getAFKManager().isAFK(target);
            sender.sendMessage(ChatColor.GREEN + target.getName() + " is " + (isAFK ? "AFK" : "not AFK") + ".");
            return true;
        }
    }
}

