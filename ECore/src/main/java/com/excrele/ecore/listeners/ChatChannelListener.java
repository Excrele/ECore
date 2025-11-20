package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for chat channels system.
 * Integrates with existing chat system to route messages to channels.
 */
public class ChatChannelListener implements Listener {
    private final Ecore plugin;

    public ChatChannelListener(Ecore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Check if player is in a channel
        String channelId = plugin.getChatChannelManager().getPlayerChannel(event.getPlayer().getUniqueId());
        
        if (channelId != null) {
            // Cancel default chat and send to channel instead
            event.setCancelled(true);
            
            // Send message to channel
            plugin.getChatChannelManager().sendChannelMessage(
                event.getPlayer(), channelId, event.getMessage());
        }
        // If not in a channel, let default chat handle it
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Auto-join to default channel
        plugin.getChatChannelManager().onPlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Cleanup channel membership
        plugin.getChatChannelManager().onPlayerQuit(event.getPlayer());
    }
}

