package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JailCommand implements CommandExecutor {
    private final Ecore plugin;

    public JailCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("jail")) {
            return handleJail(sender, args);
        } else if (cmd.equals("unjail")) {
            return handleUnjail(sender, args);
        } else if (cmd.equals("setjail")) {
            return handleSetJail(sender, args);
        } else if (cmd.equals("jailinfo")) {
            return handleJailInfo(sender, args);
        }

        return false;
    }

    private boolean handleJail(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.jail")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /jail <player> <jail> [time] [reason]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        String jailName = args[1];
        long duration = 0;
        String reason = "";

        if (args.length >= 3) {
            try {
                duration = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                reason = args[2];
            }
        }

        if (args.length >= 4 && duration > 0) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        } else if (args.length >= 3 && duration == 0) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        }

        Player staff = sender instanceof Player ? (Player) sender : null;
        plugin.getJailManager().jailPlayer(target, jailName, duration, reason, staff);
        return true;
    }

    private boolean handleUnjail(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.jail")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unjail <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (!plugin.getJailManager().isJailed(target)) {
            sender.sendMessage(ChatColor.RED + target.getName() + " is not jailed!");
            return true;
        }

        plugin.getJailManager().unjailPlayer(target);
        sender.sendMessage(ChatColor.GREEN + "Unjailed " + target.getName() + "!");
        return true;
    }

    private boolean handleSetJail(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ecore.jail.set")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /setjail <name>");
            return true;
        }

        String jailName = args[0];
        if (plugin.getJailManager().createJail(jailName, player.getLocation())) {
            player.sendMessage(ChatColor.GREEN + "Jail '" + jailName + "' created!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to create jail!");
        }
        return true;
    }

    private boolean handleJailInfo(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /jailinfo <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (!plugin.getJailManager().isJailed(target)) {
            sender.sendMessage(ChatColor.RED + target.getName() + " is not jailed!");
            return true;
        }

        String jailName = plugin.getJailManager().getJailInfo(target);
        long timeRemaining = plugin.getJailManager().getJailTimeRemaining(target);
        
        sender.sendMessage(ChatColor.GREEN + target.getName() + " is jailed in: " + jailName);
        if (timeRemaining > 0) {
            sender.sendMessage(ChatColor.YELLOW + "Time remaining: " + formatTime(timeRemaining) + " seconds");
        } else if (timeRemaining == -1) {
            sender.sendMessage(ChatColor.YELLOW + "Duration: Permanent");
        }
        return true;
    }

    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        } else {
            return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
        }
    }
}

