package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

/**
 * Manages custom enchantments.
 */
public class EnchantmentManager implements Listener {
    private final Ecore plugin;
    private File enchantmentsFile;
    private FileConfiguration enchantmentsConfig;
    private final Map<String, CustomEnchantment> enchantments;

    public EnchantmentManager(Ecore plugin) {
        this.plugin = plugin;
        this.enchantments = new HashMap<>();
        initializeEnchantmentsConfig();
        loadEnchantments();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void initializeEnchantmentsConfig() {
        enchantmentsFile = new File(plugin.getDataFolder(), "enchantments.yml");
        if (!enchantmentsFile.exists()) {
            plugin.saveResource("enchantments.yml", false);
        }
        enchantmentsConfig = YamlConfiguration.loadConfiguration(enchantmentsFile);
    }

    private void loadEnchantments() {
        if (enchantmentsConfig.getConfigurationSection("enchantments") == null) return;

        for (String enchantId : enchantmentsConfig.getConfigurationSection("enchantments").getKeys(false)) {
            String path = "enchantments." + enchantId;
            CustomEnchantment enchant = new CustomEnchantment(
                enchantId,
                enchantmentsConfig.getString(path + ".name", enchantId),
                enchantmentsConfig.getString(path + ".description", ""),
                enchantmentsConfig.getInt(path + ".max-level", 5),
                enchantmentsConfig.getStringList(path + ".applicable-items")
            );
            enchantments.put(enchantId, enchant);
        }
    }

    /**
     * Applies a custom enchantment to an item.
     */
    public boolean applyEnchantment(ItemStack item, String enchantId, int level) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        CustomEnchantment enchant = enchantments.get(enchantId);
        if (enchant == null) {
            return false;
        }

        if (!enchant.canApplyTo(item.getType())) {
            return false;
        }

        if (level < 1 || level > enchant.getMaxLevel()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        // Remove existing enchantment lore
        lore.removeIf(line -> line.contains(enchant.getName()));

        // Add new enchantment lore
        String enchantLore = "§7" + enchant.getName() + " " + toRomanNumeral(level);
        lore.add(enchantLore);
        meta.setLore(lore);
        item.setItemMeta(meta);

        return true;
    }

    /**
     * Removes a custom enchantment from an item.
     */
    public boolean removeEnchantment(ItemStack item, String enchantId) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        CustomEnchantment enchant = enchantments.get(enchantId);
        if (enchant == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        List<String> lore = meta.getLore();
        if (lore == null) {
            return false;
        }

        boolean removed = lore.removeIf(line -> line.contains(enchant.getName()));
        if (removed) {
            meta.setLore(lore);
            item.setItemMeta(meta);
            return true;
        }

        return false;
    }

    /**
     * Gets the level of a custom enchantment on an item.
     */
    public int getEnchantmentLevel(ItemStack item, String enchantId) {
        if (item == null || item.getType() == Material.AIR) {
            return 0;
        }

        CustomEnchantment enchant = enchantments.get(enchantId);
        if (enchant == null) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return 0;
        }

        List<String> lore = meta.getLore();
        if (lore == null) {
            return 0;
        }

        for (String line : lore) {
            if (line.contains(enchant.getName())) {
                // Extract level from "Enchantment Name I" format
                String[] parts = line.split(" ");
                if (parts.length > 0) {
                    String levelStr = parts[parts.length - 1];
                    return fromRomanNumeral(levelStr);
                }
            }
        }

        return 0;
    }

    /**
     * Gets all custom enchantments.
     */
    public Map<String, CustomEnchantment> getEnchantments() {
        return new HashMap<>(enchantments);
    }

    /**
     * Gets a custom enchantment by ID.
     */
    public CustomEnchantment getEnchantment(String enchantId) {
        return enchantments.get(enchantId);
    }

    /**
     * Checks if an enchantment exists.
     */
    public boolean hasEnchantment(String enchantId) {
        return enchantments.containsKey(enchantId);
    }

    /**
     * Reloads enchantments from config.
     */
    public void reload() {
        enchantments.clear();
        enchantmentsConfig = YamlConfiguration.loadConfiguration(enchantmentsFile);
        loadEnchantments();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (weapon == null || weapon.getType() == Material.AIR) return;

        // Process weapon enchantments
        for (Map.Entry<String, CustomEnchantment> entry : enchantments.entrySet()) {
            int level = getEnchantmentLevel(weapon, entry.getKey());
            if (level > 0) {
                entry.getValue().onAttack(player, event.getEntity(), level, event);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // Process armor enchantments
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null || armor.getType() == Material.AIR) continue;

            for (Map.Entry<String, CustomEnchantment> entry : enchantments.entrySet()) {
                int level = getEnchantmentLevel(armor, entry.getKey());
                if (level > 0) {
                    entry.getValue().onDefend(player, level, event);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();

        if (rod == null || rod.getType() != Material.FISHING_ROD) return;

        for (Map.Entry<String, CustomEnchantment> entry : enchantments.entrySet()) {
            int level = getEnchantmentLevel(rod, entry.getKey());
            if (level > 0) {
                entry.getValue().onFish(player, level, event);
            }
        }
    }

    private String toRomanNumeral(int number) {
        String[] romanNumerals = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        if (number >= 1 && number <= 10) {
            return romanNumerals[number];
        }
        return String.valueOf(number);
    }

    private int fromRomanNumeral(String roman) {
        Map<String, Integer> romanMap = new HashMap<>();
        romanMap.put("I", 1);
        romanMap.put("II", 2);
        romanMap.put("III", 3);
        romanMap.put("IV", 4);
        romanMap.put("V", 5);
        romanMap.put("VI", 6);
        romanMap.put("VII", 7);
        romanMap.put("VIII", 8);
        romanMap.put("IX", 9);
        romanMap.put("X", 10);
        return romanMap.getOrDefault(roman, 0);
    }

    /**
     * Custom enchantment class.
     */
    public static class CustomEnchantment {
        private final String id;
        private final String name;
        private final String description;
        private final int maxLevel;
        private final List<Material> applicableItems;

        public CustomEnchantment(String id, String name, String description, int maxLevel, List<String> applicableItemsStr) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.maxLevel = maxLevel;
            this.applicableItems = new ArrayList<>();
            
            for (String itemStr : applicableItemsStr) {
                try {
                    this.applicableItems.add(Material.valueOf(itemStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // Invalid material, skip
                }
            }
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getMaxLevel() { return maxLevel; }

        public boolean canApplyTo(Material material) {
            return applicableItems.contains(material);
        }

        public void onAttack(Player attacker, org.bukkit.entity.Entity target, int level, EntityDamageByEntityEvent event) {
            // Override in subclasses or handle via config
        }

        public void onDefend(Player defender, int level, EntityDamageEvent event) {
            // Override in subclasses or handle via config
        }

        public void onFish(Player player, int level, PlayerFishEvent event) {
            // Override in subclasses or handle via config
        }
    }
}

