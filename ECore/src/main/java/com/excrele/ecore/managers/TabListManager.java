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
        String header = tabListConfig.getString("header", "§6§lWelcome to Server!");
        
        if (tabListConfig.getBoolean("header-multiline", false)) {
            // Support multiline headers
            java.util.List<String> lines = tabListConfig.getStringList("header-lines");
            if (!lines.isEmpty()) {
                header = String.join("\n", lines);
            }
        }
        
        return processPlaceholders(player, header);
    }

    /**
     * Gets the tab list footer for a player.
     */
    private String getFooter(Player player) {
        String footer = tabListConfig.getString("footer", "§7Visit our website!");
        
        if (tabListConfig.getBoolean("footer-multiline", false)) {
            // Support multiline footers
            java.util.List<String> lines = tabListConfig.getStringList("footer-lines");
            if (!lines.isEmpty()) {
                footer = String.join("\n", lines);
            }
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

