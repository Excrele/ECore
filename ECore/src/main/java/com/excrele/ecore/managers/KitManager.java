package com.excrele.ecore.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.excrele.ecore.Ecore;

public class KitManager {
    private final Ecore plugin;
    private File kitFile;
    private FileConfiguration kitConfig;
    private final Map<UUID, Map<String, Long>> kitCooldowns;

    static {
        ConfigurationSerialization.registerClass(ItemStack.class);
    }

    public KitManager(Ecore plugin) {
        this.plugin = plugin;
        this.kitCooldowns = new HashMap<>();
        initializeKitConfig();
    }

    private void initializeKitConfig() {
        kitFile = new File(plugin.getDataFolder(), "kits.yml");
        if (!kitFile.exists()) {
            try {
                kitFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create kits.yml", e);
            }
        }
        kitConfig = YamlConfiguration.loadConfiguration(kitFile);
    }

    public boolean createKit(String name, Player creator) {
        if (kitConfig.contains("kits." + name.toLowerCase())) {
            return false;
        }

        String path = "kits." + name.toLowerCase();
        kitConfig.set(path + ".items", creator.getInventory().getContents());
        kitConfig.set(path + ".cooldown", 0);
        kitConfig.set(path + ".cost", 0.0);
        kitConfig.set(path + ".one-time", false);
        kitConfig.set(path + ".permission", "ecore.kit." + name.toLowerCase());

        saveKits();
        return true;
    }

    public boolean deleteKit(String name) {
        if (!kitConfig.contains("kits." + name.toLowerCase())) {
            return false;
        }
        kitConfig.set("kits." + name.toLowerCase(), null);
        saveKits();
        return true;
    }

    public boolean giveKit(Player player, String name) {
        String path = "kits." + name.toLowerCase();
        if (!kitConfig.contains(path)) {
            return false;
        }

        // Check cooldown
        if (hasCooldown(player, name)) {
            long remaining = getCooldownRemaining(player, name);
            player.sendMessage("§cYou must wait " + (remaining / 1000) + " more seconds before using this kit again!");
            return false;
        }

        // Check cost
        double cost = kitConfig.getDouble(path + ".cost", 0.0);
        if (cost > 0) {
            if (!plugin.getEconomyManager().removeBalance(player.getUniqueId(), cost)) {
                player.sendMessage("§cYou don't have enough money! Cost: " + cost);
                return false;
            }
            player.sendMessage("§aPaid " + cost + " for kit!");
        }

        // Check one-time
        if (kitConfig.getBoolean(path + ".one-time", false)) {
            String usedPath = "players." + player.getUniqueId() + ".used-kits";
            List<String> usedKits = kitConfig.getStringList(usedPath);
            if (usedKits.contains(name.toLowerCase())) {
                player.sendMessage("§cYou have already used this one-time kit!");
                return false;
            }
            usedKits.add(name.toLowerCase());
            kitConfig.set(usedPath, usedKits);
            saveKits();
        }

        // Give items
        @SuppressWarnings("unchecked")
        List<ItemStack> items = (List<ItemStack>) kitConfig.getList(path + ".items");
        if (items != null) {
            for (ItemStack item : items) {
                if (item != null) {
                    HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
                    if (!overflow.isEmpty()) {
                        for (ItemStack over : overflow.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), over);
                        }
                    }
                }
            }
        }

        // Set cooldown
        int cooldown = kitConfig.getInt(path + ".cooldown", 0);
        if (cooldown > 0) {
            setCooldown(player, name, cooldown);
        }

        player.sendMessage("§aYou received the kit: " + name);
        return true;
    }

    public List<String> getKits() {
        if (!kitConfig.contains("kits")) {
            return new ArrayList<>();
        }
        Set<String> kitKeys = kitConfig.getConfigurationSection("kits").getKeys(false);
        return new ArrayList<>(kitKeys);
    }

    public boolean kitExists(String name) {
        return kitConfig.contains("kits." + name.toLowerCase());
    }

    public void setCooldown(String name, int seconds) {
        kitConfig.set("kits." + name.toLowerCase() + ".cooldown", seconds);
        saveKits();
    }

    public void setCost(String name, double cost) {
        kitConfig.set("kits." + name.toLowerCase() + ".cost", cost);
        saveKits();
    }

    public void setOneTime(String name, boolean oneTime) {
        kitConfig.set("kits." + name.toLowerCase() + ".one-time", oneTime);
        saveKits();
    }

    private boolean hasCooldown(Player player, String kitName) {
        Map<String, Long> playerCooldowns = kitCooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return false;
        }
        Long cooldownEnd = playerCooldowns.get(kitName.toLowerCase());
        if (cooldownEnd == null) {
            return false;
        }
        return System.currentTimeMillis() < cooldownEnd;
    }

    private long getCooldownRemaining(Player player, String kitName) {
        Map<String, Long> playerCooldowns = kitCooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return 0;
        }
        Long cooldownEnd = playerCooldowns.get(kitName.toLowerCase());
        if (cooldownEnd == null) {
            return 0;
        }
        return Math.max(0, cooldownEnd - System.currentTimeMillis());
    }

    private void setCooldown(Player player, String kitName, int seconds) {
        kitCooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(kitName.toLowerCase(), System.currentTimeMillis() + (seconds * 1000L));
    }

    public double getKitCost(String name) {
        return kitConfig.getDouble("kits." + name.toLowerCase() + ".cost", 0.0);
    }

    public int getKitCooldown(String name) {
        return kitConfig.getInt("kits." + name.toLowerCase() + ".cooldown", 0);
    }

    private void saveKits() {
        try {
            kitConfig.save(kitFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save kits.yml: " + e.getMessage());
        }
    }
}

