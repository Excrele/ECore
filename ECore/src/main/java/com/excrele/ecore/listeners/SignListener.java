package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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

        if (lines[0].equalsIgnoreCase("[Admin Shop]") && player.hasPermission("ecore.adminshop")) {
            event.setLine(0, ChatColor.GREEN + "[Admin Shop]");
            player.sendMessage(ChatColor.GREEN + "Admin Shop sign created! Right-click to set up.");
        } else if (lines[0].equalsIgnoreCase("[PShop]") && player.hasPermission("ecore.pshop")) {
            Block block = event.getBlock();
            Block attached = getAttachedChest(block);
            if (attached != null && attached.getState() instanceof Chest) {
                event.setLine(0, ChatColor.BLUE + "[PShop]");
                player.sendMessage(ChatColor.GREEN + "Player Shop sign created! Right-click to set up.");
            } else {
                player.sendMessage(ChatColor.RED + "Player Shop sign must be placed on a chest!");
                event.setCancelled(true);
            }
        }
    }

    // Handle sign interaction
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign)) return;

        Player player = event.getPlayer();
        Sign sign = (Sign) block.getState();
        String[] lines = sign.getLines();

        if (lines[0].equals(ChatColor.GREEN + "[Admin Shop]")) {
            if (player.hasPermission("ecore.adminshop")) {
                plugin.getShopManager().handleAdminShopItem(player, player.getInventory().getItemInMainHand());
                event.setCancelled(true);
            }
        } else if (lines[0].equals(ChatColor.BLUE + "[PShop]")) {
            if (player.hasPermission("ecore.pshop")) {
                Block attached = getAttachedChest(block);
                if (attached != null && attached.getState() instanceof Chest) {
                    plugin.getShopManager().startPlayerShopCreation(player, sign, (Chest) attached.getState());
                    event.setCancelled(true);
                }
            }
        }
    }

    // Get attached chest
    private Block getAttachedChest(Block signBlock) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
        for (BlockFace face : faces) {
            Block relative = signBlock.getRelative(face);
            if (relative.getType() == Material.CHEST || relative.getType() == Material.TRAPPED_CHEST) {
                return relative;
            }
        }
        return null;
    }
}