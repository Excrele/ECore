package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

public class GameModeManager implements Listener {
    private final Ecore plugin;

    public GameModeManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Open GameMode GUI
    public void openGameModeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 9, ChatColor.DARK_GREEN + "Game Mode Selector");

        // Survival Mode
        ItemStack survival = new ItemStack(Material.IRON_SWORD);
        ItemMeta survivalMeta = survival.getItemMeta();
        survivalMeta.setDisplayName(ChatColor.GREEN + "Survival Mode");
        survival.setItemMeta(survivalMeta);
        gui.setItem(1, survival);

        // Creative Mode
        ItemStack creative = new ItemStack(Material.BRICK);
        ItemMeta creativeMeta = creative.getItemMeta();
        creativeMeta.setDisplayName(ChatColor.AQUA + "Creative Mode");
        creative.setItemMeta(creativeMeta);
        gui.setItem(3, creative);

        // Adventure Mode
        ItemStack adventure = new ItemStack(Material.MAP);
        ItemMeta adventureMeta = adventure.getItemMeta();
        adventureMeta.setDisplayName(ChatColor.YELLOW + "Adventure Mode");
        adventure.setItemMeta(adventureMeta);
        gui.setItem(5, adventure);

        // Spectator Mode
        ItemStack spectator = new ItemStack(Material.ENDER_EYE);
        ItemMeta spectatorMeta = spectator.getItemMeta();
        spectatorMeta.setDisplayName(ChatColor.GRAY + "Spectator Mode");
        spectator.setItemMeta(spectatorMeta);
        gui.setItem(7, spectator);

        player.openInventory(gui);
    }

    // Handle game mode change
    public void handleGameModeChange(Player player, GameMode mode) {
        String modeName = mode.toString();
        if (!player.hasPermission("ecore.gamemode." + modeName.toLowerCase())) {
            player.sendMessage(ChatColor.RED + "You don't have permission to switch to " + modeName + "!");
            return;
        }
        player.setGameMode(mode);
        player.sendMessage(ChatColor.GREEN + "Game mode changed to " + modeName + "!");
        plugin.getDiscordManager().sendStaffLogNotification("gamemode-log", player.getName(), "changed gamemode to", modeName, "");
    }
}