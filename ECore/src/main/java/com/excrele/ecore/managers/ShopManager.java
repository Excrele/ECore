package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopManager {
    private final Ecore plugin;
    private final FileConfiguration adminConfig;
    private final FileConfiguration playerConfig;
    private final Map<UUID, ShopCreationData> pendingCreations;

    public ShopManager(Ecore plugin) {
        this.plugin = plugin;
        this.adminConfig = plugin.getConfigManager().getAdminShopConfig();
        this.playerConfig = plugin.getConfigManager().getPlayerShopConfig();
        this.pendingCreations = new HashMap<>();
    }

    // Handle Admin Shop item selection
    public void handleAdminShopItem(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Please hold a valid item to set for the Admin Shop!");
            return;
        }
        UUID uuid = player.getUniqueId();
        ShopCreationData data = pendingCreations.getOrDefault(uuid, new ShopCreationData());
        data.setItem(item.clone());
        pendingCreations.put(uuid, data);
        player.sendMessage(ChatColor.YELLOW + "Item set to " + item.getType().name() + ". Please set quantity in chat (1-64).");
        plugin.registerPendingAction(player, "shopgui:admin:quantity");
    }

    // Start Admin Shop creation
    public void startAdminShopCreation(Player player, Sign sign) {
        if (!player.hasPermission("ecore.adminshop")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to create Admin Shops!");
            return;
        }
        UUID uuid = player.getUniqueId();
        ShopCreationData data = new ShopCreationData();
        data.setSignLocation(sign.getLocation());
        pendingCreations.put(uuid, data);
        player.sendMessage(ChatColor.YELLOW + "Please hold the item for the Admin Shop and right-click the sign again.");
    }

    // Start Player Shop creation
    public void startPlayerShopCreation(Player player, Sign sign, Chest chest) {
        if (!player.hasPermission("ecore.pshop")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to create Player Shops!");
            return;
        }
        UUID uuid = player.getUniqueId();
        ShopCreationData data = new ShopCreationData();
        data.setSignLocation(sign.getLocation());
        data.setChestLocation(chest.getLocation());
        pendingCreations.put(uuid, data);
        plugin.getShopGUIManager().openItemSelectionGUI(player, true);
    }

    // Complete shop creation
    public void completeShopCreation(Player player, boolean isPlayerShop, ItemStack item, int quantity, double buyPrice, double sellPrice) {
        UUID uuid = player.getUniqueId();
        ShopCreationData data = pendingCreations.get(uuid);
        if (data == null) {
            player.sendMessage(ChatColor.RED + "No pending shop creation found!");
            return;
        }

        // Check shop limit for player shops
        if (isPlayerShop) {
            int maxShops = plugin.getConfig().getInt("shops.max-shops-per-player", 10);
            int currentShops = getPlayerShopCount(uuid);
            if (currentShops >= maxShops) {
                player.sendMessage(ChatColor.RED + "You have reached the maximum number of shops (" + maxShops + ")!");
                pendingCreations.remove(uuid);
                return;
            }
        }

        Location signLoc = data.getSignLocation();
        Block block = signLoc.getBlock();
        if (!(block.getState() instanceof Sign)) {
            player.sendMessage(ChatColor.RED + "Sign no longer exists!");
            return;
        }
        Sign sign = (Sign) block.getState();

        FileConfiguration config = isPlayerShop ? playerConfig : adminConfig;
        String path = signLoc.getWorld().getName() + "." + signLoc.getBlockX() + "." + signLoc.getBlockY() + "." + signLoc.getBlockZ();
        config.set(path + ".item", item.getType().toString());
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            config.set(path + ".customName", item.getItemMeta().getDisplayName());
        }
        config.set(path + ".quantity", quantity);
        config.set(path + ".buyPrice", buyPrice);
        config.set(path + ".sellPrice", sellPrice);
        config.set(path + ".category", "default"); // Default category
        config.set(path + ".created", System.currentTimeMillis()); // Creation timestamp
        // Initialize statistics
        config.set(path + ".stats.views", 0);
        config.set(path + ".stats.sales", 0);
        config.set(path + ".stats.revenue", 0.0);
        config.set(path + ".stats.last-accessed", System.currentTimeMillis());
        if (isPlayerShop) {
            config.set(path + ".chestLocation", serializeLocation(data.getChestLocation()));
            config.set(path + ".owner", player.getUniqueId().toString());
        }
        plugin.getConfigManager().saveConfig(isPlayerShop ? "playershops.yml" : "adminshops.yml");

        updateShopSign(sign, item, quantity, buyPrice, sellPrice, isPlayerShop);
        pendingCreations.remove(uuid);

        String shopType = isPlayerShop ? "PlayerShop" : "AdminShop";
        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name();
        player.sendMessage(ChatColor.GREEN + shopType + " created successfully!");
        plugin.getDiscordManager().sendStaffLogNotification(
                isPlayerShop ? "playershop-log" : "adminshop-log",
                player.getName(),
                "created",
                itemName + " (Qty: " + quantity + ", Buy: " + buyPrice + ", Sell: " + sellPrice + ")",
                ""
        );
    }

    // Edit shop price
    public void editShopPrice(Player player, Sign sign, boolean isPlayerShop, boolean isBuyPrice, double newPrice) {
        if (!player.hasPermission(isPlayerShop ? "ecore.pshop.edit" : "ecore.adminshop.edit")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to edit this shop!");
            return;
        }

        Location loc = sign.getLocation();
        FileConfiguration config = isPlayerShop ? playerConfig : adminConfig;
        String path = loc.getWorld().getName() + "." + loc.getBlockX() + "." + loc.getBlockY() + "." + loc.getBlockZ();
        if (!config.contains(path)) {
            player.sendMessage(ChatColor.RED + "This shop does not exist in the configuration!");
            return;
        }

        String priceType = isBuyPrice ? "buyPrice" : "sellPrice";
        config.set(path + "." + priceType, newPrice);
        plugin.getConfigManager().saveConfig(isPlayerShop ? "playershops.yml" : "adminshops.yml");

        ItemStack item = new ItemStack(Material.valueOf(config.getString(path + ".item")));
        int quantity = config.getInt(path + ".quantity");
        double buyPrice = config.getDouble(path + ".buyPrice");
        double sellPrice = config.getDouble(path + ".sellPrice");
        updateShopSign(sign, item, quantity, buyPrice, sellPrice, isPlayerShop);

        String shopType = isPlayerShop ? "PlayerShop" : "AdminShop";
        player.sendMessage(ChatColor.GREEN + "Updated " + priceType + " to " + newPrice + " for " + shopType + "!");
        plugin.getDiscordManager().sendStaffLogNotification(
                isPlayerShop ? "playershop-log" : "adminshop-log",
                player.getName(),
                "edited " + priceType,
                config.getString(path + ".item"),
                String.valueOf(newPrice)
        );
    }

    // Edit shop quantity
    public void editShopQuantity(Player player, Sign sign, boolean isPlayerShop, int newQuantity) {
        if (!player.hasPermission(isPlayerShop ? "ecore.pshop.edit" : "ecore.adminshop.edit")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to edit this shop!");
            return;
        }

        if (newQuantity < 1 || newQuantity > 64) {
            player.sendMessage(ChatColor.RED + "Quantity must be between 1 and 64!");
            return;
        }

        Location loc = sign.getLocation();
        FileConfiguration config = isPlayerShop ? playerConfig : adminConfig;
        String path = loc.getWorld().getName() + "." + loc.getBlockX() + "." + loc.getBlockY() + "." + loc.getBlockZ();
        if (!config.contains(path)) {
            player.sendMessage(ChatColor.RED + "This shop does not exist in the configuration!");
            return;
        }

        config.set(path + ".quantity", newQuantity);
        plugin.getConfigManager().saveConfig(isPlayerShop ? "playershops.yml" : "adminshops.yml");

        ItemStack item = new ItemStack(Material.valueOf(config.getString(path + ".item")));
        double buyPrice = config.getDouble(path + ".buyPrice");
        double sellPrice = config.getDouble(path + ".sellPrice");
        updateShopSign(sign, item, newQuantity, buyPrice, sellPrice, isPlayerShop);

        String shopType = isPlayerShop ? "PlayerShop" : "AdminShop";
        player.sendMessage(ChatColor.GREEN + "Updated quantity to " + newQuantity + " for " + shopType + "!");
        plugin.getDiscordManager().sendStaffLogNotification(
                isPlayerShop ? "playershop-log" : "adminshop-log",
                player.getName(),
                "edited quantity",
                config.getString(path + ".item"),
                String.valueOf(newQuantity)
        );
    }

    // Update shop sign
    private void updateShopSign(Sign sign, ItemStack item, int quantity, double buyPrice, double sellPrice, boolean isPlayerShop) {
        List<String> format = plugin.getConfig().getStringList(isPlayerShop ? "shops.player-sign-format" : "shops.admin-sign-format");
        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name();
        for (int i = 0; i < format.size() && i < 4; i++) {
            String line = format.get(i)
                    .replace("%buy_price%", String.format("%.2f", buyPrice))
                    .replace("%sell_price%", String.format("%.2f", sellPrice))
                    .replace("%item%", itemName)
                    .replace("%quantity%", String.valueOf(quantity));
            sign.setLine(i, ChatColor.translateAlternateColorCodes('&', line));
        }
        sign.update();
    }

    // Serialize location
    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    // Get shop creation data
    public ShopCreationData getPendingCreation(UUID uuid) {
        return pendingCreations.get(uuid);
    }

    // Remove pending creation
    public void removePendingCreation(UUID uuid) {
        pendingCreations.remove(uuid);
    }

    // Advanced Shop Features

    /**
     * Set shop category
     */
    public boolean setShopCategory(Location signLoc, boolean isPlayerShop, String category) {
        FileConfiguration config = isPlayerShop ? playerConfig : adminConfig;
        String path = getShopPath(signLoc);
        if (!config.contains(path)) {
            return false;
        }
        config.set(path + ".category", category);
        plugin.getConfigManager().saveConfig(isPlayerShop ? "playershops.yml" : "adminshops.yml");
        return true;
    }

    /**
     * Get shop category
     */
    public String getShopCategory(Location signLoc, boolean isPlayerShop) {
        FileConfiguration config = isPlayerShop ? playerConfig : adminConfig;
        String path = getShopPath(signLoc);
        return config.getString(path + ".category", "default");
    }

    /**
     * Get all shops by category
     */
    public List<Location> getShopsByCategory(boolean isPlayerShop, String category) {
        FileConfiguration config = isPlayerShop ? playerConfig : adminConfig;
        List<Location> shops = new ArrayList<>();
        
        for (String worldName : config.getKeys(false)) {
            if (config.isConfigurationSection(worldName)) {
                for (String x : config.getConfigurationSection(worldName).getKeys(false)) {
                    if (config.isConfigurationSection(worldName + "." + x)) {
                        for (String y : config.getConfigurationSection(worldName + "." + x).getKeys(false)) {
                            if (config.isConfigurationSection(worldName + "." + x + "." + y)) {
                                for (String z : config.getConfigurationSection(worldName + "." + x + "." + y).getKeys(false)) {
                                    String path = worldName + "." + x + "." + y + "." + z;
                                    String shopCategory = config.getString(path + ".category", "default");
                                    if (shopCategory.equalsIgnoreCase(category)) {
                                        try {
                                            Location loc = new Location(
                                                plugin.getServer().getWorld(worldName),
                                                Integer.parseInt(x),
                                                Integer.parseInt(y),
                                                Integer.parseInt(z)
                                            );
                                            shops.add(loc);
                                        } catch (Exception e) {
                                            // Invalid location, skip
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return shops;
    }

    /**
     * Search shops by item name
     */
    public List<Location> searchShops(boolean isPlayerShop, String searchTerm) {
        FileConfiguration config = isPlayerShop ? playerConfig : adminConfig;
        List<Location> shops = new ArrayList<>();
        String searchLower = searchTerm.toLowerCase();
        
        for (String worldName : config.getKeys(false)) {
            if (config.isConfigurationSection(worldName)) {
                for (String x : config.getConfigurationSection(worldName).getKeys(false)) {
                    if (config.isConfigurationSection(worldName + "." + x)) {
                        for (String y : config.getConfigurationSection(worldName + "." + x).getKeys(false)) {
                            if (config.isConfigurationSection(worldName + "." + x + "." + y)) {
                                for (String z : config.getConfigurationSection(worldName + "." + x + "." + y).getKeys(false)) {
                                    String path = worldName + "." + x + "." + y + "." + z;
                                    String item = config.getString(path + ".item", "");
                                    String customName = config.getString(path + ".customName", "");
                                    
                                    if (item.toLowerCase().contains(searchLower) || 
                                        customName.toLowerCase().contains(searchLower)) {
                                        try {
                                            Location loc = new Location(
                                                plugin.getServer().getWorld(worldName),
                                                Integer.parseInt(x),
                                                Integer.parseInt(y),
                                                Integer.parseInt(z)
                                            );
                                            shops.add(loc);
                                        } catch (Exception e) {
                                            // Invalid location, skip
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return shops;
    }

    /**
     * Add shop to favorites
     */
    public boolean addShopToFavorites(Player player, Location signLoc, boolean isPlayerShop) {
        String uuid = player.getUniqueId().toString();
        String shopPath = serializeLocation(signLoc);
        String configPath = "favorites." + uuid;
        
        FileConfiguration config = plugin.getConfigManager().getConfig();
        List<String> favorites = config.getStringList(configPath);
        if (!favorites.contains(shopPath + ":" + (isPlayerShop ? "player" : "admin"))) {
            favorites.add(shopPath + ":" + (isPlayerShop ? "player" : "admin"));
            config.set(configPath, favorites);
            plugin.saveConfig();
            return true;
        }
        return false;
    }

    /**
     * Remove shop from favorites
     */
    public boolean removeShopFromFavorites(Player player, Location signLoc, boolean isPlayerShop) {
        String uuid = player.getUniqueId().toString();
        String shopPath = serializeLocation(signLoc);
        String configPath = "favorites." + uuid;
        
        FileConfiguration config = plugin.getConfigManager().getConfig();
        List<String> favorites = config.getStringList(configPath);
        String toRemove = shopPath + ":" + (isPlayerShop ? "player" : "admin");
        if (favorites.remove(toRemove)) {
            config.set(configPath, favorites);
            plugin.saveConfig();
            return true;
        }
        return false;
    }

    /**
     * Get player's favorite shops
     */
    public List<Location> getFavoriteShops(Player player) {
        String uuid = player.getUniqueId().toString();
        String configPath = "favorites." + uuid;
        FileConfiguration config = plugin.getConfigManager().getConfig();
        List<String> favorites = config.getStringList(configPath);
        List<Location> locations = new ArrayList<>();
        
        for (String fav : favorites) {
            String[] parts = fav.split(":");
            if (parts.length >= 4) {
                try {
                    String worldName = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);
                    Location loc = new Location(plugin.getServer().getWorld(worldName), x, y, z);
                    locations.add(loc);
                } catch (Exception e) {
                    // Invalid location, skip
                }
            }
        }
        return locations;
    }

    /**
     * Track shop view
     */
    public void trackShopView(Location signLoc, boolean isPlayerShop) {
        FileConfiguration config = isPlayerShop ? playerConfig : adminConfig;
        String path = getShopPath(signLoc);
        if (config.contains(path)) {
            int views = config.getInt(path + ".stats.views", 0);
            config.set(path + ".stats.views", views + 1);
            config.set(path + ".stats.last-accessed", System.currentTimeMillis());
            plugin.getConfigManager().saveConfig(isPlayerShop ? "playershops.yml" : "adminshops.yml");
        }
    }

    /**
     * Track shop sale
     */
    public void trackShopSale(Location signLoc, boolean isPlayerShop, double revenue) {
        FileConfiguration config = isPlayerShop ? playerConfig : adminConfig;
        String path = getShopPath(signLoc);
        if (config.contains(path)) {
            int sales = config.getInt(path + ".stats.sales", 0);
            double totalRevenue = config.getDouble(path + ".stats.revenue", 0.0);
            config.set(path + ".stats.sales", sales + 1);
            config.set(path + ".stats.revenue", totalRevenue + revenue);
            config.set(path + ".stats.last-accessed", System.currentTimeMillis());
            plugin.getConfigManager().saveConfig(isPlayerShop ? "playershops.yml" : "adminshops.yml");
        }
    }

    /**
     * Get shop statistics
     */
    public ShopStatistics getShopStatistics(Location signLoc, boolean isPlayerShop) {
        FileConfiguration config = isPlayerShop ? playerConfig : adminConfig;
        String path = getShopPath(signLoc);
        if (!config.contains(path)) {
            return null;
        }
        
        int views = config.getInt(path + ".stats.views", 0);
        int sales = config.getInt(path + ".stats.sales", 0);
        double revenue = config.getDouble(path + ".stats.revenue", 0.0);
        long lastAccessed = config.getLong(path + ".stats.last-accessed", 0);
        
        return new ShopStatistics(views, sales, revenue, lastAccessed);
    }

    /**
     * Get number of shops owned by player
     */
    public int getPlayerShopCount(UUID playerUuid) {
        int count = 0;
        for (String worldName : playerConfig.getKeys(false)) {
            if (playerConfig.isConfigurationSection(worldName)) {
                for (String x : playerConfig.getConfigurationSection(worldName).getKeys(false)) {
                    if (playerConfig.isConfigurationSection(worldName + "." + x)) {
                        for (String y : playerConfig.getConfigurationSection(worldName + "." + x).getKeys(false)) {
                            if (playerConfig.isConfigurationSection(worldName + "." + x + "." + y)) {
                                for (String z : playerConfig.getConfigurationSection(worldName + "." + x + "." + y).getKeys(false)) {
                                    String path = worldName + "." + x + "." + y + "." + z;
                                    String owner = playerConfig.getString(path + ".owner");
                                    if (playerUuid.toString().equals(owner)) {
                                        count++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

    /**
     * Check and remove expired shops
     */
    public void checkExpiredShops() {
        long expirationTime = plugin.getConfig().getLong("shops.expiration-days", 0) * 24 * 60 * 60 * 1000L;
        if (expirationTime <= 0) {
            return; // Expiration disabled
        }
        
        long currentTime = System.currentTimeMillis();
        List<String> toRemove = new ArrayList<>();
        
        for (String worldName : playerConfig.getKeys(false)) {
            if (playerConfig.isConfigurationSection(worldName)) {
                for (String x : playerConfig.getConfigurationSection(worldName).getKeys(false)) {
                    if (playerConfig.isConfigurationSection(worldName + "." + x)) {
                        for (String y : playerConfig.getConfigurationSection(worldName + "." + x).getKeys(false)) {
                            if (playerConfig.isConfigurationSection(worldName + "." + x + "." + y)) {
                                for (String z : playerConfig.getConfigurationSection(worldName + "." + x + "." + y).getKeys(false)) {
                                    String path = worldName + "." + x + "." + y + "." + z;
                                    long lastAccessed = playerConfig.getLong(path + ".stats.last-accessed", 0);
                                    if (lastAccessed > 0 && (currentTime - lastAccessed) > expirationTime) {
                                        toRemove.add(path);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        for (String path : toRemove) {
            playerConfig.set(path, null);
        }
        
        if (!toRemove.isEmpty()) {
            plugin.getConfigManager().saveConfig("playershops.yml");
            plugin.getLogger().info("Removed " + toRemove.size() + " expired player shops.");
        }
    }

    /**
     * Get shop path from location
     */
    private String getShopPath(Location loc) {
        return loc.getWorld().getName() + "." + loc.getBlockX() + "." + loc.getBlockY() + "." + loc.getBlockZ();
    }

    /**
     * Check if a location is a valid shop
     */
    public boolean isShop(Location signLoc, boolean isPlayerShop) {
        FileConfiguration config = isPlayerShop ? playerConfig : adminConfig;
        String path = getShopPath(signLoc);
        return config.contains(path);
    }

    /**
     * Get shop data from location
     */
    public ShopData getShopData(Location signLoc, boolean isPlayerShop) {
        FileConfiguration config = isPlayerShop ? playerConfig : adminConfig;
        String path = getShopPath(signLoc);
        if (!config.contains(path)) {
            return null;
        }
        
        Material itemType = Material.valueOf(config.getString(path + ".item"));
        int quantity = config.getInt(path + ".quantity", 1);
        double buyPrice = config.getDouble(path + ".buyPrice", 0.0);
        double sellPrice = config.getDouble(path + ".sellPrice", 0.0);
        String customName = config.getString(path + ".customName");
        Location chestLoc = null;
        UUID owner = null;
        
        if (isPlayerShop) {
            String chestStr = config.getString(path + ".chestLocation");
            if (chestStr != null) {
                String[] parts = chestStr.split(",");
                if (parts.length == 4) {
                    chestLoc = new Location(
                        plugin.getServer().getWorld(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3])
                    );
                }
            }
            String ownerStr = config.getString(path + ".owner");
            if (ownerStr != null) {
                try {
                    owner = UUID.fromString(ownerStr);
                } catch (IllegalArgumentException e) {
                    // Invalid UUID
                }
            }
        }
        
        return new ShopData(itemType, quantity, buyPrice, sellPrice, customName, chestLoc, owner);
    }

    /**
     * Handle buying from a shop
     */
    public boolean buyFromShop(Player player, Location signLoc, boolean isPlayerShop) {
        ShopData data = getShopData(signLoc, isPlayerShop);
        if (data == null) {
            player.sendMessage(ChatColor.RED + "This shop is not configured!");
            return false;
        }

        if (data.getBuyPrice() <= 0) {
            player.sendMessage(ChatColor.RED + "This shop does not sell items!");
            return false;
        }

        double totalCost = data.getBuyPrice() * data.getQuantity();
        if (plugin.getEconomyManager().getBalance(player.getUniqueId()) < totalCost) {
            player.sendMessage(ChatColor.RED + "You don't have enough money! Required: " + 
                ChatColor.YELLOW + String.format("%.2f", totalCost));
            return false;
        }

        // Check stock first (for player shops) - before adding items to inventory
        if (isPlayerShop) {
            if (data.getChestLocation() != null) {
                Block chestBlock = data.getChestLocation().getBlock();
                if (chestBlock.getState() instanceof Chest) {
                    Chest chest = (Chest) chestBlock.getState();
                    org.bukkit.inventory.Inventory chestInv = chest.getInventory();
                    
                    // Check if chest has enough items
                    int available = 0;
                    for (ItemStack chestItem : chestInv.getContents()) {
                        if (chestItem != null && chestItem.getType() == data.getItemType()) {
                            available += chestItem.getAmount();
                        }
                    }
                    
                    if (available < data.getQuantity()) {
                        player.sendMessage(ChatColor.RED + "This shop is out of stock!");
                        return false;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Shop chest is missing!");
                    return false;
                }
            } else {
                player.sendMessage(ChatColor.RED + "Shop chest location is invalid!");
                return false;
            }
        }

        // Check inventory space by trying to add a test item
        ItemStack testItem = new ItemStack(data.getItemType(), data.getQuantity());
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(testItem);
        if (!overflow.isEmpty()) {
            // Remove what we just added
            int totalToRemove = 0;
            for (ItemStack overflowItem : overflow.values()) {
                totalToRemove += overflowItem.getAmount();
            }
            int removed = 0;
            for (int i = 0; i < player.getInventory().getSize() && removed < totalToRemove; i++) {
                ItemStack invItem = player.getInventory().getItem(i);
                if (invItem != null && invItem.getType() == data.getItemType()) {
                    int remove = Math.min(totalToRemove - removed, invItem.getAmount());
                    invItem.setAmount(invItem.getAmount() - remove);
                    removed += remove;
                    if (invItem.getAmount() <= 0) {
                        player.getInventory().setItem(i, null);
                    }
                }
            }
            player.sendMessage(ChatColor.RED + "Your inventory is full!");
            return false;
        }

        // Create the actual item with custom name if needed
        ItemStack item = new ItemStack(data.getItemType(), data.getQuantity());
        if (data.getCustomName() != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', data.getCustomName()));
                item.setItemMeta(meta);
            }
        }
        
        // Replace the test item with the actual item (preserving custom name)
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack invItem = player.getInventory().getItem(i);
            if (invItem != null && invItem.getType() == data.getItemType() && invItem.getAmount() == data.getQuantity()) {
                player.getInventory().setItem(i, item);
                break;
            }
        }

        // Handle payment and stock removal
        if (isPlayerShop) {
            // Player shop - take from chest and pay owner
            Block chestBlock = data.getChestLocation().getBlock();
            Chest chest = (Chest) chestBlock.getState();
            org.bukkit.inventory.Inventory chestInv = chest.getInventory();
            
            // Remove items from chest
                    int remaining = data.getQuantity();
                    for (int i = 0; i < chestInv.getSize() && remaining > 0; i++) {
                        ItemStack chestItem = chestInv.getItem(i);
                        if (chestItem != null && chestItem.getType() == data.getItemType()) {
                            int take = Math.min(remaining, chestItem.getAmount());
                            chestItem.setAmount(chestItem.getAmount() - take);
                            remaining -= take;
                            if (chestItem.getAmount() <= 0) {
                                chestInv.setItem(i, null);
                            }
                        }
                    }
                    
                    // Pay shop owner
                    if (data.getOwner() != null) {
                        plugin.getEconomyManager().addBalance(data.getOwner(), totalCost);
                        Player ownerPlayer = plugin.getServer().getPlayer(data.getOwner());
                        if (ownerPlayer != null && ownerPlayer.isOnline()) {
                            ownerPlayer.sendMessage(ChatColor.GREEN + player.getName() + " bought " + 
                                data.getQuantity() + "x " + (data.getCustomName() != null ? data.getCustomName() : data.getItemType().name()) + 
                                " from your shop for " + String.format("%.2f", totalCost));
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Shop chest is missing!");
                    return false;
                }
            } else {
                player.sendMessage(ChatColor.RED + "Shop chest location is invalid!");
                return false;
            }
        } else {
            // Admin shop - unlimited stock, just charge player
        }

        // Charge player
        plugin.getEconomyManager().removeBalance(player.getUniqueId(), totalCost);
        
        // Track statistics
        trackShopView(signLoc, isPlayerShop);
        trackShopSale(signLoc, isPlayerShop, totalCost);
        
        String itemName = data.getCustomName() != null ? data.getCustomName() : data.getItemType().name();
        player.sendMessage(ChatColor.GREEN + "You bought " + data.getQuantity() + "x " + itemName + 
            " for " + String.format("%.2f", totalCost));
        
        return true;
    }

    /**
     * Handle selling to a shop
     */
    public boolean sellToShop(Player player, Location signLoc, boolean isPlayerShop) {
        ShopData data = getShopData(signLoc, isPlayerShop);
        if (data == null) {
            player.sendMessage(ChatColor.RED + "This shop is not configured!");
            return false;
        }

        if (data.getSellPrice() <= 0) {
            player.sendMessage(ChatColor.RED + "This shop does not buy items!");
            return false;
        }

        // Check if player has the item
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem == null || heldItem.getType() != data.getItemType() || heldItem.getAmount() < data.getQuantity()) {
            player.sendMessage(ChatColor.RED + "You need to hold " + data.getQuantity() + "x " + 
                (data.getCustomName() != null ? data.getCustomName() : data.getItemType().name()) + "!");
            return false;
        }

        double totalPayment = data.getSellPrice() * data.getQuantity();

        if (isPlayerShop) {
            // Player shop - check if owner has enough money
            if (data.getOwner() != null) {
                if (plugin.getEconomyManager().getBalance(data.getOwner()) < totalPayment) {
                    player.sendMessage(ChatColor.RED + "The shop owner doesn't have enough money!");
                    return false;
                }
                
                // Check if chest has space
                if (data.getChestLocation() != null) {
                    Block chestBlock = data.getChestLocation().getBlock();
                    if (chestBlock.getState() instanceof Chest) {
                        Chest chest = (Chest) chestBlock.getState();
                        org.bukkit.inventory.Inventory chestInv = chest.getInventory();
                        
                        ItemStack toStore = new ItemStack(data.getItemType(), data.getQuantity());
                        if (data.getCustomName() != null) {
                            ItemMeta meta = toStore.getItemMeta();
                            if (meta != null) {
                                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', data.getCustomName()));
                                toStore.setItemMeta(meta);
                            }
                        }
                        
                        HashMap<Integer, ItemStack> overflow = chestInv.addItem(toStore);
                        if (!overflow.isEmpty()) {
                            player.sendMessage(ChatColor.RED + "The shop chest is full!");
                            return false;
                        }
                        
                        // Pay player and charge owner
                        plugin.getEconomyManager().addBalance(player.getUniqueId(), totalPayment);
                        plugin.getEconomyManager().removeBalance(data.getOwner(), totalPayment);
                        
                        // Remove items from player
                        heldItem.setAmount(heldItem.getAmount() - data.getQuantity());
                        if (heldItem.getAmount() <= 0) {
                            player.getInventory().setItemInMainHand(null);
                        }
                        
                        // Notify owner
                        Player ownerPlayer = plugin.getServer().getPlayer(data.getOwner());
                        if (ownerPlayer != null && ownerPlayer.isOnline()) {
                            ownerPlayer.sendMessage(ChatColor.GREEN + player.getName() + " sold " + 
                                data.getQuantity() + "x " + (data.getCustomName() != null ? data.getCustomName() : data.getItemType().name()) + 
                                " to your shop for " + String.format("%.2f", totalPayment));
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Shop chest is missing!");
                        return false;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Shop chest location is invalid!");
                    return false;
                }
            } else {
                player.sendMessage(ChatColor.RED + "Shop owner is invalid!");
                return false;
            }
        } else {
            // Admin shop - unlimited money, just pay player
            plugin.getEconomyManager().addBalance(player.getUniqueId(), totalPayment);
            
            // Remove items from player
            heldItem.setAmount(heldItem.getAmount() - data.getQuantity());
            if (heldItem.getAmount() <= 0) {
                player.getInventory().setItemInMainHand(null);
            }
        }

        // Track statistics
        trackShopView(signLoc, isPlayerShop);
        trackShopSale(signLoc, isPlayerShop, totalPayment);
        
        String itemName = data.getCustomName() != null ? data.getCustomName() : data.getItemType().name();
        player.sendMessage(ChatColor.GREEN + "You sold " + data.getQuantity() + "x " + itemName + 
            " for " + String.format("%.2f", totalPayment));
        
        return true;
    }

    /**
     * Shop data class
     */
    public static class ShopData {
        private final Material itemType;
        private final int quantity;
        private final double buyPrice;
        private final double sellPrice;
        private final String customName;
        private final Location chestLocation;
        private final UUID owner;

        public ShopData(Material itemType, int quantity, double buyPrice, double sellPrice, 
                       String customName, Location chestLocation, UUID owner) {
            this.itemType = itemType;
            this.quantity = quantity;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
            this.customName = customName;
            this.chestLocation = chestLocation;
            this.owner = owner;
        }

        public Material getItemType() { return itemType; }
        public int getQuantity() { return quantity; }
        public double getBuyPrice() { return buyPrice; }
        public double getSellPrice() { return sellPrice; }
        public String getCustomName() { return customName; }
        public Location getChestLocation() { return chestLocation; }
        public UUID getOwner() { return owner; }
    }

    /**
     * Shop statistics class
     */
    public static class ShopStatistics {
        private final int views;
        private final int sales;
        private final double revenue;
        private final long lastAccessed;

        public ShopStatistics(int views, int sales, double revenue, long lastAccessed) {
            this.views = views;
            this.sales = sales;
            this.revenue = revenue;
            this.lastAccessed = lastAccessed;
        }

        public int getViews() { return views; }
        public int getSales() { return sales; }
        public double getRevenue() { return revenue; }
        public long getLastAccessed() { return lastAccessed; }
    }

    // Shop creation data class
    public static class ShopCreationData {
        private Location signLocation;
        private Location chestLocation;
        private ItemStack item;
        private int quantity;
        private double buyPrice;
        private double sellPrice;

        public void setSignLocation(Location signLocation) {
            this.signLocation = signLocation;
        }

        public void setChestLocation(Location chestLocation) {
            this.chestLocation = chestLocation;
        }

        public void setItem(ItemStack item) {
            this.item = item;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public void setBuyPrice(double buyPrice) {
            this.buyPrice = buyPrice;
        }

        public void setSellPrice(double sellPrice) {
            this.sellPrice = sellPrice;
        }

        public Location getSignLocation() {
            return signLocation;
        }

        public Location getChestLocation() {
            return chestLocation;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getBuyPrice() {
            return buyPrice;
        }

        public double getSellPrice() {
            return sellPrice;
        }
    }
}