package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.database.BlockLogDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages block logging operations including block actions, container access, and rollbacks.
 */
public class BlockLogManager {
    private final Ecore plugin;
    private final BlockLogDatabase database;
    private final Map<UUID, Location> inspectorSelections; // Player UUID -> Selected location
    private final Map<UUID, Long> lastSnapshotTime; // Player UUID -> Last snapshot time

    public BlockLogManager(Ecore plugin) {
        this.plugin = plugin;
        this.database = new BlockLogDatabase(plugin);
        this.inspectorSelections = new ConcurrentHashMap<>();
        this.lastSnapshotTime = new ConcurrentHashMap<>();
        
        // Schedule periodic inventory snapshots
        scheduleInventorySnapshots();
        
        // Schedule periodic log purging
        scheduleLogPurging();
    }

    /**
     * Logs a block break action.
     */
    public void logBlockBreak(Player player, Block block) {
        if (!isLoggingEnabled()) return;
        if (!plugin.getConfigManager().getConfig().getBoolean("block-logging.log-block-break", true)) return;
        
        Material material = block.getType();
        String data = serializeBlockData(block);
        database.logBlockAction(player.getUniqueId(), player.getName(), "BREAK", 
                block.getLocation(), material, data);
    }

    /**
     * Logs a block place action.
     */
    public void logBlockPlace(Player player, Block block) {
        if (!isLoggingEnabled()) return;
        if (!plugin.getConfigManager().getConfig().getBoolean("block-logging.log-block-place", true)) return;
        
        Material material = block.getType();
        String data = serializeBlockData(block);
        database.logBlockAction(player.getUniqueId(), player.getName(), "PLACE", 
                block.getLocation(), material, data);
    }

    /**
     * Logs a container interaction (chest, furnace, etc.).
     */
    public void logContainerInteraction(Player player, Location location, String action, int slot, ItemStack item) {
        if (!isLoggingEnabled()) return;
        if (!plugin.getConfigManager().getConfig().getBoolean("block-logging.log-container-access", true)) return;
        
        database.logContainerAction(player.getUniqueId(), player.getName(), action, location, slot, item);
    }

    /**
     * Gets block logs for a specific location.
     */
    public List<BlockLogDatabase.BlockLogEntry> getBlockLogs(Location location, long timeRange) {
        return database.getBlockLogs(location, timeRange);
    }

    /**
     * Gets block logs for a specific player.
     */
    public List<BlockLogDatabase.BlockLogEntry> getPlayerBlockLogs(UUID playerUuid, long timeRange, int limit) {
        return database.getPlayerBlockLogs(playerUuid, timeRange, limit);
    }

    /**
     * Rolls back block changes for a specific player within a time range.
     */
    public void rollbackPlayer(UUID playerUuid, long timeRange, Player executor) {
        List<BlockLogDatabase.BlockLogEntry> logs = database.getPlayerBlockLogs(playerUuid, timeRange, Integer.MAX_VALUE);
        
        if (logs.isEmpty()) {
            executor.sendMessage("§cNo block logs found for that player in the specified time range.");
            return;
        }

        executor.sendMessage("§aRolling back " + logs.size() + " block changes...");
        
        // Process rollback asynchronously
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            int rolledBack = 0;
            for (BlockLogDatabase.BlockLogEntry log : logs) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Location loc = new Location(Bukkit.getWorld(log.world), log.x, log.y, log.z);
                    if (loc.getWorld() == null) return;
                    
                    Block block = loc.getBlock();
                    
                    if (log.action.equals("BREAK")) {
                        // Restore the block that was broken
                        if (log.material != null) {
                            block.setType(Material.valueOf(log.material));
                            // Could restore block data here if needed
                        }
                    } else if (log.action.equals("PLACE")) {
                        // Remove the block that was placed
                        block.setType(Material.AIR);
                    }
                });
                rolledBack++;
                
                // Small delay to prevent server lag
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            final int finalCount = rolledBack;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                executor.sendMessage("§aRollback complete! " + finalCount + " blocks restored.");
            });
        });
    }

    /**
     * Rolls back blocks in a specific area to a previous time.
     */
    public void rollbackArea(Location pos1, Location pos2, long time, Player executor) {
        if (pos1.getWorld() != pos2.getWorld()) {
            executor.sendMessage("§cBoth locations must be in the same world!");
            return;
        }

        executor.sendMessage("§aRolling back area... This may take a while.");
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
            int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
            int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
            int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
            int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
            int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
            
            int rolledBack = 0;
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Location loc = new Location(pos1.getWorld(), x, y, z);
                        List<BlockLogDatabase.BlockLogEntry> logs = database.getBlockLogs(loc, 
                                System.currentTimeMillis() - time);
                        
                        if (!logs.isEmpty()) {
                            BlockLogDatabase.BlockLogEntry latestLog = logs.get(0);
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                Block block = loc.getBlock();
                                if (latestLog.action.equals("BREAK") && latestLog.material != null) {
                                    block.setType(Material.valueOf(latestLog.material));
                                } else if (latestLog.action.equals("PLACE")) {
                                    block.setType(Material.AIR);
                                }
                            });
                            rolledBack++;
                        }
                        
                        // Small delay
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            }
            
            final int finalCount = rolledBack;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                executor.sendMessage("§aArea rollback complete! " + finalCount + " blocks restored.");
            });
        });
    }

    /**
     * Sets the inspector selection for a player.
     */
    public void setInspectorSelection(Player player, Location location) {
        inspectorSelections.put(player.getUniqueId(), location);
    }

    /**
     * Gets the inspector selection for a player.
     */
    public Location getInspectorSelection(Player player) {
        return inspectorSelections.get(player.getUniqueId());
    }

    /**
     * Removes the inspector selection for a player.
     */
    public void clearInspectorSelection(Player player) {
        inspectorSelections.remove(player.getUniqueId());
    }

    /**
     * Checks if block logging is enabled.
     */
    public boolean isLoggingEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("block-logging.enabled", true);
    }

    /**
     * Gets the database instance.
     */
    public BlockLogDatabase getDatabase() {
        return database;
    }

    /**
     * Schedules periodic inventory snapshots.
     */
    private void scheduleInventorySnapshots() {
        int interval = plugin.getConfigManager().getConfig().getInt("block-logging.inventory-snapshot-interval", 300); // 5 minutes default
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                long lastSnapshot = lastSnapshotTime.getOrDefault(uuid, 0L);
                long now = System.currentTimeMillis();
                
                if (now - lastSnapshot >= (interval * 1000L)) {
                    if (plugin.getInventoryLogManager() != null) {
                        plugin.getInventoryLogManager().takeSnapshot(player);
                        lastSnapshotTime.put(uuid, now);
                    }
                }
            }
        }, 0L, 20L * 60L); // Check every minute
    }

    /**
     * Schedules periodic log purging.
     */
    private void scheduleLogPurging() {
        int retentionDays = plugin.getConfigManager().getConfig().getInt("block-logging.retention-days", 30);
        long purgeInterval = 24L * 60L * 60L * 20L; // 24 hours in ticks
        
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            database.purgeOldLogs(retentionDays);
        }, purgeInterval, purgeInterval);
    }

    /**
     * Serializes block data to a string.
     */
    private String serializeBlockData(Block block) {
        // Simple serialization - can be enhanced
        return block.getType().name();
    }

    /**
     * Shuts down the manager and closes database connections.
     */
    public void shutdown() {
        database.close();
    }
}

