package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages staff mode functionality similar to Staff++ plugin.
 * Handles entering/exiting staff mode, saving/restoring player state,
 * and applying staff mode features like vanish, flight, and invincibility.
 */
public class StaffModeManager {
    private final Ecore plugin;
    private final Map<UUID, StaffModeData> staffModePlayers;

    /**
     * Data class to store player state when entering staff mode.
     */
    public class StaffModeData {
        private final Location location;
        private final GameMode gameMode;
        private final ItemStack[] inventory;
        private final ItemStack[] armor;
        private final ItemStack offHand;
        private final int foodLevel;
        private final double health;
        private final int level;
        private final float exp;
        private final boolean allowFlight;
        private final boolean flying;

        public StaffModeData(Player player) {
            this.location = player.getLocation().clone();
            this.gameMode = player.getGameMode();
            PlayerInventory inv = player.getInventory();
            this.inventory = inv.getContents().clone();
            this.armor = inv.getArmorContents().clone();
            this.offHand = inv.getItemInOffHand() != null ? inv.getItemInOffHand().clone() : null;
            this.foodLevel = player.getFoodLevel();
            this.health = player.getHealth();
            this.level = player.getLevel();
            this.exp = player.getExp();
            this.allowFlight = player.getAllowFlight();
            this.flying = player.isFlying();
        }
    }

    public StaffModeManager(Ecore plugin) {
        this.plugin = plugin;
        this.staffModePlayers = new HashMap<>();
    }

    /**
     * Checks if a player is in staff mode.
     */
    public boolean isInStaffMode(Player player) {
        return staffModePlayers.containsKey(player.getUniqueId());
    }

    /**
     * Enters staff mode for a player.
     * Saves current state and applies staff mode features.
     */
    public void enterStaffMode(Player player) {
        if (isInStaffMode(player)) {
            player.sendMessage(ChatColor.RED + "You are already in staff mode!");
            return;
        }

        UUID uuid = player.getUniqueId();
        
        // Save current state
        StaffModeData data = new StaffModeData(player);
        staffModePlayers.put(uuid, data);

        // Clear inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);

        // Give staff mode items (if configured)
        giveStaffModeItems(player);

        // Apply staff mode features
        applyStaffModeFeatures(player);

        // Execute enter commands (if configured)
        executeEnterCommands(player);

        player.sendMessage(ChatColor.GREEN + "You have entered staff mode!");
        plugin.getDiscordManager().sendStaffLogNotification(
            "staff-mode-log",
            player.getName(),
            "entered staff mode",
            "",
            ""
        );
    }

    /**
     * Exits staff mode for a player.
     * Restores previous state and removes staff mode features.
     */
    public void exitStaffMode(Player player) {
        if (!isInStaffMode(player)) {
            player.sendMessage(ChatColor.RED + "You are not in staff mode!");
            return;
        }

        UUID uuid = player.getUniqueId();
        StaffModeData data = staffModePlayers.remove(uuid);
        
        if (data == null) {
            return;
        }

        // Remove staff mode features
        removeStaffModeFeatures(player);

        // Clear current inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);

        // Restore previous state
        restorePlayerState(player, data);

        // Execute exit commands (if configured)
        executeExitCommands(player);

        player.sendMessage(ChatColor.GREEN + "You have exited staff mode!");
        plugin.getDiscordManager().sendStaffLogNotification(
            "staff-mode-log",
            player.getName(),
            "exited staff mode",
            "",
            ""
        );
    }

    /**
     * Toggles staff mode for a player.
     */
    public void toggleStaffMode(Player player) {
        if (isInStaffMode(player)) {
            exitStaffMode(player);
        } else {
            enterStaffMode(player);
        }
    }

    /**
     * Gives staff mode items to the player.
     */
    private void giveStaffModeItems(Player player) {
        // Get configured items from config
        var config = plugin.getConfigManager().getConfig();
        var itemsSection = config.getConfigurationSection("staffmode.items");
        
        if (itemsSection == null) {
            // Default staff mode items
            giveDefaultStaffItems(player);
            return;
        }

        int slot = 0;
        for (String key : itemsSection.getKeys(false)) {
            String materialName = itemsSection.getString(key + ".material", "AIR");
            Material material = Material.matchMaterial(materialName);
            if (material == null) continue;

            int amount = itemsSection.getInt(key + ".amount", 1);
            int itemSlot = itemsSection.getInt(key + ".slot", slot++);
            
            ItemStack item = new ItemStack(material, amount);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            
            String displayName = itemsSection.getString(key + ".display-name");
            if (displayName != null && meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
                item.setItemMeta(meta);
            }

            if (itemSlot >= 0 && itemSlot < 36) {
                player.getInventory().setItem(itemSlot, item);
            }
        }
    }

    /**
     * Gives default staff mode items.
     */
    private void giveDefaultStaffItems(Player player) {
        // Default items: Compass (teleport), Book (reports), Chest (inspect), Redstone Block (ban)
        player.getInventory().setItem(0, createStaffItem(Material.COMPASS, ChatColor.GREEN + "Teleport Tool"));
        player.getInventory().setItem(1, createStaffItem(Material.BOOK, ChatColor.BLUE + "View Reports"));
        player.getInventory().setItem(2, createStaffItem(Material.CHEST, ChatColor.YELLOW + "Inspect Inventory"));
        player.getInventory().setItem(3, createStaffItem(Material.REDSTONE_BLOCK, ChatColor.RED + "Ban Player"));
        player.getInventory().setItem(4, createStaffItem(Material.IRON_BOOTS, ChatColor.GOLD + "Kick Player"));
        player.getInventory().setItem(8, createStaffItem(Material.BARRIER, ChatColor.RED + "Exit Staff Mode"));
    }

    /**
     * Creates a staff item with display name.
     */
    private ItemStack createStaffItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Applies staff mode features (vanish, flight, invincibility, etc.).
     */
    private void applyStaffModeFeatures(Player player) {
        var config = plugin.getConfigManager().getConfig();
        
        // Vanish
        if (config.getBoolean("staffmode.auto-vanish", true)) {
            if (!plugin.getStaffManager().isVanished(player)) {
                plugin.getStaffManager().toggleVanish(player);
            }
        }

        // Flight
        if (config.getBoolean("staffmode.auto-fly", true)) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        // Invincibility
        if (config.getBoolean("staffmode.invincible", true)) {
            player.setInvulnerable(true);
        }

        // Night vision (optional)
        if (config.getBoolean("staffmode.night-vision", true)) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.NIGHT_VISION,
                Integer.MAX_VALUE,
                0,
                false,
                false
            ));
        }

        // Set game mode (optional)
        String gameModeStr = config.getString("staffmode.game-mode", "SPECTATOR");
        try {
            GameMode gameMode = GameMode.valueOf(gameModeStr.toUpperCase());
            player.setGameMode(gameMode);
        } catch (IllegalArgumentException e) {
            // Invalid game mode, use default
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    /**
     * Removes staff mode features.
     */
    private void removeStaffModeFeatures(Player player) {
        var config = plugin.getConfigManager().getConfig();
        
        // Unvanish
        if (config.getBoolean("staffmode.auto-vanish", true)) {
            if (plugin.getStaffManager().isVanished(player)) {
                plugin.getStaffManager().toggleVanish(player);
            }
        }

        // Disable flight
        player.setFlying(false);
        // Note: allowFlight will be restored from saved state

        // Remove invincibility
        player.setInvulnerable(false);

        // Remove night vision
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);

        // Game mode will be restored from saved state
    }

    /**
     * Restores player state from saved data.
     */
    private void restorePlayerState(Player player, StaffModeData data) {
        // Restore inventory
        player.getInventory().setContents(data.inventory);
        player.getInventory().setArmorContents(data.armor);
        if (data.offHand != null) {
            player.getInventory().setItemInOffHand(data.offHand);
        }

        // Restore stats
        player.setFoodLevel(data.foodLevel);
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        player.setHealth(Math.min(data.health, maxHealth));
        player.setLevel(data.level);
        player.setExp(data.exp);

        // Restore flight
        player.setAllowFlight(data.allowFlight);
        player.setFlying(data.flying);

        // Restore game mode
        player.setGameMode(data.gameMode);

        // Restore location (optional, can be disabled in config)
        var config = plugin.getConfigManager().getConfig();
        if (config.getBoolean("staffmode.restore-location", false)) {
            player.teleport(data.location);
        }
    }

    /**
     * Executes commands when entering staff mode.
     */
    private void executeEnterCommands(Player player) {
        var config = plugin.getConfigManager().getConfig();
        var commands = config.getStringList("staffmode.enter-commands");
        
        for (String command : commands) {
            if (command.isEmpty()) continue;
            command = command.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    /**
     * Executes commands when exiting staff mode.
     */
    private void executeExitCommands(Player player) {
        var config = plugin.getConfigManager().getConfig();
        var commands = config.getStringList("staffmode.exit-commands");
        
        for (String command : commands) {
            if (command.isEmpty()) continue;
            command = command.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    /**
     * Handles player quit - exits staff mode if active.
     */
    public void handlePlayerQuit(Player player) {
        if (isInStaffMode(player)) {
            // Don't restore state on quit, just remove from map
            staffModePlayers.remove(player.getUniqueId());
        }
    }

    /**
     * Gets the saved location for a player in staff mode.
     */
    public Location getSavedLocation(Player player) {
        StaffModeData data = staffModePlayers.get(player.getUniqueId());
        return data != null ? data.location : null;
    }
}

