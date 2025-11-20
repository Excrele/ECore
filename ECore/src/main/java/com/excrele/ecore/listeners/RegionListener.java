package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.RegionFlag;
import com.excrele.ecore.managers.RegionManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 * Listens to events and enforces region protection rules.
 * 
 * @author Excrele
 * @version 1.0
 */
public class RegionListener implements Listener {
    private final Ecore plugin;

    public RegionListener(Ecore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        RegionManager regionManager = plugin.getRegionManager();

        if (!regionManager.canPerformAction(player, event.getBlock().getLocation(), RegionFlag.BREAK)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot break blocks in this region!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        RegionManager regionManager = plugin.getRegionManager();

        if (!regionManager.canPerformAction(player, event.getBlock().getLocation(), RegionFlag.BUILD)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot build in this region!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();
        RegionManager regionManager = plugin.getRegionManager();

        // Check for block interaction (doors, chests, etc.)
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (!regionManager.canPerformAction(player, event.getClickedBlock().getLocation(), RegionFlag.INTERACT)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot interact with blocks in this region!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();
        RegionManager regionManager = plugin.getRegionManager();

        // Check PvP flag at victim's location
        if (!regionManager.canPerformAction(attacker, victim.getLocation(), RegionFlag.PVP)) {
            event.setCancelled(true);
            attacker.sendMessage(ChatColor.RED + "PvP is disabled in this region!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        RegionManager regionManager = plugin.getRegionManager();

        if (!regionManager.canPerformAction(player, player.getLocation(), RegionFlag.DAMAGE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        RegionManager regionManager = plugin.getRegionManager();

        if (!regionManager.canPerformAction(player, player.getLocation(), RegionFlag.DROP_ITEMS)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot drop items in this region!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPickupItem(org.bukkit.event.entity.EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        RegionManager regionManager = plugin.getRegionManager();

        if (!regionManager.canPerformAction(player, event.getItem().getLocation(), RegionFlag.PICKUP_ITEMS)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        RegionManager regionManager = plugin.getRegionManager();

        // Check entry flag for destination
        if (!regionManager.canPerformAction(player, event.getTo(), RegionFlag.ENTRY)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot enter this region!");
            return;
        }

        // Check exit flag for origin
        if (!regionManager.canPerformAction(player, event.getFrom(), RegionFlag.EXIT)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot exit this region!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        RegionManager regionManager = plugin.getRegionManager();

        if (!regionManager.canPerformAction(player, player.getLocation(), RegionFlag.COMMANDS)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot use commands in this region!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        RegionManager regionManager = plugin.getRegionManager();

        if (!regionManager.canPerformAction(player, player.getLocation(), RegionFlag.CHAT)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot chat in this region!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        RegionManager regionManager = plugin.getRegionManager();

        if (event.getInventory().getLocation() != null) {
            if (!regionManager.canPerformAction(player, event.getInventory().getLocation(), RegionFlag.INTERACT)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot open this inventory in this region!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        RegionManager regionManager = plugin.getRegionManager();

        if (!regionManager.canPerformAction(player, event.getRightClicked().getLocation(), RegionFlag.INTERACT)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot interact with entities in this region!");
        }
    }
}

