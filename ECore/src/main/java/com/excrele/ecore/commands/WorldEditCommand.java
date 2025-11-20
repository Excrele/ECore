package com.excrele.ecore.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.WorldEditManager;

/**
 * Handles all WorldEdit-related commands including:
 * - Selection commands (wand, pos1, pos2)
 * - Block operations (set, replace, clear, walls, hollow)
 * - Clipboard operations (copy, paste, cut)
 * - History operations (undo, redo)
 * - Schematic operations (save, load, list)
 * - Brush operations (sphere, cylinder)
 * 
 * @author Excrele
 * @version 1.0
 */
public class WorldEditCommand implements CommandExecutor {
    private final Ecore plugin;
    
    public WorldEditCommand(Ecore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        WorldEditManager weManager = plugin.getWorldEditManager();
        
        // Permission check for most commands
        if (!player.hasPermission("ecore.worldedit.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use WorldEdit commands!");
            return true;
        }
        
        // Handle different commands
        switch (cmd) {
            case "wand":
                return handleWand(player);
            case "pos1":
                return handlePos1(player, weManager);
            case "pos2":
                return handlePos2(player, weManager);
            case "set":
                return handleSet(player, args, weManager);
            case "replace":
                return handleReplace(player, args, weManager);
            case "clear":
                return handleClear(player, weManager);
            case "walls":
                return handleWalls(player, args, weManager);
            case "hollow":
                return handleHollow(player, args, weManager);
            case "copy":
                return handleCopy(player, weManager);
            case "paste":
                return handlePaste(player, weManager);
            case "cut":
                return handleCut(player, weManager);
            case "undo":
                return handleUndo(player, weManager);
            case "redo":
                return handleRedo(player, weManager);
            case "schematic":
                return handleSchematic(player, args, weManager);
            case "sphere":
                return handleSphere(player, args, weManager);
            case "cylinder":
                return handleCylinder(player, args, weManager);
            case "sel":
            case "selection":
                return handleSelection(player, weManager);
            default:
                return false;
        }
    }
    
    private boolean handleWand(Player player) {
        if (!player.hasPermission("ecore.worldedit.wand")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        ItemStack wand = new ItemStack(Material.WOODEN_AXE);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Selection Wand");
            meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Left-click to set position 1",
                ChatColor.GRAY + "Right-click to set position 2"
            ));
            wand.setItemMeta(meta);
        }
        
        player.getInventory().addItem(wand);
        player.sendMessage(ChatColor.GREEN + "Selection wand added to your inventory!");
        return true;
    }
    
    private boolean handlePos1(Player player, WorldEditManager weManager) {
        weManager.setPos1(player, player.getLocation());
        return true;
    }
    
    private boolean handlePos2(Player player, WorldEditManager weManager) {
        weManager.setPos2(player, player.getLocation());
        return true;
    }
    
    private boolean handleSet(Player player, String[] args, WorldEditManager weManager) {
        if (!player.hasPermission("ecore.worldedit.set")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /set <block>");
            return true;
        }
        
        Material material = Material.matchMaterial(args[0]);
        if (material == null || !material.isBlock()) {
            player.sendMessage(ChatColor.RED + "Invalid block type: " + args[0]);
            return true;
        }
        
        weManager.setBlocks(player, material);
        return true;
    }
    
    private boolean handleReplace(Player player, String[] args, WorldEditManager weManager) {
        if (!player.hasPermission("ecore.worldedit.replace")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /replace <from> <to>");
            return true;
        }
        
        Material from = Material.matchMaterial(args[0]);
        Material to = Material.matchMaterial(args[1]);
        
        if (from == null || !from.isBlock()) {
            player.sendMessage(ChatColor.RED + "Invalid block type: " + args[0]);
            return true;
        }
        
        if (to == null || !to.isBlock()) {
            player.sendMessage(ChatColor.RED + "Invalid block type: " + args[1]);
            return true;
        }
        
        weManager.replaceBlocks(player, from, to);
        return true;
    }
    
    private boolean handleClear(Player player, WorldEditManager weManager) {
        if (!player.hasPermission("ecore.worldedit.clear")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        weManager.clearBlocks(player);
        return true;
    }
    
    private boolean handleWalls(Player player, String[] args, WorldEditManager weManager) {
        if (!player.hasPermission("ecore.worldedit.walls")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /walls <block>");
            return true;
        }
        
        Material material = Material.matchMaterial(args[0]);
        if (material == null || !material.isBlock()) {
            player.sendMessage(ChatColor.RED + "Invalid block type: " + args[0]);
            return true;
        }
        
        weManager.createWalls(player, material);
        return true;
    }
    
    private boolean handleHollow(Player player, String[] args, WorldEditManager weManager) {
        if (!player.hasPermission("ecore.worldedit.hollow")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /hollow <block>");
            return true;
        }
        
        Material material = Material.matchMaterial(args[0]);
        if (material == null || !material.isBlock()) {
            player.sendMessage(ChatColor.RED + "Invalid block type: " + args[0]);
            return true;
        }
        
        weManager.createHollow(player, material);
        return true;
    }
    
    private boolean handleCopy(Player player, WorldEditManager weManager) {
        if (!player.hasPermission("ecore.worldedit.copy")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        weManager.copySelection(player);
        return true;
    }
    
    private boolean handlePaste(Player player, WorldEditManager weManager) {
        if (!player.hasPermission("ecore.worldedit.paste")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        weManager.pasteClipboard(player);
        return true;
    }
    
    private boolean handleCut(Player player, WorldEditManager weManager) {
        if (!player.hasPermission("ecore.worldedit.cut")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        weManager.cutSelection(player);
        return true;
    }
    
    private boolean handleUndo(Player player, WorldEditManager weManager) {
        if (!player.hasPermission("ecore.worldedit.undo")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        weManager.undo(player);
        return true;
    }
    
    private boolean handleRedo(Player player, WorldEditManager weManager) {
        if (!player.hasPermission("ecore.worldedit.redo")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        weManager.redo(player);
        return true;
    }
    
    private boolean handleSchematic(Player player, String[] args, WorldEditManager weManager) {
        if (!player.hasPermission("ecore.worldedit.schematic")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /schematic <save|load|list|delete> [name]");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "save":
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /schematic save <name>");
                    return true;
                }
                weManager.saveSchematic(player, args[1]);
                break;
            case "load":
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /schematic load <name>");
                    return true;
                }
                weManager.loadSchematic(player, args[1]);
                break;
            case "list":
                List<String> schematics = weManager.getSchematics();
                if (schematics.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "No schematics found.");
                } else {
                    player.sendMessage(ChatColor.GREEN + "Available schematics (" + schematics.size() + "):");
                    player.sendMessage(ChatColor.GRAY + String.join(", ", schematics));
                }
                break;
            case "delete":
                if (!player.hasPermission("ecore.worldedit.schematic.delete")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /schematic delete <name>");
                    return true;
                }
                // Delete schematic functionality would go here
                player.sendMessage(ChatColor.YELLOW + "Schematic deletion not yet implemented.");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Usage: /schematic <save|load|list|delete> [name]");
                return true;
        }
        
        return true;
    }
    
    private boolean handleSphere(Player player, String[] args, WorldEditManager weManager) {
        if (!player.hasPermission("ecore.worldedit.sphere")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        if (args.length < 2 || args.length > 3) {
            player.sendMessage(ChatColor.RED + "Usage: /sphere <radius> <block> [hollow]");
            return true;
        }
        
        int radius;
        try {
            radius = Integer.parseInt(args[0]);
            if (radius < 1 || radius > 50) {
                player.sendMessage(ChatColor.RED + "Radius must be between 1 and 50!");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid radius: " + args[0]);
            return true;
        }
        
        Material material = Material.matchMaterial(args[1]);
        if (material == null || !material.isBlock()) {
            player.sendMessage(ChatColor.RED + "Invalid block type: " + args[1]);
            return true;
        }
        
        boolean hollow = args.length == 3 && args[2].equalsIgnoreCase("hollow");
        weManager.createSphere(player, player.getLocation(), radius, material, hollow);
        return true;
    }
    
    private boolean handleCylinder(Player player, String[] args, WorldEditManager weManager) {
        if (!player.hasPermission("ecore.worldedit.cylinder")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        
        if (args.length < 3 || args.length > 4) {
            player.sendMessage(ChatColor.RED + "Usage: /cylinder <radius> <height> <block> [hollow]");
            return true;
        }
        
        int radius;
        int height;
        try {
            radius = Integer.parseInt(args[0]);
            height = Integer.parseInt(args[1]);
            if (radius < 1 || radius > 50) {
                player.sendMessage(ChatColor.RED + "Radius must be between 1 and 50!");
                return true;
            }
            if (height < 1 || height > 100) {
                player.sendMessage(ChatColor.RED + "Height must be between 1 and 100!");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid radius or height!");
            return true;
        }
        
        Material material = Material.matchMaterial(args[2]);
        if (material == null || !material.isBlock()) {
            player.sendMessage(ChatColor.RED + "Invalid block type: " + args[2]);
            return true;
        }
        
        boolean hollow = args.length == 4 && args[3].equalsIgnoreCase("hollow");
        weManager.createCylinder(player, player.getLocation(), radius, height, material, hollow);
        return true;
    }
    
    private boolean handleSelection(Player player, WorldEditManager weManager) {
        String info = weManager.getSelectionInfo(player);
        player.sendMessage(info);
        return true;
    }
}

