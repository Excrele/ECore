package com.excrele.ecore.managers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.excrele.ecore.Ecore;

/**
 * Manages all configuration files for the Ecore plugin.
 * Handles loading, saving, validation, and migration of configuration files.
 * 
 * @author Excrele
 * @version 1.0
 */
public class ConfigManager {
    private final Ecore plugin;
    private FileConfiguration config;
    private FileConfiguration discordConfig;
    private FileConfiguration adminShopConfig;
    private FileConfiguration playerShopConfig;
    private File configFile;
    private File discordConfigFile;
    private File adminShopFile;
    private File playerShopFile;
    private static final int CONFIG_VERSION = 1; // Current config version

    /**
     * Creates a new ConfigManager instance and initializes all configuration files.
     * 
     * @param plugin The Ecore plugin instance
     */
    public ConfigManager(Ecore plugin) {
        this.plugin = plugin;
        initializeConfigs();
        validateConfig();
        migrateConfig();
    }

    private void initializeConfigs() {
        // Main config
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // Discord config
        discordConfigFile = new File(plugin.getDataFolder(), "discordconf.yml");
        if (!discordConfigFile.exists()) {
            plugin.saveResource("discordconf.yml", false);
        }
        discordConfig = YamlConfiguration.loadConfiguration(discordConfigFile);

        // Admin Shop config
        adminShopFile = new File(plugin.getDataFolder(), "adminshops.yml");
        if (!adminShopFile.exists()) {
            plugin.saveResource("adminshops.yml", false);
        }
        adminShopConfig = YamlConfiguration.loadConfiguration(adminShopFile);

        // Player Shop config
        playerShopFile = new File(plugin.getDataFolder(), "playershops.yml");
        if (!playerShopFile.exists()) {
            plugin.saveResource("playershops.yml", false);
        }
        playerShopConfig = YamlConfiguration.loadConfiguration(playerShopFile);
    }

    /**
     * Gets the main configuration file.
     * 
     * @return The main FileConfiguration
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Gets the Discord configuration file.
     * 
     * @return The Discord FileConfiguration
     */
    public FileConfiguration getDiscordConfig() {
        return discordConfig;
    }

    /**
     * Saves the main configuration file to disk.
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save config.yml: " + e.getMessage());
        }
    }

    /**
     * Saves the Discord configuration file to disk.
     */
    public void saveDiscordConfig() {
        try {
            discordConfig.save(discordConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save discordconf.yml: " + e.getMessage());
        }
    }

    /**
     * Gets the maximum number of homes allowed per player.
     * 
     * @return The maximum number of homes
     */
    public int getMaxHomes() {
        return config.getInt("home.max-homes", 5);
    }

    /**
     * Gets the admin shop configuration file.
     * 
     * @return The admin shop FileConfiguration
     */
    public FileConfiguration getAdminShopConfig() {
        return adminShopConfig;
    }

    /**
     * Gets the player shop configuration file.
     * 
     * @return The player shop FileConfiguration
     */
    public FileConfiguration getPlayerShopConfig() {
        return playerShopConfig;
    }

    /**
     * Saves a specific configuration file by name.
     * 
     * @param fileName The name of the config file to save (config.yml, discordconf.yml, adminshops.yml, playershops.yml)
     */
    public void saveConfig(String fileName) {
        try {
            switch (fileName.toLowerCase()) {
                case "config.yml":
                    config.save(configFile);
                    break;
                case "discordconf.yml":
                    discordConfig.save(discordConfigFile);
                    break;
                case "adminshops.yml":
                    adminShopConfig.save(adminShopFile);
                    break;
                case "playershops.yml":
                    playerShopConfig.save(playerShopFile);
                    break;
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Reloads all configuration files from disk.
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        discordConfig = YamlConfiguration.loadConfiguration(discordConfigFile);
        adminShopConfig = YamlConfiguration.loadConfiguration(adminShopFile);
        playerShopConfig = YamlConfiguration.loadConfiguration(playerShopFile);
        validateConfig();
    }

    /**
     * Validates the main configuration file and sets default values for missing keys.
     */
    private void validateConfig() {
        boolean needsSave = false;

        // Validate chat settings
        if (!config.contains("chat.slow-mode")) {
            config.set("chat.slow-mode", 0);
            needsSave = true;
        }
        if (!config.contains("chat.cooldown")) {
            config.set("chat.cooldown", 0);
            needsSave = true;
        }

        // Validate home settings
        if (!config.contains("home.max-homes")) {
            config.set("home.max-homes", 5);
            needsSave = true;
        }
        if (!config.contains("home.teleport-cost")) {
            config.set("home.teleport-cost", 0.0);
            needsSave = true;
        }
        if (!config.contains("home.cooldown")) {
            config.set("home.cooldown", 0);
            needsSave = true;
        }
        if (!config.contains("home.warmup")) {
            config.set("home.warmup", 0);
            needsSave = true;
        }
        if (!config.contains("home.bed-spawn-enabled")) {
            config.set("home.bed-spawn-enabled", true);
            needsSave = true;
        }
        if (!config.contains("home.auto-set-home-on-bed")) {
            config.set("home.auto-set-home-on-bed", true);
            needsSave = true;
        }

        // Validate shop settings
        if (!config.contains("shops.max-shops-per-player")) {
            config.set("shops.max-shops-per-player", 10);
            needsSave = true;
        }
        if (!config.contains("shops.expiration-days")) {
            config.set("shops.expiration-days", 30);
            needsSave = true;
        }
        if (!config.contains("shops.enable-categories")) {
            config.set("shops.enable-categories", true);
            needsSave = true;
        }
        if (!config.contains("shops.enable-favorites")) {
            config.set("shops.enable-favorites", true);
            needsSave = true;
        }
        if (!config.contains("shops.enable-statistics")) {
            config.set("shops.enable-statistics", true);
            needsSave = true;
        }

        // Validate economy settings
        if (!config.contains("economy.starting-balance")) {
            config.set("economy.starting-balance", 100.0);
            needsSave = true;
        }

        // Validate server info settings
        if (!config.contains("server-info.enabled")) {
            config.set("server-info.enabled", true);
            needsSave = true;
        }
        if (!config.contains("server-info.tps-monitoring")) {
            config.set("server-info.tps-monitoring", true);
            needsSave = true;
        }
        if (!config.contains("server-info.update-interval")) {
            config.set("server-info.update-interval", 20);
            needsSave = true;
        }

        // Validate report settings
        if (!config.contains("report.max-reports")) {
            config.set("report.max-reports", 5);
            needsSave = true;
        }
        if (!config.contains("report.report-cooldown")) {
            config.set("report.report-cooldown", 300);
            needsSave = true;
        }

        // Validate bank settings
        if (!config.contains("bank.max-accounts")) {
            config.set("bank.max-accounts", 3);
            needsSave = true;
        }
        if (!config.contains("bank.default-interest-rate")) {
            config.set("bank.default-interest-rate", 0.01);
            needsSave = true;
        }

        // Set config version
        if (!config.contains("config-version")) {
            config.set("config-version", CONFIG_VERSION);
            needsSave = true;
        }

        // Validate numeric ranges
        int maxHomes = config.getInt("home.max-homes", 5);
        if (maxHomes < 1) {
            plugin.getLogger().warning("home.max-homes must be at least 1, setting to default (5)");
            config.set("home.max-homes", 5);
            needsSave = true;
        }

        double startingBalance = config.getDouble("economy.starting-balance", 100.0);
        if (startingBalance < 0) {
            plugin.getLogger().warning("economy.starting-balance cannot be negative, setting to default (100.0)");
            config.set("economy.starting-balance", 100.0);
            needsSave = true;
        }

        if (needsSave) {
            saveConfig();
            plugin.getLogger().info("Configuration validated and updated with default values.");
        }
    }

    /**
     * Migrates configuration files from older versions to the current version.
     */
    private void migrateConfig() {
        int currentVersion = config.getInt("config-version", 0);
        
        if (currentVersion < CONFIG_VERSION) {
            plugin.getLogger().info("Migrating configuration from version " + currentVersion + " to " + CONFIG_VERSION);
            
            // Migration logic for future versions
            // Example: if (currentVersion < 2) { migrateToV2(); }
            
            config.set("config-version", CONFIG_VERSION);
            saveConfig();
            plugin.getLogger().info("Configuration migration completed!");
        }
    }

    /**
     * Gets the current configuration version.
     * 
     * @return The configuration version number
     */
    public int getConfigVersion() {
        return config.getInt("config-version", CONFIG_VERSION);
    }

    /**
     * Validates a numeric configuration value and returns it if valid, or a default if invalid.
     * 
     * @param path The configuration path
     * @param defaultValue The default value to use if invalid
     * @param min The minimum allowed value (inclusive)
     * @param max The maximum allowed value (inclusive)
     * @return The validated value
     */
    public int validateInt(String path, int defaultValue, int min, int max) {
        int value = config.getInt(path, defaultValue);
        if (value < min || value > max) {
            plugin.getLogger().warning("Invalid value for " + path + ": " + value + 
                " (must be between " + min + " and " + max + "). Using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    /**
     * Validates a double configuration value and returns it if valid, or a default if invalid.
     * 
     * @param path The configuration path
     * @param defaultValue The default value to use if invalid
     * @param min The minimum allowed value (inclusive)
     * @param max The maximum allowed value (inclusive)
     * @return The validated value
     */
    public double validateDouble(String path, double defaultValue, double min, double max) {
        double value = config.getDouble(path, defaultValue);
        if (value < min || value > max) {
            plugin.getLogger().warning("Invalid value for " + path + ": " + value + 
                " (must be between " + min + " and " + max + "). Using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }
}