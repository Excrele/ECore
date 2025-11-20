package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for title and action bar system.
 */
public class TitleCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public TitleCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "title":
                return handleTitle(sender, args);
            case "titleall":
                return handleTitleAll(sender, args);
            case "actionbar":
                return handleActionBar(sender, args);
            case "actionbarall":
                return handleActionBarAll(sender, args);
            case "cleartitle":
                return handleClearTitle(sender, args);
            default:
                return false;
        }
    }

    private boolean handleTitle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.title")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /title <player> <title> [subtitle] [fadeIn] [stay] [fadeOut]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }

        String title = args[1];
        String subtitle = args.length > 2 ? args[2] : null;
        
        int fadeIn = 10;
        int stay = 70;
        int fadeOut = 20;

        if (args.length > 3) {
            try {
                fadeIn = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid fadeIn value: " + args[3]);
                return true;
            }
        }

        if (args.length > 4) {
            try {
                stay = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid stay value: " + args[4]);
                return true;
            }
        }

        if (args.length > 5) {
            try {
                fadeOut = Integer.parseInt(args[5]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid fadeOut value: " + args[5]);
                return true;
            }
        }

        plugin.getTitleManager().sendTitle(target, title, subtitle, fadeIn, stay, fadeOut);
        sender.sendMessage(ChatColor.GREEN + "Title sent to " + target.getName() + "!");
        
        return true;
    }

    private boolean handleTitleAll(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.title.all")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /titleall <title> [subtitle] [fadeIn] [stay] [fadeOut]");
            return true;
        }

        String title = args[0];
        String subtitle = args.length > 1 ? args[1] : null;
        
        int fadeIn = 10;
        int stay = 70;
        int fadeOut = 20;

        if (args.length > 2) {
            try {
                fadeIn = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid fadeIn value: " + args[2]);
                return true;
            }
        }

        if (args.length > 3) {
            try {
                stay = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid stay value: " + args[3]);
                return true;
            }
        }

        if (args.length > 4) {
            try {
                fadeOut = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid fadeOut value: " + args[4]);
                return true;
            }
        }

        plugin.getTitleManager().broadcastTitle(title, subtitle, fadeIn, stay, fadeOut);
        sender.sendMessage(ChatColor.GREEN + "Title broadcasted to all players!");
        
        return true;
    }

    private boolean handleActionBar(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.actionbar")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /actionbar <player> <message>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }

        plugin.getTitleManager().sendActionBar(target, messageBuilder.toString());
        sender.sendMessage(ChatColor.GREEN + "Action bar message sent to " + target.getName() + "!");
        
        return true;
    }

    private boolean handleActionBarAll(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.actionbar.all")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /actionbarall <message>");
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }

        plugin.getTitleManager().broadcastActionBar(messageBuilder.toString());
        sender.sendMessage(ChatColor.GREEN + "Action bar message broadcasted to all players!");
        
        return true;
    }

    private boolean handleClearTitle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.title.clear")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            // Clear for sender if they're a player
            if (sender instanceof Player) {
                plugin.getTitleManager().clearTitle((Player) sender);
                sender.sendMessage(ChatColor.GREEN + "Your title has been cleared!");
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /cleartitle <player> or use as a player");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("all")) {
            if (!sender.hasPermission("ecore.title.clear.all")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to clear all titles!");
                return true;
            }
            plugin.getTitleManager().clearAllTitles();
            sender.sendMessage(ChatColor.GREEN + "All titles cleared!");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }

        plugin.getTitleManager().clearTitle(target);
        sender.sendMessage(ChatColor.GREEN + "Title cleared for " + target.getName() + "!");
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("title") && args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (cmd.equals("actionbar") && args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (cmd.equals("cleartitle") && args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("all");
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
            return completions;
        }

        return new ArrayList<>();
    }
}

