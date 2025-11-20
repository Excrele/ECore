package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.WorldManager;
import org.bukkit.*;
import org.bukkit.World.Environment;
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
 * Command handler for world management commands.
 * Similar to MultiverseCore commands.
 * 
 * @author Excrele
 * @version 1.0
 */
public class WorldCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public WorldCommand(Ecore plugin) {
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
            case "new":
                return handleCreate(sender, args);
            case "load":
                return handleLoad(sender, args);
            case "unload":
                return handleUnload(sender, args);
            case "delete":
            case "remove":
                return handleDelete(sender, args);
            case "list":
            case "ls":
                return handleList(sender);
            case "tp":
            case "teleport":
                return handleTeleport(sender, args);
            case "spawn":
                return handleSpawn(sender, args);
            case "setspawn":
                return handleSetSpawn(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.world.create")) {
            sender.sendMessage("§cYou don't have permission to create worlds!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /mv create <name> [type] [environment] [seed]");
            sender.sendMessage("§eTypes: NORMAL, FLAT, LARGE_BIOMES, AMPLIFIED, CUSTOMIZED");
            sender.sendMessage("§eEnvironments: NORMAL, NETHER, THE_END");
            return true;
        }

        String worldName = args[1];
        WorldType type = WorldType.NORMAL;
        Environment environment = Environment.NORMAL;
        long seed = 0;

        if (args.length >= 3) {
            try {
                type = WorldType.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cInvalid world type! Use: NORMAL, FLAT, LARGE_BIOMES, AMPLIFIED, CUSTOMIZED");
                return true;
            }
        }

        if (args.length >= 4) {
            try {
                environment = Environment.valueOf(args[3].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cInvalid environment! Use: NORMAL, NETHER, THE_END");
                return true;
            }
        }

        if (args.length >= 5) {
            try {
                seed = Long.parseLong(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid seed! Using random seed.");
            }
        }

        sender.sendMessage("§eCreating world '" + worldName + "'... This may take a moment.");
        
        World world = plugin.getWorldManager().createWorld(worldName, type, environment, seed, null, null);
        if (world != null) {
            sender.sendMessage("§aSuccessfully created world '" + worldName + "'!");
        } else {
            sender.sendMessage("§cFailed to create world '" + worldName + "'!");
        }

        return true;
    }

    private boolean handleLoad(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.world.load")) {
            sender.sendMessage("§cYou don't have permission to load worlds!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /mv load <name>");
            return true;
        }

        String worldName = args[1];
        sender.sendMessage("§eLoading world '" + worldName + "'...");
        
        World world = plugin.getWorldManager().loadWorld(worldName);
        if (world != null) {
            sender.sendMessage("§aSuccessfully loaded world '" + worldName + "'!");
        } else {
            sender.sendMessage("§cFailed to load world '" + worldName + "'!");
        }

        return true;
    }

    private boolean handleUnload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.world.unload")) {
            sender.sendMessage("§cYou don't have permission to unload worlds!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /mv unload <name> [save]");
            return true;
        }

        String worldName = args[1];
        boolean save = args.length < 3 || !args[2].equalsIgnoreCase("false");

        if (plugin.getWorldManager().unloadWorld(worldName, save)) {
            sender.sendMessage("§aSuccessfully unloaded world '" + worldName + "'!");
        } else {
            sender.sendMessage("§cFailed to unload world '" + worldName + "'!");
        }

        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.world.delete")) {
            sender.sendMessage("§cYou don't have permission to delete worlds!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /mv delete <name>");
            sender.sendMessage("§cWARNING: This will permanently delete the world!");
            return true;
        }

        String worldName = args[1];
        sender.sendMessage("§cWARNING: This will permanently delete world '" + worldName + "'!");
        sender.sendMessage("§cThis action cannot be undone!");
        
        if (plugin.getWorldManager().deleteWorld(worldName)) {
            sender.sendMessage("§aSuccessfully deleted world '" + worldName + "'!");
        } else {
            sender.sendMessage("§cFailed to delete world '" + worldName + "'!");
        }

        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("ecore.world.list")) {
            sender.sendMessage("§cYou don't have permission to list worlds!");
            return true;
        }

        List<String> allWorlds = plugin.getWorldManager().getAllWorlds();
        List<String> loadedWorlds = plugin.getWorldManager().getLoadedWorlds();

        sender.sendMessage("§6=== Worlds ===");
        sender.sendMessage("§eTotal: §f" + allWorlds.size() + " §e| Loaded: §f" + loadedWorlds.size());
        sender.sendMessage("");

        for (String worldName : allWorlds) {
            boolean loaded = loadedWorlds.contains(worldName);
            String status = loaded ? "§a[LOADED]" : "§7[UNLOADED]";
            sender.sendMessage("  " + status + " §f" + worldName);
        }

        return true;
    }

    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /mv tp <world> [player]");
            return true;
        }

        String worldName = args[1];
        Player target = player;

        if (args.length >= 3 && sender.hasPermission("ecore.world.teleport.others")) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage("§cPlayer '" + args[2] + "' not found!");
                return true;
            }
        }

        if (!sender.hasPermission("ecore.world.teleport") && !sender.hasPermission("ecore.world.teleport.others")) {
            sender.sendMessage("§cYou don't have permission to teleport to worlds!");
            return true;
        }

        if (plugin.getWorldManager().teleportToWorld(target, worldName)) {
            if (!target.equals(player)) {
                sender.sendMessage("§aTeleported " + target.getName() + " to world '" + worldName + "'!");
            }
        } else {
            sender.sendMessage("§cFailed to teleport to world '" + worldName + "'!");
        }

        return true;
    }

    private boolean handleSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /mv spawn <world>");
            return true;
        }

        if (!sender.hasPermission("ecore.world.spawn")) {
            sender.sendMessage("§cYou don't have permission to teleport to world spawns!");
            return true;
        }

        String worldName = args[1];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            world = plugin.getWorldManager().loadWorld(worldName);
            if (world == null) {
                sender.sendMessage("§cWorld '" + worldName + "' does not exist!");
                return true;
            }
        }

        Location spawn = world.getSpawnLocation();
        WorldManager.WorldProperties props = plugin.getWorldManager().getWorldProperties(worldName);
        if (props != null && props.getSpawnLocation() != null) {
            spawn = props.getSpawnLocation();
        }

        plugin.getTeleportManager().teleport(player, spawn);
        player.sendMessage("§aTeleported to spawn of world '" + worldName + "'!");
        return true;
    }

    private boolean handleSetSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!sender.hasPermission("ecore.world.setspawn")) {
            sender.sendMessage("§cYou don't have permission to set world spawns!");
            return true;
        }

        Player player = (Player) sender;
        String worldName = args.length >= 2 ? args[1] : player.getWorld().getName();

        plugin.getWorldManager().setWorldSpawn(worldName, player.getLocation());
        sender.sendMessage("§aSet spawn for world '" + worldName + "' to your location!");
        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecore.world.info")) {
            sender.sendMessage("§cYou don't have permission to view world information!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /mv info <world>");
            return true;
        }

        String worldName = args[1];
        World world = Bukkit.getWorld(worldName);
        WorldManager.WorldProperties props = plugin.getWorldManager().getWorldProperties(worldName);

        sender.sendMessage("§6=== World Info: " + worldName + " ===");
        sender.sendMessage("§eLoaded: §f" + (world != null ? "Yes" : "No"));
        
        if (world != null) {
            // getWorldType is deprecated, use getEnvironment instead
            sender.sendMessage("§eEnvironment: §f" + world.getEnvironment().name());
            sender.sendMessage("§eDifficulty: §f" + world.getDifficulty().name());
            sender.sendMessage("§ePVP: §f" + (world.getPVP() ? "Enabled" : "Disabled"));
            sender.sendMessage("§ePlayers: §f" + world.getPlayers().size());
            sender.sendMessage("§eSpawn: §f" + world.getSpawnLocation().getBlockX() + ", " + 
                              world.getSpawnLocation().getBlockY() + ", " + world.getSpawnLocation().getBlockZ());
        }
        
        if (props != null) {
            sender.sendMessage("§eSeed: §f" + props.getSeed());
            sender.sendMessage("§eAuto-Load: §f" + (props.isAutoLoad() ? "Yes" : "No"));
        }

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("ecore.world.reload")) {
            sender.sendMessage("§cYou don't have permission to reload world configuration!");
            return true;
        }

        plugin.getWorldManager().loadWorlds();
        sender.sendMessage("§aReloaded world configuration!");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== World Management Commands ===");
        sender.sendMessage("§e/mv create <name> [type] [environment] [seed] §7- Create a new world");
        sender.sendMessage("§e/mv load <name> §7- Load a world");
        sender.sendMessage("§e/mv unload <name> [save] §7- Unload a world");
        sender.sendMessage("§e/mv delete <name> §7- Delete a world (WARNING: Permanent!)");
        sender.sendMessage("§e/mv list §7- List all worlds");
        sender.sendMessage("§e/mv tp <world> [player] §7- Teleport to a world");
        sender.sendMessage("§e/mv spawn <world> §7- Teleport to world spawn");
        sender.sendMessage("§e/mv setspawn [world] §7- Set world spawn");
        sender.sendMessage("§e/mv info <world> §7- View world information");
        sender.sendMessage("§e/mv reload §7- Reload world configuration");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "load", "unload", "delete", "list", "tp", "teleport", 
                               "spawn", "setspawn", "info", "reload").stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("tp") || subCommand.equals("teleport") || 
                subCommand.equals("spawn") || subCommand.equals("info") || 
                subCommand.equals("load") || subCommand.equals("unload") || 
                subCommand.equals("delete") || subCommand.equals("setspawn")) {
                return plugin.getWorldManager().getAllWorlds().stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("teleport"))) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}

