package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatCommand implements CommandExecutor {
    private final Ecore plugin;

    public ChatCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("msg") || cmd.equals("message") || cmd.equals("tell") || cmd.equals("whisper")) {
            return handleMessage(sender, args);
        } else if (cmd.equals("reply") || cmd.equals("r")) {
            return handleReply(sender, args);
        } else if (cmd.equals("chat")) {
            return handleChat(sender, args);
        } else if (cmd.equals("sc") || cmd.equals("staffchat")) {
            return handleStaffChat(sender, args);
        } else if (cmd.equals("ac") || cmd.equals("adminchat")) {
            return handleAdminChat(sender, args);
        }

        return false;
    }

    private boolean handleMessage(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /msg <player> <message>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        plugin.getChatManager().sendPrivateMessage(player, target, message.toString().trim());
        return true;
    }

    private boolean handleReply(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /reply <message>");
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }

        plugin.getChatManager().replyToLastMessage(player, message.toString().trim());
        return true;
    }

    private boolean handleChat(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.chat.manage")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /chat <on|off|clear>");
            return true;
        }

        String action = args[0].toLowerCase();
        switch (action) {
            case "on":
                plugin.getChatManager().setChatEnabled(true);
                sender.sendMessage(ChatColor.GREEN + "Chat enabled!");
                break;
            case "off":
                plugin.getChatManager().setChatEnabled(false);
                sender.sendMessage(ChatColor.GREEN + "Chat disabled!");
                break;
            case "clear":
                for (int i = 0; i < 100; i++) {
                    Bukkit.broadcastMessage("");
                }
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Chat cleared by " + sender.getName());
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Usage: /chat <on|off|clear>");
                return true;
        }
        return true;
    }

    private boolean handleStaffChat(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ecore.staff")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /sc <message>");
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }

        plugin.getChatManager().sendStaffChat(player, message.toString().trim());
        return true;
    }

    private boolean handleAdminChat(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ecore.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /ac <message>");
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }

        plugin.getChatManager().sendAdminChat(player, message.toString().trim());
        return true;
    }
}

