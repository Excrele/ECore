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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionHouseGUIManager implements Listener {
    private final Ecore plugin;
    private final Map<Player, Integer> viewingAuction; // Player -> Auction ID

    public AuctionHouseGUIManager(Ecore plugin) {
        this.plugin = plugin;
        this.viewingAuction = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openAuctionHouseGUI(Player player) {
        List<AuctionHouseManager.Auction> auctions = plugin.getAuctionHouseManager().getActiveAuctions();
        int size = Math.max(9, ((auctions.size() + 8) / 9) * 9);
        size = Math.min(size, 54);
        
        Inventory gui = Bukkit.createInventory(player, size, ChatColor.DARK_GREEN + "Auction House");

        for (int i = 0; i < auctions.size() && i < size - 1; i++) {
            AuctionHouseManager.Auction auction = auctions.get(i);
            ItemStack displayItem = auction.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            
            List<String> lore = new ArrayList<>();
            if (meta != null && meta.hasLore()) {
                lore.addAll(meta.getLore());
            }
            lore.add("");
            lore.add(ChatColor.GRAY + "Auction #" + auction.getId());
            lore.add(ChatColor.YELLOW + "Current Bid: " + ChatColor.GREEN + String.format("%.2f", auction.getCurrentBid()));
            if (auction.getBuyoutPrice() > 0) {
                lore.add(ChatColor.GOLD + "Buyout: " + ChatColor.GREEN + String.format("%.2f", auction.getBuyoutPrice()));
            }
            lore.add(ChatColor.GRAY + "Time Left: " + formatTime(auction.getTimeRemaining()));
            
            String sellerName = Bukkit.getOfflinePlayer(auction.getSellerUuid()).getName();
            if (sellerName != null) {
                lore.add(ChatColor.GRAY + "Seller: " + ChatColor.WHITE + sellerName);
            }
            
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to view details");
            lore.add(ChatColor.YELLOW + "Right-click to bid/buyout");
            
            if (meta == null) {
                meta = Bukkit.getItemFactory().getItemMeta(displayItem.getType());
            }
            if (meta != null) {
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            
            gui.setItem(i, displayItem);
        }

        // Create Auction button
        ItemStack createAuction = new ItemStack(Material.ANVIL);
        ItemMeta createMeta = createAuction.getItemMeta();
        createMeta.setDisplayName(ChatColor.GREEN + "Create Auction");
        createMeta.setLore(List.of(
            ChatColor.GRAY + "Click to create a new auction",
            ChatColor.GRAY + "Hold the item you want to sell"
        ));
        createAuction.setItemMeta(createMeta);
        gui.setItem(size - 1, createAuction);

        player.openInventory(gui);
    }

    public void openAuctionDetailsGUI(Player player, int auctionId) {
        AuctionHouseManager.Auction auction = plugin.getAuctionHouseManager().getAuction(auctionId);
        if (auction == null) {
            player.sendMessage(ChatColor.RED + "Auction not found!");
            return;
        }

        viewingAuction.put(player, auctionId);
        Inventory gui = Bukkit.createInventory(player, 27, ChatColor.DARK_GREEN + "Auction #" + auctionId);

        // Display item
        ItemStack displayItem = auction.getItem().clone();
        ItemMeta meta = displayItem.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (meta != null && meta.hasLore()) {
            lore.addAll(meta.getLore());
        }
        lore.add("");
        lore.add(ChatColor.GRAY + "Auction #" + auction.getId());
        lore.add(ChatColor.YELLOW + "Starting Bid: " + ChatColor.GREEN + String.format("%.2f", auction.getStartingBid()));
        lore.add(ChatColor.YELLOW + "Current Bid: " + ChatColor.GREEN + String.format("%.2f", auction.getCurrentBid()));
        if (auction.getBuyoutPrice() > 0) {
            lore.add(ChatColor.GOLD + "Buyout: " + ChatColor.GREEN + String.format("%.2f", auction.getBuyoutPrice()));
        }
        lore.add(ChatColor.GRAY + "Time Left: " + formatTime(auction.getTimeRemaining()));
        
        String sellerName = Bukkit.getOfflinePlayer(auction.getSellerUuid()).getName();
        if (sellerName != null) {
            lore.add(ChatColor.GRAY + "Seller: " + ChatColor.WHITE + sellerName);
        }
        
        if (auction.getHighestBidderUuid() != null) {
            String bidderName = Bukkit.getOfflinePlayer(auction.getHighestBidderUuid()).getName();
            if (bidderName != null) {
                lore.add(ChatColor.GRAY + "Highest Bidder: " + ChatColor.WHITE + bidderName);
            }
        }
        
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(displayItem.getType());
        }
        if (meta != null) {
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
        }
        
        gui.setItem(4, displayItem);

        // Bid button
        ItemStack bidButton = new ItemStack(Material.GOLD_INGOT);
        ItemMeta bidMeta = bidButton.getItemMeta();
        bidMeta.setDisplayName(ChatColor.YELLOW + "Place Bid");
        bidMeta.setLore(List.of(
            ChatColor.GRAY + "Minimum bid: " + ChatColor.GREEN + String.format("%.2f", auction.getCurrentBid() + 0.01),
            ChatColor.GRAY + "Click to enter bid amount"
        ));
        bidButton.setItemMeta(bidMeta);
        gui.setItem(11, bidButton);

        // Buyout button (if available)
        if (auction.getBuyoutPrice() > 0) {
            ItemStack buyoutButton = new ItemStack(Material.EMERALD);
            ItemMeta buyoutMeta = buyoutButton.getItemMeta();
            buyoutMeta.setDisplayName(ChatColor.GREEN + "Buyout");
            buyoutMeta.setLore(List.of(
                ChatColor.GRAY + "Price: " + ChatColor.GREEN + String.format("%.2f", auction.getBuyoutPrice()),
                ChatColor.GRAY + "Click to buyout immediately"
            ));
            buyoutButton.setItemMeta(buyoutMeta);
            gui.setItem(13, buyoutButton);
        }

        // Cancel button (if seller)
        if (player.getUniqueId().equals(auction.getSellerUuid())) {
            ItemStack cancelButton = new ItemStack(Material.REDSTONE_BLOCK);
            ItemMeta cancelMeta = cancelButton.getItemMeta();
            cancelMeta.setDisplayName(ChatColor.RED + "Cancel Auction");
            cancelMeta.setLore(List.of(
                ChatColor.GRAY + "Click to cancel this auction",
                ChatColor.GRAY + "Item will be returned to you"
            ));
            cancelButton.setItemMeta(cancelMeta);
            gui.setItem(15, cancelButton);
        }

        // Back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Back");
        backButton.setItemMeta(backMeta);
        gui.setItem(22, backButton);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        
        if (title.equals(ChatColor.DARK_GREEN + "Auction House")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.ANVIL) {
                // Create auction
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Hold the item you want to auction and use /ah create");
                return;
            }

            // Clicked on an auction item - need to extract auction ID from lore
            if (clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
                List<String> lore = clicked.getItemMeta().getLore();
                for (String line : lore) {
                    if (line.contains("Auction #")) {
                        try {
                            int auctionId = Integer.parseInt(line.replace(ChatColor.GRAY + "Auction #", "").trim());
                            openAuctionDetailsGUI(player, auctionId);
                            return;
                        } catch (NumberFormatException e) {
                            // Invalid auction ID
                        }
                    }
                }
            }
        } else if (title.startsWith(ChatColor.DARK_GREEN + "Auction #")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            Integer auctionId = viewingAuction.get(player);
            if (auctionId == null) return;

            if (clicked.getType() == Material.ARROW) {
                // Back button
                viewingAuction.remove(player);
                openAuctionHouseGUI(player);
            } else if (clicked.getType() == Material.GOLD_INGOT) {
                // Bid button
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Enter bid amount in chat (or type 'cancel' to cancel):");
                plugin.registerPendingAction(player, "ah:bid:" + auctionId);
            } else if (clicked.getType() == Material.EMERALD) {
                // Buyout button
                player.closeInventory();
                AuctionHouseManager.Auction auction = plugin.getAuctionHouseManager().getAuction(auctionId);
                if (auction != null) {
                    if (plugin.getAuctionHouseManager().buyoutAuction(player, auctionId)) {
                        player.sendMessage(ChatColor.GREEN + "Auction bought out successfully!");
                    } else {
                        player.sendMessage(ChatColor.RED + "Failed to buyout auction!");
                    }
                }
            } else if (clicked.getType() == Material.REDSTONE_BLOCK) {
                // Cancel button
                player.closeInventory();
                if (plugin.getAuctionHouseManager().cancelAuction(player, auctionId)) {
                    player.sendMessage(ChatColor.GREEN + "Auction cancelled!");
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to cancel auction!");
                }
            }
        }
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
}

