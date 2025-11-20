package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    private final Ecore plugin;

    public PlayerMoveListener(Ecore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Check if frozen
        if (plugin.getStaffManager().isFrozen(player)) {
            // Only cancel if they actually moved (not just looked around)
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                event.setCancelled(true);
                player.sendMessage("§cYou are frozen! You cannot move.");
            }
        }
    }
}

