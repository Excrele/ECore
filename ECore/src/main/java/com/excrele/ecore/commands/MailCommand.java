package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.MailManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MailCommand implements CommandExecutor {
    private final Ecore plugin;

    public MailCommand(Ecore plugin) {
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
            player.sendMessage(ChatColor.RED + "Usage: /mail <send|read|clear> [args]");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "send":
                return handleSend(player, args);
            case "read":
                return handleRead(player);
            case "clear":
                return handleClear(player);
            case "sendall":
                if (player.hasPermission("ecore.mail.sendall")) {
                    return handleSendAll(player, args);
                }
                player.sendMessage(ChatColor.RED + "You don't have permission!");
                return true;
            default:
                player.sendMessage(ChatColor.RED + "Usage: /mail <send|read|clear> [args]");
                return true;
        }
    }

    private boolean handleSend(Player sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /mail send <player> <message>");
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        plugin.getMailManager().sendMail(sender, target, message.toString().trim());
        return true;
    }

    private boolean handleRead(Player player) {
        // Open GUI instead of text-based
        plugin.getMailGUIManager().openMailGUI(player);
        return true;
    }

    private boolean handleClear(Player player) {
        plugin.getMailManager().clearMail(player);
        player.sendMessage(ChatColor.GREEN + "All mail cleared!");
        return true;
    }

    private boolean handleSendAll(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /mail sendall <message>");
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        plugin.getMailManager().sendMailToAll(sender, message.toString().trim());
        return true;
    }
}

