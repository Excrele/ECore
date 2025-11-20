package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages portal creation and seamless teleportation between worlds.
 * Similar to Multiverse-Portals functionality.
 * 
 * @author Excrele
 * @version 1.0
 */
public class PortalManager {
    private final Ecore plugin;
    private File portalsFile;
    private FileConfiguration portalsConfig;
    private final Map<String, Portal> portals;
    private final Map<Location, Portal> portalBlocks; // Block location -> Portal

    /**
     * Creates a new PortalManager instance.
     * 
     * @param plugin The Ecore plugin instance
     */
    public PortalManager(Ecore plugin) {
        this.plugin = plugin;
        this.portals = new HashMap<>();
        this.portalBlocks = new HashMap<>();
        initializePortalsFile();
        loadPortals();
    }

    /**
     * Initializes the portals configuration file.
     */
    private void initializePortalsFile() {
        portalsFile = new File(plugin.getDataFolder(), "portals.yml");
        if (!portalsFile.exists()) {
            try {
                portalsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create portals.yml: " + e.getMessage());
            }
        }
        portalsConfig = YamlConfiguration.loadConfiguration(portalsFile);
    }

    /**
     * Loads all portals from the configuration file.
     */
    public void loadPortals() {
        portals.clear();
        portalBlocks.clear();
        
        if (!portalsConfig.contains("portals")) {
            return;
        }

        for (String portalName : portalsConfig.getConfigurationSection("portals").getKeys(false)) {
            String path = "portals." + portalName;
            Portal portal = new Portal();
            portal.setName(portalName);
            
            // Load destination
            String destWorld = portalsConfig.getString(path + ".destination.world");
            double destX = portalsConfig.getDouble(path + ".destination.x");
            double destY = portalsConfig.getDouble(path + ".destination.y");
            double destZ = portalsConfig.getDouble(path + ".destination.z");
            float destYaw = (float) portalsConfig.getDouble(path + ".destination.yaw", 0);
            float destPitch = (float) portalsConfig.getDouble(path + ".destination.pitch", 0);
            
            World destWorldObj = Bukkit.getWorld(destWorld);
            if (destWorldObj != null) {
                portal.setDestination(new Location(destWorldObj, destX, destY, destZ, destYaw, destPitch));
            }
            
            // Load portal blocks
            List<Location> blocks = new ArrayList<>();
            if (portalsConfig.contains(path + ".blocks")) {
                for (String blockKey : portalsConfig.getConfigurationSection(path + ".blocks").getKeys(false)) {
                    String worldName = portalsConfig.getString(path + ".blocks." + blockKey + ".world");
                    int x = portalsConfig.getInt(path + ".blocks." + blockKey + ".x");
                    int y = portalsConfig.getInt(path + ".blocks." + blockKey + ".y");
                    int z = portalsConfig.getInt(path + ".blocks." + blockKey + ".z");
                    
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        Location blockLoc = new Location(world, x, y, z);
                        blocks.add(blockLoc);
                    }
                }
            }
            portal.setBlocks(blocks);
            
            // Load other properties
            portal.setMaterial(Material.valueOf(portalsConfig.getString(path + ".material", "NETHER_PORTAL").toUpperCase()));
            portal.setPermission(portalsConfig.getString(path + ".permission", null));
            portal.setMessage(portalsConfig.getString(path + ".message", null));
            portal.setSound(portalsConfig.getString(path + ".sound", null));
            
            portals.put(portalName.toLowerCase(), portal);
            
            // Register portal blocks
            for (Location blockLoc : blocks) {
                portalBlocks.put(blockLoc, portal);
            }
        }
        
        plugin.getLogger().info("Loaded " + portals.size() + " portal(s)!");
    }

    /**
     * Saves all portals to the configuration file.
     */
    private void savePortals() {
        try {
            portalsConfig.save(portalsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save portals.yml: " + e.getMessage());
        }
    }

    /**
     * Creates a new portal.
     * 
     * @param name The portal name
     * @param blocks The blocks that make up the portal
     * @param destination The destination location
     * @param material The material to use for the portal (default: NETHER_PORTAL)
     * @return true if the portal was created successfully
     */
    public boolean createPortal(String name, List<Location> blocks, Location destination, Material material) {
        if (portals.containsKey(name.toLowerCase())) {
            plugin.getLogger().warning("Portal '" + name + "' already exists!");
            return false;
        }

        if (blocks == null || blocks.isEmpty()) {
            plugin.getLogger().warning("Portal must have at least one block!");
            return false;
        }

        if (destination == null || destination.getWorld() == null) {
            plugin.getLogger().warning("Portal destination must be valid!");
            return false;
        }

        Portal portal = new Portal();
        portal.setName(name);
        portal.setBlocks(new ArrayList<>(blocks));
        portal.setDestination(destination);
        portal.setMaterial(material != null ? material : Material.NETHER_PORTAL);
        
        portals.put(name.toLowerCase(), portal);
        
        // Register portal blocks
        for (Location blockLoc : blocks) {
            portalBlocks.put(blockLoc, portal);
        }
        
        savePortal(portal);
        plugin.getLogger().info("Created portal '" + name + "' with " + blocks.size() + " block(s)!");
        return true;
    }

    /**
     * Creates a portal from a selection (two corners).
     * 
     * @param name The portal name
     * @param corner1 First corner
     * @param corner2 Second corner
     * @param destination The destination location
     * @param material The material to use for the portal
     * @return true if the portal was created successfully
     */
    public boolean createPortalFromSelection(String name, Location corner1, Location corner2, 
                                             Location destination, Material material) {
        if (!corner1.getWorld().equals(corner2.getWorld())) {
            plugin.getLogger().warning("Portal corners must be in the same world!");
            return false;
        }

        List<Location> blocks = new ArrayList<>();
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        World world = corner1.getWorld();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(new Location(world, x, y, z));
                }
            }
        }

        return createPortal(name, blocks, destination, material);
    }

    /**
     * Deletes a portal.
     * 
     * @param name The portal name
     * @return true if the portal was deleted successfully
     */
    public boolean deletePortal(String name) {
        Portal portal = portals.remove(name.toLowerCase());
        if (portal == null) {
            plugin.getLogger().warning("Portal '" + name + "' does not exist!");
            return false;
        }

        // Remove portal blocks
        for (Location blockLoc : portal.getBlocks()) {
            portalBlocks.remove(blockLoc);
        }

        portalsConfig.set("portals." + name, null);
        savePortals();
        plugin.getLogger().info("Deleted portal '" + name + "'!");
        return true;
    }

    /**
     * Gets a portal by name.
     * 
     * @param name The portal name
     * @return The Portal, or null if not found
     */
    public Portal getPortal(String name) {
        return portals.get(name.toLowerCase());
    }

    /**
     * Gets a portal by block location.
     * 
     * @param location The block location
     * @return The Portal, or null if not found
     */
    public Portal getPortalByBlock(Location location) {
        // Check exact match first
        Portal portal = portalBlocks.get(location);
        if (portal != null) {
            return portal;
        }

        // Check if location is within any portal's blocks
        for (Portal p : portals.values()) {
            for (Location blockLoc : p.getBlocks()) {
                if (blockLoc.getWorld().equals(location.getWorld()) &&
                    blockLoc.getBlockX() == location.getBlockX() &&
                    blockLoc.getBlockY() == location.getBlockY() &&
                    blockLoc.getBlockZ() == location.getBlockZ()) {
                    return p;
                }
            }
        }

        return null;
    }

    /**
     * Teleports a player through a portal.
     * 
     * @param player The player to teleport
     * @param portal The portal
     * @return true if teleportation was successful
     */
    public boolean teleportThroughPortal(Player player, Portal portal) {
        if (portal == null) {
            return false;
        }

        // Check permission
        if (portal.getPermission() != null && !player.hasPermission(portal.getPermission())) {
            player.sendMessage("§cYou don't have permission to use this portal!");
            return false;
        }

        Location destination = portal.getDestination();
        if (destination == null || destination.getWorld() == null) {
            // Try to load the world
            World world = plugin.getWorldManager().loadWorld(destination.getWorld().getName());
            if (world == null) {
                player.sendMessage("§cPortal destination world is not available!");
                return false;
            }
            destination.setWorld(world);
        }

        // Find safe location
        destination = findSafeLocation(destination);

        // Play sound if configured
        if (portal.getSound() != null) {
            try {
                Sound sound = Sound.valueOf(portal.getSound().toUpperCase());
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                // Invalid sound, ignore
            }
        }

        // Teleport player
        plugin.getTeleportManager().teleport(player, destination);

        // Send message if configured
        if (portal.getMessage() != null) {
            String message = portal.getMessage()
                .replace("%world%", destination.getWorld().getName())
                .replace("%player%", player.getName());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        } else {
            player.sendMessage("§aTeleported to " + destination.getWorld().getName() + "!");
        }

        return true;
    }

    /**
     * Finds a safe location near the given location.
     */
    private Location findSafeLocation(Location loc) {
        World world = loc.getWorld();
        if (world == null) return loc;

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        // Check if current location is safe
        if (isSafeLocation(world, x, y, z)) {
            return new Location(world, x + 0.5, y, z + 0.5, loc.getYaw(), loc.getPitch());
        }

        // Search for safe location nearby
        for (int radius = 1; radius <= 5; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                        int checkX = x + dx;
                        int checkZ = z + dz;
                        int checkY = world.getHighestBlockYAt(checkX, checkZ);
                        
                        if (isSafeLocation(world, checkX, checkY, checkZ)) {
                            return new Location(world, checkX + 0.5, checkY + 1, checkZ + 0.5, loc.getYaw(), loc.getPitch());
                        }
                    }
                }
            }
        }

        // Fallback to spawn location
        return world.getSpawnLocation();
    }

    /**
     * Checks if a location is safe for teleportation.
     */
    private boolean isSafeLocation(World world, int x, int y, int z) {
        Material block = world.getBlockAt(x, y, z).getType();
        Material above = world.getBlockAt(x, y + 1, z).getType();
        Material below = world.getBlockAt(x, y - 1, z).getType();

        return block == Material.AIR && 
               above == Material.AIR && 
               !below.isAir() && 
               !below.equals(Material.LAVA) &&
               !below.equals(Material.MAGMA_BLOCK);
    }

    /**
     * Gets all portal names.
     * 
     * @return List of portal names
     */
    public List<String> getAllPortals() {
        return new ArrayList<>(portals.keySet());
    }

    /**
     * Checks if a portal exists.
     * 
     * @param name The portal name
     * @return true if the portal exists
     */
    public boolean portalExists(String name) {
        return portals.containsKey(name.toLowerCase());
    }

    /**
     * Updates a portal's destination.
     * 
     * @param name The portal name
     * @param destination The new destination
     * @return true if the update was successful
     */
    public boolean updatePortalDestination(String name, Location destination) {
        Portal portal = portals.get(name.toLowerCase());
        if (portal == null) {
            return false;
        }

        portal.setDestination(destination);
        savePortal(portal);
        return true;
    }

    /**
     * Saves a portal to the configuration file.
     */
    private void savePortal(Portal portal) {
        String path = "portals." + portal.getName();
        
        // Save destination
        Location dest = portal.getDestination();
        if (dest != null) {
            portalsConfig.set(path + ".destination.world", dest.getWorld().getName());
            portalsConfig.set(path + ".destination.x", dest.getX());
            portalsConfig.set(path + ".destination.y", dest.getY());
            portalsConfig.set(path + ".destination.z", dest.getZ());
            portalsConfig.set(path + ".destination.yaw", dest.getYaw());
            portalsConfig.set(path + ".destination.pitch", dest.getPitch());
        }
        
        // Save blocks
        int index = 0;
        for (Location blockLoc : portal.getBlocks()) {
            String blockPath = path + ".blocks." + index;
            portalsConfig.set(blockPath + ".world", blockLoc.getWorld().getName());
            portalsConfig.set(blockPath + ".x", blockLoc.getBlockX());
            portalsConfig.set(blockPath + ".y", blockLoc.getBlockY());
            portalsConfig.set(blockPath + ".z", blockLoc.getBlockZ());
            index++;
        }
        
        // Save other properties
        portalsConfig.set(path + ".material", portal.getMaterial().name());
        if (portal.getPermission() != null) {
            portalsConfig.set(path + ".permission", portal.getPermission());
        }
        if (portal.getMessage() != null) {
            portalsConfig.set(path + ".message", portal.getMessage());
        }
        if (portal.getSound() != null) {
            portalsConfig.set(path + ".sound", portal.getSound());
        }
        
        savePortals();
    }

    /**
     * Portal data class.
     */
    public static class Portal {
        private String name;
        private List<Location> blocks;
        private Location destination;
        private Material material;
        private String permission;
        private String message;
        private String sound;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public List<Location> getBlocks() { return blocks; }
        public void setBlocks(List<Location> blocks) { this.blocks = blocks; }
        
        public Location getDestination() { return destination; }
        public void setDestination(Location destination) { this.destination = destination; }
        
        public Material getMaterial() { return material; }
        public void setMaterial(Material material) { this.material = material; }
        
        public String getPermission() { return permission; }
        public void setPermission(String permission) { this.permission = permission; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getSound() { return sound; }
        public void setSound(String sound) { this.sound = sound; }
    }
}

