package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages the quests system with multiple quest types, chains, and rewards.
 */
public class QuestManager {
    private final Ecore plugin;
    private File questsFile;
    private FileConfiguration questsConfig;
    private File playerQuestsFile;
    private FileConfiguration playerQuestsConfig;
    
    // Quest data structures
    private final Map<String, Quest> quests; // Quest ID -> Quest
    private final Map<UUID, Map<String, QuestProgress>> playerQuests; // Player UUID -> Map<QuestID, Progress>
    
    /**
     * Represents a quest configuration.
     */
    public static class Quest {
        private final String id;
        private final String name;
        private final String description;
        private final QuestType type;
        private final QuestCategory category;
        private final int requiredAmount;
        private final Material targetMaterial;
        private final EntityType targetEntity;
        private final List<String> prerequisites; // Quest IDs that must be completed first
        private final double rewardMoney;
        private final int rewardXP;
        private final List<ItemReward> itemRewards;
        private final boolean isDaily;
        private final boolean isWeekly;
        private final int cooldownHours; // Hours before quest can be repeated
        private final int minLevel; // Minimum player level required
        private final Material icon;
        private final List<String> lore;
        
        public Quest(String id, String name, String description, QuestType type, QuestCategory category,
                    int requiredAmount, Material targetMaterial, EntityType targetEntity,
                    List<String> prerequisites, double rewardMoney, int rewardXP,
                    List<ItemReward> itemRewards, boolean isDaily, boolean isWeekly,
                    int cooldownHours, int minLevel, Material icon, List<String> lore) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.type = type;
            this.category = category;
            this.requiredAmount = requiredAmount;
            this.targetMaterial = targetMaterial;
            this.targetEntity = targetEntity;
            this.prerequisites = prerequisites;
            this.rewardMoney = rewardMoney;
            this.rewardXP = rewardXP;
            this.itemRewards = itemRewards;
            this.isDaily = isDaily;
            this.isWeekly = isWeekly;
            this.cooldownHours = cooldownHours;
            this.minLevel = minLevel;
            this.icon = icon;
            this.lore = lore;
        }
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public QuestType getType() { return type; }
        public QuestCategory getCategory() { return category; }
        public int getRequiredAmount() { return requiredAmount; }
        public Material getTargetMaterial() { return targetMaterial; }
        public EntityType getTargetEntity() { return targetEntity; }
        public List<String> getPrerequisites() { return prerequisites; }
        public double getRewardMoney() { return rewardMoney; }
        public int getRewardXP() { return rewardXP; }
        public List<ItemReward> getItemRewards() { return itemRewards; }
        public boolean isDaily() { return isDaily; }
        public boolean isWeekly() { return isWeekly; }
        public int getCooldownHours() { return cooldownHours; }
        public int getMinLevel() { return minLevel; }
        public Material getIcon() { return icon; }
        public List<String> getLore() { return lore; }
    }
    
    /**
     * Quest types.
     */
    public enum QuestType {
        KILL,           // Kill entities
        COLLECT,        // Collect items
        CRAFT,          // Craft items
        BREAK,          // Break blocks
        PLACE,          // Place blocks
        FISH,           // Catch fish
        BREED,          // Breed animals
        TRAVEL,         // Travel distance
        EAT,            // Eat food
        ENCHANT,        // Enchant items
        TRADE,          // Trade with villagers
        MINE,           // Mine specific blocks
        HARVEST,        // Harvest crops
        CUSTOM          // Custom quest type
    }
    
    /**
     * Quest categories.
     */
    public enum QuestCategory {
        COMBAT,
        GATHERING,
        CRAFTING,
        EXPLORATION,
        FARMING,
        FISHING,
        MINING,
        DAILY,
        WEEKLY,
        STORY,
        SIDE
    }
    
    /**
     * Represents an item reward.
     */
    public static class ItemReward {
        private final Material material;
        private final int amount;
        private final double chance; // 0.0 to 1.0
        
        public ItemReward(Material material, int amount, double chance) {
            this.material = material;
            this.amount = amount;
            this.chance = chance;
        }
        
        public Material getMaterial() { return material; }
        public int getAmount() { return amount; }
        public double getChance() { return chance; }
    }
    
    /**
     * Represents a player's quest progress.
     */
    public static class QuestProgress {
        private int progress;
        private boolean completed;
        private long completedTime; // Timestamp when completed
        private long lastResetTime; // For daily/weekly quests
        
        public QuestProgress() {
            this.progress = 0;
            this.completed = false;
            this.completedTime = 0;
            this.lastResetTime = 0;
        }
        
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public void addProgress(int amount) { this.progress += amount; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public long getCompletedTime() { return completedTime; }
        public void setCompletedTime(long time) { this.completedTime = time; }
        public long getLastResetTime() { return lastResetTime; }
        public void setLastResetTime(long time) { this.lastResetTime = time; }
    }
    
    public QuestManager(Ecore plugin) {
        this.plugin = plugin;
        this.quests = new HashMap<>();
        this.playerQuests = new HashMap<>();
        initializeConfigs();
        loadQuests();
        loadPlayerQuests();
        
        // Schedule daily/weekly quest resets
        scheduleQuestResets();
    }
    
    private void initializeConfigs() {
        // Initialize quests.yml
        questsFile = new File(plugin.getDataFolder(), "quests.yml");
        if (!questsFile.exists()) {
            plugin.saveResource("quests.yml", false);
        }
        questsConfig = YamlConfiguration.loadConfiguration(questsFile);
        
        // Initialize player-quests.yml
        playerQuestsFile = new File(plugin.getDataFolder(), "player-quests.yml");
        if (!playerQuestsFile.exists()) {
            try {
                playerQuestsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create player-quests.yml", e);
            }
        }
        playerQuestsConfig = YamlConfiguration.loadConfiguration(playerQuestsFile);
    }
    
    private void loadQuests() {
        if (questsConfig.getConfigurationSection("quests") == null) {
            plugin.getLogger().warning("No quests found in quests.yml!");
            return;
        }
        
        for (String questId : questsConfig.getConfigurationSection("quests").getKeys(false)) {
            String path = "quests." + questId;
            String name = questsConfig.getString(path + ".name", questId);
            String description = questsConfig.getString(path + ".description", "");
            QuestType type = QuestType.valueOf(questsConfig.getString(path + ".type", "CUSTOM").toUpperCase());
            QuestCategory category = QuestCategory.valueOf(questsConfig.getString(path + ".category", "SIDE").toUpperCase());
            int requiredAmount = questsConfig.getInt(path + ".required-amount", 1);
            
            Material targetMaterial = null;
            if (questsConfig.contains(path + ".target-material")) {
                try {
                    targetMaterial = Material.valueOf(questsConfig.getString(path + ".target-material").toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material for quest " + questId + ": " + questsConfig.getString(path + ".target-material"));
                }
            }
            
            EntityType targetEntity = null;
            if (questsConfig.contains(path + ".target-entity")) {
                try {
                    targetEntity = EntityType.valueOf(questsConfig.getString(path + ".target-entity").toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid entity for quest " + questId + ": " + questsConfig.getString(path + ".target-entity"));
                }
            }
            
            List<String> prerequisites = questsConfig.getStringList(path + ".prerequisites");
            double rewardMoney = questsConfig.getDouble(path + ".rewards.money", 0.0);
            int rewardXP = questsConfig.getInt(path + ".rewards.xp", 0);
            
            List<ItemReward> itemRewards = new ArrayList<>();
            if (questsConfig.getConfigurationSection(path + ".rewards.items") != null) {
                for (String itemKey : questsConfig.getConfigurationSection(path + ".rewards.items").getKeys(false)) {
                    String itemPath = path + ".rewards.items." + itemKey;
                    Material mat = Material.valueOf(questsConfig.getString(itemPath + ".material", "STONE").toUpperCase());
                    int amount = questsConfig.getInt(itemPath + ".amount", 1);
                    double chance = questsConfig.getDouble(itemPath + ".chance", 1.0);
                    itemRewards.add(new ItemReward(mat, amount, chance));
                }
            }
            
            boolean isDaily = questsConfig.getBoolean(path + ".daily", false);
            boolean isWeekly = questsConfig.getBoolean(path + ".weekly", false);
            int cooldownHours = questsConfig.getInt(path + ".cooldown-hours", 0);
            int minLevel = questsConfig.getInt(path + ".min-level", 0);
            
            Material icon = Material.BOOK;
            if (questsConfig.contains(path + ".icon")) {
                try {
                    icon = Material.valueOf(questsConfig.getString(path + ".icon", "BOOK").toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid icon for quest " + questId);
                }
            }
            
            List<String> lore = questsConfig.getStringList(path + ".lore");
            
            quests.put(questId, new Quest(questId, name, description, type, category, requiredAmount,
                                         targetMaterial, targetEntity, prerequisites, rewardMoney, rewardXP,
                                         itemRewards, isDaily, isWeekly, cooldownHours, minLevel, icon, lore));
        }
        
        plugin.getLogger().info("Loaded " + quests.size() + " quests!");
    }
    
    private void loadPlayerQuests() {
        if (playerQuestsConfig.getConfigurationSection("players") == null) return;
        
        for (String uuidStr : playerQuestsConfig.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            String path = "players." + uuidStr;
            Map<String, QuestProgress> playerQuestMap = new HashMap<>();
            
            if (playerQuestsConfig.getConfigurationSection(path + ".quests") != null) {
                for (String questId : playerQuestsConfig.getConfigurationSection(path + ".quests").getKeys(false)) {
                    String questPath = path + ".quests." + questId;
                    QuestProgress progress = new QuestProgress();
                    progress.setProgress(playerQuestsConfig.getInt(questPath + ".progress", 0));
                    progress.setCompleted(playerQuestsConfig.getBoolean(questPath + ".completed", false));
                    progress.setCompletedTime(playerQuestsConfig.getLong(questPath + ".completed-time", 0));
                    progress.setLastResetTime(playerQuestsConfig.getLong(questPath + ".last-reset-time", 0));
                    playerQuestMap.put(questId, progress);
                }
            }
            
            playerQuests.put(uuid, playerQuestMap);
        }
    }
    
    public void savePlayerQuests() {
        try {
            playerQuestsConfig.set("players", null);
            for (Map.Entry<UUID, Map<String, QuestProgress>> entry : playerQuests.entrySet()) {
                String path = "players." + entry.getKey().toString() + ".quests";
                for (Map.Entry<String, QuestProgress> questEntry : entry.getValue().entrySet()) {
                    String questPath = path + "." + questEntry.getKey();
                    QuestProgress progress = questEntry.getValue();
                    playerQuestsConfig.set(questPath + ".progress", progress.getProgress());
                    playerQuestsConfig.set(questPath + ".completed", progress.isCompleted());
                    playerQuestsConfig.set(questPath + ".completed-time", progress.getCompletedTime());
                    playerQuestsConfig.set(questPath + ".last-reset-time", progress.getLastResetTime());
                }
            }
            playerQuestsConfig.save(playerQuestsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save player-quests.yml", e);
        }
    }
    
    /**
     * Gets or creates quest progress for a player.
     */
    public QuestProgress getQuestProgress(UUID uuid, String questId) {
        return playerQuests.computeIfAbsent(uuid, k -> new HashMap<>())
                          .computeIfAbsent(questId, k -> new QuestProgress());
    }
    
    /**
     * Checks if a player can start a quest.
     */
    public boolean canStartQuest(Player player, String questId) {
        Quest quest = quests.get(questId);
        if (quest == null) return false;
        
        UUID uuid = player.getUniqueId();
        QuestProgress progress = getQuestProgress(uuid, questId);
        
        // Check if already completed (unless it's daily/weekly)
        if (progress.isCompleted() && !quest.isDaily() && !quest.isWeekly()) {
            return false;
        }
        
        // Check prerequisites
        for (String prereqId : quest.getPrerequisites()) {
            QuestProgress prereqProgress = getQuestProgress(uuid, prereqId);
            if (!prereqProgress.isCompleted()) {
                return false;
            }
        }
        
        // Check daily/weekly cooldown
        if (quest.isDaily() || quest.isWeekly()) {
            long lastReset = progress.getLastResetTime();
            long now = System.currentTimeMillis();
            long hoursSinceReset = (now - lastReset) / (1000 * 60 * 60);
            
            if (quest.isDaily() && hoursSinceReset < 24) {
                return false;
            }
            if (quest.isWeekly() && hoursSinceReset < 168) { // 7 days
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Starts a quest for a player.
     */
    public boolean startQuest(Player player, String questId) {
        if (!canStartQuest(player, questId)) {
            return false;
        }
        
        Quest quest = quests.get(questId);
        if (quest == null) return false;
        
        UUID uuid = player.getUniqueId();
        QuestProgress progress = getQuestProgress(uuid, questId);
        
        // Reset progress if it's a repeatable quest
        if (quest.isDaily() || quest.isWeekly()) {
            progress.setProgress(0);
            progress.setCompleted(false);
            progress.setLastResetTime(System.currentTimeMillis());
        }
        
        savePlayerQuests();
        player.sendMessage("§aQuest started: §e" + quest.getName());
        return true;
    }
    
    /**
     * Updates quest progress.
     */
    public void updateQuestProgress(Player player, QuestType type, Material material, EntityType entity) {
        UUID uuid = player.getUniqueId();
        Map<String, QuestProgress> playerQuestMap = playerQuests.get(uuid);
        if (playerQuestMap == null) return;
        
        for (Map.Entry<String, Quest> entry : quests.entrySet()) {
            String questId = entry.getKey();
            Quest quest = entry.getValue();
            
            if (quest.getType() != type) continue;
            
            QuestProgress progress = playerQuestMap.get(questId);
            if (progress == null || progress.isCompleted()) continue;
            
            // Check if quest matches target
            boolean matches = false;
            if (type == QuestType.KILL && entity != null) {
                matches = quest.getTargetEntity() == null || quest.getTargetEntity() == entity;
            } else if ((type == QuestType.COLLECT || type == QuestType.CRAFT || 
                       type == QuestType.BREAK || type == QuestType.PLACE || 
                       type == QuestType.MINE || type == QuestType.HARVEST) && material != null) {
                matches = quest.getTargetMaterial() == null || quest.getTargetMaterial() == material;
            } else if (type == QuestType.FISH || type == QuestType.BREED || type == QuestType.TRAVEL) {
                matches = true; // These don't require specific targets
            }
            
            if (matches) {
                progress.addProgress(1);
                if (progress.getProgress() >= quest.getRequiredAmount()) {
                    completeQuest(player, questId);
                } else {
                    // Send progress update
                    int remaining = quest.getRequiredAmount() - progress.getProgress();
                    player.sendMessage("§7[Quest] §e" + quest.getName() + " §7- Progress: §a" + 
                                     progress.getProgress() + "/" + quest.getRequiredAmount() + 
                                     " §7(" + remaining + " remaining)");
                }
            }
        }
        
        savePlayerQuests();
    }
    
    /**
     * Completes a quest and gives rewards.
     */
    public boolean completeQuest(Player player, String questId) {
        Quest quest = quests.get(questId);
        if (quest == null) return false;
        
        UUID uuid = player.getUniqueId();
        QuestProgress progress = getQuestProgress(uuid, questId);
        
        if (progress.isCompleted() && !quest.isDaily() && !quest.isWeekly()) {
            return false;
        }
        
        progress.setCompleted(true);
        progress.setCompletedTime(System.currentTimeMillis());
        
        // Give rewards
        if (quest.getRewardMoney() > 0) {
            plugin.getEconomyManager().addBalance(uuid, quest.getRewardMoney());
            player.sendMessage("§a+$" + String.format("%.2f", quest.getRewardMoney()));
        }
        
        if (quest.getRewardXP() > 0) {
            player.giveExp(quest.getRewardXP());
            player.sendMessage("§a+" + quest.getRewardXP() + " XP");
        }
        
        for (ItemReward reward : quest.getItemRewards()) {
            if (Math.random() < reward.getChance()) {
                ItemStack item = new ItemStack(reward.getMaterial(), reward.getAmount());
                player.getInventory().addItem(item);
                player.sendMessage("§a+ " + reward.getAmount() + "x " + reward.getMaterial().name());
            }
        }
        
        savePlayerQuests();
        
        // Send completion message
        player.sendMessage("§6§l[QUEST COMPLETED]");
        player.sendMessage("§e" + quest.getName());
        player.sendMessage("§7" + quest.getDescription());
        
        return true;
    }
    
    /**
     * Gets all available quests.
     */
    public Map<String, Quest> getQuests() {
        return new HashMap<>(quests);
    }
    
    /**
     * Gets a quest by ID.
     */
    public Quest getQuest(String questId) {
        return quests.get(questId);
    }
    
    /**
     * Gets active quests for a player.
     */
    public List<Quest> getActiveQuests(Player player) {
        List<Quest> activeQuests = new ArrayList<>();
        UUID uuid = player.getUniqueId();
        Map<String, QuestProgress> playerQuestMap = playerQuests.get(uuid);
        
        if (playerQuestMap == null) return activeQuests;
        
        for (Map.Entry<String, QuestProgress> entry : playerQuestMap.entrySet()) {
            Quest quest = quests.get(entry.getKey());
            if (quest != null && !entry.getValue().isCompleted()) {
                activeQuests.add(quest);
            }
        }
        
        return activeQuests;
    }
    
    /**
     * Gets completed quests for a player.
     */
    public List<Quest> getCompletedQuests(Player player) {
        List<Quest> completedQuests = new ArrayList<>();
        UUID uuid = player.getUniqueId();
        Map<String, QuestProgress> playerQuestMap = playerQuests.get(uuid);
        
        if (playerQuestMap == null) return completedQuests;
        
        for (Map.Entry<String, QuestProgress> entry : playerQuestMap.entrySet()) {
            Quest quest = quests.get(entry.getKey());
            if (quest != null && entry.getValue().isCompleted()) {
                completedQuests.add(quest);
            }
        }
        
        return completedQuests;
    }
    
    /**
     * Schedules daily/weekly quest resets.
     */
    private void scheduleQuestResets() {
        // Run every hour to check for resets
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            for (Map.Entry<UUID, Map<String, QuestProgress>> playerEntry : playerQuests.entrySet()) {
                for (Map.Entry<String, QuestProgress> questEntry : playerEntry.getValue().entrySet()) {
                    String questId = questEntry.getKey();
                    Quest quest = quests.get(questId);
                    if (quest == null) continue;
                    
                    QuestProgress progress = questEntry.getValue();
                    long lastReset = progress.getLastResetTime();
                    long hoursSinceReset = (now - lastReset) / (1000 * 60 * 60);
                    
                    if (quest.isDaily() && hoursSinceReset >= 24) {
                        progress.setProgress(0);
                        progress.setCompleted(false);
                        progress.setLastResetTime(now);
                    } else if (quest.isWeekly() && hoursSinceReset >= 168) {
                        progress.setProgress(0);
                        progress.setCompleted(false);
                        progress.setLastResetTime(now);
                    }
                }
            }
            savePlayerQuests();
        }, 0L, 72000L); // Every hour (72000 ticks)
    }
    
    /**
     * Shutdown and save data.
     */
    public void shutdown() {
        savePlayerQuests();
    }
}

