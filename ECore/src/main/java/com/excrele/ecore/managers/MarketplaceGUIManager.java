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

public class MarketplaceGUIManager implements Listener {
    private final Ecore plugin;
    
    public MarketplaceGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openMarketplaceGUI(Player player, int page) {
        List<MarketplaceManager.MarketplaceListing> allListings = 
            new ArrayList<>(plugin.getMarketplaceManager().getListings());
        int entriesPerPage = 45;
        int totalPages = (int) Math.ceil((double) allListings.size() / entriesPerPage);
        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;
        
        Inventory gui = Bukkit.createInventory(player, 54, ChatColor.DARK_GREEN + "Marketplace (Page " + page + "/" + Math.max(1, totalPages) + ")");
        
        int start = (page - 1) * entriesPerPage;
        int end = Math.min(start + entriesPerPage, allListings.size());
        
        for (int i = start; i < end; i++) {
            MarketplaceManager.MarketplaceListing listing = allListings.get(i);
            String sellerName = Bukkit.getOfflinePlayer(listing.getSeller()).getName();
            double rating = plugin.getMarketplaceManager().getSellerRating(listing.getSeller());
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Seller: " + ChatColor.WHITE + sellerName);
            lore.add(ChatColor.GRAY + "Category: " + ChatColor.WHITE + listing.getCategory());
            lore.add(ChatColor.GRAY + "Price: " + ChatColor.GREEN + plugin.getEconomyManager().format(listing.getPrice()));
            lore.add(ChatColor.GRAY + "Rating: " + ChatColor.YELLOW + String.format("%.1f", rating) + "/5.0");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to purchase");
            
            ItemStack listingItem = createGuiItem(Material.PAPER, ChatColor.WHITE + listing.getTitle(), lore);
            gui.setItem(i - start, listingItem);
        }
        
        // Navigation
        if (page > 1) {
            ItemStack prev = createGuiItem(Material.ARROW, ChatColor.GREEN + "Previous Page", null);
            gui.setItem(45, prev);
        }
        if (page < totalPages) {
            ItemStack next = createGuiItem(Material.ARROW, ChatColor.GREEN + "Next Page", null);
            gui.setItem(53, next);
        }
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.startsWith(ChatColor.DARK_GREEN + "Marketplace (")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;
            
            if (clicked.getType() == Material.ARROW) {
                String[] parts = title.split(" ");
                int currentPage = Integer.parseInt(parts[1].replace("(", ""));
                if (clicked.getItemMeta().getDisplayName().contains("Previous")) {
                    openMarketplaceGUI(player, currentPage - 1);
                } else {
                    openMarketplaceGUI(player, currentPage + 1);
                }
            } else if (clicked.getType() == Material.PAPER) {
                player.sendMessage(ChatColor.YELLOW + "Purchase functionality coming soon!");
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

