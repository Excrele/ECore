package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;

import java.util.*;

/**
 * Manages server performance optimization including entity cleanup, item stacking, and chunk optimization.
 * Integrates with ServerInfoManager for TPS monitoring and auto-cleanup.
 */
public class PerformanceManager {
    private final Ecore plugin;
    private int cleanupTaskId;

    public PerformanceManager(Ecore plugin) {
        this.plugin = plugin;
        scheduleAutoCleanup();
    }

    /**
     * Performs a comprehensive cleanup of entities and items.
     */
    public CleanupResult performCleanup() {
        CleanupResult result = new CleanupResult();
        
        for (World world : Bukkit.getWorlds()) {
            if (world == null) continue;
            
            for (Chunk chunk : world.getLoadedChunks()) {
                if (chunk == null) continue;
                
                Entity[] entities = chunk.getEntities();
                for (Entity entity : entities) {
                    if (entity == null) continue;
                    
                    // Clean up items
                    if (entity instanceof Item) {
                        Item item = (Item) entity;
                        if (shouldRemoveItem(item, chunk)) {
                            entity.remove();
                            result.itemsRemoved++;
                        }
                    }
                    // Clean up excessive mobs
                    else if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                        if (shouldRemoveMob((LivingEntity) entity, chunk)) {
                            entity.remove();
                            result.mobsRemoved++;
                        }
                    }
                    // Clean up projectiles
                    else if (entity instanceof Projectile) {
                        if (shouldRemoveProjectile((Projectile) entity)) {
                            entity.remove();
                            result.projectilesRemoved++;
                        }
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * Checks if an item should be removed based on configuration.
     */
    private boolean shouldRemoveItem(Item item, Chunk chunk) {
        if (!isItemCleanupEnabled()) return false;
        
        int maxItemsPerChunk = plugin.getConfigManager().getConfig()
                .getInt("performance.auto-cleanup.max-items-per-chunk", 100);
        
        int itemCount = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Item) {
                itemCount++;
            }
        }
        
        return itemCount > maxItemsPerChunk;
    }

    /**
     * Checks if a mob should be removed based on configuration.
     */
    private boolean shouldRemoveMob(LivingEntity mob, Chunk chunk) {
        if (!isMobCleanupEnabled()) return false;
        if (mob instanceof Player) return false;
        
        int maxEntitiesPerChunk = plugin.getConfigManager().getConfig()
                .getInt("performance.auto-cleanup.max-entities-per-chunk", 50);
        
        int entityCount = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                entityCount++;
            }
        }
        
        return entityCount > maxEntitiesPerChunk;
    }

    /**
     * Checks if a projectile should be removed.
     */
    private boolean shouldRemoveProjectile(Projectile projectile) {
        if (!isProjectileCleanupEnabled()) return false;
        
        // Remove projectiles older than 30 seconds
        long age = System.currentTimeMillis() - (projectile.getTicksLived() * 50L);
        return age > 30000L;
    }

    /**
     * Merges nearby items of the same type.
     */
    public int mergeItems(double radius) {
        if (!isItemStackingEnabled()) return 0;
        
        int merged = 0;
        
        for (World world : Bukkit.getWorlds()) {
            if (world == null) continue;
            
            List<Item> items = new ArrayList<>();
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    items.add((Item) entity);
                }
            }
            
            for (int i = 0; i < items.size(); i++) {
                Item item1 = items.get(i);
                if (item1 == null || !item1.isValid()) continue;
                
                org.bukkit.inventory.ItemStack stack1 = item1.getItemStack();
                if (stack1 == null) continue;
                
                for (int j = i + 1; j < items.size(); j++) {
                    Item item2 = items.get(j);
                    if (item2 == null || !item2.isValid()) continue;
                    
                    org.bukkit.inventory.ItemStack stack2 = item2.getItemStack();
                    if (stack2 == null) continue;
                    
                    // Check if items can be merged
                    if (canMergeItems(stack1, stack2) && 
                        item1.getLocation().distance(item2.getLocation()) <= radius) {
                        
                        int totalAmount = stack1.getAmount() + stack2.getAmount();
                        int maxStackSize = stack1.getMaxStackSize();
                        
                        if (totalAmount <= maxStackSize) {
                            stack1.setAmount(totalAmount);
                            item1.setItemStack(stack1);
                            item2.remove();
                            items.remove(j);
                            j--;
                            merged++;
                        } else {
                            stack1.setAmount(maxStackSize);
                            item1.setItemStack(stack1);
                            stack2.setAmount(totalAmount - maxStackSize);
                            item2.setItemStack(stack2);
                            merged++;
                        }
                    }
                }
            }
        }
        
        return merged;
    }

    /**
     * Checks if two item stacks can be merged.
     */
    private boolean canMergeItems(org.bukkit.inventory.ItemStack stack1, org.bukkit.inventory.ItemStack stack2) {
        if (stack1 == null || stack2 == null) return false;
        if (stack1.getType() != stack2.getType()) return false;
        if (stack1.hasItemMeta() != stack2.hasItemMeta()) return false;
        
        // Could add more checks for enchantments, etc.
        return true;
    }

    /**
     * Optimizes chunks by unloading unused chunks.
     */
    public int optimizeChunks() {
        if (!isChunkOptimizationEnabled()) return 0;
        
        int unloaded = 0;
        
        for (World world : Bukkit.getWorlds()) {
            if (world == null) continue;
            
            Chunk[] chunks = world.getLoadedChunks();
            for (Chunk chunk : chunks) {
                if (chunk == null) continue;
                
                // Unload chunks with no players and no entities
                boolean hasPlayers = false;
                boolean hasEntities = false;
                
                for (Entity entity : chunk.getEntities()) {
                    if (entity instanceof Player) {
                        hasPlayers = true;
                        break;
                    }
                    if (entity != null) {
                        hasEntities = true;
                    }
                }
                
                if (!hasPlayers && !hasEntities && chunk.isLoaded()) {
                    world.unloadChunk(chunk.getX(), chunk.getZ());
                    unloaded++;
                }
            }
        }
        
        return unloaded;
    }

    /**
     * Performs automatic cleanup when TPS drops below threshold.
     */
    public void performAutoCleanup() {
        if (!isAutoCleanupEnabled()) return;
        
        plugin.getLogger().info("TPS dropped below threshold, performing automatic cleanup...");
        CleanupResult result = performCleanup();
        plugin.getLogger().info("Auto-cleanup complete: " + result.getTotalRemoved() + " entities removed");
    }

    /**
     * Schedules automatic cleanup tasks.
     */
    private void scheduleAutoCleanup() {
        if (!isAutoCleanupEnabled()) return;
        
        int interval = plugin.getConfigManager().getConfig()
                .getInt("performance.auto-cleanup.interval", 300); // 5 minutes default (in seconds)
        
        cleanupTaskId = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // Check TPS and perform cleanup if needed
            double tps = plugin.getServerInfoManager().getTPS();
            double threshold = getAutoCleanupTPSThreshold();
            
            if (tps < threshold) {
                performAutoCleanup();
            } else {
                // Regular scheduled cleanup
                performCleanup();
            }
        }, 0L, 20L * interval).getTaskId();
    }

    /**
     * Gets detailed performance statistics.
     */
    public PerformanceStats getPerformanceStats() {
        PerformanceStats stats = new PerformanceStats();
        
        ServerInfoManager serverInfo = plugin.getServerInfoManager();
        stats.currentTPS = serverInfo.getTPS();
        stats.memoryInfo = serverInfo.getMemoryInfo();
        stats.totalChunks = serverInfo.getTotalChunks();
        stats.totalEntities = serverInfo.getTotalEntities();
        stats.onlinePlayers = serverInfo.getOnlinePlayers();
        stats.maxPlayers = serverInfo.getMaxPlayers();
        
        // Count entities by type
        for (World world : Bukkit.getWorlds()) {
            if (world == null) continue;
            
            for (Entity entity : world.getEntities()) {
                if (entity == null) continue;
                
                if (entity instanceof Item) {
                    stats.items++;
                } else if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                    stats.mobs++;
                } else if (entity instanceof Player) {
                    // Already counted in onlinePlayers
                } else {
                    stats.other++;
                }
            }
        }
        
        return stats;
    }

    /**
     * Configuration checks
     */
    private boolean isAutoCleanupEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("performance.auto-cleanup.enabled", true);
    }

    private boolean isItemCleanupEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("performance.auto-cleanup.clean-items", true);
    }

    private boolean isMobCleanupEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("performance.auto-cleanup.clean-mobs", true);
    }

    private boolean isProjectileCleanupEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("performance.auto-cleanup.clean-projectiles", true);
    }

    private boolean isItemStackingEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("performance.item-stacking.enabled", true);
    }

    private boolean isChunkOptimizationEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("performance.chunk-optimization.enabled", true);
    }

    private double getAutoCleanupTPSThreshold() {
        return plugin.getConfigManager().getConfig().getDouble("performance.auto-cleanup.tps-threshold", 15.0);
    }

    /**
     * Shuts down the performance manager.
     */
    public void shutdown() {
        if (cleanupTaskId != 0) {
            plugin.getServer().getScheduler().cancelTask(cleanupTaskId);
        }
    }

    /**
     * Result of a cleanup operation.
     */
    public static class CleanupResult {
        public int itemsRemoved = 0;
        public int mobsRemoved = 0;
        public int projectilesRemoved = 0;

        public int getTotalRemoved() {
            return itemsRemoved + mobsRemoved + projectilesRemoved;
        }
    }

    /**
     * Performance statistics.
     */
    public static class PerformanceStats {
        public double currentTPS = 20.0;
        public ServerInfoManager.MemoryInfo memoryInfo;
        public int totalEntities = 0;
        public int items = 0;
        public int mobs = 0;
        public int other = 0;
        public int totalChunks = 0;
        public int onlinePlayers = 0;
        public int maxPlayers = 0;
    }
}

