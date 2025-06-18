package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HomeCommand implements CommandExecutor {
    private final Ecore plugin;

    public HomeCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("sethome")) {
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Usage: /sethome <name>");
                return true;
            }

            String homeName = args[0];
            boolean success = plugin.getHomeManager().setHome(player, homeName, player.getLocation());
            if (success) {
                player.sendMessage(ChatColor.GREEN + "Home '" + homeName + "' set successfully!");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to set home. You may have reached the maximum number of homes!");
            }
            return true;
        } else if (cmd.equals("home")) {
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Usage: /home <name>");
                return true;
            }

            String homeName = args[0];
            Location home = plugin.getHomeManager().getHome(player, homeName);
            if (home == null) {
                player.sendMessage(ChatColor.RED + "Home '" + homeName + "' does not exist or the world is not loaded!");
                return true;
            }

            player.teleport(home);
            player.sendMessage(ChatColor.GREEN + "Teleported to home '" + homeName + "'!");
            plugin.getDiscordManager().sendStaffLogNotification(
                    "home-log",
                    player.getName(),
                    "teleported to home",
                    homeName,
                    home.toString()
            );
            return true;
        } else if (cmd.equals("listhomes")) {
            List<String> homes = plugin.getHomeManager().getPlayerHomes(player);
            if (homes.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "You have no homes set.");
            } else {
                player.sendMessage(ChatColor.GREEN + "Your homes: " + String.join(", ", homes));
            }
            return true;
        }

        return false;
    }
}