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

import java.util.UUID;

public class ShopGUIManager implements Listener {
    private final Ecore plugin;

    public ShopGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Open item selection GUI for shop creation
    public void openItemSelectionGUI(Player player, boolean isPlayerShop) {
        Inventory gui = Bukkit.createInventory(player, 27, ChatColor.DARK_GREEN + (isPlayerShop ? "Player Shop Item Selection" : "Admin Shop Item Selection"));
        // Add placeholder items (e.g., common materials for selection)
        ItemStack diamond = new ItemStack(Material.DIAMOND);
        ItemMeta diamondMeta = diamond.getItemMeta();
        diamondMeta.setDisplayName(ChatColor.AQUA + "Diamond");
        diamond.setItemMeta(diamondMeta);
        gui.setItem(10, diamond);

        ItemStack iron = new ItemStack(Material.IRON_INGOT);
        ItemMeta ironMeta = iron.getItemMeta();
        ironMeta.setDisplayName(ChatColor.GRAY + "Iron Ingot");
        iron.setItemMeta(ironMeta);
        gui.setItem(12, iron);

        ItemStack gold = new ItemStack(Material.GOLD_INGOT);
        ItemMeta goldMeta = gold.getItemMeta();
        goldMeta.setDisplayName(ChatColor.YELLOW + "Gold Ingot");
        gold.setItemMeta(goldMeta);
        gui.setItem(14, gold);

        player.openInventory(gui);
    }

    // Handle inventory clicks
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (!title.contains("Shop Item Selection")) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        boolean isPlayerShop = title.contains("Player Shop");
        plugin.getShopManager().getPendingCreation(player.getUniqueId()).setItem(clicked.clone());
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "Item set to " + clicked.getType().name() + ". Please set quantity in chat (1-64).");
        plugin.registerPendingAction(player, "shopgui:" + (isPlayerShop ? "player" : "admin") + ":quantity");
    }

    // Handle chat input for shop creation
    public void handleChatInput(Player player, String message, String action) {
        UUID uuid = player.getUniqueId();
        ShopManager.ShopCreationData data = plugin.getShopManager().getPendingCreation(uuid);
        if (data == null) {
            player.sendMessage(ChatColor.RED + "No pending shop creation found!");
            return;
        }

        String[] parts = action.split(":");
        if (parts.length != 3) return;
        boolean isPlayerShop = parts[1].equals("player");

        try {
            switch (parts[2]) {
                case "quantity":
                    int quantity = Integer.parseInt(message);
                    if (quantity < 1 || quantity > 64) {
                        player.sendMessage(ChatColor.RED + "Quantity must be between 1 and 64!");
                        return;
                    }
                    data.setQuantity(quantity);
                    player.sendMessage(ChatColor.YELLOW + "Quantity set to " + quantity + ". Please set buy price in chat.");
                    plugin.registerPendingAction(player, "shopgui:" + parts[1] + ":buyPrice");
                    break;
                case "buyPrice":
                    double buyPrice = Double.parseDouble(message);
                    if (buyPrice < 0) {
                        player.sendMessage(ChatColor.RED + "Buy price cannot be negative!");
                        return;
                    }
                    data.setBuyPrice(buyPrice);
                    player.sendMessage(ChatColor.YELLOW + "Buy price set to " + buyPrice + ". Please set sell price in chat.");
                    plugin.registerPendingAction(player, "shopgui:" + parts[1] + ":sellPrice");
                    break;
                case "sellPrice":
                    double sellPrice = Double.parseDouble(message);
                    if (sellPrice < 0) {
                        player.sendMessage(ChatColor.RED + "Sell price cannot be negative!");
                        return;
                    }
                    data.setSellPrice(sellPrice);
                    plugin.getShopManager().completeShopCreation(player, isPlayerShop, data.getItem(), data.getQuantity(), data.getBuyPrice(), data.getSellPrice());
                    break;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Please enter a valid number!");
        }
    }
}