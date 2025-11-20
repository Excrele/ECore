package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.PartyManager;
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
 * Command handler for party system.
 */
public class PartyCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public PartyCommand(Ecore plugin) {
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
            plugin.getPartyGUIManager().openPartyGUI(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreate(player);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party invite <player>");
                    return true;
                }
                handleInvite(player, args[1]);
                break;
            case "accept":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party accept <leader>");
                    return true;
                }
                handleAccept(player, args[1]);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "kick":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party kick <player>");
                    return true;
                }
                handleKick(player, args[1]);
                break;
            case "list":
            case "info":
                handleInfo(player);
                break;
            case "chat":
            case "c":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party chat <message>");
                    return true;
                }
                handleChat(player, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /party for GUI or:");
                sendHelp(player);
        }

        return true;
    }

    private void handleCreate(Player player) {
        if (plugin.getPartyManager().createParty(player) != null) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "You are already in a party!");
        }
    }

    private void handleInvite(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            return;
        }

        if (plugin.getPartyManager().invitePlayer(player, target)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "Could not invite " + targetName + ". You may not be the party leader or the party is full.");
        }
    }

    private void handleAccept(Player player, String leaderName) {
        Player leader = Bukkit.getPlayer(leaderName);
        if (leader == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + leaderName);
            return;
        }

        if (plugin.getPartyManager().acceptInvite(player, leader)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "No pending invite from " + leaderName);
        }
    }

    private void handleLeave(Player player) {
        if (plugin.getPartyManager().leaveParty(player)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
        }
    }

    private void handleKick(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            return;
        }

        if (plugin.getPartyManager().kickPlayer(player, target)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "Could not kick " + targetName + ". You may not be the party leader.");
        }
    }

    private void handleInfo(Player player) {
        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "          Party Information");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.GRAY + "Leader: " + ChatColor.WHITE + party.getLeaderName());
        player.sendMessage(ChatColor.GRAY + "Members: " + ChatColor.WHITE + party.getMembers().size() + "/" + party.getMaxSize());
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Members:");
        for (String memberName : party.getMembers().values()) {
            Player member = Bukkit.getPlayer(memberName);
            String status = member != null && member.isOnline() ? 
                ChatColor.GREEN + "● Online" : ChatColor.GRAY + "○ Offline";
            player.sendMessage(ChatColor.WHITE + "  " + memberName + " " + status);
        }
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
    }

    private void handleChat(Player player, String message) {
        plugin.getPartyManager().sendPartyMessage(player, message);
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Party Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/party create - Create a party");
        player.sendMessage(ChatColor.YELLOW + "/party invite <player> - Invite player");
        player.sendMessage(ChatColor.YELLOW + "/party accept <leader> - Accept invite");
        player.sendMessage(ChatColor.YELLOW + "/party leave - Leave party");
        player.sendMessage(ChatColor.YELLOW + "/party kick <player> - Kick player (leader only)");
        player.sendMessage(ChatColor.YELLOW + "/party list - Show party info");
        player.sendMessage(ChatColor.YELLOW + "/party chat <message> - Send party message");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "invite", "accept", "leave", "kick", "list", "info", "chat", "c")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || 
                                 args[0].equalsIgnoreCase("accept") ||
                                 args[0].equalsIgnoreCase("kick"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}

