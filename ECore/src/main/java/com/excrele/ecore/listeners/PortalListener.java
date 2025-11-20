package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.PortalManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener for portal teleportation.
 * Detects when players enter custom portals and teleports them seamlessly.
 * 
 * @author Excrele
 * @version 1.0
 */
public class PortalListener implements Listener {
    private final Ecore plugin;
    private final Map<UUID, Long> portalCooldowns; // Player UUID -> Last teleport time
    private static final long PORTAL_COOLDOWN_MS = 1000; // 1 second cooldown to prevent spam

    public PortalListener(Ecore plugin) {
        this.plugin = plugin;
        this.portalCooldowns = new HashMap<>();
    }

    /**
     * Handles player movement to detect portal entry.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if player actually moved to a different block
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }

        Player player = event.getPlayer();
        Location to = event.getTo();
        
        // Check if player is in a portal block
        PortalManager.Portal portal = plugin.getPortalManager().getPortalByBlock(to);
        if (portal == null) {
            return;
        }

        // Check cooldown to prevent spam teleportation
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Long lastTeleport = portalCooldowns.get(uuid);
        
        if (lastTeleport != null && (currentTime - lastTeleport) < PORTAL_COOLDOWN_MS) {
            return; // Still on cooldown
        }

        // Teleport through portal
        portalCooldowns.put(uuid, currentTime);
        plugin.getPortalManager().teleportThroughPortal(player, portal);
    }

    /**
     * Handles vanilla portal events to optionally redirect them.
     * This allows custom portals to work alongside vanilla portals.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        // This event is called when a player uses a vanilla portal
        // We can optionally intercept it if we want custom portal behavior
        // For now, we'll let vanilla portals work normally
    }

    /**
     * Cleans up cooldown data when player quits.
     */
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        portalCooldowns.remove(event.getPlayer().getUniqueId());
    }
}

