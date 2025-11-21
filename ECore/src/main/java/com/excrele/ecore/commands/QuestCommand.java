package com.excrele.ecore.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.QuestManager;

/**
 * Command handler for quests system.
 */
public class QuestCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public QuestCommand(Ecore plugin) {
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
            if (plugin.getQuestGUIManager() != null) {
                plugin.getQuestGUIManager().openQuestGUI(player);
            } else {
                player.sendMessage(ChatColor.RED + "Quest manager is not available!");
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "list":
                handleList(player, args.length > 1 ? args[1] : null);
                break;
            case "start":
            case "accept":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /quest start <quest-id>");
                    return true;
                }
                handleStart(player, args[1]);
                break;
            case "complete":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /quest complete <quest-id>");
                    return true;
                }
                handleComplete(player, args[1]);
                break;
            case "active":
                handleActive(player);
                break;
            case "completed":
                handleCompleted(player);
                break;
            case "info":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /quest info <quest-id>");
                    return true;
                }
                handleInfo(player, args[1]);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /quest for GUI or:");
                sendHelp(player);
        }

        return true;
    }

    private void handleList(Player player, String category) {
        List<QuestManager.Quest> availableQuests = new ArrayList<>();
        
        for (QuestManager.Quest quest : plugin.getQuestManager().getQuests().values()) {
            if (category != null) {
                if (!quest.getCategory().name().equalsIgnoreCase(category)) {
                    continue;
                }
            }
            
            if (plugin.getQuestManager().canStartQuest(player, quest.getId())) {
                availableQuests.add(quest);
            }
        }

        if (availableQuests.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No available quests" + 
                             (category != null ? " in category: " + category : "") + ".");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "          Available Quests (" + availableQuests.size() + ")");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");

        for (QuestManager.Quest quest : availableQuests) {
            String categoryColor = getCategoryColor(quest.getCategory());
            player.sendMessage(ChatColor.WHITE + "• " + categoryColor + quest.getName() + 
                             ChatColor.GRAY + " (" + quest.getId() + ")");
            player.sendMessage(ChatColor.GRAY + "  " + quest.getDescription());
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "Use /quest start <quest-id> to start a quest!");
    }

    private void handleStart(Player player, String questId) {
        if (plugin.getQuestManager().startQuest(player, questId)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "Cannot start quest: " + questId);
            player.sendMessage(ChatColor.GRAY + "You may have already completed it, or prerequisites are not met.");
        }
    }

    private void handleComplete(Player player, String questId) {
        if (plugin.getQuestManager().completeQuest(player, questId)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "Cannot complete quest: " + questId);
        }
    }

    private void handleActive(Player player) {
        List<QuestManager.Quest> activeQuests = plugin.getQuestManager().getActiveQuests(player);

        if (activeQuests.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You have no active quests.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "          Active Quests (" + activeQuests.size() + ")");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");

        for (QuestManager.Quest quest : activeQuests) {
            QuestManager.QuestProgress progress = plugin.getQuestManager().getQuestProgress(
                player.getUniqueId(), quest.getId());
            
            int remaining = quest.getRequiredAmount() - progress.getProgress();
            double percent = (double) progress.getProgress() / quest.getRequiredAmount() * 100.0;
            
            player.sendMessage(ChatColor.WHITE + "• " + ChatColor.GREEN + quest.getName());
            player.sendMessage(ChatColor.GRAY + "  Progress: " + ChatColor.YELLOW + 
                             progress.getProgress() + "/" + quest.getRequiredAmount() + 
                             ChatColor.GRAY + " (" + String.format("%.1f", percent) + "%)");
            player.sendMessage(ChatColor.GRAY + "  " + remaining + " remaining");
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
    }

    private void handleCompleted(Player player) {
        List<QuestManager.Quest> completedQuests = plugin.getQuestManager().getCompletedQuests(player);

        if (completedQuests.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You have not completed any quests yet.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "          Completed Quests (" + completedQuests.size() + ")");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");

        for (QuestManager.Quest quest : completedQuests) {
            player.sendMessage(ChatColor.WHITE + "• " + ChatColor.GREEN + quest.getName() + 
                             ChatColor.GRAY + " ✓");
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
    }

    private void handleInfo(Player player, String questId) {
        QuestManager.Quest quest = plugin.getQuestManager().getQuest(questId);
        
        if (quest == null) {
            player.sendMessage(ChatColor.RED + "Quest not found: " + questId);
            return;
        }

        QuestManager.QuestProgress progress = plugin.getQuestManager().getQuestProgress(
            player.getUniqueId(), questId);

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "          Quest Information");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.WHITE + "Name: " + ChatColor.GREEN + quest.getName());
        player.sendMessage(ChatColor.WHITE + "ID: " + ChatColor.GRAY + quest.getId());
        player.sendMessage(ChatColor.WHITE + "Description: " + ChatColor.GRAY + quest.getDescription());
        player.sendMessage(ChatColor.WHITE + "Type: " + ChatColor.YELLOW + quest.getType().name());
        player.sendMessage(ChatColor.WHITE + "Category: " + getCategoryColor(quest.getCategory()) + quest.getCategory().name());
        player.sendMessage(ChatColor.WHITE + "Required: " + ChatColor.YELLOW + quest.getRequiredAmount());
        
        if (quest.getTargetMaterial() != null) {
            player.sendMessage(ChatColor.WHITE + "Target Material: " + ChatColor.YELLOW + quest.getTargetMaterial().name());
        }
        if (quest.getTargetEntity() != null) {
            player.sendMessage(ChatColor.WHITE + "Target Entity: " + ChatColor.YELLOW + quest.getTargetEntity().name());
        }
        
        player.sendMessage(ChatColor.WHITE + "Progress: " + ChatColor.GREEN + 
                          progress.getProgress() + "/" + quest.getRequiredAmount());
        player.sendMessage(ChatColor.WHITE + "Completed: " + 
                          (progress.isCompleted() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
        
        player.sendMessage(ChatColor.GOLD + "Rewards:");
        if (quest.getRewardMoney() > 0) {
            player.sendMessage(ChatColor.GREEN + "  • $" + String.format("%.2f", quest.getRewardMoney()));
        }
        if (quest.getRewardXP() > 0) {
            player.sendMessage(ChatColor.GREEN + "  • " + quest.getRewardXP() + " XP");
        }
        for (QuestManager.ItemReward reward : quest.getItemRewards()) {
            player.sendMessage(ChatColor.GREEN + "  • " + reward.getAmount() + "x " + reward.getMaterial().name() + 
                             ChatColor.GRAY + " (" + String.format("%.0f", reward.getChance() * 100) + "% chance)");
        }
        
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
    }

    private String getCategoryColor(QuestManager.QuestCategory category) {
        switch (category) {
            case COMBAT: return ChatColor.RED.toString();
            case GATHERING: return ChatColor.GREEN.toString();
            case CRAFTING: return ChatColor.BLUE.toString();
            case EXPLORATION: return ChatColor.AQUA.toString();
            case FARMING: return ChatColor.YELLOW.toString();
            case FISHING: return ChatColor.DARK_AQUA.toString();
            case MINING: return ChatColor.GRAY.toString();
            case DAILY: return ChatColor.GOLD.toString();
            case WEEKLY: return ChatColor.LIGHT_PURPLE.toString();
            case STORY: return ChatColor.DARK_PURPLE.toString();
            default: return ChatColor.WHITE.toString();
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Quest Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/quest - Open quest GUI");
        player.sendMessage(ChatColor.YELLOW + "/quest list [category] - List available quests");
        player.sendMessage(ChatColor.YELLOW + "/quest start <quest-id> - Start a quest");
        player.sendMessage(ChatColor.YELLOW + "/quest active - View active quests");
        player.sendMessage(ChatColor.YELLOW + "/quest completed - View completed quests");
        player.sendMessage(ChatColor.YELLOW + "/quest info <quest-id> - View quest information");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("list", "start", "accept", "complete", "active", "completed", "info")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("start") || 
                                 args[0].equalsIgnoreCase("accept") ||
                                 args[0].equalsIgnoreCase("complete") ||
                                 args[0].equalsIgnoreCase("info"))) {
            return new ArrayList<>(plugin.getQuestManager().getQuests().keySet())
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("list")) {
            return Arrays.asList("COMBAT", "GATHERING", "CRAFTING", "EXPLORATION", "FARMING", 
                               "FISHING", "MINING", "DAILY", "WEEKLY", "STORY", "SIDE")
                    .stream()
                    .filter(s -> s.startsWith(args[1].toUpperCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}

