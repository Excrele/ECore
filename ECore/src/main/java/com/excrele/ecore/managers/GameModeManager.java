package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

// Manages the GameMode GUI and mode switching
public class GameModeManager implements Listener {
    private final Ecore plugin;

    public GameModeManager(Ecore plugin) {
        this.plugin = plugin;
        // Register this class as a listener for inventory click events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Open the GameMode GUI
    public void openGameModeGUI(Player player) {
        Inventory gui = plugin.getServer().createInventory(null, 9, ChatColor.DARK_GREEN + "GameMode Selector");

        // Create items for each game mode
        ItemStack survivalItem = createItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Survival Mode",
                "Click to switch to Survival", "ecore.gamemode.survival");
        ItemStack creativeItem = createItem(Material.CRAFTING_TABLE, ChatColor.AQUA + "Creative Mode",
                "Click to switch to Creative", "ecore.gamemode.creative");
        ItemStack adventureItem = createItem(Material.MAP, ChatColor.YELLOW + "Adventure Mode",
                "Click to switch to Adventure", "ecore.gamemode.adventure");
        ItemStack spectatorItem = createItem(Material.ENDER_EYE, ChatColor.LIGHT_PURPLE + "Spectator Mode",
                "Click to switch to Spectator", "ecore.gamemode.spectator");

        // Place items in the GUI
        gui.setItem(2, survivalItem);
        gui.setItem(3, creativeItem);
        gui.setItem(5, adventureItem);
        gui.setItem(6, spectatorItem);

        player.openInventory(gui);
    }

    // Create an item with name, lore, and permission info
    private ItemStack createItem(Material material, String name, String lore, String permission) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(ChatColor.GRAY + lore, ChatColor.DARK_GRAY + "Permission: " + permission));
        item.setItemMeta(meta);
        return item;
    }

    // Switch player's game mode
    public void switchGameMode(Player player, GameMode mode, String modeName, String permission) {
        if (!player.hasPermission(permission)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use " + modeName + " mode!");
            player.closeInventory();
            return;
        }
        player.setGameMode(mode);
        player.sendMessage(ChatColor.GREEN + "Game mode changed to " + modeName + "!");
        // Send Discord notification
        plugin.getDiscordManager().sendPunishmentNotification(player.getName(), "changed gamemode to", modeName, "Game mode switch");
        player.closeInventory();
    }

    // Handle inventory click events for GameMode GUI
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!event.getView().getTitle().equals(ChatColor.DARK_GREEN + "GameMode Selector")) return;

        event.setCancelled(true); // Prevent item movement
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();

        if (displayName.equals(ChatColor.GREEN + "Survival Mode")) {
            switchGameMode(player, GameMode.SURVIVAL, "Survival", "ecore.gamemode.survival");
        } else if (displayName.equals(ChatColor.AQUA + "Creative Mode")) {
            switchGameMode(player, GameMode.CREATIVE, "Creative", "ecore.gamemode.creative");
        } else if (displayName.equals(ChatColor.YELLOW + "Adventure Mode")) {
            switchGameMode(player, GameMode.ADVENTURE, "Adventure", "ecore.gamemode.adventure");
        } else if (displayName.equals(ChatColor.LIGHT_PURPLE + "Spectator Mode")) {
            switchGameMode(player, GameMode.SPECTATOR, "Spectator", "ecore.gamemode.spectator");
        }
    }
}