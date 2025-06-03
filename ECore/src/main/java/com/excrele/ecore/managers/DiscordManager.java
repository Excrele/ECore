package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import javax.security.auth.login.LoginException;

public class DiscordManager extends ListenerAdapter implements Listener {
    private final Ecore plugin;
    private JDA jda;
    private TextChannel chatChannel;
    private TextChannel punishmentChannel;
    private final FileConfiguration discordConfig;

    public DiscordManager(Ecore plugin) {
        this.plugin = plugin;
        this.discordConfig = plugin.getConfigManager().getDiscordConfig();

        // Register this class as a Bukkit listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Initialize Discord bot if enabled
        if (discordConfig.getBoolean("discord.enabled")) {
            initializeBot();
        }
    }

    // Initialize the Discord bot
    private void initializeBot() {
        String token = discordConfig.getString("discord.bot-token");
        if (token == null || token.equals("INSERT_TOKEN_HERE")) {
            plugin.getLogger().warning("Discord bot token is not set in discordconf.yml. Disabling Discord integration.");
            return;
        }

        try {
            jda = JDABuilder.createDefault(token)
                    .addEventListeners(this)
                    .build();
            jda.awaitReady(); // Wait for bot to be ready

            // Initialize chat channel
            String channelId = discordConfig.getString("discord.channel-id");
            if (channelId == null || channelId.equals("INSERT_CHANNEL_ID")) {
                plugin.getLogger().warning("Discord chat channel ID is not set in discordconf.yml. Disabling chat bridging.");
            } else {
                chatChannel = jda.getTextChannelById(channelId);
                if (chatChannel == null) {
                    plugin.getLogger().warning("Invalid Discord chat channel ID in discordconf.yml. Disabling chat bridging.");
                }
            }

            // Initialize punishment channel
            String punishmentChannelId = discordConfig.getString("discord.punishment-channel-id");
            if (punishmentChannelId == null || punishmentChannelId.equals("INSERT_PUNISHMENT_CHANNEL_ID")) {
                plugin.getLogger().warning("Discord punishment channel ID is not set in discordconf.yml. Punishment logging disabled.");
            } else {
                punishmentChannel = jda.getTextChannelById(punishmentChannelId);
                if (punishmentChannel == null) {
                    plugin.getLogger().warning("Invalid Discord punishment channel ID in discordconf.yml. Punishment logging disabled.");
                }
            }

            plugin.getLogger().info("Discord bot connected and ready!");
        } catch (LoginException e) {
            plugin.getLogger().warning("Failed to login to Discord bot: Invalid token.");
        } catch (InterruptedException e) {
            plugin.getLogger().warning("Discord bot initialization interrupted.");
        }
    }

    // Send a punishment notification to the punishment channel
    public void sendPunishmentNotification(String staff, String action, String target, String reason) {
        if (punishmentChannel == null || !discordConfig.getBoolean("discord.enabled")) return;

        String format = discordConfig.getString("discord.message-formats.punishment-log", "[Punishment] %staff% %action% %target%: %reason%");
        String message = format.replace("%staff%", staff)
                .replace("%action%", action)
                .replace("%target%", target)
                .replace("%reason%", reason);
        punishmentChannel.sendMessage(message).queue();
    }

    // Handle Discord messages
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.getChannel().getId().equals(discordConfig.getString("discord.channel-id"))) {
            return; // Ignore bot messages and messages from other channels
        }

        String format = discordConfig.getString("discord.message-formats.discord-to-minecraft", "&7[Discord] &f%user%: %message%");
        String message = format.replace("%user%", event.getAuthor().getName())
                .replace("%message%", event.getMessage().getContentDisplay());
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    // Handle Minecraft chat messages
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (chatChannel == null || !discordConfig.getBoolean("discord.enabled")) return;

        String format = discordConfig.getString("discord.message-formats.minecraft-to-discord", "[Minecraft] %player%: %message%");
        String message = format.replace("%player%", event.getPlayer().getName())
                .replace("%message%", event.getMessage());
        chatChannel.sendMessage(message).queue();
    }

    // Shutdown the Discord bot
    public void shutdownBot() {
        if (jda != null) {
            jda.shutdown();
            plugin.getLogger().info("Discord bot disconnected.");
        }
    }

    // Get JDA instance (for future extensions)
    public JDA getJDA() {
        return jda;
    }

    // Get chat channel (for future extensions)
    public TextChannel getChatChannel() {
        return chatChannel;
    }
}