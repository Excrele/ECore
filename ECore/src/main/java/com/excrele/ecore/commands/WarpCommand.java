package com.excrele.ecore.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.excrele.ecore.Ecore;

public class WarpCommand implements CommandExecutor {
    private final Ecore plugin;

    public WarpCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("warp")) {
            return handleWarp(sender, args);
        } else if (cmd.equals("setwarp")) {
            return handleSetWarp(sender, args);
        } else if (cmd.equals("delwarp") || cmd.equals("deletewarp")) {
            return handleDelWarp(sender, args);
        } else if (cmd.equals("warps")) {
            return handleListWarps(sender);
        }

        return false;
    }

    private boolean handleWarp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /warp <name>");
            return true;
        }

        String warpName = args[0];
        plugin.getWarpManager().teleportToWarp(player, warpName);
        return true;
    }

    private boolean handleSetWarp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ecore.warp.set")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /setwarp <name>");
            return true;
        }

        String warpName = args[0];
        if (plugin.getWarpManager().warpExists(warpName)) {
            player.sendMessage(ChatColor.RED + "Warp '" + warpName + "' already exists!");
            return true;
        }

        if (plugin.getWarpManager().createWarp(warpName, player.getLocation(), player)) {
            player.sendMessage(ChatColor.GREEN + "Warp '" + warpName + "' created!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to create warp!");
        }
        return true;
    }

    private boolean handleDelWarp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.warp.delete")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /delwarp <name>");
            return true;
        }

        String warpName = args[0];
        if (plugin.getWarpManager().deleteWarp(warpName)) {
            sender.sendMessage(ChatColor.GREEN + "Warp '" + warpName + "' deleted!");
        } else {
            sender.sendMessage(ChatColor.RED + "Warp '" + warpName + "' does not exist!");
        }
        return true;
    }

    private boolean handleListWarps(CommandSender sender) {
        if (!(sender instanceof Player)) {
            // Console version - text list
            List<String> warps = plugin.getWarpManager().getPublicWarps();
            if (warps.isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "No warps available.");
                return true;
            }
            sender.sendMessage(ChatColor.GREEN + "Available warps: " + String.join(", ", warps));
            return true;
        }
        
        // Player version - open GUI
        Player player = (Player) sender;
        plugin.getWarpGUIManager().openWarpGUI(player);
        return true;
    }
}

