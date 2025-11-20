package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for custom enchantments system.
 */
public class EnchantmentCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public EnchantmentCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "apply":
            case "add":
                if (!player.hasPermission("ecore.enchant.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                return handleApply(player, args);
            case "remove":
            case "rem":
                if (!player.hasPermission("ecore.enchant.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                return handleRemove(player, args);
            case "list":
                return handleList(player);
            case "info":
                return handleInfo(player, args);
            default:
                sendHelp(player);
                return true;
        }
    }

    private boolean handleApply(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /enchant apply <enchantment> [level]");
            return true;
        }

        String enchantId = args[1].toLowerCase();
        if (!plugin.getEnchantmentManager().hasEnchantment(enchantId)) {
            player.sendMessage(ChatColor.RED + "Enchantment '" + enchantId + "' not found!");
            return true;
        }

        int level = 1;
        if (args.length > 2) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid level: " + args[2]);
                return true;
            }
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().toString().contains("AIR")) {
            player.sendMessage(ChatColor.RED + "You must hold an item in your hand!");
            return true;
        }

        if (plugin.getEnchantmentManager().applyEnchantment(item, enchantId, level)) {
            player.sendMessage(ChatColor.GREEN + "Applied " + 
                plugin.getEnchantmentManager().getEnchantment(enchantId).getName() + 
                " " + toRomanNumeral(level) + " to your item!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to apply enchantment! Check if it can be applied to this item type.");
        }

        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /enchant remove <enchantment>");
            return true;
        }

        String enchantId = args[1].toLowerCase();
        if (!plugin.getEnchantmentManager().hasEnchantment(enchantId)) {
            player.sendMessage(ChatColor.RED + "Enchantment '" + enchantId + "' not found!");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().toString().contains("AIR")) {
            player.sendMessage(ChatColor.RED + "You must hold an item in your hand!");
            return true;
        }

        if (plugin.getEnchantmentManager().removeEnchantment(item, enchantId)) {
            player.sendMessage(ChatColor.GREEN + "Removed " + 
                plugin.getEnchantmentManager().getEnchantment(enchantId).getName() + " from your item!");
        } else {
            player.sendMessage(ChatColor.RED + "Enchantment not found on this item!");
        }

        return true;
    }

    private boolean handleList(Player player) {
        var enchantments = plugin.getEnchantmentManager().getEnchantments();
        
        if (enchantments.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No custom enchantments found.");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "      Custom Enchantments (" + enchantments.size() + ")");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");

        for (var entry : enchantments.entrySet()) {
            var enchant = entry.getValue();
            player.sendMessage(ChatColor.WHITE + "- " + ChatColor.YELLOW + entry.getKey() + 
                ChatColor.GRAY + " (" + enchant.getName() + ")");
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        return true;
    }

    private boolean handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /enchant info <enchantment>");
            return true;
        }

        String enchantId = args[1].toLowerCase();
        if (!plugin.getEnchantmentManager().hasEnchantment(enchantId)) {
            player.sendMessage(ChatColor.RED + "Enchantment '" + enchantId + "' not found!");
            return true;
        }

        var enchant = plugin.getEnchantmentManager().getEnchantment(enchantId);
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "          " + enchant.getName());
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.WHITE + "ID: " + ChatColor.GRAY + enchantId);
        player.sendMessage(ChatColor.WHITE + "Description: " + ChatColor.GRAY + enchant.getDescription());
        player.sendMessage(ChatColor.WHITE + "Max Level: " + ChatColor.GRAY + enchant.getMaxLevel());
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Enchantment Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/enchant list - List all custom enchantments");
        player.sendMessage(ChatColor.YELLOW + "/enchant info <id> - View enchantment info");
        if (player.hasPermission("ecore.enchant.admin")) {
            player.sendMessage(ChatColor.YELLOW + "/enchant apply <id> [level] - Apply enchantment to held item");
            player.sendMessage(ChatColor.YELLOW + "/enchant remove <id> - Remove enchantment from held item");
        }
    }

    private String toRomanNumeral(int number) {
        String[] romanNumerals = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        if (number >= 1 && number <= 10) {
            return romanNumerals[number];
        }
        return String.valueOf(number);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("apply", "remove", "list", "info").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("apply") || 
                                 args[0].equalsIgnoreCase("remove") ||
                                 args[0].equalsIgnoreCase("info"))) {
            return plugin.getEnchantmentManager().getEnchantments().keySet().stream()
                    .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}

