package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Skill System Manager
 * Complements jobs system with multiple skills, levels, abilities, bonuses, prestige, etc.
 */
public class SkillManager {
    private final Ecore plugin;
    private File skillsFile;
    private FileConfiguration skillsConfig;
    private File playerSkillsFile;
    private FileConfiguration playerSkillsConfig;
    private final Map<String, SkillType> skillTypes;
    private final Map<UUID, Map<String, PlayerSkillData>> playerSkills;
    
    public SkillManager(Ecore plugin) {
        this.plugin = plugin;
        this.skillTypes = new HashMap<>();
        this.playerSkills = new HashMap<>();
        initializeConfigs();
        loadSkillTypes();
        loadPlayerSkills();
    }
    
    private void initializeConfigs() {
        skillsFile = new File(plugin.getDataFolder(), "skills.yml");
        if (!skillsFile.exists()) {
            plugin.saveResource("skills.yml", false);
        }
        skillsConfig = YamlConfiguration.loadConfiguration(skillsFile);
        
        playerSkillsFile = new File(plugin.getDataFolder(), "player-skills.yml");
        if (!playerSkillsFile.exists()) {
            try {
                playerSkillsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create player-skills.yml", e);
            }
        }
        playerSkillsConfig = YamlConfiguration.loadConfiguration(playerSkillsFile);
    }
    
    private void loadSkillTypes() {
        if (skillsConfig.contains("skills")) {
            for (String skillId : skillsConfig.getConfigurationSection("skills").getKeys(false)) {
                String path = "skills." + skillId;
                String name = skillsConfig.getString(path + ".name");
                String description = skillsConfig.getString(path + ".description", "");
                Material icon = Material.valueOf(skillsConfig.getString(path + ".icon", "DIAMOND"));
                int maxLevel = skillsConfig.getInt(path + ".max-level", 100);
                double baseExp = skillsConfig.getDouble(path + ".base-exp", 10.0);
                double expMultiplier = skillsConfig.getDouble(path + ".exp-multiplier", 1.5);
                
                // Load abilities
                Map<Integer, SkillAbility> abilities = new HashMap<>();
                if (skillsConfig.contains(path + ".abilities")) {
                    for (String levelStr : skillsConfig.getConfigurationSection(path + ".abilities").getKeys(false)) {
                        int level = Integer.parseInt(levelStr);
                        String abilityName = skillsConfig.getString(path + ".abilities." + levelStr + ".name");
                        String abilityDesc = skillsConfig.getString(path + ".abilities." + levelStr + ".description", "");
                        abilities.put(level, new SkillAbility(abilityName, abilityDesc, level));
                    }
                }
                
                // Load bonuses
                Map<String, Double> bonuses = new HashMap<>();
                if (skillsConfig.contains(path + ".bonuses")) {
                    for (String bonusType : skillsConfig.getConfigurationSection(path + ".bonuses").getKeys(false)) {
                        bonuses.put(bonusType, skillsConfig.getDouble(path + ".bonuses." + bonusType));
                    }
                }
                
                skillTypes.put(skillId, new SkillType(skillId, name, description, icon, maxLevel, baseExp, expMultiplier, abilities, bonuses));
            }
        } else {
            createDefaultSkills();
        }
    }
    
    private void createDefaultSkills() {
        // Create default skills if none exist
        skillsConfig.set("skills.mining.name", "Mining");
        skillsConfig.set("skills.mining.description", "Mine blocks to level up!");
        skillsConfig.set("skills.mining.icon", "DIAMOND_PICKAXE");
        skillsConfig.set("skills.mining.max-level", 100);
        skillsConfig.set("skills.mining.base-exp", 10.0);
        skillsConfig.set("skills.mining.exp-multiplier", 1.5);
        
        skillsConfig.set("skills.combat.name", "Combat");
        skillsConfig.set("skills.combat.description", "Fight mobs to level up!");
        skillsConfig.set("skills.combat.icon", "IRON_SWORD");
        skillsConfig.set("skills.combat.max-level", 100);
        skillsConfig.set("skills.combat.base-exp", 15.0);
        skillsConfig.set("skills.combat.exp-multiplier", 1.5);
        
        skillsConfig.set("skills.farming.name", "Farming");
        skillsConfig.set("skills.farming.description", "Farm crops to level up!");
        skillsConfig.set("skills.farming.icon", "GOLDEN_HOE");
        skillsConfig.set("skills.farming.max-level", 100);
        skillsConfig.set("skills.farming.base-exp", 8.0);
        skillsConfig.set("skills.farming.exp-multiplier", 1.5);
        
        try {
            skillsConfig.save(skillsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save default skills", e);
        }
        loadSkillTypes();
    }
    
    private void loadPlayerSkills() {
        if (playerSkillsConfig.contains("players")) {
            for (String uuidStr : playerSkillsConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    Map<String, PlayerSkillData> skills = new HashMap<>();
                    
                    if (playerSkillsConfig.contains("players." + uuidStr + ".skills")) {
                        for (String skillId : playerSkillsConfig.getConfigurationSection("players." + uuidStr + ".skills").getKeys(false)) {
                            String path = "players." + uuidStr + ".skills." + skillId;
                            int level = playerSkillsConfig.getInt(path + ".level", 1);
                            double experience = playerSkillsConfig.getDouble(path + ".experience", 0.0);
                            int prestige = playerSkillsConfig.getInt(path + ".prestige", 0);
                            skills.put(skillId, new PlayerSkillData(skillId, level, experience, prestige));
                        }
                    }
                    
                    playerSkills.put(uuid, skills);
                } catch (IllegalArgumentException e) {
                    // Skip invalid UUIDs
                }
            }
        }
    }
    
    /**
     * Add experience to a skill
     */
    public void addExperience(Player player, String skillId, double experience) {
        UUID uuid = player.getUniqueId();
        SkillType skillType = skillTypes.get(skillId);
        if (skillType == null) return;
        
        Map<String, PlayerSkillData> skills = playerSkills.computeIfAbsent(uuid, k -> new HashMap<>());
        PlayerSkillData skillData = skills.computeIfAbsent(skillId, k -> new PlayerSkillData(skillId, 1, 0.0, 0));
        
        skillData.addExperience(experience);
        
        // Check for level up
        while (skillData.getLevel() < skillType.getMaxLevel() && 
               skillData.getExperience() >= skillData.getExpForNextLevel()) {
            skillData.levelUp();
            
            // Check for ability unlock
            SkillAbility ability = skillType.getAbility(skillData.getLevel());
            if (ability != null) {
                player.sendMessage(org.bukkit.ChatColor.GREEN + "You unlocked the ability: " + ability.getName() + "!");
            }
            
            player.sendMessage(org.bukkit.ChatColor.GREEN + skillType.getName() + " leveled up to " + skillData.getLevel() + "!");
        }
        
        // Check for prestige
        if (skillData.getLevel() >= skillType.getMaxLevel() && skillData.getExperience() >= skillData.getExpForNextLevel()) {
            if (skillData.getPrestige() < 10) { // Max 10 prestiges
                skillData.prestige();
                player.sendMessage(org.bukkit.ChatColor.GOLD + skillType.getName() + " prestiged! Prestige: " + skillData.getPrestige());
            }
        }
        
        savePlayerSkill(uuid, skillId, skillData);
    }
    
    /**
     * Get skill bonus (e.g., mining speed, combat damage)
     */
    public double getSkillBonus(Player player, String skillId, String bonusType) {
        PlayerSkillData skillData = getPlayerSkill(player, skillId);
        if (skillData == null) return 0.0;
        
        SkillType skillType = skillTypes.get(skillId);
        if (skillType == null) return 0.0;
        
        double baseBonus = skillType.getBonus(bonusType);
        if (baseBonus == 0.0) return 0.0;
        
        // Bonus scales with level
        return baseBonus * skillData.getLevel() * (1 + skillData.getPrestige() * 0.1);
    }
    
    /**
     * Get player skill data
     */
    public PlayerSkillData getPlayerSkill(Player player, String skillId) {
        Map<String, PlayerSkillData> skills = playerSkills.get(player.getUniqueId());
        return skills != null ? skills.get(skillId) : null;
    }
    
    /**
     * Get all player skills
     */
    public Map<String, PlayerSkillData> getPlayerSkills(Player player) {
        return playerSkills.getOrDefault(player.getUniqueId(), new HashMap<>());
    }
    
    /**
     * Get skill leaderboard
     */
    public List<Map.Entry<UUID, Integer>> getSkillLeaderboard(String skillId, int top) {
        List<Map.Entry<UUID, Integer>> leaderboard = new ArrayList<>();
        
        for (Map.Entry<UUID, Map<String, PlayerSkillData>> entry : playerSkills.entrySet()) {
            PlayerSkillData skillData = entry.getValue().get(skillId);
            if (skillData != null) {
                leaderboard.add(new AbstractMap.SimpleEntry<>(entry.getKey(), skillData.getLevel()));
            }
        }
        
        leaderboard.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return leaderboard.subList(0, Math.min(top, leaderboard.size()));
    }
    
    private void savePlayerSkill(UUID uuid, String skillId, PlayerSkillData skillData) {
        String path = "players." + uuid.toString() + ".skills." + skillId;
        playerSkillsConfig.set(path + ".level", skillData.getLevel());
        playerSkillsConfig.set(path + ".experience", skillData.getExperience());
        playerSkillsConfig.set(path + ".prestige", skillData.getPrestige());
        
        try {
            playerSkillsConfig.save(playerSkillsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player skills", e);
        }
    }
    
    public Collection<SkillType> getSkillTypes() {
        return skillTypes.values();
    }
    
    public SkillType getSkillType(String skillId) {
        return skillTypes.get(skillId);
    }
    
    /**
     * Skill Type class
     */
    public static class SkillType {
        private String id;
        private String name;
        private String description;
        private Material icon;
        private int maxLevel;
        private double baseExp;
        private double expMultiplier;
        private Map<Integer, SkillAbility> abilities;
        private Map<String, Double> bonuses;
        
        public SkillType(String id, String name, String description, Material icon, int maxLevel, 
                        double baseExp, double expMultiplier, Map<Integer, SkillAbility> abilities, 
                        Map<String, Double> bonuses) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.maxLevel = maxLevel;
            this.baseExp = baseExp;
            this.expMultiplier = expMultiplier;
            this.abilities = abilities;
            this.bonuses = bonuses;
        }
        
        public SkillAbility getAbility(int level) {
            return abilities.get(level);
        }
        
        public double getBonus(String bonusType) {
            return bonuses.getOrDefault(bonusType, 0.0);
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Material getIcon() { return icon; }
        public int getMaxLevel() { return maxLevel; }
        public double getBaseExp() { return baseExp; }
        public double getExpMultiplier() { return expMultiplier; }
    }
    
    /**
     * Skill Ability class
     */
    public static class SkillAbility {
        private String name;
        private String description;
        private int unlockLevel;
        
        public SkillAbility(String name, String description, int unlockLevel) {
            this.name = name;
            this.description = description;
            this.unlockLevel = unlockLevel;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getUnlockLevel() { return unlockLevel; }
    }
    
    /**
     * Player Skill Data class
     */
    public static class PlayerSkillData {
        private String skillId;
        private int level;
        private double experience;
        private int prestige;
        
        public PlayerSkillData(String skillId, int level, double experience, int prestige) {
            this.skillId = skillId;
            this.level = level;
            this.experience = experience;
            this.prestige = prestige;
        }
        
        public double getExpForNextLevel() {
            return 100.0 * Math.pow(level, 1.5) * (1 + prestige * 0.5);
        }
        
        public void addExperience(double exp) {
            this.experience += exp;
        }
        
        public void levelUp() {
            double expNeeded = getExpForNextLevel();
            this.experience -= expNeeded;
            this.level++;
        }
        
        public void prestige() {
            this.level = 1;
            this.experience = 0.0;
            this.prestige++;
        }
        
        public String getSkillId() { return skillId; }
        public int getLevel() { return level; }
        public double getExperience() { return experience; }
        public int getPrestige() { return prestige; }
    }
}

