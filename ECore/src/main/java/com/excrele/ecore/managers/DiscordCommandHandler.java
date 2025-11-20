package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles Discord slash commands for server management and information.
 */
public class DiscordCommandHandler extends ListenerAdapter {
    private final Ecore plugin;
    private final DiscordManager discordManager;

    public DiscordCommandHandler(Ecore plugin, DiscordManager discordManager) {
        this.plugin = plugin;
        this.discordManager = discordManager;
    }

    /**
     * Registers all slash commands with Discord.
     */
    public void registerCommands() {
        if (discordManager.getJDA() == null) return;

        discordManager.getJDA().updateCommands().addCommands(
            Commands.slash("serverinfo", "Get server information and status"),
            Commands.slash("online", "List all online players"),
            Commands.slash("playerinfo", "Get information about a player")
                .addOption(OptionType.STRING, "player", "Player name", true),
            Commands.slash("report", "Report a player from Discord")
                .addOption(OptionType.STRING, "player", "Player to report", true)
                .addOption(OptionType.STRING, "reason", "Reason for report", true),
            Commands.slash("link", "Link your Discord account to Minecraft")
                .addOption(OptionType.STRING, "code", "Verification code from /link in-game", true),
            Commands.slash("unlink", "Unlink your Discord account from Minecraft"),
            Commands.slash("staff", "Execute staff actions (requires staff role)")
                .addOption(OptionType.STRING, "action", "Action: ban, kick, mute, unmute", true)
                .addOption(OptionType.STRING, "player", "Target player", true)
                .addOption(OptionType.STRING, "reason", "Reason (optional)", false),
            Commands.slash("execute", "Execute console command (requires admin role)")
                .addOption(OptionType.STRING, "command", "Command to execute", true)
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();

        switch (command) {
            case "serverinfo":
                handleServerInfo(event);
                break;
            case "online":
                handleOnline(event);
                break;
            case "playerinfo":
                handlePlayerInfo(event);
                break;
            case "report":
                handleReport(event);
                break;
            case "link":
                handleLink(event);
                break;
            case "unlink":
                handleUnlink(event);
                break;
            case "staff":
                handleStaff(event);
                break;
            case "execute":
                handleExecute(event);
                break;
        }
    }

    private void handleServerInfo(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        ServerInfoManager serverInfo = plugin.getServerInfoManager();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🖥️ Server Information");
        embed.setColor(Color.CYAN);
        embed.setTimestamp(Instant.now());

        // Server Status
        double tps = serverInfo.getTPS();
        embed.addField("TPS", String.format("%.2f", tps), true);
        
        int online = serverInfo.getOnlinePlayers();
        int max = serverInfo.getMaxPlayers();
        embed.addField("Players", online + " / " + max, true);
        
        embed.addField("Uptime", serverInfo.getUptime(), true);
        embed.addField("Memory", serverInfo.getMemoryFormatted().replaceAll("§[0-9a-fk-or]", ""), true);
        embed.addField("Worlds", String.join(", ", serverInfo.getWorldNames()), false);
        embed.addField("Loaded Chunks", String.valueOf(serverInfo.getTotalChunks()), true);
        embed.addField("Entities", String.valueOf(serverInfo.getTotalEntities()), true);
        embed.addField("Version", serverInfo.getBukkitVersion(), true);

        embed.setFooter("Ecore Plugin", null);
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    private void handleOnline(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        List<Player> players = Bukkit.getOnlinePlayers().stream().collect(Collectors.toList());
        
        if (players.isEmpty()) {
            event.getHook().sendMessage("❌ No players online.").queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("👥 Online Players (" + players.size() + ")");
        embed.setColor(Color.GREEN);
        embed.setTimestamp(Instant.now());

        StringBuilder playerList = new StringBuilder();
        for (int i = 0; i < Math.min(players.size(), 20); i++) {
            Player p = players.get(i);
            playerList.append("• ").append(p.getName());
            if (plugin.getAccountLinkManager() != null) {
                String discordId = plugin.getAccountLinkManager().getDiscordId(p);
                if (discordId != null) {
                    Member member = event.getGuild().getMemberById(discordId);
                    if (member != null) {
                        playerList.append(" (").append(member.getEffectiveName()).append(")");
                    }
                }
            }
            playerList.append("\n");
        }
        
        if (players.size() > 20) {
            playerList.append("\n*... and ").append(players.size() - 20).append(" more*");
        }

        embed.setDescription(playerList.toString());
        embed.setFooter("Ecore Plugin", null);
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    private void handlePlayerInfo(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String playerName = event.getOption("player", OptionMapping::getAsString);
        if (playerName == null) {
            event.getHook().sendMessage("❌ Please provide a player name.").queue();
            return;
        }

        Player player = Bukkit.getPlayer(playerName);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if (player == null && !offlinePlayer.hasPlayedBefore()) {
            event.getHook().sendMessage("❌ Player not found.").queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("👤 Player Information: " + (player != null ? player.getName() : offlinePlayer.getName()));
        embed.setColor(Color.BLUE);
        embed.setTimestamp(Instant.now());

        UUID uuid = player != null ? player.getUniqueId() : offlinePlayer.getUniqueId();
        boolean online = player != null && player.isOnline();

        embed.addField("Status", online ? "🟢 Online" : "🔴 Offline", true);
        
        if (online) {
            embed.addField("Ping", player.getPing() + "ms", true);
            embed.addField("World", player.getWorld().getName(), true);
        }

        // Economy
        double balance = plugin.getEconomyManager().getBalance(uuid);
        embed.addField("Balance", String.format("%.2f", balance), true);

        // Statistics
        if (plugin.getStatisticsManager() != null) {
            int deaths = plugin.getStatisticsManager().getStatistic(
                player != null ? player : offlinePlayer.getPlayer(), "deaths");
            int kills = plugin.getStatisticsManager().getStatistic(
                player != null ? player : offlinePlayer.getPlayer(), "kills");
            embed.addField("Deaths", String.valueOf(deaths), true);
            embed.addField("Kills", String.valueOf(kills), true);
        }

        // Account Link
        if (plugin.getAccountLinkManager() != null) {
            String discordId = plugin.getAccountLinkManager().getDiscordId(uuid);
            if (discordId != null) {
                Member member = event.getGuild().getMemberById(discordId);
                if (member != null) {
                    embed.addField("Discord", member.getEffectiveName(), true);
                }
            }
        }

        embed.setFooter("Ecore Plugin", null);
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    private void handleReport(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String playerName = event.getOption("player", OptionMapping::getAsString);
        String reason = event.getOption("reason", OptionMapping::getAsString);

        if (playerName == null || reason == null) {
            event.getHook().sendMessage("❌ Please provide both player name and reason.").queue();
            return;
        }

        String reporter = event.getUser().getName();
        UUID reporterUuid = null;
        if (plugin.getAccountLinkManager() != null) {
            reporterUuid = plugin.getAccountLinkManager().getMinecraftUuid(event.getUser().getId());
        }

        String reporterName = reporter;
        if (reporterUuid != null) {
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(reporterUuid);
            if (offlinePlayer.hasPlayedBefore()) {
                reporterName = offlinePlayer.getName();
            }
        }

        plugin.getReportManager().createReport(reporterName, playerName, reason);

        event.getHook().sendMessage("✅ Report submitted successfully!").queue();
    }

    private void handleLink(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String code = event.getOption("code", OptionMapping::getAsString);
        if (code == null) {
            event.getHook().sendMessage("❌ Please provide a verification code.").queue();
            return;
        }

        if (plugin.getAccountLinkManager() == null) {
            event.getHook().sendMessage("❌ Account linking is not available.").queue();
            return;
        }

        // Find player with this code
        UUID targetUuid = null;
        for (UUID uuid : plugin.getAccountLinkManager().getPendingLinks().keySet()) {
            String pendingCode = plugin.getAccountLinkManager().getPendingLinks().get(uuid);
            if (code.equals(pendingCode)) {
                targetUuid = uuid;
                break;
            }
        }

        if (targetUuid == null) {
            event.getHook().sendMessage("❌ Invalid or expired verification code.").queue();
            return;
        }

        boolean linked = plugin.getAccountLinkManager().linkAccount(
            event.getUser().getId(),
            targetUuid,
            code
        );

        if (linked) {
            event.getHook().sendMessage("✅ Account linked successfully!").queue();
        } else {
            event.getHook().sendMessage("❌ Failed to link account.").queue();
        }
    }

    private void handleUnlink(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        if (plugin.getAccountLinkManager() == null) {
            event.getHook().sendMessage("❌ Account linking is not available.").queue();
            return;
        }

        UUID uuid = plugin.getAccountLinkManager().getMinecraftUuid(event.getUser().getId());
        if (uuid == null) {
            event.getHook().sendMessage("❌ Your account is not linked.").queue();
            return;
        }

        plugin.getAccountLinkManager().unlinkAccount(uuid);
        event.getHook().sendMessage("✅ Account unlinked successfully.").queue();
    }

    private void handleStaff(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        // Check permissions
        if (!discordManager.hasPermission(event.getMember(), "staff")) {
            event.getHook().sendMessage("❌ You don't have permission to use staff commands.").queue();
            return;
        }

        String action = event.getOption("action", OptionMapping::getAsString);
        String playerName = event.getOption("player", OptionMapping::getAsString);
        String reason = event.getOption("reason", OptionMapping::getAsString);

        if (action == null || playerName == null) {
            event.getHook().sendMessage("❌ Please provide action and player name.").queue();
            return;
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            event.getHook().sendMessage("❌ Player not found or offline.").queue();
            return;
        }

        // Execute staff action
        switch (action.toLowerCase()) {
            case "ban":
                plugin.getStaffManager().banPlayer(null, playerName, reason != null ? reason : "No reason");
                event.getHook().sendMessage("✅ Banned " + playerName).queue();
                break;
            case "kick":
                plugin.getStaffManager().kickPlayer(null, playerName, reason != null ? reason : "No reason");
                event.getHook().sendMessage("✅ Kicked " + playerName).queue();
                break;
            case "mute":
                plugin.getChatManager().mutePlayer(target, 0); // 0 = permanent mute
                if (reason != null) {
                    target.sendMessage("§cYou have been muted: " + reason);
                }
                event.getHook().sendMessage("✅ Muted " + playerName).queue();
                break;
            case "unmute":
                plugin.getChatManager().unmutePlayer(target);
                event.getHook().sendMessage("✅ Unmuted " + playerName).queue();
                break;
            default:
                event.getHook().sendMessage("❌ Unknown action: " + action).queue();
        }
    }

    private void handleExecute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        // Check permissions
        if (!discordManager.hasPermission(event.getMember(), "admin")) {
            event.getHook().sendMessage("❌ You don't have permission to execute commands.").queue();
            return;
        }

        String command = event.getOption("command", OptionMapping::getAsString);
        if (command == null) {
            event.getHook().sendMessage("❌ Please provide a command to execute.").queue();
            return;
        }

        // Execute command
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            plugin.getDiscordManager().sendStaffLogNotification(
                "command-execution",
                event.getMember().getEffectiveName(),
                "executed",
                command,
                "From Discord"
            );
        });

        event.getHook().sendMessage("✅ Command executed: `" + command + "`").queue();
    }
}

