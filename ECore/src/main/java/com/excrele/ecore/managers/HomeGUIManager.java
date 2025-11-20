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

public class HomeGUIManager implements Listener {
    private final Ecore plugin;

    public HomeGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openHomeGUI(Player player) {
        List<String> homes = plugin.getHomeManager().getPlayerHomes(player);
        int size = Math.max(9, ((homes.size() + 8) / 9) * 9);
        size = Math.min(size, 54);
        
        Inventory gui = Bukkit.createInventory(player, size, ChatColor.DARK_GREEN + "Your Homes");

        for (int i = 0; i < homes.size() && i < size - 1; i++) {
            String homeName = homes.get(i);
            Material icon = plugin.getHomeManager().getHomeIcon(player, homeName);
            String category = plugin.getHomeManager().getHomeCategory(player, homeName);
            String description = plugin.getHomeManager().getHomeDescription(player, homeName);
            
            ItemStack homeItem = new ItemStack(icon);
            ItemMeta meta = homeItem.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + homeName);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to teleport");
            lore.add(ChatColor.GRAY + "Right-click to manage");
            if (!category.equals("default")) {
                lore.add(ChatColor.DARK_GRAY + "Category: " + ChatColor.YELLOW + category);
            }
            if (!description.isEmpty()) {
                lore.add(ChatColor.DARK_GRAY + description);
            }
            meta.setLore(lore);
            homeItem.setItemMeta(meta);
            gui.setItem(i, homeItem);
        }

        // Add "Set New Home" button
        ItemStack newHome = new ItemStack(Material.ANVIL);
        ItemMeta newHomeMeta = newHome.getItemMeta();
        newHomeMeta.setDisplayName(ChatColor.GREEN + "Set New Home");
        newHome.setItemMeta(newHomeMeta);
        gui.setItem(size - 1, newHome);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (!event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Your Homes")) return;
        
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.ANVIL) {
            // Set new home
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Please enter a name for your new home in chat:");
            plugin.registerPendingAction(player, "home:set");
            return;
        }

        if (clicked.getType() == Material.COMPASS) {
            String homeName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            
            if (event.isRightClick()) {
                // Open management GUI
                openHomeManagementGUI(player, homeName);
            } else {
                // Teleport to home (with cost, cooldown, warmup checks)
                player.closeInventory();
                plugin.getHomeManager().teleportToHome(player, homeName);
            }
        }
    }

    private void openHomeManagementGUI(Player player, String homeName) {
        Inventory gui = Bukkit.createInventory(player, 9, ChatColor.DARK_RED + "Manage: " + homeName);

        // Delete button
        ItemStack delete = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.RED + "Delete Home");
        delete.setItemMeta(deleteMeta);
        gui.setItem(2, delete);

        // Rename button
        ItemStack rename = new ItemStack(Material.NAME_TAG);
        ItemMeta renameMeta = rename.getItemMeta();
        renameMeta.setDisplayName(ChatColor.YELLOW + "Rename Home");
        rename.setItemMeta(renameMeta);
        gui.setItem(4, rename);

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Back");
        back.setItemMeta(backMeta);
        gui.setItem(8, back);

        player.openInventory(gui);
    }
}

