package com.excrele.ecore.managers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.excrele.ecore.Ecore;

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

    public ConfigManager(Ecore plugin) {
        this.plugin = plugin;
        initializeConfigs();
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

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getDiscordConfig() {
        return discordConfig;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save config.yml: " + e.getMessage());
        }
    }

    public void saveDiscordConfig() {
        try {
            discordConfig.save(discordConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save discordconf.yml: " + e.getMessage());
        }
    }

    public int getMaxHomes() {
        return config.getInt("home.max-homes", 5);
    }

    public FileConfiguration getAdminShopConfig() {
        return adminShopConfig;
    }

    public FileConfiguration getPlayerShopConfig() {
        return playerShopConfig;
    }

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

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        discordConfig = YamlConfiguration.loadConfiguration(discordConfigFile);
        adminShopConfig = YamlConfiguration.loadConfiguration(adminShopFile);
        playerShopConfig = YamlConfiguration.loadConfiguration(playerShopFile);
    }
}