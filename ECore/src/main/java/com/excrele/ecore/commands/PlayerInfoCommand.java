package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlayerInfoCommand implements CommandExecutor {
    private final Ecore plugin;

    public PlayerInfoCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("whois")) {
            return handleWhois(sender, args);
        } else if (cmd.equals("seen")) {
            return handleSeen(sender, args);
        } else if (cmd.equals("list") || cmd.equals("who")) {
            return handleList(sender);
        } else if (cmd.equals("ping")) {
            return handlePing(sender, args);
        } else if (cmd.equals("near")) {
            return handleNear(sender, args);
        }

        return false;
    }

    private boolean handleWhois(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /whois <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        String info = plugin.getPlayerInfoManager().getPlayerInfo(target);
        sender.sendMessage(info);
        return true;
    }

    private boolean handleSeen(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /seen <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (target.isOnline()) {
            sender.sendMessage(ChatColor.GREEN + target.getName() + " is currently online!");
        } else {
            long lastSeen = plugin.getPlayerInfoManager().getLastSeen(target);
            if (lastSeen > 0) {
                long secondsAgo = (System.currentTimeMillis() - lastSeen) / 1000;
                sender.sendMessage(ChatColor.YELLOW + target.getName() + " was last seen " + formatTime(secondsAgo) + " ago.");
            } else {
                sender.sendMessage(ChatColor.RED + "No data found for " + target.getName());
            }
        }
        return true;
    }

    private boolean handleList(CommandSender sender) {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (players.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No players online.");
            return true;
        }

        StringBuilder list = new StringBuilder();
        list.append(ChatColor.GREEN).append("Online players (").append(players.size()).append("): ");
        
        String[] playerNames = players.stream()
            .map(Player::getName)
            .toArray(String[]::new);
        list.append(String.join(", ", playerNames));

        sender.sendMessage(list.toString());
        return true;
    }

    private boolean handlePing(CommandSender sender, String[] args) {
        Player target = null;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: /ping [player]");
                return true;
            }
            target = (Player) sender;
        } else if (args.length == 1) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /ping [player]");
            return true;
        }

        int ping = target.getPing();
        ChatColor color = ping < 50 ? ChatColor.GREEN : ping < 100 ? ChatColor.YELLOW : ChatColor.RED;
        sender.sendMessage(ChatColor.GREEN + target.getName() + "'s ping: " + color + ping + "ms");
        return true;
    }

    private boolean handleNear(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        int radius = 50; // Default radius

        if (args.length > 0) {
            try {
                radius = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid radius! Usage: /near [radius]");
                return true;
            }
        }

        List<Player> nearby = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(player)) continue;
            if (p.getWorld().equals(player.getWorld())) {
                double distance = p.getLocation().distance(player.getLocation());
                if (distance <= radius) {
                    nearby.add(p);
                }
            }
        }

        if (nearby.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No players nearby within " + radius + " blocks.");
        } else {
            player.sendMessage(ChatColor.GREEN + "Players nearby (" + nearby.size() + "):");
            for (Player p : nearby) {
                double distance = p.getLocation().distance(player.getLocation());
                player.sendMessage(ChatColor.YELLOW + "  - " + p.getName() + ChatColor.GRAY + 
                    " (" + String.format("%.1f", distance) + " blocks away)");
            }
        }
        return true;
    }

    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutes";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " hours";
        } else {
            return (seconds / 86400) + " days";
        }
    }
}

