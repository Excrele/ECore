package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * Manages custom tab list header and footer for players.
 */
public class TabListManager {
    private final Ecore plugin;
    private File tabListFile;
    private FileConfiguration tabListConfig;
    private int updateTaskId;

    public TabListManager(Ecore plugin) {
        this.plugin = plugin;
        initializeTabListConfig();
        startTabListUpdates();
    }

    private void initializeTabListConfig() {
        tabListFile = new File(plugin.getDataFolder(), "tablist.yml");
        if (!tabListFile.exists()) {
            plugin.saveResource("tablist.yml", false);
        }
        tabListConfig = YamlConfiguration.loadConfiguration(tabListFile);
    }

    /**
     * Sets up tab list for a player.
     */
    public void setupTabList(Player player) {
        if (!isTabListEnabled()) return;
        if (!player.hasPermission("ecore.tablist.use")) return;

        String header = getHeader(player);
        String footer = getFooter(player);
        
        player.setPlayerListHeaderFooter(header, footer);
    }

    /**
     * Updates tab list for all online players.
     */
    public void updateTabList() {
        if (!isTabListEnabled()) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("ecore.tablist.use")) continue;
            
            String header = getHeader(player);
            String footer = getFooter(player);
            
            player.setPlayerListHeaderFooter(header, footer);
        }
    }

    /**
     * Starts automatic tab list updates.
     */
    private void startTabListUpdates() {
        if (!isTabListEnabled()) return;

        int interval = tabListConfig.getInt("update-interval", 20); // Default: 1 second
        
        updateTaskId = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            updateTabList();
        }, 0L, interval).getTaskId();
    }

    /**
     * Gets the tab list header for a player.
     */
    private String getHeader(Player player) {
        String header = null;
        boolean isMultiline = false;
        java.util.List<String> headerLines = null;
        
        // Check per-group first (higher priority)
        if (tabListConfig.getBoolean("per-group.enabled", false)) {
            String group = plugin.getLuckPermsIntegration().getPrimaryGroup(player);
            header = tabListConfig.getString("per-group.groups." + group + ".header");
            if (header == null || header.isEmpty()) {
                headerLines = tabListConfig.getStringList("per-group.groups." + group + ".header-lines");
                if (!headerLines.isEmpty()) {
                    isMultiline = true;
                }
            }
        }
        
        // Check per-world if no group header found
        if ((header == null || header.isEmpty()) && headerLines == null) {
            if (tabListConfig.getBoolean("per-world.enabled", false)) {
                String worldName = player.getWorld().getName();
                header = tabListConfig.getString("per-world.worlds." + worldName + ".header");
                if (header == null || header.isEmpty()) {
                    headerLines = tabListConfig.getStringList("per-world.worlds." + worldName + ".header-lines");
                    if (!headerLines.isEmpty()) {
                        isMultiline = true;
                    }
                }
            }
        }
        
        // Default global header
        if (header == null || header.isEmpty()) {
            header = tabListConfig.getString("header", "§6§lWelcome to Server!");
            
            if (tabListConfig.getBoolean("header-multiline", false)) {
                // Support multiline headers
                headerLines = tabListConfig.getStringList("header-lines");
                if (!headerLines.isEmpty()) {
                    isMultiline = true;
                }
            }
        }
        
        // Process multiline or single line
        if (isMultiline && headerLines != null && !headerLines.isEmpty()) {
            header = String.join("\n", headerLines);
        }
        
        return processPlaceholders(player, header);
    }

    /**
     * Gets the tab list footer for a player.
     */
    private String getFooter(Player player) {
        String footer = null;
        boolean isMultiline = false;
        java.util.List<String> footerLines = null;
        
        // Check per-group first (higher priority)
        if (tabListConfig.getBoolean("per-group.enabled", false)) {
            String group = plugin.getLuckPermsIntegration().getPrimaryGroup(player);
            footer = tabListConfig.getString("per-group.groups." + group + ".footer");
            if (footer == null || footer.isEmpty()) {
                footerLines = tabListConfig.getStringList("per-group.groups." + group + ".footer-lines");
                if (!footerLines.isEmpty()) {
                    isMultiline = true;
                }
            }
        }
        
        // Check per-world if no group footer found
        if ((footer == null || footer.isEmpty()) && footerLines == null) {
            if (tabListConfig.getBoolean("per-world.enabled", false)) {
                String worldName = player.getWorld().getName();
                footer = tabListConfig.getString("per-world.worlds." + worldName + ".footer");
                if (footer == null || footer.isEmpty()) {
                    footerLines = tabListConfig.getStringList("per-world.worlds." + worldName + ".footer-lines");
                    if (!footerLines.isEmpty()) {
                        isMultiline = true;
                    }
                }
            }
        }
        
        // Default global footer
        if (footer == null || footer.isEmpty()) {
            footer = tabListConfig.getString("footer", "§7Visit our website!");
            
            if (tabListConfig.getBoolean("footer-multiline", false)) {
                // Support multiline footers
                footerLines = tabListConfig.getStringList("footer-lines");
                if (!footerLines.isEmpty()) {
                    isMultiline = true;
                }
            }
        }
        
        // Process multiline or single line
        if (isMultiline && footerLines != null && !footerLines.isEmpty()) {
            footer = String.join("\n", footerLines);
        }
        
        return processPlaceholders(player, footer);
    }

    /**
     * Processes placeholders in a string.
     */
    private String processPlaceholders(Player player, String text) {
        text = processColorCodes(text);
        
        // Replace ECore placeholders
        text = text.replace("%player%", player.getName());
        text = text.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        text = text.replace("%max%", String.valueOf(Bukkit.getMaxPlayers()));
        text = text.replace("%balance%", String.format("%.2f", plugin.getEconomyManager().getBalance(player.getUniqueId())));
        text = text.replace("%tps%", String.format("%.2f", plugin.getServerInfoManager().getTPS()));
        
        // PlaceholderAPI support
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        
        return text;
    }

    /**
     * Processes color codes.
     */
    private String processColorCodes(String text) {
        return text.replace("&", "§");
    }

    /**
     * Checks if tab list is enabled.
     */
    private boolean isTabListEnabled() {
        return tabListConfig.getBoolean("enabled", true);
    }

    /**
     * Reloads tab list configuration.
     */
    public void reload() {
        initializeTabListConfig();
        updateTabList();
    }

    /**
     * Shuts down the tab list manager.
     */
    public void shutdown() {
        if (updateTaskId != 0) {
            plugin.getServer().getScheduler().cancelTask(updateTaskId);
        }
    }
}

