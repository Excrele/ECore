package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Manages titles, subtitles, and action bar messages.
 */
public class TitleManager {
    private final Ecore plugin;

    public TitleManager(Ecore plugin) {
        this.plugin = plugin;
    }

    /**
     * Sends a title and subtitle to a player.
     */
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        String titleText = ChatColor.translateAlternateColorCodes('&', title != null ? title : "");
        String subtitleText = ChatColor.translateAlternateColorCodes('&', subtitle != null ? subtitle : "");
        
        player.sendTitle(titleText, subtitleText, fadeIn, stay, fadeOut);
    }

    /**
     * Sends a title to a player (no subtitle).
     */
    public void sendTitle(Player player, String title, int fadeIn, int stay, int fadeOut) {
        sendTitle(player, title, null, fadeIn, stay, fadeOut);
    }

    /**
     * Sends a title to a player with default timings.
     */
    public void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, 10, 70, 20);
    }

    /**
     * Sends a title to a player (no subtitle) with default timings.
     */
    public void sendTitle(Player player, String title) {
        sendTitle(player, title, null, 10, 70, 20);
    }

    /**
     * Sends an action bar message to a player.
     */
    public void sendActionBar(Player player, String message) {
        String messageText = ChatColor.translateAlternateColorCodes('&', message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(messageText));
    }

    /**
     * Broadcasts a title to all players.
     */
    public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    /**
     * Broadcasts a title to all players with default timings.
     */
    public void broadcastTitle(String title, String subtitle) {
        broadcastTitle(title, subtitle, 10, 70, 20);
    }

    /**
     * Broadcasts an action bar message to all players.
     */
    public void broadcastActionBar(String message) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            sendActionBar(player, message);
        }
    }

    /**
     * Clears the title for a player.
     */
    public void clearTitle(Player player) {
        player.resetTitle();
    }

    /**
     * Clears titles for all players.
     */
    public void clearAllTitles() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.resetTitle();
        }
    }
}

