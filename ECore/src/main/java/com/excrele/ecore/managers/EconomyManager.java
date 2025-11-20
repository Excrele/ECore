package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

// Manages the self-contained economy system
public class EconomyManager {
    private final Ecore plugin;
    private final File economyFile;
    private final YamlConfiguration economyConfig;
    private final List<Transaction> transactionHistory;

    public EconomyManager(Ecore plugin) {
        this.plugin = plugin;
        this.transactionHistory = new ArrayList<>();
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
        double oldBalance = getBalance(uuid);
        economyConfig.set("players." + uuid.toString() + ".balance", amount);
        logTransaction(uuid, "SET", amount - oldBalance, "Balance set to " + amount);
        saveEconomy();
    }

    // Add funds to a player's balance
    public void addBalance(UUID uuid, double amount) {
        if (amount < 0) return;
        double current = getBalance(uuid);
        setBalance(uuid, current + amount);
        logTransaction(uuid, "DEPOSIT", amount, "Deposit");
        
        // Track money earned in statistics
        if (plugin.getStatisticsManager() != null) {
            plugin.getStatisticsManager().trackMoneyEarned(uuid, amount);
        }
    }

    // Remove funds from a player's balance
    public boolean removeBalance(UUID uuid, double amount) {
        if (amount < 0) return false;
        double current = getBalance(uuid);
        if (current < amount) return false;
        setBalance(uuid, current - amount);
        logTransaction(uuid, "WITHDRAW", amount, "Withdrawal");
        
        // Track money spent in statistics
        if (plugin.getStatisticsManager() != null) {
            plugin.getStatisticsManager().trackMoneySpent(uuid, amount);
        }
        return true;
    }

    // Transfer funds between players
    public boolean transferBalance(UUID from, UUID to, double amount) {
        if (amount < 0 || from.equals(to)) return false;
        if (removeBalance(from, amount)) {
            addBalance(to, amount);
            Player fromPlayer = Bukkit.getPlayer(from);
            Player toPlayer = Bukkit.getPlayer(to);
            String fromName = fromPlayer != null ? fromPlayer.getName() : from.toString();
            String toName = toPlayer != null ? toPlayer.getName() : to.toString();
            logTransaction(from, "TRANSFER_OUT", amount, "Transfer to " + toName);
            logTransaction(to, "TRANSFER_IN", amount, "Transfer from " + fromName);
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

    // Transaction logging
    private void logTransaction(UUID uuid, String type, double amount, String description) {
        Transaction transaction = new Transaction(uuid, type, amount, description, System.currentTimeMillis());
        transactionHistory.add(transaction);
        
        // Keep only last 1000 transactions
        if (transactionHistory.size() > 1000) {
            transactionHistory.remove(0);
        }
        
        // Save to config
        List<Map<String, Object>> transactions = new ArrayList<>();
        for (Transaction t : transactionHistory) {
            Map<String, Object> map = new HashMap<>();
            map.put("uuid", t.getUuid().toString());
            map.put("type", t.getType());
            map.put("amount", t.getAmount());
            map.put("description", t.getDescription());
            map.put("timestamp", t.getTimestamp());
            transactions.add(map);
        }
        economyConfig.set("transactions", transactions);
        saveEconomy();
    }

    // Get economy leaderboard
    public List<Map.Entry<UUID, Double>> getLeaderboard(int limit) {
        Map<UUID, Double> balances = new HashMap<>();
        if (economyConfig.contains("players")) {
            for (String uuidStr : economyConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    double balance = getBalance(uuid);
                    if (balance > 0) {
                        balances.put(uuid, balance);
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid UUID, skip
                }
            }
        }
        
        List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(balances.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }

    // Get economy statistics
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        double totalMoney = 0;
        int playerCount = 0;
        double averageBalance = 0;
        double maxBalance = 0;
        
        if (economyConfig.contains("players")) {
            for (String uuidStr : economyConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    double balance = getBalance(uuid);
                    totalMoney += balance;
                    playerCount++;
                    if (balance > maxBalance) {
                        maxBalance = balance;
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid UUID, skip
                }
            }
        }
        
        if (playerCount > 0) {
            averageBalance = totalMoney / playerCount;
        }
        
        stats.put("totalMoney", totalMoney);
        stats.put("playerCount", playerCount);
        stats.put("averageBalance", averageBalance);
        stats.put("maxBalance", maxBalance);
        stats.put("transactionCount", transactionHistory.size());
        
        return stats;
    }

    // Transaction class
    public static class Transaction {
        private final UUID uuid;
        private final String type;
        private final double amount;
        private final String description;
        private final long timestamp;

        public Transaction(UUID uuid, String type, double amount, String description, long timestamp) {
            this.uuid = uuid;
            this.type = type;
            this.amount = amount;
            this.description = description;
            this.timestamp = timestamp;
        }

        public UUID getUuid() { return uuid; }
        public String getType() { return type; }
        public double getAmount() { return amount; }
        public String getDescription() { return description; }
        public long getTimestamp() { return timestamp; }
    }
}