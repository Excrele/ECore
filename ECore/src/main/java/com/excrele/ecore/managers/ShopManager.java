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