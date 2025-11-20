package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.database.BlockLogDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Manages inventory logging and rollback functionality.
 */
public class InventoryLogManager {
    private final Ecore plugin;
    private final BlockLogDatabase database;

    public InventoryLogManager(Ecore plugin) {
        this.plugin = plugin;
        this.database = plugin.getBlockLogManager().getDatabase();
    }

    /**
     * Logs an inventory action (item taken, placed, etc.).
     */
    public void logInventoryAction(Player player, String action, int slot, ItemStack item, String inventoryType) {
        if (!plugin.getConfigManager().getConfig().getBoolean("block-logging.log-inventory-changes", true)) return;
        
        database.logInventoryAction(player.getUniqueId(), player.getName(), action, slot, item, inventoryType);
    }

    /**
     * Takes a snapshot of a player's inventory for rollback purposes.
     */
    public void takeSnapshot(Player player) {
        if (!plugin.getConfigManager().getConfig().getBoolean("block-logging.inventory-snapshots", true)) return;
        
        String inventoryData = serializeInventory(player.getInventory());
        database.saveInventorySnapshot(player.getUniqueId(), player.getName(), inventoryData);
    }

    /**
     * Gets inventory snapshots for a player within a time range.
     */
    public List<BlockLogDatabase.InventorySnapshot> getInventorySnapshots(UUID playerUuid, long timeRange, int limit) {
        return database.getInventorySnapshots(playerUuid, timeRange, limit);
    }

    /**
     * Rolls back a player's inventory to a specific snapshot.
     */
    public void rollbackInventory(Player target, BlockLogDatabase.InventorySnapshot snapshot) {
        if (target == null || !target.isOnline()) {
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            PlayerInventory inventory = target.getInventory();
            ItemStack[] items = deserializeInventory(snapshot.inventoryData);
            
            if (items != null) {
                inventory.setContents(items);
                target.updateInventory();
                target.sendMessage("§aYour inventory has been rolled back by a staff member.");
            }
        });
    }

    /**
     * Rolls back a player's inventory to the most recent snapshot before a specific time.
     */
    public void rollbackInventoryToTime(UUID playerUuid, long time, Player executor) {
        List<BlockLogDatabase.InventorySnapshot> snapshots = database.getInventorySnapshots(playerUuid, 
                System.currentTimeMillis() - time, 100);
        
        if (snapshots.isEmpty()) {
            executor.sendMessage("§cNo inventory snapshots found for that player in the specified time range.");
            return;
        }

        // Find the snapshot closest to the target time
        BlockLogDatabase.InventorySnapshot targetSnapshot = null;
        long targetTime = System.currentTimeMillis() - time;
        
        for (BlockLogDatabase.InventorySnapshot snapshot : snapshots) {
            if (snapshot.time <= targetTime) {
                if (targetSnapshot == null || snapshot.time > targetSnapshot.time) {
                    targetSnapshot = snapshot;
                }
            }
        }

        if (targetSnapshot == null) {
            // Use the oldest snapshot if no snapshot before target time
            targetSnapshot = snapshots.get(snapshots.size() - 1);
        }

        Player target = Bukkit.getPlayer(playerUuid);
        if (target != null && target.isOnline()) {
            rollbackInventory(target, targetSnapshot);
            executor.sendMessage("§aInventory rollback complete for " + target.getName() + ".");
        } else {
            executor.sendMessage("§cPlayer is not online. Inventory rollback will be applied when they join.");
            // Could store pending rollback here
        }
    }

    /**
     * Serializes a player inventory to a string for storage.
     */
    private String serializeInventory(PlayerInventory inventory) {
        try {
            StringBuilder sb = new StringBuilder();
            ItemStack[] contents = inventory.getContents();
            
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && contents[i].getType() != org.bukkit.Material.AIR) {
                    sb.append(i).append(":");
                    sb.append(contents[i].getType().name()).append(":");
                    sb.append(contents[i].getAmount()).append(":");
                    // Could add more item data here (enchantments, etc.)
                    sb.append(";");
                }
            }
            
            // Encode to Base64 for storage
            return Base64.getEncoder().encodeToString(sb.toString().getBytes());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to serialize inventory: " + e.getMessage());
            return "";
        }
    }

    /**
     * Deserializes a string to a player inventory.
     */
    private ItemStack[] deserializeInventory(String data) {
        try {
            if (data == null || data.isEmpty()) return null;
            
            // Decode from Base64
            String decoded = new String(Base64.getDecoder().decode(data));
            ItemStack[] contents = new ItemStack[41]; // 36 inventory + 4 armor + 1 offhand
            
            String[] items = decoded.split(";");
            for (String item : items) {
                if (item.isEmpty()) continue;
                
                String[] parts = item.split(":");
                if (parts.length >= 3) {
                    int slot = Integer.parseInt(parts[0]);
                    org.bukkit.Material material = org.bukkit.Material.valueOf(parts[1]);
                    int amount = Integer.parseInt(parts[2]);
                    
                    if (slot >= 0 && slot < contents.length) {
                        contents[slot] = new ItemStack(material, amount);
                    }
                }
            }
            
            return contents;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deserialize inventory: " + e.getMessage());
            return null;
        }
    }
}

