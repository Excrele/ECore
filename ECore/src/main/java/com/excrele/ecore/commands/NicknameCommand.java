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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for nickname system.
 */
public class NicknameCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public NicknameCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ecore.nickname")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "set":
            case "s":
                return handleSet(player, args);
            case "reset":
            case "r":
                return handleReset(player);
            case "color":
            case "c":
                return handleColor(player, args);
            case "format":
            case "f":
                return handleFormat(player, args);
            case "view":
            case "v":
                return handleView(player, args);
            default:
                // If no subcommand, treat as set nickname
                return handleSet(player, args);
        }
    }

    private boolean handleSet(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /nick <nickname> or /nick set <nickname>");
            return true;
        }

        String nickname;
        if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("s")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /nick set <nickname>");
                return true;
            }
            StringBuilder nicknameBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) nicknameBuilder.append(" ");
                nicknameBuilder.append(args[i]);
            }
            nickname = nicknameBuilder.toString();
        } else {
            StringBuilder nicknameBuilder = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) nicknameBuilder.append(" ");
                nicknameBuilder.append(args[i]);
            }
            nickname = nicknameBuilder.toString();
        }

        if (plugin.getNicknameManager().setNickname(player, nickname)) {
            player.sendMessage(ChatColor.GREEN + "Nickname set to: " + plugin.getNicknameManager().getFormattedNickname(player));
        } else {
            player.sendMessage(ChatColor.RED + "Failed to set nickname!");
        }

        return true;
    }

    private boolean handleReset(Player player) {
        if (plugin.getNicknameManager().resetNickname(player)) {
            player.sendMessage(ChatColor.GREEN + "Nickname reset to your original name!");
        } else {
            player.sendMessage(ChatColor.YELLOW + "You don't have a nickname set.");
        }
        return true;
    }

    private boolean handleColor(Player player, String[] args) {
        if (!player.hasPermission("ecore.nickname.color")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use nickname colors!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /nick color <color>");
            player.sendMessage(ChatColor.YELLOW + "Available colors: BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE");
            return true;
        }

        try {
            ChatColor color = ChatColor.valueOf(args[1].toUpperCase());
            if (!color.isColor()) {
                player.sendMessage(ChatColor.RED + "Invalid color! Use a color, not a format code.");
                return true;
            }

            if (plugin.getNicknameManager().setNicknameColor(player, color)) {
                player.sendMessage(ChatColor.GREEN + "Nickname color set to: " + color + color.name());
            } else {
                player.sendMessage(ChatColor.RED + "Failed to set nickname color!");
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid color: " + args[1]);
            player.sendMessage(ChatColor.YELLOW + "Available colors: BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE");
        }

        return true;
    }

    private boolean handleFormat(Player player, String[] args) {
        if (!player.hasPermission("ecore.nickname.format")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use nickname formatting!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /nick format <format>");
            player.sendMessage(ChatColor.YELLOW + "Use %nickname% for nickname and %name% for original name");
            player.sendMessage(ChatColor.YELLOW + "Example: &7[&a%nickname%&7]");
            return true;
        }

        StringBuilder formatBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) formatBuilder.append(" ");
            formatBuilder.append(args[i]);
        }
        String format = formatBuilder.toString();

        if (plugin.getNicknameManager().setNicknameFormat(player, format)) {
            player.sendMessage(ChatColor.GREEN + "Nickname format set!");
            player.sendMessage(ChatColor.YELLOW + "Preview: " + plugin.getNicknameManager().getFormattedNickname(player));
        } else {
            player.sendMessage(ChatColor.RED + "Failed to set nickname format!");
        }

        return true;
    }

    private boolean handleView(Player player, String[] args) {
        Player target = player;
        
        if (args.length > 1) {
            if (!player.hasPermission("ecore.nickname.view.others")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to view other players' nicknames!");
                return true;
            }
            
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return true;
            }
        }

        if (plugin.getNicknameManager().hasNickname(target)) {
            String nickname = plugin.getNicknameManager().getFormattedNickname(target);
            player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
            player.sendMessage(ChatColor.YELLOW + "          Nickname Information");
            player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
            player.sendMessage(ChatColor.WHITE + "Player: " + ChatColor.GRAY + target.getName());
            player.sendMessage(ChatColor.WHITE + "Nickname: " + nickname);
            player.sendMessage(ChatColor.WHITE + "Original Name: " + ChatColor.GRAY + target.getName());
            player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        } else {
            player.sendMessage(ChatColor.YELLOW + (target == player ? "You don't" : target.getName() + " doesn't") + " have a nickname set.");
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Nickname Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/nick <nickname> - Set your nickname");
        player.sendMessage(ChatColor.YELLOW + "/nick reset - Reset your nickname");
        player.sendMessage(ChatColor.YELLOW + "/nick view [player] - View nickname");
        
        if (player.hasPermission("ecore.nickname.color")) {
            player.sendMessage(ChatColor.YELLOW + "/nick color <color> - Set nickname color");
        }
        
        if (player.hasPermission("ecore.nickname.format")) {
            player.sendMessage(ChatColor.YELLOW + "/nick format <format> - Set nickname format");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("set", "reset", "color", "format", "view")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("color")) {
            return Arrays.asList("BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", 
                    "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE")
                    .stream()
                    .filter(s -> s.startsWith(args[1].toUpperCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
            if (sender.hasPermission("ecore.nickname.view.others")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}

