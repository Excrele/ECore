package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages WorldEdit-like functionality including:
 * - Selection system (pos1, pos2)
 * - Clipboard operations (copy, paste, cut)
 * - History system (undo, redo)
 * - Block operations (set, replace, clear, walls, hollow)
 * - Schematic save/load
 * - Basic brush system (sphere, cylinder)
 * - Async operations for large builds
 * - Block change limits and progress indicators
 * 
 * @author Excrele
 * @version 1.0
 */
public class WorldEditManager {
    private final Ecore plugin;
    private final Map<UUID, Location> pos1Selections;
    private final Map<UUID, Location> pos2Selections;
    private final Map<UUID, Clipboard> clipboards;
    private final Map<UUID, List<HistoryEntry>> history;
    private final Map<UUID, Integer> historyIndex;
    private final Map<UUID, BukkitTask> activeOperations;
    private File schematicsDir;
    private File historyFile;
    private FileConfiguration historyConfig;
    
    // Configuration values
    private int maxBlockChanges;
    private int blocksPerTick;
    private int maxHistorySize;
    
    /**
     * Represents a clipboard containing copied blocks
     */
    public static class Clipboard {
        private final List<ClipboardBlock> blocks;
        private final Location origin;
        private final int width, height, length;
        
        public Clipboard(Location origin, int width, int height, int length) {
            this.origin = origin.clone();
            this.width = width;
            this.height = height;
            this.length = length;
            this.blocks = new ArrayList<>();
        }
        
        public void addBlock(Location location, Block block) {
            blocks.add(new ClipboardBlock(
                location.getBlockX() - origin.getBlockX(),
                location.getBlockY() - origin.getBlockY(),
                location.getBlockZ() - origin.getBlockZ(),
                block.getType(),
                block.getBlockData()
            ));
        }
        
        public List<ClipboardBlock> getBlocks() {
            return blocks;
        }
        
        public Location getOrigin() {
            return origin;
        }
        
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public int getLength() { return length; }
    }
    
    /**
     * Represents a block in the clipboard
     */
    public static class ClipboardBlock {
        private final int x, y, z;
        private final Material type;
        private final BlockData blockData;
        
        public ClipboardBlock(int x, int y, int z, Material type, BlockData blockData) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.type = type;
            this.blockData = blockData;
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public Material getType() { return type; }
        public BlockData getBlockData() { return blockData; }
    }
    
    /**
     * Represents a history entry for undo/redo
     */
    public static class HistoryEntry {
        private final List<BlockChange> changes;
        private final long timestamp;
        
        public HistoryEntry(List<BlockChange> changes) {
            this.changes = new ArrayList<>(changes);
            this.timestamp = System.currentTimeMillis();
        }
        
        public List<BlockChange> getChanges() {
            return changes;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Represents a single block change
     */
    public static class BlockChange {
        private final Location location;
        private final Material oldType;
        private final BlockData oldData;
        private final Material newType;
        private final BlockData newData;
        
        public BlockChange(Location location, Material oldType, BlockData oldData, Material newType, BlockData newData) {
            this.location = location;
            this.oldType = oldType;
            this.oldData = oldData;
            this.newType = newType;
            this.newData = newData;
        }
        
        public Location getLocation() { return location; }
        public Material getOldType() { return oldType; }
        public BlockData getOldData() { return oldData; }
        public Material getNewType() { return newType; }
        public BlockData getNewData() { return newData; }
    }
    
    public WorldEditManager(Ecore plugin) {
        this.plugin = plugin;
        this.pos1Selections = new ConcurrentHashMap<>();
        this.pos2Selections = new ConcurrentHashMap<>();
        this.clipboards = new ConcurrentHashMap<>();
        this.history = new ConcurrentHashMap<>();
        this.historyIndex = new ConcurrentHashMap<>();
        this.activeOperations = new ConcurrentHashMap<>();
        
        initializeConfig();
        initializeDirectories();
        loadHistory();
    }
    
    private void initializeConfig() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        maxBlockChanges = config.getInt("worldedit.max-block-changes", 100000);
        blocksPerTick = config.getInt("worldedit.blocks-per-tick", 1000);
        maxHistorySize = config.getInt("worldedit.max-history-size", 20);
    }
    
    private void initializeDirectories() {
        schematicsDir = new File(plugin.getDataFolder(), "schematics");
        if (!schematicsDir.exists()) {
            schematicsDir.mkdirs();
        }
        
        historyFile = new File(plugin.getDataFolder(), "worldedit-history.yml");
        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create worldedit-history.yml", e);
            }
        }
        historyConfig = YamlConfiguration.loadConfiguration(historyFile);
    }
    
    private void loadHistory() {
        // Load persistent history if needed
        // For now, history is session-based
    }
    
    /**
     * Sets position 1 for a player's selection
     */
    public boolean setPos1(Player player, Location location) {
        pos1Selections.put(player.getUniqueId(), location);
        player.sendMessage(ChatColor.GREEN + "Position 1 set to (" + 
            location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")");
        return true;
    }
    
    /**
     * Sets position 2 for a player's selection
     */
    public boolean setPos2(Player player, Location location) {
        pos2Selections.put(player.getUniqueId(), location);
        player.sendMessage(ChatColor.GREEN + "Position 2 set to (" + 
            location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")");
        return true;
    }
    
    /**
     * Gets the selection for a player
     */
    public Selection getSelection(Player player) {
        Location pos1 = pos1Selections.get(player.getUniqueId());
        Location pos2 = pos2Selections.get(player.getUniqueId());
        
        if (pos1 == null || pos2 == null) {
            return null;
        }
        
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            return null;
        }
        
        return new Selection(pos1, pos2);
    }
    
    /**
     * Represents a selection between two points
     */
    public static class Selection {
        private final Location min;
        private final Location max;
        private final World world;
        private final int width, height, length;
        private final int volume;
        
        public Selection(Location pos1, Location pos2) {
            int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
            int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
            int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
            int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
            int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
            int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
            
            this.world = pos1.getWorld();
            this.min = new Location(world, minX, minY, minZ);
            this.max = new Location(world, maxX, maxY, maxZ);
            
            this.width = maxX - minX + 1;
            this.height = maxY - minY + 1;
            this.length = maxZ - minZ + 1;
            this.volume = width * height * length;
        }
        
        public Location getMin() { return min; }
        public Location getMax() { return max; }
        public World getWorld() { return world; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public int getLength() { return length; }
        public int getVolume() { return volume; }
        
        public List<Location> getAllBlocks() {
            List<Location> blocks = new ArrayList<>();
            for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                    for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                        blocks.add(new Location(world, x, y, z));
                    }
                }
            }
            return blocks;
        }
    }
    
    /**
     * Fills selection with a block type
     */
    public void setBlocks(Player player, Material material) {
        Selection selection = getSelection(player);
        if (selection == null) {
            player.sendMessage(ChatColor.RED + "You must select two positions first!");
            return;
        }
        
        if (selection.getVolume() > maxBlockChanges) {
            player.sendMessage(ChatColor.RED + "Selection is too large! Maximum: " + maxBlockChanges + " blocks");
            return;
        }
        
        List<Location> blocks = selection.getAllBlocks();
        performAsyncOperation(player, blocks, material, null, true);
    }
    
    /**
     * Replaces blocks in selection
     */
    public void replaceBlocks(Player player, Material from, Material to) {
        Selection selection = getSelection(player);
        if (selection == null) {
            player.sendMessage(ChatColor.RED + "You must select two positions first!");
            return;
        }
        
        List<Location> blocksToReplace = new ArrayList<>();
        for (Location loc : selection.getAllBlocks()) {
            Block block = loc.getBlock();
            if (block.getType() == from) {
                blocksToReplace.add(loc);
            }
        }
        
        if (blocksToReplace.size() > maxBlockChanges) {
            player.sendMessage(ChatColor.RED + "Too many blocks to replace! Maximum: " + maxBlockChanges + " blocks");
            return;
        }
        
        performAsyncOperation(player, blocksToReplace, to, null, true);
    }
    
    /**
     * Clears selection blocks (sets to air)
     */
    public void clearBlocks(Player player) {
        setBlocks(player, Material.AIR);
    }
    
    /**
     * Creates walls in selection
     */
    public void createWalls(Player player, Material material) {
        Selection selection = getSelection(player);
        if (selection == null) {
            player.sendMessage(ChatColor.RED + "You must select two positions first!");
            return;
        }
        
        List<Location> blocks = new ArrayList<>();
        Location min = selection.getMin();
        Location max = selection.getMax();
        World world = selection.getWorld();
        
        // Create walls (all faces except top and bottom)
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                blocks.add(new Location(world, x, y, min.getBlockZ()));
                blocks.add(new Location(world, x, y, max.getBlockZ()));
            }
        }
        
        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                blocks.add(new Location(world, min.getBlockX(), y, z));
                blocks.add(new Location(world, max.getBlockX(), y, z));
            }
        }
        
        if (blocks.size() > maxBlockChanges) {
            player.sendMessage(ChatColor.RED + "Selection is too large! Maximum: " + maxBlockChanges + " blocks");
            return;
        }
        
        performAsyncOperation(player, blocks, material, null, true);
    }
    
    /**
     * Creates hollow box in selection
     */
    public void createHollow(Player player, Material material) {
        Selection selection = getSelection(player);
        if (selection == null) {
            player.sendMessage(ChatColor.RED + "You must select two positions first!");
            return;
        }
        
        List<Location> blocks = new ArrayList<>();
        Location min = selection.getMin();
        Location max = selection.getMax();
        World world = selection.getWorld();
        
        // Create all faces
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                blocks.add(new Location(world, x, y, min.getBlockZ()));
                blocks.add(new Location(world, x, y, max.getBlockZ()));
            }
        }
        
        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                blocks.add(new Location(world, min.getBlockX(), y, z));
                blocks.add(new Location(world, max.getBlockX(), y, z));
            }
        }
        
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                blocks.add(new Location(world, x, min.getBlockY(), z));
                blocks.add(new Location(world, x, max.getBlockY(), z));
            }
        }
        
        if (blocks.size() > maxBlockChanges) {
            player.sendMessage(ChatColor.RED + "Selection is too large! Maximum: " + maxBlockChanges + " blocks");
            return;
        }
        
        performAsyncOperation(player, blocks, material, null, true);
    }
    
    /**
     * Copies selection to clipboard
     */
    public void copySelection(Player player) {
        Selection selection = getSelection(player);
        if (selection == null) {
            player.sendMessage(ChatColor.RED + "You must select two positions first!");
            return;
        }
        
        Clipboard clipboard = new Clipboard(selection.getMin(), selection.getWidth(), selection.getHeight(), selection.getLength());
        
        for (Location loc : selection.getAllBlocks()) {
            Block block = loc.getBlock();
            clipboard.addBlock(loc, block);
        }
        
        clipboards.put(player.getUniqueId(), clipboard);
        player.sendMessage(ChatColor.GREEN + "Copied " + selection.getVolume() + " blocks to clipboard!");
    }
    
    /**
     * Cuts selection to clipboard
     */
    public void cutSelection(Player player) {
        copySelection(player);
        clearSelection(player);
        player.sendMessage(ChatColor.GREEN + "Cut selection to clipboard!");
    }
    
    /**
     * Pastes clipboard at player's location
     */
    public void pasteClipboard(Player player) {
        Clipboard clipboard = clipboards.get(player.getUniqueId());
        if (clipboard == null) {
            player.sendMessage(ChatColor.RED + "Your clipboard is empty!");
            return;
        }
        
        Location pasteLocation = player.getLocation();
        List<Location> blocksToChange = new ArrayList<>();
        List<BlockData> blockDataList = new ArrayList<>();
        
        for (ClipboardBlock cb : clipboard.getBlocks()) {
            Location loc = pasteLocation.clone().add(cb.getX(), cb.getY(), cb.getZ());
            blocksToChange.add(loc);
            blockDataList.add(cb.getBlockData());
        }
        
        if (blocksToChange.size() > maxBlockChanges) {
            player.sendMessage(ChatColor.RED + "Clipboard is too large! Maximum: " + maxBlockChanges + " blocks");
            return;
        }
        
        performAsyncOperation(player, blocksToChange, null, blockDataList, true);
        player.sendMessage(ChatColor.GREEN + "Pasted " + blocksToChange.size() + " blocks!");
    }
    
    /**
     * Creates a sphere brush
     */
    public void createSphere(Player player, Location center, int radius, Material material, boolean hollow) {
        List<Location> blocks = new ArrayList<>();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance <= radius) {
                        if (!hollow || distance >= radius - 0.5) {
                            Location loc = center.clone().add(x, y, z);
                            blocks.add(loc);
                        }
                    }
                }
            }
        }
        
        if (blocks.size() > maxBlockChanges) {
            player.sendMessage(ChatColor.RED + "Sphere is too large! Maximum: " + maxBlockChanges + " blocks");
            return;
        }
        
        performAsyncOperation(player, blocks, material, null, true);
    }
    
    /**
     * Creates a cylinder brush
     */
    public void createCylinder(Player player, Location center, int radius, int height, Material material, boolean hollow) {
        List<Location> blocks = new ArrayList<>();
        
        for (int y = 0; y < height; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + z * z);
                    if (distance <= radius) {
                        if (!hollow || distance >= radius - 0.5) {
                            Location loc = center.clone().add(x, y, z);
                            blocks.add(loc);
                        }
                    }
                }
            }
        }
        
        if (blocks.size() > maxBlockChanges) {
            player.sendMessage(ChatColor.RED + "Cylinder is too large! Maximum: " + maxBlockChanges + " blocks");
            return;
        }
        
        performAsyncOperation(player, blocks, material, null, true);
    }
    
    /**
     * Saves selection as schematic
     */
    public boolean saveSchematic(Player player, String name) {
        Selection selection = getSelection(player);
        if (selection == null) {
            player.sendMessage(ChatColor.RED + "You must select two positions first!");
            return false;
        }
        
        File schematicFile = new File(schematicsDir, name + ".schematic");
        if (schematicFile.exists()) {
            player.sendMessage(ChatColor.RED + "Schematic '" + name + "' already exists!");
            return false;
        }
        
        try {
            FileConfiguration schematicConfig = new YamlConfiguration();
            schematicConfig.set("origin.world", selection.getMin().getWorld().getName());
            schematicConfig.set("origin.x", selection.getMin().getBlockX());
            schematicConfig.set("origin.y", selection.getMin().getBlockY());
            schematicConfig.set("origin.z", selection.getMin().getBlockZ());
            schematicConfig.set("size.width", selection.getWidth());
            schematicConfig.set("size.height", selection.getHeight());
            schematicConfig.set("size.length", selection.getLength());
            
            List<Map<String, Object>> blocks = new ArrayList<>();
            for (Location loc : selection.getAllBlocks()) {
                Block block = loc.getBlock();
                Map<String, Object> blockData = new HashMap<>();
                blockData.put("x", loc.getBlockX() - selection.getMin().getBlockX());
                blockData.put("y", loc.getBlockY() - selection.getMin().getBlockY());
                blockData.put("z", loc.getBlockZ() - selection.getMin().getBlockZ());
                blockData.put("type", block.getType().toString());
                blockData.put("data", block.getBlockData().getAsString());
                blocks.add(blockData);
            }
            
            schematicConfig.set("blocks", blocks);
            schematicConfig.save(schematicFile);
            
            player.sendMessage(ChatColor.GREEN + "Schematic '" + name + "' saved with " + blocks.size() + " blocks!");
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save schematic", e);
            player.sendMessage(ChatColor.RED + "Failed to save schematic!");
            return false;
        }
    }
    
    /**
     * Loads schematic and pastes it at player location
     */
    public boolean loadSchematic(Player player, String name) {
        File schematicFile = new File(schematicsDir, name + ".schematic");
        if (!schematicFile.exists()) {
            player.sendMessage(ChatColor.RED + "Schematic '" + name + "' does not exist!");
            return false;
        }
        
        try {
            FileConfiguration schematicConfig = YamlConfiguration.loadConfiguration(schematicFile);
            
            Location pasteLocation = player.getLocation();
            List<Location> blocksToChange = new ArrayList<>();
            List<BlockData> blockDataList = new ArrayList<>();
            
            List<Map<?, ?>> blocks = schematicConfig.getMapList("blocks");
            for (Map<?, ?> blockData : blocks) {
                int x = (Integer) blockData.get("x");
                int y = (Integer) blockData.get("y");
                int z = (Integer) blockData.get("z");
                String dataStr = (String) blockData.get("data");
                
                BlockData data = Bukkit.createBlockData(dataStr);
                
                Location loc = pasteLocation.clone().add(x, y, z);
                blocksToChange.add(loc);
                blockDataList.add(data);
            }
            
            if (blocksToChange.size() > maxBlockChanges) {
                player.sendMessage(ChatColor.RED + "Schematic is too large! Maximum: " + maxBlockChanges + " blocks");
                return false;
            }
            
            performAsyncOperation(player, blocksToChange, null, blockDataList, true);
            player.sendMessage(ChatColor.GREEN + "Loaded schematic '" + name + "' with " + blocksToChange.size() + " blocks!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load schematic", e);
            player.sendMessage(ChatColor.RED + "Failed to load schematic!");
            return false;
        }
    }
    
    /**
     * Gets list of available schematics
     */
    public List<String> getSchematics() {
        List<String> schematics = new ArrayList<>();
        File[] files = schematicsDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".schematic")) {
                    schematics.add(file.getName().replace(".schematic", ""));
                }
            }
        }
        return schematics;
    }
    
    /**
     * Performs an async block operation with progress tracking
     */
    private void performAsyncOperation(Player player, List<Location> blocks, Material material, 
                                       List<BlockData> blockDataList, boolean saveHistory) {
        UUID uuid = player.getUniqueId();
        
        // Cancel any existing operation
        if (activeOperations.containsKey(uuid)) {
            activeOperations.get(uuid).cancel();
        }
        
        // Record changes for history
        List<BlockChange> changes = new ArrayList<>();
        if (saveHistory) {
            for (int i = 0; i < blocks.size(); i++) {
                Location loc = blocks.get(i);
                Block block = loc.getBlock();
                Material newType = material;
                BlockData newData = blockDataList != null && i < blockDataList.size() ? 
                    blockDataList.get(i) : (material != null ? Bukkit.createBlockData(material) : null);
                
                if (newData != null) {
                    newType = newData.getMaterial();
                }
                
                changes.add(new BlockChange(
                    loc.clone(),
                    block.getType(),
                    block.getBlockData().clone(),
                    newType != null ? newType : block.getType(),
                    newData != null ? newData : block.getBlockData()
                ));
            }
        }
        
        // Create async task
        BukkitTask task = new BukkitRunnable() {
            private int index = 0;
            private final int total = blocks.size();
            
            @Override
            public void run() {
                int processed = 0;
                while (index < total && processed < blocksPerTick) {
                    Location loc = blocks.get(index);
                    Block block = loc.getBlock();
                    
                    if (blockDataList != null && index < blockDataList.size()) {
                        block.setBlockData(blockDataList.get(index));
                    } else if (material != null) {
                        block.setType(material);
                    }
                    
                    index++;
                    processed++;
                }
                
                // Send progress update every 10%
                if (total > 0 && (index % Math.max(1, total / 10) == 0 || index == total)) {
                    int percent = (int) ((index / (double) total) * 100);
                    // Send progress message (less frequent to avoid spam)
                    if (index == total || index % (total / 5) == 0) {
                        player.sendMessage(ChatColor.GREEN + "Progress: " + percent + "% (" + index + "/" + total + ")");
                    }
                }
                
                if (index >= total) {
                    // Operation complete
                    player.sendMessage(ChatColor.GREEN + "Operation complete! Changed " + total + " blocks.");
                    
                    // Save to history
                    if (saveHistory && !changes.isEmpty()) {
                        addToHistory(player, changes);
                    }
                    
                    activeOperations.remove(uuid);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        activeOperations.put(uuid, task);
    }
    
    /**
     * Adds operation to history
     */
    private void addToHistory(Player player, List<BlockChange> changes) {
        UUID uuid = player.getUniqueId();
        List<HistoryEntry> playerHistory = history.computeIfAbsent(uuid, k -> new ArrayList<>());
        int currentIndex = historyIndex.getOrDefault(uuid, -1);
        
        // Remove any entries after current index (for redo)
        if (currentIndex < playerHistory.size() - 1) {
            playerHistory.subList(currentIndex + 1, playerHistory.size()).clear();
        }
        
        // Add new entry
        playerHistory.add(new HistoryEntry(changes));
        
        // Limit history size
        if (playerHistory.size() > maxHistorySize) {
            playerHistory.remove(0);
        } else {
            currentIndex++;
        }
        
        historyIndex.put(uuid, currentIndex);
    }
    
    /**
     * Undoes last operation
     */
    public boolean undo(Player player) {
        UUID uuid = player.getUniqueId();
        List<HistoryEntry> playerHistory = history.get(uuid);
        if (playerHistory == null || playerHistory.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Nothing to undo!");
            return false;
        }
        
        int currentIndex = historyIndex.getOrDefault(uuid, playerHistory.size() - 1);
        if (currentIndex < 0) {
            player.sendMessage(ChatColor.RED + "Nothing to undo!");
            return false;
        }
        
        HistoryEntry entry = playerHistory.get(currentIndex);
        List<Location> blocksToChange = new ArrayList<>();
        List<BlockData> blockDataList = new ArrayList<>();
        
        for (BlockChange change : entry.getChanges()) {
            blocksToChange.add(change.getLocation());
            blockDataList.add(change.getOldData());
        }
        
        // Perform undo operation (without saving to history)
        performAsyncOperation(player, blocksToChange, null, blockDataList, false);
        
        historyIndex.put(uuid, currentIndex - 1);
        player.sendMessage(ChatColor.GREEN + "Undone " + entry.getChanges().size() + " blocks!");
        return true;
    }
    
    /**
     * Redoes last undone operation
     */
    public boolean redo(Player player) {
        UUID uuid = player.getUniqueId();
        List<HistoryEntry> playerHistory = history.get(uuid);
        if (playerHistory == null || playerHistory.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Nothing to redo!");
            return false;
        }
        
        int currentIndex = historyIndex.getOrDefault(uuid, -1);
        if (currentIndex >= playerHistory.size() - 1) {
            player.sendMessage(ChatColor.RED + "Nothing to redo!");
            return false;
        }
        
        currentIndex++;
        HistoryEntry entry = playerHistory.get(currentIndex);
        List<Location> blocksToChange = new ArrayList<>();
        List<BlockData> blockDataList = new ArrayList<>();
        
        for (BlockChange change : entry.getChanges()) {
            blocksToChange.add(change.getLocation());
            blockDataList.add(change.getNewData());
        }
        
        // Perform redo operation (without saving to history)
        performAsyncOperation(player, blocksToChange, null, blockDataList, false);
        
        historyIndex.put(uuid, currentIndex);
        player.sendMessage(ChatColor.GREEN + "Redone " + entry.getChanges().size() + " blocks!");
        return true;
    }
    
    /**
     * Clears selection for a player
     */
    public void clearSelection(Player player) {
        pos1Selections.remove(player.getUniqueId());
        pos2Selections.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Selection cleared!");
    }
    
    /**
     * Gets selection info for a player
     */
    public String getSelectionInfo(Player player) {
        Selection selection = getSelection(player);
        if (selection == null) {
            return ChatColor.RED + "No selection!";
        }
        
        return ChatColor.GREEN + "Selection: " + selection.getVolume() + " blocks (" + 
            selection.getWidth() + "x" + selection.getHeight() + "x" + selection.getLength() + ")";
    }
}

