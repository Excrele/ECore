package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

// Manages the self-contained economy system
public class EconomyManager {
    private final Ecore plugin;
    private final File economyFile;
    private final YamlConfiguration economyConfig;

    public EconomyManager(Ecore plugin) {
        this.plugin = plugin;
        // Initialize economy.yml
        economyFile = new File(plugin.getDataFolder(), "economy.yml");
        if (!economyFile.exists()) {
            plugin.saveResource("economy.yml", false);
        }
        economyConfig = YamlConfiguration.loadConfiguration(economyFile);
    }

    // Save economy data
    private void saveEconomy() {
        try {
            economyConfig.save(economyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save economy.yml: " + e.getMessage());
        }
    }

    // Get a player's balance
    public double getBalance(UUID uuid) {
        return economyConfig.getDouble("players." + uuid.toString() + ".balance", 0.0);
    }

    // Set a player's balance
    public void setBalance(UUID uuid, double amount) {
        if (amount < 0) amount = 0;
        economyConfig.set("players." + uuid.toString() + ".balance", amount);
        saveEconomy();
    }

    // Add funds to a player's balance
    public void addBalance(UUID uuid, double amount) {
        if (amount < 0) return;
        double current = getBalance(uuid);
        setBalance(uuid, current + amount);
    }

    // Remove funds from a player's balance
    public boolean removeBalance(UUID uuid, double amount) {
        if (amount < 0) return false;
        double current = getBalance(uuid);
        if (current < amount) return false;
        setBalance(uuid, current - amount);
        return true;
    }

    // Transfer funds between players
    public boolean transferBalance(UUID from, UUID to, double amount) {
        if (amount < 0 || from.equals(to)) return false;
        if (removeBalance(from, amount)) {
            addBalance(to, amount);
            return true;
        }
        return false;
    }

    // Initialize a new player with starting balance
    public void initializePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (!economyConfig.contains("players." + uuid.toString())) {
            setBalance(uuid, plugin.getConfig().getDouble("economy.starting-balance", 100.0));
        }
    }

    // API for other plugins to access economy
    public static class EcoreEconomy {
        private final EconomyManager manager;

        public EcoreEconomy(EconomyManager manager) {
            this.manager = manager;
        }

        public double getBalance(UUID uuid) {
            return manager.getBalance(uuid);
        }

        public boolean withdraw(UUID uuid, double amount) {
            return manager.removeBalance(uuid, amount);
        }

        public void deposit(UUID uuid, double amount) {
            manager.addBalance(uuid, amount);
        }

        public boolean transfer(UUID from, UUID to, double amount) {
            return manager.transferBalance(from, to, amount);
        }
    }
}