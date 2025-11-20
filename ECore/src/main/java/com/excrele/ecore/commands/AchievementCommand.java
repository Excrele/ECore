package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.AchievementManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class AchievementCommand implements CommandExecutor {
    private final Ecore plugin;

    public AchievementCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("achievements") || cmd.equals("achievement")) {
            if (args.length == 0) {
                return handleList(sender);
            }

            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "list":
                    return handleList(sender);
                case "give":
                    return handleGive(sender, args);
                case "check":
                    return handleCheck(sender, args);
                default:
                    sender.sendMessage(ChatColor.RED + "Usage: /achievements [list|give|check]");
                    return true;
            }
        }

        return false;
    }

    private boolean handleList(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        List<String> unlocked = plugin.getAchievementManager().getPlayerAchievements(player);
        Map<String, AchievementManager.Achievement> allAchievements = plugin.getAchievementManager().getAllAchievements();

        player.sendMessage(ChatColor.GOLD + "=== Your Achievements ===");
        player.sendMessage(ChatColor.GRAY + "Unlocked: " + ChatColor.GREEN + unlocked.size() + 
            ChatColor.GRAY + " / " + ChatColor.YELLOW + allAchievements.size());

        if (unlocked.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "You haven't unlocked any achievements yet!");
        } else {
            player.sendMessage(ChatColor.GREEN + "Unlocked Achievements:");
            for (String achievementId : unlocked) {
                AchievementManager.Achievement achievement = plugin.getAchievementManager().getAchievement(achievementId);
                if (achievement != null) {
                    player.sendMessage(ChatColor.GREEN + "  ✓ " + achievement.getName() + 
                        ChatColor.GRAY + " - " + achievement.getDescription());
                }
            }
        }

        // Show locked achievements
        player.sendMessage(ChatColor.RED + "Locked Achievements:");
        for (AchievementManager.Achievement achievement : allAchievements.values()) {
            if (!unlocked.contains(achievement.getId())) {
                player.sendMessage(ChatColor.RED + "  ✗ " + achievement.getName() + 
                    ChatColor.GRAY + " - " + achievement.getDescription());
            }
        }

        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.achievement.give")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to give achievements!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /achievement give <player> <achievement-id>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        String achievementId = args[2];
        if (plugin.getAchievementManager().unlockAchievement(target, achievementId)) {
            sender.sendMessage(ChatColor.GREEN + "Achievement '" + achievementId + "' given to " + target.getName() + "!");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to give achievement. It may not exist or already be unlocked!");
        }
        return true;
    }

    private boolean handleCheck(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        plugin.getAchievementManager().checkAchievements(player);
        player.sendMessage(ChatColor.GREEN + "Achievements checked! You may have unlocked new ones.");
        return true;
    }
}

