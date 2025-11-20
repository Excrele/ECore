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
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Command handler for friend system.
 */
public class FriendCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public FriendCommand(Ecore plugin) {
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
            plugin.getFriendGUIManager().openFriendGUI(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /friend add <player>");
                    return true;
                }
                handleAdd(player, args[1]);
                break;
            case "remove":
            case "delete":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /friend remove <player>");
                    return true;
                }
                handleRemove(player, args[1]);
                break;
            case "list":
                handleList(player);
                break;
            case "accept":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /friend accept <player>");
                    return true;
                }
                handleAccept(player, args[1]);
                break;
            case "deny":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /friend deny <player>");
                    return true;
                }
                handleDeny(player, args[1]);
                break;
            case "requests":
                handleRequests(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /friend for GUI or:");
                sendHelp(player);
        }

        return true;
    }

    private void handleAdd(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            return;
        }

        if (plugin.getFriendManager().sendFriendRequest(player, target)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "Could not send friend request. You may already be friends or have a pending request.");
        }
    }

    private void handleRemove(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            return;
        }

        if (plugin.getFriendManager().removeFriend(player, target)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "You are not friends with " + targetName);
        }
    }

    private void handleList(Player player) {
        List<UUID> friends = plugin.getFriendManager().getFriends(player.getUniqueId());
        
        if (friends.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You have no friends. Use /friend add <player> to add one!");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "          Your Friends (" + friends.size() + ")");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");

        for (UUID friendUuid : friends) {
            Player friend = Bukkit.getPlayer(friendUuid);
            String status = friend != null && friend.isOnline() ? 
                ChatColor.GREEN + "● Online" : ChatColor.GRAY + "○ Offline";
            String name = friend != null ? friend.getName() : 
                Bukkit.getOfflinePlayer(friendUuid).getName();
            
            player.sendMessage(ChatColor.WHITE + name + " " + status);
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
    }

    private void handleAccept(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            return;
        }

        if (plugin.getFriendManager().acceptFriendRequest(player, target)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "No pending friend request from " + targetName);
        }
    }

    private void handleDeny(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            return;
        }

        if (plugin.getFriendManager().denyFriendRequest(player, target)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "No pending friend request from " + targetName);
        }
    }

    private void handleRequests(Player player) {
        List<UUID> requests = plugin.getFriendManager().getPendingRequests(player.getUniqueId());
        
        if (requests.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You have no pending friend requests.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "     Pending Friend Requests (" + requests.size() + ")");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");

        for (UUID requesterUuid : requests) {
            Player requester = Bukkit.getPlayer(requesterUuid);
            String name = requester != null ? requester.getName() : 
                Bukkit.getOfflinePlayer(requesterUuid).getName();
            
            player.sendMessage(ChatColor.WHITE + name + ChatColor.GRAY + " - /friend accept " + name);
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Friend Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/friend add <player> - Send friend request");
        player.sendMessage(ChatColor.YELLOW + "/friend remove <player> - Remove friend");
        player.sendMessage(ChatColor.YELLOW + "/friend list - List friends");
        player.sendMessage(ChatColor.YELLOW + "/friend accept <player> - Accept request");
        player.sendMessage(ChatColor.YELLOW + "/friend deny <player> - Deny request");
        player.sendMessage(ChatColor.YELLOW + "/friend requests - View pending requests");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("add", "remove", "list", "accept", "deny", "requests")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("add") || 
                                 args[0].equalsIgnoreCase("remove") ||
                                 args[0].equalsIgnoreCase("accept") ||
                                 args[0].equalsIgnoreCase("deny"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}

