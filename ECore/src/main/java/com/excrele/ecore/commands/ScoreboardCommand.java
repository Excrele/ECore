package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for scoreboard and tab list management.
 */
public class ScoreboardCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public ScoreboardCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "toggle":
            case "t":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                    return true;
                }
                handleToggle((Player) sender);
                break;
            case "reload":
                if (!sender.hasPermission("ecore.scoreboard.reload")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to reload scoreboards!");
                    return true;
                }
                handleReload(sender);
                break;
            case "tablist":
            case "tab":
                if (args.length < 2) {
                    sendTabListHelp(sender);
                    return true;
                }
                String tabSubCommand = args[1].toLowerCase();
                if (tabSubCommand.equals("reload")) {
                    if (!sender.hasPermission("ecore.tablist.reload")) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to reload tab list!");
                        return true;
                    }
                    handleTabListReload(sender);
                } else {
                    sendTabListHelp(sender);
                }
                break;
            default:
                sendHelp(sender);
        }

        return true;
    }

    private void handleToggle(Player player) {
        if (!player.hasPermission("ecore.scoreboard.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use scoreboards!");
            return;
        }

        plugin.getScoreboardManager().toggleScoreboard(player);
    }

    private void handleReload(CommandSender sender) {
        plugin.getScoreboardManager().reload();
        sender.sendMessage(ChatColor.GREEN + "Scoreboard configuration reloaded!");
    }

    private void handleTabListReload(CommandSender sender) {
        plugin.getTabListManager().reload();
        sender.sendMessage(ChatColor.GREEN + "Tab list configuration reloaded!");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Scoreboard Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/scoreboard toggle - Toggle your scoreboard");
        sender.sendMessage(ChatColor.YELLOW + "/scoreboard reload - Reload scoreboard config (admin)");
        sender.sendMessage(ChatColor.YELLOW + "/scoreboard tablist reload - Reload tab list config (admin)");
    }

    private void sendTabListHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Tab List Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/scoreboard tablist reload - Reload tab list config (admin)");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("toggle", "reload", "tablist")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("tablist")) {
            return Arrays.asList("reload")
                    .stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}

