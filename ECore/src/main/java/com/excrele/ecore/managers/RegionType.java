package com.excrele.ecore.managers;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a predefined region type with default flag configurations.
 * Region types simplify region creation by providing sensible defaults.
 * 
 * @author Excrele
 * @version 1.0
 */
public class RegionType {
    private final String name;
    private final String displayName;
    private final String description;
    private final Map<RegionFlag, Boolean> defaultFlags;
    
    /**
     * Creates a new region type with the specified name and default flags.
     * 
     * @param name The internal name of the region type
     * @param displayName The display name shown to players
     * @param description A description of what this region type is for
     * @param defaultFlags A map of default flag values
     */
    public RegionType(String name, String displayName, String description, Map<RegionFlag, Boolean> defaultFlags) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.defaultFlags = new HashMap<>(defaultFlags);
    }
    
    /**
     * Gets the internal name of the region type.
     * 
     * @return The region type name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the display name of the region type.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the description of the region type.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the default flags for this region type.
     * 
     * @return A copy of the default flags map
     */
    public Map<RegionFlag, Boolean> getDefaultFlags() {
        return new HashMap<>(defaultFlags);
    }
    
    /**
     * Gets a default flag value, or null if not set.
     * 
     * @param flag The flag to get
     * @return The flag value, or null if not set
     */
    public Boolean getDefaultFlag(RegionFlag flag) {
        return defaultFlags.get(flag);
    }
    
    /**
     * Creates the default region types.
     * 
     * @return A map of region types by name
     */
    public static Map<String, RegionType> createDefaultTypes() {
        Map<String, RegionType> types = new HashMap<>();
        
        // Spawn region type - safe area for new players
        Map<RegionFlag, Boolean> spawnFlags = new HashMap<>();
        spawnFlags.put(RegionFlag.BUILD, false);
        spawnFlags.put(RegionFlag.BREAK, false);
        spawnFlags.put(RegionFlag.INTERACT, true);
        spawnFlags.put(RegionFlag.PVP, false);
        spawnFlags.put(RegionFlag.MOB_SPAWN, false);
        spawnFlags.put(RegionFlag.USE_ITEMS, false);
        spawnFlags.put(RegionFlag.DAMAGE, false);
        spawnFlags.put(RegionFlag.DROP_ITEMS, true);
        spawnFlags.put(RegionFlag.PICKUP_ITEMS, true);
        spawnFlags.put(RegionFlag.FLY, true);
        spawnFlags.put(RegionFlag.TELEPORT, true);
        spawnFlags.put(RegionFlag.COMMANDS, true);
        spawnFlags.put(RegionFlag.CHAT, true);
        spawnFlags.put(RegionFlag.EXPLOSIONS, false);
        spawnFlags.put(RegionFlag.FIRE_SPREAD, false);
        spawnFlags.put(RegionFlag.ENTRY, true);
        spawnFlags.put(RegionFlag.EXIT, true);
        
        types.put("spawn", new RegionType(
            "spawn",
            "Spawn",
            "A safe spawn area for new players. Building and PvP are disabled.",
            spawnFlags
        ));
        
        // Shop region type - trading area
        Map<RegionFlag, Boolean> shopFlags = new HashMap<>();
        shopFlags.put(RegionFlag.BUILD, false);
        shopFlags.put(RegionFlag.BREAK, false);
        shopFlags.put(RegionFlag.INTERACT, true);
        shopFlags.put(RegionFlag.PVP, false);
        shopFlags.put(RegionFlag.MOB_SPAWN, false);
        shopFlags.put(RegionFlag.USE_ITEMS, false);
        shopFlags.put(RegionFlag.DAMAGE, false);
        shopFlags.put(RegionFlag.DROP_ITEMS, true);
        shopFlags.put(RegionFlag.PICKUP_ITEMS, true);
        shopFlags.put(RegionFlag.FLY, false);
        shopFlags.put(RegionFlag.TELEPORT, true);
        shopFlags.put(RegionFlag.COMMANDS, true);
        shopFlags.put(RegionFlag.CHAT, true);
        shopFlags.put(RegionFlag.EXPLOSIONS, false);
        shopFlags.put(RegionFlag.FIRE_SPREAD, false);
        shopFlags.put(RegionFlag.ENTRY, true);
        shopFlags.put(RegionFlag.EXIT, true);
        
        types.put("shop", new RegionType(
            "shop",
            "Shop",
            "A trading area for shops. Building is disabled but interaction is allowed.",
            shopFlags
        ));
        
        // PvP arena region type - combat area
        Map<RegionFlag, Boolean> pvpFlags = new HashMap<>();
        pvpFlags.put(RegionFlag.BUILD, false);
        pvpFlags.put(RegionFlag.BREAK, false);
        pvpFlags.put(RegionFlag.INTERACT, false);
        pvpFlags.put(RegionFlag.PVP, true);
        pvpFlags.put(RegionFlag.MOB_SPAWN, false);
        pvpFlags.put(RegionFlag.USE_ITEMS, true);
        pvpFlags.put(RegionFlag.DAMAGE, true);
        pvpFlags.put(RegionFlag.DROP_ITEMS, true);
        pvpFlags.put(RegionFlag.PICKUP_ITEMS, true);
        pvpFlags.put(RegionFlag.FLY, false);
        pvpFlags.put(RegionFlag.TELEPORT, true);
        pvpFlags.put(RegionFlag.COMMANDS, true);
        pvpFlags.put(RegionFlag.CHAT, true);
        pvpFlags.put(RegionFlag.EXPLOSIONS, true);
        pvpFlags.put(RegionFlag.FIRE_SPREAD, false);
        pvpFlags.put(RegionFlag.ENTRY, true);
        pvpFlags.put(RegionFlag.EXIT, true);
        
        types.put("pvp", new RegionType(
            "pvp",
            "PvP Arena",
            "A PvP combat area. Building is disabled but PvP is enabled.",
            pvpFlags
        ));
        
        // Protected region type - fully protected area
        Map<RegionFlag, Boolean> protectedFlags = new HashMap<>();
        protectedFlags.put(RegionFlag.BUILD, false);
        protectedFlags.put(RegionFlag.BREAK, false);
        protectedFlags.put(RegionFlag.INTERACT, false);
        protectedFlags.put(RegionFlag.PVP, false);
        protectedFlags.put(RegionFlag.MOB_SPAWN, false);
        protectedFlags.put(RegionFlag.USE_ITEMS, false);
        protectedFlags.put(RegionFlag.DAMAGE, false);
        protectedFlags.put(RegionFlag.DROP_ITEMS, false);
        protectedFlags.put(RegionFlag.PICKUP_ITEMS, false);
        protectedFlags.put(RegionFlag.FLY, false);
        protectedFlags.put(RegionFlag.TELEPORT, false);
        protectedFlags.put(RegionFlag.COMMANDS, true);
        protectedFlags.put(RegionFlag.CHAT, true);
        protectedFlags.put(RegionFlag.EXPLOSIONS, false);
        protectedFlags.put(RegionFlag.FIRE_SPREAD, false);
        protectedFlags.put(RegionFlag.ENTRY, true);
        protectedFlags.put(RegionFlag.EXIT, true);
        
        types.put("protected", new RegionType(
            "protected",
            "Protected",
            "A fully protected area. Most actions are disabled.",
            protectedFlags
        ));
        
        // Custom region type - no defaults, all flags set to allow
        Map<RegionFlag, Boolean> customFlags = new HashMap<>();
        for (RegionFlag flag : RegionFlag.values()) {
            customFlags.put(flag, true);
        }
        
        types.put("custom", new RegionType(
            "custom",
            "Custom",
            "A custom region with all flags enabled by default. Configure as needed.",
            customFlags
        ));
        
        return types;
    }
}

