package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Dimension/Realm System Manager
 * Handles player dimensions, guild dimensions, dimension creation, portals, permissions, limits, analytics
 */
public class DimensionManager {
    private final Ecore plugin;
    private File dimensionsFile;
    private FileConfiguration dimensionsConfig;
    private final Map<String, Dimension> dimensions; // Dimension ID -> Dimension
    private final Map<UUID, String> playerDimensions; // Player UUID -> Dimension ID
    private final Map<String, String> guildDimensions; // Guild ID -> Dimension ID
    
    public DimensionManager(Ecore plugin) {
        this.plugin = plugin;
        this.dimensions = new HashMap<>();
        this.playerDimensions = new HashMap<>();
        this.guildDimensions = new HashMap<>();
        initializeConfig();
        loadDimensions();
    }
    
    private void initializeConfig() {
        dimensionsFile = new File(plugin.getDataFolder(), "dimensions.yml");
        if (!dimensionsFile.exists()) {
            try {
                dimensionsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create dimensions.yml", e);
            }
        }
        dimensionsConfig = YamlConfiguration.loadConfiguration(dimensionsFile);
    }
    
    private void loadDimensions() {
        if (dimensionsConfig.contains("dimensions")) {
            for (String dimensionId : dimensionsConfig.getConfigurationSection("dimensions").getKeys(false)) {
                String path = "dimensions." + dimensionId;
                String worldName = dimensionsConfig.getString(path + ".world");
                String ownerType = dimensionsConfig.getString(path + ".owner-type", "player"); // player or guild
                String ownerId = dimensionsConfig.getString(path + ".owner-id");
                int size = dimensionsConfig.getInt(path + ".size", 100);
                boolean unlocked = dimensionsConfig.getBoolean(path + ".unlocked", false);
                
                Dimension dimension = new Dimension(dimensionId, worldName, ownerType, ownerId, size, unlocked);
                dimensions.put(dimensionId, dimension);
                
                if (ownerType.equals("player")) {
                    try {
                        playerDimensions.put(UUID.fromString(ownerId), dimensionId);
                    } catch (IllegalArgumentException e) {
                        // Skip invalid UUIDs
                    }
                } else if (ownerType.equals("guild")) {
                    guildDimensions.put(ownerId, dimensionId);
                }
            }
        }
    }
    
    /**
     * Create a player dimension
     */
    public Dimension createPlayerDimension(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (playerDimensions.containsKey(uuid)) {
            return null; // Player already has a dimension
        }
        
        String dimensionId = "player_" + uuid.toString();
        String worldName = "dimension_" + dimensionId;
        
        // Check if world already exists
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            // Create new world
            WorldCreator creator = new WorldCreator(worldName);
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
            world = creator.createWorld();
            
            if (world == null) {
                return null; // Failed to create world
            }
        }
        
        Dimension dimension = new Dimension(dimensionId, worldName, "player", uuid.toString(), 100, false);
        dimensions.put(dimensionId, dimension);
        playerDimensions.put(uuid, dimensionId);
        
        saveDimension(dimension);
        
        player.sendMessage(org.bukkit.ChatColor.GREEN + "Dimension created! Use /dimension tp to teleport to it.");
        
        return dimension;
    }
    
    /**
     * Create a guild dimension
     */
    public Dimension createGuildDimension(String guildId) {
        if (guildDimensions.containsKey(guildId)) {
            return null; // Guild already has a dimension
        }
        
        String dimensionId = "guild_" + guildId;
        String worldName = "dimension_" + dimensionId;
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            WorldCreator creator = new WorldCreator(worldName);
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
            world = creator.createWorld();
            
            if (world == null) {
                return null;
            }
        }
        
        Dimension dimension = new Dimension(dimensionId, worldName, "guild", guildId, 200, false);
        dimensions.put(dimensionId, dimension);
        guildDimensions.put(guildId, dimensionId);
        
        saveDimension(dimension);
        
        return dimension;
    }
    
    /**
     * Teleport to dimension
     */
    public boolean teleportToDimension(Player player, String dimensionId) {
        Dimension dimension = dimensions.get(dimensionId);
        if (dimension == null) {
            return false;
        }
        
        // Check permissions
        if (!hasAccess(player, dimension)) {
            player.sendMessage(org.bukkit.ChatColor.RED + "You don't have access to this dimension!");
            return false;
        }
        
        World world = Bukkit.getWorld(dimension.getWorldName());
        if (world == null) {
            player.sendMessage(org.bukkit.ChatColor.RED + "Dimension world not found!");
            return false;
        }
        
        Location spawn = world.getSpawnLocation();
        if (plugin.getTeleportManager() != null) {
            plugin.getTeleportManager().teleport(player, spawn);
            player.sendMessage(org.bukkit.ChatColor.GREEN + "Teleported to dimension!");
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if player has access to dimension
     */
    private boolean hasAccess(Player player, Dimension dimension) {
        if (dimension.isUnlocked()) {
            return true; // Public dimension
        }
        
        if (dimension.getOwnerType().equals("player")) {
            return dimension.getOwnerId().equals(player.getUniqueId().toString());
        } else if (dimension.getOwnerType().equals("guild")) {
            if (plugin.getGuildManager() != null) {
                GuildManager.Guild guild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
                return guild != null && guild.getId().equals(dimension.getOwnerId());
            }
        }
        
        return false;
    }
    
    /**
     * Unlock dimension (make it public)
     */
    public boolean unlockDimension(Player player, String dimensionId) {
        Dimension dimension = dimensions.get(dimensionId);
        if (dimension == null) {
            return false;
        }
        
        if (!dimension.getOwnerId().equals(player.getUniqueId().toString()) && 
            !player.hasPermission("ecore.dimension.admin")) {
            return false;
        }
        
        dimension.setUnlocked(true);
        saveDimension(dimension);
        
        return true;
    }
    
    private void saveDimension(Dimension dimension) {
        String path = "dimensions." + dimension.getId();
        dimensionsConfig.set(path + ".world", dimension.getWorldName());
        dimensionsConfig.set(path + ".owner-type", dimension.getOwnerType());
        dimensionsConfig.set(path + ".owner-id", dimension.getOwnerId());
        dimensionsConfig.set(path + ".size", dimension.getSize());
        dimensionsConfig.set(path + ".unlocked", dimension.isUnlocked());
        
        saveConfig();
    }
    
    private void saveConfig() {
        try {
            dimensionsConfig.save(dimensionsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save dimensions.yml", e);
        }
    }
    
    public Dimension getPlayerDimension(UUID playerUuid) {
        String dimensionId = playerDimensions.get(playerUuid);
        return dimensionId != null ? dimensions.get(dimensionId) : null;
    }
    
    public Dimension getGuildDimension(String guildId) {
        String dimensionId = guildDimensions.get(guildId);
        return dimensionId != null ? dimensions.get(dimensionId) : null;
    }
    
    public Dimension getDimension(String dimensionId) {
        return dimensions.get(dimensionId);
    }
    
    /**
     * Dimension class
     */
    public static class Dimension {
        private String id;
        private String worldName;
        private String ownerType; // player or guild
        private String ownerId;
        private int size;
        private boolean unlocked;
        
        public Dimension(String id, String worldName, String ownerType, String ownerId, int size, boolean unlocked) {
            this.id = id;
            this.worldName = worldName;
            this.ownerType = ownerType;
            this.ownerId = ownerId;
            this.size = size;
            this.unlocked = unlocked;
        }
        
        public String getId() { return id; }
        public String getWorldName() { return worldName; }
        public String getOwnerType() { return ownerType; }
        public String getOwnerId() { return ownerId; }
        public int getSize() { return size; }
        public boolean isUnlocked() { return unlocked; }
        public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    }
}

