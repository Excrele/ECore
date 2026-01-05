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
import java.util.UUID;

/**
 * GUI Manager for Advanced Ban System
 */
public class BanGUIManager implements Listener {
    private final Ecore plugin;
    
    public BanGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Open main ban management GUI
     */
    public void openBanGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 54, ChatColor.DARK_RED + "Ban Management");
        
        // Ban player button
        ItemStack banPlayer = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta banMeta = banPlayer.getItemMeta();
        banMeta.setDisplayName(ChatColor.RED + "Ban Player");
        banMeta.setLore(List.of(ChatColor.GRAY + "Click to ban a player"));
        banPlayer.setItemMeta(banMeta);
        gui.setItem(10, banPlayer);
        
        // Unban player button
        ItemStack unbanPlayer = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta unbanMeta = unbanPlayer.getItemMeta();
        unbanMeta.setDisplayName(ChatColor.GREEN + "Unban Player");
        unbanMeta.setLore(List.of(ChatColor.GRAY + "Click to unban a player"));
        unbanPlayer.setItemMeta(unbanMeta);
        gui.setItem(12, unbanPlayer);
        
        // Ban history button
        ItemStack history = new ItemStack(Material.BOOK);
        ItemMeta historyMeta = history.getItemMeta();
        historyMeta.setDisplayName(ChatColor.YELLOW + "Ban History");
        historyMeta.setLore(List.of(ChatColor.GRAY + "View ban history"));
        history.setItemMeta(historyMeta);
        gui.setItem(14, history);
        
        // Ban templates button
        ItemStack templates = new ItemStack(Material.PAPER);
        ItemMeta templatesMeta = templates.getItemMeta();
        templatesMeta.setDisplayName(ChatColor.BLUE + "Ban Templates");
        templatesMeta.setLore(List.of(ChatColor.GRAY + "View and use ban templates"));
        templates.setItemMeta(templatesMeta);
        gui.setItem(16, templates);
        
        // Appeals button
        List<BanManager.BanEntry> appeals = plugin.getBanManager().getPendingAppeals();
        ItemStack appealsItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta appealsMeta = appealsItem.getItemMeta();
        appealsMeta.setDisplayName(ChatColor.AQUA + "Ban Appeals");
        appealsMeta.setLore(List.of(
            ChatColor.GRAY + "Pending appeals: " + ChatColor.YELLOW + appeals.size(),
            ChatColor.GRAY + "Click to view appeals"
        ));
        appealsItem.setItemMeta(appealsMeta);
        gui.setItem(28, appealsItem);
        
        player.openInventory(gui);
    }
    
    /**
     * Open ban templates GUI
     */
    public void openBanTemplatesGUI(Player player) {
        List<BanManager.BanTemplate> templates = plugin.getBanManager().getBanTemplates();
        int size = Math.max(9, ((templates.size() + 8) / 9) * 9);
        size = Math.min(size, 54);
        Inventory gui = Bukkit.createInventory(player, size, ChatColor.DARK_BLUE + "Ban Templates");
        
        int slot = 0;
        for (BanManager.BanTemplate template : templates) {
            if (slot >= size) break;
            
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + template.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Reason: " + ChatColor.WHITE + template.getReason());
            if (template.getDuration() > 0) {
                lore.add(ChatColor.GRAY + "Duration: " + ChatColor.WHITE + formatDuration(template.getDuration()));
            } else {
                lore.add(ChatColor.GRAY + "Duration: " + ChatColor.RED + "Permanent");
            }
            lore.add(ChatColor.GRAY + "IP Ban: " + (template.isIpBan() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to use this template");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }
        
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Back");
        back.setItemMeta(backMeta);
        gui.setItem(size - 1, back);
        
        player.openInventory(gui);
    }
    
    /**
     * Open ban history GUI for a player
     */
    public void openBanHistoryGUI(Player player, UUID targetUUID) {
        List<BanManager.BanEntry> history = plugin.getBanManager().getBanHistory(targetUUID);
        int size = Math.max(9, ((history.size() + 8) / 9) * 9);
        size = Math.min(size, 54);
        Inventory gui = Bukkit.createInventory(player, size, ChatColor.DARK_RED + "Ban History");
        
        int slot = 0;
        for (BanManager.BanEntry entry : history) {
            if (slot >= size) break;
            
            ItemStack item = new ItemStack(Material.REDSTONE_BLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.RED + entry.getPlayerName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Reason: " + ChatColor.WHITE + entry.getReason());
            lore.add(ChatColor.GRAY + "Banned by: " + ChatColor.WHITE + entry.getStaffName());
            lore.add(ChatColor.GRAY + "Time: " + ChatColor.WHITE + new java.util.Date(entry.getBanTime()).toString());
            if (entry.getDuration() > 0) {
                lore.add(ChatColor.GRAY + "Duration: " + ChatColor.WHITE + formatDuration(entry.getDuration()));
            } else {
                lore.add(ChatColor.GRAY + "Duration: " + ChatColor.RED + "Permanent");
            }
            if (entry.isAppealed()) {
                lore.add(ChatColor.YELLOW + "Appealed: Yes");
                if (entry.getAppealMessage() != null) {
                    lore.add(ChatColor.GRAY + "Appeal: " + ChatColor.WHITE + entry.getAppealMessage());
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }
        
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Back");
        back.setItemMeta(backMeta);
        gui.setItem(size - 1, back);
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.equals(ChatColor.DARK_RED + "Ban Management")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;
            
            if (clicked.getType() == Material.REDSTONE_BLOCK) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Enter the player name to ban:");
                plugin.registerPendingAction(player, "ban:player");
            } else if (clicked.getType() == Material.EMERALD_BLOCK) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Enter the player name to unban:");
                plugin.registerPendingAction(player, "ban:unban");
            } else if (clicked.getType() == Material.BOOK) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Enter the player name to view ban history:");
                plugin.registerPendingAction(player, "ban:history");
            } else if (clicked.getType() == Material.PAPER) {
                openBanTemplatesGUI(player);
            } else if (clicked.getType() == Material.WRITABLE_BOOK) {
                // Open appeals GUI
                player.sendMessage(ChatColor.YELLOW + "Appeals feature - coming soon!");
            }
        } else if (title.equals(ChatColor.DARK_BLUE + "Ban Templates")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;
            
            if (clicked.getType() == Material.ARROW) {
                openBanGUI(player);
            } else if (clicked.getType() == Material.PAPER) {
                String templateName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Enter the player name to ban using template '" + templateName + "':");
                plugin.registerPendingAction(player, "ban:template:" + templateName);
            }
        }
    }
    
    private String formatDuration(long seconds) {
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) return (seconds / 60) + "m";
        if (seconds < 86400) return (seconds / 3600) + "h";
        return (seconds / 86400) + "d";
    }
}

