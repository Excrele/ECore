package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class ReportCommand implements CommandExecutor {
    private final Ecore plugin;

    public ReportCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("ecore.report")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to report!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /report <player> <reason>");
            return true;
        }

        String targetName = args[0];
        String reason = String.join(" ", args).substring(targetName.length() + 1);

        if (plugin.getReportManager().createReport(player.getName(), targetName, reason)) {
            player.sendMessage(ChatColor.GREEN + "Report submitted against " + targetName + " for: " + reason);
        } else {
            player.sendMessage(ChatColor.RED + "You cannot submit this report. Check your report limit or cooldown.");
        }
        return true;
    }
}