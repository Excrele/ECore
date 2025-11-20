package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class KitCommand implements CommandExecutor {
    private final Ecore plugin;

    public KitCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /kit <name> or /kit list");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("list")) {
            return handleList(sender);
        } else if (subCommand.equals("create") && sender.hasPermission("ecore.kit.create")) {
            return handleCreate(sender, args);
        } else if (subCommand.equals("delete") && sender.hasPermission("ecore.kit.delete")) {
            return handleDelete(sender, args);
        } else if (subCommand.equals("give") && sender.hasPermission("ecore.kit.give")) {
            return handleGive(sender, args);
        } else {
            return handleGetKit(sender, args[0]);
        }
    }

    private boolean handleGetKit(CommandSender sender, String kitName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ecore.kit." + kitName.toLowerCase()) && !player.hasPermission("ecore.kit.*")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this kit!");
            return true;
        }

        if (plugin.getKitManager().giveKit(player, kitName)) {
            // Success message sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "Kit '" + kitName + "' does not exist!");
        }
        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!(sender instanceof Player)) {
            // Console version - text list
            List<String> kits = plugin.getKitManager().getKits();
            if (kits.isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "No kits available.");
                return true;
            }
            sender.sendMessage(ChatColor.GREEN + "Available kits: " + String.join(", ", kits));
            return true;
        }
        
        // Player version - open GUI
        Player player = (Player) sender;
        plugin.getKitGUIManager().openKitGUI(player);
        return true;
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /kit create <name>");
            return true;
        }

        String kitName = args[1];
        if (plugin.getKitManager().createKit(kitName, player)) {
            player.sendMessage(ChatColor.GREEN + "Kit '" + kitName + "' created from your inventory!");
        } else {
            player.sendMessage(ChatColor.RED + "Kit '" + kitName + "' already exists!");
        }
        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /kit delete <name>");
            return true;
        }

        String kitName = args[1];
        if (plugin.getKitManager().deleteKit(kitName)) {
            sender.sendMessage(ChatColor.GREEN + "Kit '" + kitName + "' deleted!");
        } else {
            sender.sendMessage(ChatColor.RED + "Kit '" + kitName + "' does not exist!");
        }
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /kit give <player> <kit>");
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        String kitName = args[2];
        if (plugin.getKitManager().giveKit(target, kitName)) {
            sender.sendMessage(ChatColor.GREEN + "Gave kit '" + kitName + "' to " + target.getName() + "!");
        } else {
            sender.sendMessage(ChatColor.RED + "Kit '" + kitName + "' does not exist!");
        }
        return true;
    }
}

