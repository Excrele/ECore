package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import com.excrele.ecore.managers.QuestManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

/**
 * Listener for quest-related events.
 * Tracks player actions and updates quest progress.
 */
public class QuestListener implements Listener {
    private final Ecore plugin;

    public QuestListener(Ecore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        
        plugin.getQuestManager().updateQuestProgress(player, QuestManager.QuestType.KILL, 
            null, event.getEntityType());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        
        plugin.getQuestManager().updateQuestProgress(player, QuestManager.QuestType.BREAK, 
            event.getBlock().getType(), null);
        plugin.getQuestManager().updateQuestProgress(player, QuestManager.QuestType.MINE, 
            event.getBlock().getType(), null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        
        plugin.getQuestManager().updateQuestProgress(player, QuestManager.QuestType.PLACE, 
            event.getBlock().getType(), null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        Material resultType = event.getRecipe().getResult().getType();
        plugin.getQuestManager().updateQuestProgress(player, QuestManager.QuestType.CRAFT, 
            resultType, null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH && 
            event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) {
            return;
        }
        
        Player player = event.getPlayer();
        plugin.getQuestManager().updateQuestProgress(player, QuestManager.QuestType.FISH, 
            null, null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player)) return;
        Player player = (Player) event.getBreeder();
        
        plugin.getQuestManager().updateQuestProgress(player, QuestManager.QuestType.BREED, 
            null, event.getEntityType());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Material foodType = event.getItem().getType();
        
        // Only track food items
        if (foodType.isEdible()) {
            plugin.getQuestManager().updateQuestProgress(player, QuestManager.QuestType.EAT, 
                foodType, null);
        }
    }

    // Note: COLLECT quests are typically handled when items are picked up
    // This would require checking inventory changes, which is more complex
    // For now, COLLECT quests can be manually completed or tracked through other means
}

