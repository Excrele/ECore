package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class BankManager {
    private final Ecore plugin;
    private File bankFile;
    private FileConfiguration bankConfig;
    private BukkitTask interestTask;
    private final Map<UUID, Map<String, Long>> lastInterestCalculation; // UUID -> account name -> timestamp

    public BankManager(Ecore plugin) {
        this.plugin = plugin;
        this.lastInterestCalculation = new HashMap<>();
        initializeBankConfig();
        startInterestTask();
    }

    private void initializeBankConfig() {
        bankFile = new File(plugin.getDataFolder(), "banks.yml");
        if (!bankFile.exists()) {
            try {
                bankFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create banks.yml", e);
            }
        }
        bankConfig = YamlConfiguration.loadConfiguration(bankFile);
    }

    private void saveBankConfig() {
        try {
            bankConfig.save(bankFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save banks.yml: " + e.getMessage());
        }
    }

    // Create a new bank account
    public boolean createAccount(Player player, String accountName) {
        UUID uuid = player.getUniqueId();
        String path = "banks." + uuid.toString() + ".accounts." + accountName;
        
        if (bankConfig.contains(path)) {
            return false; // Account already exists
        }

        int maxAccounts = plugin.getConfig().getInt("bank.max-accounts", 3);
        List<String> accounts = getAccountNames(player);
        if (accounts.size() >= maxAccounts) {
            return false; // Max accounts reached
        }

        bankConfig.set(path + ".balance", 0.0);
        bankConfig.set(path + ".created", System.currentTimeMillis());
        bankConfig.set(path + ".interest-rate", plugin.getConfig().getDouble("bank.default-interest-rate", 0.01)); // 1% default
        saveBankConfig();
        return true;
    }

    // Delete a bank account
    public boolean deleteAccount(Player player, String accountName) {
        UUID uuid = player.getUniqueId();
        String path = "banks." + uuid.toString() + ".accounts." + accountName;
        
        if (!bankConfig.contains(path)) {
            return false; // Account doesn't exist
        }

        // Return balance to wallet before deleting
        double balance = getBalance(player, accountName);
        if (balance > 0) {
            plugin.getEconomyManager().addBalance(uuid, balance);
        }

        bankConfig.set(path, null);
        saveBankConfig();
        return true;
    }

    // Get all account names for a player
    public List<String> getAccountNames(Player player) {
        UUID uuid = player.getUniqueId();
        String path = "banks." + uuid.toString() + ".accounts";
        
        if (!bankConfig.contains(path)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(bankConfig.getConfigurationSection(path).getKeys(false));
    }

    // Get balance of a specific account
    public double getBalance(Player player, String accountName) {
        UUID uuid = player.getUniqueId();
        String path = "banks." + uuid.toString() + ".accounts." + accountName + ".balance";
        return bankConfig.getDouble(path, 0.0);
    }

    // Get total balance across all accounts
    public double getTotalBalance(Player player) {
        double total = 0.0;
        for (String accountName : getAccountNames(player)) {
            total += getBalance(player, accountName);
        }
        return total;
    }

    // Deposit money into an account
    public boolean deposit(Player player, String accountName, double amount) {
        if (amount <= 0) return false;
        
        UUID uuid = player.getUniqueId();
        if (!plugin.getEconomyManager().removeBalance(uuid, amount)) {
            return false; // Not enough money in wallet
        }

        double currentBalance = getBalance(player, accountName);
        String path = "banks." + uuid.toString() + ".accounts." + accountName + ".balance";
        bankConfig.set(path, currentBalance + amount);
        saveBankConfig();
        return true;
    }

    // Withdraw money from an account
    public boolean withdraw(Player player, String accountName, double amount) {
        if (amount <= 0) return false;
        
        double currentBalance = getBalance(player, accountName);
        if (currentBalance < amount) {
            return false; // Not enough money in account
        }

        UUID uuid = player.getUniqueId();
        plugin.getEconomyManager().addBalance(uuid, amount);
        
        String path = "banks." + uuid.toString() + ".accounts." + accountName + ".balance";
        bankConfig.set(path, currentBalance - amount);
        saveBankConfig();
        return true;
    }

    // Transfer money between accounts
    public boolean transfer(Player player, String fromAccount, String toAccount, double amount) {
        if (amount <= 0) return false;
        if (fromAccount.equals(toAccount)) return false;

        double fromBalance = getBalance(player, fromAccount);
        if (fromBalance < amount) {
            return false; // Not enough money
        }

        UUID uuid = player.getUniqueId();
        String fromPath = "banks." + uuid.toString() + ".accounts." + fromAccount + ".balance";
        String toPath = "banks." + uuid.toString() + ".accounts." + toAccount + ".balance";
        
        bankConfig.set(fromPath, fromBalance - amount);
        bankConfig.set(toPath, getBalance(player, toAccount) + amount);
        saveBankConfig();
        return true;
    }

    // Set interest rate for an account
    public boolean setInterestRate(Player player, String accountName, double rate) {
        UUID uuid = player.getUniqueId();
        String path = "banks." + uuid.toString() + ".accounts." + accountName;
        
        if (!bankConfig.contains(path)) {
            return false; // Account doesn't exist
        }

        bankConfig.set(path + ".interest-rate", rate);
        saveBankConfig();
        return true;
    }

    // Get interest rate for an account
    public double getInterestRate(Player player, String accountName) {
        UUID uuid = player.getUniqueId();
        String path = "banks." + uuid.toString() + ".accounts." + accountName + ".interest-rate";
        return bankConfig.getDouble(path, plugin.getConfig().getDouble("bank.default-interest-rate", 0.01));
    }

    // Calculate and apply interest (called periodically)
    private void calculateInterest() {
        long currentTime = System.currentTimeMillis();
        long interestInterval = plugin.getConfig().getLong("bank.interest-interval", 86400000L); // 24 hours default
        
        if (bankConfig.contains("banks")) {
            for (String uuidStr : bankConfig.getConfigurationSection("banks").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String accountsPath = "banks." + uuidStr + ".accounts";
                    
                    if (bankConfig.contains(accountsPath)) {
                        for (String accountName : bankConfig.getConfigurationSection(accountsPath).getKeys(false)) {
                            String accountPath = accountsPath + "." + accountName;
                            double balance = bankConfig.getDouble(accountPath + ".balance", 0.0);
                            
                            if (balance <= 0) continue;
                            
                            double interestRate = bankConfig.getDouble(accountPath + ".interest-rate", 
                                plugin.getConfig().getDouble("bank.default-interest-rate", 0.01));
                            
                            // Check if enough time has passed
                            Map<String, Long> playerLastCalc = lastInterestCalculation.computeIfAbsent(uuid, k -> new HashMap<>());
                            long lastCalc = playerLastCalc.getOrDefault(accountName, 0L);
                            
                            if (currentTime - lastCalc >= interestInterval) {
                                double interest = balance * interestRate;
                                bankConfig.set(accountPath + ".balance", balance + interest);
                                playerLastCalc.put(accountName, currentTime);
                                
                                // Notify player if online
                                Player player = Bukkit.getPlayer(uuid);
                                if (player != null && player.isOnline()) {
                                    player.sendMessage("§aYour bank account '" + accountName + "' earned §e" + 
                                        String.format("%.2f", interest) + "§a in interest!");
                                }
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid UUID, skip
                }
            }
        }
        
        saveBankConfig();
    }

    // Start the interest calculation task
    private void startInterestTask() {
        long interval = plugin.getConfig().getLong("bank.interest-check-interval", 3600000L); // 1 hour default
        
        interestTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            calculateInterest();
        }, interval / 50, interval / 50); // Convert milliseconds to ticks
    }

    // Shutdown task
    public void shutdown() {
        if (interestTask != null) {
            interestTask.cancel();
        }
        saveBankConfig();
    }
}

