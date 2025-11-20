package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.entity.Player;

public class PlayerBedEnterListener implements Listener {
    private final Ecore plugin;

    public PlayerBedEnterListener(Ecore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        
        // Check if bed spawn integration is enabled
        if (!plugin.getConfig().getBoolean("home.bed-spawn-enabled", true)) {
            return;
        }

        // Check if auto-set home on bed is enabled
        if (plugin.getConfig().getBoolean("home.auto-set-home-on-bed", true)) {
            Location bedLocation = event.getBed().getLocation();
            
            // Set or update the bed home
            String homeName = "bed";
            boolean success = plugin.getHomeManager().setHome(player, homeName, bedLocation);
            
            if (success) {
                player.sendMessage(ChatColor.GREEN + "Your bed spawn location has been set as your home!");
            } else {
                // Home might already exist, try to update it
                if (plugin.getHomeManager().getHome(player, homeName) != null) {
                    // Update existing bed home
                    plugin.getHomeManager().setHome(player, homeName, bedLocation);
                    player.sendMessage(ChatColor.GREEN + "Your bed spawn location has been updated!");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Could not set bed spawn as home. You may have reached the maximum number of homes.");
                }
            }
        }
    }
}

