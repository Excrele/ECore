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

import java.util.Arrays;
import java.util.List;

public class DimensionGUIManager implements Listener {
    private final Ecore plugin;
    
    public DimensionGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openDimensionGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, ChatColor.DARK_PURPLE + "Dimensions");
        
        DimensionManager.Dimension playerDim = plugin.getDimensionManager().getPlayerDimension(player.getUniqueId());
        
        if (playerDim == null) {
            ItemStack create = createGuiItem(Material.EMERALD, ChatColor.GREEN + "Create Dimension", 
                Arrays.asList(ChatColor.GRAY + "Create your personal dimension"));
            gui.setItem(13, create);
        } else {
            ItemStack info = createGuiItem(Material.BOOK, ChatColor.GOLD + "Your Dimension", Arrays.asList(
                ChatColor.GRAY + "World: " + ChatColor.WHITE + playerDim.getWorldName(),
                ChatColor.GRAY + "Size: " + ChatColor.WHITE + playerDim.getSize()
            ));
            gui.setItem(4, info);
            
            ItemStack teleport = createGuiItem(Material.ENDER_PEARL, ChatColor.AQUA + "Teleport", 
                Arrays.asList(ChatColor.GRAY + "Teleport to your dimension"));
            gui.setItem(10, teleport);
        }
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "Dimensions")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;
            
            if (clicked.getType() == Material.EMERALD) {
                if (plugin.getDimensionManager().createPlayerDimension(player) != null) {
                    player.closeInventory();
                }
            } else if (clicked.getType() == Material.ENDER_PEARL) {
                DimensionManager.Dimension dim = plugin.getDimensionManager().getPlayerDimension(player.getUniqueId());
                if (dim != null) {
                    plugin.getDimensionManager().teleportToDimension(player, dim.getId());
                    player.closeInventory();
                }
            }
        }
    }
    
    private ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}

