package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CommandSpyListener implements Listener {
    private final Ecore plugin;

    public CommandSpyListener(Ecore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();

        // Notify staff with command spy enabled
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("ecore.staff") && 
                plugin.getStaffManager().isCommandSpyEnabled(staff) &&
                !staff.equals(player)) {
                staff.sendMessage(ChatColor.GRAY + "[CommandSpy] " + 
                    ChatColor.YELLOW + player.getName() + ChatColor.GRAY + ": " + 
                    ChatColor.WHITE + command);
            }
        }
    }

}

