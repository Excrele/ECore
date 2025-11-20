package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class AuctionHouseManager {
    private final Ecore plugin;
    private File auctionFile;
    private FileConfiguration auctionConfig;
    private final Map<Integer, Auction> activeAuctions;
    private final Map<Integer, BukkitTask> expirationTasks;
    private int nextAuctionId;

    public AuctionHouseManager(Ecore plugin) {
        this.plugin = plugin;
        this.activeAuctions = new HashMap<>();
        this.expirationTasks = new HashMap<>();
        initializeAuctionConfig();
        loadAuctions();
    }

    private void initializeAuctionConfig() {
        auctionFile = new File(plugin.getDataFolder(), "auctions.yml");
        if (!auctionFile.exists()) {
            try {
                auctionFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create auctions.yml", e);
            }
        }
        auctionConfig = YamlConfiguration.loadConfiguration(auctionFile);
        nextAuctionId = auctionConfig.getInt("next-id", 1);
    }

    private void loadAuctions() {
        if (auctionConfig.contains("auctions")) {
            for (String idStr : auctionConfig.getConfigurationSection("auctions").getKeys(false)) {
                try {
                    int id = Integer.parseInt(idStr);
                    String path = "auctions." + id;
                    
                    UUID sellerUuid = UUID.fromString(auctionConfig.getString(path + ".seller"));
                    ItemStack item = auctionConfig.getItemStack(path + ".item");
                    double startingBid = auctionConfig.getDouble(path + ".starting-bid");
                    double buyoutPrice = auctionConfig.getDouble(path + ".buyout-price", 0.0);
                    long expirationTime = auctionConfig.getLong(path + ".expiration-time");
                    double currentBid = auctionConfig.getDouble(path + ".current-bid", startingBid);
                    UUID highestBidderUuid = auctionConfig.getString(path + ".highest-bidder") != null ?
                        UUID.fromString(auctionConfig.getString(path + ".highest-bidder")) : null;
                    
                    Auction auction = new Auction(id, sellerUuid, item, startingBid, buyoutPrice, expirationTime, currentBid, highestBidderUuid);
                    activeAuctions.put(id, auction);
                    
                    // Schedule expiration check
                    scheduleExpiration(auction);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load auction " + idStr + ": " + e.getMessage());
                }
            }
        }
    }

    // Create a new auction
    public int createAuction(Player seller, ItemStack item, double startingBid, double buyoutPrice, long durationMinutes) {
        if (item == null || item.getType().isAir()) {
            return -1; // Invalid item
        }

        // Check if player has the item
        if (!seller.getInventory().containsAtLeast(item, item.getAmount())) {
            return -1; // Player doesn't have the item
        }

        // Remove item from player's inventory
        seller.getInventory().removeItem(item);

        int auctionId = nextAuctionId++;
        long expirationTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000);

        Auction auction = new Auction(auctionId, seller.getUniqueId(), item.clone(), startingBid, buyoutPrice, expirationTime, startingBid, null);
        activeAuctions.put(auctionId, auction);

        // Save to config
        String path = "auctions." + auctionId;
        auctionConfig.set(path + ".seller", seller.getUniqueId().toString());
        auctionConfig.set(path + ".item", item);
        auctionConfig.set(path + ".starting-bid", startingBid);
        auctionConfig.set(path + ".buyout-price", buyoutPrice);
        auctionConfig.set(path + ".expiration-time", expirationTime);
        auctionConfig.set(path + ".current-bid", startingBid);
        auctionConfig.set(path + ".highest-bidder", null);
        auctionConfig.set("next-id", nextAuctionId);
        saveAuctions();

        // Schedule expiration
        scheduleExpiration(auction);

        return auctionId;
    }

    // Place a bid on an auction
    public boolean placeBid(Player bidder, int auctionId, double bidAmount) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            return false; // Auction doesn't exist
        }

        if (auction.isExpired()) {
            return false; // Auction expired
        }

        if (bidder.getUniqueId().equals(auction.getSellerUuid())) {
            return false; // Can't bid on your own auction
        }

        if (bidAmount < auction.getCurrentBid()) {
            return false; // Bid too low
        }

        // Check if player has enough money
        if (!plugin.getEconomyManager().removeBalance(bidder.getUniqueId(), bidAmount)) {
            return false; // Not enough money
        }

        // Return money to previous highest bidder if exists
        if (auction.getHighestBidderUuid() != null) {
            plugin.getEconomyManager().addBalance(auction.getHighestBidderUuid(), auction.getCurrentBid());
            
            // Notify previous bidder
            Player previousBidder = Bukkit.getPlayer(auction.getHighestBidderUuid());
            if (previousBidder != null && previousBidder.isOnline()) {
                previousBidder.sendMessage("§cYou were outbid on auction #" + auctionId + "! Your money has been returned.");
            }
        }

        // Update auction
        auction.setCurrentBid(bidAmount);
        auction.setHighestBidderUuid(bidder.getUniqueId());

        // Save to config
        String path = "auctions." + auctionId;
        auctionConfig.set(path + ".current-bid", bidAmount);
        auctionConfig.set(path + ".highest-bidder", bidder.getUniqueId().toString());
        saveAuctions();

        // Notify seller
        Player seller = Bukkit.getPlayer(auction.getSellerUuid());
        if (seller != null && seller.isOnline()) {
            seller.sendMessage("§aYour auction #" + auctionId + " received a bid of " + String.format("%.2f", bidAmount) + "!");
        }

        bidder.sendMessage("§aBid placed! Current bid: " + String.format("%.2f", bidAmount));

        return true;
    }

    // Buyout an auction
    public boolean buyoutAuction(Player buyer, int auctionId) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            return false; // Auction doesn't exist
        }

        if (auction.getBuyoutPrice() <= 0) {
            return false; // No buyout price set
        }

        if (auction.isExpired()) {
            return false; // Auction expired
        }

        if (buyer.getUniqueId().equals(auction.getSellerUuid())) {
            return false; // Can't buyout your own auction
        }

        // Check if player has enough money
        if (!plugin.getEconomyManager().removeBalance(buyer.getUniqueId(), auction.getBuyoutPrice())) {
            return false; // Not enough money
        }

        // Return money to highest bidder if exists
        if (auction.getHighestBidderUuid() != null) {
            plugin.getEconomyManager().addBalance(auction.getHighestBidderUuid(), auction.getCurrentBid());
        }

        // Complete the auction
        completeAuction(auctionId, buyer.getUniqueId(), auction.getBuyoutPrice());

        return true;
    }

    // Cancel an auction (seller only)
    public boolean cancelAuction(Player seller, int auctionId) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            return false; // Auction doesn't exist
        }

        if (!seller.getUniqueId().equals(auction.getSellerUuid())) {
            return false; // Not the seller
        }

        // Return item to seller
        if (seller.getInventory().firstEmpty() == -1) {
            seller.sendMessage("§cYour inventory is full! Cannot cancel auction.");
            return false;
        }
        seller.getInventory().addItem(auction.getItem());

        // Return money to highest bidder if exists
        if (auction.getHighestBidderUuid() != null) {
            plugin.getEconomyManager().addBalance(auction.getHighestBidderUuid(), auction.getCurrentBid());
            
            Player bidder = Bukkit.getPlayer(auction.getHighestBidderUuid());
            if (bidder != null && bidder.isOnline()) {
                bidder.sendMessage("§cAuction #" + auctionId + " was cancelled. Your bid has been returned.");
            }
        }

        // Remove auction
        removeAuction(auctionId);
        seller.sendMessage("§aAuction #" + auctionId + " cancelled. Item returned to your inventory.");

        return true;
    }

    // Complete an auction (when expired or bought out)
    private void completeAuction(int auctionId, UUID buyerUuid, double finalPrice) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) return;

        // Give item to buyer
        Player buyer = Bukkit.getPlayer(buyerUuid);
        if (buyer != null && buyer.isOnline()) {
            if (buyer.getInventory().firstEmpty() == -1) {
                // Inventory full, drop item
                buyer.getWorld().dropItem(buyer.getLocation(), auction.getItem());
                buyer.sendMessage("§eYour inventory was full! Item dropped at your location.");
            } else {
                buyer.getInventory().addItem(auction.getItem());
            }
            buyer.sendMessage("§aYou won auction #" + auctionId + " for " + String.format("%.2f", finalPrice) + "!");
        } else {
            // Buyer offline, store item for later pickup
            String path = "completed-auctions." + buyerUuid.toString() + "." + auctionId;
            auctionConfig.set(path + ".item", auction.getItem());
            auctionConfig.set(path + ".price", finalPrice);
            saveAuctions();
        }

        // Pay seller
        plugin.getEconomyManager().addBalance(auction.getSellerUuid(), finalPrice);
        
        Player seller = Bukkit.getPlayer(auction.getSellerUuid());
        if (seller != null && seller.isOnline()) {
            seller.sendMessage("§aYour auction #" + auctionId + " sold for " + String.format("%.2f", finalPrice) + "!");
        }

        // Remove auction
        removeAuction(auctionId);
    }

    // Remove an auction
    private void removeAuction(int auctionId) {
        activeAuctions.remove(auctionId);
        BukkitTask task = expirationTasks.remove(auctionId);
        if (task != null) {
            task.cancel();
        }
        auctionConfig.set("auctions." + auctionId, null);
        saveAuctions();
    }

    // Schedule expiration check for an auction
    private void scheduleExpiration(Auction auction) {
        long timeUntilExpiration = auction.getExpirationTime() - System.currentTimeMillis();
        if (timeUntilExpiration <= 0) {
            // Already expired
            expireAuction(auction.getId());
            return;
        }

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            expireAuction(auction.getId());
        }, timeUntilExpiration / 50); // Convert milliseconds to ticks

        expirationTasks.put(auction.getId(), task);
    }

    // Expire an auction
    private void expireAuction(int auctionId) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) return;

        if (auction.getHighestBidderUuid() != null) {
            // Someone bid, complete the auction
            completeAuction(auctionId, auction.getHighestBidderUuid(), auction.getCurrentBid());
        } else {
            // No bids, return item to seller
            Player seller = Bukkit.getPlayer(auction.getSellerUuid());
            if (seller != null && seller.isOnline()) {
                if (seller.getInventory().firstEmpty() == -1) {
                    seller.getWorld().dropItem(seller.getLocation(), auction.getItem());
                    seller.sendMessage("§eYour auction #" + auctionId + " expired with no bids. Item dropped at your location.");
                } else {
                    seller.getInventory().addItem(auction.getItem());
                    seller.sendMessage("§eYour auction #" + auctionId + " expired with no bids. Item returned to your inventory.");
                }
            } else {
                // Seller offline, store item
                String path = "expired-auctions." + auction.getSellerUuid().toString() + "." + auctionId;
                auctionConfig.set(path + ".item", auction.getItem());
                saveAuctions();
            }
            removeAuction(auctionId);
        }
    }

    // Get all active auctions
    public List<Auction> getActiveAuctions() {
        return new ArrayList<>(activeAuctions.values());
    }

    // Get auction by ID
    public Auction getAuction(int auctionId) {
        return activeAuctions.get(auctionId);
    }

    // Get auctions by seller
    public List<Auction> getAuctionsBySeller(UUID sellerUuid) {
        List<Auction> sellerAuctions = new ArrayList<>();
        for (Auction auction : activeAuctions.values()) {
            if (auction.getSellerUuid().equals(sellerUuid)) {
                sellerAuctions.add(auction);
            }
        }
        return sellerAuctions;
    }

    private void saveAuctions() {
        try {
            auctionConfig.save(auctionFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save auctions.yml: " + e.getMessage());
        }
    }

    // Auction class
    public static class Auction {
        private final int id;
        private final UUID sellerUuid;
        private final ItemStack item;
        private final double startingBid;
        private final double buyoutPrice;
        private final long expirationTime;
        private double currentBid;
        private UUID highestBidderUuid;

        public Auction(int id, UUID sellerUuid, ItemStack item, double startingBid, double buyoutPrice, 
                      long expirationTime, double currentBid, UUID highestBidderUuid) {
            this.id = id;
            this.sellerUuid = sellerUuid;
            this.item = item;
            this.startingBid = startingBid;
            this.buyoutPrice = buyoutPrice;
            this.expirationTime = expirationTime;
            this.currentBid = currentBid;
            this.highestBidderUuid = highestBidderUuid;
        }

        public int getId() { return id; }
        public UUID getSellerUuid() { return sellerUuid; }
        public ItemStack getItem() { return item; }
        public double getStartingBid() { return startingBid; }
        public double getBuyoutPrice() { return buyoutPrice; }
        public long getExpirationTime() { return expirationTime; }
        public double getCurrentBid() { return currentBid; }
        public UUID getHighestBidderUuid() { return highestBidderUuid; }
        
        public void setCurrentBid(double currentBid) { this.currentBid = currentBid; }
        public void setHighestBidderUuid(UUID highestBidderUuid) { this.highestBidderUuid = highestBidderUuid; }
        
        public boolean isExpired() {
            return System.currentTimeMillis() >= expirationTime;
        }
        
        public long getTimeRemaining() {
            return Math.max(0, expirationTime - System.currentTimeMillis());
        }
    }
}

