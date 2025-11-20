package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.util.*;

/**
 * Manages custom mob drops, health, damage, and spawn rates.
 */
public class MobCustomizationManager implements Listener {
    private final Ecore plugin;
    private File mobsFile;
    private FileConfiguration mobsConfig;
    private final Map<String, MobConfig> mobConfigs;
    private final Random random = new Random();

    public MobCustomizationManager(Ecore plugin) {
        this.plugin = plugin;
        this.mobConfigs = new HashMap<>();
        initializeMobsConfig();
        loadMobConfigs();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void initializeMobsConfig() {
        mobsFile = new File(plugin.getDataFolder(), "mob-customization.yml");
        if (!mobsFile.exists()) {
            plugin.saveResource("mob-customization.yml", false);
        }
        mobsConfig = YamlConfiguration.loadConfiguration(mobsFile);
    }

    private void loadMobConfigs() {
        mobConfigs.clear();
        
        if (!mobsConfig.contains("mobs")) {
            return;
        }

        for (String mobType : mobsConfig.getConfigurationSection("mobs").getKeys(false)) {
            String path = "mobs." + mobType;
            
            MobConfig config = new MobConfig();
            config.mobType = mobType;
            
            if (mobsConfig.contains(path + ".health")) {
                config.customHealth = mobsConfig.getDouble(path + ".health");
            }
            
            if (mobsConfig.contains(path + ".damage")) {
                config.customDamage = mobsConfig.getDouble(path + ".damage");
            }
            
            if (mobsConfig.contains(path + ".spawn-rate")) {
                config.spawnRate = mobsConfig.getDouble(path + ".spawn-rate", 1.0);
            }
            
            if (mobsConfig.contains(path + ".drops")) {
                List<Map<?, ?>> dropsList = mobsConfig.getMapList(path + ".drops");
                config.customDrops = new ArrayList<>();
                
                for (Map<?, ?> dropMap : dropsList) {
                    try {
                        String type = (String) dropMap.get("type");
                        Object amountObj = dropMap.get("amount");
                        int amount = amountObj instanceof Number ? ((Number) amountObj).intValue() : 1;
                        Object chanceObj = dropMap.get("chance");
                        double chance = chanceObj instanceof Number ? ((Number) chanceObj).doubleValue() : 1.0;
                        
                        if (type != null) {
                            config.customDrops.add(new CustomDrop(type, amount, chance));
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Invalid drop configuration for " + mobType + ": " + e.getMessage());
                    }
                }
            }
            
            mobConfigs.put(mobType.toUpperCase(), config);
        }
        
        plugin.getLogger().info("Loaded " + mobConfigs.size() + " mob customizations");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL &&
            event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        String mobType = entity.getType().name();
        MobConfig config = mobConfigs.get(mobType);
        
        if (config == null) {
            return;
        }

        // Apply spawn rate
        if (config.spawnRate < 1.0 && random.nextDouble() > config.spawnRate) {
            event.setCancelled(true);
            return;
        }

        // Mark entity as customized
        entity.setMetadata("ecore_customized", new FixedMetadataValue(plugin, true));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        String mobType = entity.getType().name();
        MobConfig config = mobConfigs.get(mobType);
        
        if (config == null) {
            return;
        }

        // Apply custom damage
        if (config.customDamage != null && event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
            if (damageEvent.getDamager() == entity) {
                // This mob is dealing damage
                event.setDamage(config.customDamage);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntitySpawnSetHealth(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        String mobType = entity.getType().name();
        MobConfig config = mobConfigs.get(mobType);
        
        if (config == null || config.customHealth == null) {
            return;
        }

        LivingEntity living = (LivingEntity) entity;
        
        // Set custom health after a short delay to ensure entity is fully spawned
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (living.isValid() && !living.isDead()) {
                // Use Attribute API for newer versions, fallback for older
                try {
                    // Try to use Attribute API (1.9+)
                    org.bukkit.attribute.AttributeInstance maxHealthAttr = living.getAttribute(
                            org.bukkit.attribute.Attribute.valueOf("GENERIC_MAX_HEALTH"));
                    if (maxHealthAttr != null) {
                        maxHealthAttr.setBaseValue(config.customHealth);
                        living.setHealth(config.customHealth);
                    } else {
                        // Fallback: just set health to the custom value (may be limited by max health)
                        double currentMax = living.getHealth();
                        living.setHealth(Math.min(config.customHealth, currentMax));
                    }
                } catch (Exception e) {
                    // Fallback: just set health
                    try {
                        double currentMax = living.getHealth();
                        living.setHealth(Math.min(config.customHealth, currentMax));
                    } catch (Exception e2) {
                        // Ignore if it fails
                    }
                }
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        String mobType = entity.getType().name();
        MobConfig config = mobConfigs.get(mobType);
        
        if (config == null || config.customDrops == null || config.customDrops.isEmpty()) {
            return;
        }

        // Clear default drops if we have custom drops
        if (mobsConfig.getBoolean("mobs." + mobType + ".replace-drops", false)) {
            event.getDrops().clear();
        }

        // Add custom drops
        for (CustomDrop drop : config.customDrops) {
            if (random.nextDouble() <= drop.chance) {
                try {
                    org.bukkit.Material material = org.bukkit.Material.valueOf(drop.type.toUpperCase());
                    ItemStack item = new ItemStack(material, drop.amount);
                    event.getDrops().add(item);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material type in custom drop: " + drop.type);
                }
            }
        }
    }

    /**
     * Reloads mob configurations from file.
     */
    public void reload() {
        mobsConfig = YamlConfiguration.loadConfiguration(mobsFile);
        loadMobConfigs();
    }

    /**
     * Gets mob configuration for a specific mob type.
     */
    public MobConfig getMobConfig(String mobType) {
        return mobConfigs.get(mobType.toUpperCase());
    }

    /**
     * Mob configuration class.
     */
    public static class MobConfig {
        public String mobType;
        public Double customHealth;
        public Double customDamage;
        public Double spawnRate = 1.0;
        public List<CustomDrop> customDrops;
    }

    /**
     * Custom drop configuration class.
     */
    public static class CustomDrop {
        public final String type;
        public final int amount;
        public final double chance;

        public CustomDrop(String type, int amount, double chance) {
            this.type = type;
            this.amount = amount;
            this.chance = chance;
        }
    }
}

