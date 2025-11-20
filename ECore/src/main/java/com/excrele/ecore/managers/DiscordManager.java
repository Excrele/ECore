package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Enhanced Discord integration manager with comprehensive features:
 * - Chat bridging with filtering and rate limiting
 * - Rich embeds for all notifications
 * - Discord slash commands
 * - Account linking
 * - Webhook support
 * - Role-based permissions
 * - Message queue system
 * - Scheduled reports
 * - Economy transaction logging
 * - Achievement notifications
 */
public class DiscordManager {
    private final Ecore plugin;
    private JDA jda;
    private final FileConfiguration config;
    private DiscordCommandHandler commandHandler;
    private DiscordMessageQueue messageQueue;
    private final Map<String, Long> rateLimitMap; // User ID -> Last message time
    private final List<Pattern> filteredWords;
    private int statusUpdateTaskId = -1;

    public DiscordManager(Ecore plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager().getDiscordConfig();
        this.rateLimitMap = new ConcurrentHashMap<>();
        this.filteredWords = new ArrayList<>();
        this.messageQueue = new DiscordMessageQueue(plugin);
        
        loadFilteredWords();
        
        if (config.getBoolean("discord.enabled")) {
            initializeBot();
        } else {
            plugin.getLogger().info("Discord bot is disabled in discordconf.yml.");
        }
    }

    private void loadFilteredWords() {
        List<String> words = config.getStringList("discord.chat-filter.filtered-words");
        for (String word : words) {
            filteredWords.add(Pattern.compile("(?i)" + Pattern.quote(word)));
        }
    }

    private void initializeBot() {
        String token = config.getString("discord.bot-token");
        if (token == null || token.isEmpty() || token.equals("INSERT_TOKEN_HERE")) {
            plugin.getLogger().warning("Invalid or missing Discord bot token in discordconf.yml. Bot will not start.");
            return;
        }

        try {
            commandHandler = new DiscordCommandHandler(plugin, this);
            jda = JDABuilder.createDefault(token)
                    .enableIntents(
                        GatewayIntent.GUILD_MESSAGES, 
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS
                    )
                    .addEventListeners(new DiscordMessageListener())
                    .addEventListeners(commandHandler)
                    .build();
            jda.awaitReady();
            
            // Register slash commands
            commandHandler.registerCommands();
            
            // Start status channel updates
            startStatusChannelUpdates();
            
            // Start scheduled reports
            startScheduledReports();
            
            plugin.getLogger().info("Discord bot connected successfully as " + jda.getSelfUser().getName());
        } catch (InterruptedException e) {
            plugin.getLogger().warning("Discord bot initialization interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize Discord bot: " + e.getMessage());
        }
    }

    public JDA getJDA() {
        return jda;
    }

    /**
     * Checks if a Discord member has permission for a specific action.
     */
    public boolean hasPermission(Member member, String permission) {
        if (member == null) return false;
        
        List<String> requiredRoles = config.getStringList("discord.permissions." + permission);
        if (requiredRoles.isEmpty()) {
            // Default: require admin role
            requiredRoles = config.getStringList("discord.permissions.admin");
        }
        
        if (requiredRoles.isEmpty()) return false;
        
        for (Role role : member.getRoles()) {
            if (requiredRoles.contains(role.getId()) || requiredRoles.contains(role.getName())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Sends chat message to Discord with filtering and rate limiting.
     */
    public void sendChatToDiscord(String playerName, String message) {
        if (jda == null) {
            messageQueue.queueMessage(config.getString("discord.channel-id"), 
                formatChatMessage(playerName, message));
            return;
        }

        String channelId = config.getString("discord.channel-id");
        TextChannel channel = getChannel(channelId);
        if (channel == null) {
            messageQueue.queueMessage(channelId, formatChatMessage(playerName, message));
            return;
        }

        String formatted = formatChatMessage(playerName, message);
        
        try {
            if (config.getBoolean("discord.use-webhooks", false)) {
                sendViaWebhook(channel, playerName, formatted);
            } else {
                channel.sendMessage(formatted).queue(
                    null,
                    error -> messageQueue.queueMessage(channelId, formatted)
                );
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send chat to Discord: " + e.getMessage());
            messageQueue.queueMessage(channelId, formatted);
        }
    }

    private String formatChatMessage(String playerName, String message) {
        String format = config.getString("discord.message-formats.minecraft-to-discord", 
            "[Minecraft] %player%: %message%");
        return format.replace("%player%", playerName).replace("%message%", message);
    }

    private void sendViaWebhook(TextChannel channel, String playerName, String message) {
        // Webhook implementation would go here
        // For now, fallback to regular message
        channel.sendMessage(message).queue();
    }

    /**
     * Enhanced message listener with filtering and rate limiting.
     */
    private class DiscordMessageListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().isBot()) return;
            if (!event.getChannel().getId().equals(config.getString("discord.channel-id"))) return;

            String userId = event.getAuthor().getId();
            String content = event.getMessage().getContentDisplay();

            // Rate limiting
            if (isRateLimited(userId)) {
                event.getMessage().delete().queue();
                return;
            }

            // Word filtering
            if (isFiltered(content)) {
                if (config.getBoolean("discord.chat-filter.delete-filtered", true)) {
                    event.getMessage().delete().queue();
                }
                if (config.getBoolean("discord.chat-filter.notify-staff", true)) {
                    sendStaffLogNotification("chat-filter", event.getAuthor().getName(), 
                        "sent filtered message", content, "");
                }
                return;
            }

            // Format and send to Minecraft
            String format = config.getString("discord.message-formats.discord-to-minecraft",
                "&7[Discord] &f%user%: %message%");
            
            // Handle mentions
            if (config.getBoolean("discord.chat-features.mention-support", true)) {
                content = processMentions(content, event);
            }
            
            String minecraftMessage = format
                .replace("%user%", event.getAuthor().getName())
                .replace("%message%", content);

            String formatted = ChatColor.translateAlternateColorCodes('&', minecraftMessage);
            Bukkit.broadcastMessage(formatted);
        }
    }

    private boolean isRateLimited(String userId) {
        long now = System.currentTimeMillis();
        long lastMessage = rateLimitMap.getOrDefault(userId, 0L);
        long cooldown = config.getLong("discord.chat-filter.rate-limit-cooldown", 1000L);
        
        if (now - lastMessage < cooldown) {
            return true;
        }
        
        rateLimitMap.put(userId, now);
        
        // Cleanup old entries
        if (rateLimitMap.size() > 1000) {
            rateLimitMap.entrySet().removeIf(entry -> now - entry.getValue() > 60000);
        }
        
        return false;
    }

    private boolean isFiltered(String message) {
        if (!config.getBoolean("discord.chat-filter.enabled", false)) {
            return false;
        }
        
        String lowerMessage = message.toLowerCase();
        for (Pattern pattern : filteredWords) {
            if (pattern.matcher(lowerMessage).find()) {
                return true;
            }
        }
        
        return false;
    }

    private String processMentions(String content, MessageReceivedEvent event) {
        // Process @player mentions
        for (Member member : event.getGuild().getMembers()) {
            if (content.contains("@" + member.getEffectiveName())) {
                Player player = Bukkit.getPlayer(member.getEffectiveName());
                if (player != null && player.isOnline()) {
                    content = content.replace("@" + member.getEffectiveName(), 
                        "@" + player.getName());
                }
            }
        }
        return content;
    }

    /**
     * Enhanced server start notification with rich embed.
     */
    public void sendServerStartNotification() {
        if (jda == null) {
            messageQueue.queueMessage(config.getString("discord.channel-id"), 
                "Server has started!");
            return;
        }

        String channelId = config.getString("discord.channel-id");
        TextChannel channel = getChannel(channelId);
        if (channel == null) return;

        if (config.getBoolean("discord.use-rich-embeds", true)) {
            try {
                MessageEmbed embed = createServerStatusEmbed(true);
                channel.sendMessageEmbeds(embed).queue(
                    null,
                    error -> messageQueue.queueEmbed(channelId, embed)
                );
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send server start embed: " + e.getMessage());
                channel.sendMessage("✅ Server has started!").queue();
            }
        } else {
            channel.sendMessage("✅ Server has started!").queue();
        }
    }

    /**
     * Enhanced server stop notification with rich embed.
     */
    public void sendServerStopNotification() {
        if (jda == null) return;

        String channelId = config.getString("discord.channel-id");
        TextChannel channel = getChannel(channelId);
        if (channel == null) return;

        if (config.getBoolean("discord.use-rich-embeds", true)) {
            try {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("🔴 Server Stopping");
                embed.setColor(Color.RED);
                embed.setDescription("The server is shutting down...");
                embed.setTimestamp(Instant.now());
                embed.setFooter("Ecore Plugin", null);
                
                channel.sendMessageEmbeds(embed.build()).queue();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send server stop embed: " + e.getMessage());
                channel.sendMessage("🔴 Server is stopping!").queue();
            }
        } else {
            channel.sendMessage("🔴 Server is stopping!").queue();
        }
    }

    private MessageEmbed createServerStatusEmbed(boolean isStart) {
        ServerInfoManager serverInfo = plugin.getServerInfoManager();
        EmbedBuilder embed = new EmbedBuilder();
        
        embed.setTitle(isStart ? "🟢 Server Started" : "🔴 Server Stopped");
        embed.setColor(isStart ? Color.GREEN : Color.RED);
        embed.setTimestamp(Instant.now());

        if (isStart) {
            double tps = serverInfo.getTPS();
            
            embed.addField("TPS", String.format("%.2f", tps), true);
            embed.addField("Players", serverInfo.getOnlinePlayers() + " / " + serverInfo.getMaxPlayers(), true);
            embed.addField("Uptime", serverInfo.getUptime(), true);
            embed.addField("Memory", serverInfo.getMemoryFormatted().replaceAll("§[0-9a-fk-or]", ""), true);
            embed.addField("Version", serverInfo.getBukkitVersion(), false);
        }

        embed.setFooter("Ecore Plugin", null);
        return embed.build();
    }

    /**
     * Enhanced staff log notification with separate channel routing.
     */
    public void sendStaffLogNotification(String logType, String playerName, String action, String target, String details) {
        if (jda == null) {
            return;
        }

        // Determine which channel to use
        String channelId;
        if (logType.equals("punishment-log")) {
            channelId = config.getString("discord.punishment-channel-id");
        } else if (config.contains("discord.staff-logs-channel-id") && 
                   !config.getString("discord.staff-logs-channel-id", "").isEmpty()) {
            channelId = config.getString("discord.staff-logs-channel-id");
        } else {
            channelId = config.getString("discord.punishment-channel-id");
        }

        TextChannel channel = getChannel(channelId);
        if (channel == null) {
            return;
        }

        if (config.getBoolean("discord.use-rich-embeds", true)) {
            try {
                MessageEmbed embed = createStaffLogEmbed(logType, playerName, action, target, details);
                channel.sendMessageEmbeds(embed).queue(
                    null,
                    error -> messageQueue.queueEmbed(channelId, embed)
                );
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send staff log embed: " + e.getMessage());
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
            case "achievement-log":
                color = new Color(255, 215, 0); // Gold color
                title = "🏆 Achievement Unlocked";
                embed.addField("Player", playerName, true);
                embed.addField("Achievement", action, true);
                if (!details.isEmpty()) {
                    embed.addField("Description", details, false);
                }
                break;
            case "economy-log":
                color = Color.CYAN;
                title = "💰 Economy Transaction";
                embed.addField("Player", playerName, true);
                embed.addField("Action", action, true);
                embed.addField("Amount", target, true);
                if (!details.isEmpty()) {
                    embed.addField("Details", details, false);
                }
                break;
            case "command-execution":
                color = Color.MAGENTA;
                title = "⚙️ Command Execution";
                embed.addField("Executor", playerName, true);
                embed.addField("Command", action, false);
                if (!details.isEmpty()) {
                    embed.addField("Source", details, false);
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
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send staff log: " + e.getMessage());
        }
    }

    /**
     * Enhanced player join notification with statistics.
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
                
                // Add statistics if enabled
                if (config.getBoolean("discord.join-leave-show-stats", true) && 
                    plugin.getStatisticsManager() != null) {
                    int joins = plugin.getStatisticsManager().getStatistic(player, "joins");
                    int deaths = plugin.getStatisticsManager().getStatistic(player, "deaths");
                    int kills = plugin.getStatisticsManager().getStatistic(player, "kills");
                    double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
                    
                    embed.addField("Total Joins", String.valueOf(joins), true);
                    embed.addField("Kills", String.valueOf(kills), true);
                    embed.addField("Deaths", String.valueOf(deaths), true);
                    embed.addField("Balance", String.format("%.2f", balance), true);
                }
                
                // Account link info
                if (plugin.getAccountLinkManager() != null) {
                    String discordId = plugin.getAccountLinkManager().getDiscordId(player);
                    if (discordId != null && jda.getUserById(discordId) != null) {
                        embed.addField("Discord", jda.getUserById(discordId).getName(), true);
                    }
                }
                
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
     * Enhanced player leave notification with statistics.
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
                
                // Add statistics if enabled
                if (config.getBoolean("discord.join-leave-show-stats", true) && 
                    plugin.getStatisticsManager() != null) {
                    int deaths = plugin.getStatisticsManager().getStatistic(player, "deaths");
                    int kills = plugin.getStatisticsManager().getStatistic(player, "kills");
                    double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
                    
                    embed.addField("Kills", String.valueOf(kills), true);
                    embed.addField("Deaths", String.valueOf(deaths), true);
                    embed.addField("Balance", String.format("%.2f", balance), true);
                }
                
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

    /**
     * Sends achievement notification to Discord.
     */
    public void sendAchievementNotification(Player player, String achievementName, String description) {
        if (!config.getBoolean("discord.notify-achievements", false)) {
            return;
        }
        
        if (jda == null) return;

        String channelId = config.getString("discord.channel-id");
        TextChannel channel = getChannel(channelId);
        if (channel == null) return;

        if (config.getBoolean("discord.use-rich-embeds", true)) {
            try {
                MessageEmbed embed = createStaffLogEmbed("achievement-log", player.getName(), 
                    achievementName, "", description);
                channel.sendMessageEmbeds(embed).queue();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send achievement notification: " + e.getMessage());
            }
        }
    }

    /**
     * Sends economy transaction log to Discord.
     */
    public void sendEconomyTransaction(String playerName, String action, double amount, String details) {
        if (!config.getBoolean("discord.log-economy-transactions", false)) {
            return;
        }
        
        double threshold = config.getDouble("discord.economy-transaction-threshold", 1000.0);
        if (Math.abs(amount) < threshold) {
            return;
        }
        
        if (jda == null) return;

        String channelId = config.getString("discord.punishment-channel-id");
        TextChannel channel = getChannel(channelId);
        if (channel == null) return;

        if (config.getBoolean("discord.use-rich-embeds", true)) {
            try {
                MessageEmbed embed = createStaffLogEmbed("economy-log", playerName, action, 
                    String.format("%.2f", amount), details);
                channel.sendMessageEmbeds(embed).queue();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send economy transaction log: " + e.getMessage());
            }
        }
    }

    /**
     * Starts status channel updates (updates channel topic with player count).
     */
    private void startStatusChannelUpdates() {
        if (!config.getBoolean("discord.status-channel.enabled", false)) {
            return;
        }

        String channelId = config.getString("discord.status-channel.channel-id");
        if (channelId == null || channelId.isEmpty()) {
            return;
        }

        long updateInterval = config.getLong("discord.status-channel.update-interval", 60L) * 20L; // Convert to ticks

        statusUpdateTaskId = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (jda == null) return;

            TextChannel channel = getChannel(channelId);
            if (channel == null) return;

            try {
                ServerInfoManager serverInfo = plugin.getServerInfoManager();
                int online = serverInfo.getOnlinePlayers();
                int max = serverInfo.getMaxPlayers();
                double tps = serverInfo.getTPS();
                
                String topic = config.getString("discord.status-channel.topic-format",
                    "🟢 Online: %online%/%max% | TPS: %tps%");
                topic = topic.replace("%online%", String.valueOf(online))
                           .replace("%max%", String.valueOf(max))
                           .replace("%tps%", String.format("%.1f", tps));
                
                channel.getManager().setTopic(topic).queue();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to update status channel: " + e.getMessage());
            }
        }, 0L, updateInterval).getTaskId();
    }

    /**
     * Starts scheduled reports (daily/weekly summaries).
     */
    private void startScheduledReports() {
        if (!config.getBoolean("discord.scheduled-reports.enabled", false)) {
            return;
        }

        String schedule = config.getString("discord.scheduled-reports.schedule", "daily");
        long delay = schedule.equals("daily") ? 1728000L : 6048000L; // 24 hours or 7 days in ticks

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            sendScheduledReport();
        }, delay, delay);
    }

    /**
     * Sends a scheduled report with server statistics.
     */
    private void sendScheduledReport() {
        if (jda == null) return;

        String channelId = config.getString("discord.scheduled-reports.channel-id",
            config.getString("discord.channel-id"));
        TextChannel channel = getChannel(channelId);
        if (channel == null) return;

        try {
            ServerInfoManager serverInfo = plugin.getServerInfoManager();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("📊 Server Statistics Report");
            embed.setColor(Color.CYAN);
            embed.setTimestamp(Instant.now());

            embed.addField("Uptime", serverInfo.getUptime(), true);
            embed.addField("Total Players", String.valueOf(Bukkit.getOfflinePlayers().length), true);
            embed.addField("Online Players", String.valueOf(serverInfo.getOnlinePlayers()), true);
            embed.addField("Average TPS", String.format("%.2f", serverInfo.getTPS()), true);
            embed.addField("Memory Usage", serverInfo.getMemoryFormatted().replaceAll("§[0-9a-fk-or]", ""), true);

            channel.sendMessageEmbeds(embed.build()).queue();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send scheduled report: " + e.getMessage());
        }
    }

    private TextChannel getChannel(String channelId) {
        if (channelId == null || channelId.isEmpty() || channelId.equals("INSERT_CHANNEL_ID")) {
            return null;
        }

        try {
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel == null) {
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
        if (statusUpdateTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(statusUpdateTaskId);
        }
        
        if (jda != null) {
            sendServerStopNotification();
            
            // Process message queue before shutdown
            String channelId = config.getString("discord.channel-id");
            TextChannel channel = getChannel(channelId);
            if (channel != null) {
                messageQueue.processQueue(channel);
            }
            
            try {
                jda.shutdown();
                plugin.getLogger().info("Discord bot shut down successfully.");
            } catch (Exception e) {
                plugin.getLogger().warning("Error shutting down Discord bot: " + e.getMessage());
            }
        }
    }
}

