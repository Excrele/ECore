package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EcoreCommand implements CommandExecutor {
    private final Ecore plugin;

    public EcoreCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /ecore [reload|staff|home]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (player.hasPermission("ecore.staff")) {
                    plugin.getConfigManager().reloadConfig();
                    player.sendMessage("Configuration reloaded!");
                } else {
                    player.sendMessage("You don't have permission!");
                }
                break;
            case "staff":
                if (player.hasPermission("ecore.staff")) {
                    plugin.getStaffManager().openStaffGUI(player);
                } else {
                    player.sendMessage("You don't have permission!");
                }
                break;
            case "home":
                if (player.hasPermission("ecore.home")) {
                    plugin.getHomeManager().getPlayerHomes(player);
                    // Open home GUI (to be implemented)
                    player.sendMessage("Home GUI opened!");
                } else {
                    player.sendMessage("You don't have permission!");
                }
                break;
            default:
                player.sendMessage("Unknown subcommand!");
        }
        return true;
    }
}