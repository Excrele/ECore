package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Listener for WorldEdit wand interactions.
 * Handles left-click (pos1) and right-click (pos2) when using the selection wand.
 * 
 * @author Excrele
 * @version 1.0
 */
public class WorldEditListener implements Listener {
    private final Ecore plugin;
    
    public WorldEditListener(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.WOODEN_AXE) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        
        String displayName = meta.getDisplayName();
        if (!displayName.equals(ChatColor.GOLD + "Selection Wand")) {
            return;
        }
        
        // Check permission
        if (!event.getPlayer().hasPermission("ecore.worldedit.use")) {
            return;
        }
        
        event.setCancelled(true);
        
        if (event.getClickedBlock() == null) {
            return;
        }
        
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // Set position 1
            plugin.getWorldEditManager().setPos1(event.getPlayer(), event.getClickedBlock().getLocation());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Set position 2
            plugin.getWorldEditManager().setPos2(event.getPlayer(), event.getClickedBlock().getLocation());
        }
    }
}

