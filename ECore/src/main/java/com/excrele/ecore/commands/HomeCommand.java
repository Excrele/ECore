package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeCommand implements CommandExecutor, Listener {
    private final Ecore plugin;

    public HomeCommand(Ecore plugin) {
        this.plugin = plugin;
        // Register this class as a listener for inventory click events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("ecore.home")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length > 0) {
            // Handle direct /home [name] command
            String homeName = args[0];
            Location home = plugin.getHomeManager().getHome(player, homeName);
            if (home != null) {
                player.teleport(home);
                player.sendMessage(ChatColor.GREEN + "Teleported to home '" + homeName + "'!");
            } else {
                player.sendMessage(ChatColor.RED + "Home '" + homeName + "' not found!");
            }
        } else {
            // Open home GUI
            openHomeGUI(player);
        }
        return true;
    }

    // Open the main home management GUI
    private void openHomeGUI(Player player) {
        Inventory gui = plugin.getServer().createInventory(null, 27, ChatColor.DARK_BLUE + "Home Menu");

        // Add player's homes to the GUI
        Map<String, Location> homes = plugin.getHomeManager().getPlayerHomes(player);
        int slot = 0;
        for (Map.Entry<String, Location> entry : homes.entrySet()) {
            if (slot >= 18) break; // Limit to first 18 slots for homes
            ItemStack homeItem = new ItemStack(Material.COMPASS);
            ItemMeta meta = homeItem.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + entry.getKey());
            List<String> lore = new ArrayList<>();
            Location loc = entry.getValue();
            lore.add(ChatColor.GRAY + "World: " + loc.getWorld().getName());
            lore.add(ChatColor.GRAY + "X: " + loc.getBlockX());
            lore.add(ChatColor.GRAY + "Y: " + loc.getBlockY());
            lore.add(ChatColor.GRAY + "Z: " + loc.getBlockZ());
            lore.add(ChatColor.YELLOW + "Click to teleport!");
            meta.setLore(lore);
            homeItem.setItemMeta(meta);
            gui.setItem(slot++, homeItem);
        }

        // Add action buttons
        ItemStack setHome = new ItemStack(Material.GREEN_WOOL);
        ItemMeta setMeta = setHome.getItemMeta();
        setMeta.setDisplayName(ChatColor.GREEN + "Set New Home");
        List<String> setLore = new ArrayList<>();
        setLore.add(ChatColor.GRAY + "Click to set a new home");
        setMeta.setLore(setLore);
        setHome.setItemMeta(setMeta);

        ItemStack deleteHome = new ItemStack(Material.RED_WOOL);
        ItemMeta deleteMeta = deleteHome.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.RED + "Delete Home");
        List<String> deleteLore = new ArrayList<>();
        deleteLore.add(ChatColor.GRAY + "Click to select a home to delete");
        deleteMeta.setLore(deleteLore);
        deleteHome.setItemMeta(deleteMeta);

        ItemStack renameHome = new ItemStack(Material.NAME_TAG);
        ItemMeta renameMeta = renameHome.getItemMeta();
        renameMeta.setDisplayName(ChatColor.BLUE + "Rename Home");
        List<String> renameLore = new ArrayList<>();
        renameLore.add(ChatColor.GRAY + "Click to select a home to rename");
        renameMeta.setLore(renameLore);
        renameHome.setItemMeta(renameMeta);

        // Place action buttons
        gui.setItem(20, setHome);
        gui.setItem(22, deleteHome);
        gui.setItem(24, renameHome);

        player.openInventory(gui);
    }

    // Open submenu for deleting homes
    private void openDeleteHomeGUI(Player player) {
        Inventory gui = plugin.getServer().createInventory(null, 27, ChatColor.DARK_BLUE + "Delete Home");
        Map<String, Location> homes = plugin.getHomeManager().getPlayerHomes(player);
        int slot = 0;
        for (String homeName : homes.keySet()) {
            if (slot >= 27) break;
            ItemStack homeItem = new ItemStack(Material.REDSTONE);
            ItemMeta meta = homeItem.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + homeName);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to delete this home");
            meta.setLore(lore);
            homeItem.setItemMeta(meta);
            gui.setItem(slot++, homeItem);
        }
        player.openInventory(gui);
    }

    // Open submenu for renaming homes
    private void openRenameHomeGUI(Player player) {
        Inventory gui = plugin.getServer().createInventory(null, 27, ChatColor.DARK_BLUE + "Rename Home");
        Map<String, Location> homes = plugin.getHomeManager().getPlayerHomes(player);
        int slot = 0;
        for (String homeName : homes.keySet()) {
            if (slot >= 27) break;
            ItemStack homeItem = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = homeItem.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + homeName);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to rename this home");
            meta.setLore(lore);
            homeItem.setItemMeta(meta);
            gui.setItem(slot++, homeItem);
        }
        player.openInventory(gui);
    }

    // Handle inventory click events for home GUI
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.equals(ChatColor.DARK_BLUE + "Home Menu") &&
                !title.equals(ChatColor.DARK_BLUE + "Delete Home") &&
                !title.equals(ChatColor.DARK_BLUE + "Rename Home")) {
            return;
        }

        event.setCancelled(true); // Prevent item movement
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();

        // Handle main home menu
        if (title.equals(ChatColor.DARK_BLUE + "Home Menu")) {
            if (displayName.equals(ChatColor.GREEN + "Set New Home")) {
                player.closeInventory();
                plugin.registerPendingAction(player, "sethome:new");
                player.sendMessage(ChatColor.YELLOW + "Please type the name for the new home in chat.");
            } else if (displayName.equals(ChatColor.RED + "Delete Home")) {
                openDeleteHomeGUI(player);
            } else if (displayName.equals(ChatColor.BLUE + "Rename Home")) {
                openRenameHomeGUI(player);
            } else if (clicked.getType() == Material.COMPASS) {
                String homeName = displayName.replace(ChatColor.AQUA.toString(), "");
                Location home = plugin.getHomeManager().getHome(player, homeName);
                if (home != null) {
                    player.closeInventory();
                    player.teleport(home);
                    player.sendMessage(ChatColor.GREEN + "Teleported to home '" + homeName + "'!");
                }
            }
        }
        // Handle delete home submenu
        else if (title.equals(ChatColor.DARK_BLUE + "Delete Home")) {
            String homeName = displayName.replace(ChatColor.AQUA.toString(), "");
            Map<String, Location> homes = plugin.getHomeManager().getPlayerHomes(player);
            if (homes.containsKey(homeName)) {
                homes.remove(homeName);
                plugin.getHomeManager().saveHomes();
                player.sendMessage(ChatColor.GREEN + "Deleted home '" + homeName + "'!");
                player.closeInventory();
                openHomeGUI(player); // Return to main menu
            }
        }
        // Handle rename home submenu
        else if (title.equals(ChatColor.DARK_BLUE + "Rename Home")) {
            String homeName = displayName.replace(ChatColor.AQUA.toString(), "");
            Map<String, Location> homes = plugin.getHomeManager().getPlayerHomes(player);
            if (homes.containsKey(homeName)) {
                player.closeInventory();
                plugin.registerPendingAction(player, "renamehome:" + homeName);
                player.sendMessage(ChatColor.YELLOW + "Please type the new name for home '" + homeName + "' in chat.");
            }
        }
    }
}