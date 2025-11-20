package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.PortalManager;
import com.excrele.ecore.managers.WorldEditManager;
import org.bukkit.Location;
import org.bukkit.Material;
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
 * Command handler for portal management commands.
 * 
 * @author Excrele
 * @version 1.0
 */
public class PortalCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public PortalCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreate(sender, args);
            case "delete":
            case "remove":
                return handleDelete(sender, args);
            case "list":
            case "ls":
                return handleList(sender);
            case "info":
                return handleInfo(sender, args);
            case "setdest":
            case "setdestination":
                return handleSetDestination(sender, args);
            case "wand":
                return handleWand(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!sender.hasPermission("ecore.portal.create")) {
            sender.sendMessage("§cYou don't have permission to create portals!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /portal create <name> [material]");
            sender.sendMessage("§eMaterials: NETHER_PORTAL, END_PORTAL, etc.");
            return true;
        }

        String portalName = args[1];
        Material material = Material.NETHER_PORTAL;

        if (args.length >= 3) {
            try {
                material = Material.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cInvalid material! Using NETHER_PORTAL.");
                material = Material.NETHER_PORTAL;
            }
        }

        // Check if player has a selection (from WorldEdit)
        WorldEditManager.Selection selection = plugin.getWorldEditManager().getSelection(player);

        if (selection == null) {
            sender.sendMessage("§cPlease select two corners using /pos1 and /pos2 first!");
            return true;
        }

        // Destination is current location
        Location destination = player.getLocation();

        if (plugin.getPortalManager().createPortalFromSelection(portalName, selection.getMin(), selection.getMax(), destination, material)) {
            sender.sendMessage("§aSuccessfully created portal '" + portalName + "'!");
            sender.sendMessage("§eDestination: " + destination.getWorld().getName() + " at " + 
                             (int)destination.getX() + ", " + (int)destination.getY() + ", " + (int)destination.getZ());
        } else {
            sender.sendMessage("§cFailed to create portal '" + portalName + "'!");
        }

        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.portal.delete")) {
            sender.sendMessage("§cYou don't have permission to delete portals!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /portal delete <name>");
            return true;
        }

        String portalName = args[1];

        if (plugin.getPortalManager().deletePortal(portalName)) {
            sender.sendMessage("§aSuccessfully deleted portal '" + portalName + "'!");
        } else {
            sender.sendMessage("§cPortal '" + portalName + "' does not exist!");
        }

        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("ecore.portal.list")) {
            sender.sendMessage("§cYou don't have permission to list portals!");
            return true;
        }

        List<String> portals = plugin.getPortalManager().getAllPortals();

        if (portals.isEmpty()) {
            sender.sendMessage("§eNo portals found.");
            return true;
        }

        sender.sendMessage("§6=== Portals ===");
        for (String portalName : portals) {
            PortalManager.Portal portal = plugin.getPortalManager().getPortal(portalName);
            if (portal != null) {
                String destWorld = portal.getDestination() != null ? portal.getDestination().getWorld().getName() : "Unknown";
                sender.sendMessage("§e- §f" + portalName + " §7→ " + destWorld);
            }
        }

        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.portal.info")) {
            sender.sendMessage("§cYou don't have permission to view portal information!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /portal info <name>");
            return true;
        }

        String portalName = args[1];
        PortalManager.Portal portal = plugin.getPortalManager().getPortal(portalName);

        if (portal == null) {
            sender.sendMessage("§cPortal '" + portalName + "' does not exist!");
            return true;
        }

        sender.sendMessage("§6=== Portal Info: " + portalName + " ===");
        sender.sendMessage("§eBlocks: §f" + portal.getBlocks().size());
        sender.sendMessage("§eMaterial: §f" + portal.getMaterial().name());
        
        if (portal.getDestination() != null) {
            Location dest = portal.getDestination();
            sender.sendMessage("§eDestination: §f" + dest.getWorld().getName() + " at " + 
                             (int)dest.getX() + ", " + (int)dest.getY() + ", " + (int)dest.getZ());
        }
        
        if (portal.getPermission() != null) {
            sender.sendMessage("§ePermission: §f" + portal.getPermission());
        }
        
        if (portal.getMessage() != null) {
            sender.sendMessage("§eMessage: §f" + portal.getMessage());
        }

        return true;
    }

    private boolean handleSetDestination(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!sender.hasPermission("ecore.portal.setdest")) {
            sender.sendMessage("§cYou don't have permission to set portal destinations!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /portal setdest <name>");
            return true;
        }

        String portalName = args[1];
        Location destination = player.getLocation();

        if (plugin.getPortalManager().updatePortalDestination(portalName, destination)) {
            sender.sendMessage("§aSet destination for portal '" + portalName + "' to your location!");
        } else {
            sender.sendMessage("§cPortal '" + portalName + "' does not exist!");
        }

        return true;
    }

    private boolean handleWand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!sender.hasPermission("ecore.portal.wand")) {
            sender.sendMessage("§cYou don't have permission to use the portal wand!");
            return true;
        }

        sender.sendMessage("§eUse /pos1 and /pos2 to select portal area, then /portal create <name>");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== Portal Management Commands ===");
        sender.sendMessage("§e/portal create <name> [material] §7- Create a portal from selection");
        sender.sendMessage("§e/portal delete <name> §7- Delete a portal");
        sender.sendMessage("§e/portal list §7- List all portals");
        sender.sendMessage("§e/portal info <name> §7- View portal information");
        sender.sendMessage("§e/portal setdest <name> §7- Set portal destination to your location");
        sender.sendMessage("§e/portal wand §7- Get portal creation instructions");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "list", "info", "setdest", "setdestination", "wand").stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("delete") || subCommand.equals("remove") || 
                subCommand.equals("info") || subCommand.equals("setdest") || 
                subCommand.equals("setdestination")) {
                return plugin.getPortalManager().getAllPortals().stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}

