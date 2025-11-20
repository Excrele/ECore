package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * GUI manager for vault system.
 */
public class VaultGUIManager implements Listener {
    private final Ecore plugin;
    private final java.util.Map<Player, Integer> openVaults; // Player -> Vault Number

    public VaultGUIManager(Ecore plugin) {
        this.plugin = plugin;
        this.openVaults = new java.util.HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Opens the vault selection GUI.
     */
    public void openVaultSelectionGUI(Player player) {
        VaultManager vaultManager = plugin.getVaultManager();
        int maxVaults = vaultManager.getMaxVaults(player);
        List<Integer> existingVaults = vaultManager.getPlayerVaults(player);
        
        int size = Math.max(9, Math.min(54, ((maxVaults + 8) / 9) * 9));
        Inventory gui = Bukkit.createInventory(null, size, ChatColor.DARK_PURPLE + "Your Vaults");
        
        // Add existing vaults
        for (int i = 1; i <= maxVaults; i++) {
            ItemStack vaultItem;
            if (existingVaults.contains(i)) {
                // Existing vault
                String vaultName = vaultManager.getVaultName(player, i);
                vaultItem = createMenuItem(Material.ENDER_CHEST, 
                    ChatColor.GREEN + vaultName,
                    createVaultLore(player, i, true));
            } else {
                // Empty slot - can create
                vaultItem = createMenuItem(Material.CHEST, 
                    ChatColor.GRAY + "Vault #" + i + " (Empty)",
                    createVaultLore(player, i, false));
            }
            gui.setItem(i - 1, vaultItem);
        }
        
        player.openInventory(gui);
    }

    /**
     * Opens a specific vault.
     */
    public void openVault(Player player, int vaultNumber) {
        VaultManager vaultManager = plugin.getVaultManager();
        
        if (!vaultManager.hasVault(player, vaultNumber)) {
            // Create vault if it doesn't exist
            if (!vaultManager.createVault(player, vaultNumber)) {
                player.sendMessage(ChatColor.RED + "You cannot create more vaults!");
                return;
            }
        }
        
        ItemStack[] items = vaultManager.getVaultItems(player, vaultNumber);
        String vaultName = vaultManager.getVaultName(player, vaultNumber);
        
        Inventory vault = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + vaultName);
        
        // Copy items to inventory
        for (int i = 0; i < items.length && i < 54; i++) {
            if (items[i] != null) {
                vault.setItem(i, items[i].clone());
            }
        }
        
        openVaults.put(player, vaultNumber);
        player.openInventory(vault);
    }

    private List<String> createVaultLore(Player player, int vaultNumber, boolean exists) {
        List<String> lore = new ArrayList<>();
        if (exists) {
            VaultManager vaultManager = plugin.getVaultManager();
            lore.add(ChatColor.GRAY + "Click to open");
            lore.add(ChatColor.GRAY + "Right-click to rename");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Trusted Players:");
            List<UUID> trusted = vaultManager.getTrustedPlayers(player, vaultNumber);
            if (trusted.isEmpty()) {
                lore.add(ChatColor.GRAY + "  None");
            } else {
                for (UUID uuid : trusted) {
                    String name = Bukkit.getOfflinePlayer(uuid).getName();
                    lore.add(ChatColor.WHITE + "  - " + name);
                }
            }
        } else {
            lore.add(ChatColor.GRAY + "Click to create and open");
        }
        return lore;
    }

    private ItemStack createMenuItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Handle vault selection GUI
        if (title.equals(ChatColor.DARK_PURPLE + "Your Vaults")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            // Determine vault number from slot
            int vaultNumber = event.getSlot() + 1;
            
            if (event.isRightClick() && clicked.getType() == Material.ENDER_CHEST) {
                // Rename vault
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Please enter a new name for Vault #" + vaultNumber + " in chat:");
                plugin.registerPendingAction(player, "vault:rename:" + vaultNumber);
            } else {
                // Open vault
                openVault(player, vaultNumber);
            }
        }
        
        // Handle vault inventory (save on close)
        if (openVaults.containsKey(player) && title.startsWith(ChatColor.DARK_PURPLE.toString())) {
            // Allow normal inventory interaction, but we'll save on close
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        
        Integer vaultNumber = openVaults.remove(player);
        if (vaultNumber != null) {
            // Save vault contents
            Inventory inventory = event.getInventory();
            if (inventory.getSize() == 54) {
                ItemStack[] items = new ItemStack[54];
                for (int i = 0; i < 54; i++) {
                    ItemStack item = inventory.getItem(i);
                    items[i] = item != null ? item.clone() : null;
                }
                plugin.getVaultManager().setVaultItems(player, vaultNumber, items);
            }
        }
    }
}

