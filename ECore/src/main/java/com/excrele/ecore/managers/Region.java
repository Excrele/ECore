package com.excrele.ecore.managers;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

/**
 * Represents a protected region in the world.
 * Regions define areas with specific protection rules and flags.
 * 
 * @author Excrele
 * @version 1.0
 */
public class Region {
    private final String name;
    private final World world;
    private final Location min;
    private final Location max;
    private final Map<RegionFlag, Boolean> flags;
    private final Set<UUID> owners;
    private final Set<UUID> members;
    private final String regionType;
    private final UUID creator;
    private final long createdAt;
    
    /**
     * Creates a new region.
     * 
     * @param name The region name (must be unique per world)
     * @param world The world the region is in
     * @param min The minimum corner of the region
     * @param max The maximum corner of the region
     * @param flags The flags for this region
     * @param owners The owners of this region
     * @param members The members of this region
     * @param regionType The type of region (spawn, shop, etc.)
     * @param creator The UUID of the player who created the region
     */
    public Region(String name, World world, Location min, Location max,
                  Map<RegionFlag, Boolean> flags, Set<UUID> owners, Set<UUID> members,
                  String regionType, UUID creator) {
        this.name = name;
        this.world = world;
        this.min = min.clone();
        this.max = max.clone();
        this.flags = new HashMap<>(flags);
        this.owners = new HashSet<>(owners);
        this.members = new HashSet<>(members);
        this.regionType = regionType;
        this.creator = creator;
        this.createdAt = System.currentTimeMillis();
    }
    
    /**
     * Checks if a location is within this region.
     * 
     * @param location The location to check
     * @return true if the location is within the region
     */
    public boolean contains(Location location) {
        if (!location.getWorld().equals(world)) {
            return false;
        }
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        return x >= min.getBlockX() && x <= max.getBlockX() &&
               y >= min.getBlockY() && y <= max.getBlockY() &&
               z >= min.getBlockZ() && z <= max.getBlockZ();
    }
    
    /**
     * Gets the region name.
     * 
     * @return The region name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the world this region is in.
     * 
     * @return The world
     */
    public World getWorld() {
        return world;
    }
    
    /**
     * Gets the minimum corner of the region.
     * 
     * @return The minimum location
     */
    public Location getMin() {
        return min.clone();
    }
    
    /**
     * Gets the maximum corner of the region.
     * 
     * @return The maximum location
     */
    public Location getMax() {
        return max.clone();
    }
    
    /**
     * Gets the volume of the region (number of blocks).
     * 
     * @return The volume
     */
    public int getVolume() {
        int width = max.getBlockX() - min.getBlockX() + 1;
        int height = max.getBlockY() - min.getBlockY() + 1;
        int length = max.getBlockZ() - min.getBlockZ() + 1;
        return width * height * length;
    }
    
    /**
     * Gets a flag value.
     * 
     * @param flag The flag to get
     * @return The flag value, or null if not set
     */
    public Boolean getFlag(RegionFlag flag) {
        return flags.get(flag);
    }
    
    /**
     * Sets a flag value.
     * 
     * @param flag The flag to set
     * @param value The value to set
     */
    public void setFlag(RegionFlag flag, Boolean value) {
        if (value == null) {
            flags.remove(flag);
        } else {
            flags.put(flag, value);
        }
    }
    
    /**
     * Gets all flags for this region.
     * 
     * @return A copy of the flags map
     */
    public Map<RegionFlag, Boolean> getFlags() {
        return new HashMap<>(flags);
    }
    
    /**
     * Checks if a flag is enabled (true).
     * 
     * @param flag The flag to check
     * @return true if the flag is enabled, false if disabled or not set
     */
    public boolean isFlagEnabled(RegionFlag flag) {
        Boolean value = flags.get(flag);
        return value != null && value;
    }
    
    /**
     * Checks if a player is an owner of this region.
     * 
     * @param uuid The player's UUID
     * @return true if the player is an owner
     */
    public boolean isOwner(UUID uuid) {
        return owners.contains(uuid);
    }
    
    /**
     * Checks if a player is a member of this region.
     * 
     * @param uuid The player's UUID
     * @return true if the player is a member
     */
    public boolean isMember(UUID uuid) {
        return members.contains(uuid) || owners.contains(uuid);
    }
    
    /**
     * Adds an owner to the region.
     * 
     * @param uuid The player's UUID
     */
    public void addOwner(UUID uuid) {
        owners.add(uuid);
    }
    
    /**
     * Removes an owner from the region.
     * 
     * @param uuid The player's UUID
     */
    public void removeOwner(UUID uuid) {
        owners.remove(uuid);
    }
    
    /**
     * Adds a member to the region.
     * 
     * @param uuid The player's UUID
     */
    public void addMember(UUID uuid) {
        members.add(uuid);
    }
    
    /**
     * Removes a member from the region.
     * 
     * @param uuid The player's UUID
     */
    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }
    
    /**
     * Gets all owners of the region.
     * 
     * @return A copy of the owners set
     */
    public Set<UUID> getOwners() {
        return new HashSet<>(owners);
    }
    
    /**
     * Gets all members of the region.
     * 
     * @return A copy of the members set
     */
    public Set<UUID> getMembers() {
        return new HashSet<>(members);
    }
    
    /**
     * Gets the region type.
     * 
     * @return The region type name
     */
    public String getRegionType() {
        return regionType;
    }
    
    /**
     * Gets the creator of the region.
     * 
     * @return The creator's UUID
     */
    public UUID getCreator() {
        return creator;
    }
    
    /**
     * Gets the creation timestamp.
     * 
     * @return The creation timestamp in milliseconds
     */
    public long getCreatedAt() {
        return createdAt;
    }
}

