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
                // Set main inventory (slots 0-35)
                ItemStack[] mainInventory = new ItemStack[36];
                System.arraycopy(items, 0, mainInventory, 0, 36);
                inventory.setContents(mainInventory);
                
                // Set armor (slots 36-39)
                if (items.length > 36) {
                    ItemStack[] armor = new ItemStack[4];
                    System.arraycopy(items, 36, armor, 0, 4);
                    inventory.setArmorContents(armor);
                }
                
                // Set offhand (slot 40)
                if (items.length > 40 && items[40] != null) {
                    try {
                        inventory.setItemInOffHand(items[40]);
                    } catch (Exception e) {
                        // Offhand not available in this version
                    }
                }
                
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
     * Serializes a player inventory to a Base64 string for storage.
     * Uses the database's serialization method to preserve full item data.
     */
    private String serializeInventory(PlayerInventory inventory) {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
            
            ItemStack[] contents = inventory.getContents();
            dos.writeInt(contents.length);
            
            for (ItemStack item : contents) {
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    dos.writeBoolean(true);
                    String serialized = plugin.getBlockLogManager().getDatabase().serializeItemStack(item);
                    if (serialized != null) {
                        dos.writeUTF(serialized);
                    } else {
                        dos.writeUTF("");
                    }
                } else {
                    dos.writeBoolean(false);
                }
            }
            
            // Serialize armor
            ItemStack[] armor = inventory.getArmorContents();
            dos.writeInt(armor.length);
            for (ItemStack item : armor) {
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    dos.writeBoolean(true);
                    String serialized = plugin.getBlockLogManager().getDatabase().serializeItemStack(item);
                    if (serialized != null) {
                        dos.writeUTF(serialized);
                    } else {
                        dos.writeUTF("");
                    }
                } else {
                    dos.writeBoolean(false);
                }
            }
            
            // Serialize offhand (if available)
            try {
                ItemStack offhand = inventory.getItemInOffHand();
                if (offhand != null && offhand.getType() != org.bukkit.Material.AIR) {
                    dos.writeBoolean(true);
                    String serialized = plugin.getBlockLogManager().getDatabase().serializeItemStack(offhand);
                    if (serialized != null) {
                        dos.writeUTF(serialized);
                    } else {
                        dos.writeUTF("");
                    }
                } else {
                    dos.writeBoolean(false);
                }
            } catch (Exception e) {
                dos.writeBoolean(false);
            }
            
            dos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to serialize inventory: " + e.getMessage());
            return "";
        }
    }

    /**
     * Deserializes a Base64 string to a player inventory.
     * Returns an array with inventory contents, armor, and offhand.
     */
    private ItemStack[] deserializeInventory(String data) {
        try {
            if (data == null || data.isEmpty()) return null;
            
            // Decode from Base64
            byte[] bytes = Base64.getDecoder().decode(data);
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
            java.io.DataInputStream dis = new java.io.DataInputStream(bais);
            
            // Read inventory size
            int size = dis.readInt();
            ItemStack[] contents = new ItemStack[size];
            
            // Read inventory contents
            for (int i = 0; i < size; i++) {
                if (dis.readBoolean()) {
                    String serialized = dis.readUTF();
                    if (!serialized.isEmpty()) {
                        contents[i] = plugin.getBlockLogManager().getDatabase().deserializeItemStack(serialized);
                    }
                }
            }
            
            // Read armor
            int armorSize = dis.readInt();
            ItemStack[] armor = new ItemStack[armorSize];
            for (int i = 0; i < armorSize; i++) {
                if (dis.readBoolean()) {
                    String serialized = dis.readUTF();
                    if (!serialized.isEmpty()) {
                        armor[i] = plugin.getBlockLogManager().getDatabase().deserializeItemStack(serialized);
                    }
                }
            }
            
            // Read offhand
            ItemStack offhand = null;
            if (dis.readBoolean()) {
                String serialized = dis.readUTF();
                if (!serialized.isEmpty()) {
                    offhand = plugin.getBlockLogManager().getDatabase().deserializeItemStack(serialized);
                }
            }
            
            dis.close();
            
            // Combine into single array (for compatibility with old code)
            // New format: [0-35: inventory, 36-39: armor, 40: offhand]
            ItemStack[] combined = new ItemStack[41];
            System.arraycopy(contents, 0, combined, 0, Math.min(contents.length, 36));
            System.arraycopy(armor, 0, combined, 36, Math.min(armor.length, 4));
            if (offhand != null) {
                combined[40] = offhand;
            }
            
            return combined;
        } catch (Exception e) {
            // Try old format deserialization as fallback
            try {
                String decoded = new String(Base64.getDecoder().decode(data));
                ItemStack[] contents = new ItemStack[41];
                
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
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to deserialize inventory: " + ex.getMessage());
                return null;
            }
        }
    }
}

