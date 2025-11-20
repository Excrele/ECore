package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
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
 * Manages Discord account linking for Minecraft players.
 * Links Discord user IDs to Minecraft player UUIDs.
 */
public class AccountLinkManager {
    private final Ecore plugin;
    private File linkFile;
    private FileConfiguration linkConfig;
    private final Map<String, UUID> discordToMinecraft; // Discord ID -> Minecraft UUID
    private final Map<UUID, String> minecraftToDiscord; // Minecraft UUID -> Discord ID
    private final Map<UUID, String> pendingLinks; // Minecraft UUID -> Verification Code

    public AccountLinkManager(Ecore plugin) {
        this.plugin = plugin;
        this.discordToMinecraft = new HashMap<>();
        this.minecraftToDiscord = new HashMap<>();
        this.pendingLinks = new HashMap<>();
        initializeLinkConfig();
        loadLinks();
    }

    /**
     * Gets pending links map (for DiscordCommandHandler access).
     */
    public Map<UUID, String> getPendingLinks() {
        return pendingLinks;
    }

    private void initializeLinkConfig() {
        linkFile = new File(plugin.getDataFolder(), "discord-links.yml");
        if (!linkFile.exists()) {
            try {
                linkFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create discord-links.yml", e);
            }
        }
        linkConfig = YamlConfiguration.loadConfiguration(linkFile);
    }

    private void loadLinks() {
        if (linkConfig.contains("links")) {
            for (String discordId : linkConfig.getConfigurationSection("links").getKeys(false)) {
                String uuidString = linkConfig.getString("links." + discordId);
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    discordToMinecraft.put(discordId, uuid);
                    minecraftToDiscord.put(uuid, discordId);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in discord-links.yml: " + uuidString);
                }
            }
        }
    }

    private void saveLinks() {
        try {
            linkConfig.set("links", null);
            for (Map.Entry<String, UUID> entry : discordToMinecraft.entrySet()) {
                linkConfig.set("links." + entry.getKey(), entry.getValue().toString());
            }
            linkConfig.save(linkFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save discord-links.yml: " + e.getMessage());
        }
    }

    /**
     * Generates a verification code for linking accounts.
     */
    public String generateVerificationCode(Player player) {
        String code = String.valueOf((int)(Math.random() * 900000) + 100000); // 6-digit code
        pendingLinks.put(player.getUniqueId(), code);
        // Expire after 5 minutes
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            pendingLinks.remove(player.getUniqueId());
        }, 6000L); // 5 minutes = 6000 ticks
        return code;
    }

    /**
     * Links a Discord account to a Minecraft player using a verification code.
     */
    public boolean linkAccount(String discordId, UUID minecraftUuid, String code) {
        String expectedCode = pendingLinks.get(minecraftUuid);
        if (expectedCode == null || !expectedCode.equals(code)) {
            return false;
        }

        // Remove old link if exists
        String oldDiscordId = minecraftToDiscord.get(minecraftUuid);
        if (oldDiscordId != null) {
            discordToMinecraft.remove(oldDiscordId);
        }

        // Create new link
        discordToMinecraft.put(discordId, minecraftUuid);
        minecraftToDiscord.put(minecraftUuid, discordId);
        pendingLinks.remove(minecraftUuid);
        saveLinks();
        return true;
    }

    /**
     * Unlinks a Discord account from a Minecraft player.
     */
    public void unlinkAccount(UUID minecraftUuid) {
        String discordId = minecraftToDiscord.remove(minecraftUuid);
        if (discordId != null) {
            discordToMinecraft.remove(discordId);
            saveLinks();
        }
    }

    /**
     * Gets the Minecraft UUID linked to a Discord ID.
     */
    public UUID getMinecraftUuid(String discordId) {
        return discordToMinecraft.get(discordId);
    }

    /**
     * Gets the Discord ID linked to a Minecraft UUID.
     */
    public String getDiscordId(UUID minecraftUuid) {
        return minecraftToDiscord.get(minecraftUuid);
    }

    /**
     * Gets the Discord ID linked to a Minecraft player.
     */
    public String getDiscordId(Player player) {
        return minecraftToDiscord.get(player.getUniqueId());
    }

    /**
     * Checks if a Minecraft player has a linked Discord account.
     */
    public boolean isLinked(Player player) {
        return minecraftToDiscord.containsKey(player.getUniqueId());
    }

    /**
     * Checks if a Discord ID is linked to a Minecraft account.
     */
    public boolean isLinked(String discordId) {
        return discordToMinecraft.containsKey(discordId);
    }
}

