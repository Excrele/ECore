package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages chat channels system with multiple channels, permissions, and range-based local chat.
 */
public class ChatChannelManager {
    private final Ecore plugin;
    private final Map<String, ChatChannel> channels; // Channel ID -> Channel
    private final Map<UUID, String> playerChannels; // Player UUID -> Current channel ID
    private final Map<UUID, Set<String>> mutedChannels; // Player UUID -> Set of muted channel IDs
    private final Map<String, Set<UUID>> channelMembers; // Channel ID -> Set of player UUIDs
    
    /**
     * Represents a chat channel configuration.
     */
    public static class ChatChannel {
        private final String id;
        private final String name;
        private final String prefix;
        private final String permission;
        private final int range; // -1 for unlimited, positive for block range
        private final ChatColor color;
        private final boolean defaultChannel;
        private final boolean autoJoin;
        
        public ChatChannel(String id, String name, String prefix, String permission, 
                          int range, ChatColor color, boolean defaultChannel, boolean autoJoin) {
            this.id = id;
            this.name = name;
            this.prefix = prefix;
            this.permission = permission;
            this.range = range;
            this.color = color;
            this.defaultChannel = defaultChannel;
            this.autoJoin = autoJoin;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getPrefix() { return prefix; }
        public String getPermission() { return permission; }
        public int getRange() { return range; }
        public ChatColor getColor() { return color; }
        public boolean isDefaultChannel() { return defaultChannel; }
        public boolean isAutoJoin() { return autoJoin; }
    }
    
    public ChatChannelManager(Ecore plugin) {
        this.plugin = plugin;
        this.channels = new HashMap<>();
        this.playerChannels = new HashMap<>();
        this.mutedChannels = new HashMap<>();
        this.channelMembers = new HashMap<>();
        loadChannels();
    }
    
    private void loadChannels() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        
        if (!config.contains("chat-channels.enabled") || !config.getBoolean("chat-channels.enabled")) {
            return;
        }
        
        if (config.getConfigurationSection("chat-channels.channels") == null) {
            plugin.getLogger().warning("No chat channels configured!");
            return;
        }
        
        for (String channelId : config.getConfigurationSection("chat-channels.channels").getKeys(false)) {
            String path = "chat-channels.channels." + channelId;
            String name = config.getString(path + ".name", channelId);
            String prefix = config.getString(path + ".prefix", "[" + name + "]");
            String permission = config.getString(path + ".permission", "ecore.chat." + channelId);
            int range = config.getInt(path + ".range", -1);
            String colorStr = config.getString(path + ".color", "WHITE");
            ChatColor color = ChatColor.valueOf(colorStr.toUpperCase());
            boolean defaultChannel = config.getBoolean(path + ".default", false);
            boolean autoJoin = config.getBoolean(path + ".auto-join", false);
            
            ChatChannel channel = new ChatChannel(channelId, name, prefix, permission, 
                                                range, color, defaultChannel, autoJoin);
            channels.put(channelId, channel);
            channelMembers.put(channelId, new HashSet<>());
            
            // Auto-join players to default channel
            if (defaultChannel || autoJoin) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission(permission)) {
                        joinChannel(player, channelId, false);
                    }
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + channels.size() + " chat channels!");
    }
    
    /**
     * Gets a channel by ID.
     */
    public ChatChannel getChannel(String channelId) {
        return channels.get(channelId);
    }
    
    /**
     * Gets all channels.
     */
    public Map<String, ChatChannel> getChannels() {
        return new HashMap<>(channels);
    }
    
    /**
     * Gets player's current channel.
     */
    public String getPlayerChannel(UUID uuid) {
        return playerChannels.get(uuid);
    }
    
    /**
     * Joins a channel.
     */
    public boolean joinChannel(Player player, String channelId, boolean sendMessage) {
        ChatChannel channel = channels.get(channelId);
        if (channel == null) {
            return false;
        }
        
        if (!player.hasPermission(channel.getPermission())) {
            if (sendMessage) {
                player.sendMessage(ChatColor.RED + "You don't have permission to join " + channel.getName() + " channel!");
            }
            return false;
        }
        
        // Leave previous channel
        String previousChannel = playerChannels.get(player.getUniqueId());
        if (previousChannel != null) {
            leaveChannel(player, previousChannel, false);
        }
        
        // Join new channel
        playerChannels.put(player.getUniqueId(), channelId);
        channelMembers.get(channelId).add(player.getUniqueId());
        
        if (sendMessage) {
            player.sendMessage(ChatColor.GREEN + "You joined " + channel.getColor() + channel.getName() + 
                             ChatColor.GREEN + " channel!");
        }
        
        return true;
    }
    
    /**
     * Leaves a channel.
     */
    public boolean leaveChannel(Player player, String channelId, boolean sendMessage) {
        if (!playerChannels.get(player.getUniqueId()).equals(channelId)) {
            return false;
        }
        
        playerChannels.remove(player.getUniqueId());
        channelMembers.get(channelId).remove(player.getUniqueId());
        
        if (sendMessage) {
            ChatChannel channel = channels.get(channelId);
            player.sendMessage(ChatColor.YELLOW + "You left " + channel.getColor() + channel.getName() + 
                             ChatColor.YELLOW + " channel!");
        }
        
        return true;
    }
    
    /**
     * Mutes a channel for a player.
     */
    public boolean muteChannel(Player player, String channelId) {
        mutedChannels.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(channelId);
        ChatChannel channel = channels.get(channelId);
        player.sendMessage(ChatColor.YELLOW + "You muted " + channel.getColor() + channel.getName() + 
                         ChatColor.YELLOW + " channel!");
        return true;
    }
    
    /**
     * Unmutes a channel for a player.
     */
    public boolean unmuteChannel(Player player, String channelId) {
        Set<String> muted = mutedChannels.get(player.getUniqueId());
        if (muted == null || !muted.contains(channelId)) {
            return false;
        }
        
        muted.remove(channelId);
        if (muted.isEmpty()) {
            mutedChannels.remove(player.getUniqueId());
        }
        
        ChatChannel channel = channels.get(channelId);
        player.sendMessage(ChatColor.GREEN + "You unmuted " + channel.getColor() + channel.getName() + 
                         ChatColor.GREEN + " channel!");
        return true;
    }
    
    /**
     * Checks if a channel is muted for a player.
     */
    public boolean isChannelMuted(UUID uuid, String channelId) {
        return mutedChannels.getOrDefault(uuid, Collections.emptySet()).contains(channelId);
    }
    
    /**
     * Sends a message to a channel.
     */
    public void sendChannelMessage(Player sender, String channelId, String message) {
        ChatChannel channel = channels.get(channelId);
        if (channel == null) {
            return;
        }
        
        if (!playerChannels.get(sender.getUniqueId()).equals(channelId)) {
            sender.sendMessage(ChatColor.RED + "You are not in " + channel.getName() + " channel!");
            return;
        }
        
        String format = channel.getColor() + channel.getPrefix() + ChatColor.RESET + " " + 
                       sender.getDisplayName() + ChatColor.RESET + ": " + message;
        
        if (channel.getRange() == -1) {
            // Global channel - send to all members
            for (UUID memberUuid : channelMembers.get(channelId)) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null && !isChannelMuted(memberUuid, channelId)) {
                    member.sendMessage(format);
                }
            }
        } else {
            // Range-based channel - send to nearby members
            for (UUID memberUuid : channelMembers.get(channelId)) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null && !isChannelMuted(memberUuid, channelId)) {
                    if (member.getWorld() == sender.getWorld()) {
                        double distance = member.getLocation().distance(sender.getLocation());
                        if (distance <= channel.getRange()) {
                            member.sendMessage(format);
                        }
                    }
                }
            }
        }
        
        // Send to Discord if enabled
        if (plugin.getDiscordManager() != null) {
            plugin.getDiscordManager().sendChatToDiscord(
                "[" + channel.getName() + "] " + sender.getName(), message);
        }
    }
    
    /**
     * Creates a new channel (admin only).
     */
    public boolean createChannel(String channelId, String name, String prefix, String permission, 
                                int range, ChatColor color, boolean defaultChannel, boolean autoJoin) {
        if (channels.containsKey(channelId)) {
            return false;
        }
        
        ChatChannel channel = new ChatChannel(channelId, name, prefix, permission, 
                                            range, color, defaultChannel, autoJoin);
        channels.put(channelId, channel);
        channelMembers.put(channelId, new HashSet<>());
        
        // Save to config
        FileConfiguration config = plugin.getConfigManager().getConfig();
        String path = "chat-channels.channels." + channelId;
        config.set(path + ".name", name);
        config.set(path + ".prefix", prefix);
        config.set(path + ".permission", permission);
        config.set(path + ".range", range);
        config.set(path + ".color", color.name());
        config.set(path + ".default", defaultChannel);
        config.set(path + ".auto-join", autoJoin);
        plugin.getConfigManager().saveConfig();
        
        return true;
    }
    
    /**
     * Deletes a channel (admin only).
     */
    public boolean deleteChannel(String channelId) {
        if (!channels.containsKey(channelId)) {
            return false;
        }
        
        // Remove all members
        for (UUID memberUuid : channelMembers.get(channelId)) {
            playerChannels.remove(memberUuid);
        }
        
        channels.remove(channelId);
        channelMembers.remove(channelId);
        
        // Remove from config
        FileConfiguration config = plugin.getConfigManager().getConfig();
        config.set("chat-channels.channels." + channelId, null);
        plugin.getConfigManager().saveConfig();
        
        return true;
    }
    
    /**
     * Handles player join - auto-join to default channel.
     */
    public void onPlayerJoin(Player player) {
        // Find default channel
        for (ChatChannel channel : channels.values()) {
            if (channel.isDefaultChannel() || channel.isAutoJoin()) {
                if (player.hasPermission(channel.getPermission())) {
                    joinChannel(player, channel.getId(), false);
                    break;
                }
            }
        }
    }
    
    /**
     * Handles player quit - cleanup.
     */
    public void onPlayerQuit(Player player) {
        String channelId = playerChannels.remove(player.getUniqueId());
        if (channelId != null) {
            channelMembers.get(channelId).remove(player.getUniqueId());
        }
        mutedChannels.remove(player.getUniqueId());
    }
}

