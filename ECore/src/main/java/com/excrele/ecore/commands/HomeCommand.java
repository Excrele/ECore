package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HomeCommand implements CommandExecutor {
    private final Ecore plugin;

    public HomeCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("sethome")) {
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Usage: /sethome <name>");
                return true;
            }

            String homeName = args[0];
            boolean success = plugin.getHomeManager().setHome(player, homeName, player.getLocation());
            if (success) {
                player.sendMessage(ChatColor.GREEN + "Home '" + homeName + "' set successfully!");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to set home. You may have reached the maximum number of homes!");
            }
            return true;
        } else if (cmd.equals("home")) {
            if (args.length == 0) {
                // Open GUI if no args
                plugin.getHomeGUIManager().openHomeGUI(player);
                return true;
            }
            
            if (args.length == 1) {
                String homeName = args[0];
                // Try own home first
                if (plugin.getHomeManager().getHome(player, homeName) != null) {
                    plugin.getHomeManager().teleportToHome(player, homeName);
                    return true;
                }
                
                // Try shared homes
                for (java.util.Map.Entry<String, java.util.UUID> sharedHome : plugin.getHomeManager().getSharedHomes(player)) {
                    if (sharedHome.getKey().equals(homeName)) {
                        // Teleport to shared home
                        org.bukkit.Location sharedHomeLoc = plugin.getHomeManager().getHome(sharedHome.getValue(), homeName);
                        if (sharedHomeLoc != null) {
                            plugin.getTeleportManager().teleport(player, sharedHomeLoc);
                            player.sendMessage(ChatColor.GREEN + "Teleported to " + plugin.getHomeManager().getHomeOwnerName(sharedHome.getValue()) + "'s home '" + homeName + "'!");
                            return true;
                        }
                    }
                }
                
                player.sendMessage(ChatColor.RED + "Home '" + homeName + "' not found!");
                return true;
            }
            
            player.sendMessage(ChatColor.RED + "Usage: /home [name]");
            return true;
        } else if (cmd.equals("listhomes")) {
            List<String> homes = plugin.getHomeManager().getPlayerHomes(player);
            if (homes.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "You have no homes set.");
            } else {
                player.sendMessage(ChatColor.GREEN + "Your homes: " + String.join(", ", homes));
            }
            return true;
        } else if (cmd.equals("homeshare") || cmd.equals("sharehome")) {
            return handleShareHome(player, args);
        } else if (cmd.equals("homeunshare") || cmd.equals("unsharehome")) {
            return handleUnshareHome(player, args);
        } else if (cmd.equals("homecategory") || cmd.equals("sethomecategory")) {
            return handleSetHomeCategory(player, args);
        } else if (cmd.equals("homeicon") || cmd.equals("sethomeicon")) {
            return handleSetHomeIcon(player, args);
        } else if (cmd.equals("homedescription") || cmd.equals("sethomedescription")) {
            return handleSetHomeDescription(player, args);
        }

        return false;
    }

    private boolean handleShareHome(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /homeshare <home> <player>");
            return true;
        }

        String homeName = args[0];
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (plugin.getHomeManager().shareHome(player, homeName, target)) {
            player.sendMessage(ChatColor.GREEN + "Home '" + homeName + "' is now shared with " + target.getName() + "!");
            target.sendMessage(ChatColor.GREEN + player.getName() + " shared their home '" + homeName + "' with you!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to share home. It may not exist or already be shared with that player.");
        }
        return true;
    }

    private boolean handleUnshareHome(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /homeunshare <home> <player>");
            return true;
        }

        String homeName = args[0];
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (plugin.getHomeManager().unshareHome(player, homeName, target)) {
            player.sendMessage(ChatColor.GREEN + "Home '" + homeName + "' is no longer shared with " + target.getName() + "!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to unshare home. It may not exist or not be shared with that player.");
        }
        return true;
    }

    private boolean handleSetHomeCategory(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /homecategory <home> <category>");
            return true;
        }

        String homeName = args[0];
        String category = args[1];

        if (plugin.getHomeManager().setHomeCategory(player, homeName, category)) {
            player.sendMessage(ChatColor.GREEN + "Home '" + homeName + "' category set to '" + category + "'!");
        } else {
            player.sendMessage(ChatColor.RED + "Home not found!");
        }
        return true;
    }

    private boolean handleSetHomeIcon(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /homeicon <home> <material>");
            player.sendMessage(ChatColor.GRAY + "Example: /homeicon myhome DIAMOND");
            return true;
        }

        String homeName = args[0];
        try {
            org.bukkit.Material icon = org.bukkit.Material.valueOf(args[1].toUpperCase());
            if (plugin.getHomeManager().setHomeIcon(player, homeName, icon)) {
                player.sendMessage(ChatColor.GREEN + "Home '" + homeName + "' icon set to " + icon.name() + "!");
            } else {
                player.sendMessage(ChatColor.RED + "Home not found!");
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid material! Use a valid Minecraft material name.");
        }
        return true;
    }

    private boolean handleSetHomeDescription(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /homedescription <home> <description>");
            return true;
        }

        String homeName = args[0];
        String description = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        if (plugin.getHomeManager().setHomeDescription(player, homeName, description)) {
            player.sendMessage(ChatColor.GREEN + "Home '" + homeName + "' description set!");
        } else {
            player.sendMessage(ChatColor.RED + "Home not found!");
        }
        return true;
    }
}