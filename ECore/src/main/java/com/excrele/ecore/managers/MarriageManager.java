package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Marriage/Relationship System Manager
 * Handles proposals, ceremonies, shared homes, teleport, chat, rings, statistics, anniversaries, divorce
 */
public class MarriageManager {
    private final Ecore plugin;
    private File marriagesFile;
    private FileConfiguration marriagesConfig;
    private final Map<UUID, UUID> marriages; // Player UUID -> Spouse UUID
    private final Map<UUID, UUID> pendingProposals; // Proposer UUID -> Target UUID
    private final Map<UUID, MarriageData> marriageData; // Player UUID -> Marriage Data
    private BukkitTask anniversaryTask;
    
    public MarriageManager(Ecore plugin) {
        this.plugin = plugin;
        this.marriages = new HashMap<>();
        this.pendingProposals = new HashMap<>();
        this.marriageData = new HashMap<>();
        initializeConfig();
        loadMarriages();
        startAnniversaryTask();
    }
    
    private void initializeConfig() {
        marriagesFile = new File(plugin.getDataFolder(), "marriages.yml");
        if (!marriagesFile.exists()) {
            try {
                marriagesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create marriages.yml", e);
            }
        }
        marriagesConfig = YamlConfiguration.loadConfiguration(marriagesFile);
    }
    
    private void loadMarriages() {
        if (marriagesConfig.contains("marriages")) {
            for (String uuidStr : marriagesConfig.getConfigurationSection("marriages").getKeys(false)) {
                try {
                    UUID playerUuid = UUID.fromString(uuidStr);
                    UUID spouseUuid = UUID.fromString(marriagesConfig.getString("marriages." + uuidStr + ".spouse"));
                    long marriedAt = marriagesConfig.getLong("marriages." + uuidStr + ".married-at", System.currentTimeMillis());
                    String ceremonyLocation = marriagesConfig.getString("marriages." + uuidStr + ".ceremony-location", "");
                    
                    marriages.put(playerUuid, spouseUuid);
                    marriages.put(spouseUuid, playerUuid);
                    
                    MarriageData data = new MarriageData(playerUuid, spouseUuid, marriedAt, ceremonyLocation);
                    marriageData.put(playerUuid, data);
                    marriageData.put(spouseUuid, data);
                } catch (IllegalArgumentException e) {
                    // Skip invalid UUIDs
                }
            }
        }
    }
    
    /**
     * Propose to another player
     */
    public boolean propose(Player proposer, Player target) {
        if (isMarried(proposer.getUniqueId())) {
            proposer.sendMessage(org.bukkit.ChatColor.RED + "You are already married!");
            return false;
        }
        
        if (isMarried(target.getUniqueId())) {
            proposer.sendMessage(org.bukkit.ChatColor.RED + target.getName() + " is already married!");
            return false;
        }
        
        if (pendingProposals.containsKey(proposer.getUniqueId()) || 
            pendingProposals.containsValue(target.getUniqueId())) {
            proposer.sendMessage(org.bukkit.ChatColor.RED + "You already have a pending proposal!");
            return false;
        }
        
        pendingProposals.put(proposer.getUniqueId(), target.getUniqueId());
        
        proposer.sendMessage(org.bukkit.ChatColor.GREEN + "Marriage proposal sent to " + target.getName() + "!");
        target.sendMessage(org.bukkit.ChatColor.GOLD + proposer.getName() + " has proposed to you! Use /marry accept to accept or /marry deny to deny.");
        
        return true;
    }
    
    /**
     * Accept a marriage proposal
     */
    public boolean acceptProposal(Player accepter) {
        UUID proposerUuid = null;
        for (Map.Entry<UUID, UUID> entry : pendingProposals.entrySet()) {
            if (entry.getValue().equals(accepter.getUniqueId())) {
                proposerUuid = entry.getKey();
                break;
            }
        }
        
        if (proposerUuid == null) {
            accepter.sendMessage(org.bukkit.ChatColor.RED + "You don't have any pending proposals!");
            return false;
        }
        
        Player proposer = Bukkit.getPlayer(proposerUuid);
        if (proposer == null || !proposer.isOnline()) {
            accepter.sendMessage(org.bukkit.ChatColor.RED + "The proposer is no longer online!");
            pendingProposals.remove(proposerUuid);
            return false;
        }
        
        // Create marriage
        long marriedAt = System.currentTimeMillis();
        Location ceremonyLocation = accepter.getLocation();
        String ceremonyLocStr = ceremonyLocation.getWorld().getName() + "," + 
                                ceremonyLocation.getX() + "," + 
                                ceremonyLocation.getY() + "," + 
                                ceremonyLocation.getZ();
        
        MarriageData data = new MarriageData(proposerUuid, accepter.getUniqueId(), marriedAt, ceremonyLocStr);
        marriages.put(proposerUuid, accepter.getUniqueId());
        marriages.put(accepter.getUniqueId(), proposerUuid);
        marriageData.put(proposerUuid, data);
        marriageData.put(accepter.getUniqueId(), data);
        
        pendingProposals.remove(proposerUuid);
        
        // Give marriage rings
        giveMarriageRing(proposer);
        giveMarriageRing(accepter);
        
        // Make them friends automatically
        if (plugin.getFriendManager() != null) {
            // Send friend requests and accept them to make them friends
            plugin.getFriendManager().sendFriendRequest(proposer, accepter);
            plugin.getFriendManager().acceptFriendRequest(accepter, proposer);
        }
        
        // Broadcast marriage
        Bukkit.broadcastMessage(org.bukkit.ChatColor.LIGHT_PURPLE + "❤ " + proposer.getName() + " and " + 
                               accepter.getName() + " are now married! ❤");
        
        // Save
        saveMarriage(proposerUuid, accepter.getUniqueId(), data);
        
        return true;
    }
    
    /**
     * Deny a marriage proposal
     */
    public boolean denyProposal(Player denier) {
        UUID proposerUuid = null;
        for (Map.Entry<UUID, UUID> entry : pendingProposals.entrySet()) {
            if (entry.getValue().equals(denier.getUniqueId())) {
                proposerUuid = entry.getKey();
                break;
            }
        }
        
        if (proposerUuid == null) {
            denier.sendMessage(org.bukkit.ChatColor.RED + "You don't have any pending proposals!");
            return false;
        }
        
        pendingProposals.remove(proposerUuid);
        
        Player proposer = Bukkit.getPlayer(proposerUuid);
        if (proposer != null && proposer.isOnline()) {
            proposer.sendMessage(org.bukkit.ChatColor.RED + denier.getName() + " denied your marriage proposal.");
        }
        
        denier.sendMessage(org.bukkit.ChatColor.YELLOW + "Marriage proposal denied.");
        
        return true;
    }
    
    /**
     * Divorce
     */
    public boolean divorce(Player player) {
        UUID spouseUuid = marriages.get(player.getUniqueId());
        if (spouseUuid == null) {
            player.sendMessage(org.bukkit.ChatColor.RED + "You are not married!");
            return false;
        }
        
        marriages.remove(player.getUniqueId());
        marriages.remove(spouseUuid);
        marriageData.remove(player.getUniqueId());
        marriageData.remove(spouseUuid);
        
        Player spouse = Bukkit.getPlayer(spouseUuid);
        if (spouse != null && spouse.isOnline()) {
            spouse.sendMessage(org.bukkit.ChatColor.RED + player.getName() + " has divorced you.");
        }
        
        player.sendMessage(org.bukkit.ChatColor.RED + "You have divorced your spouse.");
        
        // Remove from config
        marriagesConfig.set("marriages." + player.getUniqueId().toString(), null);
        marriagesConfig.set("marriages." + spouseUuid.toString(), null);
        saveConfig();
        
        return true;
    }
    
    /**
     * Teleport to spouse
     */
    public boolean teleportToSpouse(Player player) {
        UUID spouseUuid = marriages.get(player.getUniqueId());
        if (spouseUuid == null) {
            player.sendMessage(org.bukkit.ChatColor.RED + "You are not married!");
            return false;
        }
        
        Player spouse = Bukkit.getPlayer(spouseUuid);
        if (spouse == null || !spouse.isOnline()) {
            player.sendMessage(org.bukkit.ChatColor.RED + "Your spouse is not online!");
            return false;
        }
        
        if (plugin.getTeleportManager() != null) {
            plugin.getTeleportManager().teleport(player, spouse.getLocation());
            player.sendMessage(org.bukkit.ChatColor.GREEN + "Teleported to your spouse!");
            return true;
        }
        
        return false;
    }
    
    /**
     * Send message to spouse
     */
    public void sendMarriageChat(Player sender, String message) {
        UUID spouseUuid = marriages.get(sender.getUniqueId());
        if (spouseUuid == null) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "You are not married!");
            return;
        }
        
        Player spouse = Bukkit.getPlayer(spouseUuid);
        if (spouse == null || !spouse.isOnline()) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "Your spouse is not online!");
            return;
        }
        
        String formattedMessage = org.bukkit.ChatColor.translateAlternateColorCodes('&', 
            "&d[Marriage] &7" + sender.getName() + ": &f" + message);
        
        sender.sendMessage(formattedMessage);
        spouse.sendMessage(formattedMessage);
    }
    
    /**
     * Get marriage statistics
     */
    public MarriageStats getMarriageStats(UUID playerUuid) {
        MarriageData data = marriageData.get(playerUuid);
        if (data == null) return null;
        
        long duration = System.currentTimeMillis() - data.getMarriedAt();
        long days = duration / (24 * 60 * 60 * 1000);
        
        return new MarriageStats(data.getMarriedAt(), days, data.getCeremonyLocation());
    }
    
    /**
     * Give marriage ring
     */
    private void giveMarriageRing(Player player) {
        ItemStack ring = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta meta = ring.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(org.bukkit.ChatColor.LIGHT_PURPLE + "Marriage Ring");
            meta.setLore(Arrays.asList(
                org.bukkit.ChatColor.GRAY + "A symbol of eternal love",
                org.bukkit.ChatColor.GRAY + "Right-click to teleport to spouse"
            ));
            ring.setItemMeta(meta);
        }
        
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), ring);
        } else {
            player.getInventory().addItem(ring);
        }
    }
    
    private void startAnniversaryTask() {
        // Check for anniversaries daily
        anniversaryTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            for (MarriageData data : marriageData.values()) {
                long daysMarried = (currentTime - data.getMarriedAt()) / (24 * 60 * 60 * 1000);
                
                // Check if it's an anniversary (every 30 days)
                if (daysMarried > 0 && daysMarried % 30 == 0) {
                    Player player1 = Bukkit.getPlayer(data.getPlayer1());
                    Player player2 = Bukkit.getPlayer(data.getPlayer2());
                    
                    if (player1 != null && player1.isOnline()) {
                        player1.sendMessage(org.bukkit.ChatColor.LIGHT_PURPLE + "❤ Happy " + (daysMarried / 30) + 
                                          " month anniversary! ❤");
                    }
                    if (player2 != null && player2.isOnline()) {
                        player2.sendMessage(org.bukkit.ChatColor.LIGHT_PURPLE + "❤ Happy " + (daysMarried / 30) + 
                                          " month anniversary! ❤");
                    }
                }
            }
        }, 0L, 1728000L); // Every 20 minutes (check for anniversaries)
    }
    
    private void saveMarriage(UUID player1, UUID player2, MarriageData data) {
        String path1 = "marriages." + player1.toString();
        marriagesConfig.set(path1 + ".spouse", player2.toString());
        marriagesConfig.set(path1 + ".married-at", data.getMarriedAt());
        marriagesConfig.set(path1 + ".ceremony-location", data.getCeremonyLocation());
        
        String path2 = "marriages." + player2.toString();
        marriagesConfig.set(path2 + ".spouse", player1.toString());
        marriagesConfig.set(path2 + ".married-at", data.getMarriedAt());
        marriagesConfig.set(path2 + ".ceremony-location", data.getCeremonyLocation());
        
        saveConfig();
    }
    
    private void saveConfig() {
        try {
            marriagesConfig.save(marriagesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save marriages.yml", e);
        }
    }
    
    public boolean isMarried(UUID playerUuid) {
        return marriages.containsKey(playerUuid);
    }
    
    public UUID getSpouse(UUID playerUuid) {
        return marriages.get(playerUuid);
    }
    
    public void shutdown() {
        if (anniversaryTask != null) {
            anniversaryTask.cancel();
        }
        saveConfig();
    }
    
    /**
     * Marriage Data class
     */
    public static class MarriageData {
        private UUID player1;
        private UUID player2;
        private long marriedAt;
        private String ceremonyLocation;
        
        public MarriageData(UUID player1, UUID player2, long marriedAt, String ceremonyLocation) {
            this.player1 = player1;
            this.player2 = player2;
            this.marriedAt = marriedAt;
            this.ceremonyLocation = ceremonyLocation;
        }
        
        public UUID getPlayer1() { return player1; }
        public UUID getPlayer2() { return player2; }
        public long getMarriedAt() { return marriedAt; }
        public String getCeremonyLocation() { return ceremonyLocation; }
    }
    
    /**
     * Marriage Stats class
     */
    public static class MarriageStats {
        private long marriedAt;
        private long daysMarried;
        private String ceremonyLocation;
        
        public MarriageStats(long marriedAt, long daysMarried, String ceremonyLocation) {
            this.marriedAt = marriedAt;
            this.daysMarried = daysMarried;
            this.ceremonyLocation = ceremonyLocation;
        }
        
        public long getMarriedAt() { return marriedAt; }
        public long getDaysMarried() { return daysMarried; }
        public String getCeremonyLocation() { return ceremonyLocation; }
    }
}

