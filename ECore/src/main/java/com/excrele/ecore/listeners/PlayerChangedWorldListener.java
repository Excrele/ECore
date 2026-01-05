package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 * Listener for player world changes.
 * Updates scoreboard and tab list when players change worlds to support per-world configurations.
 */
public class PlayerChangedWorldListener implements Listener {
    private final Ecore plugin;

    public PlayerChangedWorldListener(Ecore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        // Update scoreboard if player has one active
        if (plugin.getScoreboardManager() != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Only update if player has scoreboard enabled
                if (plugin.getScoreboardManager().hasScoreboard(event.getPlayer())) {
                    plugin.getScoreboardManager().setupScoreboard(event.getPlayer());
                }
            }, 5L); // Small delay to ensure world change is complete
        }
        
        // Update tab list
        if (plugin.getTabListManager() != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getTabListManager().setupTabList(event.getPlayer());
            }, 5L); // Small delay to ensure world change is complete
        }
    }
}

