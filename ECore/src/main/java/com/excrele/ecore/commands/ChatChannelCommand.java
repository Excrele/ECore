package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.ChatChannelManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for chat channels system.
 */
public class ChatChannelCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public ChatChannelCommand(Ecore plugin) {
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
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /channel join <channel>");
                    return true;
                }
                handleJoin(player, args[1]);
                break;
            case "leave":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /channel leave <channel>");
                    return true;
                }
                handleLeave(player, args[1]);
                break;
            case "list":
                handleList(player);
                break;
            case "current":
            case "info":
                handleCurrent(player);
                break;
            case "mute":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /channel mute <channel>");
                    return true;
                }
                handleMute(player, args[1]);
                break;
            case "unmute":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /channel unmute <channel>");
                    return true;
                }
                handleUnmute(player, args[1]);
                break;
            case "create":
                if (!player.hasPermission("ecore.channel.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to create channels!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /channel create <channel-id> [name] [prefix]");
                    return true;
                }
                handleCreate(player, args);
                break;
            case "delete":
                if (!player.hasPermission("ecore.channel.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to delete channels!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /channel delete <channel-id>");
                    return true;
                }
                handleDelete(player, args[1]);
                break;
            default:
                // Treat as message to current channel
                if (plugin.getChatChannelManager().getPlayerChannel(player.getUniqueId()) != null) {
                    String channelId = plugin.getChatChannelManager().getPlayerChannel(player.getUniqueId());
                    String message = String.join(" ", args);
                    plugin.getChatChannelManager().sendChannelMessage(player, channelId, message);
                } else {
                    player.sendMessage(ChatColor.RED + "You are not in any channel! Use /channel join <channel>");
                }
        }

        return true;
    }

    private void handleJoin(Player player, String channelId) {
        if (plugin.getChatChannelManager().joinChannel(player, channelId, true)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "Channel not found or you don't have permission: " + channelId);
        }
    }

    private void handleLeave(Player player, String channelId) {
        if (plugin.getChatChannelManager().leaveChannel(player, channelId, true)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "You are not in channel: " + channelId);
        }
    }

    private void handleList(Player player) {
        List<ChatChannelManager.ChatChannel> availableChannels = new ArrayList<>();
        
        for (ChatChannelManager.ChatChannel channel : plugin.getChatChannelManager().getChannels().values()) {
            if (player.hasPermission(channel.getPermission())) {
                availableChannels.add(channel);
            }
        }

        if (availableChannels.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No available channels.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "          Available Channels");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");

        String currentChannelId = plugin.getChatChannelManager().getPlayerChannel(player.getUniqueId());
        
        for (ChatChannelManager.ChatChannel channel : availableChannels) {
            boolean isCurrent = channel.getId().equals(currentChannelId);
            ChatColor nameColor = isCurrent ? ChatColor.GREEN : channel.getColor();
            String status = isCurrent ? ChatColor.GREEN + " [CURRENT]" : "";
            
            player.sendMessage(ChatColor.WHITE + "• " + nameColor + channel.getName() + 
                             ChatColor.GRAY + " (" + channel.getId() + ")" + status);
            player.sendMessage(ChatColor.GRAY + "  Prefix: " + channel.getPrefix());
            if (channel.getRange() == -1) {
                player.sendMessage(ChatColor.GRAY + "  Range: Global");
            } else {
                player.sendMessage(ChatColor.GRAY + "  Range: " + channel.getRange() + " blocks");
            }
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "Use /channel join <channel> to join a channel!");
    }

    private void handleCurrent(Player player) {
        String channelId = plugin.getChatChannelManager().getPlayerChannel(player.getUniqueId());
        
        if (channelId == null) {
            player.sendMessage(ChatColor.YELLOW + "You are not in any channel!");
            return;
        }

        ChatChannelManager.ChatChannel channel = plugin.getChatChannelManager().getChannel(channelId);
        if (channel == null) {
            player.sendMessage(ChatColor.RED + "Your current channel is invalid!");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "          Current Channel");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.WHITE + "Channel: " + channel.getColor() + channel.getName());
        player.sendMessage(ChatColor.WHITE + "ID: " + ChatColor.GRAY + channel.getId());
        player.sendMessage(ChatColor.WHITE + "Prefix: " + ChatColor.GRAY + channel.getPrefix());
        if (channel.getRange() == -1) {
            player.sendMessage(ChatColor.WHITE + "Range: " + ChatColor.GRAY + "Global");
        } else {
            player.sendMessage(ChatColor.WHITE + "Range: " + ChatColor.GRAY + channel.getRange() + " blocks");
        }
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
    }

    private void handleMute(Player player, String channelId) {
        if (plugin.getChatChannelManager().muteChannel(player, channelId)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "Channel not found: " + channelId);
        }
    }

    private void handleUnmute(Player player, String channelId) {
        if (plugin.getChatChannelManager().unmuteChannel(player, channelId)) {
            // Success message already sent by manager
        } else {
            player.sendMessage(ChatColor.RED + "Channel not found or not muted: " + channelId);
        }
    }

    private void handleCreate(Player player, String[] args) {
        String channelId = args[1].toLowerCase();
        String name = args.length > 2 ? args[2] : channelId;
        String prefix = args.length > 3 ? args[3] : "[" + name + "]";
        String permission = "ecore.chat." + channelId;
        int range = -1; // Default to global
        ChatColor color = ChatColor.WHITE;
        boolean defaultChannel = false;
        boolean autoJoin = false;

        if (plugin.getChatChannelManager().createChannel(channelId, name, prefix, permission, 
                                                        range, color, defaultChannel, autoJoin)) {
            player.sendMessage(ChatColor.GREEN + "Channel created: " + channelId);
        } else {
            player.sendMessage(ChatColor.RED + "Channel already exists: " + channelId);
        }
    }

    private void handleDelete(Player player, String channelId) {
        if (plugin.getChatChannelManager().deleteChannel(channelId)) {
            player.sendMessage(ChatColor.GREEN + "Channel deleted: " + channelId);
        } else {
            player.sendMessage(ChatColor.RED + "Channel not found: " + channelId);
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Channel Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/channel - Show this help");
        player.sendMessage(ChatColor.YELLOW + "/channel list - List available channels");
        player.sendMessage(ChatColor.YELLOW + "/channel join <channel> - Join a channel");
        player.sendMessage(ChatColor.YELLOW + "/channel leave <channel> - Leave a channel");
        player.sendMessage(ChatColor.YELLOW + "/channel current - View current channel");
        player.sendMessage(ChatColor.YELLOW + "/channel mute <channel> - Mute a channel");
        player.sendMessage(ChatColor.YELLOW + "/channel unmute <channel> - Unmute a channel");
        player.sendMessage(ChatColor.YELLOW + "/ch <message> - Send message to current channel");
        if (player.hasPermission("ecore.channel.admin")) {
            player.sendMessage(ChatColor.RED + "/channel create <id> [name] [prefix] - Create channel (admin)");
            player.sendMessage(ChatColor.RED + "/channel delete <id> - Delete channel (admin)");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("join", "leave", "list", "current", "info", "mute", "unmute", "create", "delete")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("join") || 
                                 args[0].equalsIgnoreCase("leave") ||
                                 args[0].equalsIgnoreCase("mute") ||
                                 args[0].equalsIgnoreCase("unmute") ||
                                 args[0].equalsIgnoreCase("delete"))) {
            return new ArrayList<>(plugin.getChatChannelManager().getChannels().keySet())
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}

