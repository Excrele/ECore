package com.excrele.ecore.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

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
    }
}