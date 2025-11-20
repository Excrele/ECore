package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for custom recipes system.
 */
public class RecipeCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public RecipeCommand(Ecore plugin) {
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
                if (!sender.hasPermission("ecore.recipe.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                return handleCreate(sender, args);
            case "remove":
            case "delete":
                if (!sender.hasPermission("ecore.recipe.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                return handleRemove(sender, args);
            case "list":
                return handleList(sender);
            case "reload":
                if (!sender.hasPermission("ecore.recipe.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /recipe create <id> <shaped|shapeless>");
            sender.sendMessage(ChatColor.YELLOW + "Then follow the prompts or use the GUI.");
            return true;
        }

        String recipeId = args[1];
        if (plugin.getRecipeManager().hasRecipe(recipeId)) {
            sender.sendMessage(ChatColor.RED + "Recipe with ID '" + recipeId + "' already exists!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /recipe create <id> <shaped|shapeless>");
            return true;
        }

        String type = args[2].toLowerCase();
        if (!type.equals("shaped") && !type.equals("shapeless")) {
            sender.sendMessage(ChatColor.RED + "Type must be 'shaped' or 'shapeless'!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        
        if (heldItem.getType() == Material.AIR) {
            sender.sendMessage(ChatColor.RED + "You must hold the result item in your hand!");
            return true;
        }

        if (type.equals("shaped")) {
            sender.sendMessage(ChatColor.YELLOW + "Please arrange items in a crafting table and type 'confirm' in chat.");
            sender.sendMessage(ChatColor.GRAY + "The recipe will be saved based on your current crafting table layout.");
            plugin.registerPendingAction(player, "recipe:create:shaped:" + recipeId);
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Please place all ingredients in your inventory and type 'confirm' in chat.");
            sender.sendMessage(ChatColor.GRAY + "The recipe will use the items in your inventory slots 0-8.");
            plugin.registerPendingAction(player, "recipe:create:shapeless:" + recipeId);
        }

        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /recipe remove <id>");
            return true;
        }

        String recipeId = args[1];
        if (!plugin.getRecipeManager().hasRecipe(recipeId)) {
            sender.sendMessage(ChatColor.RED + "Recipe with ID '" + recipeId + "' does not exist!");
            return true;
        }

        if (plugin.getRecipeManager().removeRecipe(recipeId)) {
            sender.sendMessage(ChatColor.GREEN + "Recipe '" + recipeId + "' removed!");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to remove recipe!");
        }

        return true;
    }

    private boolean handleList(CommandSender sender) {
        List<String> recipes = plugin.getRecipeManager().getRecipeIds();
        
        if (recipes.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No custom recipes found.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        sender.sendMessage(ChatColor.YELLOW + "          Custom Recipes (" + recipes.size() + ")");
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");

        for (String recipeId : recipes) {
            sender.sendMessage(ChatColor.WHITE + "- " + ChatColor.YELLOW + recipeId);
        }

        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        plugin.getRecipeManager().reload();
        sender.sendMessage(ChatColor.GREEN + "Recipes reloaded!");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Recipe Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/recipe list - List all custom recipes");
        if (sender.hasPermission("ecore.recipe.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/recipe create <id> <shaped|shapeless> - Create a recipe");
            sender.sendMessage(ChatColor.YELLOW + "/recipe remove <id> - Remove a recipe");
            sender.sendMessage(ChatColor.YELLOW + "/recipe reload - Reload recipes from config");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "remove", "list", "reload")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("remove"))) {
            return plugin.getRecipeManager().getRecipeIds().stream()
                    .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return Arrays.asList("shaped", "shapeless")
                    .stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}

