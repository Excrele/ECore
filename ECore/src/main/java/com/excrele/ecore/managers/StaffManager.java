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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class StaffManager implements Listener {
    private final Ecore plugin;
    private final Set<UUID> vanishedPlayers; // Tracks vanished players

    public StaffManager(Ecore plugin) {
        this.plugin = plugin;
        this.vanishedPlayers = new HashSet<>();
        // Register this class as a listener for inventory click events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Open the main staff GUI
    public void openStaffGUI(Player staff) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_RED + "Staff Menu");

        // Create items for the GUI
        ItemStack banItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta banMeta = banItem.getItemMeta();
        banMeta.setDisplayName(ChatColor.RED + "Ban Player");
        List<String> banLore = new ArrayList<>();
        banLore.add(ChatColor.GRAY + "Click to ban a player");
        banMeta.setLore(banLore);
        banItem.setItemMeta(banMeta);

        ItemStack kickItem = new ItemStack(Material.IRON_AXE);
        ItemMeta kickMeta = kickItem.getItemMeta();
        kickMeta.setDisplayName(ChatColor.YELLOW + "Kick Player");
        List<String> kickLore = new ArrayList<>();
        kickLore.add(ChatColor.GRAY + "Click to kick a player");
        kickMeta.setLore(kickLore);
        kickItem.setItemMeta(kickMeta);

        ItemStack invItem = new ItemStack(Material.CHEST);
        ItemMeta invMeta = invItem.getItemMeta();
        invMeta.setDisplayName(ChatColor.GREEN + "Inspect Inventory");
        List<String> invLore = new ArrayList<>();
        invLore.add(ChatColor.GRAY + "Click to inspect a player's inventory");
        invMeta.setLore(invLore);
        invItem.setItemMeta(invMeta);

        ItemStack reportItem = new ItemStack(Material.BOOK);
        ItemMeta reportMeta = reportItem.getItemMeta();
        reportMeta.setDisplayName(ChatColor.BLUE + "View Reports");
        List<String> reportLore = new ArrayList<>();
        reportLore.add(ChatColor.GRAY + "Click to view player reports");
        reportMeta.setLore(reportLore);
        reportItem.setItemMeta(reportMeta);

        ItemStack vanishItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta vanishMeta = vanishItem.getItemMeta();
        vanishMeta.setDisplayName(ChatColor.LIGHT_PURPLE + (isVanished(staff) ? "Unvanish" : "Vanish"));
        List<String> vanishLore = new ArrayList<>();
        vanishLore.add(ChatColor.GRAY + "Click to toggle vanish mode");
        vanishMeta.setLore(vanishLore);
        vanishItem.setItemMeta(vanishMeta);

        ItemStack teleportItem = new ItemStack(Material.COMPASS);
        ItemMeta teleportMeta = teleportItem.getItemMeta();
        teleportMeta.setDisplayName(ChatColor.AQUA + "Teleport to Player");
        List<String> teleportLore = new ArrayList<>();
        teleportLore.add(ChatColor.GRAY + "Click to teleport to a player");
        teleportMeta.setLore(teleportLore);
        teleportItem.setItemMeta(teleportMeta);

        // Place items in the GUI
        gui.setItem(9, banItem);
        gui.setItem(11, kickItem);
        gui.setItem(13, invItem);
        gui.setItem(15, reportItem);
        gui.setItem(17, vanishItem);
        gui.setItem(19, teleportItem);

        staff.openInventory(gui);
    }

    // Open the reports GUI
    private void openReportsGUI(Player staff) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "Player Reports");
        List<ReportManager.Report> reports = plugin.getReportManager().getReports();

        int slot = 0;
        for (ReportManager.Report report : reports) {
            if (slot >= 54) break;
            ItemStack reportItem = new ItemStack(Material.PAPER);
            ItemMeta meta = reportItem.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Report #" + report.getId());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Reporter: " + report.getReporter());
            lore.add(ChatColor.GRAY + "Target: " + report.getTarget());
            lore.add(ChatColor.GRAY + "Reason: " + report.getReason());
            lore.add(ChatColor.GRAY + "Time: " + report.getTimestamp());
            lore.add(ChatColor.GREEN + "Click to resolve");
            meta.setLore(lore);
            reportItem.setItemMeta(meta);
            gui.setItem(slot++, reportItem);
        }

        staff.openInventory(gui);
    }

    // Ban a player
    public void banPlayer(Player staff, String targetName, String reason) {
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            target.kickPlayer("You have been banned: " + reason);
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(targetName, reason, null, staff.getName());
            staff.sendMessage(ChatColor.GREEN + "Banned " + targetName + " for: " + reason);
            // Send punishment notification to Discord
            plugin.getDiscordManager().sendPunishmentNotification(staff.getName(), "banned", targetName, reason);
        } else {
            staff.sendMessage(ChatColor.RED + "Player " + targetName + " not found!");
        }
    }

    // Kick a player
    public void kickPlayer(Player staff, String targetName, String reason) {
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            target.kickPlayer("You have been kicked: " + reason);
            staff.sendMessage(ChatColor.GREEN + "Kicked " + targetName + " for: " + reason);
            // Send punishment notification to Discord
            plugin.getDiscordManager().sendPunishmentNotification(staff.getName(), "kicked", targetName, reason);
        } else {
            staff.sendMessage(ChatColor.RED + "Player " + targetName + " not found!");
        }
    }

    // Open another player's inventory
    public void openPlayerInventory(Player staff, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            staff.openInventory(target.getInventory());
            staff.sendMessage(ChatColor.GREEN + "Opened inventory of " + targetName);
            // Send punishment notification to Discord
            plugin.getDiscordManager().sendPunishmentNotification(staff.getName(), "inspected inventory of", targetName, "Inventory inspection");
        } else {
            staff.sendMessage(ChatColor.RED + "Player " + targetName + " not found!");
        }
    }

    // Toggle vanish for a staff member
    public void toggleVanish(Player staff) {
        UUID staffUUID = staff.getUniqueId();
        boolean isVanishing = !vanishedPlayers.contains(staffUUID);

        if (isVanishing) {
            // Vanish: Hide from non-staff and send fake disconnect
            vanishedPlayers.add(staffUUID);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("ecore.staff")) {
                    online.hidePlayer(plugin, staff);
                }
            }
            Bukkit.broadcastMessage(ChatColor.YELLOW + staff.getName() + " has left the game");
            staff.sendMessage(ChatColor.GREEN + "You are now vanished!");
            // Send vanish notification to Discord
            plugin.getDiscordManager().sendPunishmentNotification(staff.getName(), "vanished", staff.getName(), "Entered vanish mode");
        } else {
            // Unvanish: Show to all players, send fake join message, and clear vanish state
            vanishedPlayers.remove(staffUUID);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("ecore.staff")) {
                    online.showPlayer(plugin, staff);
                }
            }
            Bukkit.broadcastMessage(ChatColor.YELLOW + staff.getName() + " has joined the game");
            staff.sendMessage(ChatColor.GREEN + "You are no longer vanished!");
            // Send unvanish notification to Discord
            plugin.getDiscordManager().sendPunishmentNotification(staff.getName(), "unvanished", staff.getName(), "Exited vanish mode");
        }

        // Reopen the GUI to update the Vanish/Unvanish button
        openStaffGUI(staff);
    }

    // Check if a player is vanished
    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    // Teleport to a player
    public void teleportToPlayer(Player staff, String target) {
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            staff.teleport(targetPlayer.getLocation());
            staff.sendMessage(ChatColor.GREEN + "Teleported to " + target + "!");
            // Send teleport notification to Discord
            plugin.getDiscordManager().sendPunishmentNotification(staff.getName(), "teleported to", target, "Teleport action");
        } else {
            staff.sendMessage(ChatColor.RED + "Player " + target + " not found!");
        }
    }

    // Handle inventory click events for staff GUI
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.equals(ChatColor.DARK_RED + "Staff Menu") &&
                !title.equals(ChatColor.DARK_RED + "Player Reports")) {
            return;
        }

        event.setCancelled(true); // Prevent item movement
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();

        // Handle main staff menu clicks
        if (title.equals(ChatColor.DARK_RED + "Staff Menu")) {
            if (displayName.equals(ChatColor.RED + "Ban Player")) {
                player.closeInventory();
                plugin.registerPendingAction(player, "punish:ban");
                player.sendMessage(ChatColor.YELLOW + "Please type the player's name to ban.");
            } else if (displayName.equals(ChatColor.YELLOW + "Kick Player")) {
                player.closeInventory();
                plugin.registerPendingAction(player, "punish:kick");
                player.sendMessage(ChatColor.YELLOW + "Please type the player's name to kick.");
            } else if (displayName.equals(ChatColor.GREEN + "Inspect Inventory")) {
                player.closeInventory();
                plugin.registerPendingAction(player, "punish:inspect");
                player.sendMessage(ChatColor.YELLOW + "Please type the player's name to inspect.");
            } else if (displayName.equals(ChatColor.BLUE + "View Reports")) {
                player.closeInventory();
                openReportsGUI(player);
            } else if (displayName.equals(ChatColor.LIGHT_PURPLE + "Vanish") || displayName.equals(ChatColor.LIGHT_PURPLE + "Unvanish")) {
                if (!player.hasPermission("ecore.vanish")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to vanish!");
                    player.closeInventory();
                    return;
                }
                toggleVanish(player); // Toggle vanish and reopen GUI
            } else if (displayName.equals(ChatColor.AQUA + "Teleport to Player")) {
                if (!player.hasPermission("ecore.teleport")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to teleport!");
                    player.closeInventory();
                    return;
                }
                player.closeInventory();
                plugin.registerPendingAction(player, "teleport");
                player.sendMessage(ChatColor.YELLOW + "Please type the player's name to teleport to.");
            }
        }
        // Handle reports GUI clicks
        else if (title.equals(ChatColor.DARK_RED + "Player Reports")) {
            if (displayName.startsWith(ChatColor.YELLOW + "Report #")) {
                int reportId = Integer.parseInt(displayName.replace(ChatColor.YELLOW + "Report #", ""));
                plugin.getReportManager().resolveReport(reportId);
                player.sendMessage(ChatColor.GREEN + "Report #" + reportId + " resolved!");
                player.closeInventory();
                openReportsGUI(player); // Refresh GUI
            }
        }
    }
}