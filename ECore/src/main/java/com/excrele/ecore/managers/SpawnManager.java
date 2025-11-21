package com.excrele.ecore.managers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.excrele.ecore.Ecore;

public class SpawnManager {
    private final Ecore plugin;
    private File spawnFile;
    private FileConfiguration spawnConfig;

    public SpawnManager(Ecore plugin) {
        this.plugin = plugin;
        initializeSpawnConfig();
    }

    private void initializeSpawnConfig() {
        spawnFile = new File(plugin.getDataFolder(), "spawns.yml");
        if (!spawnFile.exists()) {
            try {
                spawnFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create spawns.yml", e);
            }
        }
        spawnConfig = YamlConfiguration.loadConfiguration(spawnFile);
    }

    public void setSpawn(Location location, String worldName) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        String path = "spawns." + worldName;
        spawnConfig.set(path + ".world", location.getWorld().getName());
        spawnConfig.set(path + ".x", location.getX());
        spawnConfig.set(path + ".y", location.getY());
        spawnConfig.set(path + ".z", location.getZ());
        spawnConfig.set(path + ".yaw", location.getYaw());
        spawnConfig.set(path + ".pitch", location.getPitch());
        saveSpawns();
    }

    public Location getSpawn(String worldName) {
        String path = "spawns." + worldName;
        if (!spawnConfig.contains(path)) {
            // Try default world
            path = "spawns.default";
            if (!spawnConfig.contains(path)) {
                return null;
            }
        }

        String worldNameConfig = spawnConfig.getString(path + ".world");
        World world = plugin.getServer().getWorld(worldNameConfig);
        if (world == null) {
            world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                return null;
            }
        }

        double x = spawnConfig.getDouble(path + ".x");
        double y = spawnConfig.getDouble(path + ".y");
        double z = spawnConfig.getDouble(path + ".z");
        float yaw = (float) spawnConfig.getDouble(path + ".yaw", 0);
        float pitch = (float) spawnConfig.getDouble(path + ".pitch", 0);

        return new Location(world, x, y, z, yaw, pitch);
    }

    public Location getSpawn(World world) {
        return getSpawn(world.getName());
    }

    public boolean hasSpawn(String worldName) {
        return spawnConfig.contains("spawns." + worldName) || spawnConfig.contains("spawns.default");
    }

    private void saveSpawns() {
        try {
            spawnConfig.save(spawnFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save spawns.yml: " + e.getMessage());
        }
    }
}

