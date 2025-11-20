package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final Ecore plugin;

    public ChatListener(Ecore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer().hasPermission("ecore.colorchat")) {
            String message = ChatColor.translateAlternateColorCodes('&', event.getMessage());
            event.setMessage(message);
        }

        if (event.getPlayer().hasPermission("ecore.nickname")) {
            String nickname = event.getPlayer().getDisplayName();
            if (nickname != null) {
                event.setFormat(ChatColor.translateAlternateColorCodes('&', nickname) + "§r: %2$s");
            }
        }

        // Send to Discord
        plugin.getDiscordManager().sendChatToDiscord(event.getPlayer().getName(), event.getMessage());
    }
}