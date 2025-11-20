package com.excrele.ecore.managers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.excrele.ecore.Ecore;

public class PlayerInfoManager {
    private final Ecore plugin;
    private File playerDataFile;
    private FileConfiguration playerDataConfig;

    public PlayerInfoManager(Ecore plugin) {
        this.plugin = plugin;
        initializePlayerDataConfig();
    }

    private void initializePlayerDataConfig() {
        playerDataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create playerdata.yml", e);
            }
        }
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    public void updatePlayerData(Player player) {
        String uuid = player.getUniqueId().toString();
        String path = "players." + uuid;
        
        playerDataConfig.set(path + ".name", player.getName());
        playerDataConfig.set(path + ".last-seen", System.currentTimeMillis());
        playerDataConfig.set(path + ".first-seen", playerDataConfig.getLong(path + ".first-seen", System.currentTimeMillis()));
        
        if (player.isOnline()) {
            long currentPlaytime = playerDataConfig.getLong(path + ".playtime", 0);
            playerDataConfig.set(path + ".last-login", System.currentTimeMillis());
            playerDataConfig.set(path + ".playtime", currentPlaytime);
        }
        
        savePlayerData();
    }

    public long getLastSeen(OfflinePlayer player) {
        String uuid = player.getUniqueId().toString();
        return playerDataConfig.getLong("players." + uuid + ".last-seen", 0);
    }

    public long getFirstSeen(OfflinePlayer player) {
        String uuid = player.getUniqueId().toString();
        return playerDataConfig.getLong("players." + uuid + ".first-seen", 0);
    }

    public long getPlaytime(OfflinePlayer player) {
        String uuid = player.getUniqueId().toString();
        long basePlaytime = playerDataConfig.getLong("players." + uuid + ".playtime", 0);
        
        if (player.isOnline()) {
            long lastLogin = playerDataConfig.getLong("players." + uuid + ".last-login", System.currentTimeMillis());
            basePlaytime += (System.currentTimeMillis() - lastLogin) / 1000;
        }
        
        return basePlaytime;
    }

    public String getPlayerInfo(OfflinePlayer player) {
        if (player == null) {
            return "§cPlayer not found!";
        }

        StringBuilder info = new StringBuilder();
        info.append("§6=== Player Info: ").append(player.getName()).append(" ===\n");
        info.append("§7UUID: §f").append(player.getUniqueId()).append("\n");
        
        if (player.isOnline()) {
            Player onlinePlayer = player.getPlayer();
            info.append("§7Status: §aOnline\n");
            info.append("§7Health: §f").append(String.format("%.1f", onlinePlayer.getHealth())).append(" / ").append(String.format("%.1f", onlinePlayer.getMaxHealth())).append("\n");
            info.append("§7Food: §f").append(onlinePlayer.getFoodLevel()).append(" / 20\n");
            info.append("§7Level: §f").append(onlinePlayer.getLevel()).append("\n");
            info.append("§7GameMode: §f").append(onlinePlayer.getGameMode().name()).append("\n");
            info.append("§7World: §f").append(onlinePlayer.getWorld().getName()).append("\n");
            info.append("§7Location: §f").append(String.format("%.0f, %.0f, %.0f", 
                onlinePlayer.getLocation().getX(),
                onlinePlayer.getLocation().getY(),
                onlinePlayer.getLocation().getZ())).append("\n");
        } else {
            info.append("§7Status: §cOffline\n");
            long lastSeen = getLastSeen(player);
            if (lastSeen > 0) {
                long secondsAgo = (System.currentTimeMillis() - lastSeen) / 1000;
                info.append("§7Last Seen: §f").append(formatTime(secondsAgo)).append(" ago\n");
            }
        }
        
        long playtime = getPlaytime(player);
        info.append("§7Playtime: §f").append(formatTime(playtime)).append("\n");
        
        long firstSeen = getFirstSeen(player);
        if (firstSeen > 0) {
            info.append("§7First Seen: §f").append(new java.util.Date(firstSeen).toString()).append("\n");
        }

        return info.toString();
    }

    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutes";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " hours";
        } else {
            return (seconds / 86400) + " days";
        }
    }

    private void savePlayerData() {
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save playerdata.yml: " + e.getMessage());
        }
    }
}

