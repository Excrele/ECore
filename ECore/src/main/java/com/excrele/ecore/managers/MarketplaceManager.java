package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Marketplace System Manager
 * Unified marketplace integrating shops and auction house with listings, categories, search, ratings, reviews
 */
public class MarketplaceManager {
    private final Ecore plugin;
    private File marketplaceFile;
    private FileConfiguration marketplaceConfig;
    private final Map<Integer, MarketplaceListing> listings; // Listing ID -> Listing
    private final Map<String, List<Integer>> categoryListings; // Category -> List of listing IDs
    private final Map<UUID, List<Review>> sellerReviews; // Seller UUID -> List of reviews
    private int nextListingId;
    
    public MarketplaceManager(Ecore plugin) {
        this.plugin = plugin;
        this.listings = new HashMap<>();
        this.categoryListings = new HashMap<>();
        this.sellerReviews = new HashMap<>();
        initializeConfig();
        loadListings();
    }
    
    private void initializeConfig() {
        marketplaceFile = new File(plugin.getDataFolder(), "marketplace.yml");
        if (!marketplaceFile.exists()) {
            try {
                marketplaceFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create marketplace.yml", e);
            }
        }
        marketplaceConfig = YamlConfiguration.loadConfiguration(marketplaceFile);
        nextListingId = marketplaceConfig.getInt("next-id", 1);
    }
    
    private void loadListings() {
        if (marketplaceConfig.contains("listings")) {
            for (String listingIdStr : marketplaceConfig.getConfigurationSection("listings").getKeys(false)) {
                try {
                    int listingId = Integer.parseInt(listingIdStr);
                    String path = "listings." + listingIdStr;
                    UUID seller = UUID.fromString(marketplaceConfig.getString(path + ".seller"));
                    String type = marketplaceConfig.getString(path + ".type", "item"); // item or service
                    String category = marketplaceConfig.getString(path + ".category", "general");
                    String title = marketplaceConfig.getString(path + ".title");
                    String description = marketplaceConfig.getString(path + ".description", "");
                    double price = marketplaceConfig.getDouble(path + ".price");
                    long createdAt = marketplaceConfig.getLong(path + ".created-at", System.currentTimeMillis());
                    
                    MarketplaceListing listing = new MarketplaceListing(listingId, seller, type, category, title, description, price, createdAt);
                    listings.put(listingId, listing);
                    
                    // Add to category
                    List<Integer> categoryList = categoryListings.computeIfAbsent(category, k -> new ArrayList<>());
                    categoryList.add(listingId);
                } catch (IllegalArgumentException e) {
                    // Skip invalid entries (NumberFormatException is a subclass of IllegalArgumentException)
                }
            }
        }
    }
    
    /**
     * Create a listing
     */
    public int createListing(Player seller, String type, String category, String title, String description, double price) {
        int listingId = nextListingId++;
        long createdAt = System.currentTimeMillis();
        
        MarketplaceListing listing = new MarketplaceListing(listingId, seller.getUniqueId(), type, category, title, description, price, createdAt);
        listings.put(listingId, listing);
        
        // Add to category
        List<Integer> categoryList = categoryListings.computeIfAbsent(category, k -> new ArrayList<>());
        categoryList.add(listingId);
        
        // Save
        String path = "listings." + listingId;
        marketplaceConfig.set(path + ".seller", seller.getUniqueId().toString());
        marketplaceConfig.set(path + ".type", type);
        marketplaceConfig.set(path + ".category", category);
        marketplaceConfig.set(path + ".title", title);
        marketplaceConfig.set(path + ".description", description);
        marketplaceConfig.set(path + ".price", price);
        marketplaceConfig.set(path + ".created-at", createdAt);
        marketplaceConfig.set("next-id", nextListingId);
        saveConfig();
        
        seller.sendMessage(org.bukkit.ChatColor.GREEN + "Listing created! ID: " + listingId);
        
        return listingId;
    }
    
    /**
     * Purchase a listing
     */
    public boolean purchaseListing(Player buyer, int listingId) {
        MarketplaceListing listing = listings.get(listingId);
        if (listing == null) {
            return false;
        }
        
        if (listing.getSeller().equals(buyer.getUniqueId())) {
            buyer.sendMessage(org.bukkit.ChatColor.RED + "You cannot purchase your own listing!");
            return false;
        }
        
        if (plugin.getEconomyManager().getBalance(buyer.getUniqueId()) < listing.getPrice()) {
            buyer.sendMessage(org.bukkit.ChatColor.RED + "You don't have enough money!");
            return false;
        }
        
        // Process payment
        plugin.getEconomyManager().removeBalance(buyer.getUniqueId(), listing.getPrice());
        
        Player seller = Bukkit.getPlayer(listing.getSeller());
        if (seller != null && seller.isOnline()) {
            plugin.getEconomyManager().addBalance(listing.getSeller(), listing.getPrice());
            seller.sendMessage(org.bukkit.ChatColor.GREEN + buyer.getName() + " purchased your listing: " + listing.getTitle());
        } else {
            plugin.getEconomyManager().addBalance(listing.getSeller(), listing.getPrice());
        }
        
        buyer.sendMessage(org.bukkit.ChatColor.GREEN + "Purchase successful!");
        
        // Remove listing
        removeListing(listingId);
        
        return true;
    }
    
    /**
     * Add review
     */
    public boolean addReview(Player reviewer, UUID sellerUuid, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            return false;
        }
        
        Review review = new Review(reviewer.getUniqueId(), sellerUuid, rating, comment, System.currentTimeMillis());
        List<Review> reviews = sellerReviews.computeIfAbsent(sellerUuid, k -> new ArrayList<>());
        reviews.add(review);
        
        // Save
        String path = "reviews." + sellerUuid.toString() + "." + review.getId();
        marketplaceConfig.set(path + ".reviewer", reviewer.getUniqueId().toString());
        marketplaceConfig.set(path + ".rating", rating);
        marketplaceConfig.set(path + ".comment", comment);
        marketplaceConfig.set(path + ".created-at", review.getCreatedAt());
        saveConfig();
        
        return true;
    }
    
    /**
     * Get seller rating
     */
    public double getSellerRating(UUID sellerUuid) {
        List<Review> reviews = sellerReviews.get(sellerUuid);
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        
        double total = 0.0;
        for (Review review : reviews) {
            total += review.getRating();
        }
        
        return total / reviews.size();
    }
    
    private void removeListing(int listingId) {
        MarketplaceListing listing = listings.remove(listingId);
        if (listing != null) {
            List<Integer> categoryList = categoryListings.get(listing.getCategory());
            if (categoryList != null) {
                categoryList.remove((Integer) listingId);
            }
        }
        
        marketplaceConfig.set("listings." + listingId, null);
        saveConfig();
    }
    
    private void saveConfig() {
        try {
            marketplaceConfig.save(marketplaceFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save marketplace.yml", e);
        }
    }
    
    public Collection<MarketplaceListing> getListings() {
        return listings.values();
    }
    
    public List<MarketplaceListing> getListingsByCategory(String category) {
        List<Integer> listingIds = categoryListings.get(category);
        if (listingIds == null) {
            return new ArrayList<>();
        }
        
        List<MarketplaceListing> result = new ArrayList<>();
        for (Integer id : listingIds) {
            MarketplaceListing listing = listings.get(id);
            if (listing != null) {
                result.add(listing);
            }
        }
        return result;
    }
    
    public MarketplaceListing getListing(int listingId) {
        return listings.get(listingId);
    }
    
    /**
     * Marketplace Listing class
     */
    public static class MarketplaceListing {
        private int id;
        private UUID seller;
        private String type;
        private String category;
        private String title;
        private String description;
        private double price;
        private long createdAt;
        
        public MarketplaceListing(int id, UUID seller, String type, String category, String title, 
                                 String description, double price, long createdAt) {
            this.id = id;
            this.seller = seller;
            this.type = type;
            this.category = category;
            this.title = title;
            this.description = description;
            this.price = price;
            this.createdAt = createdAt;
        }
        
        public int getId() { return id; }
        public UUID getSeller() { return seller; }
        public String getType() { return type; }
        public String getCategory() { return category; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public double getPrice() { return price; }
        public long getCreatedAt() { return createdAt; }
    }
    
    /**
     * Review class
     */
    public static class Review {
        private String id;
        private UUID reviewer;
        private UUID seller;
        private int rating;
        private String comment;
        private long createdAt;
        
        public Review(UUID reviewer, UUID seller, int rating, String comment, long createdAt) {
            this.id = UUID.randomUUID().toString();
            this.reviewer = reviewer;
            this.seller = seller;
            this.rating = rating;
            this.comment = comment;
            this.createdAt = createdAt;
        }
        
        public String getId() { return id; }
        public UUID getReviewer() { return reviewer; }
        public UUID getSeller() { return seller; }
        public int getRating() { return rating; }
        public String getComment() { return comment; }
        public long getCreatedAt() { return createdAt; }
    }
}

