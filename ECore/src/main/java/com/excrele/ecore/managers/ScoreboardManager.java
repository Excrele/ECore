package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages custom scoreboards for players.
 */
public class ScoreboardManager {
    private final Ecore plugin;
    private File scoreboardFile;
    private FileConfiguration scoreboardConfig;
    private final java.util.Map<UUID, Scoreboard> playerScoreboards;
    private int updateTaskId;

    public ScoreboardManager(Ecore plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new ConcurrentHashMap<>();
        initializeScoreboardConfig();
        startScoreboardUpdates();
    }

    private void initializeScoreboardConfig() {
        scoreboardFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        if (!scoreboardFile.exists()) {
            plugin.saveResource("scoreboard.yml", false);
        }
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
    }

    /**
     * Sets up scoreboard for a player.
     */
    public void setupScoreboard(Player player) {
        if (!isScoreboardEnabled()) return;
        if (!player.hasPermission("ecore.scoreboard.use")) return;

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("ecore", Criteria.DUMMY, getTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateScoreboard(player, scoreboard, objective);
        player.setScoreboard(scoreboard);
        playerScoreboards.put(player.getUniqueId(), scoreboard);
    }

    /**
     * Updates a player's scoreboard.
     */
    public void updateScoreboard(Player player, Scoreboard scoreboard, Objective objective) {
        if (objective == null) return;

        List<String> lines = getScoreboardLines(player);
        
        // Clear existing lines
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        // Add new lines (in reverse order for scoreboard)
        int score = lines.size();
        for (String line : lines) {
            if (line == null || line.isEmpty()) continue;
            
            String processedLine = processPlaceholders(player, line);
            if (processedLine.length() > 40) {
                processedLine = processedLine.substring(0, 40);
            }
            
            Team team = scoreboard.getTeam("line" + score);
            if (team == null) {
                team = scoreboard.registerNewTeam("line" + score);
            }
            
            String entry = getEntryForScore(score);
            team.addEntry(entry);
            team.setPrefix(processedLine);
            objective.getScore(entry).setScore(score);
            score--;
        }
    }

    /**
     * Removes scoreboard from a player.
     */
    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    /**
     * Toggles scoreboard for a player.
     */
    public void toggleScoreboard(Player player) {
        if (playerScoreboards.containsKey(player.getUniqueId())) {
            removeScoreboard(player);
            player.sendMessage("§cScoreboard disabled.");
        } else {
            setupScoreboard(player);
            player.sendMessage("§aScoreboard enabled.");
        }
    }

    /**
     * Starts automatic scoreboard updates.
     */
    private void startScoreboardUpdates() {
        if (!isScoreboardEnabled()) return;

        int interval = scoreboardConfig.getInt("update-interval", 20); // Default: 1 second
        
        updateTaskId = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!playerScoreboards.containsKey(player.getUniqueId())) continue;
                
                Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
                Objective objective = scoreboard.getObjective("ecore");
                if (objective != null) {
                    updateScoreboard(player, scoreboard, objective);
                }
            }
        }, 0L, interval).getTaskId();
    }

    /**
     * Gets the scoreboard title.
     */
    private String getTitle() {
        String title = scoreboardConfig.getString("title", "§6§lYour Server");
        return processColorCodes(title);
    }

    /**
     * Gets the scoreboard lines for a player.
     */
    private List<String> getScoreboardLines(Player player) {
        List<String> lines = scoreboardConfig.getStringList("lines");
        
        // If no lines configured, use default
        if (lines.isEmpty()) {
            lines = getDefaultLines();
        }
        
        return lines;
    }

    /**
     * Gets default scoreboard lines.
     */
    private List<String> getDefaultLines() {
        return java.util.Arrays.asList(
            "§7§m-------------------",
            "§eBalance: §a%ecore_balance%",
            "§eHomes: §a%ecore_homes%/%ecore_max_homes%",
            "§eKills: §a%ecore_kills%",
            "§7§m-------------------"
        );
    }

    /**
     * Processes placeholders in a string.
     */
    private String processPlaceholders(Player player, String text) {
        text = processColorCodes(text);
        
        // Replace ECore placeholders
        text = text.replace("%player%", player.getName());
        text = text.replace("%balance%", String.format("%.2f", plugin.getEconomyManager().getBalance(player.getUniqueId())));
        text = text.replace("%homes%", String.valueOf(plugin.getHomeManager().getPlayerHomes(player).size()));
        text = text.replace("%kills%", String.valueOf(plugin.getStatisticsManager().getStatistic(player, "kills")));
        text = text.replace("%deaths%", String.valueOf(plugin.getStatisticsManager().getStatistic(player, "deaths")));
        
        // PlaceholderAPI support
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        
        return text;
    }

    /**
     * Processes color codes.
     */
    private String processColorCodes(String text) {
        return text.replace("&", "§");
    }

    /**
     * Gets a unique entry for a score.
     */
    private String getEntryForScore(int score) {
        return org.bukkit.ChatColor.values()[score % org.bukkit.ChatColor.values().length].toString() + "§r";
    }

    /**
     * Checks if scoreboard is enabled.
     */
    private boolean isScoreboardEnabled() {
        return scoreboardConfig.getBoolean("enabled", true);
    }

    /**
     * Reloads scoreboard configuration.
     */
    public void reload() {
        initializeScoreboardConfig();
        
        // Re-setup scoreboards for all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (playerScoreboards.containsKey(player.getUniqueId())) {
                setupScoreboard(player);
            }
        }
    }

    /**
     * Shuts down the scoreboard manager.
     */
    public void shutdown() {
        if (updateTaskId != 0) {
            plugin.getServer().getScheduler().cancelTask(updateTaskId);
        }
    }
}

