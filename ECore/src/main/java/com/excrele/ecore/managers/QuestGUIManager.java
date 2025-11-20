package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUI manager for quests system.
 */
public class QuestGUIManager implements Listener {
    private final Ecore plugin;
    private final Map<UUID, String> openCategory; // Player UUID -> Category filter

    public QuestGUIManager(Ecore plugin) {
        this.plugin = plugin;
        this.openCategory = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openQuestGUI(Player player) {
        openQuestGUI(player, null);
    }

    public void openQuestGUI(Player player, String category) {
        List<QuestManager.Quest> activeQuests = plugin.getQuestManager().getActiveQuests(player);
        Map<String, QuestManager.Quest> allQuests = plugin.getQuestManager().getQuests();
        
        int size = 54; // 6 rows
        Inventory gui = Bukkit.createInventory(null, size, ChatColor.DARK_PURPLE + "Quests");

        // Category filter buttons (top row)
        int categorySlot = 0;
        for (QuestManager.QuestCategory cat : QuestManager.QuestCategory.values()) {
            if (categorySlot >= 9) break;
            String categoryName = cat.name();
            boolean isSelected = category != null && category.equalsIgnoreCase(categoryName);
            ChatColor color = isSelected ? ChatColor.GREEN : ChatColor.GRAY;
            ItemStack catItem = createMenuItem(getCategoryMaterial(cat), 
                color + categoryName, 
                Arrays.asList(isSelected ? ChatColor.GREEN + "Selected" : ChatColor.GRAY + "Click to filter"));
            gui.setItem(categorySlot++, catItem);
        }

        // Active quests section
        int slot = 9;
        if (!activeQuests.isEmpty()) {
            ItemStack activeHeader = createMenuItem(Material.BOOK, 
                ChatColor.GREEN + "Active Quests (" + activeQuests.size() + ")", 
                Collections.singletonList(ChatColor.GRAY + "Your current quests"));
            gui.setItem(slot++, activeHeader);
            
            for (QuestManager.Quest quest : activeQuests) {
                if (slot >= 45) break;
                if (category != null && !quest.getCategory().name().equalsIgnoreCase(category)) continue;
                
                QuestManager.QuestProgress progress = plugin.getQuestManager().getQuestProgress(
                    player.getUniqueId(), quest.getId());
                ItemStack questItem = createQuestItem(quest, progress, true);
                gui.setItem(slot++, questItem);
            }
        }

        // Available quests section
        if (slot < 45) {
            ItemStack availableHeader = createMenuItem(Material.BOOKSHELF, 
                ChatColor.YELLOW + "Available Quests", 
                Collections.singletonList(ChatColor.GRAY + "Click to start"));
            gui.setItem(slot++, availableHeader);
            
            for (QuestManager.Quest quest : allQuests.values()) {
                if (slot >= 45) break;
                if (category != null && !quest.getCategory().name().equalsIgnoreCase(category)) continue;
                
                if (plugin.getQuestManager().canStartQuest(player, quest.getId())) {
                    QuestManager.QuestProgress progress = plugin.getQuestManager().getQuestProgress(
                        player.getUniqueId(), quest.getId());
                    ItemStack questItem = createQuestItem(quest, progress, false);
                    gui.setItem(slot++, questItem);
                }
            }
        }

        // Action buttons (bottom row)
        ItemStack activeButton = createMenuItem(Material.BOOK, ChatColor.GREEN + "View Active", 
                Arrays.asList(ChatColor.GRAY + "Click to view active quests"));
        gui.setItem(45, activeButton);

        ItemStack completedButton = createMenuItem(Material.ENCHANTED_BOOK, ChatColor.GOLD + "View Completed", 
                Arrays.asList(ChatColor.GRAY + "Click to view completed quests"));
        gui.setItem(46, completedButton);

        ItemStack allButton = createMenuItem(Material.BOOKSHELF, ChatColor.YELLOW + "View All", 
                Arrays.asList(ChatColor.GRAY + "Click to view all quests"));
        gui.setItem(47, allButton);

        // Fill empty slots with glass panes
        for (int i = 0; i < size; i++) {
            if (gui.getItem(i) == null) {
                ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta meta = glass.getItemMeta();
                meta.setDisplayName(" ");
                glass.setItemMeta(meta);
                gui.setItem(i, glass);
            }
        }

        openCategory.put(player.getUniqueId(), category);
        player.openInventory(gui);
    }

    private ItemStack createQuestItem(QuestManager.Quest quest, QuestManager.QuestProgress progress, boolean isActive) {
        ItemStack item = new ItemStack(quest.getIcon());
        ItemMeta meta = item.getItemMeta();
        
        ChatColor nameColor = isActive ? ChatColor.GREEN : ChatColor.YELLOW;
        meta.setDisplayName(nameColor + quest.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + quest.getDescription());
        lore.add("");
        lore.add(ChatColor.WHITE + "Type: " + ChatColor.YELLOW + quest.getType().name());
        lore.add(ChatColor.WHITE + "Category: " + getCategoryColor(quest.getCategory()) + quest.getCategory().name());
        lore.add(ChatColor.WHITE + "Required: " + ChatColor.YELLOW + quest.getRequiredAmount());
        
        if (isActive) {
            lore.add("");
            int remaining = quest.getRequiredAmount() - progress.getProgress();
            double percent = (double) progress.getProgress() / quest.getRequiredAmount() * 100.0;
            lore.add(ChatColor.GREEN + "Progress: " + progress.getProgress() + "/" + quest.getRequiredAmount() + 
                    ChatColor.GRAY + " (" + String.format("%.1f", percent) + "%)");
            lore.add(ChatColor.GRAY + String.valueOf(remaining) + " remaining");
        }
        
        lore.add("");
        lore.add(ChatColor.GOLD + "Rewards:");
        if (quest.getRewardMoney() > 0) {
            lore.add(ChatColor.GREEN + "  • $" + String.format("%.2f", quest.getRewardMoney()));
        }
        if (quest.getRewardXP() > 0) {
            lore.add(ChatColor.GREEN + "  • " + quest.getRewardXP() + " XP");
        }
        for (QuestManager.ItemReward reward : quest.getItemRewards()) {
            lore.add(ChatColor.GREEN + "  • " + reward.getAmount() + "x " + reward.getMaterial().name());
        }
        
        if (!isActive) {
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to start this quest!");
        } else {
            lore.add("");
            lore.add(ChatColor.YELLOW + "Right-click for details");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createMenuItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private Material getCategoryMaterial(QuestManager.QuestCategory category) {
        switch (category) {
            case COMBAT: return Material.IRON_SWORD;
            case GATHERING: return Material.LEATHER;
            case CRAFTING: return Material.CRAFTING_TABLE;
            case EXPLORATION: return Material.COMPASS;
            case FARMING: return Material.GOLDEN_HOE;
            case FISHING: return Material.FISHING_ROD;
            case MINING: return Material.DIAMOND_PICKAXE;
            case DAILY: return Material.SUNFLOWER;
            case WEEKLY: return Material.CLOCK;
            case STORY: return Material.WRITTEN_BOOK;
            default: return Material.BOOK;
        }
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        if (!title.equals(ChatColor.DARK_PURPLE + "Quests")) return;
        
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || 
            clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // Handle category filters (top row)
        if (event.getSlot() < 9) {
            String categoryName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            openQuestGUI(player, categoryName);
            return;
        }

        // Handle action buttons
        if (clicked.getType() == Material.BOOK && clicked.getItemMeta().getDisplayName().contains("View Active")) {
            player.closeInventory();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getCommand("quest").getExecutor().onCommand(player, 
                    plugin.getCommand("quest"), "quest", new String[]{"active"});
            });
            return;
        }

        if (clicked.getType() == Material.ENCHANTED_BOOK && clicked.getItemMeta().getDisplayName().contains("View Completed")) {
            player.closeInventory();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getCommand("quest").getExecutor().onCommand(player, 
                    plugin.getCommand("quest"), "quest", new String[]{"completed"});
            });
            return;
        }

        if (clicked.getType() == Material.BOOKSHELF && clicked.getItemMeta().getDisplayName().contains("View All")) {
            player.closeInventory();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getCommand("quest").getExecutor().onCommand(player, 
                    plugin.getCommand("quest"), "quest", new String[]{"list"});
            });
            return;
        }

        // Handle quest items
        if (clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
            String questName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            
            // Find quest by name
            final String[] questId = {null};
            for (QuestManager.Quest quest : plugin.getQuestManager().getQuests().values()) {
                if (quest.getName().equals(questName)) {
                    questId[0] = quest.getId();
                    break;
                }
            }
            
            if (questId[0] != null) {
                final String finalQuestId = questId[0];
                List<String> lore = clicked.getItemMeta().getLore();
                boolean isActive = lore.stream().anyMatch(s -> s.contains("Progress:"));
                
                if (isActive && event.isRightClick()) {
                    // Show quest info
                    player.closeInventory();
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        plugin.getCommand("quest").getExecutor().onCommand(player, 
                            plugin.getCommand("quest"), "quest", new String[]{"info", finalQuestId});
                    });
                } else if (!isActive) {
                    // Start quest
                    plugin.getQuestManager().startQuest(player, finalQuestId);
                    openQuestGUI(player, openCategory.get(player.getUniqueId()));
                }
            }
        }
    }
}

