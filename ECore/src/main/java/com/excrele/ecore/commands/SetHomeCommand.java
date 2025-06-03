package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class SetHomeCommand implements CommandExecutor {
    private final Ecore plugin;

    public SetHomeCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("ecore.home")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        // Prompt for home name via chat
        plugin.registerPendingAction(player, "sethome:new");
        player.sendMessage(ChatColor.YELLOW + "Please type the name for the new home in chat.");
        return true;
    }
}