package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class AchievementManager {
    private final Ecore plugin;
    private File achievementFile;
    private FileConfiguration achievementConfig;
    private File playerAchievementFile;
    private FileConfiguration playerAchievementConfig;
    private final Map<String, Achievement> achievements;

    public AchievementManager(Ecore plugin) {
        this.plugin = plugin;
        this.achievements = new HashMap<>();
        initializeAchievementConfig();
        initializePlayerAchievementConfig();
        loadAchievements();
    }

    private void initializeAchievementConfig() {
        achievementFile = new File(plugin.getDataFolder(), "achievements.yml");
        if (!achievementFile.exists()) {
            try {
                achievementFile.createNewFile();
                createDefaultAchievements();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create achievements.yml", e);
            }
        }
        achievementConfig = YamlConfiguration.loadConfiguration(achievementFile);
    }

    private void initializePlayerAchievementConfig() {
        playerAchievementFile = new File(plugin.getDataFolder(), "player-achievements.yml");
        if (!playerAchievementFile.exists()) {
            try {
                playerAchievementFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create player-achievements.yml", e);
            }
        }
        playerAchievementConfig = YamlConfiguration.loadConfiguration(playerAchievementFile);
    }

    private void createDefaultAchievements() {
        // Create some default achievements
        achievementConfig.set("achievements.first-join.name", "First Steps");
        achievementConfig.set("achievements.first-join.description", "Join the server for the first time");
        achievementConfig.set("achievements.first-join.reward-money", 10.0);
        achievementConfig.set("achievements.first-join.reward-xp", 0);

        achievementConfig.set("achievements.blocks-100.name", "Block Breaker");
        achievementConfig.set("achievements.blocks-100.description", "Break 100 blocks");
        achievementConfig.set("achievements.blocks-100.requirement.stat", "blocks-broken");
        achievementConfig.set("achievements.blocks-100.requirement.value", 100);
        achievementConfig.set("achievements.blocks-100.reward-money", 50.0);

        achievementConfig.set("achievements.blocks-1000.name", "Master Miner");
        achievementConfig.set("achievements.blocks-1000.description", "Break 1,000 blocks");
        achievementConfig.set("achievements.blocks-1000.requirement.stat", "blocks-broken");
        achievementConfig.set("achievements.blocks-1000.requirement.value", 1000);
        achievementConfig.set("achievements.blocks-1000.reward-money", 200.0);

        achievementConfig.set("achievements.kills-10.name", "Warrior");
        achievementConfig.set("achievements.kills-10.description", "Kill 10 players");
        achievementConfig.set("achievements.kills-10.requirement.stat", "kills");
        achievementConfig.set("achievements.kills-10.requirement.value", 10);
        achievementConfig.set("achievements.kills-10.reward-money", 100.0);

        achievementConfig.set("achievements.distance-1000.name", "Explorer");
        achievementConfig.set("achievements.distance-1000.description", "Travel 1,000 blocks");
        achievementConfig.set("achievements.distance-1000.requirement.stat", "distance-traveled");
        achievementConfig.set("achievements.distance-1000.requirement.value", 1000.0);
        achievementConfig.set("achievements.distance-1000.reward-money", 75.0);

        try {
            achievementConfig.save(achievementFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save default achievements: " + e.getMessage());
        }
    }

    private void loadAchievements() {
        if (achievementConfig.contains("achievements")) {
            for (String key : achievementConfig.getConfigurationSection("achievements").getKeys(false)) {
                String path = "achievements." + key;
                String name = achievementConfig.getString(path + ".name", key);
                String description = achievementConfig.getString(path + ".description", "");
                double rewardMoney = achievementConfig.getDouble(path + ".reward-money", 0.0);
                int rewardXP = achievementConfig.getInt(path + ".reward-xp", 0);
                
                String reqStat = achievementConfig.getString(path + ".requirement.stat");
                Object reqValue = achievementConfig.get(path + ".requirement.value");
                
                Achievement achievement = new Achievement(key, name, description, reqStat, reqValue, rewardMoney, rewardXP);
                achievements.put(key, achievement);
            }
        }
    }

    // Check if player has unlocked an achievement
    public boolean hasAchievement(Player player, String achievementId) {
        UUID uuid = player.getUniqueId();
        String path = "players." + uuid.toString() + ".achievements";
        List<String> playerAchievements = playerAchievementConfig.getStringList(path);
        return playerAchievements.contains(achievementId);
    }

    // Get all achievements unlocked by a player
    public List<String> getPlayerAchievements(Player player) {
        UUID uuid = player.getUniqueId();
        String path = "players." + uuid.toString() + ".achievements";
        return playerAchievementConfig.getStringList(path);
    }

    // Unlock an achievement for a player
    public boolean unlockAchievement(Player player, String achievementId) {
        if (hasAchievement(player, achievementId)) {
            return false; // Already unlocked
        }

        Achievement achievement = achievements.get(achievementId);
        if (achievement == null) {
            return false; // Achievement doesn't exist
        }

        UUID uuid = player.getUniqueId();
        String path = "players." + uuid.toString() + ".achievements";
        List<String> playerAchievements = new ArrayList<>(playerAchievementConfig.getStringList(path));
        playerAchievements.add(achievementId);
        playerAchievementConfig.set(path, playerAchievements);
        savePlayerAchievements();

        // Give rewards
        if (achievement.getRewardMoney() > 0) {
            plugin.getEconomyManager().addBalance(uuid, achievement.getRewardMoney());
        }
        if (achievement.getRewardXP() > 0) {
            player.giveExp(achievement.getRewardXP());
        }

        // Notify player
        player.sendMessage("§6§l[ACHIEVEMENT UNLOCKED]");
        player.sendMessage("§e" + achievement.getName());
        player.sendMessage("§7" + achievement.getDescription());
        if (achievement.getRewardMoney() > 0) {
            player.sendMessage("§aReward: " + String.format("%.2f", achievement.getRewardMoney()) + " coins");
        }
        if (achievement.getRewardXP() > 0) {
            player.sendMessage("§aReward: " + achievement.getRewardXP() + " XP");
        }

        // Broadcast to server
        Bukkit.broadcastMessage("§6" + player.getName() + " unlocked achievement: §e" + achievement.getName() + "§6!");

        // Send Discord notification
        if (plugin.getDiscordManager() != null) {
            plugin.getDiscordManager().sendAchievementNotification(player, achievement.getName(), achievement.getDescription());
        }

        return true;
    }

    // Check achievements for a player (called periodically or on stat updates)
    public void checkAchievements(Player player) {
        for (Achievement achievement : achievements.values()) {
            if (hasAchievement(player, achievement.getId())) {
                continue; // Already unlocked
            }

            // Check if requirement is met
            if (achievement.getRequirementStat() == null) {
                // Special achievement (like first-join)
                continue;
            }

            boolean unlocked = false;
            String reqStat = achievement.getRequirementStat();
            Object reqValue = achievement.getRequirementValue();

            if (reqValue instanceof Integer) {
                int playerValue = plugin.getStatisticsManager().getStatistic(player, reqStat);
                if (playerValue >= (Integer) reqValue) {
                    unlocked = true;
                }
            } else if (reqValue instanceof Double) {
                double playerValue = plugin.getStatisticsManager().getStatisticDouble(player, reqStat);
                if (playerValue >= (Double) reqValue) {
                    unlocked = true;
                }
            }

            if (unlocked) {
                unlockAchievement(player, achievement.getId());
            }
        }
    }

    // Get achievement by ID
    public Achievement getAchievement(String achievementId) {
        return achievements.get(achievementId);
    }

    // Get all achievements
    public Map<String, Achievement> getAllAchievements() {
        return achievements;
    }

    private void savePlayerAchievements() {
        try {
            playerAchievementConfig.save(playerAchievementFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save player-achievements.yml: " + e.getMessage());
        }
    }

    // Achievement class
    public static class Achievement {
        private final String id;
        private final String name;
        private final String description;
        private final String requirementStat;
        private final Object requirementValue;
        private final double rewardMoney;
        private final int rewardXP;

        public Achievement(String id, String name, String description, String requirementStat, 
                         Object requirementValue, double rewardMoney, int rewardXP) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.requirementStat = requirementStat;
            this.requirementValue = requirementValue;
            this.rewardMoney = rewardMoney;
            this.rewardXP = rewardXP;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getRequirementStat() { return requirementStat; }
        public Object getRequirementValue() { return requirementValue; }
        public double getRewardMoney() { return rewardMoney; }
        public int getRewardXP() { return rewardXP; }
    }
}

