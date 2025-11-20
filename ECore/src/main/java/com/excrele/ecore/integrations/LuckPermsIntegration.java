package com.excrele.ecore.integrations;

import com.excrele.ecore.Ecore;
import org.bukkit.entity.Player;

/**
 * LuckPerms integration hooks.
 * Provides compatibility and integration with LuckPerms for permissions and groups.
 * Note: LuckPerms dependency is optional. This class will gracefully handle its absence.
 * 
 * @author Excrele
 * @version 1.0
 */
public class LuckPermsIntegration {
    private final Ecore plugin;
    private boolean luckPermsEnabled = false;
    private Object luckPerms; // Using Object to avoid compile-time dependency

    public LuckPermsIntegration(Ecore plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            try {
                // Use reflection to avoid compile-time dependency
                Class<?> luckPermsProviderClass = Class.forName("net.luckperms.api.LuckPermsProvider");
                luckPerms = luckPermsProviderClass.getMethod("get").invoke(null);
                luckPermsEnabled = true;
                plugin.getLogger().info("LuckPerms integration enabled!");
            } catch (Exception e) {
                plugin.getLogger().warning("LuckPerms found but integration failed: " + e.getMessage());
            }
        } else {
            plugin.getLogger().info("LuckPerms not found. Using default permission system.");
        }
    }

    /**
     * Checks if LuckPerms is enabled and available.
     * 
     * @return true if LuckPerms is available
     */
    public boolean isLuckPermsEnabled() {
        return luckPermsEnabled && luckPerms != null;
    }

    /**
     * Gets the primary group of a player.
     * 
     * @param player The player
     * @return The primary group name, or "default" if not available
     */
    public String getPrimaryGroup(Player player) {
        if (!isLuckPermsEnabled()) {
            return "default";
        }
        
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", java.util.UUID.class)
                .invoke(userManager, player.getUniqueId());
            if (user != null) {
                return (String) user.getClass().getMethod("getPrimaryGroup").invoke(user);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting LuckPerms primary group: " + e.getMessage());
        }
        return "default";
    }

    /**
     * Gets the prefix of a player.
     * 
     * @param player The player
     * @return The prefix, or empty string if not available
     */
    public String getPrefix(Player player) {
        if (!isLuckPermsEnabled()) {
            return "";
        }
        
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", java.util.UUID.class)
                .invoke(userManager, player.getUniqueId());
            if (user != null) {
                Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
                Object permissionData = cachedData.getClass()
                    .getMethod("getPermissionData", Object.class).invoke(cachedData, 
                        Class.forName("net.luckperms.api.query.QueryOptions")
                            .getMethod("defaultContextualOptions").invoke(null));
                Object metaData = permissionData.getClass().getMethod("getMetaData").invoke(permissionData);
                String prefix = (String) metaData.getClass().getMethod("getPrefix").invoke(metaData);
                return prefix != null ? prefix : "";
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting LuckPerms prefix: " + e.getMessage());
        }
        return "";
    }

    /**
     * Gets the suffix of a player.
     * 
     * @param player The player
     * @return The suffix, or empty string if not available
     */
    public String getSuffix(Player player) {
        if (!isLuckPermsEnabled()) {
            return "";
        }
        
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", java.util.UUID.class)
                .invoke(userManager, player.getUniqueId());
            if (user != null) {
                Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
                Object permissionData = cachedData.getClass()
                    .getMethod("getPermissionData", Object.class).invoke(cachedData,
                        Class.forName("net.luckperms.api.query.QueryOptions")
                            .getMethod("defaultContextualOptions").invoke(null));
                Object metaData = permissionData.getClass().getMethod("getMetaData").invoke(permissionData);
                String suffix = (String) metaData.getClass().getMethod("getSuffix").invoke(metaData);
                return suffix != null ? suffix : "";
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting LuckPerms suffix: " + e.getMessage());
        }
        return "";
    }

    /**
     * Gets the LuckPerms API instance.
     * 
     * @return The LuckPerms instance, or null if not available
     */
    public Object getLuckPerms() {
        return luckPerms;
    }
}

