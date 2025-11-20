package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.JobManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Command handler for jobs system.
 */
public class JobCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public JobCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            plugin.getJobGUIManager().openJobGUI(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /jobs join <job>");
                    return true;
                }
                handleJoin(player, args[1]);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "info":
                handleInfo(player);
                break;
            case "top":
            case "leaderboard":
                handleTop(player, args.length > 1 ? args[1] : null);
                break;
            case "list":
                handleList(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /jobs for GUI or:");
                sendHelp(player);
        }

        return true;
    }

    private void handleJoin(Player player, String jobId) {
        if (plugin.getJobManager().joinJob(player, jobId)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "Job not found: " + jobId);
        }
    }

    private void handleLeave(Player player) {
        if (plugin.getJobManager().leaveJob(player)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "You are not in any job!");
        }
    }

    private void handleInfo(Player player) {
        JobManager.PlayerJobData data = plugin.getJobManager().getPlayerJobData(player.getUniqueId());
        
        if (data.getCurrentJob() == null) {
            player.sendMessage(ChatColor.YELLOW + "You are not in any job. Use /jobs to join one!");
            return;
        }

        JobManager.JobType job = plugin.getJobManager().getJobType(data.getCurrentJob());
        if (job == null) {
            player.sendMessage(ChatColor.RED + "Your current job is invalid!");
            return;
        }

        double expForNext = data.getExpForNextLevel();
        double progress = (data.getExperience() / expForNext) * 100.0;

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "          Job Information");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.WHITE + "Job: " + ChatColor.GREEN + job.getName());
        player.sendMessage(ChatColor.WHITE + "Level: " + ChatColor.GREEN + data.getLevel());
        player.sendMessage(ChatColor.WHITE + "Experience: " + ChatColor.GREEN + 
                          String.format("%.1f", data.getExperience()) + " / " + 
                          String.format("%.1f", expForNext) + 
                          ChatColor.GRAY + " (" + String.format("%.1f", progress) + "%)");
        player.sendMessage(ChatColor.WHITE + "Total Experience: " + ChatColor.GREEN + 
                          String.format("%.1f", data.getTotalExperience() + data.getExperience()));
        player.sendMessage(ChatColor.WHITE + "Total Money Earned: " + ChatColor.GREEN + 
                          String.format("%.2f", data.getTotalMoneyEarned()));
        player.sendMessage(ChatColor.WHITE + "Total Actions: " + ChatColor.GREEN + data.getTotalActions());
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
    }

    private void handleTop(Player player, String jobId) {
        if (jobId == null) {
            JobManager.PlayerJobData data = plugin.getJobManager().getPlayerJobData(player.getUniqueId());
            if (data.getCurrentJob() == null) {
                player.sendMessage(ChatColor.RED + "Usage: /jobs top <job>");
                return;
            }
            jobId = data.getCurrentJob();
        }

        JobManager.JobType job = plugin.getJobManager().getJobType(jobId);
        if (job == null) {
            player.sendMessage(ChatColor.RED + "Job not found: " + jobId);
            return;
        }

        List<Map.Entry<UUID, JobManager.PlayerJobData>> leaderboard = 
            plugin.getJobManager().getLeaderboard(jobId, 10);

        if (leaderboard.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No players found for " + job.getName() + " job.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "     " + job.getName() + " Leaderboard");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");

        int rank = 1;
        for (Map.Entry<UUID, JobManager.PlayerJobData> entry : leaderboard) {
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            if (name == null) name = "Unknown";
            
            ChatColor rankColor = rank <= 3 ? ChatColor.GOLD : ChatColor.WHITE;
            String rankPrefix = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "#" + rank;
            
            player.sendMessage(rankColor + rankPrefix + " " + name + 
                             ChatColor.GRAY + " - Level " + ChatColor.GREEN + entry.getValue().getLevel() +
                             ChatColor.GRAY + " (" + String.format("%.1f", entry.getValue().getTotalExperience() + entry.getValue().getExperience()) + " exp)");
            rank++;
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
    }

    private void handleList(Player player) {
        Map<String, JobManager.JobType> jobs = plugin.getJobManager().getJobTypes();
        
        if (jobs.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No jobs available.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "          Available Jobs");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");

        for (JobManager.JobType job : jobs.values()) {
            player.sendMessage(ChatColor.WHITE + "• " + ChatColor.GREEN + job.getName() + 
                             ChatColor.GRAY + " - " + job.getDescription());
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "Use /jobs join <job> to join a job!");
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Job Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/jobs - Open jobs GUI");
        player.sendMessage(ChatColor.YELLOW + "/jobs join <job> - Join a job");
        player.sendMessage(ChatColor.YELLOW + "/jobs leave - Leave current job");
        player.sendMessage(ChatColor.YELLOW + "/jobs info - View your job info");
        player.sendMessage(ChatColor.YELLOW + "/jobs top [job] - View leaderboard");
        player.sendMessage(ChatColor.YELLOW + "/jobs list - List available jobs");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("join", "leave", "info", "top", "list", "leaderboard")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            return new ArrayList<>(plugin.getJobManager().getJobTypes().keySet())
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("top") || args[0].equalsIgnoreCase("leaderboard"))) {
            return new ArrayList<>(plugin.getJobManager().getJobTypes().keySet())
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}

