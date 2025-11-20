package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.time.Instant;

public class DiscordManager {
    private final Ecore plugin;
    private JDA jda;
    private final FileConfiguration config;

    public DiscordManager(Ecore plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager().getDiscordConfig();
        if (config.getBoolean("discord.enabled")) {
            initializeBot();
        } else {
            plugin.getLogger().info("Discord bot is disabled in discordconf.yml.");
        }
    }

    private void initializeBot() {
        String token = config.getString("discord.bot-token");
        if (token == null || token.isEmpty() || token.equals("YOUR_BOT_TOKEN_HERE")) {
            plugin.getLogger().warning("Invalid or missing Discord bot token in discordconf.yml. Bot will not start.");
            return;
        }

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(
                        GatewayIntent.GUILD_MESSAGES, 
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS
                    )
                    .addEventListeners(new DiscordMessageListener())
                    .build();
            jda.awaitReady();
            plugin.getLogger().info("Discord bot connected successfully as " + jda.getSelfUser().getName());
        } catch (InterruptedException e) {
            plugin.getLogger().warning("Discord bot initialization interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize Discord bot: " + e.getMessage());
        }
    }

    public void sendChatToDiscord(String playerName, String message) {
        if (jda == null) return;

        String channelId = config.getString("discord.channel-id");
        TextChannel channel = getChannel(channelId);
        if (channel == null) return;

        String format = config.getString("discord.message-formats.minecraft-to-discord", 
            "[Minecraft] %player%: %message%");
        String discordMessage = format
            .replace("%player%", playerName)
            .replace("%message%", message);

        try {
            channel.sendMessage(discordMessage).queue();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send chat to Discord: " + e.getMessage());
        }
    }

    private class DiscordMessageListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().isBot()) return;
            if (!event.getChannel().getId().equals(config.getString("discord.channel-id"))) return;

            String format = config.getString("discord.message-formats.discord-to-minecraft",
                "&7[Discord] &f%user%: %message%");
            String minecraftMessage = format
                .replace("%user%", event.getAuthor().getName())
                .replace("%message%", event.getMessage().getContentDisplay());

            String formatted = ChatColor.translateAlternateColorCodes('&', minecraftMessage);
            Bukkit.broadcastMessage(formatted);
        }
    }

    public void sendServerStartNotification() {
        if (jda == null) {
            plugin.getLogger().warning("Cannot send server start notification: Discord bot is not initialized.");
            return;
        }

        String channelId = config.getString("discord.channel-id");
        TextChannel channel = getChannel(channelId);
        if (channel != null) {
            try {
                channel.sendMessage("Server has started!").queue();
                plugin.getLogger().info("Sent server start message to Discord chat channel.");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send server start message: " + e.getMessage());
            }
        }
    }

    public void sendServerStopNotification() {
        if (jda == null) {
            plugin.getLogger().warning("Cannot send server stop notification: Discord bot is not initialized.");
            return;
        }

        String channelId = config.getString("discord.channel-id");
        TextChannel channel = getChannel(channelId);
        if (channel != null) {
            try {
                channel.sendMessage("Server is stopping!").queue();
                plugin.getLogger().info("Sent server stop message to Discord chat channel.");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send server stop message: " + e.getMessage());
            }
        }
    }

    public void sendStaffLogNotification(String logType, String playerName, String action, String target, String details) {
        if (jda == null) {
            plugin.getLogger().warning("Cannot send staff log: Discord bot is not initialized.");
            return;
        }

        String channelId = config.getString("discord.punishment-channel-id");
        TextChannel channel = getChannel(channelId);
        if (channel == null) {
            return;
        }

        // Use rich embeds if enabled
        if (config.getBoolean("discord.use-rich-embeds", true)) {
            try {
                MessageEmbed embed = createStaffLogEmbed(logType, playerName, action, target, details);
                channel.sendMessageEmbeds(embed).queue();
                plugin.getLogger().info("Sent staff log embed to Discord: " + logType);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send staff log embed: " + e.getMessage());
                // Fallback to plain message
                sendPlainStaffLog(channel, logType, playerName, action, target, details);
            }
        } else {
            sendPlainStaffLog(channel, logType, playerName, action, target, details);
        }
    }

    private MessageEmbed createStaffLogEmbed(String logType, String playerName, String action, String target, String details) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTimestamp(Instant.now());
        
        Color color;
        String title;
        
        switch (logType.toLowerCase()) {
            case "punishment-log":
                color = Color.RED;
                title = "⚖️ Punishment Log";
                embed.addField("Staff", playerName, true);
                embed.addField("Action", action, true);
                embed.addField("Target", target, false);
                if (!details.isEmpty()) {
                    embed.addField("Reason", details, false);
                }
                break;
            case "report-log":
                color = Color.ORANGE;
                title = "📋 Report Log";
                embed.addField("Reporter", playerName, true);
                embed.addField("Action", action, true);
                embed.addField("Target", target, false);
                if (!details.isEmpty()) {
                    embed.addField("Reason", details, false);
                }
                break;
            case "home-log":
                color = Color.BLUE;
                title = "🏠 Home Log";
                embed.addField("Player", playerName, true);
                embed.addField("Action", action, true);
                embed.addField("Home", target, false);
                if (!details.isEmpty()) {
                    embed.addField("Location", details, false);
                }
                break;
            case "shop-log":
            case "adminshop-log":
            case "playershop-log":
                color = Color.GREEN;
                title = "🛒 Shop Log";
                embed.addField("Player", playerName, true);
                embed.addField("Action", action, true);
                embed.addField("Item", target, false);
                if (!details.isEmpty()) {
                    embed.addField("Details", details, false);
                }
                break;
            default:
                color = Color.GRAY;
                title = "📝 Staff Log";
                embed.addField("Player", playerName, true);
                embed.addField("Action", action, true);
                embed.addField("Target", target, false);
                if (!details.isEmpty()) {
                    embed.addField("Details", details, false);
                }
        }
        
        embed.setTitle(title);
        embed.setColor(color);
        embed.setFooter("Ecore Plugin", null);
        
        return embed.build();
    }

    private void sendPlainStaffLog(TextChannel channel, String logType, String playerName, String action, String target, String details) {
        String message;
        switch (logType.toLowerCase()) {
            case "shop-log":
                message = String.format("**Shop Edit Log**\nPlayer: %s\nAction: %s\nTarget: %s\nDetails: %s",
                        playerName, action, target, details);
                break;
            case "report-log":
                message = String.format("**Report Log**\nReporter: %s\nAction: %s\nTarget: %s\nReason: %s",
                        playerName, action, target, details);
                break;
            case "home-log":
                message = String.format("**Home Log**\nPlayer: %s\nAction: %s\nHome: %s\nLocation: %s",
                        playerName, action, target, details);
                break;
            case "punishment-log":
                message = String.format("**Punishment Log**\nStaff: %s\nAction: %s\nTarget: %s\nReason: %s",
                        playerName, action, target, details);
                break;
            default:
                message = String.format("**Staff Log**\nPlayer: %s\nAction: %s\nTarget: %s\nDetails: %s",
                        playerName, action, target, details);
        }

        try {
            channel.sendMessage(message).queue();
            plugin.getLogger().info("Sent staff log to Discord: " + message);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send staff log: " + e.getMessage());
        }
    }

    /**
     * Sends a player join notification to Discord with rich embed.
     */
    public void sendPlayerJoinNotification(Player player) {
        if (!config.getBoolean("discord.notify-player-join", false)) {
            return;
        }
        
        if (jda == null) return;

        String channelId = config.getString("discord.channel-id");
        TextChannel channel = getChannel(channelId);
        if (channel == null) return;

        if (config.getBoolean("discord.use-rich-embeds", true)) {
            try {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("✅ Player Joined");
                embed.setColor(Color.GREEN);
                embed.setDescription(player.getName() + " joined the server");
                embed.addField("Player", player.getName(), true);
                embed.addField("Online Players", String.valueOf(Bukkit.getOnlinePlayers().size()), true);
                embed.setTimestamp(Instant.now());
                embed.setFooter("Ecore Plugin", null);
                
                channel.sendMessageEmbeds(embed.build()).queue();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send player join notification: " + e.getMessage());
            }
        } else {
            try {
                channel.sendMessage("✅ **" + player.getName() + "** joined the server! (" + 
                    Bukkit.getOnlinePlayers().size() + " online)").queue();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send player join notification: " + e.getMessage());
            }
        }
    }

    /**
     * Sends a player leave notification to Discord with rich embed.
     */
    public void sendPlayerLeaveNotification(Player player) {
        if (!config.getBoolean("discord.notify-player-leave", false)) {
            return;
        }
        
        if (jda == null) return;

        String channelId = config.getString("discord.channel-id");
        TextChannel channel = getChannel(channelId);
        if (channel == null) return;

        if (config.getBoolean("discord.use-rich-embeds", true)) {
            try {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("❌ Player Left");
                embed.setColor(Color.RED);
                embed.setDescription(player.getName() + " left the server");
                embed.addField("Player", player.getName(), true);
                embed.addField("Online Players", String.valueOf(Bukkit.getOnlinePlayers().size()), true);
                embed.setTimestamp(Instant.now());
                embed.setFooter("Ecore Plugin", null);
                
                channel.sendMessageEmbeds(embed.build()).queue();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send player leave notification: " + e.getMessage());
            }
        } else {
            try {
                channel.sendMessage("❌ **" + player.getName() + "** left the server. (" + 
                    Bukkit.getOnlinePlayers().size() + " online)").queue();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send player leave notification: " + e.getMessage());
            }
        }
    }

    private TextChannel getChannel(String channelId) {
        if (channelId == null || channelId.isEmpty() || channelId.equals("YOUR_CHANNEL_ID_HERE")) {
            plugin.getLogger().warning("Invalid or missing channel ID in discordconf.yml.");
            return null;
        }

        try {
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel == null) {
                plugin.getLogger().warning("Discord channel not found for ID: " + channelId);
                return null;
            }
            if (!channel.canTalk()) {
                plugin.getLogger().warning("Bot lacks permission to send messages in channel: " + channelId);
                return null;
            }
            return channel;
        } catch (Exception e) {
            plugin.getLogger().warning("Error retrieving Discord channel " + channelId + ": " + e.getMessage());
            return null;
        }
    }

    public void shutdownBot() {
        if (jda != null) {
            sendServerStopNotification();
            try {
                jda.shutdown();
                plugin.getLogger().info("Discord bot shut down successfully.");
            } catch (Exception e) {
                plugin.getLogger().warning("Error shutting down Discord bot: " + e.getMessage());
            }
        }
    }
}