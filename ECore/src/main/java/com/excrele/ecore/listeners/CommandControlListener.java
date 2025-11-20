package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Listener for command cooldowns and costs.
 */
public class CommandControlListener implements Listener {
    private final Ecore plugin;

    public CommandControlListener(Ecore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        // Check if command is controlled
        if (!plugin.getCommandControlManager().isCommandControlled(command)) {
            return;
        }

        // Check bypass permission
        String bypassPermission = plugin.getCommandControlManager().getBypassPermission(command);
        if (player.hasPermission(bypassPermission)) {
            return;
        }

        // Check cooldown
        if (plugin.getCommandControlManager().isOnCooldown(player.getUniqueId(), command)) {
            long remaining = plugin.getCommandControlManager().getRemainingCooldown(player.getUniqueId(), command);
            player.sendMessage(plugin.getCommandControlManager().getCooldownMessage(remaining));
            event.setCancelled(true);
            return;
        }

        // Check cost
        if (!plugin.getCommandControlManager().canAfford(player, command)) {
            double cost = plugin.getCommandControlManager().getCost(command);
            player.sendMessage("§cYou don't have enough money! Required: §e" + 
                plugin.getEconomyManager().format(cost));
            event.setCancelled(true);
            return;
        }

        // Charge player
        double cost = plugin.getCommandControlManager().getCost(command);
        if (cost > 0) {
            if (!plugin.getCommandControlManager().chargePlayer(player, command)) {
                player.sendMessage("§cFailed to charge for command!");
                event.setCancelled(true);
                return;
            }
            player.sendMessage("§aCharged §e" + plugin.getEconomyManager().format(cost) + 
                " §afor using this command.");
        }

        // Set cooldown
        int cooldown = plugin.getCommandControlManager().getCooldown(command);
        if (cooldown > 0) {
            plugin.getCommandControlManager().setCooldown(player.getUniqueId(), command, cooldown);
        }
    }
}

