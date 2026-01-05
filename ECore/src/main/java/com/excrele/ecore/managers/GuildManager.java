package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Guild/Clan System Manager
 * Extends party system with permanent guilds, ranks, banks, warps, homes, quests, etc.
 */
public class GuildManager {
    private final Ecore plugin;
    private File guildsFile;
    private FileConfiguration guildsConfig;
    private final Map<String, Guild> guilds; // Guild ID -> Guild
    private final Map<UUID, String> playerGuilds; // Player UUID -> Guild ID
    private final Map<String, List<UUID>> guildApplications; // Guild ID -> List of applicant UUIDs
    
    public GuildManager(Ecore plugin) {
        this.plugin = plugin;
        this.guilds = new HashMap<>();
        this.playerGuilds = new HashMap<>();
        this.guildApplications = new HashMap<>();
        initializeConfig();
        loadGuilds();
        startAutoTasks();
    }
    
    private void initializeConfig() {
        guildsFile = new File(plugin.getDataFolder(), "guilds.yml");
        if (!guildsFile.exists()) {
            try {
                guildsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create guilds.yml", e);
            }
        }
        guildsConfig = YamlConfiguration.loadConfiguration(guildsFile);
    }
    
    private void loadGuilds() {
        if (guildsConfig.contains("guilds")) {
            for (String guildId : guildsConfig.getConfigurationSection("guilds").getKeys(false)) {
                String path = "guilds." + guildId;
                String name = guildsConfig.getString(path + ".name");
                UUID leader = UUID.fromString(guildsConfig.getString(path + ".leader"));
                String tag = guildsConfig.getString(path + ".tag", "");
                String description = guildsConfig.getString(path + ".description", "");
                int level = guildsConfig.getInt(path + ".level", 1);
                double experience = guildsConfig.getDouble(path + ".experience", 0.0);
                long created = guildsConfig.getLong(path + ".created", System.currentTimeMillis());
                
                Guild guild = new Guild(guildId, name, leader, tag, description, level, experience, created);
                
                // Load members and ranks
                if (guildsConfig.contains(path + ".members")) {
                    for (String uuidStr : guildsConfig.getConfigurationSection(path + ".members").getKeys(false)) {
                        UUID memberUuid = UUID.fromString(uuidStr);
                        String rank = guildsConfig.getString(path + ".members." + uuidStr + ".rank", "member");
                        String memberName = guildsConfig.getString(path + ".members." + uuidStr + ".name", "Unknown");
                        guild.addMember(memberUuid, memberName, rank);
                        playerGuilds.put(memberUuid, guildId);
                    }
                }
                
                // Load guild bank balance
                double bankBalance = guildsConfig.getDouble(path + ".bank-balance", 0.0);
                guild.setBankBalance(bankBalance);
                
                // Load guild warps
                if (guildsConfig.contains(path + ".warps")) {
                    for (String warpName : guildsConfig.getStringList(path + ".warps")) {
                        guild.addWarp(warpName);
                    }
                }
                
                // Load guild homes
                if (guildsConfig.contains(path + ".homes")) {
                    for (String homeName : guildsConfig.getStringList(path + ".homes")) {
                        guild.addHome(homeName);
                    }
                }
                
                // Load alliances
                if (guildsConfig.contains(path + ".alliances")) {
                    for (String allyGuildId : guildsConfig.getStringList(path + ".alliances")) {
                        guild.addAlliance(allyGuildId);
                    }
                }
                
                // Load wars
                if (guildsConfig.contains(path + ".wars")) {
                    for (String enemyGuildId : guildsConfig.getStringList(path + ".wars")) {
                        guild.addWar(enemyGuildId);
                    }
                }
                
                // Load statistics
                if (guildsConfig.contains(path + ".stats")) {
                    guild.setKills(guildsConfig.getInt(path + ".stats.kills", 0));
                    guild.setDeaths(guildsConfig.getInt(path + ".stats.deaths", 0));
                    guild.setQuestsCompleted(guildsConfig.getInt(path + ".stats.quests-completed", 0));
                }
                
                guilds.put(guildId, guild);
            }
        }
        
        // Load applications
        if (guildsConfig.contains("applications")) {
            for (String guildId : guildsConfig.getConfigurationSection("applications").getKeys(false)) {
                List<String> applicantStrs = guildsConfig.getStringList("applications." + guildId);
                List<UUID> applicants = new ArrayList<>();
                for (String uuidStr : applicantStrs) {
                    try {
                        applicants.add(UUID.fromString(uuidStr));
                    } catch (IllegalArgumentException e) {
                        // Skip invalid UUIDs
                    }
                }
                guildApplications.put(guildId, applicants);
            }
        }
    }
    
    private void startAutoTasks() {
        // Check for expired wars, process guild level ups, etc.
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Guild guild : guilds.values()) {
                // Process guild level ups
                processGuildLevelUp(guild);
            }
            saveGuilds();
        }, 0L, 12000L); // Every 10 minutes
    }
    
    /**
     * Create a guild
     */
    public Guild createGuild(Player leader, String guildId, String name, String tag) {
        if (playerGuilds.containsKey(leader.getUniqueId())) {
            return null; // Player already in a guild
        }
        
        if (guilds.containsKey(guildId.toLowerCase())) {
            return null; // Guild ID already exists
        }
        
        Guild guild = new Guild(guildId.toLowerCase(), name, leader.getUniqueId(), tag, "", 1, 0.0, System.currentTimeMillis());
        guild.addMember(leader.getUniqueId(), leader.getName(), "leader");
        guilds.put(guildId.toLowerCase(), guild);
        playerGuilds.put(leader.getUniqueId(), guildId.toLowerCase());
        
        // Create guild bank account (if advanced bank manager is available)
        if (plugin.getAdvancedBankManager() != null) {
            plugin.getAdvancedBankManager().createGuildAccount(guildId.toLowerCase(), guild);
        }
        
        saveGuild(guild);
        return guild;
    }
    
    /**
     * Delete a guild
     */
    public boolean deleteGuild(String guildId) {
        Guild guild = guilds.remove(guildId.toLowerCase());
        if (guild == null) return false;
        
        // Remove all members from playerGuilds
        for (UUID memberUuid : guild.getMembers().keySet()) {
            playerGuilds.remove(memberUuid);
        }
        
        // Remove applications
        guildApplications.remove(guildId.toLowerCase());
        
        // Delete guild bank account (if advanced bank manager is available)
        if (plugin.getAdvancedBankManager() != null) {
            plugin.getAdvancedBankManager().deleteGuildAccount(guildId.toLowerCase());
        }
        
        guildsConfig.set("guilds." + guildId.toLowerCase(), null);
        guildsConfig.set("applications." + guildId.toLowerCase(), null);
        saveConfig();
        
        return true;
    }
    
    /**
     * Apply to join a guild
     */
    public boolean applyToGuild(Player player, String guildId) {
        if (playerGuilds.containsKey(player.getUniqueId())) {
            return false; // Already in a guild
        }
        
        Guild guild = guilds.get(guildId.toLowerCase());
        if (guild == null) return false;
        
        List<UUID> applicants = guildApplications.computeIfAbsent(guildId.toLowerCase(), k -> new ArrayList<>());
        if (applicants.contains(player.getUniqueId())) {
            return false; // Already applied
        }
        
        applicants.add(player.getUniqueId());
        saveApplications();
        
        // Notify guild leader
        Player leader = Bukkit.getPlayer(guild.getLeader());
        if (leader != null) {
            leader.sendMessage(org.bukkit.ChatColor.YELLOW + player.getName() + " applied to join your guild!");
        }
        
        return true;
    }
    
    /**
     * Accept a guild application
     */
    public boolean acceptApplication(String guildId, UUID applicantUuid, String rank) {
        Guild guild = guilds.get(guildId.toLowerCase());
        if (guild == null) return false;
        
        List<UUID> applicants = guildApplications.get(guildId.toLowerCase());
        if (applicants == null || !applicants.contains(applicantUuid)) {
            return false;
        }
        
        applicants.remove(applicantUuid);
        saveApplications();
        
        org.bukkit.OfflinePlayer applicant = Bukkit.getOfflinePlayer(applicantUuid);
        guild.addMember(applicantUuid, applicant.getName(), rank);
        playerGuilds.put(applicantUuid, guildId.toLowerCase());
        
        if (applicant.isOnline()) {
            Player player = applicant.getPlayer();
            if (player != null) {
                player.sendMessage(org.bukkit.ChatColor.GREEN + "You were accepted into " + guild.getName() + "!");
            }
        }
        
        saveGuild(guild);
        return true;
    }
    
    /**
     * Send message to guild chat
     */
    public void sendGuildChat(Player sender, String message) {
        String guildId = playerGuilds.get(sender.getUniqueId());
        if (guildId == null) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "You are not in a guild!");
            return;
        }
        
        Guild guild = guilds.get(guildId);
        if (guild == null) return;
        
        String formattedMessage = org.bukkit.ChatColor.translateAlternateColorCodes('&', 
            "&b[Guild] &7" + sender.getName() + ": &f" + message);
        
        guild.broadcast(formattedMessage);
    }
    
    /**
     * Deposit money to guild bank
     */
    public boolean depositToGuildBank(Player player, double amount) {
        String guildId = playerGuilds.get(player.getUniqueId());
        if (guildId == null) return false;
        
        Guild guild = guilds.get(guildId);
        if (guild == null) return false;
        
        if (!plugin.getEconomyManager().removeBalance(player.getUniqueId(), amount)) {
            return false;
        }
        
        guild.addBankBalance(amount);
        saveGuild(guild);
        
        guild.broadcast(org.bukkit.ChatColor.GREEN + player.getName() + " deposited " + 
            plugin.getEconomyManager().format(amount) + " to the guild bank!");
        
        return true;
    }
    
    /**
     * Withdraw money from guild bank (requires permission)
     */
    public boolean withdrawFromGuildBank(Player player, double amount) {
        String guildId = playerGuilds.get(player.getUniqueId());
        if (guildId == null) return false;
        
        Guild guild = guilds.get(guildId);
        if (guild == null) return false;
        
        GuildMember member = guild.getMember(player.getUniqueId());
        if (member == null || !member.hasPermission("withdraw")) {
            return false;
        }
        
        if (guild.getBankBalance() < amount) {
            return false;
        }
        
        guild.removeBankBalance(amount);
        plugin.getEconomyManager().addBalance(player.getUniqueId(), amount);
        saveGuild(guild);
        
        guild.broadcast(org.bukkit.ChatColor.YELLOW + player.getName() + " withdrew " + 
            plugin.getEconomyManager().format(amount) + " from the guild bank!");
        
        return true;
    }
    
    /**
     * Form alliance with another guild
     */
    public boolean formAlliance(String guildId1, String guildId2) {
        Guild guild1 = guilds.get(guildId1.toLowerCase());
        Guild guild2 = guilds.get(guildId2.toLowerCase());
        
        if (guild1 == null || guild2 == null) return false;
        if (guild1.hasAlliance(guildId2.toLowerCase()) || guild2.hasAlliance(guildId1.toLowerCase())) {
            return false; // Already allied
        }
        
        guild1.addAlliance(guildId2.toLowerCase());
        guild2.addAlliance(guildId1.toLowerCase());
        
        saveGuild(guild1);
        saveGuild(guild2);
        
        guild1.broadcast(org.bukkit.ChatColor.GREEN + "Alliance formed with " + guild2.getName() + "!");
        guild2.broadcast(org.bukkit.ChatColor.GREEN + "Alliance formed with " + guild1.getName() + "!");
        
        return true;
    }
    
    /**
     * Declare war on another guild
     */
    public boolean declareWar(String guildId1, String guildId2) {
        Guild guild1 = guilds.get(guildId1.toLowerCase());
        Guild guild2 = guilds.get(guildId2.toLowerCase());
        
        if (guild1 == null || guild2 == null) return false;
        if (guild1.hasWar(guildId2.toLowerCase()) || guild2.hasWar(guildId1.toLowerCase())) {
            return false; // Already at war
        }
        
        guild1.addWar(guildId2.toLowerCase());
        guild2.addWar(guildId1.toLowerCase());
        
        saveGuild(guild1);
        saveGuild(guild2);
        
        guild1.broadcast(org.bukkit.ChatColor.RED + "War declared on " + guild2.getName() + "!");
        guild2.broadcast(org.bukkit.ChatColor.RED + guild1.getName() + " declared war on you!");
        
        return true;
    }
    
    /**
     * Add experience to guild (from quests, activities, etc.)
     */
    public void addGuildExperience(String guildId, double experience) {
        Guild guild = guilds.get(guildId.toLowerCase());
        if (guild == null) return;
        
        guild.addExperience(experience);
        processGuildLevelUp(guild);
        saveGuild(guild);
    }
    
    private void processGuildLevelUp(Guild guild) {
        double expForNextLevel = guild.getExpForNextLevel();
        if (guild.getExperience() >= expForNextLevel) {
            guild.setLevel(guild.getLevel() + 1);
            guild.setExperience(guild.getExperience() - expForNextLevel);
            guild.broadcast(org.bukkit.ChatColor.GREEN + "Guild leveled up to level " + guild.getLevel() + "!");
        }
    }
    
    private void saveGuild(Guild guild) {
        String path = "guilds." + guild.getId();
        guildsConfig.set(path + ".name", guild.getName());
        guildsConfig.set(path + ".leader", guild.getLeader().toString());
        guildsConfig.set(path + ".tag", guild.getTag());
        guildsConfig.set(path + ".description", guild.getDescription());
        guildsConfig.set(path + ".level", guild.getLevel());
        guildsConfig.set(path + ".experience", guild.getExperience());
        guildsConfig.set(path + ".created", guild.getCreated());
        guildsConfig.set(path + ".bank-balance", guild.getBankBalance());
        
        // Save members
        for (Map.Entry<UUID, GuildMember> entry : guild.getMembers().entrySet()) {
            guildsConfig.set(path + ".members." + entry.getKey().toString() + ".name", entry.getValue().getName());
            guildsConfig.set(path + ".members." + entry.getKey().toString() + ".rank", entry.getValue().getRank());
        }
        
        // Save warps, homes, alliances, wars
        guildsConfig.set(path + ".warps", new ArrayList<>(guild.getWarps()));
        guildsConfig.set(path + ".homes", new ArrayList<>(guild.getHomes()));
        guildsConfig.set(path + ".alliances", new ArrayList<>(guild.getAlliances()));
        guildsConfig.set(path + ".wars", new ArrayList<>(guild.getWars()));
        
        // Save statistics
        guildsConfig.set(path + ".stats.kills", guild.getKills());
        guildsConfig.set(path + ".stats.deaths", guild.getDeaths());
        guildsConfig.set(path + ".stats.quests-completed", guild.getQuestsCompleted());
        
        saveConfig();
    }
    
    private void saveApplications() {
        guildsConfig.set("applications", null);
        for (Map.Entry<String, List<UUID>> entry : guildApplications.entrySet()) {
            List<String> applicantStrs = new ArrayList<>();
            for (UUID uuid : entry.getValue()) {
                applicantStrs.add(uuid.toString());
            }
            guildsConfig.set("applications." + entry.getKey(), applicantStrs);
        }
        saveConfig();
    }
    
    private void saveGuilds() {
        for (Guild guild : guilds.values()) {
            saveGuild(guild);
        }
    }
    
    private void saveConfig() {
        try {
            guildsConfig.save(guildsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save guilds.yml", e);
        }
    }
    
    public Guild getGuild(String guildId) {
        return guilds.get(guildId.toLowerCase());
    }
    
    public Guild getPlayerGuild(UUID playerUuid) {
        String guildId = playerGuilds.get(playerUuid);
        return guildId != null ? guilds.get(guildId) : null;
    }
    
    public Collection<Guild> getGuilds() {
        return guilds.values();
    }
    
    public List<UUID> getGuildApplications(String guildId) {
        return guildApplications.getOrDefault(guildId.toLowerCase(), new ArrayList<>());
    }
    
    /**
     * Guild class
     */
    public static class Guild {
        private String id;
        private String name;
        private UUID leader;
        private String tag;
        private String description;
        private int level;
        private double experience;
        private long created;
        private double bankBalance;
        private Map<UUID, GuildMember> members;
        private Set<String> warps;
        private Set<String> homes;
        private Set<String> alliances;
        private Set<String> wars;
        private int kills;
        private int deaths;
        private int questsCompleted;
        
        public Guild(String id, String name, UUID leader, String tag, String description, 
                    int level, double experience, long created) {
            this.id = id;
            this.name = name;
            this.leader = leader;
            this.tag = tag;
            this.description = description;
            this.level = level;
            this.experience = experience;
            this.created = created;
            this.bankBalance = 0.0;
            this.members = new HashMap<>();
            this.warps = new HashSet<>();
            this.homes = new HashSet<>();
            this.alliances = new HashSet<>();
            this.wars = new HashSet<>();
            this.kills = 0;
            this.deaths = 0;
            this.questsCompleted = 0;
        }
        
        public void addMember(UUID uuid, String name, String rank) {
            members.put(uuid, new GuildMember(uuid, name, rank));
        }
        
        public void removeMember(UUID uuid) {
            members.remove(uuid);
        }
        
        public void broadcast(String message) {
            for (UUID memberUuid : members.keySet()) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null && member.isOnline()) {
                    member.sendMessage(message);
                }
            }
        }
        
        public double getExpForNextLevel() {
            return 1000.0 * Math.pow(level, 1.5);
        }
        
        // Getters and setters
        public String getId() { return id; }
        public String getName() { return name; }
        public UUID getLeader() { return leader; }
        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public double getExperience() { return experience; }
        public void setExperience(double experience) { this.experience = experience; }
        public void addExperience(double exp) { this.experience += exp; }
        public long getCreated() { return created; }
        public double getBankBalance() { return bankBalance; }
        public void setBankBalance(double balance) { this.bankBalance = balance; }
        public void addBankBalance(double amount) { this.bankBalance += amount; }
        public void removeBankBalance(double amount) { this.bankBalance -= amount; }
        public Map<UUID, GuildMember> getMembers() { return members; }
        public GuildMember getMember(UUID uuid) { return members.get(uuid); }
        public Set<String> getWarps() { return warps; }
        public void addWarp(String warp) { warps.add(warp); }
        public void removeWarp(String warp) { warps.remove(warp); }
        public Set<String> getHomes() { return homes; }
        public void addHome(String home) { homes.add(home); }
        public void removeHome(String home) { homes.remove(home); }
        public Set<String> getAlliances() { return alliances; }
        public void addAlliance(String guildId) { alliances.add(guildId.toLowerCase()); }
        public boolean hasAlliance(String guildId) { return alliances.contains(guildId.toLowerCase()); }
        public Set<String> getWars() { return wars; }
        public void addWar(String guildId) { wars.add(guildId.toLowerCase()); }
        public boolean hasWar(String guildId) { return wars.contains(guildId.toLowerCase()); }
        public int getKills() { return kills; }
        public void setKills(int kills) { this.kills = kills; }
        public void addKill() { this.kills++; }
        public int getDeaths() { return deaths; }
        public void setDeaths(int deaths) { this.deaths = deaths; }
        public void addDeath() { this.deaths++; }
        public int getQuestsCompleted() { return questsCompleted; }
        public void setQuestsCompleted(int quests) { this.questsCompleted = quests; }
        public void addQuestCompleted() { this.questsCompleted++; }
    }
    
    /**
     * Guild member class
     */
    public static class GuildMember {
        private UUID uuid;
        private String name;
        private String rank;
        
        public GuildMember(UUID uuid, String name, String rank) {
            this.uuid = uuid;
            this.name = name;
            this.rank = rank;
        }
        
        public boolean hasPermission(String permission) {
            // Rank permissions: leader > officer > member
            if (rank.equalsIgnoreCase("leader")) return true;
            if (rank.equalsIgnoreCase("officer")) {
                return !permission.equals("delete") && !permission.equals("transfer");
            }
            return permission.equals("chat") || permission.equals("view");
        }
        
        public UUID getUuid() { return uuid; }
        public String getName() { return name; }
        public String getRank() { return rank; }
        public void setRank(String rank) { this.rank = rank; }
    }
}

