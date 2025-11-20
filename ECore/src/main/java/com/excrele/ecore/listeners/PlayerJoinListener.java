package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final Ecore plugin;

    public PlayerJoinListener(Ecore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Initialize economy for new players
        plugin.getEconomyManager().initializePlayer(event.getPlayer());
        
        // Update player data
        plugin.getPlayerInfoManager().updatePlayerData(event.getPlayer());
        
        // Check for mail
        int mailCount = plugin.getMailManager().getMailCount(event.getPlayer());
        if (mailCount > 0) {
            event.getPlayer().sendMessage("§eYou have " + mailCount + " unread mail(s)! Use /mail read to view them.");
        }
        
        // Check achievements (including first-join)
        if (plugin.getAchievementManager() != null) {
            plugin.getAchievementManager().checkAchievements(event.getPlayer());
        }
        
        // Send Discord join notification
        plugin.getDiscordManager().sendPlayerJoinNotification(event.getPlayer());
    }
}

