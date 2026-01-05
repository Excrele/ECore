package com.excrele.ecore.database;

import com.excrele.ecore.Ecore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Database manager for block logging system.
 * Handles all database operations for block and inventory logging.
 */
public class BlockLogDatabase {
    private final Ecore plugin;
    private Connection connection;
    private final String databaseType;

    public BlockLogDatabase(Ecore plugin) {
        this.plugin = plugin;
        this.databaseType = plugin.getConfigManager().getConfig().getString("block-logging.database-type", "sqlite").toLowerCase();
        initializeDatabase();
    }

    /**
     * Initializes the database connection and creates tables if they don't exist.
     */
    private void initializeDatabase() {
        try {
            if (databaseType.equals("mysql")) {
                String host = plugin.getConfigManager().getConfig().getString("block-logging.mysql.host", "localhost");
                int port = plugin.getConfigManager().getConfig().getInt("block-logging.mysql.port", 3306);
                String database = plugin.getConfigManager().getConfig().getString("block-logging.mysql.database", "ecore");
                String username = plugin.getConfigManager().getConfig().getString("block-logging.mysql.username", "root");
                String password = plugin.getConfigManager().getConfig().getString("block-logging.mysql.password", "");
                
                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";
                connection = DriverManager.getConnection(url, username, password);
            } else {
                // SQLite (default)
                String dbPath = plugin.getDataFolder().getAbsolutePath() + "/blocklog.db";
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            }

            createTables();
            plugin.getLogger().info("Block logging database initialized (" + databaseType + ")");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize block logging database", e);
        }
    }

    /**
     * Creates all necessary tables if they don't exist.
     */
    private void createTables() throws SQLException {
        // Block logs table
        String blockLogsTable = "CREATE TABLE IF NOT EXISTS block_logs (" +
                "id INTEGER PRIMARY KEY " + (databaseType.equals("mysql") ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
                "time BIGINT NOT NULL," +
                "player_uuid VARCHAR(36) NOT NULL," +
                "player_name VARCHAR(16) NOT NULL," +
                "action VARCHAR(20) NOT NULL," +
                "world VARCHAR(255) NOT NULL," +
                "x INTEGER NOT NULL," +
                "y INTEGER NOT NULL," +
                "z INTEGER NOT NULL," +
                "material VARCHAR(50)," +
                "data TEXT" +
                ")";
        
        // Container logs table
        String containerLogsTable = "CREATE TABLE IF NOT EXISTS container_logs (" +
                "id INTEGER PRIMARY KEY " + (databaseType.equals("mysql") ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
                "time BIGINT NOT NULL," +
                "player_uuid VARCHAR(36) NOT NULL," +
                "player_name VARCHAR(16) NOT NULL," +
                "action VARCHAR(20) NOT NULL," +
                "world VARCHAR(255) NOT NULL," +
                "x INTEGER NOT NULL," +
                "y INTEGER NOT NULL," +
                "z INTEGER NOT NULL," +
                "slot INTEGER," +
                "item_type VARCHAR(50)," +
                "item_amount INTEGER," +
                "item_data TEXT" +
                ")";
        
        // Inventory logs table
        String inventoryLogsTable = "CREATE TABLE IF NOT EXISTS inventory_logs (" +
                "id INTEGER PRIMARY KEY " + (databaseType.equals("mysql") ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
                "time BIGINT NOT NULL," +
                "player_uuid VARCHAR(36) NOT NULL," +
                "player_name VARCHAR(16) NOT NULL," +
                "action VARCHAR(20) NOT NULL," +
                "slot INTEGER," +
                "item_type VARCHAR(50)," +
                "item_amount INTEGER," +
                "item_data TEXT," +
                "inventory_type VARCHAR(50)" +
                ")";
        
        // Inventory snapshots table (for rollback)
        String inventorySnapshotsTable = "CREATE TABLE IF NOT EXISTS inventory_snapshots (" +
                "id INTEGER PRIMARY KEY " + (databaseType.equals("mysql") ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
                "time BIGINT NOT NULL," +
                "player_uuid VARCHAR(36) NOT NULL," +
                "player_name VARCHAR(16) NOT NULL," +
                "inventory_data TEXT NOT NULL" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(blockLogsTable);
            stmt.execute(containerLogsTable);
            stmt.execute(inventoryLogsTable);
            stmt.execute(inventorySnapshotsTable);
            
            // Create indexes for better query performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_block_logs_time ON block_logs(time)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_block_logs_player ON block_logs(player_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_block_logs_location ON block_logs(world, x, y, z)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_container_logs_time ON container_logs(time)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_container_logs_player ON container_logs(player_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_inventory_logs_time ON inventory_logs(time)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_inventory_logs_player ON inventory_logs(player_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_inventory_snapshots_time ON inventory_snapshots(time)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_inventory_snapshots_player ON inventory_snapshots(player_uuid)");
        }
    }

    /**
     * Logs a block action (place, break, etc.)
     */
    public void logBlockAction(UUID playerUuid, String playerName, String action, Location location, Material material, String data) {
        if (connection == null) return;
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO block_logs (time, player_uuid, player_name, action, world, x, y, z, material, data) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setLong(1, System.currentTimeMillis());
                stmt.setString(2, playerUuid.toString());
                stmt.setString(3, playerName);
                stmt.setString(4, action);
                stmt.setString(5, location.getWorld().getName());
                stmt.setInt(6, location.getBlockX());
                stmt.setInt(7, location.getBlockY());
                stmt.setInt(8, location.getBlockZ());
                stmt.setString(9, material != null ? material.name() : null);
                stmt.setString(10, data);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to log block action", e);
            }
        });
    }

    /**
     * Logs a container action (open, take, place, etc.)
     */
    public void logContainerAction(UUID playerUuid, String playerName, String action, Location location, int slot, ItemStack item) {
        if (connection == null) return;
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO container_logs (time, player_uuid, player_name, action, world, x, y, z, slot, item_type, item_amount, item_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setLong(1, System.currentTimeMillis());
                stmt.setString(2, playerUuid.toString());
                stmt.setString(3, playerName);
                stmt.setString(4, action);
                stmt.setString(5, location.getWorld().getName());
                stmt.setInt(6, location.getBlockX());
                stmt.setInt(7, location.getBlockY());
                stmt.setInt(8, location.getBlockZ());
                stmt.setInt(9, slot);
                stmt.setString(10, item != null ? item.getType().name() : null);
                stmt.setInt(11, item != null ? item.getAmount() : 0);
                stmt.setString(12, item != null ? serializeItemStack(item) : null);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to log container action", e);
            }
        });
    }

    /**
     * Logs an inventory action (take, place, drop, etc.)
     */
    public void logInventoryAction(UUID playerUuid, String playerName, String action, int slot, ItemStack item, String inventoryType) {
        if (connection == null) return;
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO inventory_logs (time, player_uuid, player_name, action, slot, item_type, item_amount, item_data, inventory_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setLong(1, System.currentTimeMillis());
                stmt.setString(2, playerUuid.toString());
                stmt.setString(3, playerName);
                stmt.setString(4, action);
                stmt.setInt(5, slot);
                stmt.setString(6, item != null ? item.getType().name() : null);
                stmt.setInt(7, item != null ? item.getAmount() : 0);
                stmt.setString(8, item != null ? serializeItemStack(item) : null);
                stmt.setString(9, inventoryType);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to log inventory action", e);
            }
        });
    }

    /**
     * Saves an inventory snapshot for rollback purposes.
     */
    public void saveInventorySnapshot(UUID playerUuid, String playerName, String inventoryData) {
        if (connection == null) return;
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO inventory_snapshots (time, player_uuid, player_name, inventory_data) VALUES (?, ?, ?, ?)")) {
                stmt.setLong(1, System.currentTimeMillis());
                stmt.setString(2, playerUuid.toString());
                stmt.setString(3, playerName);
                stmt.setString(4, inventoryData);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save inventory snapshot", e);
            }
        });
    }

    /**
     * Gets block logs for a specific location.
     */
    public List<BlockLogEntry> getBlockLogs(Location location, long timeRange) {
        List<BlockLogEntry> logs = new ArrayList<>();
        if (connection == null) return logs;
        
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM block_logs WHERE world = ? AND x = ? AND y = ? AND z = ? AND time >= ? ORDER BY time DESC")) {
            stmt.setString(1, location.getWorld().getName());
            stmt.setInt(2, location.getBlockX());
            stmt.setInt(3, location.getBlockY());
            stmt.setInt(4, location.getBlockZ());
            stmt.setLong(5, System.currentTimeMillis() - timeRange);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(new BlockLogEntry(
                        rs.getLong("time"),
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("player_name"),
                        rs.getString("action"),
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getString("material"),
                        rs.getString("data")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get block logs", e);
        }
        
        return logs;
    }

    /**
     * Gets block logs for a specific player.
     */
    public List<BlockLogEntry> getPlayerBlockLogs(UUID playerUuid, long timeRange, int limit) {
        List<BlockLogEntry> logs = new ArrayList<>();
        if (connection == null) return logs;
        
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM block_logs WHERE player_uuid = ? AND time >= ? ORDER BY time DESC LIMIT ?")) {
            stmt.setString(1, playerUuid.toString());
            stmt.setLong(2, System.currentTimeMillis() - timeRange);
            stmt.setInt(3, limit);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(new BlockLogEntry(
                        rs.getLong("time"),
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("player_name"),
                        rs.getString("action"),
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getString("material"),
                        rs.getString("data")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get player block logs", e);
        }
        
        return logs;
    }

    /**
     * Gets inventory snapshots for a player within a time range.
     */
    public List<InventorySnapshot> getInventorySnapshots(UUID playerUuid, long timeRange, int limit) {
        List<InventorySnapshot> snapshots = new ArrayList<>();
        if (connection == null) return snapshots;
        
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM inventory_snapshots WHERE player_uuid = ? AND time >= ? ORDER BY time DESC LIMIT ?")) {
            stmt.setString(1, playerUuid.toString());
            stmt.setLong(2, System.currentTimeMillis() - timeRange);
            stmt.setInt(3, limit);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                snapshots.add(new InventorySnapshot(
                        rs.getLong("time"),
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("player_name"),
                        rs.getString("inventory_data")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get inventory snapshots", e);
        }
        
        return snapshots;
    }

    /**
     * Purges old logs based on retention days.
     */
    public void purgeOldLogs(int retentionDays) {
        if (connection == null) return;
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            long cutoffTime = System.currentTimeMillis() - (retentionDays * 24L * 60L * 60L * 1000L);
            
            try (Statement stmt = connection.createStatement()) {
                int blockLogsDeleted = stmt.executeUpdate("DELETE FROM block_logs WHERE time < " + cutoffTime);
                int containerLogsDeleted = stmt.executeUpdate("DELETE FROM container_logs WHERE time < " + cutoffTime);
                int inventoryLogsDeleted = stmt.executeUpdate("DELETE FROM inventory_logs WHERE time < " + cutoffTime);
                int snapshotsDeleted = stmt.executeUpdate("DELETE FROM inventory_snapshots WHERE time < " + cutoffTime);
                
                plugin.getLogger().info("Purged old logs: " + blockLogsDeleted + " block logs, " + 
                        containerLogsDeleted + " container logs, " + inventoryLogsDeleted + " inventory logs, " +
                        snapshotsDeleted + " snapshots");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to purge old logs", e);
            }
        });
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to close database connection", e);
        }
    }

    /**
     * Serializes an ItemStack to a Base64 string for storage.
     * Preserves material, amount, durability, enchantments, display name, lore, and other metadata.
     */
    public String serializeItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            // Write basic item data
            dos.writeUTF(item.getType().name());
            dos.writeInt(item.getAmount());
            
            // Write durability if applicable
            if (item.getType().getMaxDurability() > 0) {
                dos.writeShort(item.getDurability());
            } else {
                dos.writeShort(-1);
            }
            
            // Write item meta if present
            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                dos.writeBoolean(true);
                
                // Write display name
                if (meta.hasDisplayName()) {
                    dos.writeBoolean(true);
                    dos.writeUTF(meta.getDisplayName());
                } else {
                    dos.writeBoolean(false);
                }
                
                // Write lore
                if (meta.hasLore()) {
                    dos.writeBoolean(true);
                    dos.writeInt(meta.getLore().size());
                    for (String line : meta.getLore()) {
                        dos.writeUTF(line);
                    }
                } else {
                    dos.writeBoolean(false);
                }
                
                // Write enchantments
                if (meta.hasEnchants()) {
                    dos.writeBoolean(true);
                    Map<Enchantment, Integer> enchants = meta.getEnchants();
                    dos.writeInt(enchants.size());
                    for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                        dos.writeUTF(entry.getKey().getKey().toString());
                        dos.writeInt(entry.getValue());
                    }
                } else {
                    dos.writeBoolean(false);
                }
            } else {
                dos.writeBoolean(false);
            }
            
            dos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to serialize ItemStack", e);
            // Fallback to simple serialization
            return item.getType().name() + ":" + item.getAmount();
        }
    }

    /**
     * Deserializes a Base64 string to an ItemStack.
     */
    public ItemStack deserializeItemStack(String data) {
        if (data == null || data.isEmpty()) return null;
        
        try {
            // Try Base64 deserialization first
            byte[] bytes = Base64.getDecoder().decode(data);
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
            java.io.DataInputStream dis = new java.io.DataInputStream(bais);
            
            String materialName = dis.readUTF();
            int amount = dis.readInt();
            short durability = dis.readShort();
            
            Material material = Material.valueOf(materialName);
            ItemStack item = new ItemStack(material, amount);
            
            if (durability >= 0 && material.getMaxDurability() > 0) {
                item.setDurability(durability);
            }
            
            // Read item meta if present
            if (dis.readBoolean()) {
                ItemMeta meta = item.getItemMeta();
                
                // Read display name
                if (dis.readBoolean()) {
                    meta.setDisplayName(dis.readUTF());
                }
                
                // Read lore
                if (dis.readBoolean()) {
                    int loreSize = dis.readInt();
                    List<String> lore = new ArrayList<>();
                    for (int i = 0; i < loreSize; i++) {
                        lore.add(dis.readUTF());
                    }
                    meta.setLore(lore);
                }
                
                // Read enchantments
                if (dis.readBoolean()) {
                    int enchantCount = dis.readInt();
                    for (int i = 0; i < enchantCount; i++) {
                        String enchantKey = dis.readUTF();
                        int level = dis.readInt();
                        try {
                            NamespacedKey key = NamespacedKey.fromString(enchantKey);
                            Enchantment enchant = Enchantment.getByKey(key);
                            if (enchant != null) {
                                meta.addEnchant(enchant, level, true);
                            }
                        } catch (Exception e) {
                            // Ignore invalid enchantment
                        }
                    }
                }
                
                item.setItemMeta(meta);
            }
            
            dis.close();
            return item;
        } catch (Exception e) {
            // Fallback to simple deserialization
            try {
                String[] parts = data.split(":");
                if (parts.length >= 2) {
                    Material material = Material.valueOf(parts[0]);
                    int amount = Integer.parseInt(parts[1]);
                    return new ItemStack(material, amount);
                }
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Failed to deserialize ItemStack: " + data, ex);
            }
            return null;
        }
    }

    /**
     * Represents a block log entry.
     */
    public static class BlockLogEntry {
        public final long time;
        public final UUID playerUuid;
        public final String playerName;
        public final String action;
        public final String world;
        public final int x, y, z;
        public final String material;
        public final String data;

        public BlockLogEntry(long time, UUID playerUuid, String playerName, String action, 
                           String world, int x, int y, int z, String material, String data) {
            this.time = time;
            this.playerUuid = playerUuid;
            this.playerName = playerName;
            this.action = action;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.material = material;
            this.data = data;
        }
    }

    /**
     * Represents an inventory snapshot.
     */
    public static class InventorySnapshot {
        public final long time;
        public final UUID playerUuid;
        public final String playerName;
        public final String inventoryData;

        public InventorySnapshot(long time, UUID playerUuid, String playerName, String inventoryData) {
            this.time = time;
            this.playerUuid = playerUuid;
            this.playerName = playerName;
            this.inventoryData = inventoryData;
        }
    }
}

