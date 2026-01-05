package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Advanced Ban System Manager
 * Handles temporary bans, IP bans, ban history, appeals, and ban templates
 */
public class BanManager {
    private final Ecore plugin;
    private File bansFile;
    private FileConfiguration bansConfig;
    private File banTemplatesFile;
    private FileConfiguration banTemplatesConfig;
    private final Map<UUID, Long> temporaryBans; // UUID -> unban timestamp
    private final Map<String, Long> ipBans; // IP -> unban timestamp
    private final Map<UUID, List<BanEntry>> banHistory; // UUID -> list of ban entries
    
    public BanManager(Ecore plugin) {
        this.plugin = plugin;
        this.temporaryBans = new ConcurrentHashMap<>();
        this.ipBans = new ConcurrentHashMap<>();
        this.banHistory = new ConcurrentHashMap<>();
        initializeConfigs();
        loadBans();
        startUnbanTask();
    }
    
    private void initializeConfigs() {
        // Initialize bans.yml
        bansFile = new File(plugin.getDataFolder(), "bans.yml");
        if (!bansFile.exists()) {
            try {
                bansFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create bans.yml", e);
            }
        }
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
        
        // Initialize ban-templates.yml
        banTemplatesFile = new File(plugin.getDataFolder(), "ban-templates.yml");
        if (!banTemplatesFile.exists()) {
            try {
                banTemplatesFile.createNewFile();
                // Create default templates
                banTemplatesConfig = YamlConfiguration.loadConfiguration(banTemplatesFile);
                createDefaultTemplates();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create ban-templates.yml", e);
            }
        } else {
            banTemplatesConfig = YamlConfiguration.loadConfiguration(banTemplatesFile);
        }
    }
    
    private void createDefaultTemplates() {
        banTemplatesConfig.set("templates.hacking.reason", "Hacking/Cheating");
        banTemplatesConfig.set("templates.hacking.duration", -1); // Permanent
        banTemplatesConfig.set("templates.hacking.ip-ban", false);
        
        banTemplatesConfig.set("templates.griefing.reason", "Griefing");
        banTemplatesConfig.set("templates.griefing.duration", 2592000); // 30 days
        banTemplatesConfig.set("templates.griefing.ip-ban", false);
        
        banTemplatesConfig.set("templates.spam.reason", "Spam/Advertising");
        banTemplatesConfig.set("templates.spam.duration", 86400); // 1 day
        banTemplatesConfig.set("templates.spam.ip-ban", false);
        
        banTemplatesConfig.set("templates.inappropriate-behavior.reason", "Inappropriate Behavior");
        banTemplatesConfig.set("templates.inappropriate-behavior.duration", 604800); // 7 days
        banTemplatesConfig.set("templates.inappropriate-behavior.ip-ban", false);
        
        saveBanTemplates();
    }
    
    private void loadBans() {
        // Load temporary bans
        if (bansConfig.contains("temporary-bans")) {
            for (String uuidStr : bansConfig.getConfigurationSection("temporary-bans").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                long unbanTime = bansConfig.getLong("temporary-bans." + uuidStr);
                if (unbanTime > System.currentTimeMillis()) {
                    temporaryBans.put(uuid, unbanTime);
                }
            }
        }
        
        // Load IP bans
        if (bansConfig.contains("ip-bans")) {
            for (String ip : bansConfig.getConfigurationSection("ip-bans").getKeys(false)) {
                long unbanTime = bansConfig.getLong("ip-bans." + ip);
                if (unbanTime > System.currentTimeMillis()) {
                    ipBans.put(ip, unbanTime);
                }
            }
        }
        
        // Load ban history
        if (bansConfig.contains("history")) {
            for (String uuidStr : bansConfig.getConfigurationSection("history").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                List<BanEntry> history = new ArrayList<>();
                List<Map<?, ?>> entries = bansConfig.getMapList("history." + uuidStr);
                for (Map<?, ?> entryMap : entries) {
                    BanEntry entry = BanEntry.fromMap(entryMap);
                    if (entry != null) {
                        history.add(entry);
                    }
                }
                banHistory.put(uuid, history);
            }
        }
    }
    
    private void startUnbanTask() {
        // Check for expired bans every minute
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            
            // Check temporary bans
            Iterator<Map.Entry<UUID, Long>> tempBanIterator = temporaryBans.entrySet().iterator();
            while (tempBanIterator.hasNext()) {
                Map.Entry<UUID, Long> entry = tempBanIterator.next();
                if (entry.getValue() <= currentTime) {
                    UUID uuid = entry.getKey();
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                    @SuppressWarnings({"deprecation", "rawtypes"})
                    BanList nameBanList = Bukkit.getBanList(BanList.Type.NAME);
                    @SuppressWarnings("deprecation")
                    String playerName = player.getName();
                    nameBanList.pardon(playerName);
                    tempBanIterator.remove();
                    plugin.getLogger().info("Auto-unbanned " + player.getName() + " (temporary ban expired)");
                }
            }
            
            // Check IP bans
            Iterator<Map.Entry<String, Long>> ipBanIterator = ipBans.entrySet().iterator();
            while (ipBanIterator.hasNext()) {
                Map.Entry<String, Long> entry = ipBanIterator.next();
                if (entry.getValue() <= currentTime) {
                    String ip = entry.getKey();
                    @SuppressWarnings({"deprecation", "rawtypes"})
                    BanList ipBanList = Bukkit.getBanList(BanList.Type.IP);
                    @SuppressWarnings("deprecation")
                    String ipAddress = ip;
                    ipBanList.pardon(ipAddress);
                    ipBanIterator.remove();
                    plugin.getLogger().info("Auto-unbanned IP " + ip + " (temporary ban expired)");
                }
            }
            
            saveBans();
        }, 0L, 1200L); // Every minute
    }
    
    /**
     * Ban a player with optional duration
     * @param staff The staff member banning (can be null for console)
     * @param target The target player name or UUID
     * @param reason The ban reason
     * @param duration Duration in seconds (-1 for permanent)
     * @param ipBan Whether to also ban the IP
     * @param evidence Optional evidence/notes
     * @return true if successful
     */
    @SuppressWarnings("deprecation")
    public boolean banPlayer(Player staff, String target, String reason, long duration, boolean ipBan, String evidence) {
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);
        UUID uuid = targetPlayer.getUniqueId();
        
        // Add to ban list
        if (duration > 0) {
            long unbanTime = System.currentTimeMillis() + (duration * 1000L);
            temporaryBans.put(uuid, unbanTime);
            bansConfig.set("temporary-bans." + uuid.toString(), unbanTime);
        } else {
            bansConfig.set("temporary-bans." + uuid.toString(), -1);
        }
        
        // Ban the player
        Bukkit.getBanList(BanList.Type.NAME).addBan(
            targetPlayer.getName(),
            reason,
            duration > 0 ? new Date(System.currentTimeMillis() + (duration * 1000L)) : null,
            staff != null ? staff.getName() : "Console"
        );
        
        // Kick if online
        if (targetPlayer.isOnline()) {
            Player onlinePlayer = targetPlayer.getPlayer();
            if (onlinePlayer != null) {
                String kickMessage = ChatColor.RED + "You have been banned!\n" +
                    ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + reason;
                if (duration > 0) {
                    kickMessage += "\n" + ChatColor.YELLOW + "Duration: " + ChatColor.WHITE + formatDuration(duration);
                }
                onlinePlayer.kickPlayer(kickMessage);
            }
        }
        
        // IP ban if requested
        if (ipBan && targetPlayer.isOnline()) {
            Player onlinePlayer = targetPlayer.getPlayer();
            if (onlinePlayer != null) {
                String ip = onlinePlayer.getAddress().getAddress().getHostAddress();
                if (duration > 0) {
                    long unbanTime = System.currentTimeMillis() + (duration * 1000L);
                    ipBans.put(ip, unbanTime);
                    bansConfig.set("ip-bans." + ip, unbanTime);
                } else {
                    bansConfig.set("ip-bans." + ip, -1);
                }
                Bukkit.getBanList(BanList.Type.IP).addBan(
                    ip,
                    reason,
                    duration > 0 ? new Date(System.currentTimeMillis() + (duration * 1000L)) : null,
                    staff != null ? staff.getName() : "Console"
                );
            }
        }
        
        // Add to ban history
        BanEntry banEntry = new BanEntry(
            uuid,
            targetPlayer.getName(),
            reason,
            duration,
            staff != null ? staff.getUniqueId() : null,
            staff != null ? staff.getName() : "Console",
            System.currentTimeMillis(),
            evidence,
            false // not appealed
        );
        
        List<BanEntry> history = banHistory.getOrDefault(uuid, new ArrayList<>());
        history.add(banEntry);
        banHistory.put(uuid, history);
        
        // Save to config
        List<Map<String, Object>> historyList = new ArrayList<>();
        for (BanEntry entry : history) {
            historyList.add(entry.toMap());
        }
        bansConfig.set("history." + uuid.toString(), historyList);
        
        saveBans();
        
        // Notify Discord
        if (staff != null) {
            plugin.getDiscordManager().sendStaffLogNotification(
                "punishment-log",
                staff.getName(),
                "banned",
                targetPlayer.getName(),
                reason + (duration > 0 ? " (" + formatDuration(duration) + ")" : " (Permanent)")
            );
        }
        
        if (staff != null) {
            staff.sendMessage(ChatColor.GREEN + "Banned " + targetPlayer.getName() + " for: " + reason);
        }
        
        return true;
    }
    
    /**
     * Unban a player
     */
    @SuppressWarnings("deprecation")
    public boolean unbanPlayer(String target) {
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);
        UUID uuid = targetPlayer.getUniqueId();
        
        // Remove from temporary bans
        temporaryBans.remove(uuid);
        bansConfig.set("temporary-bans." + uuid.toString(), null);
        
        // Pardon from ban list
        Bukkit.getBanList(BanList.Type.NAME).pardon(targetPlayer.getName());
        
        saveBans();
        return true;
    }
    
    /**
     * Check if a player is banned
     */
    @SuppressWarnings({"deprecation", "rawtypes"})
    public boolean isBanned(String target) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(target);
        BanList nameBanList = Bukkit.getBanList(BanList.Type.NAME);
        return nameBanList.isBanned(player.getName());
    }
    
    /**
     * Check if an IP is banned
     */
    @SuppressWarnings({"deprecation", "rawtypes"})
    public boolean isIPBanned(String ip) {
        BanList ipBanList = Bukkit.getBanList(BanList.Type.IP);
        @SuppressWarnings("deprecation")
        boolean isBanned = ipBanList.isBanned(ip);
        return isBanned || (ipBans.containsKey(ip) && ipBans.get(ip) > System.currentTimeMillis());
    }
    
    /**
     * Get ban history for a player
     */
    public List<BanEntry> getBanHistory(UUID uuid) {
        return banHistory.getOrDefault(uuid, new ArrayList<>());
    }
    
    /**
     * Get ban template
     */
    public BanTemplate getBanTemplate(String templateName) {
        if (!banTemplatesConfig.contains("templates." + templateName)) {
            return null;
        }
        String reason = banTemplatesConfig.getString("templates." + templateName + ".reason");
        long duration = banTemplatesConfig.getLong("templates." + templateName + ".duration");
        boolean ipBan = banTemplatesConfig.getBoolean("templates." + templateName + ".ip-ban", false);
        return new BanTemplate(templateName, reason, duration, ipBan);
    }
    
    /**
     * Get all ban templates
     */
    public List<BanTemplate> getBanTemplates() {
        List<BanTemplate> templates = new ArrayList<>();
        if (banTemplatesConfig.contains("templates")) {
            for (String key : banTemplatesConfig.getConfigurationSection("templates").getKeys(false)) {
                BanTemplate template = getBanTemplate(key);
                if (template != null) {
                    templates.add(template);
                }
            }
        }
        return templates;
    }
    
    /**
     * Create a ban template
     */
    public void createBanTemplate(String name, String reason, long duration, boolean ipBan) {
        banTemplatesConfig.set("templates." + name + ".reason", reason);
        banTemplatesConfig.set("templates." + name + ".duration", duration);
        banTemplatesConfig.set("templates." + name + ".ip-ban", ipBan);
        saveBanTemplates();
    }
    
    /**
     * Submit a ban appeal
     */
    public void submitAppeal(UUID uuid, String appealMessage) {
        List<BanEntry> history = banHistory.get(uuid);
        if (history != null && !history.isEmpty()) {
            BanEntry lastBan = history.get(history.size() - 1);
            lastBan.setAppealed(true);
            lastBan.setAppealMessage(appealMessage);
            lastBan.setAppealTime(System.currentTimeMillis());
            
            // Save to config
            List<Map<String, Object>> historyList = new ArrayList<>();
            for (BanEntry entry : history) {
                historyList.add(entry.toMap());
            }
            bansConfig.set("history." + uuid.toString(), historyList);
            saveBans();
        }
    }
    
    /**
     * Get pending appeals
     */
    public List<BanEntry> getPendingAppeals() {
        List<BanEntry> appeals = new ArrayList<>();
        for (List<BanEntry> history : banHistory.values()) {
            for (BanEntry entry : history) {
                if (entry.isAppealed() && !entry.isAppealReviewed()) {
                    appeals.add(entry);
                }
            }
        }
        return appeals;
    }
    
    private void saveBans() {
        try {
            bansConfig.save(bansFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save bans.yml", e);
        }
    }
    
    private void saveBanTemplates() {
        try {
            banTemplatesConfig.save(banTemplatesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save ban-templates.yml", e);
        }
    }
    
    private String formatDuration(long seconds) {
        if (seconds < 60) return seconds + " seconds";
        if (seconds < 3600) return (seconds / 60) + " minutes";
        if (seconds < 86400) return (seconds / 3600) + " hours";
        return (seconds / 86400) + " days";
    }
    
    /**
     * Ban entry class
     */
    public static class BanEntry {
        private UUID playerUUID;
        private String playerName;
        private String reason;
        private long duration; // -1 for permanent
        private UUID staffUUID;
        private String staffName;
        private long banTime;
        private String evidence;
        private boolean appealed;
        private String appealMessage;
        private long appealTime;
        private boolean appealReviewed;
        
        public BanEntry(UUID playerUUID, String playerName, String reason, long duration,
                       UUID staffUUID, String staffName, long banTime, String evidence, boolean appealed) {
            this.playerUUID = playerUUID;
            this.playerName = playerName;
            this.reason = reason;
            this.duration = duration;
            this.staffUUID = staffUUID;
            this.staffName = staffName;
            this.banTime = banTime;
            this.evidence = evidence;
            this.appealed = appealed;
        }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("player-uuid", playerUUID.toString());
            map.put("player-name", playerName);
            map.put("reason", reason);
            map.put("duration", duration);
            if (staffUUID != null) map.put("staff-uuid", staffUUID.toString());
            map.put("staff-name", staffName);
            map.put("ban-time", banTime);
            if (evidence != null) map.put("evidence", evidence);
            map.put("appealed", appealed);
            if (appealMessage != null) map.put("appeal-message", appealMessage);
            if (appealTime > 0) map.put("appeal-time", appealTime);
            map.put("appeal-reviewed", appealReviewed);
            return map;
        }
        
        public static BanEntry fromMap(Map<?, ?> map) {
            try {
                UUID playerUUID = UUID.fromString((String) map.get("player-uuid"));
                String playerName = (String) map.get("player-name");
                String reason = (String) map.get("reason");
                long duration = ((Number) map.get("duration")).longValue();
                UUID staffUUID = map.get("staff-uuid") != null ? UUID.fromString((String) map.get("staff-uuid")) : null;
                String staffName = (String) map.get("staff-name");
                long banTime = ((Number) map.get("ban-time")).longValue();
                String evidence = (String) map.get("evidence");
                boolean appealed = map.get("appealed") != null ? (Boolean) map.get("appealed") : false;
                
                BanEntry entry = new BanEntry(playerUUID, playerName, reason, duration, staffUUID, staffName, banTime, evidence, appealed);
                if (map.get("appeal-message") != null) entry.setAppealMessage((String) map.get("appeal-message"));
                if (map.get("appeal-time") != null) entry.setAppealTime(((Number) map.get("appeal-time")).longValue());
                if (map.get("appeal-reviewed") != null) entry.setAppealReviewed((Boolean) map.get("appeal-reviewed"));
                return entry;
            } catch (Exception e) {
                return null;
            }
        }
        
        // Getters and setters
        public UUID getPlayerUUID() { return playerUUID; }
        public String getPlayerName() { return playerName; }
        public String getReason() { return reason; }
        public long getDuration() { return duration; }
        public UUID getStaffUUID() { return staffUUID; }
        public String getStaffName() { return staffName; }
        public long getBanTime() { return banTime; }
        public String getEvidence() { return evidence; }
        public boolean isAppealed() { return appealed; }
        public void setAppealed(boolean appealed) { this.appealed = appealed; }
        public String getAppealMessage() { return appealMessage; }
        public void setAppealMessage(String appealMessage) { this.appealMessage = appealMessage; }
        public long getAppealTime() { return appealTime; }
        public void setAppealTime(long appealTime) { this.appealTime = appealTime; }
        public boolean isAppealReviewed() { return appealReviewed; }
        public void setAppealReviewed(boolean appealReviewed) { this.appealReviewed = appealReviewed; }
    }
    
    /**
     * Ban template class
     */
    public static class BanTemplate {
        private String name;
        private String reason;
        private long duration;
        private boolean ipBan;
        
        public BanTemplate(String name, String reason, long duration, boolean ipBan) {
            this.name = name;
            this.reason = reason;
            this.duration = duration;
            this.ipBan = ipBan;
        }
        
        public String getName() { return name; }
        public String getReason() { return reason; }
        public long getDuration() { return duration; }
        public boolean isIpBan() { return ipBan; }
    }
}

