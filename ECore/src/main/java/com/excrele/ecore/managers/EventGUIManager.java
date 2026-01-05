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

public class EventGUIManager implements Listener {
    private final Ecore plugin;
    
    public EventGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openEventGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 54, ChatColor.GOLD + "Events");
        
        int slot = 0;
        for (EventManager.Event event : plugin.getEventManager().getActiveEvents()) {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + event.getDescription());
            lore.add(ChatColor.GRAY + "Type: " + ChatColor.WHITE + event.getType());
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to join!");
            
            ItemStack eventItem = createGuiItem(Material.BEACON, ChatColor.GOLD + event.getName(), lore);
            gui.setItem(slot++, eventItem);
        }
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (event.getView().getTitle().equals(ChatColor.GOLD + "Events")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;
            
            // Find event by name
            String eventName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            for (EventManager.Event eventObj : plugin.getEventManager().getActiveEvents()) {
                if (eventObj.getName().equals(eventName)) {
                    if (plugin.getEventManager().joinEvent(player, eventObj.getId())) {
                        player.closeInventory();
                    }
                    break;
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

