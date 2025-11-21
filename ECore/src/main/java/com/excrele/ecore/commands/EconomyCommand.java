package com.excrele.ecore.commands;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.excrele.ecore.Ecore;

public class EconomyCommand implements CommandExecutor {
    private final Ecore plugin;

    public EconomyCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("balance") || cmd.equals("bal") || cmd.equals("money")) {
            return handleBalance(sender, args);
        } else if (cmd.equals("pay")) {
            return handlePay(sender, args);
        } else if (cmd.equals("economy") || cmd.equals("eco")) {
            return handleEconomy(sender, args);
        } else if (cmd.equals("baltop") || cmd.equals("balancetop")) {
            return handleBalanceTop(sender, args);
        }

        return false;
    }

    private boolean handleBalance(CommandSender sender, String[] args) {
        Player target = null;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: /balance <player>");
                return true;
            }
            target = (Player) sender;
        } else if (args.length == 1) {
            if (!sender.hasPermission("ecore.economy.balance.others")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission!");
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /balance [player]");
            return true;
        }

        double balance = plugin.getEconomyManager().getBalance(target.getUniqueId());
        sender.sendMessage(ChatColor.GREEN + target.getName() + "'s balance: " + 
            ChatColor.GOLD + String.format("%.2f", balance));
        return true;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /pay <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (target == player) {
            player.sendMessage(ChatColor.RED + "You can't pay yourself!");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount!");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "Amount must be positive!");
            return true;
        }

        if (plugin.getEconomyManager().transferBalance(player.getUniqueId(), target.getUniqueId(), amount)) {
            player.sendMessage(ChatColor.GREEN + "Paid " + String.format("%.2f", amount) + " to " + target.getName() + "!");
            target.sendMessage(ChatColor.GREEN + "Received " + String.format("%.2f", amount) + " from " + player.getName() + "!");
        } else {
            player.sendMessage(ChatColor.RED + "You don't have enough money!");
        }
        return true;
    }

    private boolean handleEconomy(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.economy.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /economy <give|take|set|stats> [player] [amount]");
            return true;
        }

        if (args[0].equalsIgnoreCase("stats")) {
            return handleEconomyStats(sender);
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /economy <give|take|set> <player> <amount>");
            return true;
        }

        // Try online player first, then offline
        Player onlineTarget = Bukkit.getPlayer(args[1]);
        OfflinePlayer target;
        if (onlineTarget != null) {
            target = onlineTarget;
        } else {
            // Try to get offline player (deprecated but still works)
            target = Bukkit.getOfflinePlayer(args[1]);
            if (target != null && !target.hasPlayedBefore()) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
        }
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        UUID uuid = target.getUniqueId();
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount!");
            return true;
        }

        String action = args[0].toLowerCase();
        switch (action) {
            case "give":
                plugin.getEconomyManager().addBalance(uuid, amount);
                sender.sendMessage(ChatColor.GREEN + "Gave " + String.format("%.2f", amount) + " to " + target.getName() + "!");
                break;
            case "take":
                if (plugin.getEconomyManager().removeBalance(uuid, amount)) {
                    sender.sendMessage(ChatColor.GREEN + "Took " + String.format("%.2f", amount) + " from " + target.getName() + "!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Player doesn't have enough money!");
                }
                break;
            case "set":
                plugin.getEconomyManager().setBalance(uuid, amount);
                sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s balance to " + String.format("%.2f", amount) + "!");
                break;
            case "stats":
                return handleEconomyStats(sender);
            default:
                sender.sendMessage(ChatColor.RED + "Usage: /economy <give|take|set|stats> [player] [amount]");
                return true;
        }
        return true;
    }

    private boolean handleBalanceTop(CommandSender sender, String[] args) {
        int limit = 10;
        if (args.length > 0) {
            try {
                limit = Integer.parseInt(args[0]);
                if (limit < 1 || limit > 100) {
                    sender.sendMessage(ChatColor.RED + "Limit must be between 1 and 100!");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid limit!");
                return true;
            }
        }

        List<Map.Entry<UUID, Double>> leaderboard = plugin.getEconomyManager().getLeaderboard(limit);
        
        if (leaderboard.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No players with balances found.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Top " + leaderboard.size() + " Richest Players ===");
        int rank = 1;
        for (Map.Entry<UUID, Double> entry : leaderboard) {
            Player player = Bukkit.getPlayer(entry.getKey());
            String name = player != null ? player.getName() : Bukkit.getOfflinePlayer(entry.getKey()).getName();
            if (name == null) name = entry.getKey().toString();
            
            sender.sendMessage(ChatColor.YELLOW + "#" + rank + " " + ChatColor.WHITE + name + 
                ChatColor.GRAY + " - " + ChatColor.GOLD + String.format("%.2f", entry.getValue()));
            rank++;
        }
        return true;
    }

    // Add economy stats command handler
    public boolean handleEconomyStats(CommandSender sender) {
        if (!sender.hasPermission("ecore.economy.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        Map<String, Object> stats = plugin.getEconomyManager().getStatistics();
        sender.sendMessage(ChatColor.GOLD + "=== Economy Statistics ===");
        sender.sendMessage(ChatColor.YELLOW + "Total Money: " + ChatColor.WHITE + 
            String.format("%.2f", (Double) stats.get("totalMoney")));
        sender.sendMessage(ChatColor.YELLOW + "Players with Balance: " + ChatColor.WHITE + stats.get("playerCount"));
        sender.sendMessage(ChatColor.YELLOW + "Average Balance: " + ChatColor.WHITE + 
            String.format("%.2f", (Double) stats.get("averageBalance")));
        sender.sendMessage(ChatColor.YELLOW + "Highest Balance: " + ChatColor.WHITE + 
            String.format("%.2f", (Double) stats.get("maxBalance")));
        sender.sendMessage(ChatColor.YELLOW + "Total Transactions: " + ChatColor.WHITE + stats.get("transactionCount"));
        return true;
    }
}

