package com.excrele.ecore.managers;

/**
 * Represents a flag that can be set on a region.
 * Flags control various behaviors within protected regions.
 * 
 * @author Excrele
 * @version 1.0
 */
public enum RegionFlag {
    /**
     * Whether players can build/place blocks in the region
     */
    BUILD("build", "Allow building in region"),
    
    /**
     * Whether players can break blocks in the region
     */
    BREAK("break", "Allow breaking blocks in region"),
    
    /**
     * Whether players can interact with blocks/entities (doors, chests, etc.)
     */
    INTERACT("interact", "Allow interacting with blocks/entities"),
    
    /**
     * Whether PvP is allowed in the region
     */
    PVP("pvp", "Allow PvP in region"),
    
    /**
     * Whether mobs can spawn naturally in the region
     */
    MOB_SPAWN("mob-spawn", "Allow natural mob spawning"),
    
    /**
     * Whether players can use items (e.g., ender pearls, chorus fruit)
     */
    USE_ITEMS("use-items", "Allow using items (ender pearls, etc.)"),
    
    /**
     * Whether players can take damage in the region
     */
    DAMAGE("damage", "Allow players to take damage"),
    
    /**
     * Whether players can drop items in the region
     */
    DROP_ITEMS("drop-items", "Allow dropping items"),
    
    /**
     * Whether players can pick up items in the region
     */
    PICKUP_ITEMS("pickup-items", "Allow picking up items"),
    
    /**
     * Whether players can fly in the region
     */
    FLY("fly", "Allow flying in region"),
    
    /**
     * Whether players can teleport into/out of the region
     */
    TELEPORT("teleport", "Allow teleportation in region"),
    
    /**
     * Whether players can use commands in the region
     */
    COMMANDS("commands", "Allow command usage in region"),
    
    /**
     * Whether players can chat in the region
     */
    CHAT("chat", "Allow chatting in region"),
    
    /**
     * Whether explosions can occur in the region
     */
    EXPLOSIONS("explosions", "Allow explosions"),
    
    /**
     * Whether fire can spread in the region
     */
    FIRE_SPREAD("fire-spread", "Allow fire to spread"),
    
    /**
     * Whether players can enter the region
     */
    ENTRY("entry", "Allow entry into region"),
    
    /**
     * Whether players can exit the region
     */
    EXIT("exit", "Allow exit from region");
    
    private final String name;
    private final String description;
    
    RegionFlag(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    /**
     * Gets the flag name (used in configuration).
     * 
     * @return The flag name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the flag description.
     * 
     * @return The flag description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets a RegionFlag by name (case-insensitive).
     * 
     * @param name The flag name
     * @return The RegionFlag, or null if not found
     */
    public static RegionFlag byName(String name) {
        for (RegionFlag flag : values()) {
            if (flag.name.equalsIgnoreCase(name)) {
                return flag;
            }
        }
        return null;
    }
}

