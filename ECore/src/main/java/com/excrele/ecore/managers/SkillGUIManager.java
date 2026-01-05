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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkillGUIManager implements Listener {
    private final Ecore plugin;
    
    public SkillGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openSkillsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 54, ChatColor.DARK_AQUA + "Skills");
        
        int slot = 0;
        for (SkillManager.SkillType skillType : plugin.getSkillManager().getSkillTypes()) {
            SkillManager.PlayerSkillData skillData = plugin.getSkillManager().getPlayerSkill(player, skillType.getId());
            int level = skillData != null ? skillData.getLevel() : 1;
            double experience = skillData != null ? skillData.getExperience() : 0.0;
            int prestige = skillData != null ? skillData.getPrestige() : 0;
            double expForNext = skillData != null ? skillData.getExpForNextLevel() : 100.0;
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + skillType.getDescription());
            lore.add("");
            lore.add(ChatColor.GRAY + "Level: " + ChatColor.WHITE + level);
            lore.add(ChatColor.GRAY + "Experience: " + ChatColor.WHITE + String.format("%.1f", experience) + "/" + String.format("%.1f", expForNext));
            if (prestige > 0) {
                lore.add(ChatColor.GOLD + "Prestige: " + prestige);
            }
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to view details");
            
            ItemStack skillItem = createGuiItem(skillType.getIcon(), ChatColor.AQUA + skillType.getName(), lore);
            gui.setItem(slot++, skillItem);
        }
        
        // Leaderboard button
        ItemStack leaderboard = createGuiItem(Material.GOLDEN_APPLE, ChatColor.GOLD + "Leaderboards", 
            Arrays.asList(ChatColor.GRAY + "View skill leaderboards"));
        gui.setItem(49, leaderboard);
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (event.getView().getTitle().equals(ChatColor.DARK_AQUA + "Skills")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;
            
            if (clicked.getType() == Material.GOLDEN_APPLE) {
                player.sendMessage(ChatColor.YELLOW + "Leaderboards coming soon!");
            } else {
                // Show skill details
                player.sendMessage(ChatColor.YELLOW + "Skill details coming soon!");
            }
        }
    }
    
    private ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}

