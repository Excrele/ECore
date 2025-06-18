package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

public class DiscordManager {
    private final Ecore plugin;
    private JDA jda;
    private TextChannel chatChannel;
    private TextChannel punishmentChannel;
    private TextChannel staffLogsChannel;

    public DiscordManager(Ecore plugin) {
        this.plugin = plugin;
        initializeDiscord();
    }

    private void initializeDiscord() {
        FileConfiguration config = plugin.getConfigManager().getDiscordConfig();
        if (!config.getBoolean("discord.enabled", false)) {
            plugin.getLogger().info("Discord integration is disabled in discordconf.yml.");
            return;
        }

        String token = config.getString("discord.bot-token", "");
        if (token == null || token.trim().isEmpty()) {
            plugin.getLogger().warning("Discord bot token is not set or invalid in discordconf.yml!");
            return;
        }

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .build();
            jda.awaitReady();

            String chatChannelId = config.getString("discord.channel-id", "");
            String punishmentChannelId = config.getString("discord.punishment-channel-id", "");
            String staffLogsChannelId = config.getString("discord.staff-logs-channel-id", "");

            chatChannel = jda.getTextChannelById(chatChannelId);
            punishmentChannel = jda.getTextChannelById(punishmentChannelId);
            staffLogsChannel = jda.getTextChannelById(staffLogsChannelId);

            if (chatChannel == null) {
                plugin.getLogger().warning("Chat channel ID " + chatChannelId + " is invalid or bot lacks access!");
            }
            if (punishmentChannel == null) {
                plugin.getLogger().warning("Punishment channel ID " + punishmentChannelId + " is invalid or bot lacks access!");
            }
            if (staffLogsChannel == null) {
                plugin.getLogger().warning("Staff logs channel ID " + staffLogsChannelId + " is invalid or bot lacks access!");
            }

            if (chatChannel != null) {
                sendServerStartNotification();
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize Discord bot: Invalid token or configuration!", e);
        } catch (InterruptedException e) {
            plugin.getLogger().log(Level.SEVERE, "Interrupted while waiting for Discord bot to be ready!", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Unexpected error initializing Discord bot!", e);
        }
    }

    public void sendServerStartNotification() {
        if (chatChannel != null) {
            chatChannel.sendMessage("Server has started!").queue(
                    success -> plugin.getLogger().info("Sent server start message to Discord chat channel."),
                    error -> plugin.getLogger().warning("Failed to send server start message: " + error.getMessage())
            );
        } else {
            plugin.getLogger().warning("Cannot send server start message: Chat channel is not initialized!");
        }
    }

    public void sendChatNotification(String message) {
        if (chatChannel != null) {
            chatChannel.sendMessage(message).queue(
                    success -> plugin.getLogger().info("Sent chat notification to Discord: " + message),
                    error -> plugin.getLogger().warning("Failed to send chat notification: " + error.getMessage())
            );
        } else {
            plugin.getLogger().warning("Cannot send chat notification: Chat channel is not initialized!");
        }
    }

    public void sendPunishmentNotification(String player, String action, String reason, String duration) {
        if (punishmentChannel != null) {
            String message = String.format("**Punishment Log**\nPlayer: %s\nAction: %s\nReason: %s\nDuration: %s", player, action, reason, duration);
            punishmentChannel.sendMessage(message).queue(
                    success -> plugin.getLogger().info("Sent punishment notification for " + player + " to Discord."),
                    error -> plugin.getLogger().warning("Failed to send punishment notification: " + error.getMessage())
            );
        } else {
            plugin.getLogger().warning("Cannot send punishment notification: Punishment channel is not initialized!");
        }
    }

    public void sendStaffLogNotification(String channel, String player, String action, String target, String value) {
        TextChannel logChannel = channel.equals("playershop-log") || channel.equals("adminshop-log") ? punishmentChannel : staffLogsChannel;
        if (logChannel != null) {
            String message = String.format("**Staff Log**\nPlayer: %s\nAction: %s\nTarget: %s\nValue: %s", player, action, target, value);
            logChannel.sendMessage(message).queue(
                    success -> plugin.getLogger().info("Sent staff log for " + player + " to Discord channel " + channel + "."),
                    error -> plugin.getLogger().warning("Failed to send staff log to " + channel + ": " + error.getMessage())
            );
        } else {
            plugin.getLogger().warning("Cannot send staff log to " + channel + ": Log channel is not initialized!");
        }
    }

    public void shutdown() {
        if (chatChannel != null) {
            chatChannel.sendMessage("Server is stopping!").queue(
                    success -> plugin.getLogger().info("Sent server stop message to Discord chat channel."),
                    error -> plugin.getLogger().warning("Failed to send server stop message: " + error.getMessage())
            );
        }
        if (jda != null) {
            jda.shutdown();
            plugin.getLogger().info("Discord bot has been shut down.");
        }
    }

    public void shutdownBot() {
        shutdown();
    }
}