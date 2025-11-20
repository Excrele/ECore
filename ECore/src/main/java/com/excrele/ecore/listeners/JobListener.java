package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.JobManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;

/**
 * Listener for job-related events.
 * Tracks player actions and rewards them based on their job.
 */
public class JobListener implements Listener {
    private final Ecore plugin;

    public JobListener(Ecore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        JobManager.PlayerJobData data = plugin.getJobManager().getPlayerJobData(player.getUniqueId());
        if (data.getCurrentJob() == null) return;

        JobManager.JobType job = plugin.getJobManager().getJobType(data.getCurrentJob());
        if (job == null) return;

        // Check for miner job
        if (job.getId().equals("miner")) {
            JobManager.JobAction action = job.getActions().get("mine");
            if (action != null) {
                plugin.getJobManager().processJobAction(player, "mine", event.getBlock().getType(), null);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        JobManager.PlayerJobData data = plugin.getJobManager().getPlayerJobData(player.getUniqueId());
        if (data.getCurrentJob() == null) return;

        JobManager.JobType job = plugin.getJobManager().getJobType(data.getCurrentJob());
        if (job == null) return;

        // Check for builder job
        if (job.getId().equals("builder")) {
            JobManager.JobAction action = job.getActions().get("build");
            if (action != null) {
                plugin.getJobManager().processJobAction(player, "build", event.getBlock().getType(), null);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();

        JobManager.PlayerJobData data = plugin.getJobManager().getPlayerJobData(player.getUniqueId());
        if (data.getCurrentJob() == null) return;

        JobManager.JobType job = plugin.getJobManager().getJobType(data.getCurrentJob());
        if (job == null) return;

        // Check for hunter job
        if (job.getId().equals("hunter")) {
            JobManager.JobAction action = job.getActions().get("kill");
            if (action != null) {
                plugin.getJobManager().processJobAction(player, "kill", null, event.getEntityType());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerHarvestBlock(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        JobManager.PlayerJobData data = plugin.getJobManager().getPlayerJobData(player.getUniqueId());
        if (data.getCurrentJob() == null) return;

        JobManager.JobType job = plugin.getJobManager().getJobType(data.getCurrentJob());
        if (job == null) return;

        // Check for farmer job
        if (job.getId().equals("farmer")) {
            JobManager.JobAction action = job.getActions().get("harvest");
            if (action != null) {
                plugin.getJobManager().processJobAction(player, "harvest", event.getHarvestedBlock().getType(), null);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player)) return;
        Player player = (Player) event.getBreeder();

        JobManager.PlayerJobData data = plugin.getJobManager().getPlayerJobData(player.getUniqueId());
        if (data.getCurrentJob() == null) return;

        JobManager.JobType job = plugin.getJobManager().getJobType(data.getCurrentJob());
        if (job == null) return;

        // Check for farmer job
        if (job.getId().equals("farmer")) {
            JobManager.JobAction action = job.getActions().get("breed");
            if (action != null) {
                plugin.getJobManager().processJobAction(player, "breed", null, event.getEntityType());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH && 
            event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) return;

        JobManager.PlayerJobData data = plugin.getJobManager().getPlayerJobData(player.getUniqueId());
        if (data.getCurrentJob() == null) return;

        JobManager.JobType job = plugin.getJobManager().getJobType(data.getCurrentJob());
        if (job == null) return;

        // Check for fisher job
        if (job.getId().equals("fisher")) {
            JobManager.JobAction action = job.getActions().get("fish");
            if (action != null) {
                // Fishing doesn't have material/entity, so pass null
                plugin.getJobManager().processJobAction(player, "fish", null, null);
            }
        }
    }
}

