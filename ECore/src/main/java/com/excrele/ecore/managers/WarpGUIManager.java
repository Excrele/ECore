package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class WarpGUIManager implements Listener {
    private final Ecore plugin;

    public WarpGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openWarpGUI(Player player) {
        List<String> warps = plugin.getWarpManager().getPublicWarps();
        int size = Math.max(9, ((warps.size() + 8) / 9) * 9);
        size = Math.min(size, 54);
        
        Inventory gui = Bukkit.createInventory(player, size, ChatColor.DARK_PURPLE + "Warps (" + warps.size() + ")");

        for (int i = 0; i < warps.size() && i < size; i++) {
            String warpName = warps.get(i);
            ItemStack warpItem = new ItemStack(Material.ENDER_PEARL);
            ItemMeta meta = warpItem.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + warpName);
            meta.setLore(List.of(
                ChatColor.GRAY + "Click to teleport",
                ChatColor.GRAY + "Right-click for info"
            ));
            warpItem.setItemMeta(meta);
            gui.setItem(i, warpItem);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        if (!title.startsWith(ChatColor.DARK_PURPLE + "Warps")) return;
        
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.ENDER_PEARL) {
            String warpName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            
            if (event.isRightClick()) {
                // Show warp info
                player.sendMessage(ChatColor.GOLD + "=== Warp Info ===");
                player.sendMessage(ChatColor.YELLOW + "Name: " + ChatColor.WHITE + warpName);
                // Could add more info like location, creator, etc.
            } else {
                // Teleport to warp
                player.closeInventory();
                plugin.getWarpManager().teleportToWarp(player, warpName);
            }
        }
    }
}

