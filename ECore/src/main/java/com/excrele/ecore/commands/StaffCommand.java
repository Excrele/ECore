package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffCommand implements CommandExecutor {
    private final Ecore plugin;

    public StaffCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("mute")) {
            return handleMute(sender, args);
        } else if (cmd.equals("unmute")) {
            return handleUnmute(sender, args);
        } else if (cmd.equals("freeze")) {
            return handleFreeze(sender, args);
        } else if (cmd.equals("unfreeze")) {
            return handleUnfreeze(sender, args);
        } else if (cmd.equals("commandspy")) {
            return handleCommandSpy(sender);
        } else if (cmd.equals("socialspy")) {
            return handleSocialSpy(sender);
        } else if (cmd.equals("give")) {
            return handleGive(sender, args);
        } else if (cmd.equals("enchant")) {
            return handleEnchant(sender, args);
        } else if (cmd.equals("repair")) {
            return handleRepair(sender, args);
        }

        return false;
    }

    private boolean handleMute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.staff")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /mute <player> [duration in seconds]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        long duration = 0; // 0 = permanent
        if (args.length >= 2) {
            try {
                duration = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid duration! Use seconds.");
                return true;
            }
        }

        plugin.getChatManager().mutePlayer(target, duration);
        sender.sendMessage(ChatColor.GREEN + "Muted " + target.getName() + 
            (duration > 0 ? " for " + duration + " seconds" : " permanently") + "!");
        
        plugin.getDiscordManager().sendStaffLogNotification(
            "punishment-log",
            sender.getName(),
            "muted",
            target.getName(),
            duration > 0 ? duration + " seconds" : "permanently"
        );
        return true;
    }

    private boolean handleUnmute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.staff")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unmute <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        plugin.getChatManager().unmutePlayer(target);
        sender.sendMessage(ChatColor.GREEN + "Unmuted " + target.getName() + "!");
        return true;
    }

    private boolean handleFreeze(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.staff")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /freeze <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        plugin.getStaffManager().freezePlayer(target);
        sender.sendMessage(ChatColor.GREEN + "Froze " + target.getName() + "!");
        return true;
    }

    private boolean handleUnfreeze(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.staff")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unfreeze <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        plugin.getStaffManager().unfreezePlayer(target);
        sender.sendMessage(ChatColor.GREEN + "Unfroze " + target.getName() + "!");
        return true;
    }

    private boolean handleCommandSpy(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ecore.staff")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        boolean enabled = plugin.getStaffManager().isCommandSpyEnabled(player);
        plugin.getStaffManager().setCommandSpyEnabled(player, !enabled);
        return true;
    }

    private boolean handleSocialSpy(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ecore.staff")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        boolean enabled = plugin.getStaffManager().isSocialSpyEnabled(player);
        plugin.getStaffManager().setSocialSpyEnabled(player, !enabled);
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.staff")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /give <player> <item> [amount]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount!");
                return true;
            }
        }

        plugin.getStaffManager().giveItem(target, args[1], amount);
        sender.sendMessage(ChatColor.GREEN + "Gave " + amount + "x " + args[1] + " to " + target.getName() + "!");
        return true;
    }

    private boolean handleEnchant(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.staff")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /enchant <player> <enchantment> <level>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        int level;
        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid level!");
            return true;
        }

        plugin.getStaffManager().enchantItem(target, args[1], level);
        sender.sendMessage(ChatColor.GREEN + "Enchanted " + target.getName() + "'s item with " + args[1] + " " + level + "!");
        return true;
    }

    private boolean handleRepair(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.staff")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        boolean all = args.length > 0 && args[0].equalsIgnoreCase("all");

        plugin.getStaffManager().repairItem(player, all);
        player.sendMessage(ChatColor.GREEN + "Repaired " + (all ? "all items" : "item in hand") + "!");
        return true;
    }
}

