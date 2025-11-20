package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.AuctionHouseManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuctionHouseCommand implements CommandExecutor {
    private final Ecore plugin;

    public AuctionHouseCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            plugin.getAuctionHouseGUIManager().openAuctionHouseGUI(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreate(player, args);
            case "bid":
                return handleBid(player, args);
            case "buyout":
                return handleBuyout(player, args);
            case "cancel":
                return handleCancel(player, args);
            case "list":
            case "view":
                plugin.getAuctionHouseGUIManager().openAuctionHouseGUI(player);
                return true;
            case "my":
            case "mine":
                return handleMyAuctions(player);
            default:
                player.sendMessage(ChatColor.RED + "Usage: /ah [create|bid|buyout|cancel|list|my]");
                return true;
        }
    }

    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /ah create <starting-bid> <buyout-price> <duration-minutes>");
            player.sendMessage(ChatColor.GRAY + "Hold the item you want to auction in your hand.");
            player.sendMessage(ChatColor.GRAY + "Set buyout-price to 0 for no buyout option.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must hold an item in your hand to auction!");
            return true;
        }

        try {
            double startingBid = Double.parseDouble(args[1]);
            double buyoutPrice = Double.parseDouble(args[2]);
            long durationMinutes = Long.parseLong(args[3]);

            if (startingBid <= 0) {
                player.sendMessage(ChatColor.RED + "Starting bid must be positive!");
                return true;
            }

            if (durationMinutes <= 0 || durationMinutes > 10080) { // Max 7 days
                player.sendMessage(ChatColor.RED + "Duration must be between 1 and 10080 minutes (7 days)!");
                return true;
            }

            int auctionId = plugin.getAuctionHouseManager().createAuction(player, item, startingBid, buyoutPrice, durationMinutes);
            if (auctionId > 0) {
                player.sendMessage(ChatColor.GREEN + "Auction #" + auctionId + " created successfully!");
                player.sendMessage(ChatColor.GRAY + "Starting bid: " + String.format("%.2f", startingBid));
                if (buyoutPrice > 0) {
                    player.sendMessage(ChatColor.GRAY + "Buyout price: " + String.format("%.2f", buyoutPrice));
                }
                player.sendMessage(ChatColor.GRAY + "Duration: " + durationMinutes + " minutes");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to create auction! Make sure you have the item in your hand.");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number format!");
        }
        return true;
    }

    private boolean handleBid(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /ah bid <auction-id> <bid-amount>");
            return true;
        }

        try {
            int auctionId = Integer.parseInt(args[1]);
            double bidAmount = Double.parseDouble(args[2]);

            if (bidAmount <= 0) {
                player.sendMessage(ChatColor.RED + "Bid amount must be positive!");
                return true;
            }

            if (plugin.getAuctionHouseManager().placeBid(player, auctionId, bidAmount)) {
                player.sendMessage(ChatColor.GREEN + "Bid placed successfully!");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to place bid! Check that the auction exists and your bid is high enough.");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number format!");
        }
        return true;
    }

    private boolean handleBuyout(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /ah buyout <auction-id>");
            return true;
        }

        try {
            int auctionId = Integer.parseInt(args[1]);

            if (plugin.getAuctionHouseManager().buyoutAuction(player, auctionId)) {
                player.sendMessage(ChatColor.GREEN + "Auction bought out successfully!");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to buyout auction! It may not exist, have no buyout price, or you may not have enough money.");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid auction ID!");
        }
        return true;
    }

    private boolean handleCancel(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /ah cancel <auction-id>");
            return true;
        }

        try {
            int auctionId = Integer.parseInt(args[1]);

            if (plugin.getAuctionHouseManager().cancelAuction(player, auctionId)) {
                player.sendMessage(ChatColor.GREEN + "Auction cancelled successfully!");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to cancel auction! You may not be the seller or the auction may not exist.");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid auction ID!");
        }
        return true;
    }

    private boolean handleMyAuctions(Player player) {
        java.util.List<AuctionHouseManager.Auction> myAuctions = 
            plugin.getAuctionHouseManager().getAuctionsBySeller(player.getUniqueId());

        if (myAuctions.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You have no active auctions.");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "=== Your Active Auctions ===");
        for (AuctionHouseManager.Auction auction : myAuctions) {
            player.sendMessage(ChatColor.GREEN + "Auction #" + auction.getId() + ":");
            player.sendMessage(ChatColor.GRAY + "  Item: " + auction.getItem().getType().name() + " x" + auction.getItem().getAmount());
            player.sendMessage(ChatColor.GRAY + "  Current Bid: " + String.format("%.2f", auction.getCurrentBid()));
            if (auction.getBuyoutPrice() > 0) {
                player.sendMessage(ChatColor.GRAY + "  Buyout: " + String.format("%.2f", auction.getBuyoutPrice()));
            }
            player.sendMessage(ChatColor.GRAY + "  Time Left: " + formatTime(auction.getTimeRemaining()));
        }
        return true;
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

