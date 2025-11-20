package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.database.BlockLogDatabase;
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

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * GUI manager for block logging system.
 * Provides easy-to-use interfaces for browsing logs and performing rollbacks.
 */
public class BlockLogGUIManager implements Listener {
    private final Ecore plugin;
    private final Map<UUID, String> openGUIs; // Player UUID -> GUI type
    private final Map<UUID, Object> guiData; // Player UUID -> GUI data (for pagination, etc.)

    public BlockLogGUIManager(Ecore plugin) {
        this.plugin = plugin;
        this.openGUIs = new HashMap<>();
        this.guiData = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Opens the main block log GUI.
     */
    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Block Logging");

        // Lookup button
        ItemStack lookup = createMenuItem(Material.BOOK, ChatColor.GREEN + "Lookup Logs", 
                Arrays.asList(ChatColor.GRAY + "View block logs for players", 
                             ChatColor.GRAY + "Click to open lookup menu"));
        gui.setItem(10, lookup);

        // Rollback button
        ItemStack rollback = createMenuItem(Material.ANVIL, ChatColor.RED + "Rollback Blocks", 
                Arrays.asList(ChatColor.GRAY + "Rollback player block changes", 
                             ChatColor.GRAY + "Click to open rollback menu"));
        gui.setItem(12, rollback);

        // Inspector button
        ItemStack inspect = createMenuItem(Material.WOODEN_AXE, ChatColor.GOLD + "Inspector Tool", 
                Arrays.asList(ChatColor.GRAY + "Get inspector wand", 
                             ChatColor.GRAY + "Right-click blocks to inspect"));
        gui.setItem(14, inspect);

        // Inventory Rollback button
        ItemStack inventory = createMenuItem(Material.CHEST, ChatColor.BLUE + "Inventory Rollback", 
                Arrays.asList(ChatColor.GRAY + "Rollback player inventories", 
                             ChatColor.GRAY + "Click to open inventory menu"));
        gui.setItem(16, inventory);

        // Close button
        ItemStack close = createMenuItem(Material.BARRIER, ChatColor.RED + "Close", 
                Collections.singletonList(ChatColor.GRAY + "Close this menu"));
        gui.setItem(22, close);

        player.openInventory(gui);
        openGUIs.put(player.getUniqueId(), "main");
    }

    /**
     * Opens the lookup GUI for a specific player.
     */
    public void openLookupGUI(Player player, UUID targetUuid, String targetName, long timeRange) {
        List<BlockLogDatabase.BlockLogEntry> logs = plugin.getBlockLogManager()
                .getPlayerBlockLogs(targetUuid, timeRange, 45);

        int size = Math.min(54, Math.max(9, ((logs.size() + 8) / 9) * 9));
        Inventory gui = Bukkit.createInventory(null, size, 
                ChatColor.DARK_GREEN + "Logs: " + targetName);

        for (int i = 0; i < logs.size() && i < size - 1; i++) {
            BlockLogDatabase.BlockLogEntry log = logs.get(i);
            ItemStack item = createLogItem(log);
            gui.setItem(i, item);
        }

        // Back button
        ItemStack back = createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back", 
                Collections.singletonList(ChatColor.GRAY + "Return to main menu"));
        gui.setItem(size - 1, back);

        player.openInventory(gui);
        openGUIs.put(player.getUniqueId(), "lookup");
        guiData.put(player.getUniqueId(), new LookupData(targetUuid, targetName, timeRange, 0));
    }

    /**
     * Opens the inventory rollback GUI for a specific player.
     */
    public void openInventoryRollbackGUI(Player player, UUID targetUuid, String targetName, long timeRange) {
        List<BlockLogDatabase.InventorySnapshot> snapshots = plugin.getInventoryLogManager()
                .getInventorySnapshots(targetUuid, timeRange, 45);

        int size = Math.min(54, Math.max(9, ((snapshots.size() + 8) / 9) * 9));
        Inventory gui = Bukkit.createInventory(null, size, 
                ChatColor.DARK_BLUE + "Inventory: " + targetName);

        for (int i = 0; i < snapshots.size() && i < size - 1; i++) {
            BlockLogDatabase.InventorySnapshot snapshot = snapshots.get(i);
            ItemStack item = createSnapshotItem(snapshot);
            gui.setItem(i, item);
        }

        // Back button
        ItemStack back = createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back", 
                Collections.singletonList(ChatColor.GRAY + "Return to main menu"));
        gui.setItem(size - 1, back);

        player.openInventory(gui);
        openGUIs.put(player.getUniqueId(), "inventory");
        guiData.put(player.getUniqueId(), new InventoryData(targetUuid, targetName, timeRange));
    }

    /**
     * Creates a menu item with display name and lore.
     */
    private ItemStack createMenuItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates an item representing a block log entry.
     */
    private ItemStack createLogItem(BlockLogDatabase.BlockLogEntry log) {
        Material material = log.material != null ? Material.valueOf(log.material) : Material.BARRIER;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String actionColor = log.action.equals("BREAK") ? ChatColor.RED.toString() : ChatColor.GREEN.toString();
        meta.setDisplayName(actionColor + log.action + " " + (log.material != null ? log.material : "Unknown"));
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Player: " + ChatColor.WHITE + log.playerName);
        lore.add(ChatColor.GRAY + "Time: " + ChatColor.WHITE + sdf.format(new Date(log.time)));
        lore.add(ChatColor.GRAY + "Location: " + ChatColor.WHITE + 
                log.x + ", " + log.y + ", " + log.z);
        lore.add(ChatColor.GRAY + "World: " + ChatColor.WHITE + log.world);
        
        if (log.action.equals("BREAK")) {
            lore.add(ChatColor.YELLOW + "Right-click to restore this block");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates an item representing an inventory snapshot.
     */
    private ItemStack createSnapshotItem(BlockLogDatabase.InventorySnapshot snapshot) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        meta.setDisplayName(ChatColor.GREEN + "Snapshot: " + sdf.format(new Date(snapshot.time)));
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Player: " + ChatColor.WHITE + snapshot.playerName);
        lore.add(ChatColor.GRAY + "Time: " + ChatColor.WHITE + sdf.format(new Date(snapshot.time)));
        lore.add(ChatColor.YELLOW + "Click to rollback to this snapshot");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String guiType = openGUIs.get(player.getUniqueId());
        
        if (guiType == null) return;
        
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (guiType) {
            case "main":
                handleMainGUIClick(player, clicked);
                break;
            case "lookup":
                handleLookupGUIClick(player, clicked, event.getSlot());
                break;
            case "inventory":
                handleInventoryGUIClick(player, clicked, event.getSlot());
                break;
        }
    }

    private void handleMainGUIClick(Player player, ItemStack clicked) {
        String displayName = clicked.getItemMeta().getDisplayName();
        
        if (displayName.contains("Lookup")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Please enter a player name in chat:");
            plugin.registerPendingAction(player, "blocklog:lookup:player");
        } else if (displayName.contains("Rollback")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Please enter a player name in chat:");
            plugin.registerPendingAction(player, "blocklog:rollback:player");
        } else if (displayName.contains("Inspector")) {
            // Give inspector wand
            ItemStack wand = new ItemStack(Material.WOODEN_AXE);
            ItemMeta meta = wand.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Block Inspector");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Right-click a block to inspect");
            lore.add(ChatColor.GRAY + "its history");
            meta.setLore(lore);
            wand.setItemMeta(meta);
            player.getInventory().addItem(wand);
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Inspector wand given!");
        } else if (displayName.contains("Inventory")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Please enter a player name in chat:");
            plugin.registerPendingAction(player, "blocklog:inventory:player");
        } else if (displayName.contains("Close")) {
            player.closeInventory();
        }
    }

    private void handleLookupGUIClick(Player player, ItemStack clicked, int slot) {
        if (clicked.getType() == Material.ARROW) {
            openMainGUI(player);
            return;
        }

        // For now, clicking on a log entry shows details
        LookupData data = (LookupData) guiData.get(player.getUniqueId());
        if (data != null) {
            List<BlockLogDatabase.BlockLogEntry> logs = plugin.getBlockLogManager()
                    .getPlayerBlockLogs(data.targetUuid, data.timeRange, 45);
            
            if (slot < logs.size()) {
                BlockLogDatabase.BlockLogEntry log = logs.get(slot);
                player.sendMessage(ChatColor.GOLD + "=== Block Log Entry ===");
                player.sendMessage(ChatColor.GRAY + "Action: " + ChatColor.WHITE + log.action);
                player.sendMessage(ChatColor.GRAY + "Player: " + ChatColor.WHITE + log.playerName);
                player.sendMessage(ChatColor.GRAY + "Location: " + ChatColor.WHITE + 
                        log.x + ", " + log.y + ", " + log.z);
                player.sendMessage(ChatColor.GRAY + "World: " + ChatColor.WHITE + log.world);
                if (log.material != null) {
                    player.sendMessage(ChatColor.GRAY + "Material: " + ChatColor.WHITE + log.material);
                }
            }
        }
    }

    private void handleInventoryGUIClick(Player player, ItemStack clicked, int slot) {
        if (clicked.getType() == Material.ARROW) {
            openMainGUI(player);
            return;
        }

        InventoryData data = (InventoryData) guiData.get(player.getUniqueId());
        if (data != null) {
            List<BlockLogDatabase.InventorySnapshot> snapshots = plugin.getInventoryLogManager()
                    .getInventorySnapshots(data.targetUuid, data.timeRange, 45);
            
            if (slot < snapshots.size()) {
                BlockLogDatabase.InventorySnapshot snapshot = snapshots.get(slot);
                Player target = Bukkit.getPlayer(data.targetUuid);
                
                if (target != null && target.isOnline()) {
                    plugin.getInventoryLogManager().rollbackInventory(target, snapshot);
                    player.sendMessage(ChatColor.GREEN + "Inventory rolled back for " + target.getName() + "!");
                    player.closeInventory();
                } else {
                    player.sendMessage(ChatColor.RED + "Player is not online!");
                }
            }
        }
    }

    /**
     * Data class for lookup GUI state.
     */
    private static class LookupData {
        final UUID targetUuid;
        final String targetName;
        final long timeRange;
        final int page;

        LookupData(UUID targetUuid, String targetName, long timeRange, int page) {
            this.targetUuid = targetUuid;
            this.targetName = targetName;
            this.timeRange = timeRange;
            this.page = page;
        }
    }

    /**
     * Data class for inventory GUI state.
     */
    private static class InventoryData {
        final UUID targetUuid;
        final String targetName;
        final long timeRange;

        InventoryData(UUID targetUuid, String targetName, long timeRange) {
            this.targetUuid = targetUuid;
            this.targetName = targetName;
            this.timeRange = timeRange;
        }
    }
}

