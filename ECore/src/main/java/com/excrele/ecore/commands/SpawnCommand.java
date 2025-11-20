package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    private final Ecore plugin;

    public SpawnCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("spawn")) {
            return handleSpawn(sender, args);
        } else if (cmd.equals("setspawn")) {
            return handleSetSpawn(sender);
        }

        return false;
    }

    private boolean handleSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            Location spawn = plugin.getSpawnManager().getSpawn(player.getWorld());
            if (spawn == null) {
                player.sendMessage(ChatColor.RED + "No spawn point set for this world!");
                return true;
            }

            plugin.getTeleportManager().teleport(player, spawn);
            player.sendMessage(ChatColor.GREEN + "Teleported to spawn!");
            return true;
        } else if (args.length == 1) {
            if (!player.hasPermission("ecore.spawn.others")) {
                player.sendMessage(ChatColor.RED + "You don't have permission!");
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }

            Location spawn = plugin.getSpawnManager().getSpawn(target.getWorld());
            if (spawn == null) {
                player.sendMessage(ChatColor.RED + "No spawn point set for this world!");
                return true;
            }

            plugin.getTeleportManager().teleport(target, spawn);
            target.sendMessage(ChatColor.GREEN + "You were teleported to spawn by " + player.getName() + "!");
            player.sendMessage(ChatColor.GREEN + "Teleported " + target.getName() + " to spawn!");
            return true;
        }

        player.sendMessage(ChatColor.RED + "Usage: /spawn [player]");
        return true;
    }

    private boolean handleSetSpawn(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ecore.spawn.set")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        plugin.getSpawnManager().setSpawn(player.getLocation(), player.getWorld().getName());
        player.sendMessage(ChatColor.GREEN + "Spawn point set for " + player.getWorld().getName() + "!");
        return true;
    }
}

