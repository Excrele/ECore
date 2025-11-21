package com.excrele.ecore.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.excrele.ecore.Ecore;

public class StatisticsManager implements Listener {
    private final Ecore plugin;
    private File statsFile;
    private FileConfiguration statsConfig;
    private final Map<UUID, Location> lastLocations; // For distance tracking
    private final Map<UUID, Double> totalDistance; // Cached distance per player

    public StatisticsManager(Ecore plugin) {
        this.plugin = plugin;
        this.lastLocations = new HashMap<>();
        this.totalDistance = new HashMap<>();
        initializeStatsConfig();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void initializeStatsConfig() {
        statsFile = new File(plugin.getDataFolder(), "statistics.yml");
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create statistics.yml", e);
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String path = "players." + uuid.toString();
        if (!statsConfig.contains(path + ".joins")) {
            statsConfig.set(path + ".joins", 0);
        }
        statsConfig.set(path + ".joins", statsConfig.getInt(path + ".joins") + 1);
        
        // Initialize last location for distance tracking
        lastLocations.put(uuid, player.getLocation());
        
        // Initialize distance if not set
        if (!statsConfig.contains(path + ".distance-traveled")) {
            statsConfig.set(path + ".distance-traveled", 0.0);
        }
        
        saveStats();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Send Discord leave notification
        plugin.getDiscordManager().sendPlayerLeaveNotification(event.getPlayer());
        // Save distance on quit
        UUID uuid = event.getPlayer().getUniqueId();
        lastLocations.remove(uuid);
        totalDistance.remove(uuid);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String path = "players." + uuid.toString() + ".blocks-broken";
        statsConfig.set(path, statsConfig.getInt(path, 0) + 1);
        saveStats();
        
        // Check achievements
        if (plugin.getAchievementManager() != null) {
            plugin.getAchievementManager().checkAchievements(player);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String path = "players." + uuid.toString() + ".blocks-placed";
        statsConfig.set(path, statsConfig.getInt(path, 0) + 1);
        saveStats();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Location currentLoc = event.getTo();
        Location lastLoc = lastLocations.get(uuid);
        
        if (lastLoc != null && currentLoc != null && currentLoc.getWorld() == lastLoc.getWorld()) {
            // Only track if player actually moved (not just looked around)
            if (currentLoc.getBlockX() != lastLoc.getBlockX() || 
                currentLoc.getBlockY() != lastLoc.getBlockY() || 
                currentLoc.getBlockZ() != lastLoc.getBlockZ()) {
                
                double distance = lastLoc.distance(currentLoc);
                String path = "players." + uuid.toString() + ".distance-traveled";
                double totalDist = statsConfig.getDouble(path, 0.0) + distance;
                statsConfig.set(path, totalDist);
                
                // Update cached distance
                totalDistance.put(uuid, totalDist);
                
                // Save periodically (every 100 blocks to reduce file writes)
                if (totalDist % 100 < distance) {
                    saveStats();
                }
            }
        }
        
        lastLocations.put(uuid, currentLoc);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            UUID uuid = player.getUniqueId();
            String path = "players." + uuid.toString() + ".items-crafted";
            int amount = event.getRecipe().getResult().getAmount();
            statsConfig.set(path, statsConfig.getInt(path, 0) + amount);
            saveStats();
        }
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int expGained = event.getAmount();
        if (expGained > 0) {
            String path = "players." + uuid.toString() + ".experience-gained";
            statsConfig.set(path, statsConfig.getInt(path, 0) + expGained);
            saveStats();
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID uuid = player.getUniqueId();
            double damage = event.getFinalDamage();
            
            String path = "players." + uuid.toString() + ".damage-taken";
            statsConfig.set(path, statsConfig.getDouble(path, 0.0) + damage);
            saveStats();
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            UUID uuid = attacker.getUniqueId();
            double damage = event.getFinalDamage();
            
            String path = "players." + uuid.toString() + ".damage-dealt";
            statsConfig.set(path, statsConfig.getDouble(path, 0.0) + damage);
            saveStats();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();
        String path = "players." + uuid.toString() + ".deaths";
        statsConfig.set(path, statsConfig.getInt(path, 0) + 1);
        
        // Save death location for /back command
        plugin.getTeleportManager().saveDeathLocation(player);
        
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            UUID killerUuid = killer.getUniqueId();
            String killPath = "players." + killerUuid.toString() + ".kills";
            statsConfig.set(killPath, statsConfig.getInt(killPath, 0) + 1);
            
            // Check achievements for killer
            if (plugin.getAchievementManager() != null) {
                plugin.getAchievementManager().checkAchievements(killer);
            }
        }
        
        saveStats();
    }

    public int getStatistic(Player player, String stat) {
        String path = "players." + player.getUniqueId().toString() + "." + stat;
        return statsConfig.getInt(path, 0);
    }

    public double getStatisticDouble(Player player, String stat) {
        String path = "players." + player.getUniqueId().toString() + "." + stat;
        return statsConfig.getDouble(path, 0.0);
    }

    public void setStatistic(Player player, String stat, int value) {
        String path = "players." + player.getUniqueId().toString() + "." + stat;
        statsConfig.set(path, value);
        saveStats();
    }

    public void setStatistic(Player player, String stat, double value) {
        String path = "players." + player.getUniqueId().toString() + "." + stat;
        statsConfig.set(path, value);
        saveStats();
    }

    // Track money earned (called from EconomyManager)
    public void trackMoneyEarned(UUID uuid, double amount) {
        String path = "players." + uuid.toString() + ".money-earned";
        statsConfig.set(path, statsConfig.getDouble(path, 0.0) + amount);
        saveStats();
    }

    // Track money spent (called from EconomyManager)
    public void trackMoneySpent(UUID uuid, double amount) {
        String path = "players." + uuid.toString() + ".money-spent";
        statsConfig.set(path, statsConfig.getDouble(path, 0.0) + amount);
        saveStats();
    }

    private void saveStats() {
        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save statistics.yml: " + e.getMessage());
        }
    }
}

