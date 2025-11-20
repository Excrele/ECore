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

import java.util.ArrayList;
import java.util.List;

public class KitGUIManager implements Listener {
    private final Ecore plugin;

    public KitGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openKitGUI(Player player) {
        List<String> kits = plugin.getKitManager().getKits();
        
        // Filter kits by permission
        List<String> availableKits = new ArrayList<>();
        for (String kit : kits) {
            if (player.hasPermission("ecore.kit." + kit.toLowerCase()) || 
                player.hasPermission("ecore.kit.*")) {
                availableKits.add(kit);
            }
        }
        
        int size = Math.max(9, ((availableKits.size() + 8) / 9) * 9);
        size = Math.min(size, 54);
        
        Inventory gui = Bukkit.createInventory(player, size, ChatColor.DARK_GREEN + "Available Kits");

        for (int i = 0; i < availableKits.size() && i < size; i++) {
            String kitName = availableKits.get(i);
            ItemStack kitItem = new ItemStack(Material.CHEST);
            ItemMeta meta = kitItem.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + kitName);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to receive this kit");
            
            // Add cost info if applicable
            double cost = plugin.getKitManager().getKitCost(kitName);
            if (cost > 0) {
                lore.add(ChatColor.YELLOW + "Cost: " + ChatColor.WHITE + String.format("%.2f", cost));
            }
            
            // Add cooldown info if applicable
            int cooldown = plugin.getKitManager().getKitCooldown(kitName);
            if (cooldown > 0) {
                lore.add(ChatColor.YELLOW + "Cooldown: " + ChatColor.WHITE + cooldown + " seconds");
            }
            
            meta.setLore(lore);
            kitItem.setItemMeta(meta);
            gui.setItem(i, kitItem);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        if (!title.equals(ChatColor.DARK_GREEN + "Available Kits")) return;
        
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.CHEST) {
            String kitName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            player.closeInventory();
            plugin.getKitManager().giveKit(player, kitName);
        }
    }
}

