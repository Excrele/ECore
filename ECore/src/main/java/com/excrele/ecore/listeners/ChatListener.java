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

        // Use nickname system if available
        if (plugin.getNicknameManager() != null && plugin.getNicknameManager().hasNickname(event.getPlayer())) {
            String formattedNickname = plugin.getNicknameManager().getFormattedNickname(event.getPlayer());
            event.setFormat(formattedNickname + ChatColor.RESET + ": %2$s");
        } else if (event.getPlayer().hasPermission("ecore.nickname")) {
            // Fallback to display name if nickname system not available
            String nickname = event.getPlayer().getDisplayName();
            if (nickname != null && !nickname.equals(event.getPlayer().getName())) {
                event.setFormat(ChatColor.translateAlternateColorCodes('&', nickname) + ChatColor.RESET + ": %2$s");
            }
        }

        // Send to Discord
        plugin.getDiscordManager().sendChatToDiscord(event.getPlayer().getName(), event.getMessage());
    }
}