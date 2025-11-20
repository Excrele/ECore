package com.excrele.ecore.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.excrele.ecore.Ecore;

public class WarpManager {
    private final Ecore plugin;
    private File warpFile;
    private FileConfiguration warpConfig;

    public WarpManager(Ecore plugin) {
        this.plugin = plugin;
        initializeWarpConfig();
    }

    private void initializeWarpConfig() {
        warpFile = new File(plugin.getDataFolder(), "warps.yml");
        if (!warpFile.exists()) {
            try {
                warpFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create warps.yml", e);
            }
        }
        warpConfig = YamlConfiguration.loadConfiguration(warpFile);
    }

    public boolean createWarp(String name, Location location, Player creator) {
        if (warpConfig.contains("warps." + name.toLowerCase())) {
            return false;
        }

        String path = "warps." + name.toLowerCase();
        warpConfig.set(path + ".world", location.getWorld().getName());
        warpConfig.set(path + ".x", location.getX());
        warpConfig.set(path + ".y", location.getY());
        warpConfig.set(path + ".z", location.getZ());
        warpConfig.set(path + ".yaw", location.getYaw());
        warpConfig.set(path + ".pitch", location.getPitch());
        warpConfig.set(path + ".creator", creator.getUniqueId().toString());
        warpConfig.set(path + ".public", true);

        saveWarps();
        return true;
    }

    public boolean deleteWarp(String name) {
        if (!warpConfig.contains("warps." + name.toLowerCase())) {
            return false;
        }
        warpConfig.set("warps." + name.toLowerCase(), null);
        saveWarps();
        return true;
    }

    public Location getWarp(String name) {
        String path = "warps." + name.toLowerCase();
        if (!warpConfig.contains(path)) {
            return null;
        }

        String worldName = warpConfig.getString(path + ".world");
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            return null;
        }

        double x = warpConfig.getDouble(path + ".x");
        double y = warpConfig.getDouble(path + ".y");
        double z = warpConfig.getDouble(path + ".z");
        float yaw = (float) warpConfig.getDouble(path + ".yaw");
        float pitch = (float) warpConfig.getDouble(path + ".pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public List<String> getWarps() {
        if (!warpConfig.contains("warps")) {
            return new ArrayList<>();
        }
        Set<String> warpKeys = warpConfig.getConfigurationSection("warps").getKeys(false);
        return new ArrayList<>(warpKeys);
    }

    public List<String> getPublicWarps() {
        List<String> warps = new ArrayList<>();
        if (!warpConfig.contains("warps")) {
            return warps;
        }
        for (String warp : warpConfig.getConfigurationSection("warps").getKeys(false)) {
            if (warpConfig.getBoolean("warps." + warp + ".public", true)) {
                warps.add(warp);
            }
        }
        return warps;
    }

    public boolean warpExists(String name) {
        return warpConfig.contains("warps." + name.toLowerCase());
    }

    public void setPublic(String name, boolean isPublic) {
        warpConfig.set("warps." + name.toLowerCase() + ".public", isPublic);
        saveWarps();
    }

    public boolean isPublic(String name) {
        return warpConfig.getBoolean("warps." + name.toLowerCase() + ".public", true);
    }

    public void teleportToWarp(Player player, String name) {
        Location warp = getWarp(name);
        if (warp == null) {
            player.sendMessage("§cWarp '" + name + "' does not exist!");
            return;
        }
        plugin.getTeleportManager().teleport(player, warp);
        player.sendMessage("§aTeleported to warp: " + name);
    }

    private void saveWarps() {
        try {
            warpConfig.save(warpFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save warps.yml: " + e.getMessage());
        }
    }
}

