package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.Region;
import com.excrele.ecore.managers.RegionFlag;
import com.excrele.ecore.managers.RegionManager;
import com.excrele.ecore.managers.RegionType;
import com.excrele.ecore.managers.WorldEditManager;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Handles region management commands.
 * 
 * @author Excrele
 * @version 1.0
 */
public class RegionCommand implements CommandExecutor {
    private final Ecore plugin;

    public RegionCommand(Ecore plugin) {
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
            case "add":
                return handleCreate(sender, args);
            case "delete":
            case "remove":
            case "del":
                return handleDelete(sender, args);
            case "list":
                return handleList(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "flag":
                return handleFlag(sender, args);
            case "flags":
                return handleFlags(sender, args);
            case "addowner":
            case "owner":
                return handleAddOwner(sender, args);
            case "removeowner":
                return handleRemoveOwner(sender, args);
            case "addmember":
            case "member":
                return handleAddMember(sender, args);
            case "removemember":
                return handleRemoveMember(sender, args);
            case "types":
                return handleTypes(sender);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Region Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/region create <name> <type> - Create a region from selection");
        sender.sendMessage(ChatColor.YELLOW + "/region delete <name> - Delete a region");
        sender.sendMessage(ChatColor.YELLOW + "/region list [world] - List all regions");
        sender.sendMessage(ChatColor.YELLOW + "/region info <name> - Show region information");
        sender.sendMessage(ChatColor.YELLOW + "/region flag <name> <flag> <true|false> - Set a flag");
        sender.sendMessage(ChatColor.YELLOW + "/region flags <name> - Show all flags for a region");
        sender.sendMessage(ChatColor.YELLOW + "/region addowner <name> <player> - Add an owner");
        sender.sendMessage(ChatColor.YELLOW + "/region removeowner <name> <player> - Remove an owner");
        sender.sendMessage(ChatColor.YELLOW + "/region addmember <name> <player> - Add a member");
        sender.sendMessage(ChatColor.YELLOW + "/region removemember <name> <player> - Remove a member");
        sender.sendMessage(ChatColor.YELLOW + "/region types - List available region types");
        sender.sendMessage(ChatColor.YELLOW + "/region reload - Reload regions from file");
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ecore.region.create")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /region create <name> <type>");
            player.sendMessage(ChatColor.YELLOW + "Use /region types to see available types");
            return true;
        }

        String regionName = args[1];
        String regionType = args[2].toLowerCase();

        RegionManager regionManager = plugin.getRegionManager();
        RegionType type = regionManager.getRegionType(regionType);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Unknown region type: " + regionType);
            player.sendMessage(ChatColor.YELLOW + "Use /region types to see available types");
            return true;
        }

        WorldEditManager worldEditManager = plugin.getWorldEditManager();
        WorldEditManager.Selection selection = worldEditManager.getSelection(player);

        if (selection == null) {
            player.sendMessage(ChatColor.RED + "You must select an area first!");
            player.sendMessage(ChatColor.YELLOW + "Use /pos1 and /pos2 to set selection points");
            return true;
        }

        if (regionManager.getRegion(player.getWorld(), regionName) != null) {
            player.sendMessage(ChatColor.RED + "A region with that name already exists!");
            return true;
        }

        Region region = regionManager.createRegion(
            regionName,
            player.getWorld(),
            selection.getMin(),
            selection.getMax(),
            regionType,
            player.getUniqueId()
        );

        if (region != null) {
            player.sendMessage(ChatColor.GREEN + "Region '" + regionName + "' created with type '" + type.getDisplayName() + "'!");
            player.sendMessage(ChatColor.GRAY + "Volume: " + region.getVolume() + " blocks");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to create region!");
        }

        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.region.delete")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /region delete <name>");
            return true;
        }

        String regionName = args[1];
        RegionManager regionManager = plugin.getRegionManager();

        World world = null;
        if (sender instanceof Player) {
            world = ((Player) sender).getWorld();
        } else {
            // Console - need to find region in any world
            for (String worldName : plugin.getServer().getWorlds().stream()
                    .map(org.bukkit.World::getName).toArray(String[]::new)) {
                Region region = regionManager.getRegion(plugin.getServer().getWorld(worldName), regionName);
                if (region != null) {
                    world = region.getWorld();
                    break;
                }
            }
        }

        if (world == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found!");
            return true;
        }

        if (regionManager.deleteRegion(world, regionName)) {
            sender.sendMessage(ChatColor.GREEN + "Region '" + regionName + "' deleted!");
        } else {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found!");
        }

        return true;
    }

    private boolean handleList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.region.list")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        RegionManager regionManager = plugin.getRegionManager();
        Collection<Region> regions;

        if (args.length > 1) {
            org.bukkit.World world = plugin.getServer().getWorld(args[1]);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "World '" + args[1] + "' not found!");
                return true;
            }
            regions = regionManager.getRegions(world);
        } else if (sender instanceof Player) {
            regions = regionManager.getRegions(((Player) sender).getWorld());
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /region list <world>");
            return true;
        }

        if (regions.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No regions found.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Regions ===");
        for (Region region : regions) {
            sender.sendMessage(ChatColor.YELLOW + "- " + region.getName() + 
                ChatColor.GRAY + " (" + region.getRegionType() + ", " + region.getVolume() + " blocks)");
        }

        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.region.info")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /region info <name>");
            return true;
        }

        String regionName = args[1];
        RegionManager regionManager = plugin.getRegionManager();

        World world = null;
        if (sender instanceof Player) {
            world = ((Player) sender).getWorld();
        } else {
            // Console - find in any world
            for (org.bukkit.World w : plugin.getServer().getWorlds()) {
                Region region = regionManager.getRegion(w, regionName);
                if (region != null) {
                    world = w;
                    break;
                }
            }
        }

        if (world == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found!");
            return true;
        }

        Region region = regionManager.getRegion(world, regionName);
        if (region == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found!");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Region Info: " + region.getName() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "World: " + region.getWorld().getName());
        sender.sendMessage(ChatColor.YELLOW + "Type: " + region.getRegionType());
        sender.sendMessage(ChatColor.YELLOW + "Volume: " + region.getVolume() + " blocks");
        sender.sendMessage(ChatColor.YELLOW + "Min: " + region.getMin().getBlockX() + ", " + 
            region.getMin().getBlockY() + ", " + region.getMin().getBlockZ());
        sender.sendMessage(ChatColor.YELLOW + "Max: " + region.getMax().getBlockX() + ", " + 
            region.getMax().getBlockY() + ", " + region.getMax().getBlockZ());
        sender.sendMessage(ChatColor.YELLOW + "Owners: " + region.getOwners().size());
        sender.sendMessage(ChatColor.YELLOW + "Members: " + region.getMembers().size());

        return true;
    }

    private boolean handleFlag(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.region.flag")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /region flag <name> <flag> <true|false>");
            return true;
        }

        String regionName = args[1];
        String flagName = args[2];
        String valueStr = args[3].toLowerCase();

        boolean value;
        if (valueStr.equals("true") || valueStr.equals("1") || valueStr.equals("on") || valueStr.equals("enable")) {
            value = true;
        } else if (valueStr.equals("false") || valueStr.equals("0") || valueStr.equals("off") || valueStr.equals("disable")) {
            value = false;
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid value! Use true or false");
            return true;
        }

        RegionFlag flag = RegionFlag.byName(flagName);
        if (flag == null) {
            sender.sendMessage(ChatColor.RED + "Unknown flag: " + flagName);
            return true;
        }

        RegionManager regionManager = plugin.getRegionManager();
        World world = sender instanceof Player ? ((Player) sender).getWorld() : null;
        
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "You must specify a world or use this command as a player!");
            return true;
        }

        Region region = regionManager.getRegion(world, regionName);
        if (region == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found!");
            return true;
        }

        region.setFlag(flag, value);
        regionManager.saveRegions();
        sender.sendMessage(ChatColor.GREEN + "Flag '" + flag.getName() + "' set to " + value + " for region '" + regionName + "'");

        return true;
    }

    private boolean handleFlags(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.region.info")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /region flags <name>");
            return true;
        }

        String regionName = args[1];
        RegionManager regionManager = plugin.getRegionManager();
        World world = sender instanceof Player ? ((Player) sender).getWorld() : null;
        
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "You must specify a world or use this command as a player!");
            return true;
        }

        Region region = regionManager.getRegion(world, regionName);
        if (region == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found!");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Flags for " + region.getName() + " ===");
        for (RegionFlag flag : RegionFlag.values()) {
            Boolean value = region.getFlag(flag);
            String status = value == null ? ChatColor.GRAY + "not set" : 
                (value ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled");
            sender.sendMessage(ChatColor.YELLOW + flag.getName() + ": " + status);
        }

        return true;
    }

    private boolean handleAddOwner(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.region.owner")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /region addowner <name> <player>");
            return true;
        }

        String regionName = args[1];
        String playerName = args[2];
        Player target = plugin.getServer().getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + playerName + "' not found!");
            return true;
        }

        RegionManager regionManager = plugin.getRegionManager();
        World world = sender instanceof Player ? ((Player) sender).getWorld() : target.getWorld();
        Region region = regionManager.getRegion(world, regionName);

        if (region == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found!");
            return true;
        }

        region.addOwner(target.getUniqueId());
        regionManager.saveRegions();
        sender.sendMessage(ChatColor.GREEN + "Added " + target.getName() + " as owner of region '" + regionName + "'");

        return true;
    }

    private boolean handleRemoveOwner(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.region.owner")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /region removeowner <name> <player>");
            return true;
        }

        String regionName = args[1];
        String playerName = args[2];
        Player target = plugin.getServer().getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + playerName + "' not found!");
            return true;
        }

        RegionManager regionManager = plugin.getRegionManager();
        World world = sender instanceof Player ? ((Player) sender).getWorld() : target.getWorld();
        Region region = regionManager.getRegion(world, regionName);

        if (region == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found!");
            return true;
        }

        region.removeOwner(target.getUniqueId());
        regionManager.saveRegions();
        sender.sendMessage(ChatColor.GREEN + "Removed " + target.getName() + " as owner of region '" + regionName + "'");

        return true;
    }

    private boolean handleAddMember(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.region.member")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /region addmember <name> <player>");
            return true;
        }

        String regionName = args[1];
        String playerName = args[2];
        Player target = plugin.getServer().getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + playerName + "' not found!");
            return true;
        }

        RegionManager regionManager = plugin.getRegionManager();
        World world = sender instanceof Player ? ((Player) sender).getWorld() : target.getWorld();
        Region region = regionManager.getRegion(world, regionName);

        if (region == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found!");
            return true;
        }

        region.addMember(target.getUniqueId());
        regionManager.saveRegions();
        sender.sendMessage(ChatColor.GREEN + "Added " + target.getName() + " as member of region '" + regionName + "'");

        return true;
    }

    private boolean handleRemoveMember(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.region.member")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /region removemember <name> <player>");
            return true;
        }

        String regionName = args[1];
        String playerName = args[2];
        Player target = plugin.getServer().getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + playerName + "' not found!");
            return true;
        }

        RegionManager regionManager = plugin.getRegionManager();
        World world = sender instanceof Player ? ((Player) sender).getWorld() : target.getWorld();
        Region region = regionManager.getRegion(world, regionName);

        if (region == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found!");
            return true;
        }

        region.removeMember(target.getUniqueId());
        regionManager.saveRegions();
        sender.sendMessage(ChatColor.GREEN + "Removed " + target.getName() + " as member of region '" + regionName + "'");

        return true;
    }

    private boolean handleTypes(CommandSender sender) {
        if (!sender.hasPermission("ecore.region.info")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        RegionManager regionManager = plugin.getRegionManager();
        Collection<RegionType> types = regionManager.getRegionTypes();

        sender.sendMessage(ChatColor.GOLD + "=== Available Region Types ===");
        for (RegionType type : types) {
            sender.sendMessage(ChatColor.YELLOW + "- " + type.getName() + 
                ChatColor.GRAY + " (" + type.getDisplayName() + ")");
            sender.sendMessage(ChatColor.GRAY + "  " + type.getDescription());
        }

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("ecore.region.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        plugin.getRegionManager().loadRegions();
        sender.sendMessage(ChatColor.GREEN + "Regions reloaded!");

        return true;
    }
}

