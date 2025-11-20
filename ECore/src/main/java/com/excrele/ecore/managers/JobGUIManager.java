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

import java.util.*;

/**
 * GUI manager for jobs system.
 */
public class JobGUIManager implements Listener {
    private final Ecore plugin;

    public JobGUIManager(Ecore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openJobGUI(Player player) {
        JobManager.PlayerJobData data = plugin.getJobManager().getPlayerJobData(player.getUniqueId());
        Map<String, JobManager.JobType> jobs = plugin.getJobManager().getJobTypes();
        
        int size = 54; // 6 rows
        Inventory gui = Bukkit.createInventory(null, size, ChatColor.DARK_BLUE + "Jobs");

        // Current job info (top section)
        if (data.getCurrentJob() != null) {
            JobManager.JobType currentJob = plugin.getJobManager().getJobType(data.getCurrentJob());
            if (currentJob != null) {
                ItemStack currentJobItem = createJobItem(currentJob, data, true);
                gui.setItem(4, currentJobItem);
            }
        } else {
            ItemStack noJob = createMenuItem(Material.BARRIER, ChatColor.RED + "No Active Job", 
                    Arrays.asList(ChatColor.GRAY + "Select a job below to join!"));
            gui.setItem(4, noJob);
        }

        // Job list (middle section)
        int slot = 9; // Start from second row
        for (JobManager.JobType job : jobs.values()) {
            if (slot >= 45) break; // Don't go into last row
            
            boolean isCurrentJob = data.getCurrentJob() != null && data.getCurrentJob().equals(job.getId());
            ItemStack jobItem = createJobItem(job, isCurrentJob ? data : null, isCurrentJob);
            gui.setItem(slot++, jobItem);
        }

        // Action buttons (bottom row)
        ItemStack infoButton = createMenuItem(Material.BOOK, ChatColor.GREEN + "Job Information", 
                Arrays.asList(ChatColor.GRAY + "Click to view detailed info"));
        gui.setItem(45, infoButton);

        ItemStack leaderboardButton = createMenuItem(Material.GOLD_INGOT, ChatColor.GOLD + "Leaderboard", 
                Arrays.asList(ChatColor.GRAY + "Click to view top players"));
        gui.setItem(46, leaderboardButton);

        if (data.getCurrentJob() != null) {
            ItemStack leaveButton = createMenuItem(Material.REDSTONE_BLOCK, ChatColor.RED + "Leave Job", 
                    Arrays.asList(ChatColor.GRAY + "Click to leave your current job"));
            gui.setItem(53, leaveButton);
        }

        // Fill empty slots with glass panes
        for (int i = 0; i < size; i++) {
            if (gui.getItem(i) == null) {
                ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta meta = glass.getItemMeta();
                meta.setDisplayName(" ");
                glass.setItemMeta(meta);
                gui.setItem(i, glass);
            }
        }

        player.openInventory(gui);
    }

    private ItemStack createJobItem(JobManager.JobType job, JobManager.PlayerJobData data, boolean isCurrent) {
        ItemStack item = new ItemStack(job.getIcon());
        ItemMeta meta = item.getItemMeta();
        
        ChatColor nameColor = isCurrent ? ChatColor.GREEN : ChatColor.YELLOW;
        String prefix = isCurrent ? "✓ " : "";
        meta.setDisplayName(nameColor + prefix + job.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + job.getDescription());
        lore.add("");
        
        if (isCurrent && data != null) {
            lore.add(ChatColor.WHITE + "Level: " + ChatColor.GREEN + data.getLevel());
            double expForNext = data.getExpForNextLevel();
            double progress = (data.getExperience() / expForNext) * 100.0;
            lore.add(ChatColor.WHITE + "Experience: " + ChatColor.GREEN + 
                     String.format("%.1f", data.getExperience()) + " / " + 
                     String.format("%.1f", expForNext));
            lore.add(ChatColor.WHITE + "Progress: " + ChatColor.GREEN + 
                     String.format("%.1f", progress) + "%");
            lore.add("");
            lore.add(ChatColor.RED + "Right-click to leave");
        } else {
            lore.add(ChatColor.GREEN + "Click to join this job!");
        }
        
        // Add job actions info
        if (!job.getActions().isEmpty()) {
            lore.add("");
            lore.add(ChatColor.GOLD + "Actions:");
            for (JobManager.JobAction action : job.getActions().values()) {
                lore.add(ChatColor.YELLOW + "• " + action.getName());
            }
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createMenuItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        if (!title.equals(ChatColor.DARK_BLUE + "Jobs")) return;
        
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || 
            clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        JobManager.PlayerJobData data = plugin.getJobManager().getPlayerJobData(player.getUniqueId());

        // Handle job selection
        if (clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
            String displayName = clicked.getItemMeta().getDisplayName();
            
            // Check if it's a job item (has "Click to join" or "Right-click to leave")
            boolean isJobItem = false;
            String jobId = null;
            
            for (JobManager.JobType job : plugin.getJobManager().getJobTypes().values()) {
                if (job.getIcon() == clicked.getType() && displayName.contains(job.getName())) {
                    isJobItem = true;
                    jobId = job.getId();
                    break;
                }
            }
            
            if (isJobItem && jobId != null) {
                boolean isCurrent = data.getCurrentJob() != null && data.getCurrentJob().equals(jobId);
                
                if (isCurrent && event.isRightClick()) {
                    // Leave job
                    plugin.getJobManager().leaveJob(player);
                    player.closeInventory();
                } else if (!isCurrent) {
                    // Join job
                    plugin.getJobManager().joinJob(player, jobId);
                    player.closeInventory();
                }
                return;
            }
        }

        // Handle action buttons
        if (clicked.getType() == Material.BOOK) {
            // Job info
            player.closeInventory();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getCommand("jobs").getExecutor().onCommand(player, 
                    plugin.getCommand("jobs"), "jobs", new String[]{"info"});
            });
        } else if (clicked.getType() == Material.GOLD_INGOT) {
            // Leaderboard
            player.closeInventory();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getCommand("jobs").getExecutor().onCommand(player, 
                    plugin.getCommand("jobs"), "jobs", new String[]{"top"});
            });
        } else if (clicked.getType() == Material.REDSTONE_BLOCK) {
            // Leave job
            plugin.getJobManager().leaveJob(player);
            player.closeInventory();
        }
    }
}

