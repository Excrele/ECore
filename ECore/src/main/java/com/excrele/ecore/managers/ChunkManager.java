package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages chunk pregeneration for the server.
 * Allows administrators to pregenerate chunks in a radius from spawn.
 * 
 * @author Excrele
 * @version 1.0
 */
public class ChunkManager {
    private final Ecore plugin;
    private final List<UUID> activeGenerators; // Track who is currently generating chunks
    
    public ChunkManager(Ecore plugin) {
        this.plugin = plugin;
        this.activeGenerators = new ArrayList<>();
    }
    
    /**
     * Starts chunk generation in a radius from spawn.
     * 
     * @param world The world to generate chunks in
     * @param radius The radius in chunks from spawn
     * @param playerUUID The UUID of the player who initiated the generation (for progress updates)
     * @return true if generation started, false if already in progress
     */
    public boolean generateChunks(World world, int radius, UUID playerUUID) {
        if (activeGenerators.contains(playerUUID)) {
            return false; // Already generating
        }
        
        Location spawnLocation = getSpawnLocation(world);
        if (spawnLocation == null) {
            return false;
        }
        
        activeGenerators.add(playerUUID);
        
        // Calculate all chunks to generate
        int spawnChunkX = spawnLocation.getBlockX() >> 4; // Divide by 16
        int spawnChunkZ = spawnLocation.getBlockZ() >> 4;
        
        List<ChunkCoords> chunksToGenerate = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Check if chunk is within radius (circular, not square)
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= radius) {
                    chunksToGenerate.add(new ChunkCoords(spawnChunkX + x, spawnChunkZ + z));
                }
            }
        }
        
        final int totalChunks = chunksToGenerate.size();
        final int[] generated = {0};
        final int[] failed = {0};
        
        // Send initial message
        sendMessage(playerUUID, ChatColor.GREEN + "Starting chunk generation...");
        sendMessage(playerUUID, ChatColor.YELLOW + "Total chunks to generate: " + totalChunks);
        sendMessage(playerUUID, ChatColor.GRAY + "This may take a while. Progress updates will be sent periodically.");
        
        // Generate chunks on main thread in batches to avoid lag
        new BukkitRunnable() {
            private int index = 0;
            private long lastUpdate = System.currentTimeMillis();
            private final long UPDATE_INTERVAL = 5000; // Update every 5 seconds
            
            @Override
            public void run() {
                if (index >= chunksToGenerate.size()) {
                    // Finished
                    sendMessage(playerUUID, ChatColor.GREEN + "Chunk generation completed!");
                    sendMessage(playerUUID, ChatColor.YELLOW + "Generated: " + generated[0] + " chunks");
                    if (failed[0] > 0) {
                        sendMessage(playerUUID, ChatColor.RED + "Failed: " + failed[0] + " chunks");
                    }
                    activeGenerators.remove(playerUUID);
                    cancel();
                    return;
                }
                
                // Generate chunks in batches to avoid lag (chunk generation must be on main thread)
                int batchSize = 5; // Generate 5 chunks per tick to avoid server lag
                for (int i = 0; i < batchSize && index < chunksToGenerate.size(); i++) {
                    ChunkCoords coords = chunksToGenerate.get(index);
                    index++;
                    
                    try {
                        Chunk chunk = world.getChunkAt(coords.x, coords.z);
                        if (!chunk.isLoaded()) {
                            chunk.load(true); // Force load and generate
                        }
                        generated[0]++;
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to generate chunk at " + coords.x + ", " + coords.z + ": " + e.getMessage());
                        failed[0]++;
                    }
                }
                
                // Send progress update periodically
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastUpdate >= UPDATE_INTERVAL) {
                    double progress = (double) index / totalChunks * 100;
                    sendMessage(playerUUID, ChatColor.YELLOW + String.format("Progress: %.1f%% (%d/%d chunks)", 
                        progress, index, totalChunks));
                    lastUpdate = currentTime;
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Run every tick on main thread
        
        return true;
    }
    
    /**
     * Gets the spawn location for a world.
     * First tries custom spawn from SpawnManager, then falls back to world spawn.
     * 
     * @param world The world
     * @return The spawn location, or null if not found
     */
    private Location getSpawnLocation(World world) {
        // Try custom spawn first
        Location spawn = plugin.getSpawnManager().getSpawn(world);
        if (spawn != null) {
            return spawn;
        }
        
        // Fall back to world spawn
        return world.getSpawnLocation();
    }
    
    /**
     * Checks if a player is currently generating chunks.
     * 
     * @param playerUUID The player's UUID
     * @return true if generating, false otherwise
     */
    public boolean isGenerating(UUID playerUUID) {
        return activeGenerators.contains(playerUUID);
    }
    
    /**
     * Cancels chunk generation for a player.
     * 
     * @param playerUUID The player's UUID
     * @return true if cancelled, false if not generating
     */
    public boolean cancelGeneration(UUID playerUUID) {
        if (activeGenerators.remove(playerUUID)) {
            sendMessage(playerUUID, ChatColor.YELLOW + "Chunk generation cancelled.");
            return true;
        }
        return false;
    }
    
    /**
     * Sends a message to a player if they are online.
     * 
     * @param playerUUID The player's UUID
     * @param message The message to send
     */
    private void sendMessage(UUID playerUUID, String message) {
        org.bukkit.entity.Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            player.sendMessage(message);
        }
    }
    
    /**
     * Helper class to store chunk coordinates.
     */
    private static class ChunkCoords {
        final int x;
        final int z;
        
        ChunkCoords(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }
}

