package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeManager {
    private final Ecore plugin;
    private final Map<UUID, Map<String, Location>> homes;
    private final File homeFile;
    private FileConfiguration homeConfig;

    public HomeManager(Ecore plugin) {
        this.plugin = plugin;
        this.homes = new HashMap<>();
        this.homeFile = new File(plugin.getDataFolder(), "homes.yml");
        this.homeConfig = YamlConfiguration.loadConfiguration(homeFile);
    }

    // Set a home for a player
    public boolean setHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        Map<String, Location> playerHomes = homes.computeIfAbsent(uuid, k -> new HashMap<>());

        if (playerHomes.size() >= plugin.getConfigManager().getMaxHomes() && !playerHomes.containsKey(name)) {
            return false;
        }

        playerHomes.put(name, player.getLocation());
        saveHomes();
        return true;
    }

    // Get a player's home
    public Location getHome(Player player, String name) {
        return homes.getOrDefault(player.getUniqueId(), new HashMap<>()).get(name);
    }

    // Get all homes for a player
    public Map<String, Location> getPlayerHomes(Player player) {
        return homes.getOrDefault(player.getUniqueId(), new HashMap<>());
    }

    // Load homes from file
    public void loadHomes() {
        if (!homeFile.exists()) return;

        homeConfig = YamlConfiguration.loadConfiguration(homeFile);
        for (String uuid : homeConfig.getKeys(false)) {
            Map<String, Location> playerHomes = new HashMap<>();
            for (String homeName : homeConfig.getConfigurationSection(uuid).getKeys(false)) {
                playerHomes.put(homeName, (Location) homeConfig.get(uuid + "." + homeName));
            }
            homes.put(UUID.fromString(uuid), playerHomes);
        }
    }

    // Save homes to file
    public void saveHomes() {
        for (UUID uuid : homes.keySet()) {
            Map<String, Location> playerHomes = homes.get(uuid);
            for (Map.Entry<String, Location> entry : playerHomes.entrySet()) {
                homeConfig.set(uuid.toString() + "." + entry.getKey(), entry.getValue());
            }
        }
        try {
            homeConfig.save(homeFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save homes: " + e.getMessage());
        }
    }
}