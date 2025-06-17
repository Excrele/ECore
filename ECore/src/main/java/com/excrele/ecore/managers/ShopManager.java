package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Manages Admin Shops and Player Shops
public class ShopManager {
    private final Ecore plugin;
    private final File adminShopsFile;
    private final YamlConfiguration adminShopsConfig;
    private final File playerShopsFile;
    private final YamlConfiguration playerShopsConfig;
    private final Map<UUID, ShopCreationState> pendingCreations;

    // Tracks shop creation state
    private static class ShopCreationState {
        String type; // "admin" or "player"
        Location location; // Sign for admin, chest for player
        Location signLocation; // Sign for player shops
        ItemStack item;
        int step; // 0: waiting for item, 1: quantity, 2: buy price, 3: sell price
        int quantity;
        double buyPrice;
    }

    public ShopManager(Ecore plugin) {
        this.plugin = plugin;
        // Initialize adminshops.yml
        adminShopsFile = new File(plugin.getDataFolder(), "adminshops.yml");
        if (!adminShopsFile.exists()) {
            plugin.saveResource("adminshops.yml", false);
        }
        adminShopsConfig = YamlConfiguration.loadConfiguration(adminShopsFile);
        // Initialize playershops.yml
        playerShopsFile = new File(plugin.getDataFolder(), "playershops.yml");
        if (!playerShopsFile.exists()) {
            plugin.saveResource("playershops.yml", false);
        }
        playerShopsConfig = YamlConfiguration.loadConfiguration(playerShopsFile);
        pendingCreations = new HashMap<>();
    }

    // Save shop configurations
    private void saveAdminShops() {
        try {
            adminShopsConfig.save(adminShopsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save adminshops.yml: " + e.getMessage());
        }
    }

    private void savePlayerShops() {
        try {
            playerShopsConfig.save(playerShopsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save playershops.yml: " + e.getMessage());
        }
    }

    // Start Admin Shop creation
    public void startAdminShopCreation(Player player, Location signLocation) {
        if (!player.hasPermission("ecore.adminshop")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to create Admin Shops!");
            return;
        }
        UUID uuid = player.getUniqueId();
        ShopCreationState state = new ShopCreationState();
        state.type = "admin";
        state.location = signLocation;
        state.step = 0;
        pendingCreations.put(uuid, state);
        player.sendMessage(ChatColor.YELLOW + "Right-click the sign with the item to sell.");
    }

    // Start Player Shop creation
    public void startPlayerShopCreation(Player player, Location chestLocation, Location signLocation) {
        if (!player.hasPermission("ecore.pshop")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to create Player Shops!");
            return;
        }
        Block block = chestLocation.getBlock();
        if (!(block.getState() instanceof Chest)) {
            player.sendMessage(ChatColor.RED + "The block is not a chest!");
            return;
        }
        UUID uuid = player.getUniqueId();
        ShopCreationState state = new ShopCreationState();
        state.type = "player";
        state.location = chestLocation;
        state.signLocation = signLocation;
        state.step = 0;
        pendingCreations.put(uuid, state);
        player.sendMessage(ChatColor.YELLOW + "Drop the item into the chest to set the shop's item.");
    }

    // Handle item selection for Admin Shop
    public void handleAdminShopItem(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        ShopCreationState state = pendingCreations.get(uuid);
        if (state == null || !state.type.equals("admin") || state.step != 0) return;
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Please hold a valid item!");
            return;
        }
        state.item = item.clone();
        state.item.setAmount(1);
        state.step = 1;
        plugin.registerPendingAction(player, "shop:admin:quantity");
        player.sendMessage(ChatColor.YELLOW + "Enter the quantity of items per transaction in chat.");
    }

    // Handle item drop for Player Shop
    public void handlePlayerShopItem(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        ShopCreationState state = pendingCreations.get(uuid);
        if (state == null || !state.type.equals("player") || state.step != 0) return;
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Please drop a valid item!");
            return;
        }
        state.item = item.clone();
        state.item.setAmount(1);
        state.step = 1;
        plugin.registerPendingAction(player, "shop:player:quantity");
        player.sendMessage(ChatColor.YELLOW + "Enter the quantity of items per transaction in chat.");
    }

    // Handle chat input for shop creation
    public void handleChatInput(Player player, String message, String action) {
        UUID uuid = player.getUniqueId();
        ShopCreationState state = pendingCreations.get(uuid);
        if (state == null) return;

        try {
            if (action.equals("shop:admin:quantity") || action.equals("shop:player:quantity")) {
                int quantity = Integer.parseInt(message);
                if (quantity <= 0 || quantity > 64) {
                    player.sendMessage(ChatColor.RED + "Quantity must be between 1 and 64!");
                    return;
                }
                state.quantity = quantity;
                state.step = 2;
                plugin.registerPendingAction(player, "shop:" + state.type + ":buyprice");
                player.sendMessage(ChatColor.YELLOW + "Enter the buy price (what players pay) in chat.");
            } else if (action.equals("shop:admin:buyprice") || action.equals("shop:player:buyprice")) {
                double buyPrice = Double.parseDouble(message);
                if (buyPrice < 0) {
                    player.sendMessage(ChatColor.RED + "Buy price cannot be negative!");
                    return;
                }
                state.buyPrice = buyPrice;
                state.step = 3;
                plugin.registerPendingAction(player, "shop:" + state.type + ":sellprice");
                player.sendMessage(ChatColor.YELLOW + "Enter the sell price (what players receive) in chat.");
            } else if (action.equals("shop:admin:sellprice") || action.equals("shop:player:sellprice")) {
                double sellPrice = Double.parseDouble(message);
                if (sellPrice < 0) {
                    player.sendMessage(ChatColor.RED + "Sell price cannot be negative!");
                    return;
                }
                completeShopCreation(player, state, sellPrice);
                pendingCreations.remove(uuid);
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Please enter a valid number!");
        }
    }

    // Complete shop creation
    private void completeShopCreation(Player player, ShopCreationState state, double sellPrice) {
        String locKey = state.location.getWorld().getName() + "," +
                state.location.getBlockX() + "," +
                state.location.getBlockY() + "," +
                state.location.getBlockZ();
        if (state.type.equals("admin")) {
            adminShopsConfig.set(locKey + ".item", state.item.getType().toString());
            adminShopsConfig.set(locKey + ".quantity", state.quantity);
            adminShopsConfig.set(locKey + ".buy-price", state.buyPrice);
            adminShopsConfig.set(locKey + ".sell-price", sellPrice);
            saveAdminShops();
            updateAdminShopSign(state.location, state.item.getType(), state.quantity, state.buyPrice, sellPrice);
            player.sendMessage(ChatColor.GREEN + "Admin Shop created!");
            plugin.getDiscordManager().sendPunishmentNotification(player.getName(), "created admin shop",
                    state.item.getType().toString(), "Quantity: " + state.quantity + ", Buy: " + state.buyPrice + ", Sell: " + sellPrice);
        } else {
            String signLocKey = state.signLocation.getWorld().getName() + "," +
                    state.signLocation.getBlockX() + "," +
                    state.signLocation.getBlockY() + "," +
                    state.signLocation.getBlockZ();
            playerShopsConfig.set(locKey + ".sign", signLocKey);
            playerShopsConfig.set(locKey + ".item", state.item.getType().toString());
            playerShopsConfig.set(locKey + ".quantity", state.quantity);
            playerShopsConfig.set(locKey + ".buy-price", state.buyPrice);
            playerShopsConfig.set(locKey + ".sell-price", sellPrice);
            playerShopsConfig.set(locKey + ".owner", player.getUniqueId().toString());
            savePlayerShops();
            updatePlayerShopSign(state.signLocation, state.item.getType(), state.quantity, state.buyPrice, sellPrice);
            player.sendMessage(ChatColor.GREEN + "Player Shop created!");
            plugin.getDiscordManager().sendPunishmentNotification(player.getName(), "created player shop",
                    state.item.getType().toString(), "Quantity: " + state.quantity + ", Buy: " + state.buyPrice + ", Sell: " + sellPrice);
        }
    }

    // Update Admin Shop sign text
    private void updateAdminShopSign(Location location, Material item, int quantity, double buyPrice, double sellPrice) {
        Block block = location.getBlock();
        if (!(block.getState() instanceof Sign)) return;
        Sign sign = (Sign) block.getState();
        sign.setLine(0, ChatColor.DARK_GREEN + "[Admin Shop]");
        sign.setLine(1, item.toString());
        sign.setLine(2, "Buy: " + buyPrice + " for " + quantity);
        sign.setLine(3, "Sell: " + sellPrice + " for " + quantity);
        sign.update();
    }

    // Update Player Shop sign text
    private void updatePlayerShopSign(Location location, Material item, int quantity, double buyPrice, double sellPrice) {
        Block block = location.getBlock();
        if (!(block.getState() instanceof Sign)) return;
        Sign sign = (Sign) block.getState();
        sign.setLine(0, ChatColor.DARK_BLUE + "[PShop]");
        sign.setLine(1, item.toString());
        sign.setLine(2, "Buy: " + buyPrice + " for " + quantity);
        sign.setLine(3, "Sell: " + sellPrice + " for " + quantity);
        sign.update();
    }

    // Handle Admin Shop interaction
    public void handleAdminShopInteraction(Player player, Location location, boolean isBuy) {
        String locKey = location.getWorld().getName() + "," +
                location.getBlockX() + "," +
                location.getBlockY() + "," +
                location.getBlockZ();
        if (!adminShopsConfig.contains(locKey)) {
            player.sendMessage(ChatColor.RED + "This Admin Shop is invalid!");
            return;
        }
        Material itemType = Material.valueOf(adminShopsConfig.getString(locKey + ".item"));
        int quantity = adminShopsConfig.getInt(locKey + ".quantity");
        double buyPrice = adminShopsConfig.getDouble(locKey + ".buy-price");
        double sellPrice = adminShopsConfig.getDouble(locKey + ".sell-price");

        if (isBuy) {
            if (!plugin.getEconomyManager().removeBalance(player.getUniqueId(), buyPrice)) {
                player.sendMessage(ChatColor.RED + "You don't have enough money!");
                return;
            }
            ItemStack item = new ItemStack(itemType, quantity);
            player.getInventory().addItem(item);
            player.sendMessage(ChatColor.GREEN + "Bought " + quantity + " " + itemType + " for " + buyPrice + "!");
        } else {
            ItemStack item = new ItemStack(itemType, quantity);
            if (!player.getInventory().containsAtLeast(item, quantity)) {
                player.sendMessage(ChatColor.RED + "You don't have enough items to sell!");
                return;
            }
            player.getInventory().removeItem(item);
            plugin.getEconomyManager().addBalance(player.getUniqueId(), sellPrice);
            player.sendMessage(ChatColor.GREEN + "Sold " + quantity + " " + itemType + " for " + sellPrice + "!");
        }
    }

    // Handle Player Shop interaction
    public void handlePlayerShopInteraction(Player player, Location signLocation, boolean isBuy) {
        String signLocKey = signLocation.getWorld().getName() + "," +
                signLocation.getBlockX() + "," +
                signLocation.getBlockY() + "," +
                signLocation.getBlockZ();
        String chestLocKey = playerShopsConfig.getKeys(false).stream()
                .filter(key -> signLocKey.equals(playerShopsConfig.getString(key + ".sign")))
                .findFirst()
                .orElse(null);
        if (chestLocKey == null) {
            player.sendMessage(ChatColor.RED + "This Player Shop is invalid!");
            return;
        }
        Location chestLocation = parseLocation(chestLocKey);
        if (chestLocation == null || !(chestLocation.getBlock().getState() instanceof Chest)) {
            player.sendMessage(ChatColor.RED + "The shop's chest is missing or invalid!");
            return;
        }
        Chest chest = (Chest) chestLocation.getBlock().getState();
        Material itemType = Material.valueOf(playerShopsConfig.getString(chestLocKey + ".item"));
        int quantity = playerShopsConfig.getInt(chestLocKey + ".quantity");
        double buyPrice = playerShopsConfig.getDouble(chestLocKey + ".buy-price");
        double sellPrice = playerShopsConfig.getDouble(chestLocKey + ".sell-price");
        UUID ownerUUID = UUID.fromString(playerShopsConfig.getString(chestLocKey + ".owner"));

        if (isBuy) {
            ItemStack item = new ItemStack(itemType, quantity);
            if (!chest.getInventory().containsAtLeast(item, quantity)) {
                player.sendMessage(ChatColor.RED + "The shop is out of stock!");
                return;
            }
            if (!plugin.getEconomyManager().removeBalance(player.getUniqueId(), buyPrice)) {
                player.sendMessage(ChatColor.RED + "You don't have enough money!");
                return;
            }
            chest.getInventory().removeItem(item);
            player.getInventory().addItem(item);
            plugin.getEconomyManager().addBalance(ownerUUID, buyPrice);
            player.sendMessage(ChatColor.GREEN + "Bought " + quantity + " " + itemType + " for " + buyPrice + "!");
        } else {
            ItemStack item = new ItemStack(itemType, quantity);
            if (!player.getInventory().containsAtLeast(item, quantity)) {
                player.sendMessage(ChatColor.RED + "You don't have enough items to sell!");
                return;
            }
            if (chest.getInventory().firstEmpty() == -1) {
                player.sendMessage(ChatColor.RED + "The shop's chest is full!");
                return;
            }
            player.getInventory().removeItem(item);
            chest.getInventory().addItem(item);
            plugin.getEconomyManager().addBalance(player.getUniqueId(), sellPrice);
            plugin.getEconomyManager().removeBalance(ownerUUID, sellPrice);
            player.sendMessage(ChatColor.GREEN + "Sold " + quantity + " " + itemType + " for " + sellPrice + "!");
        }
    }

    // Parse location string (world,x,y,z)
    private Location parseLocation(String locKey) {
        String[] parts = locKey.split(",");
        if (parts.length != 4) return null;
        try {
            return new Location(
                    plugin.getServer().getWorld(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])
            );
        } catch (Exception e) {
            return null;
        }
    }
}