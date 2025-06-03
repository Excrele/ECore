package com.excrele.ecore.listeners;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class SitListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getPlayer().hasPermission("ecore.sit")) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (!event.getClickedBlock().getType().name().endsWith("_STAIRS")) return;

        ArmorStand seat = (ArmorStand) event.getClickedBlock().getWorld().spawnEntity(
                event.getClickedBlock().getLocation().add(0.5, 0.3, 0.5),
                EntityType.ARMOR_STAND
        );
        seat.setGravity(false);
        seat.setVisible(false);
        seat.addPassenger(event.getPlayer());
    }
}