package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
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

public class ClaimGUIManager implements Listener {
    private final Ecore plugin;
    
    public ClaimGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openClaimGUI(Player player) {
        if (!plugin.getClaimManager().isEnabled()) {
            player.sendMessage(ChatColor.RED + "The claim system is disabled!");
            return;
        }
        
        Chunk chunk = player.getLocation().getChunk();
        ClaimManager.Claim claim = plugin.getClaimManager().getClaim(chunk);
        
        Inventory gui = Bukkit.createInventory(player, 27, ChatColor.DARK_GREEN + "Claim Management");
        
        if (claim == null) {
            // Not claimed - show claim option
            ItemStack claimItem = createGuiItem(Material.GOLDEN_SHOVEL, ChatColor.GREEN + "Claim This Chunk", 
                Arrays.asList(ChatColor.GRAY + "Click to claim this chunk"));
            gui.setItem(13, claimItem);
        } else {
            // Claimed - show claim info and options
            ItemStack info = createGuiItem(Material.BOOK, ChatColor.GOLD + "Claim Info", Arrays.asList(
                ChatColor.GRAY + "Owner: " + ChatColor.WHITE + Bukkit.getOfflinePlayer(claim.getOwner()).getName(),
                ChatColor.GRAY + "Chunk: " + ChatColor.WHITE + claim.getChunkX() + ", " + claim.getChunkZ(),
                ChatColor.GRAY + "Members: " + ChatColor.WHITE + claim.getMembers().size()
            ));
            gui.setItem(4, info);
            
            if (claim.getOwner().equals(player.getUniqueId()) || player.hasPermission("ecore.claim.admin")) {
                ItemStack unclaim = createGuiItem(Material.BARRIER, ChatColor.RED + "Unclaim", 
                    Arrays.asList(ChatColor.GRAY + "Unclaim this chunk"));
                gui.setItem(10, unclaim);
                
                ItemStack members = createGuiItem(Material.PLAYER_HEAD, ChatColor.AQUA + "Manage Members", 
                    Arrays.asList(ChatColor.GRAY + "Add/remove members"));
                gui.setItem(12, members);
                
                ItemStack sell = createGuiItem(Material.EMERALD, ChatColor.YELLOW + "Sell Claim", 
                    Arrays.asList(ChatColor.GRAY + "Put claim up for sale"));
                gui.setItem(14, sell);
                
                ItemStack permissions = createGuiItem(Material.REDSTONE, ChatColor.RED + "Permissions", 
                    Arrays.asList(ChatColor.GRAY + "Manage claim permissions"));
                gui.setItem(16, permissions);
            }
            
            if (claim.isForSale()) {
                ItemStack buy = createGuiItem(Material.GOLD_INGOT, ChatColor.GREEN + "Buy Claim", 
                    Arrays.asList(ChatColor.GRAY + "Price: " + ChatColor.WHITE + plugin.getEconomyManager().format(claim.getSalePrice())));
                gui.setItem(22, buy);
            }
        }
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Claim Management")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;
            
            Chunk chunk = player.getLocation().getChunk();
            
            if (clicked.getType() == Material.GOLDEN_SHOVEL) {
                if (plugin.getClaimManager().claimChunk(player, chunk)) {
                    player.closeInventory();
                }
            } else if (clicked.getType() == Material.BARRIER) {
                if (plugin.getClaimManager().unclaimChunk(player, chunk)) {
                    player.closeInventory();
                }
            } else if (clicked.getType() == Material.GOLD_INGOT) {
                if (plugin.getClaimManager().buyClaim(player, chunk)) {
                    player.sendMessage(ChatColor.GREEN + "Claim purchased!");
                    player.closeInventory();
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to purchase claim!");
                }
            } else if (clicked.getType() == Material.PLAYER_HEAD) {
                player.sendMessage(ChatColor.YELLOW + "Member management coming soon!");
            } else if (clicked.getType() == Material.REDSTONE) {
                player.sendMessage(ChatColor.YELLOW + "Permission management coming soon!");
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

