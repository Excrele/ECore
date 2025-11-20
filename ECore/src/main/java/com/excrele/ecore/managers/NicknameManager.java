package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages player nicknames with color and formatting support.
 */
public class NicknameManager {
    private final Ecore plugin;
    private File nicknamesFile;
    private FileConfiguration nicknamesConfig;
    private final Map<UUID, String> nicknames; // UUID -> Nickname
    private final Map<UUID, ChatColor> nicknameColors; // UUID -> Color
    private final Map<UUID, String> nicknameFormats; // UUID -> Format

    public NicknameManager(Ecore plugin) {
        this.plugin = plugin;
        this.nicknames = new HashMap<>();
        this.nicknameColors = new HashMap<>();
        this.nicknameFormats = new HashMap<>();
        initializeNicknamesConfig();
        loadNicknames();
    }

    private void initializeNicknamesConfig() {
        nicknamesFile = new File(plugin.getDataFolder(), "nicknames.yml");
        if (!nicknamesFile.exists()) {
            try {
                nicknamesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create nicknames.yml", e);
            }
        }
        nicknamesConfig = YamlConfiguration.loadConfiguration(nicknamesFile);
    }

    private void loadNicknames() {
        if (nicknamesConfig.getConfigurationSection("nicknames") == null) return;

        for (String uuidStr : nicknamesConfig.getConfigurationSection("nicknames").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            String path = "nicknames." + uuidStr;
            
            String nickname = nicknamesConfig.getString(path + ".nickname");
            if (nickname != null) {
                nicknames.put(uuid, nickname);
            }
            
            String colorStr = nicknamesConfig.getString(path + ".color");
            if (colorStr != null) {
                try {
                    nicknameColors.put(uuid, ChatColor.valueOf(colorStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid nickname color for " + uuidStr + ": " + colorStr);
                }
            }
            
            String format = nicknamesConfig.getString(path + ".format");
            if (format != null) {
                nicknameFormats.put(uuid, format);
            }
        }
    }

    private void saveNicknames() {
        try {
            nicknamesConfig.set("nicknames", null);
            for (UUID uuid : nicknames.keySet()) {
                String path = "nicknames." + uuid.toString();
                nicknamesConfig.set(path + ".nickname", nicknames.get(uuid));
                
                if (nicknameColors.containsKey(uuid)) {
                    nicknamesConfig.set(path + ".color", nicknameColors.get(uuid).name());
                }
                
                if (nicknameFormats.containsKey(uuid)) {
                    nicknamesConfig.set(path + ".format", nicknameFormats.get(uuid));
                }
            }
            nicknamesConfig.save(nicknamesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save nicknames.yml", e);
        }
    }

    /**
     * Sets a player's nickname.
     */
    public boolean setNickname(Player player, String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            return false;
        }

        // Check if nickname contains color codes and player has permission
        String processedNickname = nickname;
        if (player.hasPermission("ecore.nickname.color")) {
            processedNickname = ChatColor.translateAlternateColorCodes('&', nickname);
        } else {
            // Remove color codes if no permission
            processedNickname = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', nickname));
        }

        nicknames.put(player.getUniqueId(), processedNickname);
        applyNickname(player);
        saveNicknames();
        return true;
    }

    /**
     * Resets a player's nickname.
     */
    public boolean resetNickname(Player player) {
        UUID uuid = player.getUniqueId();
        boolean hadNickname = nicknames.containsKey(uuid);
        
        nicknames.remove(uuid);
        nicknameColors.remove(uuid);
        nicknameFormats.remove(uuid);
        
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
        
        saveNicknames();
        return hadNickname;
    }

    /**
     * Sets nickname color.
     */
    public boolean setNicknameColor(Player player, ChatColor color) {
        if (color == null || !color.isColor()) {
            return false;
        }

        if (!player.hasPermission("ecore.nickname.color")) {
            return false;
        }

        nicknameColors.put(player.getUniqueId(), color);
        applyNickname(player);
        saveNicknames();
        return true;
    }

    /**
     * Sets nickname format.
     */
    public boolean setNicknameFormat(Player player, String format) {
        if (!player.hasPermission("ecore.nickname.format")) {
            return false;
        }

        nicknameFormats.put(player.getUniqueId(), format);
        applyNickname(player);
        saveNicknames();
        return true;
    }

    /**
     * Gets a player's nickname.
     */
    public String getNickname(Player player) {
        return nicknames.get(player.getUniqueId());
    }

    /**
     * Gets formatted nickname (with color and format).
     */
    public String getFormattedNickname(Player player) {
        UUID uuid = player.getUniqueId();
        String nickname = nicknames.get(uuid);
        
        if (nickname == null) {
            return player.getName();
        }

        // Apply color
        ChatColor color = nicknameColors.get(uuid);
        if (color != null) {
            nickname = color + nickname;
        }

        // Apply format
        String format = nicknameFormats.get(uuid);
        if (format != null && player.hasPermission("ecore.nickname.format")) {
            format = ChatColor.translateAlternateColorCodes('&', format);
            nickname = format.replace("%nickname%", nickname).replace("%name%", player.getName());
        }

        return nickname;
    }

    /**
     * Checks if a player has a nickname.
     */
    public boolean hasNickname(Player player) {
        return nicknames.containsKey(player.getUniqueId());
    }

    /**
     * Applies nickname to player (display name and tab list).
     */
    public void applyNickname(Player player) {
        if (!hasNickname(player)) {
            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());
            return;
        }

        String formattedNickname = getFormattedNickname(player);
        player.setDisplayName(formattedNickname);
        player.setPlayerListName(formattedNickname);
    }

    /**
     * Reloads nicknames from config.
     */
    public void reload() {
        nicknames.clear();
        nicknameColors.clear();
        nicknameFormats.clear();
        nicknamesConfig = YamlConfiguration.loadConfiguration(nicknamesFile);
        loadNicknames();
        
        // Reapply nicknames to online players
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            applyNickname(player);
        }
    }
}

