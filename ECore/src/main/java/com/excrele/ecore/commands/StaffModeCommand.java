package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command handler for staff mode toggle.
 * Allows staff members to enter/exit staff mode.
 */
public class StaffModeCommand implements CommandExecutor {
    private final Ecore plugin;

    public StaffModeCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ecore.staffmode")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use staff mode!");
            return true;
        }

        // Toggle staff mode
        plugin.getStaffModeManager().toggleStaffMode(player);
        return true;
    }
}


