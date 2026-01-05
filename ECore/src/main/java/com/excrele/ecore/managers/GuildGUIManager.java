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

public class GuildGUIManager implements Listener {
    private final Ecore plugin;
    
    public GuildGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void openGuildGUI(Player player) {
        GuildManager.Guild guild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
        
        if (guild == null) {
            openGuildListGUI(player, 1);
            return;
        }
        
        Inventory gui = Bukkit.createInventory(player, 54, ChatColor.DARK_PURPLE + "Guild: " + guild.getName());
        
        // Guild info
        ItemStack info = createGuiItem(Material.BOOK, ChatColor.GOLD + "Guild Info", Arrays.asList(
            ChatColor.GRAY + "Name: " + ChatColor.WHITE + guild.getName(),
            ChatColor.GRAY + "Tag: " + ChatColor.WHITE + guild.getTag(),
            ChatColor.GRAY + "Level: " + ChatColor.WHITE + guild.getLevel(),
            ChatColor.GRAY + "Experience: " + ChatColor.WHITE + String.format("%.1f", guild.getExperience()) + "/" + String.format("%.1f", guild.getExpForNextLevel()),
            ChatColor.GRAY + "Members: " + ChatColor.WHITE + guild.getMembers().size(),
            ChatColor.GRAY + "Bank: " + ChatColor.GREEN + plugin.getEconomyManager().format(guild.getBankBalance())
        ));
        gui.setItem(4, info);
        
        // Members
        ItemStack members = createGuiItem(Material.PLAYER_HEAD, ChatColor.AQUA + "Members", 
            Arrays.asList(ChatColor.GRAY + "View and manage guild members"));
        gui.setItem(10, members);
        
        // Guild Bank
        ItemStack bank = createGuiItem(Material.GOLD_INGOT, ChatColor.YELLOW + "Guild Bank", 
            Arrays.asList(ChatColor.GRAY + "Deposit/Withdraw from guild bank"));
        gui.setItem(12, bank);
        
        // Guild Warps
        ItemStack warps = createGuiItem(Material.ENDER_PEARL, ChatColor.LIGHT_PURPLE + "Guild Warps", 
            Arrays.asList(ChatColor.GRAY + "View guild warps"));
        gui.setItem(14, warps);
        
        // Guild Homes
        ItemStack homes = createGuiItem(Material.RED_BED, ChatColor.RED + "Guild Homes", 
            Arrays.asList(ChatColor.GRAY + "View guild homes"));
        gui.setItem(16, homes);
        
        // Alliances
        ItemStack alliances = createGuiItem(Material.GOLDEN_APPLE, ChatColor.GREEN + "Alliances", 
            Arrays.asList(ChatColor.GRAY + "View guild alliances"));
        gui.setItem(28, alliances);
        
        // Wars
        ItemStack wars = createGuiItem(Material.IRON_SWORD, ChatColor.DARK_RED + "Wars", 
            Arrays.asList(ChatColor.GRAY + "View active wars"));
        gui.setItem(30, wars);
        
        // Statistics
        ItemStack stats = createGuiItem(Material.DIAMOND, ChatColor.BLUE + "Statistics", Arrays.asList(
            ChatColor.GRAY + "Kills: " + ChatColor.WHITE + guild.getKills(),
            ChatColor.GRAY + "Deaths: " + ChatColor.WHITE + guild.getDeaths(),
            ChatColor.GRAY + "Quests: " + ChatColor.WHITE + guild.getQuestsCompleted()
        ));
        gui.setItem(32, stats);
        
        // Applications (if leader/officer)
        GuildManager.GuildMember member = guild.getMember(player.getUniqueId());
        if (member != null && (member.getRank().equalsIgnoreCase("leader") || member.getRank().equalsIgnoreCase("officer"))) {
            ItemStack applications = createGuiItem(Material.PAPER, ChatColor.YELLOW + "Applications", 
                Arrays.asList(ChatColor.GRAY + "View pending applications"));
            gui.setItem(34, applications);
        }
        
        player.openInventory(gui);
    }
    
    public void openGuildListGUI(Player player, int page) {
        List<GuildManager.Guild> allGuilds = new ArrayList<>(plugin.getGuildManager().getGuilds());
        int entriesPerPage = 45;
        int totalPages = (int) Math.ceil((double) allGuilds.size() / entriesPerPage);
        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;
        
        Inventory gui = Bukkit.createInventory(player, 54, ChatColor.DARK_PURPLE + "Guilds (Page " + page + "/" + Math.max(1, totalPages) + ")");
        
        int start = (page - 1) * entriesPerPage;
        int end = Math.min(start + entriesPerPage, allGuilds.size());
        
        for (int i = start; i < end; i++) {
            GuildManager.Guild guild = allGuilds.get(i);
            ItemStack guildItem = createGuiItem(Material.WHITE_BANNER, ChatColor.GOLD + guild.getName(), Arrays.asList(
                ChatColor.GRAY + "Tag: " + ChatColor.WHITE + guild.getTag(),
                ChatColor.GRAY + "Level: " + ChatColor.WHITE + guild.getLevel(),
                ChatColor.GRAY + "Members: " + ChatColor.WHITE + guild.getMembers().size(),
                "",
                ChatColor.YELLOW + "Click to view details"
            ));
            gui.setItem(i - start, guildItem);
        }
        
        // Navigation
        if (page > 1) {
            ItemStack prev = createGuiItem(Material.ARROW, ChatColor.GREEN + "Previous Page", null);
            gui.setItem(45, prev);
        }
        if (page < totalPages) {
            ItemStack next = createGuiItem(Material.ARROW, ChatColor.GREEN + "Next Page", null);
            gui.setItem(53, next);
        }
        
        // Create Guild button
        ItemStack create = createGuiItem(Material.EMERALD, ChatColor.GREEN + "Create Guild", 
            Arrays.asList(ChatColor.GRAY + "Create a new guild"));
        gui.setItem(49, create);
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.startsWith(ChatColor.DARK_PURPLE + "Guild: ")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;
            
            GuildManager.Guild guild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
            if (guild == null) return;
            
            if (clicked.getType() == Material.PLAYER_HEAD) {
                // Open members GUI
                player.sendMessage(ChatColor.YELLOW + "Members GUI coming soon!");
            } else if (clicked.getType() == Material.GOLD_INGOT) {
                // Open bank GUI
                player.sendMessage(ChatColor.YELLOW + "Guild bank GUI coming soon!");
            } else if (clicked.getType() == Material.PAPER) {
                // Open applications GUI
                player.sendMessage(ChatColor.YELLOW + "Applications GUI coming soon!");
            }
        } else if (title.startsWith(ChatColor.DARK_PURPLE + "Guilds (")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;
            
            if (clicked.getType() == Material.ARROW) {
                String[] parts = title.split(" ");
                int currentPage = Integer.parseInt(parts[1].replace("(", ""));
                if (clicked.getItemMeta().getDisplayName().contains("Previous")) {
                    openGuildListGUI(player, currentPage - 1);
                } else {
                    openGuildListGUI(player, currentPage + 1);
                }
            } else if (clicked.getType() == Material.EMERALD) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Use /guild create <id> <name> <tag> to create a guild!");
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

