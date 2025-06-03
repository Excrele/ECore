package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigManager {
    private final Ecore plugin;
    private FileConfiguration config;
    private FileConfiguration discordConfig;
    private File discordConfigFile;

    public ConfigManager(Ecore plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadDiscordConfig();
    }

    // Load or create discordconf.yml
    private void loadDiscordConfig() {
        discordConfigFile = new File(plugin.getDataFolder(), "discordconf.yml");
        if (!discordConfigFile.exists()) {
            plugin.saveResource("discordconf.yml", false);
        }
        discordConfig = YamlConfiguration.loadConfiguration(discordConfigFile);
    }

    // Save default discordconf.yml
    public void saveDefaultDiscordConfig() {
        if (!discordConfigFile.exists()) {
            plugin.saveResource("discordconf.yml", false);
        }
    }

    // Get the main configuration
    public FileConfiguration getConfig() {
        return config;
    }

    // Get the Discord configuration
    public FileConfiguration getDiscordConfig() {
        return discordConfig;
    }

    // Get maximum number of homes
    public int getMaxHomes() {
        return config.getInt("home.max-homes", 3);
    }

    // Reload all configurations
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        discordConfig = YamlConfiguration.loadConfiguration(discordConfigFile);
    }
}