package com.excrele.ecore.listeners;

import com.excrele.ecore.Ecore;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Listener for the "Combat Back to the Roots" feature.
 * Removes attack cooldowns for melee weapons and tools, allowing for smoother combat
 * similar to older Minecraft versions and Bedrock edition.
 * 
 * Note: Bows and crossbows still have their wind-up time as intended.
 */
public class CombatBackToRootsListener implements Listener {
    private final Ecore plugin;
    private BukkitRunnable cooldownResetTask;
    
    // Materials that should have cooldowns removed (melee weapons and tools)
    private static final Set<Material> MELEE_WEAPONS_AND_TOOLS = new HashSet<>(Arrays.asList(
        // Swords
        Material.WOODEN_SWORD,
        Material.STONE_SWORD,
        Material.IRON_SWORD,
        Material.GOLDEN_SWORD,
        Material.DIAMOND_SWORD,
        Material.NETHERITE_SWORD,
        
        // Axes
        Material.WOODEN_AXE,
        Material.STONE_AXE,
        Material.IRON_AXE,
        Material.GOLDEN_AXE,
        Material.DIAMOND_AXE,
        Material.NETHERITE_AXE,
        
        // Shovels
        Material.WOODEN_SHOVEL,
        Material.STONE_SHOVEL,
        Material.IRON_SHOVEL,
        Material.GOLDEN_SHOVEL,
        Material.DIAMOND_SHOVEL,
        Material.NETHERITE_SHOVEL,
        
        // Pickaxes
        Material.WOODEN_PICKAXE,
        Material.STONE_PICKAXE,
        Material.IRON_PICKAXE,
        Material.GOLDEN_PICKAXE,
        Material.DIAMOND_PICKAXE,
        Material.NETHERITE_PICKAXE,
        
        // Hoes
        Material.WOODEN_HOE,
        Material.STONE_HOE,
        Material.IRON_HOE,
        Material.GOLDEN_HOE,
        Material.DIAMOND_HOE,
        Material.NETHERITE_HOE,
        
        // Trident (melee weapon)
        Material.TRIDENT
    ));
    
    public CombatBackToRootsListener(Ecore plugin) {
        this.plugin = plugin;
        startCooldownResetTask();
    }
    
    /**
     * Starts the repeating task that resets attack cooldowns for all players.
     * Runs every tick to ensure cooldowns are always at 0.
     */
    private void startCooldownResetTask() {
        // Cancel existing task if any
        if (cooldownResetTask != null) {
            cooldownResetTask.cancel();
        }
        
        cooldownResetTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Check if feature is enabled
                if (!plugin.getConfigManager().getConfig().getBoolean("combat-back-to-roots.enabled", false)) {
                    return;
                }
                
                // Reset cooldowns for all online players
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    
                    if (item != null && item.getType() != Material.AIR) {
                        Material material = item.getType();
                        
                        // Only reset cooldown for melee weapons and tools
                        if (MELEE_WEAPONS_AND_TOOLS.contains(material)) {
                            // Set cooldown to 0 (no cooldown)
                            player.setCooldown(material, 0);
                        }
                    }
                }
            }
        };
        
        // Run every tick (20 times per second)
        cooldownResetTask.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Stops the cooldown reset task.
     * Called when the listener is disabled or the plugin is disabled.
     */
    public void stop() {
        if (cooldownResetTask != null) {
            cooldownResetTask.cancel();
            cooldownResetTask = null;
        }
    }
}

