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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

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

    /**
     * Handles inspector wand interactions (right-clicking blocks).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getItem() == null) return;
        
        ItemStack item = event.getItem();
        if (item.getType() != org.bukkit.Material.WOODEN_AXE) return;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        
        if (!meta.getDisplayName().equals(ChatColor.GOLD + "Block Inspector")) return;
        
        event.setCancelled(true);
        
        Player player = event.getPlayer();
        if (!player.hasPermission("ecore.blocklog.inspect")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use the inspector tool!");
            return;
        }
        
        org.bukkit.block.Block block = event.getClickedBlock();
        org.bukkit.Location location = block.getLocation();
        
        // Get block logs for this location
        long timeRange = 7L * 24L * 60L * 60L * 1000L; // 7 days
        java.util.List<com.excrele.ecore.database.BlockLogDatabase.BlockLogEntry> logs = 
                plugin.getBlockLogManager().getBlockLogs(location, timeRange);
        
        if (logs.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No block logs found for this location.");
            return;
        }
        
        // Show block history
        player.sendMessage(ChatColor.GOLD + "=== Block History ===");
        player.sendMessage(ChatColor.GRAY + "Location: " + ChatColor.WHITE + 
                location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        player.sendMessage(ChatColor.GRAY + "World: " + ChatColor.WHITE + location.getWorld().getName());
        player.sendMessage(ChatColor.GRAY + "Found " + logs.size() + " log entries:");
        
        int count = 0;
        for (com.excrele.ecore.database.BlockLogDatabase.BlockLogEntry log : logs) {
            if (count >= 10) { // Limit to 10 most recent
                player.sendMessage(ChatColor.GRAY + "... and " + (logs.size() - 10) + " more entries");
                break;
            }
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String actionColor = log.action.equals("BREAK") ? ChatColor.RED.toString() : ChatColor.GREEN.toString();
            player.sendMessage(ChatColor.GRAY + "- " + actionColor + log.action + ChatColor.GRAY + " by " + 
                    ChatColor.WHITE + log.playerName + ChatColor.GRAY + " at " + 
                    ChatColor.WHITE + sdf.format(new java.util.Date(log.time)));
            if (log.material != null) {
                player.sendMessage(ChatColor.GRAY + "  Material: " + ChatColor.WHITE + log.material);
            }
            count++;
        }
        
        // Store selection for restore command
        plugin.getBlockLogManager().setInspectorSelection(player, location);
    }
}

