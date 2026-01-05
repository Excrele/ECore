package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for staff mode events.
 * Prevents staff members in staff mode from performing certain actions
 * like breaking/placing blocks, picking up items, dropping items, etc.
 */
public class StaffModeListener implements Listener {
    private final Ecore plugin;

    public StaffModeListener(Ecore plugin) {
        this.plugin = plugin;
    }

    /**
     * Prevents block breaking in staff mode (unless allowed in config).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getStaffModeManager().isInStaffMode(player)) {
            return;
        }

        var config = plugin.getConfigManager().getConfig();
        if (!config.getBoolean("staffmode.allow-block-break", false)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot break blocks while in staff mode!");
        }
    }

    /**
     * Prevents block placing in staff mode (unless allowed in config).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getStaffModeManager().isInStaffMode(player)) {
            return;
        }

        var config = plugin.getConfigManager().getConfig();
        if (!config.getBoolean("staffmode.allow-block-place", false)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot place blocks while in staff mode!");
        }
    }

    /**
     * Prevents item pickup in staff mode (unless allowed in config).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (!plugin.getStaffModeManager().isInStaffMode(player)) {
            return;
        }

        var config = plugin.getConfigManager().getConfig();
        if (!config.getBoolean("staffmode.allow-item-pickup", false)) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents item drop in staff mode (unless allowed in config).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getStaffModeManager().isInStaffMode(player)) {
            return;
        }

        var config = plugin.getConfigManager().getConfig();
        if (!config.getBoolean("staffmode.allow-item-drop", false)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot drop items while in staff mode!");
        }
    }

    /**
     * Prevents damage in staff mode (invincibility).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (!plugin.getStaffModeManager().isInStaffMode(player)) {
            return;
        }

        var config = plugin.getConfigManager().getConfig();
        if (config.getBoolean("staffmode.invincible", true)) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles staff mode item clicks.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (!plugin.getStaffModeManager().isInStaffMode(player)) {
            return;
        }

        // Prevent moving items in own inventory (unless allowed)
        var config = plugin.getConfigManager().getConfig();
        if (event.getInventory().getType() == InventoryType.PLAYER) {
            if (!config.getBoolean("staffmode.allow-inventory-edit", false)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles staff mode item interactions (right-click with items).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getStaffModeManager().isInStaffMode(player)) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        Material material = item.getType();
        String displayName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() 
            ? item.getItemMeta().getDisplayName() 
            : "";

        // Handle staff mode tool interactions
        if (material == Material.COMPASS) {
            // Teleport tool - handled by StaffManager
            event.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW + "Right-click a player to teleport to them, or use /tp");
            return;
        } else if (material == Material.BOOK) {
            // View reports
            event.setCancelled(true);
            plugin.getStaffManager().openReportsGUI(player);
            return;
        } else if (material == Material.CHEST) {
            // Inspect inventory
            event.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW + "Right-click a player to inspect their inventory, or use the staff GUI");
            return;
        } else if (material == Material.REDSTONE_BLOCK) {
            // Ban player
            event.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW + "Use /ban or the staff GUI to ban players");
            return;
        } else if (material == Material.IRON_BOOTS) {
            // Kick player
            event.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW + "Use /kick or the staff GUI to kick players");
            return;
        } else if (material == Material.BARRIER && displayName.contains("Exit Staff Mode")) {
            // Exit staff mode
            event.setCancelled(true);
            plugin.getStaffModeManager().exitStaffMode(player);
            return;
        }
    }

    /**
     * Handles player quit - ensures staff mode is cleaned up.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getStaffModeManager().handlePlayerQuit(player);
    }

    /**
     * Prevents interaction with blocks/entities in staff mode (unless allowed).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getStaffModeManager().isInStaffMode(player)) {
            return;
        }

        // Allow interaction with players for teleport/inspect
        if (event.getRightClicked() instanceof Player) {
            Player target = (Player) event.getRightClicked();
            ItemStack item = player.getInventory().getItemInMainHand();
            
            if (item != null) {
                Material material = item.getType();
                if (material == Material.COMPASS) {
                    // Teleport to player
                    event.setCancelled(true);
                    plugin.getStaffManager().teleportToPlayer(player, target.getName());
                } else if (material == Material.CHEST) {
                    // Inspect inventory
                    event.setCancelled(true);
                    plugin.getStaffManager().openPlayerInventory(player, target.getName());
                }
            }
            return;
        }

        var config = plugin.getConfigManager().getConfig();
        if (!config.getBoolean("staffmode.allow-entity-interact", false)) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents interaction with blocks in staff mode (unless allowed).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractBlock(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        // Don't process if event was already cancelled by staff tool handler
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        if (!plugin.getStaffModeManager().isInStaffMode(player)) {
            return;
        }

        // Allow interaction with certain blocks (chests, etc.) if configured
        var config = plugin.getConfigManager().getConfig();
        if (!config.getBoolean("staffmode.allow-block-interact", false)) {
            // Allow opening containers for inspection
            Material material = event.getClickedBlock().getType();
            if (material == Material.CHEST || material == Material.TRAPPED_CHEST ||
                material == Material.BARREL || material == Material.SHULKER_BOX) {
                // Allow container inspection
                return;
            }
            event.setCancelled(true);
        }
    }
}


