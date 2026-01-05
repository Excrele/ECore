package com.excrele.ecore.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.excrele.ecore.Ecore;

/**
 * Advanced Banking System
 * Extends basic bank with branches, loans, investments, cards, transfers, security, analytics, etc.
 */
public class AdvancedBankManager {
    private final Ecore plugin;
    private File advancedBankFile;
    private FileConfiguration advancedBankConfig;
    private final Map<UUID, String> playerPINs; // Player UUID -> PIN
    private final Map<UUID, List<BankLoan>> playerLoans; // Player UUID -> List of loans
    private final Map<String, BankBranch> branches; // Branch ID -> Branch
    private final Map<String, Double> investments; // Investment ID -> Value
    private BukkitTask loanTask;
    
    public AdvancedBankManager(Ecore plugin) {
        this.plugin = plugin;
        this.playerPINs = new HashMap<>();
        this.playerLoans = new HashMap<>();
        this.branches = new HashMap<>();
        this.investments = new HashMap<>();
        initializeConfig();
        loadData();
        startLoanTask();
    }
    
    private void initializeConfig() {
        advancedBankFile = new File(plugin.getDataFolder(), "advanced-banks.yml");
        if (!advancedBankFile.exists()) {
            try {
                advancedBankFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create advanced-banks.yml", e);
            }
        }
        advancedBankConfig = YamlConfiguration.loadConfiguration(advancedBankFile);
    }
    
    private void loadData() {
        // Load PINs
        if (advancedBankConfig.contains("pins")) {
            for (String uuidStr : advancedBankConfig.getConfigurationSection("pins").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String pin = advancedBankConfig.getString("pins." + uuidStr);
                    playerPINs.put(uuid, pin);
                } catch (IllegalArgumentException e) {
                    // Skip invalid UUIDs
                }
            }
        }
        
        // Load loans
        if (advancedBankConfig.contains("loans")) {
            for (String uuidStr : advancedBankConfig.getConfigurationSection("loans").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    List<BankLoan> loans = new ArrayList<>();
                    for (String loanId : advancedBankConfig.getConfigurationSection("loans." + uuidStr).getKeys(false)) {
                        String path = "loans." + uuidStr + "." + loanId;
                        double amount = advancedBankConfig.getDouble(path + ".amount");
                        double interestRate = advancedBankConfig.getDouble(path + ".interest-rate");
                        long startTime = advancedBankConfig.getLong(path + ".start-time");
                        long duration = advancedBankConfig.getLong(path + ".duration");
                        boolean paid = advancedBankConfig.getBoolean(path + ".paid", false);
                        loans.add(new BankLoan(loanId, amount, interestRate, startTime, duration, paid));
                    }
                    playerLoans.put(uuid, loans);
                } catch (IllegalArgumentException e) {
                    // Skip invalid UUIDs
                }
            }
        }
        
        // Load branches
        if (advancedBankConfig.contains("branches")) {
            for (String branchId : advancedBankConfig.getConfigurationSection("branches").getKeys(false)) {
                String path = "branches." + branchId;
                String name = advancedBankConfig.getString(path + ".name");
                String world = advancedBankConfig.getString(path + ".world");
                double x = advancedBankConfig.getDouble(path + ".x");
                double y = advancedBankConfig.getDouble(path + ".y");
                double z = advancedBankConfig.getDouble(path + ".z");
                Location location = new Location(Bukkit.getWorld(world), x, y, z);
                branches.put(branchId, new BankBranch(branchId, name, location));
            }
        }
        
        // Load investments
        if (advancedBankConfig.contains("investments")) {
            for (String investmentId : advancedBankConfig.getConfigurationSection("investments").getKeys(false)) {
                double value = advancedBankConfig.getDouble("investments." + investmentId);
                investments.put(investmentId, value);
            }
        }
    }
    
    /**
     * Set PIN for bank account
     */
    public boolean setPIN(Player player, String pin) {
        if (pin.length() != 4 || !pin.matches("\\d+")) {
            return false; // PIN must be 4 digits
        }
        
        playerPINs.put(player.getUniqueId(), pin);
        advancedBankConfig.set("pins." + player.getUniqueId().toString(), pin);
        saveConfig();
        return true;
    }
    
    /**
     * Verify PIN
     */
    public boolean verifyPIN(Player player, String pin) {
        String storedPIN = playerPINs.get(player.getUniqueId());
        return storedPIN != null && storedPIN.equals(pin);
    }
    
    /**
     * Take out a loan
     */
    public BankLoan takeLoan(Player player, double amount, long durationMillis) {
        UUID uuid = player.getUniqueId();
        
        // Check existing loans
        List<BankLoan> existingLoans = playerLoans.getOrDefault(uuid, new ArrayList<>());
        for (BankLoan loan : existingLoans) {
            if (!loan.isPaid()) {
                return null; // Already has an active loan
            }
        }
        
        double maxLoan = plugin.getConfig().getDouble("bank.max-loan", 10000.0);
        if (amount > maxLoan) {
            return null; // Exceeds max loan
        }
        
        double interestRate = plugin.getConfig().getDouble("bank.loan-interest-rate", 0.05); // 5% default
        
        String loanId = UUID.randomUUID().toString();
        BankLoan loan = new BankLoan(loanId, amount, interestRate, System.currentTimeMillis(), durationMillis, false);
        
        existingLoans.add(loan);
        playerLoans.put(uuid, existingLoans);
        
        // Give money to player
        plugin.getEconomyManager().addBalance(uuid, amount);
        
        // Save
        String path = "loans." + uuid.toString() + "." + loanId;
        advancedBankConfig.set(path + ".amount", amount);
        advancedBankConfig.set(path + ".interest-rate", interestRate);
        advancedBankConfig.set(path + ".start-time", loan.getStartTime());
        advancedBankConfig.set(path + ".duration", durationMillis);
        advancedBankConfig.set(path + ".paid", false);
        saveConfig();
        
        return loan;
    }
    
    /**
     * Pay off a loan
     */
    public boolean payLoan(Player player, String loanId) {
        UUID uuid = player.getUniqueId();
        List<BankLoan> loans = playerLoans.get(uuid);
        if (loans == null) return false;
        
        BankLoan loan = null;
        for (BankLoan l : loans) {
            if (l.getId().equals(loanId) && !l.isPaid()) {
                loan = l;
                break;
            }
        }
        
        if (loan == null) return false;
        
        double totalOwed = loan.getTotalOwed();
        if (plugin.getEconomyManager().getBalance(uuid) < totalOwed) {
            return false; // Not enough money
        }
        
        plugin.getEconomyManager().removeBalance(uuid, totalOwed);
        loan.setPaid(true);
        
        // Update config
        String path = "loans." + uuid.toString() + "." + loanId;
        advancedBankConfig.set(path + ".paid", true);
        saveConfig();
        
        return true;
    }
    
    /**
     * Transfer money between players via bank
     */
    public boolean transferToPlayer(Player from, String toPlayerName, double amount) {
        @SuppressWarnings("deprecation")
        org.bukkit.OfflinePlayer toPlayer = Bukkit.getOfflinePlayer(toPlayerName);
        if (!toPlayer.hasPlayedBefore()) {
            return false;
        }
        
        UUID toUuid = toPlayer.getUniqueId();
        
        // Check if player has enough in any account
        double totalBalance = plugin.getBankManager().getTotalBalance(from);
        if (totalBalance < amount) {
            return false;
        }
        
        // Withdraw from first account with enough balance
        List<String> accounts = plugin.getBankManager().getAccountNames(from);
        double remaining = amount;
        for (String accountName : accounts) {
            double balance = plugin.getBankManager().getBalance(from, accountName);
            if (balance > 0) {
                double withdrawAmount = Math.min(balance, remaining);
                plugin.getBankManager().withdraw(from, accountName, withdrawAmount);
                remaining -= withdrawAmount;
                if (remaining <= 0) break;
            }
        }
        
        // Deposit to recipient's wallet or first account
        plugin.getEconomyManager().addBalance(toUuid, amount);
        
        // Notify recipient if online
        if (toPlayer.isOnline()) {
            Player to = toPlayer.getPlayer();
            if (to != null) {
                to.sendMessage(org.bukkit.ChatColor.GREEN + from.getName() + " sent you " + 
                    plugin.getEconomyManager().format(amount) + " via bank transfer!");
            }
        }
        
        return true;
    }
    
    /**
     * Invest in server-controlled stocks/bonds
     */
    public boolean invest(Player player, String investmentId, double amount) {
        if (!investments.containsKey(investmentId)) {
            return false; // Invalid investment
        }
        
        UUID uuid = player.getUniqueId();
        if (plugin.getEconomyManager().getBalance(uuid) < amount) {
            return false;
        }
        
        plugin.getEconomyManager().removeBalance(uuid, amount);
        
        // Store investment
        String path = "player-investments." + uuid.toString() + "." + investmentId;
        double current = advancedBankConfig.getDouble(path, 0.0);
        advancedBankConfig.set(path, current + amount);
        saveConfig();
        
        return true;
    }
    
    /**
     * Create a bank branch
     */
    public BankBranch createBranch(String branchId, String name, Location location) {
        BankBranch branch = new BankBranch(branchId, name, location);
        branches.put(branchId, branch);
        
        String path = "branches." + branchId;
        advancedBankConfig.set(path + ".name", name);
        advancedBankConfig.set(path + ".world", location.getWorld().getName());
        advancedBankConfig.set(path + ".x", location.getX());
        advancedBankConfig.set(path + ".y", location.getY());
        advancedBankConfig.set(path + ".z", location.getZ());
        saveConfig();
        
        return branch;
    }
    
    /**
     * Create guild bank account
     */
    public boolean createGuildAccount(String guildId, GuildManager.Guild guild) {
        String path = "guild-accounts." + guildId;
        if (advancedBankConfig.contains(path)) {
            return false; // Account already exists
        }
        
        advancedBankConfig.set(path + ".balance", 0.0);
        advancedBankConfig.set(path + ".created", System.currentTimeMillis());
        saveConfig();
        return true;
    }
    
    /**
     * Delete guild bank account
     */
    public boolean deleteGuildAccount(String guildId) {
        String path = "guild-accounts." + guildId;
        if (!advancedBankConfig.contains(path)) {
            return false;
        }
        
        advancedBankConfig.set(path, null);
        saveConfig();
        return true;
    }
    
    /**
     * Get spending analytics for a player
     */
    public Map<String, Double> getSpendingAnalytics(Player player) {
        // This would track spending patterns - simplified for now
        Map<String, Double> analytics = new HashMap<>();
        analytics.put("total-spent", 0.0);
        analytics.put("total-earned", 0.0);
        analytics.put("shop-purchases", 0.0);
        analytics.put("auction-spending", 0.0);
        return analytics;
    }
    
    private void startLoanTask() {
        // Process loan interest and due dates
        loanTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            for (Map.Entry<UUID, List<BankLoan>> entry : playerLoans.entrySet()) {
                for (BankLoan loan : entry.getValue()) {
                    if (!loan.isPaid() && currentTime > loan.getDueDate()) {
                        // Loan is overdue - could auto-deduct or notify
                        Player player = Bukkit.getPlayer(entry.getKey());
                        if (player != null) {
                            player.sendMessage(org.bukkit.ChatColor.RED + "Your loan of " + 
                                plugin.getEconomyManager().format(loan.getAmount()) + " is overdue!");
                        }
                    }
                }
            }
        }, 0L, 12000L); // Every 10 minutes
    }
    
    private void saveConfig() {
        try {
            advancedBankConfig.save(advancedBankFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save advanced-banks.yml", e);
        }
    }
    
    public void shutdown() {
        if (loanTask != null) {
            loanTask.cancel();
        }
        saveConfig();
    }
    
    public List<BankLoan> getPlayerLoans(UUID uuid) {
        return playerLoans.getOrDefault(uuid, new ArrayList<>());
    }
    
    public BankBranch getBranch(String branchId) {
        return branches.get(branchId);
    }
    
    public Collection<BankBranch> getBranches() {
        return branches.values();
    }
    
    /**
     * Bank Loan class
     */
    public static class BankLoan {
        private String id;
        private double amount;
        private double interestRate;
        private long startTime;
        private long duration;
        private boolean paid;
        
        public BankLoan(String id, double amount, double interestRate, long startTime, long duration, boolean paid) {
            this.id = id;
            this.amount = amount;
            this.interestRate = interestRate;
            this.startTime = startTime;
            this.duration = duration;
            this.paid = paid;
        }
        
        public double getTotalOwed() {
            long elapsed = System.currentTimeMillis() - startTime;
            double interest = amount * interestRate * (elapsed / (double) duration);
            return amount + interest;
        }
        
        public long getDueDate() {
            return startTime + duration;
        }
        
        public String getId() { return id; }
        public double getAmount() { return amount; }
        public double getInterestRate() { return interestRate; }
        public long getStartTime() { return startTime; }
        public long getDuration() { return duration; }
        public boolean isPaid() { return paid; }
        public void setPaid(boolean paid) { this.paid = paid; }
    }
    
    /**
     * Bank Branch class
     */
    public static class BankBranch {
        private String id;
        private String name;
        private Location location;
        
        public BankBranch(String id, String name, Location location) {
            this.id = id;
            this.name = name;
            this.location = location;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public Location getLocation() { return location; }
    }
}

