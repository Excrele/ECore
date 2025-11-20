package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages command cooldowns and economy costs.
 */
public class CommandControlManager {
    private final Ecore plugin;
    private final Map<UUID, Map<String, Long>> cooldowns; // Player UUID -> Command -> Expiry Time
    private FileConfiguration config;

    public CommandControlManager(Ecore plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
        this.config = plugin.getConfigManager().getConfig();
    }

    /**
     * Reloads configuration.
     */
    public void reload() {
        this.config = plugin.getConfigManager().getConfig();
        cooldowns.clear();
    }

    /**
     * Checks if a command has cooldown/cost configured.
     */
    public boolean isCommandControlled(String command) {
        String path = "command-control.commands." + command.toLowerCase();
        return config.contains(path);
    }

    /**
     * Gets the cooldown for a command (in seconds).
     */
    public int getCooldown(String command) {
        String path = "command-control.commands." + command.toLowerCase() + ".cooldown";
        return config.getInt(path, 0);
    }

    /**
     * Gets the economy cost for a command.
     */
    public double getCost(String command) {
        String path = "command-control.commands." + command.toLowerCase() + ".cost";
        return config.getDouble(path, 0.0);
    }

    /**
     * Gets the bypass permission for a command.
     */
    public String getBypassPermission(String command) {
        String path = "command-control.commands." + command.toLowerCase() + ".bypass-permission";
        return config.getString(path, "ecore.command.bypass");
    }

    /**
     * Checks if a player is on cooldown for a command.
     */
    public boolean isOnCooldown(UUID playerUuid, String command) {
        if (!isCommandControlled(command)) {
            return false;
        }

        int cooldown = getCooldown(command);
        if (cooldown <= 0) {
            return false;
        }

        Map<String, Long> playerCooldowns = cooldowns.get(playerUuid);
        if (playerCooldowns == null) {
            return false;
        }

        Long expiryTime = playerCooldowns.get(command.toLowerCase());
        if (expiryTime == null) {
            return false;
        }

        return System.currentTimeMillis() < expiryTime;
    }

    /**
     * Gets the remaining cooldown time in seconds.
     */
    public long getRemainingCooldown(UUID playerUuid, String command) {
        if (!isOnCooldown(playerUuid, command)) {
            return 0;
        }

        Map<String, Long> playerCooldowns = cooldowns.get(playerUuid);
        if (playerCooldowns == null) {
            return 0;
        }

        Long expiryTime = playerCooldowns.get(command.toLowerCase());
        if (expiryTime == null) {
            return 0;
        }

        long remaining = (expiryTime - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    /**
     * Sets a cooldown for a player and command.
     */
    public void setCooldown(UUID playerUuid, String command, int seconds) {
        if (seconds <= 0) {
            return;
        }

        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(playerUuid, k -> new HashMap<>());
        long expiryTime = System.currentTimeMillis() + (seconds * 1000L);
        playerCooldowns.put(command.toLowerCase(), expiryTime);
    }

    /**
     * Clears cooldown for a player and command.
     */
    public void clearCooldown(UUID playerUuid, String command) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerUuid);
        if (playerCooldowns != null) {
            playerCooldowns.remove(command.toLowerCase());
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(playerUuid);
            }
        }
    }

    /**
     * Clears all cooldowns for a player.
     */
    public void clearAllCooldowns(UUID playerUuid) {
        cooldowns.remove(playerUuid);
    }

    /**
     * Checks if a player can afford the command cost.
     */
    public boolean canAfford(org.bukkit.entity.Player player, String command) {
        if (!isCommandControlled(command)) {
            return true;
        }

        double cost = getCost(command);
        if (cost <= 0) {
            return true;
        }

        return plugin.getEconomyManager().getBalance(player.getUniqueId()) >= cost;
    }

    /**
     * Charges a player for using a command.
     */
    public boolean chargePlayer(org.bukkit.entity.Player player, String command) {
        if (!isCommandControlled(command)) {
            return true;
        }

        double cost = getCost(command);
        if (cost <= 0) {
            return true;
        }

        if (!canAfford(player, command)) {
            return false;
        }

        plugin.getEconomyManager().removeBalance(player.getUniqueId(), cost);
        return true;
    }

    /**
     * Formats cooldown message.
     */
    public String getCooldownMessage(long seconds) {
        if (seconds < 60) {
            return "§cYou must wait " + seconds + " second(s) before using this command again!";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return "§cYou must wait " + minutes + " minute(s) and " + remainingSeconds + " second(s) before using this command again!";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return "§cYou must wait " + hours + " hour(s) and " + minutes + " minute(s) before using this command again!";
        }
    }
}

