package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * NPC System Manager
 * Creates NPCs that can give quests, run shops, and interact with players
 */
public class NPCManager implements Listener {
    private final Ecore plugin;
    private File npcsFile;
    private FileConfiguration npcsConfig;
    private final Map<String, NPC> npcs;
    private final Map<org.bukkit.entity.Entity, String> npcEntities;
    
    public NPCManager(Ecore plugin) {
        this.plugin = plugin;
        this.npcs = new HashMap<>();
        this.npcEntities = new HashMap<>();
        initializeConfig();
        loadNPCs();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void initializeConfig() {
        npcsFile = new File(plugin.getDataFolder(), "npcs.yml");
        if (!npcsFile.exists()) {
            try {
                npcsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create npcs.yml", e);
            }
        }
        npcsConfig = YamlConfiguration.loadConfiguration(npcsFile);
    }
    
    private void loadNPCs() {
        if (npcsConfig.contains("npcs")) {
            for (String id : npcsConfig.getConfigurationSection("npcs").getKeys(false)) {
                String path = "npcs." + id;
                Location loc = deserializeLocation(npcsConfig.getString(path + ".location"));
                String name = npcsConfig.getString(path + ".name");
                String type = npcsConfig.getString(path + ".type", "villager");
                List<String> dialogue = npcsConfig.getStringList(path + ".dialogue");
                String questId = npcsConfig.getString(path + ".quest-id", null);
                String shopId = npcsConfig.getString(path + ".shop-id", null);
                String command = npcsConfig.getString(path + ".command", null);
                String permission = npcsConfig.getString(path + ".permission", null);
                
                NPC npc = new NPC(id, loc, name, type, dialogue, questId, shopId, command, permission);
                npcs.put(id, npc);
                spawnNPC(npc);
            }
        }
    }
    
    /**
     * Create an NPC
     */
    public void createNPC(String id, Location location, String name, String type, List<String> dialogue, String questId, String shopId, String command, String permission) {
        NPC npc = new NPC(id, location, name, type, dialogue, questId, shopId, command, permission);
        npcs.put(id, npc);
        spawnNPC(npc);
        saveNPC(npc);
    }
    
    /**
     * Delete an NPC
     */
    public void deleteNPC(String id) {
        NPC npc = npcs.remove(id);
        if (npc != null) {
            // Find and remove entity
            for (Map.Entry<org.bukkit.entity.Entity, String> entry : npcEntities.entrySet()) {
                if (entry.getValue().equals(id)) {
                    entry.getKey().remove();
                    npcEntities.remove(entry.getKey());
                    break;
                }
            }
            npcsConfig.set("npcs." + id, null);
            saveConfig();
        }
    }
    
    private void spawnNPC(NPC npc) {
        Location loc = npc.getLocation();
        org.bukkit.entity.Entity entity;
        
        if (npc.getType().equalsIgnoreCase("villager")) {
            Villager villager = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
            villager.setCustomName(org.bukkit.ChatColor.translateAlternateColorCodes('&', npc.getName()));
            villager.setCustomNameVisible(true);
            villager.setAI(false);
            villager.setInvulnerable(true);
            entity = villager;
        } else {
            // Default to villager
            Villager villager = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
            villager.setCustomName(org.bukkit.ChatColor.translateAlternateColorCodes('&', npc.getName()));
            villager.setCustomNameVisible(true);
            villager.setAI(false);
            villager.setInvulnerable(true);
            entity = villager;
        }
        
        npcEntities.put(entity, npc.getId());
    }
    
    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        org.bukkit.entity.Entity entity = event.getRightClicked();
        String npcId = npcEntities.get(entity);
        
        if (npcId == null) return;
        
        NPC npc = npcs.get(npcId);
        if (npc == null) return;
        
        Player player = event.getPlayer();
        
        // Check permission
        if (npc.getPermission() != null && !player.hasPermission(npc.getPermission())) {
            player.sendMessage(org.bukkit.ChatColor.RED + "You don't have permission to interact with this NPC!");
            return;
        }
        
        // Show dialogue
        if (!npc.getDialogue().isEmpty()) {
            for (String line : npc.getDialogue()) {
                player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        
        // Handle quest
        if (npc.getQuestId() != null) {
            plugin.getQuestManager().startQuest(player, npc.getQuestId());
        }
        
        // Handle shop
        if (npc.getShopId() != null) {
            // Open shop GUI if available
            if (plugin.getGUIShopManager() != null && plugin.getGUIShopManager().isEnabled()) {
                plugin.getGUIShopManager().openShopGUI(player);
            }
        }
        
        // Execute command
        if (npc.getCommand() != null) {
            String command = npc.getCommand().replace("%player%", player.getName());
            plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
    
    private void saveNPC(NPC npc) {
        String path = "npcs." + npc.getId();
        npcsConfig.set(path + ".location", serializeLocation(npc.getLocation()));
        npcsConfig.set(path + ".name", npc.getName());
        npcsConfig.set(path + ".type", npc.getType());
        npcsConfig.set(path + ".dialogue", npc.getDialogue());
        if (npc.getQuestId() != null) npcsConfig.set(path + ".quest-id", npc.getQuestId());
        if (npc.getShopId() != null) npcsConfig.set(path + ".shop-id", npc.getShopId());
        if (npc.getCommand() != null) npcsConfig.set(path + ".command", npc.getCommand());
        if (npc.getPermission() != null) npcsConfig.set(path + ".permission", npc.getPermission());
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
            npcsConfig.save(npcsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save npcs.yml", e);
        }
    }
    
    public NPC getNPC(String id) {
        return npcs.get(id);
    }
    
    public Collection<NPC> getNPCs() {
        return npcs.values();
    }
    
    public static class NPC {
        private String id;
        private Location location;
        private String name;
        private String type;
        private List<String> dialogue;
        private String questId;
        private String shopId;
        private String command;
        private String permission;
        
        public NPC(String id, Location location, String name, String type, List<String> dialogue, 
                  String questId, String shopId, String command, String permission) {
            this.id = id;
            this.location = location;
            this.name = name;
            this.type = type;
            this.dialogue = dialogue;
            this.questId = questId;
            this.shopId = shopId;
            this.command = command;
            this.permission = permission;
        }
        
        public String getId() { return id; }
        public Location getLocation() { return location; }
        public String getName() { return name; }
        public String getType() { return type; }
        public List<String> getDialogue() { return dialogue; }
        public String getQuestId() { return questId; }
        public String getShopId() { return shopId; }
        public String getCommand() { return command; }
        public String getPermission() { return permission; }
    }
}

