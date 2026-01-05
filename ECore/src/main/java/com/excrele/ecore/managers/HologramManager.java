package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Hologram System Manager
 * Creates static, dynamic, and interactive holograms
 */
public class HologramManager implements Listener {
    private final Ecore plugin;
    private File hologramsFile;
    private FileConfiguration hologramsConfig;
    private final Map<String, Hologram> holograms;
    private final Map<String, List<ArmorStand>> hologramEntities;
    
    public HologramManager(Ecore plugin) {
        this.plugin = plugin;
        this.holograms = new HashMap<>();
        this.hologramEntities = new HashMap<>();
        initializeConfig();
        loadHolograms();
        startUpdateTask();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void initializeConfig() {
        hologramsFile = new File(plugin.getDataFolder(), "holograms.yml");
        if (!hologramsFile.exists()) {
            try {
                hologramsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create holograms.yml", e);
            }
        }
        hologramsConfig = YamlConfiguration.loadConfiguration(hologramsFile);
    }
    
    private void loadHolograms() {
        if (hologramsConfig.contains("holograms")) {
            for (String id : hologramsConfig.getConfigurationSection("holograms").getKeys(false)) {
                String path = "holograms." + id;
                Location loc = deserializeLocation(hologramsConfig.getString(path + ".location"));
                List<String> lines = hologramsConfig.getStringList(path + ".lines");
                boolean dynamic = hologramsConfig.getBoolean(path + ".dynamic", false);
                boolean interactive = hologramsConfig.getBoolean(path + ".interactive", false);
                String permission = hologramsConfig.getString(path + ".permission", null);
                String command = hologramsConfig.getString(path + ".command", null);
                
                Hologram hologram = new Hologram(id, loc, lines, dynamic, interactive, permission, command);
                holograms.put(id, hologram);
                createHologramEntities(hologram);
            }
        }
    }
    
    private void createHologramEntities(Hologram hologram) {
        List<ArmorStand> entities = new ArrayList<>();
        Location loc = hologram.getLocation().clone();
        
        for (String line : hologram.getLines()) {
            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
            stand.setCustomName(processPlaceholders(null, line));
            stand.setInvulnerable(true);
            stand.setMarker(true);
            entities.add(stand);
            loc.add(0, -0.3, 0); // Move down for next line
        }
        
        hologramEntities.put(hologram.getId(), entities);
    }
    
    /**
     * Create a hologram
     */
    public void createHologram(String id, Location location, List<String> lines, boolean dynamic, boolean interactive, String permission, String command) {
        Hologram hologram = new Hologram(id, location, lines, dynamic, interactive, permission, command);
        holograms.put(id, hologram);
        createHologramEntities(hologram);
        saveHologram(hologram);
    }
    
    /**
     * Delete a hologram
     */
    public void deleteHologram(String id) {
        Hologram hologram = holograms.remove(id);
        if (hologram != null) {
            List<ArmorStand> entities = hologramEntities.remove(id);
            if (entities != null) {
                for (ArmorStand stand : entities) {
                    stand.remove();
                }
            }
            hologramsConfig.set("holograms." + id, null);
            saveConfig();
        }
    }
    
    private void startUpdateTask() {
        // Update dynamic holograms every second
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Hologram hologram : holograms.values()) {
                if (hologram.isDynamic()) {
                    updateHologram(hologram);
                }
            }
        }, 0L, 20L);
    }
    
    private void updateHologram(Hologram hologram) {
        List<ArmorStand> entities = hologramEntities.get(hologram.getId());
        if (entities == null) return;
        
        List<String> lines = hologram.getLines();
        for (int i = 0; i < Math.min(entities.size(), lines.size()); i++) {
            ArmorStand stand = entities.get(i);
            String processedLine = processPlaceholders(null, lines.get(i));
            stand.setCustomName(processedLine);
        }
    }
    
    private String processPlaceholders(Player player, String text) {
        if (text == null) return "";
        
        text = text.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        text = text.replace("%max%", String.valueOf(Bukkit.getMaxPlayers()));
        text = text.replace("%tps%", String.format("%.2f", plugin.getServerInfoManager().getTPS()));
        
        if (player != null) {
            text = text.replace("%player%", player.getName());
            text = text.replace("%balance%", String.format("%.2f", plugin.getEconomyManager().getBalance(player.getUniqueId())));
        }
        
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }
    
    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        ArmorStand stand = (ArmorStand) event.getRightClicked();
        
        // Find which hologram this belongs to
        for (Map.Entry<String, List<ArmorStand>> entry : hologramEntities.entrySet()) {
            if (entry.getValue().contains(stand)) {
                Hologram hologram = holograms.get(entry.getKey());
                if (hologram != null && hologram.isInteractive()) {
                    Player player = event.getPlayer();
                    if (hologram.getPermission() != null && !player.hasPermission(hologram.getPermission())) {
                        player.sendMessage(org.bukkit.ChatColor.RED + "You don't have permission!");
                        return;
                    }
                    if (hologram.getCommand() != null) {
                        plugin.getServer().dispatchCommand(player, hologram.getCommand());
                    }
                }
                break;
            }
        }
    }
    
    private void saveHologram(Hologram hologram) {
        String path = "holograms." + hologram.getId();
        hologramsConfig.set(path + ".location", serializeLocation(hologram.getLocation()));
        hologramsConfig.set(path + ".lines", hologram.getLines());
        hologramsConfig.set(path + ".dynamic", hologram.isDynamic());
        hologramsConfig.set(path + ".interactive", hologram.isInteractive());
        if (hologram.getPermission() != null) hologramsConfig.set(path + ".permission", hologram.getPermission());
        if (hologram.getCommand() != null) hologramsConfig.set(path + ".command", hologram.getCommand());
        saveConfig();
    }
    
    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }
    
    private Location deserializeLocation(String str) {
        String[] parts = str.split(",");
        return new Location(
            Bukkit.getWorld(parts[0]),
            Double.parseDouble(parts[1]),
            Double.parseDouble(parts[2]),
            Double.parseDouble(parts[3])
        );
    }
    
    private void saveConfig() {
        try {
            hologramsConfig.save(hologramsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save holograms.yml", e);
        }
    }
    
    public Hologram getHologram(String id) {
        return holograms.get(id);
    }
    
    public Collection<Hologram> getHolograms() {
        return holograms.values();
    }
    
    public static class Hologram {
        private String id;
        private Location location;
        private List<String> lines;
        private boolean dynamic;
        private boolean interactive;
        private String permission;
        private String command;
        
        public Hologram(String id, Location location, List<String> lines, boolean dynamic, boolean interactive, String permission, String command) {
            this.id = id;
            this.location = location;
            this.lines = lines;
            this.dynamic = dynamic;
            this.interactive = interactive;
            this.permission = permission;
            this.command = command;
        }
        
        public String getId() { return id; }
        public Location getLocation() { return location; }
        public List<String> getLines() { return lines; }
        public boolean isDynamic() { return dynamic; }
        public boolean isInteractive() { return interactive; }
        public String getPermission() { return permission; }
        public String getCommand() { return command; }
    }
}

