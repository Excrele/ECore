package com.excrele.ecore.managers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.Structure;
import org.bukkit.scheduler.BukkitTask;

import com.excrele.ecore.Ecore;

public class TeleportManager {
    private final Ecore plugin;
    private final Map<UUID, Location> lastLocations;
    private final Map<UUID, Location> deathLocations;
    private final Map<UUID, Deque<Location>> teleportHistory; // Teleport history (multiple back locations)
    private final Map<UUID, TeleportRequest> teleportRequests;
    private final Map<UUID, BukkitTask> teleportTasks;
    private final Set<UUID> teleportingPlayers;
    private final Random random;
    private static final int MAX_HISTORY_SIZE = 10; // Maximum number of locations in history

    public TeleportManager(Ecore plugin) {
        this.plugin = plugin;
        this.lastLocations = new HashMap<>();
        this.deathLocations = new HashMap<>();
        this.teleportHistory = new HashMap<>();
        this.teleportRequests = new HashMap<>();
        this.teleportTasks = new HashMap<>();
        this.teleportingPlayers = new HashSet<>();
        this.random = new Random();
    }

    public void saveLastLocation(Player player) {
        UUID uuid = player.getUniqueId();
        Location currentLoc = player.getLocation();
        
        // Save to simple last location (for backward compatibility)
        lastLocations.put(uuid, currentLoc);
        
        // Add to teleport history
        Deque<Location> history = teleportHistory.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        
        // Don't add if it's the same location (within 5 blocks)
        if (!history.isEmpty()) {
            Location last = history.peekLast();
            if (last != null && last.getWorld().equals(currentLoc.getWorld()) &&
                last.distance(currentLoc) < 5.0) {
                return; // Too close to last location, skip
            }
        }
        
        history.addLast(currentLoc.clone());
        
        // Limit history size
        while (history.size() > MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
    }

    public Location getLastLocation(Player player) {
        return lastLocations.get(player.getUniqueId());
    }

    // Get teleport history (all previous locations)
    public List<Location> getTeleportHistory(Player player) {
        Deque<Location> history = teleportHistory.get(player.getUniqueId());
        if (history == null || history.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(history);
    }

    // Get the most recent location from history (for /back)
    public Location getPreviousLocation(Player player) {
        Deque<Location> history = teleportHistory.get(player.getUniqueId());
        if (history == null || history.isEmpty()) {
            return null;
        }
        // Get the last location (most recent)
        return history.peekLast();
    }

    // Go back to previous location and remove it from history
    public Location goBack(Player player) {
        Deque<Location> history = teleportHistory.get(player.getUniqueId());
        if (history == null || history.isEmpty()) {
            return null;
        }
        // Remove the most recent location and return the one before it
        history.removeLast();
        if (history.isEmpty()) {
            return null;
        }
        return history.peekLast();
    }

    // Clear teleport history for a player
    public void clearHistory(Player player) {
        teleportHistory.remove(player.getUniqueId());
    }

    public void teleport(Player player, Location location) {
        saveLastLocation(player);
        player.teleport(location);
    }

    public void teleport(Player player, Player target) {
        saveLastLocation(player);
        player.teleport(target.getLocation());
    }

    public void teleportWithDelay(Player player, Location location, int delaySeconds) {
        if (teleportingPlayers.contains(player.getUniqueId())) {
            player.sendMessage("§cYou are already teleporting!");
            return;
        }

        teleportingPlayers.add(player.getUniqueId());
        player.sendMessage("§eTeleporting in " + delaySeconds + " seconds... Don't move!");

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (teleportingPlayers.contains(player.getUniqueId())) {
                if (player.isOnline()) {
                    teleport(player, location);
                    player.sendMessage("§aTeleported!");
                }
                teleportingPlayers.remove(player.getUniqueId());
            }
        }, delaySeconds * 20L);

        teleportTasks.put(player.getUniqueId(), task);
    }

    public void cancelTeleport(Player player) {
        BukkitTask task = teleportTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        teleportingPlayers.remove(player.getUniqueId());
    }

    public boolean isTeleporting(Player player) {
        return teleportingPlayers.contains(player.getUniqueId());
    }

    public void createRequest(Player requester, Player target, boolean here) {
        UUID targetUuid = target.getUniqueId();
        TeleportRequest existing = teleportRequests.get(targetUuid);
        if (existing != null && existing.getRequester().equals(requester.getUniqueId())) {
            requester.sendMessage("§cYou already have a pending request to " + target.getName() + "!");
            return;
        }

        TeleportRequest request = new TeleportRequest(requester.getUniqueId(), target.getUniqueId(), here, System.currentTimeMillis());
        teleportRequests.put(targetUuid, request);

        String message = here 
            ? "§e" + requester.getName() + " wants you to teleport to them. Use /tpaccept or /tpdeny"
            : "§e" + requester.getName() + " wants to teleport to you. Use /tpaccept or /tpdeny";
        target.sendMessage(message);
        requester.sendMessage("§aTeleport request sent to " + target.getName() + "!");

        // Auto-expire after 60 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (teleportRequests.get(targetUuid) == request) {
                teleportRequests.remove(targetUuid);
                if (requester.isOnline()) {
                    requester.sendMessage("§cYour teleport request to " + target.getName() + " expired.");
                }
            }
        }, 1200L);
    }

    public boolean acceptRequest(Player player) {
        TeleportRequest request = teleportRequests.remove(player.getUniqueId());
        if (request == null) {
            player.sendMessage("§cYou have no pending teleport requests!");
            return false;
        }

        Player requester = Bukkit.getPlayer(request.getRequester());
        if (requester == null || !requester.isOnline()) {
            player.sendMessage("§cThe player who sent the request is no longer online.");
            return false;
        }

        if (request.isHere()) {
            teleport(player, requester.getLocation());
            player.sendMessage("§aTeleported to " + requester.getName() + "!");
            requester.sendMessage("§a" + player.getName() + " accepted your teleport request!");
        } else {
            teleport(requester, player.getLocation());
            requester.sendMessage("§aTeleported to " + player.getName() + "!");
            player.sendMessage("§a" + requester.getName() + " teleported to you!");
        }

        plugin.getDiscordManager().sendStaffLogNotification(
            "teleport-log",
            requester.getName(),
            "teleported via request",
            player.getName(),
            ""
        );
        return true;
    }

    public boolean denyRequest(Player player) {
        TeleportRequest request = teleportRequests.remove(player.getUniqueId());
        if (request == null) {
            player.sendMessage("§cYou have no pending teleport requests!");
            return false;
        }

        Player requester = Bukkit.getPlayer(request.getRequester());
        if (requester != null && requester.isOnline()) {
            requester.sendMessage("§c" + player.getName() + " denied your teleport request.");
        }
        player.sendMessage("§aTeleport request denied.");
        return true;
    }

    // Death location tracking
    public void saveDeathLocation(Player player) {
        deathLocations.put(player.getUniqueId(), player.getLocation());
    }

    public Location getDeathLocation(Player player) {
        return deathLocations.get(player.getUniqueId());
    }

    // Teleport to top (highest block)
    public boolean teleportToTop(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return false;

        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        int y = world.getMaxHeight() - 1;

        // Find highest solid block
        while (y > world.getMinHeight()) {
            Block block = world.getBlockAt(x, y, z);
            Block above = world.getBlockAt(x, y + 1, z);
            if (block.getType().isSolid() && !above.getType().isSolid()) {
                Location safeLoc = new Location(world, x + 0.5, y + 1, z + 0.5, loc.getYaw(), loc.getPitch());
                teleport(player, safeLoc);
                player.sendMessage("§aTeleported to highest block!");
                return true;
            }
            y--;
        }

        player.sendMessage("§cCould not find a safe location!");
        return false;
    }

    // Teleport forward (jump)
    public boolean teleportJump(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return false;

        // Get direction vector
        double yaw = Math.toRadians(-loc.getYaw() - 90);
        double pitch = Math.toRadians(-loc.getPitch());
        double distance = 8.0; // Default jump distance

        double x = loc.getX() + Math.cos(yaw) * Math.cos(pitch) * distance;
        double z = loc.getZ() + Math.sin(yaw) * Math.cos(pitch) * distance;
        double y = loc.getY() + Math.sin(pitch) * distance;

        Location targetLoc = new Location(world, x, y, z, loc.getYaw(), loc.getPitch());

        // Find safe location below
        int blockY = targetLoc.getBlockY();
        while (blockY > world.getMinHeight()) {
            Block block = world.getBlockAt(targetLoc.getBlockX(), blockY, targetLoc.getBlockZ());
            Block above = world.getBlockAt(targetLoc.getBlockX(), blockY + 1, targetLoc.getBlockZ());
            Block below = world.getBlockAt(targetLoc.getBlockX(), blockY - 1, targetLoc.getBlockZ());
            
            if (block.getType() == Material.AIR && above.getType() == Material.AIR && below.getType().isSolid()) {
                Location safeLoc = new Location(world, x, blockY + 1, z, loc.getYaw(), loc.getPitch());
                teleport(player, safeLoc);
                player.sendMessage("§aJumped forward!");
                return true;
            }
            blockY--;
        }

        player.sendMessage("§cCould not find a safe location!");
        return false;
    }

    // Random teleport
    public boolean teleportRandom(Player player) {
        World world = player.getWorld();
        if (world == null) return false;

        // Get world border or default bounds
        int minX = -10000;
        int maxX = 10000;
        int minZ = -10000;
        int maxZ = 10000;
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();

        // Try to find safe location (max 50 attempts)
        for (int attempt = 0; attempt < 50; attempt++) {
            int x = random.nextInt(maxX - minX) + minX;
            int z = random.nextInt(maxZ - minZ) + minZ;
            int y = maxY - 1;

            // Find highest solid block
            while (y > minY) {
                Block block = world.getBlockAt(x, y, z);
                Block above = world.getBlockAt(x, y + 1, z);
                Block above2 = world.getBlockAt(x, y + 2, z);
                
                if (block.getType().isSolid() && 
                    !above.getType().isSolid() && 
                    !above2.getType().isSolid() &&
                    !isDangerous(above.getType())) {
                    Location safeLoc = new Location(world, x + 0.5, y + 1, z + 0.5);
                    teleport(player, safeLoc);
                    player.sendMessage("§aRandomly teleported!");
                    return true;
                }
                y--;
            }
        }

        player.sendMessage("§cCould not find a safe random location! Try again.");
        return false;
    }

    private boolean isDangerous(Material material) {
        return material == Material.LAVA || 
               material == Material.FIRE || 
               material == Material.MAGMA_BLOCK ||
               material == Material.CACTUS;
    }

    // Teleport to coordinates
    public boolean teleportToCoordinates(Player player, double x, double y, double z, World world) {
        if (world == null) {
            world = player.getWorld();
        }

        Location targetLoc = new Location(world, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
        
        // Validate location is safe
        Block block = world.getBlockAt(targetLoc.getBlockX(), targetLoc.getBlockY(), targetLoc.getBlockZ());
        Block above = world.getBlockAt(targetLoc.getBlockX(), targetLoc.getBlockY() + 1, targetLoc.getBlockZ());
        
        if (!block.getType().isSolid() && !above.getType().isSolid()) {
            teleport(player, targetLoc);
            player.sendMessage("§aTeleported to coordinates!");
            return true;
        } else {
            // Find safe Y position
            int safeY = targetLoc.getBlockY();
            while (safeY > world.getMinHeight()) {
                Block checkBlock = world.getBlockAt(targetLoc.getBlockX(), safeY, targetLoc.getBlockZ());
                Block checkAbove = world.getBlockAt(targetLoc.getBlockX(), safeY + 1, targetLoc.getBlockZ());
                if (checkBlock.getType().isSolid() && !checkAbove.getType().isSolid()) {
                    Location safeLoc = new Location(world, x, safeY + 1, z, targetLoc.getYaw(), targetLoc.getPitch());
                    teleport(player, safeLoc);
                    player.sendMessage("§aTeleported to coordinates (adjusted for safety)!");
                    return true;
                }
                safeY--;
            }
        }

        player.sendMessage("§cInvalid or unsafe coordinates!");
        return false;
    }

    // Teleport to biome (async search)
    public void teleportToBiome(Player player, Biome biome) {
        World world = player.getWorld();
        Location startLoc = player.getLocation();
        
        player.sendMessage("§eSearching for biome " + biome.name() + "... This may take a moment.");
        
        // Run async to avoid blocking server
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int searchRadius = 2000; // Search within 2000 blocks
            int step = 50; // Check every 50 blocks
            int attempts = 0;
            int maxAttempts = 100;
            
            for (int x = -searchRadius; x < searchRadius && attempts < maxAttempts; x += step) {
                for (int z = -searchRadius; z < searchRadius && attempts < maxAttempts; z += step) {
                    attempts++;
                    int checkX = startLoc.getBlockX() + x;
                    int checkZ = startLoc.getBlockZ() + z;
                    
                    Biome foundBiome = world.getBiome(checkX, world.getHighestBlockYAt(checkX, checkZ), checkZ);
                    if (foundBiome == biome) {
                        // Found the biome, find safe location
                        int y = world.getHighestBlockYAt(checkX, checkZ);
                        Location targetLoc = new Location(world, checkX + 0.5, y + 1, checkZ + 0.5);
                        
                        // Teleport on main thread
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            teleport(player, targetLoc);
                            player.sendMessage("§aTeleported to " + biome.name() + " biome!");
                        });
                        return;
                    }
                }
            }
            
            // Not found
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage("§cCould not find " + biome.name() + " biome nearby! Try a different location.");
            });
        });
    }

    // Teleport to structure (async search)
    public void teleportToStructure(Player player, Structure structure) {
        World world = player.getWorld();
        Location startLoc = player.getLocation();
        
        String structureName;
        try {
            // Try to get structure name from key if available
            structureName = structure.getKey().getKey();
            if (structureName == null) {
                structureName = structure.toString();
            }
        } catch (Exception e) {
            // Use toString as fallback
            structureName = structure.toString();
        }
        final String finalStructureName = structureName;
        
        player.sendMessage("§eSearching for structure " + finalStructureName + "... This may take a moment.");
        
        // Run async to avoid blocking server
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Use locateStructure method (available in newer Bukkit versions)
                Object result = world.locateNearestStructure(
                    startLoc,
                    structure,
                    100, // radius
                    false // findUnexplored
                );
                
                if (result != null) {
                    Location structureLoc;
                    // Handle different return types based on API version
                    if (result instanceof Location) {
                        structureLoc = (Location) result;
                    } else {
                        // Try to get location from result object using reflection
                        try {
                            java.lang.reflect.Method getLocation = result.getClass().getMethod("getLocation");
                            structureLoc = (Location) getLocation.invoke(result);
                        } catch (Exception e) {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                player.sendMessage("§cCould not find " + finalStructureName + " structure nearby!");
                            });
                            return;
                        }
                    }
                    
                    // Find safe location near structure
                    int y = world.getHighestBlockYAt(structureLoc.getBlockX(), structureLoc.getBlockZ());
                    Location targetLoc = new Location(world, structureLoc.getBlockX() + 0.5, y + 1, structureLoc.getBlockZ() + 0.5);
                    
                    // Teleport on main thread
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        teleport(player, targetLoc);
                        player.sendMessage("§aTeleported to " + finalStructureName + " structure!");
                    });
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage("§cCould not find " + finalStructureName + " structure nearby!");
                    });
                }
            } catch (Exception e) {
                // Fallback: try manual search
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage("§cCould not find " + finalStructureName + " structure! The structure may not exist in this world.");
                });
            }
        });
    }

    public static class TeleportRequest {
        private final UUID requester;
        private final UUID target;
        private final boolean here;
        private final long timestamp;

        public TeleportRequest(UUID requester, UUID target, boolean here, long timestamp) {
            this.requester = requester;
            this.target = target;
            this.here = here;
            this.timestamp = timestamp;
        }

        public UUID getRequester() {
            return requester;
        }

        public UUID getTarget() {
            return target;
        }

        public boolean isHere() {
            return here;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}

