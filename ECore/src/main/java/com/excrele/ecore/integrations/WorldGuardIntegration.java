package com.excrele.ecore.integrations;

import com.excrele.ecore.Ecore;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * WorldGuard integration hooks.
 * Provides compatibility and integration with WorldGuard for region protection.
 * Note: WorldGuard dependency is optional. This class will gracefully handle its absence.
 * 
 * @author Excrele
 * @version 1.0
 */
public class WorldGuardIntegration {
    private final Ecore plugin;
    private boolean worldGuardEnabled = false;
    private Object regionContainer; // Using Object to avoid compile-time dependency

    public WorldGuardIntegration(Ecore plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                // Use reflection to avoid compile-time dependency
                Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
                Object worldGuardInstance = worldGuardClass.getMethod("getInstance").invoke(null);
                Object platform = worldGuardInstance.getClass().getMethod("getPlatform").invoke(worldGuardInstance);
                regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);
                worldGuardEnabled = true;
                plugin.getLogger().info("WorldGuard integration enabled!");
            } catch (Exception e) {
                plugin.getLogger().warning("WorldGuard found but integration failed: " + e.getMessage());
            }
        } else {
            plugin.getLogger().info("WorldGuard not found. Region protection features disabled.");
        }
    }

    /**
     * Checks if WorldGuard is enabled and available.
     * 
     * @return true if WorldGuard is available
     */
    public boolean isWorldGuardEnabled() {
        return worldGuardEnabled && regionContainer != null;
    }

    /**
     * Checks if a location is in a protected region.
     * 
     * @param location The location to check
     * @return true if the location is in a protected region
     */
    public boolean isInProtectedRegion(Location location) {
        if (!isWorldGuardEnabled()) {
            return false;
        }
        
        try {
            // Use reflection to check region
            Object world = location.getWorld();
            Object wgWorld = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter")
                .getMethod("asWorld", org.bukkit.World.class).invoke(null, world);
            Object regions = regionContainer.getClass()
                .getMethod("get", Object.class).invoke(regionContainer, wgWorld);
            Object applicableRegions = regions.getClass()
                .getMethod("getApplicableRegions", Object.class).invoke(regions, location);
            int size = (Integer) applicableRegions.getClass().getMethod("size").invoke(applicableRegions);
            return size > 0;
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking WorldGuard region: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a player can build at a location according to WorldGuard.
     * 
     * @param player The player to check
     * @param location The location to check
     * @return true if the player can build at the location
     */
    public boolean canBuild(Player player, Location location) {
        if (!isWorldGuardEnabled()) {
            return true; // No WorldGuard, allow building
        }
        
        try {
            // Use reflection to check build permission
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object worldGuardInstance = worldGuardClass.getMethod("getInstance").invoke(null);
            Object platform = worldGuardInstance.getClass().getMethod("getPlatform").invoke(worldGuardInstance);
            Object sessionManager = platform.getClass().getMethod("getSessionManager").invoke(platform);
            
            Object world = location.getWorld();
            Object wgWorld = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter")
                .getMethod("asWorld", org.bukkit.World.class).invoke(null, world);
            Object wgPlayer = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter")
                .getMethod("asPlayer", org.bukkit.entity.Player.class).invoke(null, player);
            Object wgLocation = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter")
                .getMethod("asBlockVector", Location.class).invoke(null, location);
            
            Boolean hasPermission = (Boolean) sessionManager.getClass()
                .getMethod("hasPermission", Object.class, Object.class, Object.class)
                .invoke(sessionManager, wgWorld, wgPlayer, wgLocation);
            return hasPermission != null && hasPermission;
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking WorldGuard build permission: " + e.getMessage());
            return true; // Default to allowing on error
        }
    }

    /**
     * Gets the WorldGuard region container.
     * 
     * @return The RegionContainer, or null if not available
     */
    public Object getRegionContainer() {
        return regionContainer;
    }
}

