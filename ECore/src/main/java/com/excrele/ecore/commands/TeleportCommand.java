package com.excrele.ecore.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.Structure;

import com.excrele.ecore.Ecore;

public class TeleportCommand implements CommandExecutor {
    private final Ecore plugin;

    public TeleportCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("tp") || cmd.equals("teleport")) {
            return handleTeleport(sender, args);
        } else if (cmd.equals("tpa")) {
            return handleTeleportRequest(sender, args, false);
        } else if (cmd.equals("tpahere")) {
            return handleTeleportRequest(sender, args, true);
        } else if (cmd.equals("tpaccept")) {
            return handleAcceptRequest(sender);
        } else if (cmd.equals("tpdeny")) {
            return handleDenyRequest(sender);
        } else if (cmd.equals("back")) {
            return handleBack(sender);
        } else if (cmd.equals("top")) {
            return handleTop(sender);
        } else if (cmd.equals("jump")) {
            return handleJump(sender);
        } else if (cmd.equals("rtp")) {
            return handleRandomTeleport(sender);
        } else if (cmd.equals("tpbiome") || cmd.equals("teleportbiome")) {
            return handleTeleportBiome(sender, args);
        } else if (cmd.equals("tpstructure") || cmd.equals("teleportstructure")) {
            return handleTeleportStructure(sender, args);
        }

        return false;
    }

    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ecore.teleport")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        // Handle coordinate teleport: /tp <x> <y> <z> [world]
        if (args.length >= 3) {
            try {
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                double z = Double.parseDouble(args[2]);
                World world = args.length >= 4 ? Bukkit.getWorld(args[3]) : player.getWorld();
                
                if (world == null && args.length >= 4) {
                    player.sendMessage(ChatColor.RED + "World not found!");
                    return true;
                }
                
                if (plugin.getTeleportManager() != null) {
                    plugin.getTeleportManager().teleportToCoordinates(player, x, y, z, world);
                } else {
                    player.sendMessage(ChatColor.RED + "Teleport manager is not available!");
                }
                return true;
            } catch (NumberFormatException e) {
                // Not coordinates, continue with player teleport logic
            }
        }

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
            if (plugin.getTeleportManager() != null) {
                plugin.getTeleportManager().teleport(player, target);
                player.sendMessage(ChatColor.GREEN + "Teleported to " + target.getName() + "!");
            } else {
                player.sendMessage(ChatColor.RED + "Teleport manager is not available!");
            }
            return true;
        } else if (args.length == 2) {
            if (!player.hasPermission("ecore.teleport.others")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to teleport others!");
                return true;
            }
            Player target1 = Bukkit.getPlayer(args[0]);
            Player target2 = Bukkit.getPlayer(args[1]);
            if (target1 == null || target2 == null) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
            if (plugin.getTeleportManager() != null) {
                plugin.getTeleportManager().teleport(target1, target2);
                player.sendMessage(ChatColor.GREEN + "Teleported " + target1.getName() + " to " + target2.getName() + "!");
            } else {
                player.sendMessage(ChatColor.RED + "Teleport manager is not available!");
            }
            return true;
        }

        player.sendMessage(ChatColor.RED + "Usage: /tp <player> or /tp <player1> <player2> or /tp <x> <y> <z> [world]");
        return true;
    }

    private boolean handleTeleportRequest(CommandSender sender, String[] args, boolean here) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /" + (here ? "tpahere" : "tpa") + " <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        if (target == player) {
            player.sendMessage(ChatColor.RED + "You can't teleport to yourself!");
            return true;
        }

        if (plugin.getTeleportManager() != null) {
            plugin.getTeleportManager().createRequest(player, target, here);
        } else {
            player.sendMessage(ChatColor.RED + "Teleport manager is not available!");
        }
        return true;
    }

    private boolean handleAcceptRequest(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        if (plugin.getTeleportManager() != null) {
            plugin.getTeleportManager().acceptRequest(player);
        } else {
            player.sendMessage(ChatColor.RED + "Teleport manager is not available!");
        }
        return true;
    }

    private boolean handleDenyRequest(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        if (plugin.getTeleportManager() != null) {
            plugin.getTeleportManager().denyRequest(player);
        } else {
            player.sendMessage(ChatColor.RED + "Teleport manager is not available!");
        }
        return true;
    }

    private boolean handleBack(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        // Try death location first, then teleport history
        var deathLocation = plugin.getTeleportManager().getDeathLocation(player);
        if (deathLocation != null) {
            plugin.getTeleportManager().teleport(player, deathLocation);
            player.sendMessage(ChatColor.GREEN + "Returned to death location!");
            return true;
        }
        
        // Use teleport history (multiple back locations)
        var previousLocation = plugin.getTeleportManager().goBack(player);
        if (previousLocation == null) {
            // Fallback to simple last location for backward compatibility
            var lastLocation = plugin.getTeleportManager().getLastLocation(player);
            if (lastLocation == null) {
                player.sendMessage(ChatColor.RED + "You have no previous location!");
                return true;
            }
            plugin.getTeleportManager().teleport(player, lastLocation);
            player.sendMessage(ChatColor.GREEN + "Returned to previous location!");
            return true;
        }

        plugin.getTeleportManager().teleport(player, previousLocation);
        player.sendMessage(ChatColor.GREEN + "Returned to previous location!");
        return true;
    }

    private boolean handleTop(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("ecore.teleport")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (plugin.getTeleportManager() != null) {
            plugin.getTeleportManager().teleportToTop(player);
        } else {
            player.sendMessage(ChatColor.RED + "Teleport manager is not available!");
        }
        return true;
    }

    private boolean handleJump(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("ecore.teleport")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (plugin.getTeleportManager() != null) {
            plugin.getTeleportManager().teleportJump(player);
        } else {
            player.sendMessage(ChatColor.RED + "Teleport manager is not available!");
        }
        return true;
    }

    private boolean handleRandomTeleport(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("ecore.teleport")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Finding a safe random location...");
        if (plugin.getTeleportManager() != null) {
            plugin.getTeleportManager().teleportRandom(player);
        } else {
            player.sendMessage(ChatColor.RED + "Teleport manager is not available!");
        }
        return true;
    }

    private boolean handleTeleportBiome(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("ecore.teleport")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /tpbiome <biome>");
            player.sendMessage(ChatColor.GRAY + "Example: /tpbiome PLAINS");
            return true;
        }

        try {
            // Use Registry to get biome
            Biome biome = Registry.BIOME.get(org.bukkit.NamespacedKey.minecraft(args[0].toLowerCase()));
            if (biome == null) {
                player.sendMessage(ChatColor.RED + "Invalid biome! Use one of: " + getBiomeList());
                return true;
            }
            if (plugin.getTeleportManager() != null) {
                plugin.getTeleportManager().teleportToBiome(player, biome);
            } else {
                player.sendMessage(ChatColor.RED + "Teleport manager is not available!");
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Invalid biome! Use one of: " + getBiomeList());
        }
        return true;
    }

    private boolean handleTeleportStructure(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("ecore.teleport")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /tpstructure <structure>");
            player.sendMessage(ChatColor.GRAY + "Example: /tpstructure VILLAGE");
            return true;
        }

        try {
            // Try to get structure from registry
            Structure structure = Registry.STRUCTURE.get(NamespacedKey.minecraft(args[0].toLowerCase()));
            if (structure == null) {
                player.sendMessage(ChatColor.RED + "Invalid structure! Use one of: " + getStructureList());
                return true;
            }
            if (plugin.getTeleportManager() != null) {
                plugin.getTeleportManager().teleportToStructure(player, structure);
            } else {
                player.sendMessage(ChatColor.RED + "Teleport manager is not available!");
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Invalid structure! Use one of: " + getStructureList());
        }
        return true;
    }

    private String getBiomeList() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Biome biome : Registry.BIOME) {
            if (count >= 10) break;
            if (count > 0) sb.append(", ");
            try {
                @SuppressWarnings("deprecation")
                org.bukkit.NamespacedKey key = biome.getKey();
                sb.append(key != null ? key.getKey() : biome.toString());
            } catch (Exception e) {
                sb.append(biome.toString());
            }
            count++;
        }
        sb.append(", ...");
        return sb.toString();
    }

    private String getStructureList() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Structure structure : Registry.STRUCTURE) {
            if (count >= 10) break;
            if (count > 0) sb.append(", ");
            try {
                @SuppressWarnings("deprecation")
                org.bukkit.NamespacedKey key = structure.getKey();
                sb.append(key != null ? key.getKey() : structure.toString());
            } catch (Exception ex) {
                sb.append(structure.toString());
            }
            count++;
        }
        sb.append(", ...");
        return sb.toString();
    }
}

