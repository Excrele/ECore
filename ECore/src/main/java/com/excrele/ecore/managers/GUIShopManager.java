package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * GUI Shop System Manager
 * A fully featured GUI shop system with balanced pricing and dynamic inflation adjustment
 * Can be toggled on/off in config to allow other shop systems to take priority
 */
public class GUIShopManager implements Listener {
    private final Ecore plugin;
    private File guiShopFile;
    private FileConfiguration guiShopConfig;
    private boolean enabled;
    private boolean dynamicPricingEnabled;
    private double adjustmentRate;
    private double minPriceMultiplier;
    private double maxPriceMultiplier;
    private double buyInflationRate;
    private double sellDeflationRate;
    private int transactionThreshold;
    
    public GUIShopManager(Ecore plugin) {
        this.plugin = plugin;
        initializeConfig();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void initializeConfig() {
        guiShopFile = new File(plugin.getDataFolder(), "gui-shops.yml");
        if (!guiShopFile.exists()) {
            try {
                guiShopFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create gui-shops.yml", e);
            }
        }
        guiShopConfig = YamlConfiguration.loadConfiguration(guiShopFile);
        
        // Check if enabled in config
        enabled = plugin.getConfig().getBoolean("shops.gui-shop-enabled", true);
        
        // Load dynamic pricing settings
        dynamicPricingEnabled = plugin.getConfig().getBoolean("shops.dynamic-pricing.enabled", true);
        adjustmentRate = plugin.getConfig().getDouble("shops.dynamic-pricing.adjustment-rate", 0.05);
        minPriceMultiplier = plugin.getConfig().getDouble("shops.dynamic-pricing.min-price-multiplier", 0.1);
        maxPriceMultiplier = plugin.getConfig().getDouble("shops.dynamic-pricing.max-price-multiplier", 10.0);
        buyInflationRate = plugin.getConfig().getDouble("shops.dynamic-pricing.buy-inflation-rate", 0.02);
        sellDeflationRate = plugin.getConfig().getDouble("shops.dynamic-pricing.sell-deflation-rate", 0.02);
        transactionThreshold = plugin.getConfig().getInt("shops.dynamic-pricing.transaction-threshold", 10);
        
        // Create default shop items if config is empty
        if (guiShopConfig.getKeys(false).isEmpty()) {
            createDefaultShopItems();
        }
        
        // Initialize dynamic pricing data for existing items
        initializeDynamicPricingData();
    }
    
    private void createDefaultShopItems() {
        // Example shop items with balanced pricing
        createShopItem("diamond", Material.DIAMOND, 100.0, 50.0, "Mining", "Rare gem");
        createShopItem("iron_ingot", Material.IRON_INGOT, 5.0, 2.5, "Mining", "Common metal");
        createShopItem("gold_ingot", Material.GOLD_INGOT, 10.0, 5.0, "Mining", "Precious metal");
        createShopItem("coal", Material.COAL, 2.0, 1.0, "Mining", "Fuel source");
        saveConfig();
    }
    
    private void createShopItem(String id, Material material, double buyPrice, double sellPrice, String category, String description) {
        guiShopConfig.set("items." + id + ".material", material.toString());
        guiShopConfig.set("items." + id + ".buy-price", buyPrice);
        guiShopConfig.set("items." + id + ".sell-price", sellPrice);
        guiShopConfig.set("items." + id + ".category", category);
        guiShopConfig.set("items." + id + ".description", description);
        
        // Initialize base prices for dynamic pricing
        if (dynamicPricingEnabled) {
            guiShopConfig.set("items." + id + ".base-buy-price", buyPrice);
            guiShopConfig.set("items." + id + ".base-sell-price", sellPrice);
            guiShopConfig.set("items." + id + ".buy-count", 0);
            guiShopConfig.set("items." + id + ".sell-count", 0);
        }
    }
    
    /**
     * Initialize dynamic pricing data for existing items that don't have it
     */
    private void initializeDynamicPricingData() {
        if (!dynamicPricingEnabled || !guiShopConfig.contains("items")) {
            return;
        }
        
        boolean needsSave = false;
        for (String itemId : guiShopConfig.getConfigurationSection("items").getKeys(false)) {
            String baseBuyPath = "items." + itemId + ".base-buy-price";
            String baseSellPath = "items." + itemId + ".base-sell-price";
            
            // If base prices don't exist, initialize them from current prices
            if (!guiShopConfig.contains(baseBuyPath)) {
                double currentBuyPrice = guiShopConfig.getDouble("items." + itemId + ".buy-price");
                double currentSellPrice = guiShopConfig.getDouble("items." + itemId + ".sell-price");
                guiShopConfig.set(baseBuyPath, currentBuyPrice);
                guiShopConfig.set(baseSellPath, currentSellPrice);
                guiShopConfig.set("items." + itemId + ".buy-count", 0);
                guiShopConfig.set("items." + itemId + ".sell-count", 0);
                needsSave = true;
            }
        }
        
        if (needsSave) {
            saveConfig();
        }
    }
    
    /**
     * Open the main shop GUI
     */
    public void openShopGUI(Player player) {
        if (!enabled) {
            player.sendMessage(ChatColor.RED + "GUI Shop is disabled!");
            return;
        }
        
        Inventory gui = Bukkit.createInventory(player, 54, ChatColor.DARK_GREEN + "Shop");
        
        // Get categories
        Set<String> categories = getCategories();
        int slot = 0;
        
        // Add category buttons
        for (String category : categories) {
            if (slot >= 9) break;
            ItemStack categoryItem = new ItemStack(Material.CHEST);
            ItemMeta meta = categoryItem.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + category);
            meta.setLore(List.of(ChatColor.GRAY + "Click to browse " + category));
            categoryItem.setItemMeta(meta);
            gui.setItem(slot++, categoryItem);
        }
        
        // Add shop items
        slot = 9;
        for (String itemId : guiShopConfig.getConfigurationSection("items").getKeys(false)) {
            if (slot >= 45) break;
            ItemStack shopItem = createShopItemStack(itemId);
            if (shopItem != null) {
                gui.setItem(slot++, shopItem);
            }
        }
        
        player.openInventory(gui);
    }
    
    /**
     * Open category GUI
     */
    public void openCategoryGUI(Player player, String category) {
        Inventory gui = Bukkit.createInventory(player, 54, ChatColor.DARK_GREEN + "Shop - " + category);
        
        int slot = 0;
        for (String itemId : guiShopConfig.getConfigurationSection("items").getKeys(false)) {
            if (slot >= 45) break;
            String itemCategory = guiShopConfig.getString("items." + itemId + ".category");
            if (category.equalsIgnoreCase(itemCategory)) {
                ItemStack shopItem = createShopItemStack(itemId);
                if (shopItem != null) {
                    gui.setItem(slot++, shopItem);
                }
            }
        }
        
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Back");
        back.setItemMeta(backMeta);
        gui.setItem(49, back);
        
        player.openInventory(gui);
    }
    
    private ItemStack createShopItemStack(String itemId) {
        Material material = Material.valueOf(guiShopConfig.getString("items." + itemId + ".material"));
        double buyPrice = getCurrentBuyPrice(itemId);
        double sellPrice = getCurrentSellPrice(itemId);
        String description = guiShopConfig.getString("items." + itemId + ".description", "");
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + material.name().replace("_", " "));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + description);
        lore.add("");
        
        // Show price change indicator if dynamic pricing is enabled
        if (dynamicPricingEnabled) {
            double baseBuyPrice = guiShopConfig.getDouble("items." + itemId + ".base-buy-price", buyPrice);
            double baseSellPrice = guiShopConfig.getDouble("items." + itemId + ".base-sell-price", sellPrice);
            
            String buyChange = getPriceChangeIndicator(buyPrice, baseBuyPrice);
            String sellChange = getPriceChangeIndicator(sellPrice, baseSellPrice);
            
            lore.add(ChatColor.GREEN + "Buy: " + ChatColor.WHITE + String.format("%.2f", buyPrice) + buyChange);
            lore.add(ChatColor.RED + "Sell: " + ChatColor.WHITE + String.format("%.2f", sellPrice) + sellChange);
        } else {
            lore.add(ChatColor.GREEN + "Buy: " + ChatColor.WHITE + String.format("%.2f", buyPrice));
            lore.add(ChatColor.RED + "Sell: " + ChatColor.WHITE + String.format("%.2f", sellPrice));
        }
        
        lore.add("");
        lore.add(ChatColor.YELLOW + "Left-click to buy");
        lore.add(ChatColor.YELLOW + "Right-click to sell");
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Get price change indicator for display
     */
    private String getPriceChangeIndicator(double currentPrice, double basePrice) {
        if (currentPrice > basePrice) {
            double percentChange = ((currentPrice - basePrice) / basePrice) * 100;
            return ChatColor.RED + " (+" + String.format("%.1f", percentChange) + "%)";
        } else if (currentPrice < basePrice) {
            double percentChange = ((basePrice - currentPrice) / basePrice) * 100;
            return ChatColor.GREEN + " (-" + String.format("%.1f", percentChange) + "%)";
        }
        return "";
    }
    
    /**
     * Get current buy price (with dynamic pricing if enabled)
     */
    private double getCurrentBuyPrice(String itemId) {
        if (!dynamicPricingEnabled) {
            return guiShopConfig.getDouble("items." + itemId + ".buy-price");
        }
        
        double basePrice = guiShopConfig.getDouble("items." + itemId + ".base-buy-price");
        int buyCount = guiShopConfig.getInt("items." + itemId + ".buy-count", 0);
        int sellCount = guiShopConfig.getInt("items." + itemId + ".sell-count", 0);
        
        // Calculate price based on supply and demand
        // Net demand = buys - sells (positive = high demand, negative = high supply)
        int netDemand = buyCount - sellCount;
        
        // Calculate price multiplier based on net demand
        // Use a formula that provides diminishing returns to prevent extreme price changes
        double priceMultiplier = 1.0;
        
        if (netDemand > 0) {
            // High demand: price increases
            // Use square root scaling for diminishing returns
            double demandFactor = Math.sqrt(netDemand) * buyInflationRate;
            priceMultiplier += demandFactor;
        } else if (netDemand < 0) {
            // High supply: price decreases
            double supplyFactor = Math.sqrt(Math.abs(netDemand)) * sellDeflationRate;
            priceMultiplier -= supplyFactor;
        }
        
        // Apply additional adjustment for significant transaction imbalances
        if (Math.abs(netDemand) >= transactionThreshold) {
            double thresholdFactor = (netDemand / (double) transactionThreshold) * adjustmentRate;
            priceMultiplier += thresholdFactor;
        }
        
        // Clamp to min/max multipliers
        priceMultiplier = Math.max(minPriceMultiplier, Math.min(maxPriceMultiplier, priceMultiplier));
        
        double currentPrice = basePrice * priceMultiplier;
        
        // Update stored price
        guiShopConfig.set("items." + itemId + ".buy-price", currentPrice);
        
        return currentPrice;
    }
    
    /**
     * Get current sell price (with dynamic pricing if enabled)
     */
    private double getCurrentSellPrice(String itemId) {
        if (!dynamicPricingEnabled) {
            return guiShopConfig.getDouble("items." + itemId + ".sell-price");
        }
        
        double basePrice = guiShopConfig.getDouble("items." + itemId + ".base-sell-price");
        int buyCount = guiShopConfig.getInt("items." + itemId + ".buy-count", 0);
        int sellCount = guiShopConfig.getInt("items." + itemId + ".sell-count", 0);
        
        // Calculate price based on supply and demand
        // Net demand = buys - sells (positive = high demand, negative = high supply)
        int netDemand = buyCount - sellCount;
        
        // Calculate price multiplier based on net demand
        // Sell price follows buy price but with less volatility
        double priceMultiplier = 1.0;
        
        if (netDemand > 0) {
            // High demand: sell price increases (but less than buy price)
            double demandFactor = Math.sqrt(netDemand) * buyInflationRate * 0.5;
            priceMultiplier += demandFactor;
        } else if (netDemand < 0) {
            // High supply: sell price decreases more significantly
            double supplyFactor = Math.sqrt(Math.abs(netDemand)) * sellDeflationRate;
            priceMultiplier -= supplyFactor;
        }
        
        // Apply additional adjustment for significant transaction imbalances (reduced effect)
        if (Math.abs(netDemand) >= transactionThreshold) {
            double thresholdFactor = (netDemand / (double) transactionThreshold) * adjustmentRate * 0.5;
            priceMultiplier += thresholdFactor;
        }
        
        // Clamp to min/max multipliers
        priceMultiplier = Math.max(minPriceMultiplier, Math.min(maxPriceMultiplier, priceMultiplier));
        
        double currentPrice = basePrice * priceMultiplier;
        
        // Update stored price
        guiShopConfig.set("items." + itemId + ".sell-price", currentPrice);
        
        return currentPrice;
    }
    
    private Set<String> getCategories() {
        Set<String> categories = new java.util.HashSet<>();
        if (guiShopConfig.contains("items")) {
            for (String itemId : guiShopConfig.getConfigurationSection("items").getKeys(false)) {
                String category = guiShopConfig.getString("items." + itemId + ".category", "Other");
                categories.add(category);
            }
        }
        return categories;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (!title.startsWith(ChatColor.DARK_GREEN + "Shop")) return;
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (title.equals(ChatColor.DARK_GREEN + "Shop")) {
            // Main shop GUI
            if (clicked.getType() == Material.CHEST) {
                String category = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                openCategoryGUI(player, category);
            } else if (clicked.getType() != Material.AIR) {
                // Buy item
                handleBuyItem(player, clicked);
            }
        } else if (title.startsWith(ChatColor.DARK_GREEN + "Shop - ")) {
            // Category GUI
            if (clicked.getType() == Material.ARROW) {
                openShopGUI(player);
            } else if (clicked.getType() != Material.AIR) {
                if (event.isLeftClick()) {
                    handleBuyItem(player, clicked);
                } else if (event.isRightClick()) {
                    handleSellItem(player, clicked);
                }
            }
        }
    }
    
    private void handleBuyItem(Player player, ItemStack shopItem) {
        Material material = shopItem.getType();
        String itemId = findItemIdByMaterial(material);
        if (itemId == null) return;
        
        double buyPrice = getCurrentBuyPrice(itemId);
        
        if (plugin.getEconomyManager().getBalance(player.getUniqueId()) < buyPrice) {
            player.sendMessage(ChatColor.RED + "You don't have enough money! Required: " + 
                ChatColor.YELLOW + String.format("%.2f", buyPrice));
            return;
        }
        
        // Give item
        ItemStack item = new ItemStack(material, 1);
        player.getInventory().addItem(item);
        
        // Charge player
        plugin.getEconomyManager().removeBalance(player.getUniqueId(), buyPrice);
        
        // Update transaction count for dynamic pricing
        if (dynamicPricingEnabled) {
            int buyCount = guiShopConfig.getInt("items." + itemId + ".buy-count", 0);
            guiShopConfig.set("items." + itemId + ".buy-count", buyCount + 1);
            saveConfig();
        }
        
        player.sendMessage(ChatColor.GREEN + "Bought " + material.name() + " for " + 
            ChatColor.YELLOW + String.format("%.2f", buyPrice));
    }
    
    private void handleSellItem(Player player, ItemStack shopItem) {
        Material material = shopItem.getType();
        String itemId = findItemIdByMaterial(material);
        if (itemId == null) return;
        
        double sellPrice = getCurrentSellPrice(itemId);
        
        // Check if player has the item
        if (!player.getInventory().contains(material)) {
            player.sendMessage(ChatColor.RED + "You don't have any " + material.name() + "!");
            return;
        }
        
        // Remove item and pay player
        player.getInventory().removeItem(new ItemStack(material, 1));
        plugin.getEconomyManager().addBalance(player.getUniqueId(), sellPrice);
        
        // Update transaction count for dynamic pricing
        if (dynamicPricingEnabled) {
            int sellCount = guiShopConfig.getInt("items." + itemId + ".sell-count", 0);
            guiShopConfig.set("items." + itemId + ".sell-count", sellCount + 1);
            saveConfig();
        }
        
        player.sendMessage(ChatColor.GREEN + "Sold " + material.name() + " for " + 
            ChatColor.YELLOW + String.format("%.2f", sellPrice));
    }
    
    private String findItemIdByMaterial(Material material) {
        if (guiShopConfig.contains("items")) {
            for (String itemId : guiShopConfig.getConfigurationSection("items").getKeys(false)) {
                Material itemMaterial = Material.valueOf(guiShopConfig.getString("items." + itemId + ".material"));
                if (itemMaterial == material) {
                    return itemId;
                }
            }
        }
        return null;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getConfig().set("shops.gui-shop-enabled", enabled);
        plugin.getConfigManager().saveConfig();
    }
    
    /**
     * Enable or disable dynamic pricing system
     */
    public void setDynamicPricingEnabled(boolean enabled) {
        this.dynamicPricingEnabled = enabled;
        plugin.getConfig().set("shops.dynamic-pricing.enabled", enabled);
        plugin.getConfigManager().saveConfig();
    }
    
    /**
     * Check if dynamic pricing is enabled
     */
    public boolean isDynamicPricingEnabled() {
        return dynamicPricingEnabled;
    }
    
    /**
     * Reset prices and transaction counts for a specific item
     * Useful for admin commands to reset economy
     */
    public void resetItemPrices(String itemId) {
        if (!guiShopConfig.contains("items." + itemId)) {
            return;
        }
        
        double baseBuyPrice = guiShopConfig.getDouble("items." + itemId + ".base-buy-price");
        double baseSellPrice = guiShopConfig.getDouble("items." + itemId + ".base-sell-price");
        
        guiShopConfig.set("items." + itemId + ".buy-price", baseBuyPrice);
        guiShopConfig.set("items." + itemId + ".sell-price", baseSellPrice);
        guiShopConfig.set("items." + itemId + ".buy-count", 0);
        guiShopConfig.set("items." + itemId + ".sell-count", 0);
        
        saveConfig();
    }
    
    /**
     * Reset all item prices and transaction counts
     */
    public void resetAllPrices() {
        if (!guiShopConfig.contains("items")) {
            return;
        }
        
        for (String itemId : guiShopConfig.getConfigurationSection("items").getKeys(false)) {
            resetItemPrices(itemId);
        }
    }
    
    private void saveConfig() {
        try {
            guiShopConfig.save(guiShopFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save gui-shops.yml", e);
        }
    }
}

