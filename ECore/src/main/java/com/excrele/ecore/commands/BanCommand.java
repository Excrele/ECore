package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Advanced Ban System Command Handler
 */
public class BanCommand implements CommandExecutor {
    private final Ecore plugin;
    
    public BanCommand(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecore.ban.use")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        if (args.length == 0) {
            if (sender instanceof Player) {
                plugin.getBanGUIManager().openBanGUI((Player) sender);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /ban <player> [reason] [duration] [ipban]");
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "gui":
                if (sender instanceof Player) {
                    plugin.getBanGUIManager().openBanGUI((Player) sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use the GUI!");
                }
                break;
                
            case "history":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ban history <player>");
                    return true;
                }
                if (sender instanceof Player) {
                    String targetName = args[1];
                    @SuppressWarnings("deprecation")
                    org.bukkit.OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(targetName);
                    plugin.getBanGUIManager().openBanHistoryGUI((Player) sender, target.getUniqueId());
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use the GUI!");
                }
                break;
                
            case "templates":
                if (sender instanceof Player) {
                    plugin.getBanGUIManager().openBanTemplatesGUI((Player) sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can use the GUI!");
                }
                break;
                
            case "unban":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ban unban <player>");
                    return true;
                }
                String unbanTarget = args[1];
                if (plugin.getBanManager().unbanPlayer(unbanTarget)) {
                    sender.sendMessage(ChatColor.GREEN + "Unbanned " + unbanTarget);
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to unban " + unbanTarget);
                }
                break;
                
            case "template":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ban template <template> <player>");
                    return true;
                }
                String templateName = args[1];
                String templateTarget = args[2];
                com.excrele.ecore.managers.BanManager.BanTemplate template = plugin.getBanManager().getBanTemplate(templateName);
                if (template == null) {
                    sender.sendMessage(ChatColor.RED + "Template not found: " + templateName);
                    return true;
                }
                Player staff = sender instanceof Player ? (Player) sender : null;
                plugin.getBanManager().banPlayer(staff, templateTarget, template.getReason(), 
                    template.getDuration(), template.isIpBan(), null);
                break;
                
            default:
                // Ban player: /ban <player> [reason] [duration] [ipban]
                String target = args[0];
                String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "No reason provided";
                long duration = -1; // Permanent by default
                boolean ipBan = false;
                
                // Parse duration if provided (format: 1d, 2h, 30m, 60s)
                if (args.length > 1) {
                    String lastArg = args[args.length - 1].toLowerCase();
                    if (lastArg.equals("ipban") || lastArg.equals("ip")) {
                        ipBan = true;
                        if (args.length > 2) {
                            String durationArg = args[args.length - 2];
                            duration = parseDuration(durationArg);
                        }
                    } else {
                        duration = parseDuration(lastArg);
                        if (args.length > 2 && (args[args.length - 2].toLowerCase().equals("ipban") || args[args.length - 2].toLowerCase().equals("ip"))) {
                            ipBan = true;
                        }
                    }
                }
                
                Player staffMember = sender instanceof Player ? (Player) sender : null;
                if (plugin.getBanManager().banPlayer(staffMember, target, reason, duration, ipBan, null)) {
                    sender.sendMessage(ChatColor.GREEN + "Banned " + target);
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to ban " + target);
                }
                break;
        }
        
        return true;
    }
    
    private long parseDuration(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) return -1;
        
        durationStr = durationStr.toLowerCase();
        if (durationStr.equals("permanent") || durationStr.equals("perm")) return -1;
        
        try {
            if (durationStr.endsWith("d")) {
                return Long.parseLong(durationStr.substring(0, durationStr.length() - 1)) * 86400;
            } else if (durationStr.endsWith("h")) {
                return Long.parseLong(durationStr.substring(0, durationStr.length() - 1)) * 3600;
            } else if (durationStr.endsWith("m")) {
                return Long.parseLong(durationStr.substring(0, durationStr.length() - 1)) * 60;
            } else if (durationStr.endsWith("s")) {
                return Long.parseLong(durationStr.substring(0, durationStr.length() - 1));
            } else {
                // Assume seconds
                return Long.parseLong(durationStr);
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

