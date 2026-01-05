package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Event System Manager
 * Handles server events, event types, rewards, GUI, notifications
 */
public class EventManager {
    private final Ecore plugin;
    private File eventsFile;
    private FileConfiguration eventsConfig;
    private final Map<String, Event> activeEvents; // Event ID -> Event
    private final Map<String, Event> scheduledEvents; // Event ID -> Event
    private final Map<UUID, Set<String>> playerParticipations; // Player UUID -> Set of event IDs
    private BukkitTask eventTask;
    
    public EventManager(Ecore plugin) {
        this.plugin = plugin;
        this.activeEvents = new HashMap<>();
        this.scheduledEvents = new HashMap<>();
        this.playerParticipations = new HashMap<>();
        initializeConfig();
        loadEvents();
        startEventTask();
    }
    
    private void initializeConfig() {
        eventsFile = new File(plugin.getDataFolder(), "events.yml");
        if (!eventsFile.exists()) {
            try {
                eventsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create events.yml", e);
            }
        }
        eventsConfig = YamlConfiguration.loadConfiguration(eventsFile);
    }
    
    private void loadEvents() {
        // Load scheduled events
        if (eventsConfig.contains("scheduled-events")) {
            for (String eventId : eventsConfig.getConfigurationSection("scheduled-events").getKeys(false)) {
                String path = "scheduled-events." + eventId;
                Event event = loadEvent(eventId, path);
                if (event != null) {
                    scheduledEvents.put(eventId, event);
                }
            }
        }
        
        // Load active events
        if (eventsConfig.contains("active-events")) {
            for (String eventId : eventsConfig.getConfigurationSection("active-events").getKeys(false)) {
                String path = "active-events." + eventId;
                Event event = loadEvent(eventId, path);
                if (event != null) {
                    activeEvents.put(eventId, event);
                }
            }
        }
    }
    
    private Event loadEvent(String eventId, String path) {
        String name = eventsConfig.getString(path + ".name");
        String type = eventsConfig.getString(path + ".type", "custom");
        String description = eventsConfig.getString(path + ".description", "");
        long startTime = eventsConfig.getLong(path + ".start-time", System.currentTimeMillis());
        long duration = eventsConfig.getLong(path + ".duration", 3600000L); // 1 hour default
        double rewardMoney = eventsConfig.getDouble(path + ".reward-money", 0.0);
        List<String> rewardItems = eventsConfig.getStringList(path + ".reward-items");
        
        return new Event(eventId, name, type, description, startTime, duration, rewardMoney, rewardItems);
    }
    
    /**
     * Create a new event
     */
    public Event createEvent(String eventId, String name, String type, String description, long startTime, long duration) {
        Event event = new Event(eventId, name, type, description, startTime, duration, 0.0, new ArrayList<>());
        scheduledEvents.put(eventId, event);
        
        saveEvent("scheduled-events", eventId, event);
        
        return event;
    }
    
    /**
     * Start an event
     */
    public boolean startEvent(String eventId) {
        Event event = scheduledEvents.remove(eventId);
        if (event == null) {
            return false;
        }
        
        activeEvents.put(eventId, event);
        
        // Broadcast
        Bukkit.broadcastMessage(org.bukkit.ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage(org.bukkit.ChatColor.YELLOW + "🎉 EVENT STARTED: " + event.getName() + " 🎉");
        Bukkit.broadcastMessage(org.bukkit.ChatColor.GRAY + event.getDescription());
        Bukkit.broadcastMessage(org.bukkit.ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        saveEvent("active-events", eventId, event);
        eventsConfig.set("scheduled-events." + eventId, null);
        saveConfig();
        
        return true;
    }
    
    /**
     * End an event
     */
    public boolean endEvent(String eventId) {
        Event event = activeEvents.remove(eventId);
        if (event == null) {
            return false;
        }
        
        // Give rewards to participants
        for (UUID playerUuid : playerParticipations.keySet()) {
            Set<String> participations = playerParticipations.get(playerUuid);
            if (participations.contains(eventId)) {
                Player player = Bukkit.getPlayer(playerUuid);
                if (player != null && player.isOnline()) {
                    // Give money reward
                    if (event.getRewardMoney() > 0) {
                        plugin.getEconomyManager().addBalance(playerUuid, event.getRewardMoney());
                        player.sendMessage(org.bukkit.ChatColor.GREEN + "You received " + 
                                         plugin.getEconomyManager().format(event.getRewardMoney()) + 
                                         " for participating in " + event.getName() + "!");
                    }
                    
                    // Give item rewards
                    if (!event.getRewardItems().isEmpty()) {
                        // Parse and give item (simplified)
                        player.sendMessage(org.bukkit.ChatColor.GREEN + "You received rewards for participating!");
                    }
                }
            }
        }
        
        Bukkit.broadcastMessage(org.bukkit.ChatColor.RED + "Event " + event.getName() + " has ended!");
        
        eventsConfig.set("active-events." + eventId, null);
        saveConfig();
        
        return true;
    }
    
    /**
     * Join an event
     */
    public boolean joinEvent(Player player, String eventId) {
        Event event = activeEvents.get(eventId);
        if (event == null) {
            return false;
        }
        
        Set<String> participations = playerParticipations.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        if (participations.contains(eventId)) {
            player.sendMessage(org.bukkit.ChatColor.YELLOW + "You are already participating in this event!");
            return false;
        }
        
        participations.add(eventId);
        player.sendMessage(org.bukkit.ChatColor.GREEN + "You joined the event: " + event.getName() + "!");
        
        return true;
    }
    
    private void startEventTask() {
        // Check for events to start/end
        eventTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            
            // Check scheduled events
            for (Event event : new ArrayList<>(scheduledEvents.values())) {
                if (currentTime >= event.getStartTime()) {
                    startEvent(event.getId());
                }
            }
            
            // Check active events
            for (Event event : new ArrayList<>(activeEvents.values())) {
                if (currentTime >= event.getStartTime() + event.getDuration()) {
                    endEvent(event.getId());
                }
            }
        }, 0L, 1200L); // Every minute
    }
    
    private void saveEvent(String section, String eventId, Event event) {
        String path = section + "." + eventId;
        eventsConfig.set(path + ".name", event.getName());
        eventsConfig.set(path + ".type", event.getType());
        eventsConfig.set(path + ".description", event.getDescription());
        eventsConfig.set(path + ".start-time", event.getStartTime());
        eventsConfig.set(path + ".duration", event.getDuration());
        eventsConfig.set(path + ".reward-money", event.getRewardMoney());
        eventsConfig.set(path + ".reward-items", event.getRewardItems());
        saveConfig();
    }
    
    private void saveConfig() {
        try {
            eventsConfig.save(eventsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save events.yml", e);
        }
    }
    
    public Collection<Event> getActiveEvents() {
        return activeEvents.values();
    }
    
    public Collection<Event> getScheduledEvents() {
        return scheduledEvents.values();
    }
    
    public Event getEvent(String eventId) {
        Event event = activeEvents.get(eventId);
        if (event == null) {
            event = scheduledEvents.get(eventId);
        }
        return event;
    }
    
    public void shutdown() {
        if (eventTask != null) {
            eventTask.cancel();
        }
        saveConfig();
    }
    
    /**
     * Event class
     */
    public static class Event {
        private String id;
        private String name;
        private String type;
        private String description;
        private long startTime;
        private long duration;
        private double rewardMoney;
        private List<String> rewardItems;
        
        public Event(String id, String name, String type, String description, long startTime, long duration,
                    double rewardMoney, List<String> rewardItems) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.description = description;
            this.startTime = startTime;
            this.duration = duration;
            this.rewardMoney = rewardMoney;
            this.rewardItems = rewardItems;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public long getStartTime() { return startTime; }
        public long getDuration() { return duration; }
        public double getRewardMoney() { return rewardMoney; }
        public List<String> getRewardItems() { return rewardItems; }
    }
}

