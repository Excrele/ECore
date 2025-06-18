package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Manages staff-related functionalities
public class StaffManager implements Listener {
    private final Ecore plugin;
    private final Map<UUID, Boolean> vanishedPlayers;

    public StaffManager(Ecore plugin) {
        this.plugin = plugin;
        this.vanishedPlayers = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Open staff GUI
    public void openStaffGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, ChatColor.DARK_GREEN + "Staff Management");

        ItemStack ban = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta banMeta = ban.getItemMeta();
        banMeta.setDisplayName(ChatColor.RED + "Ban Player");
        ban.setItemMeta(banMeta);
        gui.setItem(10, ban);

        ItemStack kick = new ItemStack(Material.IRON_BOOTS);
        ItemMeta kickMeta = kick.getItemMeta();
        kickMeta.setDisplayName(ChatColor.YELLOW + "Kick Player");
        kick.setItemMeta(kickMeta);
        gui.setItem(12, kick);

        ItemStack inspect = new ItemStack(Material.CHEST);
        ItemMeta inspectMeta = inspect.getItemMeta();
        inspectMeta.setDisplayName(ChatColor.BLUE + "Inspect Inventory");
        inspect.setItemMeta(inspectMeta);
        gui.setItem(14, inspect);

        ItemStack reports = new ItemStack(Material.BOOK);
        ItemMeta reportsMeta = reports.getItemMeta();
        reportsMeta.setDisplayName(ChatColor.GREEN + "View Reports");
        reports.setItemMeta(reportsMeta);
        gui.setItem(16, reports);

        player.openInventory(gui);
    }

    // Ban player
    public void banPlayer(Player staff, String target, String reason) {
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            targetPlayer.kickPlayer(ChatColor.RED + "You have been banned: " + reason);
        }
        Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(target, reason, null, staff.getName());
        staff.sendMessage(ChatColor.GREEN + "Banned " + target + " for: " + reason);
        plugin.getDiscordManager().sendStaffLogNotification("punishment-log", staff.getName(), "banned", target, reason);
    }

    // Kick player
    public void kickPlayer(Player staff, String target, String reason) {
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            targetPlayer.kickPlayer(ChatColor.RED + "You have been kicked by " + staff.getName() + ": " + reason);
            staff.sendMessage(ChatColor.GREEN + "Kicked " + target);
            plugin.getDiscordManager().sendStaffLogNotification("punishment-log", staff.getName(), "kicked", target, reason);
        } else {
            staff.sendMessage(ChatColor.RED + "Player not found!");
        }
    }

    // Inspect player inventory
    public void openPlayerInventory(Player staff, String target) {
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            Inventory inv = Bukkit.createInventory(null, 54, target + "'s Inventory");
            ItemStack[] contents = targetPlayer.getInventory().getContents();
            for (int i = 0; i < contents.length && i < 36; i++) {
                inv.setItem(i, contents[i]);
            }
            ItemStack[] armor = targetPlayer.getInventory().getArmorContents();
            for (int i = 0; i < armor.length; i++) {
                inv.setItem(36 + i, armor[i]);
            }
            inv.setItem(45, targetPlayer.getInventory().getItemInOffHand());
            staff.openInventory(inv);
            staff.sendMessage(ChatColor.GREEN + "Opened inventory of " + target);
            plugin.getDiscordManager().sendStaffLogNotification("punishment-log", staff.getName(), "inspected inventory of", target, "");
        } else {
            staff.sendMessage(ChatColor.RED + "Player not found!");
        }
    }

    // Toggle vanish mode
    public void toggleVanish(Player player) {
        UUID uuid = player.getUniqueId();
        boolean isVanished = vanishedPlayers.getOrDefault(uuid, false);
        if (!isVanished) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission("ecore.vanish.staff")) {
                    p.hidePlayer(plugin, player);
                }
            }
            vanishedPlayers.put(uuid, true);
            player.sendMessage(ChatColor.GREEN + "You are now vanished!");
            plugin.getDiscordManager().sendStaffLogNotification("vanish-log", player.getName(), "vanished", "", "Enabled");
            // Fake leave message
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " left the game");
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(plugin, player);
            }
            vanishedPlayers.remove(uuid);
            player.sendMessage(ChatColor.GREEN + "You are no longer vanished!");
            plugin.getDiscordManager().sendStaffLogNotification("vanish-log", player.getName(), "unvanished", "", "Disabled");
            // Fake join message
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " joined the game");
        }
    }

    // Teleport to player
    public void teleportToPlayer(Player staff, String target) {
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            staff.teleport(targetPlayer.getLocation());
            staff.sendMessage(ChatColor.GREEN + "Teleported to " + target);
            plugin.getDiscordManager().sendStaffLogNotification("teleport-log", staff.getName(), "teleported to", target, "");
        } else {
            staff.sendMessage(ChatColor.RED + "Player not found!");
        }
    }

    // Handle join event for vanished players
    @EventHandler
    public void handleJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (vanishedPlayers.getOrDefault(player.getUniqueId(), false)) {
            event.setJoinMessage(null);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission("ecore.vanish.staff")) {
                    p.hidePlayer(plugin, player);
                }
            }
        }
    }

    // Handle quit event for vanished players
    @EventHandler
    public void handleQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (vanishedPlayers.getOrDefault(player.getUniqueId(), false)) {
            event.setQuitMessage(null);
        }
    }

    // Check if player is vanished
    public boolean isVanished(Player player) {
        return vanishedPlayers.getOrDefault(player.getUniqueId(), false);
    }
}