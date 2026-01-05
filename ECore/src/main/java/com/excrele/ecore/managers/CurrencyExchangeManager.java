package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Currency Exchange System Manager
 * Handles multiple currencies, exchange rates, conversion, trading, inflation, events
 */
public class CurrencyExchangeManager {
    private final Ecore plugin;
    private File currenciesFile;
    private FileConfiguration currenciesConfig;
    private final Map<String, Currency> currencies; // Currency ID -> Currency
    private final Map<UUID, Map<String, Double>> playerBalances; // Player UUID -> Currency ID -> Balance
    private final Map<String, Double> exchangeRates; // "currency1:currency2" -> rate
    
    public CurrencyExchangeManager(Ecore plugin) {
        this.plugin = plugin;
        this.currencies = new HashMap<>();
        this.playerBalances = new HashMap<>();
        this.exchangeRates = new HashMap<>();
        initializeConfig();
        loadCurrencies();
        loadExchangeRates();
    }
    
    private void initializeConfig() {
        currenciesFile = new File(plugin.getDataFolder(), "currencies.yml");
        if (!currenciesFile.exists()) {
            try {
                currenciesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create currencies.yml", e);
            }
        }
        currenciesConfig = YamlConfiguration.loadConfiguration(currenciesFile);
    }
    
    private void loadCurrencies() {
        if (currenciesConfig.contains("currencies")) {
            for (String currencyId : currenciesConfig.getConfigurationSection("currencies").getKeys(false)) {
                String path = "currencies." + currencyId;
                String name = currenciesConfig.getString(path + ".name");
                String symbol = currenciesConfig.getString(path + ".symbol", "$");
                String world = currenciesConfig.getString(path + ".world", "");
                double inflationRate = currenciesConfig.getDouble(path + ".inflation-rate", 0.0);
                
                currencies.put(currencyId, new Currency(currencyId, name, symbol, world, inflationRate));
            }
        } else {
            // Create default currency
            createCurrency("default", "Dollars", "$", "", 0.0);
        }
    }
    
    private void loadExchangeRates() {
        if (currenciesConfig.contains("exchange-rates")) {
            for (String pair : currenciesConfig.getConfigurationSection("exchange-rates").getKeys(false)) {
                double rate = currenciesConfig.getDouble("exchange-rates." + pair);
                exchangeRates.put(pair, rate);
            }
        }
    }
    
    /**
     * Create a new currency
     */
    public Currency createCurrency(String currencyId, String name, String symbol, String world, double inflationRate) {
        Currency currency = new Currency(currencyId, name, symbol, world, inflationRate);
        currencies.put(currencyId, currency);
        
        String path = "currencies." + currencyId;
        currenciesConfig.set(path + ".name", name);
        currenciesConfig.set(path + ".symbol", symbol);
        currenciesConfig.set(path + ".world", world);
        currenciesConfig.set(path + ".inflation-rate", inflationRate);
        saveConfig();
        
        return currency;
    }
    
    /**
     * Set exchange rate between two currencies
     */
    public void setExchangeRate(String currency1, String currency2, double rate) {
        String key = currency1 + ":" + currency2;
        exchangeRates.put(key, rate);
        
        // Also set reverse rate
        String reverseKey = currency2 + ":" + currency1;
        exchangeRates.put(reverseKey, 1.0 / rate);
        
        currenciesConfig.set("exchange-rates." + key, rate);
        currenciesConfig.set("exchange-rates." + reverseKey, 1.0 / rate);
        saveConfig();
    }
    
    /**
     * Get exchange rate
     */
    public double getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return 1.0;
        }
        
        String key = fromCurrency + ":" + toCurrency;
        return exchangeRates.getOrDefault(key, 1.0);
    }
    
    /**
     * Exchange currency
     */
    public boolean exchangeCurrency(Player player, String fromCurrency, String toCurrency, double amount) {
        UUID uuid = player.getUniqueId();
        
        // Check balance
        double balance = getBalance(uuid, fromCurrency);
        if (balance < amount) {
            return false;
        }
        
        // Calculate exchange
        double rate = getExchangeRate(fromCurrency, toCurrency);
        double converted = amount * rate;
        
        // Apply fee (5% default)
        double fee = converted * 0.05;
        double finalAmount = converted - fee;
        
        // Transfer
        removeBalance(uuid, fromCurrency, amount);
        addBalance(uuid, toCurrency, finalAmount);
        
        player.sendMessage(org.bukkit.ChatColor.GREEN + "Exchanged " + format(amount, fromCurrency) + 
                          " for " + format(finalAmount, toCurrency) + " (Fee: " + format(fee, toCurrency) + ")");
        
        return true;
    }
    
    /**
     * Get balance for a currency
     */
    public double getBalance(UUID playerUuid, String currencyId) {
        Map<String, Double> balances = playerBalances.get(playerUuid);
        if (balances == null) {
            return 0.0;
        }
        return balances.getOrDefault(currencyId, 0.0);
    }
    
    /**
     * Add balance
     */
    public void addBalance(UUID playerUuid, String currencyId, double amount) {
        Map<String, Double> balances = playerBalances.computeIfAbsent(playerUuid, k -> new HashMap<>());
        balances.put(currencyId, balances.getOrDefault(currencyId, 0.0) + amount);
        savePlayerBalance(playerUuid);
    }
    
    /**
     * Remove balance
     */
    public boolean removeBalance(UUID playerUuid, String currencyId, double amount) {
        double balance = getBalance(playerUuid, currencyId);
        if (balance < amount) {
            return false;
        }
        
        Map<String, Double> balances = playerBalances.computeIfAbsent(playerUuid, k -> new HashMap<>());
        balances.put(currencyId, balance - amount);
        savePlayerBalance(playerUuid);
        return true;
    }
    
    /**
     * Format currency amount
     */
    public String format(double amount, String currencyId) {
        Currency currency = currencies.get(currencyId);
        if (currency == null) {
            return String.format("%.2f", amount);
        }
        return currency.getSymbol() + String.format("%.2f", amount);
    }
    
    private void savePlayerBalance(UUID playerUuid) {
        Map<String, Double> balances = playerBalances.get(playerUuid);
        if (balances == null) return;
        
        String path = "players." + playerUuid.toString();
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            currenciesConfig.set(path + "." + entry.getKey(), entry.getValue());
        }
        saveConfig();
    }
    
    private void saveConfig() {
        try {
            currenciesConfig.save(currenciesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save currencies.yml", e);
        }
    }
    
    public Collection<Currency> getCurrencies() {
        return currencies.values();
    }
    
    public Currency getCurrency(String currencyId) {
        return currencies.get(currencyId);
    }
    
    /**
     * Currency class
     */
    public static class Currency {
        private String id;
        private String name;
        private String symbol;
        private String world;
        private double inflationRate;
        
        public Currency(String id, String name, String symbol, String world, double inflationRate) {
            this.id = id;
            this.name = name;
            this.symbol = symbol;
            this.world = world;
            this.inflationRate = inflationRate;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getSymbol() { return symbol; }
        public String getWorld() { return world; }
        public double getInflationRate() { return inflationRate; }
    }
}

