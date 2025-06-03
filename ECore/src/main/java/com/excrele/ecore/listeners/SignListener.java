package com.excrele.ecore.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.getPlayer().hasPermission("ecore.colorsign")) {
            for (int i = 0; i < 4; i++) {
                String line = event.getLine(i);
                if (line != null) {
                    event.setLine(i, ChatColor.translateAlternateColorCodes('&', line));
                }
            }
        }
    }
}