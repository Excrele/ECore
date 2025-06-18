package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class HomeManager {
    private final Ecore plugin;
    private File homeFile;
    private FileConfiguration homeConfig;

    public HomeManager(Ecore plugin) {
        this.plugin = plugin;
        initializeHomeConfig();
    }

    private void initializeHomeConfig() {
        homeFile = new File(plugin.getDataFolder(), "homes.yml");
        if (!homeFile.exists()) {
            plugin.saveResource("homes.yml", false);
        }
        homeConfig = YamlConfiguration.loadConfiguration(homeFile);
    }

    public boolean setHome(Player player, String homeName, Location location) {
        String uuid = player.getUniqueId().toString();
        List<String> homes = homeConfig.getStringList("homes." + uuid + ".list");
        if (homes.size() >= plugin.getConfigManager().getMaxHomes()) {
            return false;
        }

        if (!homes.contains(homeName)) {
            homes.add(homeName);
            homeConfig.set("homes." + uuid + ".list", homes);
        }

        String path = "homes." + uuid + "." + homeName;
        homeConfig.set(path + ".world", location.getWorld().getName());
        homeConfig.set(path + ".x", location.getX());
        homeConfig.set(path + ".y", location.getY());
        homeConfig.set(path + ".z", location.getZ());
        homeConfig.set(path + ".yaw", location.getYaw());
        homeConfig.set(path + ".pitch", location.getPitch());

        try {
            homeConfig.save(homeFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save home to homes.yml: " + e.getMessage());
            return false;
        }

        plugin.getDiscordManager().sendStaffLogNotification(
                "home-log",
                player.getName(),
                "set home",
                homeName,
                location.toString()
        );
        return true;
    }

    public List<String> getHomes(Player player) {
        String uuid = player.getUniqueId().toString();
        return homeConfig.getStringList("homes." + uuid + ".list");
    }

    public Location getHome(Player player, String homeName) {
        String uuid = player.getUniqueId().toString();
        String path = "homes." + uuid + "." + homeName;

        if (!homeConfig.contains(path)) {
            return null;
        }

        String worldName = homeConfig.getString(path + ".world");
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World " + worldName + " not found for home " + homeName);
            return null;
        }

        double x = homeConfig.getDouble(path + ".x");
        double y = homeConfig.getDouble(path + ".y");
        double z = homeConfig.getDouble(path + ".z");
        float yaw = (float) homeConfig.getDouble(path + ".yaw");
        float pitch = (float) homeConfig.getDouble(path + ".pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    // Alias for getHomes to resolve compilation error
    public List<String> getPlayerHomes(Player player) {
        return getHomes(player);
    }
}