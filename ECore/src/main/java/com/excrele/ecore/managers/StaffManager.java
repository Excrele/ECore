package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
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
    private final Map<UUID, Boolean> frozenPlayers;
    private final Map<UUID, Boolean> commandSpyEnabled;
    private final Map<UUID, Boolean> socialSpyEnabled;

    public StaffManager(Ecore plugin) {
        this.plugin = plugin;
        this.vanishedPlayers = new HashMap<>();
        this.frozenPlayers = new HashMap<>();
        this.commandSpyEnabled = new HashMap<>();
        this.socialSpyEnabled = new HashMap<>();
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

    // Freeze player
    public void freezePlayer(Player player) {
        frozenPlayers.put(player.getUniqueId(), true);
        player.sendMessage("§cYou have been frozen! Do not move.");
        plugin.getDiscordManager().sendStaffLogNotification(
            "punishment-log",
            "Staff",
            "froze",
            player.getName(),
            ""
        );
    }

    public void unfreezePlayer(Player player) {
        frozenPlayers.remove(player.getUniqueId());
        player.sendMessage("§aYou have been unfrozen!");
    }

    public boolean isFrozen(Player player) {
        return frozenPlayers.getOrDefault(player.getUniqueId(), false);
    }

    // Command spy
    public void setCommandSpyEnabled(Player player, boolean enabled) {
        if (enabled) {
            commandSpyEnabled.put(player.getUniqueId(), true);
            player.sendMessage(ChatColor.GREEN + "Command spy enabled!");
        } else {
            commandSpyEnabled.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Command spy disabled!");
        }
    }

    public boolean isCommandSpyEnabled(Player player) {
        return commandSpyEnabled.getOrDefault(player.getUniqueId(), false);
    }

    // Social spy
    public void setSocialSpyEnabled(Player player, boolean enabled) {
        if (enabled) {
            socialSpyEnabled.put(player.getUniqueId(), true);
            player.sendMessage(ChatColor.GREEN + "Social spy enabled!");
        } else {
            socialSpyEnabled.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Social spy disabled!");
        }
    }

    public boolean isSocialSpyEnabled(Player player) {
        return socialSpyEnabled.getOrDefault(player.getUniqueId(), false);
    }

    // Item commands
    public void giveItem(Player target, String itemName, int amount) {
        org.bukkit.Material material = org.bukkit.Material.matchMaterial(itemName.toUpperCase());
        if (material == null) {
            return;
        }
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material, amount);
        target.getInventory().addItem(item);
    }

    public void enchantItem(Player target, String enchantName, int level) {
        org.bukkit.enchantments.Enchantment enchant = org.bukkit.enchantments.Enchantment.getByName(enchantName.toUpperCase());
        if (enchant == null) {
            return;
        }
        org.bukkit.inventory.ItemStack item = target.getInventory().getItemInMainHand();
        if (item == null || item.getType() == org.bukkit.Material.AIR) {
            return;
        }
        item.addUnsafeEnchantment(enchant, level);
    }

    public void repairItem(Player target, boolean all) {
        if (all) {
            for (org.bukkit.inventory.ItemStack item : target.getInventory().getContents()) {
                if (item != null && item.getType().getMaxDurability() > 0) {
                    item.setDurability((short) 0);
                }
            }
            for (org.bukkit.inventory.ItemStack item : target.getInventory().getArmorContents()) {
                if (item != null && item.getType().getMaxDurability() > 0) {
                    item.setDurability((short) 0);
                }
            }
        } else {
            org.bukkit.inventory.ItemStack item = target.getInventory().getItemInMainHand();
            if (item != null && item.getType().getMaxDurability() > 0) {
                item.setDurability((short) 0);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        if (title.equals(ChatColor.DARK_GREEN + "Staff Management")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            if (clicked.getType() == Material.BOOK) {
                // View Reports
                openReportsGUI(player);
            } else if (clicked.getType() == Material.REDSTONE_BLOCK) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Enter the player name to ban:");
                plugin.registerPendingAction(player, "staff:ban");
            } else if (clicked.getType() == Material.IRON_BOOTS) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Enter the player name to kick:");
                plugin.registerPendingAction(player, "staff:kick");
            } else if (clicked.getType() == Material.CHEST) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Enter the player name to inspect:");
                plugin.registerPendingAction(player, "staff:inspect");
            }
        } else if (title.startsWith(ChatColor.DARK_RED + "Reports")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.PAPER) return;

            String reportId = clicked.getItemMeta().getLore().get(0).replace(ChatColor.GRAY + "ID: ", "");
            openReportDetailGUI(player, reportId);
        } else if (title.startsWith(ChatColor.DARK_RED + "Report:")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            String reportId = title.replace(ChatColor.DARK_RED + "Report: ", "");
            if (clicked.getType() == Material.GREEN_CONCRETE) {
                // Resolve
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Enter resolution notes (or 'none' for no notes):");
                plugin.registerPendingAction(player, "report:resolve:" + reportId);
            } else if (clicked.getType() == Material.ARROW) {
                openReportsGUI(player);
            }
        }
    }

    public void openReportsGUI(Player player) {
        java.util.Set<String> reportIds = plugin.getReportManager().getReportIds();
        int unresolvedCount = 0;
        for (String id : reportIds) {
            if (!plugin.getReportManager().isResolved(id)) {
                unresolvedCount++;
            }
        }

        int size = Math.max(9, ((unresolvedCount + 8) / 9) * 9);
        size = Math.min(size, 54);
        Inventory gui = Bukkit.createInventory(player, size, ChatColor.DARK_RED + "Reports (" + unresolvedCount + ")");

        int slot = 0;
        for (String reportId : reportIds) {
            if (plugin.getReportManager().isResolved(reportId)) continue;
            if (slot >= size) break;

            ItemStack report = new ItemStack(Material.PAPER);
            ItemMeta meta = report.getItemMeta();
            meta.setDisplayName(ChatColor.RED + plugin.getReportManager().getTarget(reportId));
            meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "ID: " + reportId,
                ChatColor.YELLOW + "Reporter: " + ChatColor.WHITE + plugin.getReportManager().getReporter(reportId),
                ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + plugin.getReportManager().getReason(reportId),
                ChatColor.GRAY + "Click to view details"
            ));
            report.setItemMeta(meta);
            gui.setItem(slot++, report);
        }

        player.openInventory(gui);
    }

    public void openReportDetailGUI(Player player, String reportId) {
        Inventory gui = Bukkit.createInventory(player, 9, ChatColor.DARK_RED + "Report: " + reportId);

        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GREEN + "Report Information");
        infoMeta.setLore(java.util.Arrays.asList(
            ChatColor.YELLOW + "Reporter: " + ChatColor.WHITE + plugin.getReportManager().getReporter(reportId),
            ChatColor.YELLOW + "Target: " + ChatColor.WHITE + plugin.getReportManager().getTarget(reportId),
            ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + plugin.getReportManager().getReason(reportId),
            ChatColor.GRAY + "Time: " + new java.util.Date(plugin.getReportManager().getTimestamp(reportId))
        ));
        info.setItemMeta(infoMeta);
        gui.setItem(4, info);

        ItemStack resolve = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta resolveMeta = resolve.getItemMeta();
        resolveMeta.setDisplayName(ChatColor.GREEN + "Resolve Report");
        resolve.setItemMeta(resolveMeta);
        gui.setItem(6, resolve);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Back");
        back.setItemMeta(backMeta);
        gui.setItem(8, back);

        player.openInventory(gui);
    }
}