package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final Ecore plugin;
    private FileConfiguration config;
    private FileConfiguration discordConfig;
    private FileConfiguration adminShopConfig;
    private FileConfiguration playerShopConfig;
    private File configFile;
    private File discordFile;
    private File adminShopFile;
    private File playerShopFile;

    public ConfigManager(Ecore plugin) {
        this.plugin = plugin;
        saveDefaultConfigs();
    }

    // Save default configuration files
    public void saveDefaultConfigs() {
        saveDefaultConfig();
        saveDefaultDiscordConfig();
        saveDefaultAdminShopConfig();
        saveDefaultPlayerShopConfig();
    }

    // Save default main config
    public void saveDefaultConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    // Save default discord config
    public void saveDefaultDiscordConfig() {
        discordFile = new File(plugin.getDataFolder(), "discordconf.yml");
        if (!discordFile.exists()) {
            plugin.saveResource("discordconf.yml", false);
        }
        discordConfig = YamlConfiguration.loadConfiguration(discordFile);
    }

    // Save default admin shop config
    public void saveDefaultAdminShopConfig() {
        adminShopFile = new File(plugin.getDataFolder(), "adminshops.yml");
        if (!adminShopFile.exists()) {
            plugin.saveResource("adminshops.yml", false);
        }
        adminShopConfig = YamlConfiguration.loadConfiguration(adminShopFile);
    }

    // Save default player shop config
    public void saveDefaultPlayerShopConfig() {
        playerShopFile = new File(plugin.getDataFolder(), "playershops.yml");
        if (!playerShopFile.exists()) {
            plugin.saveResource("playershops.yml", false);
        }
        playerShopConfig = YamlConfiguration.loadConfiguration(playerShopFile);
    }

    // Get main config
    public FileConfiguration getConfig() {
        return config;
    }

    // Get discord config
    public FileConfiguration getDiscordConfig() {
        return discordConfig;
    }

    // Get admin shop config
    public FileConfiguration getAdminShopConfig() {
        return adminShopConfig;
    }

    // Get player shop config
    public FileConfiguration getPlayerShopConfig() {
        return playerShopConfig;
    }

    // Get maximum homes
    public int getMaxHomes() {
        return config.getInt("homes.max-homes", 5);
    }

    // Save specific config file
    public void saveConfig(String fileName) {
        try {
            switch (fileName) {
                case "config.yml":
                    config.save(configFile);
                    break;
                case "discordconf.yml":
                    discordConfig.save(discordFile);
                    break;
                case "adminshops.yml":
                    adminShopConfig.save(adminShopFile);
                    break;
                case "playershops.yml":
                    playerShopConfig.save(playerShopFile);
                    break;
                default:
                    plugin.getLogger().warning("Unknown config file: " + fileName);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save config " + fileName + ": " + e.getMessage());
        }
    }

    // Reload main config
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    // Reload all configs
    public void reloadConfigs() {
        config = YamlConfiguration.loadConfiguration(configFile);
        discordConfig = YamlConfiguration.loadConfiguration(discordFile);
        adminShopConfig = YamlConfiguration.loadConfiguration(adminShopFile);
        playerShopConfig = YamlConfiguration.loadConfiguration(playerShopFile);
    }
}