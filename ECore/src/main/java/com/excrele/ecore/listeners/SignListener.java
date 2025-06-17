package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SignListener implements Listener {
    private final Ecore plugin;

    public SignListener(Ecore plugin) {
        this.plugin = plugin;
    }

    // Handle sign creation
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();
        if (lines[0].equalsIgnoreCase("[Admin Shop]")) {
            if (!player.hasPermission("ecore.adminshop")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to create Admin Shops!");
                event.setCancelled(true);
                return;
            }
            event.setLine(0, ChatColor.DARK_GREEN + "[Admin Shop]");
            plugin.getShopManager().startAdminShopCreation(player, event.getBlock().getLocation());
        } else if (lines[0].equalsIgnoreCase("[PShop]")) {
            if (!player.hasPermission("ecore.pshop")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to create Player Shops!");
                event.setCancelled(true);
                return;
            }
            Block attached = getAttachedBlock(event.getBlock());
            if (attached == null || attached.getType() != Material.CHEST) {
                player.sendMessage(ChatColor.RED + "Player Shop sign must be placed on a chest!");
                event.setCancelled(true);
                return;
            }
            event.setLine(0, ChatColor.DARK_BLUE + "[PShop]");
            plugin.getShopManager().startPlayerShopCreation(player, attached.getLocation(), event.getBlock().getLocation());
        }
    }

    // Handle sign interaction
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign)) return;
        Sign sign = (Sign) block.getState();
        Player player = event.getPlayer();
        boolean isBuy = event.getAction() == Action.RIGHT_CLICK_BLOCK;

        if (sign.getLine(0).equals(ChatColor.DARK_GREEN + "[Admin Shop]")) {
            event.setCancelled(true);
            if (isBuy && player.getItemInHand().getType() != Material.AIR) {
                plugin.getShopManager().handleAdminShopItem(player, player.getItemInHand());
            } else {
                plugin.getShopManager().handleAdminShopInteraction(player, block.getLocation(), isBuy);
            }
        } else if (sign.getLine(0).equals(ChatColor.DARK_BLUE + "[PShop]")) {
            event.setCancelled(true);
            plugin.getShopManager().handlePlayerShopInteraction(player, block.getLocation(), isBuy);
        }
    }

    // Get block the sign is attached to
    private Block getAttachedBlock(Block signBlock) {
        if (!(signBlock.getState() instanceof Sign)) return null;
        org.bukkit.block.data.type.WallSign signData = (org.bukkit.block.data.type.WallSign) signBlock.getBlockData();
        BlockFace attachedFace = signData.getFacing().getOppositeFace();
        return signBlock.getRelative(attachedFace);
    }
}