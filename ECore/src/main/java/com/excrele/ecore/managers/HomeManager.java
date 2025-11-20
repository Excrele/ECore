package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

/**
 * Manages the home system including:
 * - Home creation and deletion
 * - Home teleportation with cooldowns and costs
 * - Home sharing between players
 * - Home categories and icons
 * - Bed spawn integration
 * 
 * @author Excrele
 * @version 1.0
 */
public class HomeManager {
    private final Ecore plugin;
    private File homeFile;
    private FileConfiguration homeConfig;
    private final Map<UUID, Long> homeCooldowns; // UUID -> last teleport timestamp

    public HomeManager(Ecore plugin) {
        this.plugin = plugin;
        this.homeCooldowns = new HashMap<>();
        initializeHomeConfig();
    }

    private void initializeHomeConfig() {
        homeFile = new File(plugin.getDataFolder(), "homes.yml");
        if (!homeFile.exists()) {
            try {
                homeFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create homes.yml", e);
            }
        }
        homeConfig = YamlConfiguration.loadConfiguration(homeFile);
    }

    public boolean setHome(Player player, String homeName, Location location) {
        String uuid = player.getUniqueId().toString();
        List<String> homes = homeConfig.getStringList("homes." + uuid + ".list");
        if (homes.size() >= plugin.getConfigManager().getMaxHomes()) {
            return false;
        }

        if (!homes.contains(homeName)) {
            homes.add(homeName);
            homeConfig.set("homes." + uuid + ".list", homes);
        }

        String path = "homes." + uuid + "." + homeName;
        homeConfig.set(path + ".world", location.getWorld().getName());
        homeConfig.set(path + ".x", location.getX());
        homeConfig.set(path + ".y", location.getY());
        homeConfig.set(path + ".z", location.getZ());
        homeConfig.set(path + ".yaw", location.getYaw());
        homeConfig.set(path + ".pitch", location.getPitch());
        
        // Initialize default values for new features if not set
        if (!homeConfig.contains(path + ".category")) {
            homeConfig.set(path + ".category", "default");
        }
        if (!homeConfig.contains(path + ".icon")) {
            homeConfig.set(path + ".icon", "COMPASS");
        }
        if (!homeConfig.contains(path + ".description")) {
            homeConfig.set(path + ".description", "");
        }
        if (!homeConfig.contains(path + ".shared-with")) {
            homeConfig.set(path + ".shared-with", new ArrayList<String>());
        }

        try {
            homeConfig.save(homeFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save home to homes.yml: " + e.getMessage());
            return false;
        }

        plugin.getDiscordManager().sendStaffLogNotification(
                "home-log",
                player.getName(),
                "set home",
                homeName,
                location.toString()
        );
        return true;
    }

    public List<String> getHomes(Player player) {
        String uuid = player.getUniqueId().toString();
        return homeConfig.getStringList("homes." + uuid + ".list");
    }

    public Location getHome(Player player, String homeName) {
        return getHome(player.getUniqueId(), homeName);
    }

    public Location getHome(UUID ownerUuid, String homeName) {
        String uuid = ownerUuid.toString();
        String path = "homes." + uuid + "." + homeName;

        if (!homeConfig.contains(path)) {
            return null;
        }

        String worldName = homeConfig.getString(path + ".world");
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World " + worldName + " not found for home " + homeName);
            return null;
        }

        double x = homeConfig.getDouble(path + ".x");
        double y = homeConfig.getDouble(path + ".y");
        double z = homeConfig.getDouble(path + ".z");
        float yaw = (float) homeConfig.getDouble(path + ".yaw");
        float pitch = (float) homeConfig.getDouble(path + ".pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public List<String> getPlayerHomes(Player player) {
        return getHomes(player);
    }

    // Check if player can teleport to home (cost and cooldown)
    public boolean canTeleportToHome(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Check cooldown
        int cooldown = plugin.getConfig().getInt("home.cooldown", 0);
        if (cooldown > 0) {
            Long lastTeleport = homeCooldowns.get(uuid);
            if (lastTeleport != null) {
                long timeSince = (System.currentTimeMillis() - lastTeleport) / 1000;
                if (timeSince < cooldown) {
                    long remaining = cooldown - timeSince;
                    player.sendMessage("§cYou must wait " + remaining + " more seconds before teleporting to a home!");
                    return false;
                }
            }
        }
        
        // Check cost
        double cost = plugin.getConfig().getDouble("home.teleport-cost", 0.0);
        if (cost > 0) {
            double balance = plugin.getEconomyManager().getBalance(uuid);
            if (balance < cost) {
                player.sendMessage("§cYou don't have enough money! Cost: " + String.format("%.2f", cost));
                return false;
            }
        }
        
        return true;
    }

    // Teleport to home with cost, cooldown, and warmup
    public void teleportToHome(Player player, String homeName) {
        Location home = getHome(player, homeName);
        if (home == null) {
            player.sendMessage("§cHome '" + homeName + "' does not exist or the world is not loaded!");
            return;
        }

        if (!canTeleportToHome(player)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        double cost = plugin.getConfig().getDouble("home.teleport-cost", 0.0);
        int warmup = plugin.getConfig().getInt("home.warmup", 0);

        // Deduct cost
        if (cost > 0) {
            plugin.getEconomyManager().removeBalance(uuid, cost);
            player.sendMessage("§eCharged " + String.format("%.2f", cost) + " for home teleport!");
        }

        // Update cooldown
        int cooldown = plugin.getConfig().getInt("home.cooldown", 0);
        if (cooldown > 0) {
            homeCooldowns.put(uuid, System.currentTimeMillis());
        }

        // Teleport with warmup if configured
        if (warmup > 0) {
            player.sendMessage("§eTeleporting to home '" + homeName + "' in " + warmup + " seconds... Don't move!");
            plugin.getTeleportManager().teleportWithDelay(player, home, warmup);
        } else {
            plugin.getTeleportManager().teleport(player, home);
            player.sendMessage("§aTeleported to home '" + homeName + "'!");
        }

        plugin.getDiscordManager().sendStaffLogNotification(
            "home-log",
            player.getName(),
            "teleported to home",
            homeName,
            home.toString()
        );
    }

    // Get remaining cooldown
    public long getCooldownRemaining(Player player) {
        UUID uuid = player.getUniqueId();
        int cooldown = plugin.getConfig().getInt("home.cooldown", 0);
        if (cooldown <= 0) {
            return 0;
        }
        
        Long lastTeleport = homeCooldowns.get(uuid);
        if (lastTeleport == null) {
            return 0;
        }
        
        long timeSince = (System.currentTimeMillis() - lastTeleport) / 1000;
        if (timeSince >= cooldown) {
            return 0;
        }
        
        return cooldown - timeSince;
    }

    // Home sharing methods
    public boolean shareHome(Player owner, String homeName, Player target) {
        String uuid = owner.getUniqueId().toString();
        String path = "homes." + uuid + "." + homeName;
        
        if (!homeConfig.contains(path)) {
            return false;
        }
        
        List<String> sharedWith = homeConfig.getStringList(path + ".shared-with");
        String targetUuid = target.getUniqueId().toString();
        
        if (sharedWith.contains(targetUuid)) {
            return false; // Already shared
        }
        
        sharedWith.add(targetUuid);
        homeConfig.set(path + ".shared-with", sharedWith);
        saveHomeConfig();
        return true;
    }

    public boolean unshareHome(Player owner, String homeName, Player target) {
        String uuid = owner.getUniqueId().toString();
        String path = "homes." + uuid + "." + homeName;
        
        if (!homeConfig.contains(path)) {
            return false;
        }
        
        List<String> sharedWith = homeConfig.getStringList(path + ".shared-with");
        String targetUuid = target.getUniqueId().toString();
        
        if (!sharedWith.remove(targetUuid)) {
            return false; // Not shared with this player
        }
        
        homeConfig.set(path + ".shared-with", sharedWith);
        saveHomeConfig();
        return true;
    }

    public List<String> getSharedWith(Player owner, String homeName) {
        String uuid = owner.getUniqueId().toString();
        String path = "homes." + uuid + "." + homeName;
        return homeConfig.getStringList(path + ".shared-with");
    }

    public boolean isHomeSharedWith(Player owner, String homeName, Player player) {
        List<String> sharedWith = getSharedWith(owner, homeName);
        return sharedWith.contains(player.getUniqueId().toString());
    }

    // Get homes shared with a player
    public List<Map.Entry<String, UUID>> getSharedHomes(Player player) {
        List<Map.Entry<String, UUID>> sharedHomes = new ArrayList<>();
        String playerUuid = player.getUniqueId().toString();
        
        if (homeConfig.contains("homes")) {
            for (String ownerUuidStr : homeConfig.getConfigurationSection("homes").getKeys(false)) {
                try {
                    UUID ownerUuid = UUID.fromString(ownerUuidStr);
                    if (homeConfig.contains("homes." + ownerUuidStr)) {
                        for (String homeName : homeConfig.getStringList("homes." + ownerUuidStr + ".list")) {
                            String path = "homes." + ownerUuidStr + "." + homeName;
                            List<String> sharedWith = homeConfig.getStringList(path + ".shared-with");
                            if (sharedWith.contains(playerUuid)) {
                                sharedHomes.add(new java.util.AbstractMap.SimpleEntry<>(homeName, ownerUuid));
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid UUID, skip
                }
            }
        }
        
        return sharedHomes;
    }

    // Home category methods
    public boolean setHomeCategory(Player player, String homeName, String category) {
        String uuid = player.getUniqueId().toString();
        String path = "homes." + uuid + "." + homeName;
        
        if (!homeConfig.contains(path)) {
            return false;
        }
        
        homeConfig.set(path + ".category", category);
        saveHomeConfig();
        return true;
    }

    public String getHomeCategory(Player player, String homeName) {
        String uuid = player.getUniqueId().toString();
        String path = "homes." + uuid + "." + homeName;
        return homeConfig.getString(path + ".category", "default");
    }

    public List<String> getHomesByCategory(Player player, String category) {
        List<String> homes = new ArrayList<>();
        for (String homeName : getHomes(player)) {
            if (getHomeCategory(player, homeName).equalsIgnoreCase(category)) {
                homes.add(homeName);
            }
        }
        return homes;
    }

    // Home icon methods
    public boolean setHomeIcon(Player player, String homeName, Material icon) {
        String uuid = player.getUniqueId().toString();
        String path = "homes." + uuid + "." + homeName;
        
        if (!homeConfig.contains(path)) {
            return false;
        }
        
        homeConfig.set(path + ".icon", icon.name());
        saveHomeConfig();
        return true;
    }

    public Material getHomeIcon(Player player, String homeName) {
        String uuid = player.getUniqueId().toString();
        String path = "homes." + uuid + "." + homeName;
        String iconName = homeConfig.getString(path + ".icon", "COMPASS");
        
        try {
            return Material.valueOf(iconName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.COMPASS;
        }
    }

    // Home description methods
    public boolean setHomeDescription(Player player, String homeName, String description) {
        String uuid = player.getUniqueId().toString();
        String path = "homes." + uuid + "." + homeName;
        
        if (!homeConfig.contains(path)) {
            return false;
        }
        
        homeConfig.set(path + ".description", description);
        saveHomeConfig();
        return true;
    }

    public String getHomeDescription(Player player, String homeName) {
        String uuid = player.getUniqueId().toString();
        String path = "homes." + uuid + "." + homeName;
        return homeConfig.getString(path + ".description", "");
    }

    // Check if player can access a home (owner or shared)
    public boolean canAccessHome(Player player, UUID ownerUuid, String homeName) {
        if (player.getUniqueId().equals(ownerUuid)) {
            return true; // Owner
        }
        
        String path = "homes." + ownerUuid.toString() + "." + homeName;
        List<String> sharedWith = homeConfig.getStringList(path + ".shared-with");
        return sharedWith.contains(player.getUniqueId().toString());
    }

    // Get home owner name from UUID
    public String getHomeOwnerName(UUID ownerUuid) {
        OfflinePlayer owner = plugin.getServer().getOfflinePlayer(ownerUuid);
        return owner.getName() != null ? owner.getName() : ownerUuid.toString();
    }

    private void saveHomeConfig() {
        try {
            homeConfig.save(homeFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save homes.yml: " + e.getMessage());
        }
    }

    // Bed spawn integration methods
    public boolean setBedSpawn(Player player, Location bedLocation) {
        return setHome(player, "bed", bedLocation);
    }

    public Location getBedSpawn(Player player) {
        return getHome(player, "bed");
    }

    public boolean hasBedSpawn(Player player) {
        return getBedSpawn(player) != null;
    }

    public boolean useBedAsHome(Player player) {
        if (!plugin.getConfig().getBoolean("home.bed-spawn-enabled", true)) {
            return false;
        }
        
        Location bedSpawn = getBedSpawn(player);
        if (bedSpawn == null) {
            return false;
        }
        
        // Check if bed location is still valid (bed still exists)
        Material bedMaterial = bedSpawn.getBlock().getType();
        String materialName = bedMaterial.name();
        if (!materialName.endsWith("_BED") && !materialName.equals("BED")) {
            // Bed no longer exists, remove bed home
            deleteHome(player, "bed");
            return false;
        }
        
        return true;
    }

    public boolean deleteHome(Player player, String homeName) {
        String uuid = player.getUniqueId().toString();
        String path = "homes." + uuid + "." + homeName;
        
        if (!homeConfig.contains(path)) {
            return false;
        }
        
        // Remove from list
        List<String> homes = homeConfig.getStringList("homes." + uuid + ".list");
        homes.remove(homeName);
        homeConfig.set("homes." + uuid + ".list", homes);
        
        // Remove home data
        homeConfig.set(path, null);
        saveHomeConfig();
        return true;
    }
}