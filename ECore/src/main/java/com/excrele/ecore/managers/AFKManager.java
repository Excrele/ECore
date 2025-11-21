package com.excrele.ecore.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import com.excrele.ecore.Ecore;

public class AFKManager implements Listener {
    private final Ecore plugin;
    private final Map<UUID, Long> lastActivity;
    private final Map<UUID, BukkitTask> afkCheckTasks;
    private final Map<UUID, Boolean> afkStatus;

    public AFKManager(Ecore plugin) {
        this.plugin = plugin;
        this.lastActivity = new HashMap<>();
        this.afkCheckTasks = new HashMap<>();
        this.afkStatus = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startAFKChecks();
    }

    private void startAFKChecks() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long afkTime = plugin.getConfig().getLong("afk.auto-afk-time", 300) * 20; // Default 5 minutes
            long currentTime = System.currentTimeMillis();

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                Long lastActive = lastActivity.get(uuid);
                
                if (lastActive != null && (currentTime - lastActive) > afkTime && !isAFK(player)) {
                    setAFK(player, true, false);
                }
            }
        }, 0L, 100L); // Check every 5 seconds
    }

    public void updateActivity(Player player) {
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
        if (isAFK(player)) {
            setAFK(player, false, false);
        }
    }

    public void setAFK(Player player, boolean afk, boolean manual) {
        UUID uuid = player.getUniqueId();
        boolean wasAFK = isAFK(player);
        afkStatus.put(uuid, afk);

        if (afk && !wasAFK) {
            // Player went AFK
            if (manual) {
                Bukkit.broadcastMessage("§7" + player.getName() + " is now AFK");
            } else {
                Bukkit.broadcastMessage("§7" + player.getName() + " is now AFK (inactive)");
            }
            lastActivity.put(uuid, System.currentTimeMillis());
        } else if (!afk && wasAFK) {
            // Player returned
            Bukkit.broadcastMessage("§7" + player.getName() + " is no longer AFK");
            lastActivity.put(uuid, System.currentTimeMillis());
        }
    }

    public boolean isAFK(Player player) {
        return afkStatus.getOrDefault(player.getUniqueId(), false);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to != null) {
            Location from = event.getFrom();
            if (from.getBlockX() != to.getBlockX() ||
                from.getBlockY() != to.getBlockY() ||
                from.getBlockZ() != to.getBlockZ()) {
                updateActivity(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastActivity.remove(uuid);
        afkStatus.remove(uuid);
        BukkitTask task = afkCheckTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }
}

