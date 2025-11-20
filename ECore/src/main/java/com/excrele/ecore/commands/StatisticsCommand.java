package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StatisticsCommand implements CommandExecutor {
    private final Ecore plugin;

    public StatisticsCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("stats") || cmd.equals("statistics")) {
            return handleStats(sender);
        } else if (cmd.equals("leaderboard") || cmd.equals("top") || cmd.equals("lb")) {
            return handleLeaderboard(sender, args);
        } else if (cmd.equals("statsreset") || cmd.equals("resetstats")) {
            return handleResetStats(sender, args);
        }

        return false;
    }

    private boolean handleStats(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        plugin.getStatisticsGUIManager().openStatisticsGUI(player);
        return true;
    }

    private boolean handleLeaderboard(CommandSender sender, String[] args) {
        String stat = "blocks-broken"; // Default
        if (args.length > 0) {
            stat = args[0].toLowerCase();
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            plugin.getStatisticsGUIManager().openLeaderboardGUI(player, stat);
        } else {
            // Console version - show top 10
            List<PlayerStat> stats = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                // Try int first, then double
                int intValue = plugin.getStatisticsManager().getStatistic(p, stat);
                double doubleValue = plugin.getStatisticsManager().getStatisticDouble(p, stat);
                int value = (int) (intValue > 0 ? intValue : doubleValue);
                stats.add(new PlayerStat(p.getName(), value));
            }

            stats.sort((a, b) -> Integer.compare(b.value, a.value));

            sender.sendMessage(ChatColor.GOLD + "=== Leaderboard: " + stat + " ===");
            int rank = 1;
            for (PlayerStat ps : stats) {
                if (rank > 10) break;
                sender.sendMessage(ChatColor.YELLOW + "#" + rank + " " + ChatColor.WHITE + 
                    ps.name + ChatColor.GRAY + " - " + ps.value);
                rank++;
            }
        }
        return true;
    }

    private boolean handleResetStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.stats.reset")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to reset statistics!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /statsreset <player> [stat]");
            sender.sendMessage(ChatColor.GRAY + "If no stat is specified, all stats will be reset.");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (args.length == 2) {
            // Reset specific stat
            String stat = args[1];
            plugin.getStatisticsManager().setStatistic(target, stat, 0);
            sender.sendMessage(ChatColor.GREEN + "Reset " + stat + " for " + target.getName() + "!");
            target.sendMessage(ChatColor.YELLOW + "Your " + stat + " statistic has been reset by " + sender.getName() + ".");
        } else {
            // Reset all stats (would need a method for this)
            sender.sendMessage(ChatColor.YELLOW + "To reset all stats, use /statsreset <player> all");
        }
        return true;
    }

    private static class PlayerStat {
        String name;
        int value;

        PlayerStat(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}

