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
 * Manages the jobs system with multiple job types, levels, experience, and rewards.
 */
public class JobManager {
    private final Ecore plugin;
    private File jobsFile;
    private FileConfiguration jobsConfig;
    private File playerJobsFile;
    private FileConfiguration playerJobsConfig;
    
    // Job data structures
    private final Map<String, JobType> jobTypes; // Job ID -> JobType
    private final Map<UUID, PlayerJobData> playerJobs; // Player UUID -> Job data
    
    /**
     * Represents a job type configuration.
     */
    public static class JobType {
        private final String id;
        private final String name;
        private final String description;
        private final Material icon;
        private final List<String> lore;
        private final Map<String, JobAction> actions; // Action ID -> JobAction
        
        public JobType(String id, String name, String description, Material icon, List<String> lore, Map<String, JobAction> actions) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.lore = lore;
            this.actions = actions;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Material getIcon() { return icon; }
        public List<String> getLore() { return lore; }
        public Map<String, JobAction> getActions() { return actions; }
    }
    
    /**
     * Represents a job action (what gives experience).
     */
    public static class JobAction {
        private final String id;
        private final String name;
        private final double baseExp;
        private final double expMultiplier; // Multiplier per level
        private final double baseMoney;
        private final double moneyMultiplier; // Multiplier per level
        private final List<ItemReward> itemRewards;
        private final Map<Material, Double> materialExp; // Material -> exp multiplier
        private final Map<EntityType, Double> entityExp; // Entity -> exp multiplier
        
        public JobAction(String id, String name, double baseExp, double expMultiplier, 
                       double baseMoney, double moneyMultiplier, List<ItemReward> itemRewards,
                       Map<Material, Double> materialExp, Map<EntityType, Double> entityExp) {
            this.id = id;
            this.name = name;
            this.baseExp = baseExp;
            this.expMultiplier = expMultiplier;
            this.baseMoney = baseMoney;
            this.moneyMultiplier = moneyMultiplier;
            this.itemRewards = itemRewards;
            this.materialExp = materialExp;
            this.entityExp = entityExp;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public double getBaseExp() { return baseExp; }
        public double getExpMultiplier() { return expMultiplier; }
        public double getBaseMoney() { return baseMoney; }
        public double getMoneyMultiplier() { return moneyMultiplier; }
        public List<ItemReward> getItemRewards() { return itemRewards; }
        public Map<Material, Double> getMaterialExp() { return materialExp; }
        public Map<EntityType, Double> getEntityExp() { return entityExp; }
    }
    
    /**
     * Represents an item reward.
     */
    public static class ItemReward {
        private final Material material;
        private final int amount;
        private final int level;
        private final double chance; // 0.0 to 1.0
        
        public ItemReward(Material material, int amount, int level, double chance) {
            this.material = material;
            this.amount = amount;
            this.level = level;
            this.chance = chance;
        }
        
        public Material getMaterial() { return material; }
        public int getAmount() { return amount; }
        public int getLevel() { return level; }
        public double getChance() { return chance; }
    }
    
    /**
     * Represents a player's job data.
     */
    public static class PlayerJobData {
        private String currentJob;
        private int level;
        private double experience;
        private double totalExperience; // Total exp earned
        private double totalMoneyEarned;
        private int totalActions; // Total actions performed
        
        public PlayerJobData() {
            this.currentJob = null;
            this.level = 1;
            this.experience = 0.0;
            this.totalExperience = 0.0;
            this.totalMoneyEarned = 0.0;
            this.totalActions = 0;
        }
        
        public String getCurrentJob() { return currentJob; }
        public void setCurrentJob(String job) { this.currentJob = job; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public double getExperience() { return experience; }
        public void setExperience(double exp) { this.experience = exp; }
        public double getTotalExperience() { return totalExperience; }
        public void addTotalExperience(double exp) { this.totalExperience += exp; }
        public double getTotalMoneyEarned() { return totalMoneyEarned; }
        public void addTotalMoneyEarned(double money) { this.totalMoneyEarned += money; }
        public int getTotalActions() { return totalActions; }
        public void incrementTotalActions() { this.totalActions++; }
        public void setTotalActions(int actions) { this.totalActions = actions; }
        
        public double getExpForNextLevel() {
            // Exponential leveling: 100 * level^1.5
            return 100.0 * Math.pow(level, 1.5);
        }
    }
    
    public JobManager(Ecore plugin) {
        this.plugin = plugin;
        this.jobTypes = new HashMap<>();
        this.playerJobs = new HashMap<>();
        initializeConfigs();
        loadJobTypes();
        loadPlayerJobs();
    }
    
    private void initializeConfigs() {
        // Initialize jobs.yml
        jobsFile = new File(plugin.getDataFolder(), "jobs.yml");
        if (!jobsFile.exists()) {
            plugin.saveResource("jobs.yml", false);
        }
        jobsConfig = YamlConfiguration.loadConfiguration(jobsFile);
        
        // Initialize player-jobs.yml
        playerJobsFile = new File(plugin.getDataFolder(), "player-jobs.yml");
        if (!playerJobsFile.exists()) {
            try {
                playerJobsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create player-jobs.yml", e);
            }
        }
        playerJobsConfig = YamlConfiguration.loadConfiguration(playerJobsFile);
    }
    
    private void loadJobTypes() {
        if (jobsConfig.getConfigurationSection("jobs") == null) {
            plugin.getLogger().warning("No jobs found in jobs.yml! Creating default jobs...");
            createDefaultJobs();
            return;
        }
        
        for (String jobId : jobsConfig.getConfigurationSection("jobs").getKeys(false)) {
            String path = "jobs." + jobId;
            String name = jobsConfig.getString(path + ".name", jobId);
            String description = jobsConfig.getString(path + ".description", "");
            Material icon;
            try {
                icon = Material.valueOf(jobsConfig.getString(path + ".icon", "DIAMOND_PICKAXE"));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid icon material for job '" + jobId + "'. Using DIAMOND_PICKAXE as default.");
                icon = Material.DIAMOND_PICKAXE;
            }
            List<String> lore = jobsConfig.getStringList(path + ".lore");
            
            Map<String, JobAction> actions = new HashMap<>();
            if (jobsConfig.getConfigurationSection(path + ".actions") != null) {
                for (String actionId : jobsConfig.getConfigurationSection(path + ".actions").getKeys(false)) {
                    String actionPath = path + ".actions." + actionId;
                    String actionName = jobsConfig.getString(actionPath + ".name", actionId);
                    double baseExp = jobsConfig.getDouble(actionPath + ".base-exp", 1.0);
                    double expMultiplier = jobsConfig.getDouble(actionPath + ".exp-multiplier", 0.1);
                    double baseMoney = jobsConfig.getDouble(actionPath + ".base-money", 0.0);
                    double moneyMultiplier = jobsConfig.getDouble(actionPath + ".money-multiplier", 0.05);
                    
                    List<ItemReward> itemRewards = new ArrayList<>();
                    if (jobsConfig.getConfigurationSection(actionPath + ".item-rewards") != null) {
                        for (String rewardKey : jobsConfig.getConfigurationSection(actionPath + ".item-rewards").getKeys(false)) {
                            String rewardPath = actionPath + ".item-rewards." + rewardKey;
                            try {
                                Material mat = Material.valueOf(jobsConfig.getString(rewardPath + ".material", "STONE"));
                                int amount = jobsConfig.getInt(rewardPath + ".amount", 1);
                                int level = jobsConfig.getInt(rewardPath + ".level", 1);
                                double chance = jobsConfig.getDouble(rewardPath + ".chance", 1.0);
                                itemRewards.add(new ItemReward(mat, amount, level, chance));
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Invalid material in item reward '" + rewardKey + "' for job '" + jobId + "' action '" + actionId + "'. Skipping...");
                            }
                        }
                    }
                    
                    Map<Material, Double> materialExp = new HashMap<>();
                    if (jobsConfig.getConfigurationSection(actionPath + ".materials") != null) {
                        for (String matKey : jobsConfig.getConfigurationSection(actionPath + ".materials").getKeys(false)) {
                            try {
                                Material mat = Material.valueOf(matKey);
                                double multiplier = jobsConfig.getDouble(actionPath + ".materials." + matKey, 1.0);
                                materialExp.put(mat, multiplier);
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Invalid material '" + matKey + "' in job '" + jobId + "' action '" + actionId + "'. Skipping...");
                            }
                        }
                    }
                    
                    Map<EntityType, Double> entityExp = new HashMap<>();
                    if (jobsConfig.getConfigurationSection(actionPath + ".entities") != null) {
                        for (String entityKey : jobsConfig.getConfigurationSection(actionPath + ".entities").getKeys(false)) {
                            try {
                                EntityType entity = EntityType.valueOf(entityKey);
                                double multiplier = jobsConfig.getDouble(actionPath + ".entities." + entityKey, 1.0);
                                entityExp.put(entity, multiplier);
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Invalid entity type '" + entityKey + "' in job '" + jobId + "' action '" + actionId + "'. Skipping...");
                            }
                        }
                    }
                    
                    actions.put(actionId, new JobAction(actionId, actionName, baseExp, expMultiplier, 
                                                       baseMoney, moneyMultiplier, itemRewards, materialExp, entityExp));
                }
            }
            
            jobTypes.put(jobId, new JobType(jobId, name, description, icon, lore, actions));
        }
    }
    
    private void createDefaultJobs() {
        // This will be handled by the default jobs.yml file
        plugin.getLogger().info("Please configure jobs in jobs.yml");
    }
    
    private void loadPlayerJobs() {
        if (playerJobsConfig.getConfigurationSection("players") == null) return;
        
        for (String uuidStr : playerJobsConfig.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            String path = "players." + uuidStr;
            
            PlayerJobData data = new PlayerJobData();
            data.setCurrentJob(playerJobsConfig.getString(path + ".current-job"));
            data.setLevel(playerJobsConfig.getInt(path + ".level", 1));
            data.setExperience(playerJobsConfig.getDouble(path + ".experience", 0.0));
            data.addTotalExperience(playerJobsConfig.getDouble(path + ".total-experience", 0.0) - data.getExperience());
            data.addTotalMoneyEarned(playerJobsConfig.getDouble(path + ".total-money-earned", 0.0));
            data.setTotalActions(playerJobsConfig.getInt(path + ".total-actions", 0));
            
            playerJobs.put(uuid, data);
        }
    }
    
    public void savePlayerJobs() {
        try {
            playerJobsConfig.set("players", null);
            for (Map.Entry<UUID, PlayerJobData> entry : playerJobs.entrySet()) {
                String path = "players." + entry.getKey().toString();
                PlayerJobData data = entry.getValue();
                playerJobsConfig.set(path + ".current-job", data.getCurrentJob());
                playerJobsConfig.set(path + ".level", data.getLevel());
                playerJobsConfig.set(path + ".experience", data.getExperience());
                playerJobsConfig.set(path + ".total-experience", data.getTotalExperience() + data.getExperience());
                playerJobsConfig.set(path + ".total-money-earned", data.getTotalMoneyEarned());
                playerJobsConfig.set(path + ".total-actions", data.getTotalActions());
            }
            playerJobsConfig.save(playerJobsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save player-jobs.yml", e);
        }
    }
    
    /**
     * Gets or creates player job data.
     */
    public PlayerJobData getPlayerJobData(UUID uuid) {
        return playerJobs.computeIfAbsent(uuid, k -> new PlayerJobData());
    }
    
    /**
     * Joins a job.
     */
    public boolean joinJob(Player player, String jobId) {
        if (!jobTypes.containsKey(jobId)) {
            return false;
        }
        
        PlayerJobData data = getPlayerJobData(player.getUniqueId());
        if (data.getCurrentJob() != null && data.getCurrentJob().equals(jobId)) {
            return false; // Already in this job
        }
        
        data.setCurrentJob(jobId);
        data.setLevel(1);
        data.setExperience(0.0);
        savePlayerJobs();
        
        JobType job = jobTypes.get(jobId);
        player.sendMessage("§aYou joined the " + job.getName() + " job!");
        return true;
    }
    
    /**
     * Leaves current job.
     */
    public boolean leaveJob(Player player) {
        PlayerJobData data = getPlayerJobData(player.getUniqueId());
        if (data.getCurrentJob() == null) {
            return false;
        }
        
        String jobName = jobTypes.get(data.getCurrentJob()).getName();
        data.setCurrentJob(null);
        savePlayerJobs();
        
        player.sendMessage("§cYou left the " + jobName + " job.");
        return true;
    }
    
    /**
     * Processes a job action and gives rewards.
     */
    public void processJobAction(Player player, String actionId, Material material, EntityType entity) {
        PlayerJobData data = getPlayerJobData(player.getUniqueId());
        if (data.getCurrentJob() == null) return;
        
        JobType job = jobTypes.get(data.getCurrentJob());
        JobAction action = job.getActions().get(actionId);
        if (action == null) return;
        
        // Calculate experience with multipliers
        double exp = action.getBaseExp();
        double materialMultiplier = 1.0;
        double entityMultiplier = 1.0;
        
        if (material != null && action.getMaterialExp().containsKey(material)) {
            materialMultiplier = action.getMaterialExp().get(material);
        }
        if (entity != null && action.getEntityExp().containsKey(entity)) {
            entityMultiplier = action.getEntityExp().get(entity);
        }
        
        exp *= materialMultiplier * entityMultiplier;
        exp += action.getExpMultiplier() * data.getLevel();
        
        // Calculate money
        double money = action.getBaseMoney() + (action.getMoneyMultiplier() * data.getLevel());
        
        // Add experience
        data.setExperience(data.getExperience() + exp);
        data.addTotalExperience(exp);
        data.incrementTotalActions();
        
        // Check for level up
        boolean leveledUp = false;
        while (data.getExperience() >= data.getExpForNextLevel()) {
            data.setExperience(data.getExperience() - data.getExpForNextLevel());
            data.setLevel(data.getLevel() + 1);
            leveledUp = true;
        }
        
        // Give money
        if (money > 0) {
            plugin.getEconomyManager().addBalance(player.getUniqueId(), money);
            data.addTotalMoneyEarned(money);
        }
        
        // Give item rewards
        for (ItemReward reward : action.getItemRewards()) {
            if (data.getLevel() >= reward.getLevel() && Math.random() < reward.getChance()) {
                ItemStack item = new ItemStack(reward.getMaterial(), reward.getAmount());
                player.getInventory().addItem(item);
            }
        }
        
        savePlayerJobs();
        
        // Send messages
        if (leveledUp) {
            player.sendMessage("§6§lLEVEL UP! §r§eYou reached level " + data.getLevel() + " in " + job.getName() + "!");
        }
    }
    
    /**
     * Gets all available job types.
     */
    public Map<String, JobType> getJobTypes() {
        return new HashMap<>(jobTypes);
    }
    
    /**
     * Gets a job type by ID.
     */
    public JobType getJobType(String jobId) {
        return jobTypes.get(jobId);
    }
    
    /**
     * Gets job leaderboard (top players by level).
     */
    public List<Map.Entry<UUID, PlayerJobData>> getLeaderboard(String jobId, int limit) {
        List<Map.Entry<UUID, PlayerJobData>> entries = new ArrayList<>();
        for (Map.Entry<UUID, PlayerJobData> entry : playerJobs.entrySet()) {
            if (entry.getValue().getCurrentJob() != null && entry.getValue().getCurrentJob().equals(jobId)) {
                entries.add(entry);
            }
        }
        
        entries.sort((a, b) -> {
            int levelCompare = Integer.compare(b.getValue().getLevel(), a.getValue().getLevel());
            if (levelCompare != 0) return levelCompare;
            return Double.compare(b.getValue().getTotalExperience(), a.getValue().getTotalExperience());
        });
        
        return entries.subList(0, Math.min(limit, entries.size()));
    }
    
    /**
     * Shutdown and save data.
     */
    public void shutdown() {
        savePlayerJobs();
    }
}

