package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class JailManager {
    private final Ecore plugin;
    private File jailFile;
    private FileConfiguration jailConfig;
    private final Map<UUID, Location> preJailLocations;
    private final Map<UUID, BukkitTask> jailTasks;

    public JailManager(Ecore plugin) {
        this.plugin = plugin;
        this.preJailLocations = new HashMap<>();
        this.jailTasks = new HashMap<>();
        initializeJailConfig();
    }

    private void initializeJailConfig() {
        jailFile = new File(plugin.getDataFolder(), "jails.yml");
        if (!jailFile.exists()) {
            try {
                jailFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create jails.yml", e);
            }
        }
        jailConfig = YamlConfiguration.loadConfiguration(jailFile);
    }

    public boolean createJail(String name, Location location) {
        String path = "jails." + name.toLowerCase();
        if (jailConfig.contains(path)) {
            return false; // Jail already exists
        }
        jailConfig.set(path + ".world", location.getWorld().getName());
        jailConfig.set(path + ".x", location.getX());
        jailConfig.set(path + ".y", location.getY());
        jailConfig.set(path + ".z", location.getZ());
        jailConfig.set(path + ".yaw", location.getYaw());
        jailConfig.set(path + ".pitch", location.getPitch());
        saveJails();
        return true;
    }

    public boolean deleteJail(String name) {
        if (!jailConfig.contains("jails." + name.toLowerCase())) {
            return false;
        }
        jailConfig.set("jails." + name.toLowerCase(), null);
        saveJails();
        return true;
    }

    public Location getJail(String name) {
        String path = "jails." + name.toLowerCase();
        if (!jailConfig.contains(path)) {
            return null;
        }

        String worldName = jailConfig.getString(path + ".world");
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            return null;
        }

        double x = jailConfig.getDouble(path + ".x");
        double y = jailConfig.getDouble(path + ".y");
        double z = jailConfig.getDouble(path + ".z");
        float yaw = (float) jailConfig.getDouble(path + ".yaw");
        float pitch = (float) jailConfig.getDouble(path + ".pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public void jailPlayer(Player player, String jailName, long durationSeconds, String reason, Player staff) {
        Location jail = getJail(jailName);
        if (jail == null) {
            if (staff != null) {
                staff.sendMessage("§cJail '" + jailName + "' does not exist!");
            }
            return;
        }

        UUID uuid = player.getUniqueId();
        preJailLocations.put(uuid, player.getLocation());
        
        player.teleport(jail);
        player.sendMessage("§cYou have been jailed" + (durationSeconds > 0 ? " for " + durationSeconds + " seconds" : "") + "!");
        if (reason != null && !reason.isEmpty()) {
            player.sendMessage("§cReason: " + reason);
        }

        String path = "jailed." + uuid.toString();
        jailConfig.set(path + ".jail", jailName);
        jailConfig.set(path + ".reason", reason);
        jailConfig.set(path + ".staff", staff != null ? staff.getName() : "Console");
        jailConfig.set(path + ".start-time", System.currentTimeMillis());
        jailConfig.set(path + ".duration", durationSeconds);
        saveJails();

        if (staff != null) {
            staff.sendMessage("§aJailed " + player.getName() + " in " + jailName);
        }

        plugin.getDiscordManager().sendStaffLogNotification(
            "punishment-log",
            staff != null ? staff.getName() : "Console",
            "jailed",
            player.getName(),
            reason != null ? reason : ""
        );

        if (durationSeconds > 0) {
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                unjailPlayer(player);
            }, durationSeconds * 20L);
            jailTasks.put(uuid, task);
        }
    }

    public void unjailPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        Location preJail = preJailLocations.remove(uuid);
        
        if (preJail != null) {
            player.teleport(preJail);
        }
        
        player.sendMessage("§aYou have been unjailed!");

        jailConfig.set("jailed." + uuid.toString(), null);
        saveJails();

        BukkitTask task = jailTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    public boolean isJailed(Player player) {
        return jailConfig.contains("jailed." + player.getUniqueId().toString());
    }

    public String getJailInfo(Player player) {
        String path = "jailed." + player.getUniqueId().toString();
        if (!jailConfig.contains(path)) {
            return null;
        }
        return jailConfig.getString(path + ".jail");
    }

    public long getJailTimeRemaining(Player player) {
        String path = "jailed." + player.getUniqueId().toString();
        if (!jailConfig.contains(path)) {
            return 0;
        }
        long startTime = jailConfig.getLong(path + ".start-time");
        long duration = jailConfig.getLong(path + ".duration");
        if (duration <= 0) {
            return -1; // Permanent
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        return Math.max(0, duration - elapsed);
    }

    private void saveJails() {
        try {
            jailConfig.save(jailFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save jails.yml: " + e.getMessage());
        }
    }
}

