package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.database.BlockLogDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Command handler for block logging operations.
 * Commands: /blocklog, /bl, /co (CoreProtect-like aliases)
 */
public class BlockLogCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public BlockLogCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ecore.blocklog.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            // Open GUI
            plugin.getBlockLogGUIManager().openMainGUI(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "lookup":
            case "l":
                handleLookup(player, args);
                break;
            case "rollback":
            case "rb":
                handleRollback(player, args);
                break;
            case "restore":
            case "rs":
                handleRestore(player, args);
                break;
            case "inspect":
            case "i":
                handleInspect(player, args);
                break;
            case "inventory":
            case "inv":
                handleInventory(player, args);
                break;
            case "purge":
                handlePurge(player, args);
                break;
            case "reload":
                handleReload(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /blocklog for GUI or:");
                sendHelp(player);
        }

        return true;
    }

    private void handleLookup(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /blocklog lookup <player> [time]");
            return;
        }

        if (!player.hasPermission("ecore.blocklog.lookup")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to lookup logs!");
            return;
        }

        String targetName = args[1];
        long timeRange = parseTimeRange(args.length > 2 ? args[2] : "1h");

        Player target = Bukkit.getPlayer(targetName);
        UUID targetUuid = target != null ? target.getUniqueId() : null;

        if (targetUuid == null) {
            // Try to get UUID from offline player
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (offlinePlayer.hasPlayedBefore()) {
                targetUuid = offlinePlayer.getUniqueId();
            } else {
                player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
                return;
            }
        }

        plugin.getBlockLogGUIManager().openLookupGUI(player, targetUuid, targetName, timeRange);
    }

    private void handleRollback(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /blocklog rollback <player> [time]");
            return;
        }

        if (!player.hasPermission("ecore.blocklog.rollback")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to rollback!");
            return;
        }

        String targetName = args[1];
        long timeRange = parseTimeRange(args.length > 2 ? args[2] : "1h");

        Player target = Bukkit.getPlayer(targetName);
        UUID targetUuid = target != null ? target.getUniqueId() : null;

        if (targetUuid == null) {
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (offlinePlayer.hasPlayedBefore()) {
                targetUuid = offlinePlayer.getUniqueId();
            } else {
                player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
                return;
            }
        }

        plugin.getBlockLogManager().rollbackPlayer(targetUuid, timeRange, player);
    }

    private void handleRestore(Player player, String[] args) {
        if (!player.hasPermission("ecore.blocklog.restore")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to restore!");
            return;
        }

        Location pos1 = plugin.getBlockLogManager().getInspectorSelection(player);
        if (pos1 == null) {
            player.sendMessage(ChatColor.RED + "You need to select an area first! Use /blocklog inspect");
            return;
        }

        // For now, restore uses the selected area
        // Could be enhanced to use pos1/pos2 selection
        long timeRange = parseTimeRange(args.length > 1 ? args[1] : "1h");
        plugin.getBlockLogManager().rollbackArea(pos1, pos1, timeRange, player);
    }

    private void handleInspect(Player player, String[] args) {
        if (!player.hasPermission("ecore.blocklog.inspect")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to inspect!");
            return;
        }

        // Give inspector wand
        ItemStack wand = new ItemStack(Material.WOODEN_AXE);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Block Inspector");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Right-click a block to inspect");
        lore.add(ChatColor.GRAY + "its history");
        meta.setLore(lore);
        wand.setItemMeta(meta);

        player.getInventory().addItem(wand);
        player.sendMessage(ChatColor.GREEN + "Inspector wand given! Right-click a block to inspect it.");
    }

    private void handleInventory(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /blocklog inventory <player> [time]");
            return;
        }

        if (!player.hasPermission("ecore.blocklog.inventory")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to rollback inventories!");
            return;
        }

        String targetName = args[1];
        long timeRange = parseTimeRange(args.length > 2 ? args[2] : "1h");

        Player target = Bukkit.getPlayer(targetName);
        UUID targetUuid = target != null ? target.getUniqueId() : null;

        if (targetUuid == null) {
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (offlinePlayer.hasPlayedBefore()) {
                targetUuid = offlinePlayer.getUniqueId();
            } else {
                player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
                return;
            }
        }

        plugin.getBlockLogGUIManager().openInventoryRollbackGUI(player, targetUuid, targetName, timeRange);
    }

    private void handlePurge(Player player, String[] args) {
        if (!player.hasPermission("ecore.blocklog.purge")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to purge logs!");
            return;
        }

        int days = args.length > 1 ? Integer.parseInt(args[1]) : 30;
        plugin.getBlockLogManager().getDatabase().purgeOldLogs(days);
        player.sendMessage(ChatColor.GREEN + "Purging logs older than " + days + " days...");
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("ecore.blocklog.reload")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to reload!");
            return;
        }

        plugin.getConfigManager().reloadConfig();
        player.sendMessage(ChatColor.GREEN + "Block logging configuration reloaded!");
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Block Log Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/blocklog lookup <player> [time] - View player logs");
        player.sendMessage(ChatColor.YELLOW + "/blocklog rollback <player> [time] - Rollback player actions");
        player.sendMessage(ChatColor.YELLOW + "/blocklog restore [time] - Restore selected area");
        player.sendMessage(ChatColor.YELLOW + "/blocklog inspect - Get inspector wand");
        player.sendMessage(ChatColor.YELLOW + "/blocklog inventory <player> [time] - Rollback inventory");
        player.sendMessage(ChatColor.YELLOW + "/blocklog purge [days] - Purge old logs");
    }

    private long parseTimeRange(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return 3600000L; // 1 hour default

        timeStr = timeStr.toLowerCase();
        long multiplier = 1;

        if (timeStr.endsWith("s")) {
            multiplier = 1000L;
            timeStr = timeStr.substring(0, timeStr.length() - 1);
        } else if (timeStr.endsWith("m")) {
            multiplier = 60L * 1000L;
            timeStr = timeStr.substring(0, timeStr.length() - 1);
        } else if (timeStr.endsWith("h")) {
            multiplier = 60L * 60L * 1000L;
            timeStr = timeStr.substring(0, timeStr.length() - 1);
        } else if (timeStr.endsWith("d")) {
            multiplier = 24L * 60L * 60L * 1000L;
            timeStr = timeStr.substring(0, timeStr.length() - 1);
        }

        try {
            long value = Long.parseLong(timeStr);
            return value * multiplier;
        } catch (NumberFormatException e) {
            return 3600000L; // Default 1 hour
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("lookup", "rollback", "restore", "inspect", "inventory", "purge", "reload")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("lookup") || 
                                 args[0].equalsIgnoreCase("rollback") || 
                                 args[0].equalsIgnoreCase("inventory"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("lookup") || 
                                args[0].equalsIgnoreCase("rollback") || 
                                args[0].equalsIgnoreCase("inventory"))) {
            return Arrays.asList("1m", "5m", "10m", "30m", "1h", "6h", "12h", "1d", "7d")
                    .stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}

