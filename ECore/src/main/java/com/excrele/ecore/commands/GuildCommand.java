package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.GuildManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class GuildCommand implements CommandExecutor {
    private final Ecore plugin;
    
    public GuildCommand(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use guild commands!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            plugin.getGuildGUIManager().openGuildGUI(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                if (!player.hasPermission("ecore.guild.create")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to create a guild!");
                    return true;
                }
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild create <id> <name> <tag>");
                    return true;
                }
                String guildId = args[1];
                String guildName = args[2];
                String tag = args[3];
                GuildManager.Guild guild = plugin.getGuildManager().createGuild(player, guildId, guildName, tag);
                if (guild == null) {
                    player.sendMessage(ChatColor.RED + "Failed to create guild! You may already be in a guild or the ID is taken.");
                } else {
                    player.sendMessage(ChatColor.GREEN + "Guild created successfully!");
                }
                break;
                
            case "apply":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild apply <guild-id>");
                    return true;
                }
                if (plugin.getGuildManager().applyToGuild(player, args[1])) {
                    player.sendMessage(ChatColor.GREEN + "Application sent!");
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to apply! You may already be in a guild or already applied.");
                }
                break;
                
            case "accept":
                if (!player.hasPermission("ecore.guild.accept")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to accept applications!");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild accept <guild-id> <player>");
                    return true;
                }
                Player applicant = Bukkit.getPlayer(args[2]);
                if (applicant == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
                if (plugin.getGuildManager().acceptApplication(args[1], applicant.getUniqueId(), "member")) {
                    player.sendMessage(ChatColor.GREEN + "Application accepted!");
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to accept application!");
                }
                break;
                
            case "chat":
            case "c":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild chat <message>");
                    return true;
                }
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                plugin.getGuildManager().sendGuildChat(player, message);
                break;
                
            case "deposit":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild deposit <amount>");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    if (plugin.getGuildManager().depositToGuildBank(player, amount)) {
                        player.sendMessage(ChatColor.GREEN + "Deposited " + plugin.getEconomyManager().format(amount) + " to guild bank!");
                    } else {
                        player.sendMessage(ChatColor.RED + "Failed to deposit! You may not have enough money.");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid amount!");
                }
                break;
                
            case "withdraw":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild withdraw <amount>");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    if (plugin.getGuildManager().withdrawFromGuildBank(player, amount)) {
                        player.sendMessage(ChatColor.GREEN + "Withdrew " + plugin.getEconomyManager().format(amount) + " from guild bank!");
                    } else {
                        player.sendMessage(ChatColor.RED + "Failed to withdraw! You may not have permission or the bank doesn't have enough money.");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid amount!");
                }
                break;
                
            case "alliance":
                if (!player.hasPermission("ecore.guild.alliance")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to form alliances!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild alliance <guild-id>");
                    return true;
                }
                GuildManager.Guild playerGuild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
                if (playerGuild == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a guild!");
                    return true;
                }
                if (plugin.getGuildManager().formAlliance(playerGuild.getId(), args[1])) {
                    player.sendMessage(ChatColor.GREEN + "Alliance formed!");
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to form alliance!");
                }
                break;
                
            case "war":
                if (!player.hasPermission("ecore.guild.war")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to declare war!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /guild war <guild-id>");
                    return true;
                }
                GuildManager.Guild playerGuild2 = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
                if (playerGuild2 == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a guild!");
                    return true;
                }
                if (plugin.getGuildManager().declareWar(playerGuild2.getId(), args[1])) {
                    player.sendMessage(ChatColor.RED + "War declared!");
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to declare war!");
                }
                break;
                
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand! Use /guild for GUI or /guild help for commands.");
                break;
        }
        
        return true;
    }
}

