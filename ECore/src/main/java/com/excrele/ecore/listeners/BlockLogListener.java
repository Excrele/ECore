package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Listens for block and inventory events to log them.
 */
public class BlockLogListener implements Listener {
    private final Ecore plugin;

    public BlockLogListener(Ecore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getBlockLogManager().isLoggingEnabled()) return;
        plugin.getBlockLogManager().logBlockBreak(event.getPlayer(), event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getBlockLogManager().isLoggingEnabled()) return;
        plugin.getBlockLogManager().logBlockPlace(event.getPlayer(), event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (!plugin.getBlockLogManager().isLoggingEnabled()) return;
        
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        
        // Log container access
        if (inventory.getHolder() instanceof BlockState) {
            BlockState state = (BlockState) inventory.getHolder();
            if (state instanceof Container) {
                plugin.getBlockLogManager().logContainerInteraction(player, state.getLocation(), 
                        "OPEN", -1, null);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!plugin.getBlockLogManager().isLoggingEnabled()) return;
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        int slot = event.getSlot();
        
        // Log container interactions
        if (inventory.getHolder() instanceof BlockState) {
            BlockState state = (BlockState) inventory.getHolder();
            if (state instanceof Container) {
                ItemStack item = event.getCurrentItem();
                String action = event.getClick().isShiftClick() ? "SHIFT_CLICK" : "CLICK";
                
                plugin.getBlockLogManager().logContainerInteraction(player, state.getLocation(), 
                        action, slot, item);
            }
        }
        
        // Log inventory changes (player inventory)
        if (inventory.getType() == InventoryType.PLAYER || 
            event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
            ItemStack item = event.getCurrentItem();
            String action = event.getClick().isShiftClick() ? "SHIFT_CLICK" : "CLICK";
            
            plugin.getInventoryLogManager().logInventoryAction(player, action, slot, item, "PLAYER");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (!plugin.getBlockLogManager().isLoggingEnabled()) return;
        
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        
        // Log container close
        if (inventory.getHolder() instanceof BlockState) {
            BlockState state = (BlockState) inventory.getHolder();
            if (state instanceof Container) {
                plugin.getBlockLogManager().logContainerInteraction(player, state.getLocation(), 
                        "CLOSE", -1, null);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!plugin.getBlockLogManager().isLoggingEnabled()) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        plugin.getInventoryLogManager().logInventoryAction(player, "DROP", -1, item, "PLAYER");
    }

    // Note: PlayerPickupItemEvent is deprecated in newer versions
    // Using EntityPickupItemEvent instead for 1.21+
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityPickupItem(org.bukkit.event.entity.EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!plugin.getBlockLogManager().isLoggingEnabled()) return;
        
        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();
        plugin.getInventoryLogManager().logInventoryAction(player, "PICKUP", -1, item, "PLAYER");
    }
}

