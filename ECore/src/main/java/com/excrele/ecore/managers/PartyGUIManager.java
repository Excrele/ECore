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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GUI manager for party system.
 */
public class PartyGUIManager implements Listener {
    private final Ecore plugin;

    public PartyGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openPartyGUI(Player player) {
        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        
        int size = 27;
        String title = party != null ? ChatColor.DARK_BLUE + "Party: " + party.getLeaderName() : 
                      ChatColor.DARK_BLUE + "Party";
        Inventory gui = Bukkit.createInventory(null, size, title);

        if (party != null) {
            // Show party members
            int slot = 0;
            for (String memberName : party.getMembers().values()) {
                if (slot >= size - 3) break;
                
                Player member = Bukkit.getPlayer(memberName);
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                if (meta != null) {
                    if (member != null) {
                        meta.setOwningPlayer(member);
                    } else {
                        meta.setOwningPlayer(Bukkit.getOfflinePlayer(memberName));
                    }
                    
                    boolean isLeader = memberName.equals(party.getLeaderName());
                    meta.setDisplayName((isLeader ? ChatColor.GOLD : ChatColor.WHITE) + memberName + 
                                       (isLeader ? " (Leader)" : ""));
                    List<String> lore = new ArrayList<>();
                    if (member != null && member.isOnline()) {
                        lore.add(ChatColor.GREEN + "● Online");
                    } else {
                        lore.add(ChatColor.GRAY + "○ Offline");
                    }
                    meta.setLore(lore);
                    head.setItemMeta(meta);
                }
                gui.setItem(slot++, head);
            }

            // Add action buttons
            if (party.isLeader(player.getUniqueId())) {
                ItemStack invite = createMenuItem(Material.EMERALD, ChatColor.GREEN + "Invite Player", 
                        Collections.singletonList(ChatColor.GRAY + "Click to invite"));
                gui.setItem(size - 3, invite);

                ItemStack kick = createMenuItem(Material.REDSTONE, ChatColor.RED + "Kick Player", 
                        Collections.singletonList(ChatColor.GRAY + "Click to kick"));
                gui.setItem(size - 2, kick);
            }

            ItemStack leave = createMenuItem(Material.BARRIER, ChatColor.RED + "Leave Party", 
                    Collections.singletonList(ChatColor.GRAY + "Click to leave"));
            gui.setItem(size - 1, leave);
        } else {
            // No party - show create button
            ItemStack create = createMenuItem(Material.EMERALD, ChatColor.GREEN + "Create Party", 
                    Collections.singletonList(ChatColor.GRAY + "Click to create a party"));
            gui.setItem(13, create);
        }

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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        if (!title.startsWith(ChatColor.DARK_BLUE + "Party")) return;
        
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        PartyManager.Party party = plugin.getPartyManager().getParty(player.getUniqueId());

        if (clicked.getType() == Material.EMERALD) {
            if (party == null) {
                // Create party
                plugin.getPartyManager().createParty(player);
            } else if (party.isLeader(player.getUniqueId())) {
                // Invite player
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Please enter a player name in chat:");
                plugin.registerPendingAction(player, "party:invite");
            }
        } else if (clicked.getType() == Material.REDSTONE && party != null && party.isLeader(player.getUniqueId())) {
            // Kick player
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Please enter a player name in chat:");
            plugin.registerPendingAction(player, "party:kick");
        } else if (clicked.getType() == Material.BARRIER && party != null) {
            // Leave party
            plugin.getPartyManager().leaveParty(player);
            player.closeInventory();
        }
    }
}

