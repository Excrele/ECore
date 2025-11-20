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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * GUI manager for friend system.
 */
public class FriendGUIManager implements Listener {
    private final Ecore plugin;

    public FriendGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openFriendGUI(Player player) {
        List<UUID> friends = plugin.getFriendManager().getFriends(player.getUniqueId());
        List<UUID> requests = plugin.getFriendManager().getPendingRequests(player.getUniqueId());
        
        int size = Math.max(9, Math.min(54, ((friends.size() + requests.size() + 2 + 8) / 9) * 9));
        Inventory gui = Bukkit.createInventory(null, size, ChatColor.DARK_GREEN + "Friends");

        int slot = 0;

        // Add pending requests section
        if (!requests.isEmpty()) {
            ItemStack requestsHeader = createMenuItem(Material.BOOK, ChatColor.YELLOW + "Pending Requests (" + requests.size() + ")", 
                    Collections.singletonList(ChatColor.GRAY + "Click to accept"));
            gui.setItem(slot++, requestsHeader);

            for (UUID requesterUuid : requests) {
                if (slot >= size - 1) break;
                ItemStack head = createPlayerHead(requesterUuid);
                ItemMeta meta = head.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + Bukkit.getOfflinePlayer(requesterUuid).getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "Click to accept");
                lore.add(ChatColor.RED + "Right-click to deny");
                meta.setLore(lore);
                head.setItemMeta(meta);
                gui.setItem(slot++, head);
            }
        }

        // Add separator
        if (slot < size - 1 && !friends.isEmpty()) {
            ItemStack separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = separator.getItemMeta();
            meta.setDisplayName(" ");
            separator.setItemMeta(meta);
            gui.setItem(slot++, separator);
        }

        // Add friends section
        if (!friends.isEmpty()) {
            ItemStack friendsHeader = createMenuItem(Material.PLAYER_HEAD, ChatColor.GREEN + "Your Friends (" + friends.size() + ")", 
                    Collections.singletonList(ChatColor.GRAY + "Click to view info"));
            gui.setItem(slot++, friendsHeader);

            for (UUID friendUuid : friends) {
                if (slot >= size - 1) break;
                ItemStack head = createPlayerHead(friendUuid);
                ItemMeta meta = head.getItemMeta();
                Player friend = Bukkit.getPlayer(friendUuid);
                String name = friend != null ? friend.getName() : Bukkit.getOfflinePlayer(friendUuid).getName();
                boolean online = friend != null && friend.isOnline();
                
                meta.setDisplayName((online ? ChatColor.GREEN : ChatColor.GRAY) + name);
                List<String> lore = new ArrayList<>();
                lore.add(online ? ChatColor.GREEN + "● Online" : ChatColor.GRAY + "○ Offline");
                if (online && friend != null) {
                    lore.add(ChatColor.GRAY + "World: " + ChatColor.WHITE + friend.getWorld().getName());
                }
                meta.setLore(lore);
                head.setItemMeta(meta);
                gui.setItem(slot++, head);
            }
        }

        // Add "Add Friend" button
        ItemStack addFriend = createMenuItem(Material.EMERALD, ChatColor.GREEN + "Add Friend", 
                Collections.singletonList(ChatColor.GRAY + "Click to add a friend"));
        gui.setItem(size - 1, addFriend);

        player.openInventory(gui);
    }

    private ItemStack createMenuItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPlayerHead(UUID uuid) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                meta.setOwningPlayer(player);
            } else {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            }
            head.setItemMeta(meta);
        }
        return head;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (!event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Friends")) return;
        
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.EMERALD) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Please enter a player name in chat:");
            plugin.registerPendingAction(player, "friend:add");
            return;
        }

        if (clicked.getType() == Material.PLAYER_HEAD && clicked.hasItemMeta()) {
            String displayName = clicked.getItemMeta().getDisplayName();
            String playerName = ChatColor.stripColor(displayName);
            
            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }

            // Check if it's a pending request
            List<UUID> requests = plugin.getFriendManager().getPendingRequests(player.getUniqueId());
            if (requests.contains(target.getUniqueId())) {
                if (event.isRightClick()) {
                    plugin.getFriendManager().denyFriendRequest(player, target);
                } else {
                    plugin.getFriendManager().acceptFriendRequest(player, target);
                }
                openFriendGUI(player);
            }
        }
    }
}

