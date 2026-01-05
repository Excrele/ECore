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

public class MarriageGUIManager implements Listener {
    private final Ecore plugin;
    
    public MarriageGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openMarriageGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, ChatColor.LIGHT_PURPLE + "Marriage");
        
        if (plugin.getMarriageManager().isMarried(player.getUniqueId())) {
            // Married - show marriage info
            java.util.UUID spouseUuid = plugin.getMarriageManager().getSpouse(player.getUniqueId());
            String spouseName = spouseUuid != null ? Bukkit.getOfflinePlayer(spouseUuid).getName() : "Unknown";
            MarriageManager.MarriageStats stats = plugin.getMarriageManager().getMarriageStats(player.getUniqueId());
            
            ItemStack info = createGuiItem(Material.BOOK, ChatColor.GOLD + "Marriage Info", Arrays.asList(
                ChatColor.GRAY + "Spouse: " + ChatColor.WHITE + spouseName,
                ChatColor.GRAY + "Days Married: " + ChatColor.WHITE + (stats != null ? stats.getDaysMarried() : 0),
                ChatColor.GRAY + "Married: " + ChatColor.WHITE + (stats != null ? new java.util.Date(stats.getMarriedAt()).toString() : "Unknown")
            ));
            gui.setItem(4, info);
            
            ItemStack teleport = createGuiItem(Material.ENDER_PEARL, ChatColor.AQUA + "Teleport to Spouse", 
                Arrays.asList(ChatColor.GRAY + "Teleport to your spouse"));
            gui.setItem(10, teleport);
            
            ItemStack chat = createGuiItem(Material.PAPER, ChatColor.YELLOW + "Marriage Chat", 
                Arrays.asList(ChatColor.GRAY + "Send a message to your spouse"));
            gui.setItem(12, chat);
            
            ItemStack divorce = createGuiItem(Material.BARRIER, ChatColor.RED + "Divorce", 
                Arrays.asList(ChatColor.GRAY + "End your marriage"));
            gui.setItem(16, divorce);
        } else {
            // Not married - show proposal options
            ItemStack propose = createGuiItem(Material.DIAMOND, ChatColor.GREEN + "Propose", 
                Arrays.asList(ChatColor.GRAY + "Propose to another player"));
            gui.setItem(13, propose);
        }
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (event.getView().getTitle().equals(ChatColor.LIGHT_PURPLE + "Marriage")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;
            
            if (clicked.getType() == Material.ENDER_PEARL) {
                if (plugin.getMarriageManager().teleportToSpouse(player)) {
                    player.closeInventory();
                }
            } else if (clicked.getType() == Material.BARRIER) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Type /marry divorce to confirm divorce.");
            } else if (clicked.getType() == Material.DIAMOND) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Use /marry propose <player> to propose!");
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

