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

import java.util.Arrays;

public class StatisticsGUIManager implements Listener {
    private final Ecore plugin;

    public StatisticsGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openStatisticsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 54, ChatColor.DARK_BLUE + "Your Statistics");

        StatisticsManager statsManager = plugin.getStatisticsManager();
        
        // Blocks broken
        ItemStack blocksBroken = createStatItem(
            Material.DIAMOND_PICKAXE,
            "Blocks Broken",
            statsManager.getStatistic(player, "blocks-broken")
        );
        gui.setItem(10, blocksBroken);

        // Blocks placed
        ItemStack blocksPlaced = createStatItem(
            Material.GRASS_BLOCK,
            "Blocks Placed",
            statsManager.getStatistic(player, "blocks-placed")
        );
        gui.setItem(12, blocksPlaced);

        // Deaths
        ItemStack deaths = createStatItem(
            Material.SKELETON_SKULL,
            "Deaths",
            statsManager.getStatistic(player, "deaths")
        );
        gui.setItem(14, deaths);

        // Kills
        ItemStack kills = createStatItem(
            Material.DIAMOND_SWORD,
            "Kills",
            statsManager.getStatistic(player, "kills")
        );
        gui.setItem(16, kills);

        // Player joins
        ItemStack joins = createStatItem(
            Material.PLAYER_HEAD,
            "Times Joined",
            statsManager.getStatistic(player, "joins")
        );
        gui.setItem(28, joins);

        // Playtime (from PlayerInfoManager)
        long playtime = plugin.getPlayerInfoManager().getPlaytime(player);
        ItemStack playtimeItem = createStatItem(
            Material.CLOCK,
            "Playtime",
            formatPlaytime(playtime)
        );
        gui.setItem(30, playtimeItem);

        // Distance traveled
        double distance = statsManager.getStatisticDouble(player, "distance-traveled");
        ItemStack distanceItem = createStatItem(
            Material.FEATHER,
            "Distance Traveled",
            String.format("%.2f blocks", distance)
        );
        gui.setItem(19, distanceItem);

        // Items crafted
        ItemStack itemsCrafted = createStatItem(
            Material.CRAFTING_TABLE,
            "Items Crafted",
            statsManager.getStatistic(player, "items-crafted")
        );
        gui.setItem(21, itemsCrafted);

        // Experience gained
        ItemStack expGained = createStatItem(
            Material.EXPERIENCE_BOTTLE,
            "Experience Gained",
            statsManager.getStatistic(player, "experience-gained")
        );
        gui.setItem(23, expGained);

        // Damage dealt
        double damageDealt = statsManager.getStatisticDouble(player, "damage-dealt");
        ItemStack damageDealtItem = createStatItem(
            Material.IRON_SWORD,
            "Damage Dealt",
            String.format("%.2f", damageDealt)
        );
        gui.setItem(25, damageDealtItem);

        // Damage taken
        double damageTaken = statsManager.getStatisticDouble(player, "damage-taken");
        ItemStack damageTakenItem = createStatItem(
            Material.SHIELD,
            "Damage Taken",
            String.format("%.2f", damageTaken)
        );
        gui.setItem(34, damageTakenItem);

        // Money earned
        double moneyEarned = statsManager.getStatisticDouble(player, "money-earned");
        ItemStack moneyEarnedItem = createStatItem(
            Material.GOLD_INGOT,
            "Money Earned",
            String.format("%.2f", moneyEarned)
        );
        gui.setItem(37, moneyEarnedItem);

        // Money spent
        double moneySpent = statsManager.getStatisticDouble(player, "money-spent");
        ItemStack moneySpentItem = createStatItem(
            Material.EMERALD,
            "Money Spent",
            String.format("%.2f", moneySpent)
        );
        gui.setItem(39, moneySpentItem);

        // Balance (current)
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        ItemStack balanceItem = createStatItem(
            Material.GOLD_NUGGET,
            "Current Balance",
            String.format("%.2f", balance)
        );
        gui.setItem(32, balanceItem);

        // Leaderboard button
        ItemStack leaderboard = new ItemStack(Material.BOOK);
        ItemMeta leaderboardMeta = leaderboard.getItemMeta();
        leaderboardMeta.setDisplayName(ChatColor.GOLD + "View Leaderboards");
        leaderboardMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Click to view top players",
            ChatColor.GRAY + "for each statistic"
        ));
        leaderboard.setItemMeta(leaderboardMeta);
        gui.setItem(40, leaderboard);

        player.openInventory(gui);
    }

    private ItemStack createStatItem(Material material, String name, int value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + name);
        meta.setLore(Arrays.asList(
            ChatColor.YELLOW + "Value: " + ChatColor.WHITE + value
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createStatItem(Material material, String name, String value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + name);
        meta.setLore(Arrays.asList(
            ChatColor.YELLOW + "Value: " + ChatColor.WHITE + value
        ));
        item.setItemMeta(meta);
        return item;
    }

    private String formatPlaytime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return hours + "h " + minutes + "m";
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        if (title.equals(ChatColor.DARK_BLUE + "Your Statistics")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            if (clicked.getType() == Material.BOOK) {
                // Open leaderboard GUI
                openLeaderboardGUI(player);
            }
        } else if (title.startsWith(ChatColor.GOLD + "Leaderboard:")) {
            event.setCancelled(true);
        }
    }

    public void openLeaderboardGUI(Player player, String stat) {
        Inventory gui = Bukkit.createInventory(player, 54, ChatColor.GOLD + "Leaderboard: " + stat);

        // Get top players for this stat
        // This is a simplified version - you'd want to implement proper leaderboard calculation
        int slot = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) break;
            int value = plugin.getStatisticsManager().getStatistic(p, stat);
            ItemStack item = createLeaderboardItem(p, value, slot + 1);
            gui.setItem(slot++, item);
        }

        player.openInventory(gui);
    }

    public void openLeaderboardGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, ChatColor.GOLD + "Select Leaderboard");

        ItemStack blocksBroken = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = blocksBroken.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Blocks Broken");
        blocksBroken.setItemMeta(meta);
        gui.setItem(10, blocksBroken);

        ItemStack blocksPlaced = new ItemStack(Material.GRASS_BLOCK);
        meta = blocksPlaced.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Blocks Placed");
        blocksPlaced.setItemMeta(meta);
        gui.setItem(12, blocksPlaced);

        ItemStack deaths = new ItemStack(Material.SKELETON_SKULL);
        meta = deaths.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Deaths");
        deaths.setItemMeta(meta);
        gui.setItem(14, deaths);

        ItemStack kills = new ItemStack(Material.DIAMOND_SWORD);
        meta = kills.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Kills");
        kills.setItemMeta(meta);
        gui.setItem(16, kills);

        player.openInventory(gui);
    }

    private ItemStack createLeaderboardItem(Player player, int value, int rank) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "#" + rank + " " + player.getName());
        meta.setLore(Arrays.asList(
            ChatColor.YELLOW + "Value: " + ChatColor.WHITE + value
        ));
        item.setItemMeta(meta);
        return item;
    }
}

