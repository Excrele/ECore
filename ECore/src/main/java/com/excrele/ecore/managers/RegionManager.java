package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages regions and their protection rules.
 * Handles region creation, deletion, storage, and queries.
 * 
 * @author Excrele
 * @version 1.0
 */
public class RegionManager {
    private final Ecore plugin;
    private final Map<String, Map<String, Region>> regionsByWorld; // world name -> region name -> region
    private final Map<String, RegionType> regionTypes;
    private File regionsFile;
    private FileConfiguration regionsConfig;
    
    /**
     * Creates a new RegionManager instance.
     * 
     * @param plugin The Ecore plugin instance
     */
    public RegionManager(Ecore plugin) {
        this.plugin = plugin;
        this.regionsByWorld = new HashMap<>();
        this.regionTypes = RegionType.createDefaultTypes();
        initializeRegionsFile();
        loadRegions();
    }
    
    /**
     * Initializes the regions configuration file.
     */
    private void initializeRegionsFile() {
        regionsFile = new File(plugin.getDataFolder(), "regions.yml");
        if (!regionsFile.exists()) {
            try {
                regionsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create regions.yml: " + e.getMessage());
            }
        }
        regionsConfig = YamlConfiguration.loadConfiguration(regionsFile);
    }
    
    /**
     * Loads all regions from the configuration file.
     */
    public void loadRegions() {
        regionsByWorld.clear();
        
        if (!regionsConfig.contains("regions")) {
            return;
        }
        
        for (String worldName : regionsConfig.getConfigurationSection("regions").getKeys(false)) {
            Map<String, Region> worldRegions = new HashMap<>();
            
            for (String regionName : regionsConfig.getConfigurationSection("regions." + worldName).getKeys(false)) {
                String path = "regions." + worldName + "." + regionName;
                
                try {
                    World world = plugin.getServer().getWorld(worldName);
                    if (world == null) {
                        plugin.getLogger().warning("World '" + worldName + "' not found for region '" + regionName + "'. Skipping.");
                        continue;
                    }
                    
                    Location min = new Location(
                        world,
                        regionsConfig.getDouble(path + ".min.x"),
                        regionsConfig.getDouble(path + ".min.y"),
                        regionsConfig.getDouble(path + ".min.z")
                    );
                    
                    Location max = new Location(
                        world,
                        regionsConfig.getDouble(path + ".max.x"),
                        regionsConfig.getDouble(path + ".max.y"),
                        regionsConfig.getDouble(path + ".max.z")
                    );
                    
                    Map<RegionFlag, Boolean> flags = new HashMap<>();
                    if (regionsConfig.contains(path + ".flags")) {
                        for (String flagName : regionsConfig.getConfigurationSection(path + ".flags").getKeys(false)) {
                            RegionFlag flag = RegionFlag.byName(flagName);
                            if (flag != null) {
                                flags.put(flag, regionsConfig.getBoolean(path + ".flags." + flagName));
                            }
                        }
                    }
                    
                    Set<UUID> owners = new HashSet<>();
                    if (regionsConfig.contains(path + ".owners")) {
                        for (String ownerStr : regionsConfig.getStringList(path + ".owners")) {
                            try {
                                owners.add(UUID.fromString(ownerStr));
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Invalid owner UUID in region '" + regionName + "': " + ownerStr);
                            }
                        }
                    }
                    
                    Set<UUID> members = new HashSet<>();
                    if (regionsConfig.contains(path + ".members")) {
                        for (String memberStr : regionsConfig.getStringList(path + ".members")) {
                            try {
                                members.add(UUID.fromString(memberStr));
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Invalid member UUID in region '" + regionName + "': " + memberStr);
                            }
                        }
                    }
                    
                    String regionType = regionsConfig.getString(path + ".type", "custom");
                    UUID creator = null;
                    if (regionsConfig.contains(path + ".creator")) {
                        try {
                            creator = UUID.fromString(regionsConfig.getString(path + ".creator"));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid creator UUID in region '" + regionName + "'");
                        }
                    }
                    
                    Region region = new Region(regionName, world, min, max, flags, owners, members, regionType, creator);
                    
                    // Load rent/sell data
                    if (regionsConfig.contains(path + ".for-sale")) {
                        region.setForSale(regionsConfig.getBoolean(path + ".for-sale"));
                        region.setSalePrice(regionsConfig.getDouble(path + ".sale-price", 0.0));
                    }
                    if (regionsConfig.contains(path + ".for-rent")) {
                        region.setForRent(regionsConfig.getBoolean(path + ".for-rent"));
                        region.setRentPrice(regionsConfig.getDouble(path + ".rent-price", 0.0));
                        region.setRentDuration(regionsConfig.getLong(path + ".rent-duration", 0L));
                    }
                    if (regionsConfig.contains(path + ".renter")) {
                        try {
                            UUID renter = UUID.fromString(regionsConfig.getString(path + ".renter"));
                            region.setRenter(renter);
                            region.setRentExpires(regionsConfig.getLong(path + ".rent-expires", 0L));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid renter UUID in region '" + regionName + "'");
                        }
                    }
                    
                    worldRegions.put(regionName, region);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load region '" + regionName + "' in world '" + worldName + "': " + e.getMessage());
                }
            }
            
            regionsByWorld.put(worldName, worldRegions);
        }
        
        plugin.getLogger().info("Loaded " + getTotalRegionCount() + " regions from " + regionsByWorld.size() + " worlds.");
    }
    
    /**
     * Saves all regions to the configuration file.
     */
    public void saveRegions() {
        regionsConfig.set("regions", null);
        
        for (Map.Entry<String, Map<String, Region>> worldEntry : regionsByWorld.entrySet()) {
            String worldName = worldEntry.getKey();
            Map<String, Region> worldRegions = worldEntry.getValue();
            
            for (Map.Entry<String, Region> regionEntry : worldRegions.entrySet()) {
                String regionName = regionEntry.getKey();
                Region region = regionEntry.getValue();
                String path = "regions." + worldName + "." + regionName;
                
                regionsConfig.set(path + ".min.x", region.getMin().getX());
                regionsConfig.set(path + ".min.y", region.getMin().getY());
                regionsConfig.set(path + ".min.z", region.getMin().getZ());
                
                regionsConfig.set(path + ".max.x", region.getMax().getX());
                regionsConfig.set(path + ".max.y", region.getMax().getY());
                regionsConfig.set(path + ".max.z", region.getMax().getZ());
                
                for (Map.Entry<RegionFlag, Boolean> flagEntry : region.getFlags().entrySet()) {
                    regionsConfig.set(path + ".flags." + flagEntry.getKey().getName(), flagEntry.getValue());
                }
                
                List<String> ownerList = new ArrayList<>();
                for (UUID owner : region.getOwners()) {
                    ownerList.add(owner.toString());
                }
                regionsConfig.set(path + ".owners", ownerList);
                
                List<String> memberList = new ArrayList<>();
                for (UUID member : region.getMembers()) {
                    memberList.add(member.toString());
                }
                regionsConfig.set(path + ".members", memberList);
                
                regionsConfig.set(path + ".type", region.getRegionType());
                if (region.getCreator() != null) {
                    regionsConfig.set(path + ".creator", region.getCreator().toString());
                }
                
                // Save rent/sell data
                regionsConfig.set(path + ".for-sale", region.isForSale());
                regionsConfig.set(path + ".sale-price", region.getSalePrice());
                regionsConfig.set(path + ".for-rent", region.isForRent());
                regionsConfig.set(path + ".rent-price", region.getRentPrice());
                regionsConfig.set(path + ".rent-duration", region.getRentDuration());
                if (region.getRenter() != null) {
                    regionsConfig.set(path + ".renter", region.getRenter().toString());
                    regionsConfig.set(path + ".rent-expires", region.getRentExpires());
                }
            }
        }
        
        try {
            regionsConfig.save(regionsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save regions.yml: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new region.
     * 
     * @param name The region name
     * @param world The world
     * @param min The minimum corner
     * @param max The maximum corner
     * @param regionType The region type (spawn, shop, etc.)
     * @param creator The creator's UUID
     * @return The created region, or null if a region with that name already exists
     */
    public Region createRegion(String name, World world, Location min, Location max, String regionType, UUID creator) {
        Map<String, Region> worldRegions = regionsByWorld.computeIfAbsent(world.getName(), k -> new HashMap<>());
        
        if (worldRegions.containsKey(name)) {
            return null; // Region already exists
        }
        
        Map<RegionFlag, Boolean> flags = new HashMap<>();
        RegionType type = regionTypes.get(regionType.toLowerCase());
        if (type != null) {
            flags = type.getDefaultFlags();
        } else {
            // Default: all flags enabled
            for (RegionFlag flag : RegionFlag.values()) {
                flags.put(flag, true);
            }
        }
        
        Region region = new Region(name, world, min, max, flags, new HashSet<>(), new HashSet<>(), regionType, creator);
        worldRegions.put(name, region);
        saveRegions();
        
        return region;
    }
    
    /**
     * Deletes a region.
     * 
     * @param world The world
     * @param name The region name
     * @return true if the region was deleted, false if it didn't exist
     */
    public boolean deleteRegion(World world, String name) {
        Map<String, Region> worldRegions = regionsByWorld.get(world.getName());
        if (worldRegions == null) {
            return false;
        }
        
        if (worldRegions.remove(name) != null) {
            if (worldRegions.isEmpty()) {
                regionsByWorld.remove(world.getName());
            }
            saveRegions();
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets a region by name.
     * 
     * @param world The world
     * @param name The region name
     * @return The region, or null if not found
     */
    public Region getRegion(World world, String name) {
        Map<String, Region> worldRegions = regionsByWorld.get(world.getName());
        if (worldRegions == null) {
            return null;
        }
        return worldRegions.get(name);
    }
    
    /**
     * Gets all regions in a world.
     * 
     * @param world The world
     * @return A collection of all regions in the world
     */
    public Collection<Region> getRegions(World world) {
        Map<String, Region> worldRegions = regionsByWorld.get(world.getName());
        if (worldRegions == null) {
            return Collections.emptyList();
        }
        return worldRegions.values();
    }
    
    /**
     * Gets all regions at a specific location.
     * 
     * @param location The location to check
     * @return A list of regions containing the location (ordered by priority)
     */
    public List<Region> getRegionsAt(Location location) {
        List<Region> regions = new ArrayList<>();
        Map<String, Region> worldRegions = regionsByWorld.get(location.getWorld().getName());
        
        if (worldRegions != null) {
            for (Region region : worldRegions.values()) {
                if (region.contains(location)) {
                    regions.add(region);
                }
            }
        }
        
        return regions;
    }
    
    /**
     * Gets the most specific region at a location (smallest volume).
     * 
     * @param location The location to check
     * @return The most specific region, or null if none found
     */
    public Region getRegionAt(Location location) {
        List<Region> regions = getRegionsAt(location);
        if (regions.isEmpty()) {
            return null;
        }
        
        // Return the smallest region (most specific)
        regions.sort(Comparator.comparingInt(Region::getVolume));
        return regions.get(0);
    }
    
    /**
     * Checks if a player can perform an action at a location based on region flags.
     * 
     * @param player The player
     * @param location The location
     * @param flag The flag to check
     * @return true if the action is allowed
     */
    public boolean canPerformAction(Player player, Location location, RegionFlag flag) {
        // Staff with bypass permission can always perform actions
        if (player.hasPermission("ecore.region.bypass")) {
            return true;
        }
        
        Region region = getRegionAt(location);
        if (region == null) {
            return true; // No region, allow action
        }
        
        // Owners and members can always perform actions in their regions
        if (region.isOwner(player.getUniqueId()) || region.isMember(player.getUniqueId())) {
            return true;
        }
        
        // Check the flag
        Boolean flagValue = region.getFlag(flag);
        return flagValue == null || flagValue; // Default to allow if not set
    }
    
    /**
     * Gets all available region types.
     * 
     * @return A collection of all region types
     */
    public Collection<RegionType> getRegionTypes() {
        return regionTypes.values();
    }
    
    /**
     * Gets a region type by name.
     * 
     * @param name The region type name
     * @return The region type, or null if not found
     */
    public RegionType getRegionType(String name) {
        return regionTypes.get(name.toLowerCase());
    }
    
    /**
     * Gets the total number of regions across all worlds.
     * 
     * @return The total region count
     */
    public int getTotalRegionCount() {
        int count = 0;
        for (Map<String, Region> worldRegions : regionsByWorld.values()) {
            count += worldRegions.size();
        }
        return count;
    }
    
    /**
     * Visualizes a region by showing particles at the borders.
     * 
     * @param player The player to show particles to
     * @param region The region to visualize
     */
    public void visualizeRegion(Player player, Region region) {
        if (!player.getWorld().equals(region.getWorld())) {
            player.sendMessage("§cYou must be in the same world as the region!");
            return;
        }
        
        Location min = region.getMin();
        Location max = region.getMax();
        
        // Show particles at corners and edges
        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();
        
        // Spawn particles along edges
        for (int x = minX; x <= maxX; x += 2) {
            spawnParticle(player, new Location(region.getWorld(), x, minY, minZ));
            spawnParticle(player, new Location(region.getWorld(), x, maxY, minZ));
            spawnParticle(player, new Location(region.getWorld(), x, minY, maxZ));
            spawnParticle(player, new Location(region.getWorld(), x, maxY, maxZ));
        }
        
        for (int y = minY; y <= maxY; y += 2) {
            spawnParticle(player, new Location(region.getWorld(), minX, y, minZ));
            spawnParticle(player, new Location(region.getWorld(), maxX, y, minZ));
            spawnParticle(player, new Location(region.getWorld(), minX, y, maxZ));
            spawnParticle(player, new Location(region.getWorld(), maxX, y, maxZ));
        }
        
        for (int z = minZ; z <= maxZ; z += 2) {
            spawnParticle(player, new Location(region.getWorld(), minX, minY, z));
            spawnParticle(player, new Location(region.getWorld(), maxX, minY, z));
            spawnParticle(player, new Location(region.getWorld(), minX, maxY, z));
            spawnParticle(player, new Location(region.getWorld(), maxX, maxY, z));
        }
    }
    
    private void spawnParticle(Player player, Location location) {
        try {
            // Use FLAME particle for compatibility
            player.spawnParticle(org.bukkit.Particle.FLAME, location, 1);
        } catch (Exception e) {
            // If particles don't work, just skip
        }
    }
    
    /**
     * Buys a region for a player.
     * 
     * @param player The player buying the region
     * @param region The region to buy
     * @return true if successful
     */
    public boolean buyRegion(Player player, Region region) {
        if (!region.isForSale()) {
            player.sendMessage("§cThis region is not for sale!");
            return false;
        }
        
        double price = region.getSalePrice();
        if (price <= 0) {
            player.sendMessage("§cInvalid sale price!");
            return false;
        }
        
        if (plugin.getEconomyManager().getBalance(player.getUniqueId()) < price) {
            player.sendMessage("§cYou don't have enough money! Required: §e" + 
                    String.format("%.2f", price));
            return false;
        }
        
        // Transfer ownership
        UUID oldOwner = region.getCreator();
        if (oldOwner != null && plugin.getServer().getOfflinePlayer(oldOwner).isOnline()) {
            Player oldOwnerPlayer = plugin.getServer().getPlayer(oldOwner);
            if (oldOwnerPlayer != null) {
                plugin.getEconomyManager().addBalance(oldOwnerPlayer.getUniqueId(), price);
                oldOwnerPlayer.sendMessage("§aYour region '" + region.getName() + "' was sold to " + 
                        player.getName() + " for " + String.format("%.2f", price));
            } else {
                plugin.getEconomyManager().addBalance(oldOwner, price);
            }
        }
        
        plugin.getEconomyManager().removeBalance(player.getUniqueId(), price);
        
        // Transfer ownership
        region.getOwners().clear();
        region.addOwner(player.getUniqueId());
        region.setForSale(false);
        region.setSalePrice(0.0);
        saveRegions();
        
        player.sendMessage("§aYou bought the region '" + region.getName() + "' for " + 
                String.format("%.2f", price));
        return true;
    }
    
    /**
     * Rents a region for a player.
     * 
     * @param player The player renting the region
     * @param region The region to rent
     * @return true if successful
     */
    public boolean rentRegion(Player player, Region region) {
        if (!region.isForRent()) {
            player.sendMessage("§cThis region is not for rent!");
            return false;
        }
        
        if (region.isRented()) {
            player.sendMessage("§cThis region is already rented!");
            return false;
        }
        
        double price = region.getRentPrice();
        if (price <= 0) {
            player.sendMessage("§cInvalid rent price!");
            return false;
        }
        
        if (plugin.getEconomyManager().getBalance(player.getUniqueId()) < price) {
            player.sendMessage("§cYou don't have enough money! Required: §e" + 
                    String.format("%.2f", price));
            return false;
        }
        
        UUID owner = region.getCreator();
        if (owner != null && plugin.getServer().getOfflinePlayer(owner).isOnline()) {
            Player ownerPlayer = plugin.getServer().getPlayer(owner);
            if (ownerPlayer != null) {
                plugin.getEconomyManager().addBalance(ownerPlayer.getUniqueId(), price);
                ownerPlayer.sendMessage("§aYour region '" + region.getName() + "' was rented by " + 
                        player.getName() + " for " + String.format("%.2f", price));
            } else {
                plugin.getEconomyManager().addBalance(owner, price);
            }
        }
        
        plugin.getEconomyManager().removeBalance(player.getUniqueId(), price);
        
        region.setRenter(player.getUniqueId());
        saveRegions();
        
        player.sendMessage("§aYou rented the region '" + region.getName() + "' for " + 
                String.format("%.2f", price));
        return true;
    }
}

