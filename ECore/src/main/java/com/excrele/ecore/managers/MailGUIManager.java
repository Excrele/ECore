package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MailGUIManager implements Listener {
    private final Ecore plugin;
    private final SimpleDateFormat dateFormat;

    public MailGUIManager(Ecore plugin) {
        this.plugin = plugin;
        this.dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openMailGUI(Player player) {
        List<MailManager.MailEntry> mail = plugin.getMailManager().getMail(player);
        int size = Math.max(9, ((mail.size() + 8) / 9) * 9);
        size = Math.min(size, 54);
        
        Inventory gui = Bukkit.createInventory(player, size, ChatColor.DARK_BLUE + "Your Mail (" + mail.size() + ")");

        for (int i = 0; i < mail.size() && i < size - 1; i++) {
            MailManager.MailEntry entry = mail.get(i);
            ItemStack mailItem = new ItemStack(Material.PAPER);
            ItemMeta meta = mailItem.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "From: " + entry.getSender());
            meta.setLore(List.of(
                ChatColor.GRAY + "Message: " + ChatColor.WHITE + entry.getMessage(),
                ChatColor.GRAY + "Date: " + ChatColor.WHITE + dateFormat.format(new Date(entry.getTimestamp())),
                ChatColor.GRAY + "Click to view details"
            ));
            mailItem.setItemMeta(meta);
            gui.setItem(i, mailItem);
        }

        // Clear all button
        ItemStack clearAll = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta clearMeta = clearAll.getItemMeta();
        clearMeta.setDisplayName(ChatColor.RED + "Clear All Mail");
        clearAll.setItemMeta(clearMeta);
        gui.setItem(size - 1, clearAll);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        if (!title.startsWith(ChatColor.DARK_BLUE + "Your Mail")) return;
        
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.REDSTONE_BLOCK) {
            // Clear all mail
            plugin.getMailManager().clearMail(player);
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "All mail cleared!");
        } else if (clicked.getType() == Material.PAPER) {
            // Show mail details
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.getLore() != null) {
                player.sendMessage(ChatColor.GOLD + "=== Mail Details ===");
                for (String line : meta.getLore()) {
                    player.sendMessage(line);
                }
            }
        }
    }
}

