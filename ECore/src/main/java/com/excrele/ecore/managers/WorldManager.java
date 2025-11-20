package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages multi-world functionality similar to MultiverseCore.
 * Handles world creation, loading, unloading, modification, and teleportation.
 * 
 * @author Excrele
 * @version 1.0
 */
public class WorldManager {
    private final Ecore plugin;
    private File worldsFile;
    private FileConfiguration worldsConfig;
    private final Map<String, WorldProperties> worldProperties;

    /**
     * Creates a new WorldManager instance.
     * 
     * @param plugin The Ecore plugin instance
     */
    public WorldManager(Ecore plugin) {
        this.plugin = plugin;
        this.worldProperties = new HashMap<>();
        initializeWorldsFile();
        loadWorlds();
    }

    /**
     * Initializes the worlds configuration file.
     */
    private void initializeWorldsFile() {
        worldsFile = new File(plugin.getDataFolder(), "worlds.yml");
        if (!worldsFile.exists()) {
            try {
                worldsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create worlds.yml: " + e.getMessage());
            }
        }
        worldsConfig = YamlConfiguration.loadConfiguration(worldsFile);
    }

    /**
     * Loads all world properties from the configuration file.
     */
    public void loadWorlds() {
        worldProperties.clear();
        if (!worldsConfig.contains("worlds")) {
            return;
        }

        for (String worldName : worldsConfig.getConfigurationSection("worlds").getKeys(false)) {
            String path = "worlds." + worldName;
            WorldProperties props = new WorldProperties();
            props.setName(worldName);
            props.setType(WorldType.valueOf(worldsConfig.getString(path + ".type", "NORMAL").toUpperCase()));
            props.setEnvironment(Environment.valueOf(worldsConfig.getString(path + ".environment", "NORMAL").toUpperCase()));
            props.setSeed(worldsConfig.getLong(path + ".seed", 0));
            props.setGenerateStructures(worldsConfig.getBoolean(path + ".generateStructures", true));
            props.setGenerator(worldsConfig.getString(path + ".generator", null));
            props.setGeneratorSettings(worldsConfig.getString(path + ".generatorSettings", null));
            props.setSpawnLocation(worldsConfig.getLocation(path + ".spawn", null));
            props.setDifficulty(Difficulty.valueOf(worldsConfig.getString(path + ".difficulty", "NORMAL").toUpperCase()));
            props.setPvp(worldsConfig.getBoolean(path + ".pvp", true));
            props.setAutoLoad(worldsConfig.getBoolean(path + ".autoLoad", true));
            props.setKeepSpawnInMemory(worldsConfig.getBoolean(path + ".keepSpawnInMemory", true));
            props.setRespawnWorld(worldsConfig.getBoolean(path + ".respawnWorld", false));
            
            worldProperties.put(worldName.toLowerCase(), props);
        }
    }

    /**
     * Saves all world properties to the configuration file.
     */
    private void saveWorlds() {
        try {
            worldsConfig.save(worldsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save worlds.yml: " + e.getMessage());
        }
    }

    /**
     * Creates a new world with the specified properties.
     * 
     * @param name The world name
     * @param type The world type (NORMAL, FLAT, LARGE_BIOMES, etc.)
     * @param environment The environment (NORMAL, NETHER, THE_END)
     * @param seed The world seed (0 for random)
     * @param generator The generator class name (null for default)
     * @param generatorSettings The generator settings (null for default)
     * @return The created World, or null if creation failed
     */
    public World createWorld(String name, WorldType type, Environment environment, long seed, 
                            String generator, String generatorSettings) {
        if (worldExists(name)) {
            plugin.getLogger().warning("World '" + name + "' already exists!");
            return null;
        }

        WorldCreator creator = new WorldCreator(name);
        creator.type(type);
        creator.environment(environment);
        if (seed != 0) {
            creator.seed(seed);
        }
        if (generator != null && !generator.isEmpty()) {
            try {
                Class<?> generatorClass = Class.forName(generator);
                creator.generator((org.bukkit.generator.ChunkGenerator) generatorClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load generator '" + generator + "': " + e.getMessage());
            }
        }
        if (generatorSettings != null && !generatorSettings.isEmpty()) {
            creator.generatorSettings(generatorSettings);
        }

        try {
            World world = creator.createWorld();
            if (world == null) {
                plugin.getLogger().severe("Failed to create world '" + name + "'!");
                return null;
            }

            // Save world properties
            WorldProperties props = new WorldProperties();
            props.setName(name);
            props.setType(type);
            props.setEnvironment(environment);
            props.setSeed(seed);
            props.setGenerateStructures(true);
            props.setGenerator(generator);
            props.setGeneratorSettings(generatorSettings);
            props.setSpawnLocation(world.getSpawnLocation());
            props.setDifficulty(world.getDifficulty());
            props.setPvp(world.getPVP());
            props.setAutoLoad(true);
            props.setKeepSpawnInMemory(true);
            props.setRespawnWorld(false);

            worldProperties.put(name.toLowerCase(), props);
            saveWorldProperties(props);
            
            plugin.getLogger().info("Successfully created world '" + name + "'!");
            return world;
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating world '" + name + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a world that exists on disk but is not currently loaded.
     * 
     * @param name The world name
     * @return The loaded World, or null if loading failed
     */
    public World loadWorld(String name) {
        if (isWorldLoaded(name)) {
            return Bukkit.getWorld(name);
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), name);
        if (!worldFolder.exists()) {
            plugin.getLogger().warning("World folder '" + name + "' does not exist!");
            return null;
        }

        WorldProperties props = worldProperties.get(name.toLowerCase());
        if (props == null) {
            // Load default properties
            props = new WorldProperties();
            props.setName(name);
            props.setType(WorldType.NORMAL);
            props.setEnvironment(Environment.NORMAL);
            props.setAutoLoad(true);
        }

        WorldCreator creator = new WorldCreator(name);
        creator.type(props.getType());
        creator.environment(props.getEnvironment());
        if (props.getSeed() != 0) {
            creator.seed(props.getSeed());
        }
        if (props.getGenerator() != null && !props.getGenerator().isEmpty()) {
            try {
                Class<?> generatorClass = Class.forName(props.getGenerator());
                creator.generator((org.bukkit.generator.ChunkGenerator) generatorClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load generator for world '" + name + "': " + e.getMessage());
            }
        }
        if (props.getGeneratorSettings() != null && !props.getGeneratorSettings().isEmpty()) {
            creator.generatorSettings(props.getGeneratorSettings());
        }

        try {
            World world = creator.createWorld();
            if (world == null) {
                plugin.getLogger().severe("Failed to load world '" + name + "'!");
                return null;
            }

            // Apply saved properties
            if (props.getSpawnLocation() != null) {
                world.setSpawnLocation(props.getSpawnLocation());
            }
            world.setDifficulty(props.getDifficulty());
            world.setPVP(props.isPvp());
            // setKeepSpawnInMemory is deprecated in 1.20.5+, but we'll keep it for compatibility
            try {
                world.setKeepSpawnInMemory(props.isKeepSpawnInMemory());
            } catch (NoSuchMethodError e) {
                // Method doesn't exist in newer versions, ignore
            }

            plugin.getLogger().info("Successfully loaded world '" + name + "'!");
            return world;
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading world '" + name + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Unloads a world from memory.
     * 
     * @param name The world name
     * @param save Whether to save the world before unloading
     * @return true if the world was unloaded successfully
     */
    public boolean unloadWorld(String name, boolean save) {
        World world = Bukkit.getWorld(name);
        if (world == null) {
            plugin.getLogger().warning("World '" + name + "' is not loaded!");
            return false;
        }

        // Teleport all players out of the world
        List<Player> players = new ArrayList<>(world.getPlayers());
        World defaultWorld = Bukkit.getWorlds().get(0);
        for (Player player : players) {
            if (defaultWorld != null) {
                plugin.getTeleportManager().teleport(player, defaultWorld.getSpawnLocation());
                player.sendMessage("§eYou were teleported out of world '" + name + "' as it was being unloaded.");
            }
        }

        boolean unloaded = Bukkit.unloadWorld(world, save);
        if (unloaded) {
            plugin.getLogger().info("Successfully unloaded world '" + name + "'!");
        } else {
            plugin.getLogger().warning("Failed to unload world '" + name + "'!");
        }
        return unloaded;
    }

    /**
     * Deletes a world from disk.
     * WARNING: This is a destructive operation!
     * 
     * @param name The world name
     * @return true if the world was deleted successfully
     */
    public boolean deleteWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            // Unload first
            if (!unloadWorld(name, false)) {
                return false;
            }
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), name);
        if (!worldFolder.exists()) {
            plugin.getLogger().warning("World folder '" + name + "' does not exist!");
            return false;
        }

        // Remove from config
        worldProperties.remove(name.toLowerCase());
        worldsConfig.set("worlds." + name, null);
        saveWorlds();

        // Delete folder
        try {
            deleteDirectory(worldFolder);
            plugin.getLogger().info("Successfully deleted world '" + name + "'!");
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to delete world '" + name + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Recursively deletes a directory.
     */
    private void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        throw new IOException("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }

        if (!directory.delete()) {
            throw new IOException("Failed to delete directory: " + directory.getAbsolutePath());
        }
    }

    /**
     * Checks if a world exists (on disk or loaded).
     * 
     * @param name The world name
     * @return true if the world exists
     */
    public boolean worldExists(String name) {
        if (isWorldLoaded(name)) {
            return true;
        }
        File worldFolder = new File(Bukkit.getWorldContainer(), name);
        return worldFolder.exists();
    }

    /**
     * Checks if a world is currently loaded.
     * 
     * @param name The world name
     * @return true if the world is loaded
     */
    public boolean isWorldLoaded(String name) {
        return Bukkit.getWorld(name) != null;
    }

    /**
     * Gets a list of all world names (loaded and unloaded).
     * 
     * @return List of world names
     */
    public List<String> getAllWorlds() {
        Set<String> worlds = new HashSet<>();
        
        // Add loaded worlds
        for (World world : Bukkit.getWorlds()) {
            worlds.add(world.getName());
        }
        
        // Add worlds from config
        worlds.addAll(worldProperties.keySet());
        
        // Add worlds from disk
        File worldContainer = Bukkit.getWorldContainer();
        File[] files = worldContainer.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File levelDat = new File(file, "level.dat");
                    if (levelDat.exists()) {
                        worlds.add(file.getName());
                    }
                }
            }
        }
        
        return new ArrayList<>(worlds);
    }

    /**
     * Gets a list of currently loaded world names.
     * 
     * @return List of loaded world names
     */
    public List<String> getLoadedWorlds() {
        List<String> worlds = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            worlds.add(world.getName());
        }
        return worlds;
    }

    /**
     * Teleports a player to a world.
     * 
     * @param player The player to teleport
     * @param worldName The world name
     * @return true if teleportation was successful
     */
    public boolean teleportToWorld(Player player, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            // Try to load the world
            world = loadWorld(worldName);
            if (world == null) {
                player.sendMessage("§cWorld '" + worldName + "' does not exist or could not be loaded!");
                return false;
            }
        }

        Location spawn = world.getSpawnLocation();
        WorldProperties props = worldProperties.get(worldName.toLowerCase());
        if (props != null && props.getSpawnLocation() != null) {
            spawn = props.getSpawnLocation();
        }

        // Find safe spawn location
        spawn = findSafeLocation(spawn);
        plugin.getTeleportManager().teleport(player, spawn);
        player.sendMessage("§aTeleported to world: " + worldName);
        return true;
    }

    /**
     * Finds a safe location near the given location.
     */
    private Location findSafeLocation(Location loc) {
        World world = loc.getWorld();
        if (world == null) return loc;

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        // Check if current location is safe
        if (isSafeLocation(world, x, y, z)) {
            return new Location(world, x + 0.5, y, z + 0.5, loc.getYaw(), loc.getPitch());
        }

        // Search for safe location nearby
        for (int radius = 1; radius <= 10; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                        int checkX = x + dx;
                        int checkZ = z + dz;
                        int checkY = world.getHighestBlockYAt(checkX, checkZ);
                        
                        if (isSafeLocation(world, checkX, checkY, checkZ)) {
                            return new Location(world, checkX + 0.5, checkY + 1, checkZ + 0.5, loc.getYaw(), loc.getPitch());
                        }
                    }
                }
            }
        }

        // Fallback to spawn location
        return world.getSpawnLocation();
    }

    /**
     * Checks if a location is safe for teleportation.
     */
    private boolean isSafeLocation(World world, int x, int y, int z) {
        Material block = world.getBlockAt(x, y, z).getType();
        Material above = world.getBlockAt(x, y + 1, z).getType();
        Material below = world.getBlockAt(x, y - 1, z).getType();

        return block == Material.AIR && 
               above == Material.AIR && 
               !below.isAir() && 
               !below.equals(Material.LAVA) &&
               !below.equals(Material.MAGMA_BLOCK);
    }

    /**
     * Updates world properties.
     */
    public void updateWorldProperties(String worldName, WorldProperties props) {
        worldProperties.put(worldName.toLowerCase(), props);
        saveWorldProperties(props);
    }

    /**
     * Gets world properties.
     */
    public WorldProperties getWorldProperties(String worldName) {
        return worldProperties.get(worldName.toLowerCase());
    }

    /**
     * Saves world properties to config.
     */
    private void saveWorldProperties(WorldProperties props) {
        String path = "worlds." + props.getName();
        worldsConfig.set(path + ".type", props.getType().name());
        worldsConfig.set(path + ".environment", props.getEnvironment().name());
        worldsConfig.set(path + ".seed", props.getSeed());
        worldsConfig.set(path + ".generateStructures", props.isGenerateStructures());
        if (props.getGenerator() != null) {
            worldsConfig.set(path + ".generator", props.getGenerator());
        }
        if (props.getGeneratorSettings() != null) {
            worldsConfig.set(path + ".generatorSettings", props.getGeneratorSettings());
        }
        if (props.getSpawnLocation() != null) {
            worldsConfig.set(path + ".spawn", props.getSpawnLocation());
        }
        worldsConfig.set(path + ".difficulty", props.getDifficulty().name());
        worldsConfig.set(path + ".pvp", props.isPvp());
        worldsConfig.set(path + ".autoLoad", props.isAutoLoad());
        worldsConfig.set(path + ".keepSpawnInMemory", props.isKeepSpawnInMemory());
        worldsConfig.set(path + ".respawnWorld", props.isRespawnWorld());
        saveWorlds();
    }

    /**
     * Sets the spawn location for a world.
     */
    public void setWorldSpawn(String worldName, Location location) {
        WorldProperties props = worldProperties.get(worldName.toLowerCase());
        if (props == null) {
            props = new WorldProperties();
            props.setName(worldName);
            worldProperties.put(worldName.toLowerCase(), props);
        }
        props.setSpawnLocation(location);
        saveWorldProperties(props);
        
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            world.setSpawnLocation(location);
        }
    }

    /**
     * World properties data class.
     */
    public static class WorldProperties {
        private String name;
        private WorldType type;
        private Environment environment;
        private long seed;
        private boolean generateStructures;
        private String generator;
        private String generatorSettings;
        private Location spawnLocation;
        private Difficulty difficulty;
        private boolean pvp;
        private boolean autoLoad;
        private boolean keepSpawnInMemory;
        private boolean respawnWorld;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public WorldType getType() { return type; }
        public void setType(WorldType type) { this.type = type; }
        
        public Environment getEnvironment() { return environment; }
        public void setEnvironment(Environment environment) { this.environment = environment; }
        
        public long getSeed() { return seed; }
        public void setSeed(long seed) { this.seed = seed; }
        
        public boolean isGenerateStructures() { return generateStructures; }
        public void setGenerateStructures(boolean generateStructures) { this.generateStructures = generateStructures; }
        
        public String getGenerator() { return generator; }
        public void setGenerator(String generator) { this.generator = generator; }
        
        public String getGeneratorSettings() { return generatorSettings; }
        public void setGeneratorSettings(String generatorSettings) { this.generatorSettings = generatorSettings; }
        
        public Location getSpawnLocation() { return spawnLocation; }
        public void setSpawnLocation(Location spawnLocation) { this.spawnLocation = spawnLocation; }
        
        public Difficulty getDifficulty() { return difficulty; }
        public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
        
        public boolean isPvp() { return pvp; }
        public void setPvp(boolean pvp) { this.pvp = pvp; }
        
        public boolean isAutoLoad() { return autoLoad; }
        public void setAutoLoad(boolean autoLoad) { this.autoLoad = autoLoad; }
        
        public boolean isKeepSpawnInMemory() { return keepSpawnInMemory; }
        public void setKeepSpawnInMemory(boolean keepSpawnInMemory) { this.keepSpawnInMemory = keepSpawnInMemory; }
        
        public boolean isRespawnWorld() { return respawnWorld; }
        public void setRespawnWorld(boolean respawnWorld) { this.respawnWorld = respawnWorld; }
    }
}

