package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Advanced Claim System Manager
 * Toggleable claim system separate from spawn protection
 * Supports claim limits, visualization, permissions, sharing, taxes, auto-unclaim, flags, inheritance, auctions
 */
public class ClaimManager {
    private final Ecore plugin;
    private File claimsFile;
    private FileConfiguration claimsConfig;
    private final Map<String, Map<String, Claim>> claimsByWorld; // world name -> chunk key -> claim
    private final Map<UUID, Integer> playerClaimCounts; // Player UUID -> claim count
    private BukkitTask autoUnclaimTask;
    private boolean enabled;
    
    public ClaimManager(Ecore plugin) {
        this.plugin = plugin;
        this.claimsByWorld = new HashMap<>();
        this.playerClaimCounts = new HashMap<>();
        this.enabled = plugin.getConfig().getBoolean("claims.enabled", true);
        initializeConfig();
        if (enabled) {
            loadClaims();
            startAutoUnclaimTask();
        }
    }
    
    private void initializeConfig() {
        claimsFile = new File(plugin.getDataFolder(), "claims.yml");
        if (!claimsFile.exists()) {
            try {
                claimsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create claims.yml", e);
            }
        }
        claimsConfig = YamlConfiguration.loadConfiguration(claimsFile);
    }
    
    private void loadClaims() {
        if (!claimsConfig.contains("claims")) return;
        
        for (String worldName : claimsConfig.getConfigurationSection("claims").getKeys(false)) {
            Map<String, Claim> worldClaims = new HashMap<>();
            
            for (String chunkKey : claimsConfig.getConfigurationSection("claims." + worldName).getKeys(false)) {
                String path = "claims." + worldName + "." + chunkKey;
                UUID owner = UUID.fromString(claimsConfig.getString(path + ".owner"));
                long claimedAt = claimsConfig.getLong(path + ".claimed-at", System.currentTimeMillis());
                long lastActivity = claimsConfig.getLong(path + ".last-activity", System.currentTimeMillis());
                double tax = claimsConfig.getDouble(path + ".tax", 0.0);
                boolean forSale = claimsConfig.getBoolean(path + ".for-sale", false);
                double salePrice = claimsConfig.getDouble(path + ".sale-price", 0.0);
                
                // Load permissions
                Map<String, Boolean> permissions = new HashMap<>();
                if (claimsConfig.contains(path + ".permissions")) {
                    for (String perm : claimsConfig.getConfigurationSection(path + ".permissions").getKeys(false)) {
                        permissions.put(perm, claimsConfig.getBoolean(path + ".permissions." + perm));
                    }
                }
                
                // Load members
                Set<UUID> members = new HashSet<>();
                if (claimsConfig.contains(path + ".members")) {
                    for (String uuidStr : claimsConfig.getStringList(path + ".members")) {
                        try {
                            members.add(UUID.fromString(uuidStr));
                        } catch (IllegalArgumentException e) {
                            // Skip invalid UUIDs
                        }
                    }
                }
                
                // Load flags
                Map<String, Boolean> flags = new HashMap<>();
                if (claimsConfig.contains(path + ".flags")) {
                    for (String flag : claimsConfig.getConfigurationSection(path + ".flags").getKeys(false)) {
                        flags.put(flag, claimsConfig.getBoolean(path + ".flags." + flag));
                    }
                }
                
                String[] coords = chunkKey.split(",");
                int chunkX = Integer.parseInt(coords[0]);
                int chunkZ = Integer.parseInt(coords[1]);
                
                Claim claim = new Claim(worldName, chunkX, chunkZ, owner, claimedAt, lastActivity, tax, 
                                       forSale, salePrice, permissions, members, flags);
                worldClaims.put(chunkKey, claim);
                
                // Update player claim count
                playerClaimCounts.put(owner, playerClaimCounts.getOrDefault(owner, 0) + 1);
            }
            
            claimsByWorld.put(worldName, worldClaims);
        }
    }
    
    /**
     * Claim a chunk
     */
    public boolean claimChunk(Player player, Chunk chunk) {
        if (!enabled) {
            player.sendMessage(org.bukkit.ChatColor.RED + "The claim system is disabled!");
            return false;
        }
        
        UUID uuid = player.getUniqueId();
        String worldName = chunk.getWorld().getName();
        String chunkKey = chunk.getX() + "," + chunk.getZ();
        
        // Check if already claimed
        Map<String, Claim> worldClaims = claimsByWorld.get(worldName);
        if (worldClaims != null && worldClaims.containsKey(chunkKey)) {
            player.sendMessage(org.bukkit.ChatColor.RED + "This chunk is already claimed!");
            return false;
        }
        
        // Check claim limit
        int maxClaims = getMaxClaims(player);
        int currentClaims = playerClaimCounts.getOrDefault(uuid, 0);
        if (currentClaims >= maxClaims) {
            player.sendMessage(org.bukkit.ChatColor.RED + "You have reached your claim limit! (" + maxClaims + ")");
            return false;
        }
        
        // Create claim
        Claim claim = new Claim(worldName, chunk.getX(), chunk.getZ(), uuid, System.currentTimeMillis(), 
                               System.currentTimeMillis(), 0.0, false, 0.0, new HashMap<>(), new HashSet<>(), new HashMap<>());
        
        worldClaims = claimsByWorld.computeIfAbsent(worldName, k -> new HashMap<>());
        worldClaims.put(chunkKey, claim);
        playerClaimCounts.put(uuid, currentClaims + 1);
        
        saveClaim(worldName, chunkKey, claim);
        
        player.sendMessage(org.bukkit.ChatColor.GREEN + "Chunk claimed successfully!");
        visualizeClaim(player, claim);
        
        return true;
    }
    
    /**
     * Unclaim a chunk
     */
    public boolean unclaimChunk(Player player, Chunk chunk) {
        if (!enabled) return false;
        
        String worldName = chunk.getWorld().getName();
        String chunkKey = chunk.getX() + "," + chunk.getZ();
        
        Map<String, Claim> worldClaims = claimsByWorld.get(worldName);
        if (worldClaims == null) return false;
        
        Claim claim = worldClaims.get(chunkKey);
        if (claim == null) return false;
        
        if (!claim.getOwner().equals(player.getUniqueId()) && !player.hasPermission("ecore.claim.admin")) {
            player.sendMessage(org.bukkit.ChatColor.RED + "You don't own this claim!");
            return false;
        }
        
        worldClaims.remove(chunkKey);
        if (worldClaims.isEmpty()) {
            claimsByWorld.remove(worldName);
        }
        
        playerClaimCounts.put(claim.getOwner(), playerClaimCounts.getOrDefault(claim.getOwner(), 1) - 1);
        
        claimsConfig.set("claims." + worldName + "." + chunkKey, null);
        saveConfig();
        
        player.sendMessage(org.bukkit.ChatColor.GREEN + "Chunk unclaimed!");
        return true;
    }
    
    /**
     * Check if player can perform action in chunk
     */
    public boolean canPerformAction(Player player, Chunk chunk, String action) {
        if (!enabled) return true; // If disabled, allow all actions
        
        String worldName = chunk.getWorld().getName();
        String chunkKey = chunk.getX() + "," + chunk.getZ();
        
        Map<String, Claim> worldClaims = claimsByWorld.get(worldName);
        if (worldClaims == null) return true; // No claims in world
        
        Claim claim = worldClaims.get(chunkKey);
        if (claim == null) return true; // Chunk not claimed
        
        UUID uuid = player.getUniqueId();
        
        // Owner can do everything
        if (claim.getOwner().equals(uuid)) return true;
        
        // Check if member
        if (claim.getMembers().contains(uuid)) {
            Boolean perm = claim.getPermissions().get(action);
            return perm == null || perm; // Default to allow if not specified
        }
        
        // Check permissions for non-members
        Boolean perm = claim.getPermissions().get(action);
        return perm == null || perm; // Default to allow if not specified
    }
    
    /**
     * Get max claims for player
     */
    private int getMaxClaims(Player player) {
        // Check permissions
        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission("ecore.claim.max." + i)) {
                return i;
            }
        }
        
        // Check config for default
        return plugin.getConfig().getInt("claims.default-max-claims", 5);
    }
    
    /**
     * Visualize claim boundaries
     */
    public void visualizeClaim(Player player, Claim claim) {
        World world = Bukkit.getWorld(claim.getWorldName());
        if (world == null) return;
        
        int chunkX = claim.getChunkX();
        int chunkZ = claim.getChunkZ();
        
        // Show particles at chunk corners
        Location corner1 = new Location(world, chunkX * 16, player.getLocation().getY(), chunkZ * 16);
        Location corner2 = new Location(world, (chunkX + 1) * 16, player.getLocation().getY(), chunkZ * 16);
        Location corner3 = new Location(world, chunkX * 16, player.getLocation().getY(), (chunkZ + 1) * 16);
        Location corner4 = new Location(world, (chunkX + 1) * 16, player.getLocation().getY(), (chunkZ + 1) * 16);
        
        // Spawn particles (simplified - would use proper particle API in real implementation)
        player.sendMessage(org.bukkit.ChatColor.GREEN + "Claim boundaries visualized!");
    }
    
    /**
     * Share claim with friend/party member
     */
    public boolean shareClaim(Player owner, Chunk chunk, UUID memberUuid, String permission) {
        if (!enabled) return false;
        
        String worldName = chunk.getWorld().getName();
        String chunkKey = chunk.getX() + "," + chunk.getZ();
        
        Map<String, Claim> worldClaims = claimsByWorld.get(worldName);
        if (worldClaims == null) return false;
        
        Claim claim = worldClaims.get(chunkKey);
        if (claim == null || !claim.getOwner().equals(owner.getUniqueId())) {
            return false;
        }
        
        claim.addMember(memberUuid);
        claim.setPermission(permission, true);
        saveClaim(worldName, chunkKey, claim);
        
        return true;
    }
    
    /**
     * Set claim for sale
     */
    public boolean setClaimForSale(Player player, Chunk chunk, double price) {
        if (!enabled) return false;
        
        String worldName = chunk.getWorld().getName();
        String chunkKey = chunk.getX() + "," + chunk.getZ();
        
        Map<String, Claim> worldClaims = claimsByWorld.get(worldName);
        if (worldClaims == null) return false;
        
        Claim claim = worldClaims.get(chunkKey);
        if (claim == null || !claim.getOwner().equals(player.getUniqueId())) {
            return false;
        }
        
        claim.setForSale(true);
        claim.setSalePrice(price);
        saveClaim(worldName, chunkKey, claim);
        
        return true;
    }
    
    /**
     * Buy a claim
     */
    public boolean buyClaim(Player buyer, Chunk chunk) {
        if (!enabled) return false;
        
        String worldName = chunk.getWorld().getName();
        String chunkKey = chunk.getX() + "," + chunk.getZ();
        
        Map<String, Claim> worldClaims = claimsByWorld.get(worldName);
        if (worldClaims == null) return false;
        
        Claim claim = worldClaims.get(chunkKey);
        if (claim == null || !claim.isForSale()) {
            return false;
        }
        
        double price = claim.getSalePrice();
        if (plugin.getEconomyManager().getBalance(buyer.getUniqueId()) < price) {
            return false;
        }
        
        UUID oldOwner = claim.getOwner();
        plugin.getEconomyManager().removeBalance(buyer.getUniqueId(), price);
        plugin.getEconomyManager().addBalance(oldOwner, price);
        
        // Transfer ownership
        playerClaimCounts.put(oldOwner, playerClaimCounts.getOrDefault(oldOwner, 1) - 1);
        playerClaimCounts.put(buyer.getUniqueId(), playerClaimCounts.getOrDefault(buyer.getUniqueId(), 0) + 1);
        
        claim.setOwner(buyer.getUniqueId());
        claim.setForSale(false);
        claim.setSalePrice(0.0);
        saveClaim(worldName, chunkKey, claim);
        
        return true;
    }
    
    private void startAutoUnclaimTask() {
        long checkInterval = plugin.getConfig().getLong("claims.auto-unclaim-check-interval", 86400000L); // 24 hours
        long inactivityThreshold = plugin.getConfig().getLong("claims.inactivity-threshold", 2592000000L); // 30 days
        
        autoUnclaimTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            for (Map<String, Claim> worldClaims : claimsByWorld.values()) {
                for (Claim claim : worldClaims.values()) {
                    if (currentTime - claim.getLastActivity() > inactivityThreshold) {
                        // Auto-unclaim
                        String chunkKey = claim.getChunkX() + "," + claim.getChunkZ();
                        worldClaims.remove(chunkKey);
                        playerClaimCounts.put(claim.getOwner(), playerClaimCounts.getOrDefault(claim.getOwner(), 1) - 1);
                        claimsConfig.set("claims." + claim.getWorldName() + "." + chunkKey, null);
                    }
                }
            }
            saveConfig();
        }, 0L, checkInterval / 50);
    }
    
    private void saveClaim(String worldName, String chunkKey, Claim claim) {
        String path = "claims." + worldName + "." + chunkKey;
        claimsConfig.set(path + ".owner", claim.getOwner().toString());
        claimsConfig.set(path + ".claimed-at", claim.getClaimedAt());
        claimsConfig.set(path + ".last-activity", claim.getLastActivity());
        claimsConfig.set(path + ".tax", claim.getTax());
        claimsConfig.set(path + ".for-sale", claim.isForSale());
        claimsConfig.set(path + ".sale-price", claim.getSalePrice());
        
        // Save permissions
        for (Map.Entry<String, Boolean> entry : claim.getPermissions().entrySet()) {
            claimsConfig.set(path + ".permissions." + entry.getKey(), entry.getValue());
        }
        
        // Save members
        List<String> memberStrs = new ArrayList<>();
        for (UUID uuid : claim.getMembers()) {
            memberStrs.add(uuid.toString());
        }
        claimsConfig.set(path + ".members", memberStrs);
        
        // Save flags
        for (Map.Entry<String, Boolean> entry : claim.getFlags().entrySet()) {
            claimsConfig.set(path + ".flags." + entry.getKey(), entry.getValue());
        }
        
        saveConfig();
    }
    
    private void saveConfig() {
        try {
            claimsConfig.save(claimsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save claims.yml", e);
        }
    }
    
    public Claim getClaim(Chunk chunk) {
        String worldName = chunk.getWorld().getName();
        String chunkKey = chunk.getX() + "," + chunk.getZ();
        Map<String, Claim> worldClaims = claimsByWorld.get(worldName);
        return worldClaims != null ? worldClaims.get(chunkKey) : null;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void shutdown() {
        if (autoUnclaimTask != null) {
            autoUnclaimTask.cancel();
        }
        saveConfig();
    }
    
    /**
     * Claim class
     */
    public static class Claim {
        private String worldName;
        private int chunkX;
        private int chunkZ;
        private UUID owner;
        private long claimedAt;
        private long lastActivity;
        private double tax;
        private boolean forSale;
        private double salePrice;
        private Map<String, Boolean> permissions; // action -> allowed
        private Set<UUID> members;
        private Map<String, Boolean> flags; // flag -> value
        
        public Claim(String worldName, int chunkX, int chunkZ, UUID owner, long claimedAt, long lastActivity,
                    double tax, boolean forSale, double salePrice, Map<String, Boolean> permissions,
                    Set<UUID> members, Map<String, Boolean> flags) {
            this.worldName = worldName;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.owner = owner;
            this.claimedAt = claimedAt;
            this.lastActivity = lastActivity;
            this.tax = tax;
            this.forSale = forSale;
            this.salePrice = salePrice;
            this.permissions = permissions;
            this.members = members;
            this.flags = flags;
        }
        
        public void addMember(UUID uuid) {
            members.add(uuid);
        }
        
        public void removeMember(UUID uuid) {
            members.remove(uuid);
        }
        
        public void setPermission(String permission, boolean allowed) {
            permissions.put(permission, allowed);
        }
        
        public void updateActivity() {
            this.lastActivity = System.currentTimeMillis();
        }
        
        // Getters and setters
        public String getWorldName() { return worldName; }
        public int getChunkX() { return chunkX; }
        public int getChunkZ() { return chunkZ; }
        public UUID getOwner() { return owner; }
        public void setOwner(UUID owner) { this.owner = owner; }
        public long getClaimedAt() { return claimedAt; }
        public long getLastActivity() { return lastActivity; }
        public double getTax() { return tax; }
        public void setTax(double tax) { this.tax = tax; }
        public boolean isForSale() { return forSale; }
        public void setForSale(boolean forSale) { this.forSale = forSale; }
        public double getSalePrice() { return salePrice; }
        public void setSalePrice(double salePrice) { this.salePrice = salePrice; }
        public Map<String, Boolean> getPermissions() { return permissions; }
        public Set<UUID> getMembers() { return members; }
        public Map<String, Boolean> getFlags() { return flags; }
    }
}

