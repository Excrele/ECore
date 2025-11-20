package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages server information and performance metrics including:
 * - TPS (Ticks Per Second) monitoring
 * - Memory usage tracking
 * - Server uptime calculation
 * - Performance statistics
 * 
 * @author Excrele
 * @version 1.0
 */
public class ServerInfoManager {
    private final Ecore plugin;
    private final List<Long> tickTimes;
    private final int maxSamples = 100; // Track last 100 ticks for TPS calculation
    private long lastTickTime;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public ServerInfoManager(Ecore plugin) {
        this.plugin = plugin;
        this.tickTimes = new ArrayList<>();
        this.lastTickTime = System.currentTimeMillis();
        
        // Start TPS monitoring task
        startTPSMonitoring();
    }

    private void startTPSMonitoring() {
        // Run every tick to monitor TPS
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            long tickTime = currentTime - lastTickTime;
            lastTickTime = currentTime;
            
            tickTimes.add(tickTime);
            if (tickTimes.size() > maxSamples) {
                tickTimes.remove(0);
            }
        }, 0L, 1L);
    }

    /**
     * Calculates the current TPS (Ticks Per Second) based on recent tick times.
     * 
     * @return Current TPS value (should be around 20.0 for a healthy server)
     */
    public double getTPS() {
        if (tickTimes.isEmpty()) {
            return 20.0; // Default TPS
        }
        
        long totalTime = 0;
        for (Long tickTime : tickTimes) {
            totalTime += tickTime;
        }
        
        if (totalTime == 0) {
            return 20.0;
        }
        
        double averageTickTime = (double) totalTime / tickTimes.size();
        double tps = 1000.0 / averageTickTime;
        
        // Cap TPS at 20 (Minecraft's max)
        return Math.min(20.0, tps);
    }

    /**
     * Gets a formatted TPS string with color coding based on performance.
     * 
     * @return Formatted TPS string with color codes (Green: ≥19, Yellow: ≥15, Orange: ≥10, Red: <10)
     */
    public String getTPSFormatted() {
        double tps = getTPS();
        String color;
        if (tps >= 19.0) {
            color = "§a"; // Green - Excellent
        } else if (tps >= 15.0) {
            color = "§e"; // Yellow - Good
        } else if (tps >= 10.0) {
            color = "§6"; // Orange - Fair
        } else {
            color = "§c"; // Red - Poor
        }
        return color + df.format(tps);
    }

    /**
     * Gets the server uptime formatted as a human-readable string.
     * 
     * @return Formatted uptime string (e.g., "2d 5h 30m 15s")
     */
    public String getUptime() {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        seconds %= 60;
        minutes %= 60;
        hours %= 24;
        
        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    /**
     * Gets current memory usage information from the JVM.
     * 
     * @return MemoryInfo object containing max, used, and free memory
     */
    public MemoryInfo getMemoryInfo() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long freeMemory = maxMemory - usedMemory;
        
        return new MemoryInfo(maxMemory, usedMemory, freeMemory);
    }

    /**
     * Gets a formatted memory usage string with color coding.
     * 
     * @return Formatted memory string showing used/max MB and percentage with color codes
     */
    public String getMemoryFormatted() {
        MemoryInfo memInfo = getMemoryInfo();
        double usedMB = memInfo.getUsedMemory() / (1024.0 * 1024.0);
        double maxMB = memInfo.getMaxMemory() / (1024.0 * 1024.0);
        double percent = (memInfo.getUsedMemory() * 100.0) / memInfo.getMaxMemory();
        
        String color;
        if (percent < 50) {
            color = "§a"; // Green
        } else if (percent < 75) {
            color = "§e"; // Yellow
        } else if (percent < 90) {
            color = "§6"; // Orange
        } else {
            color = "§c"; // Red
        }
        
        return color + df.format(usedMB) + "MB / " + df.format(maxMB) + "MB (" + df.format(percent) + "%)";
    }

    /**
     * Get total number of chunks loaded
     */
    public int getTotalChunks() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += world.getLoadedChunks().length;
        }
        return total;
    }

    /**
     * Get total number of entities
     */
    public int getTotalEntities() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += world.getEntities().size();
        }
        return total;
    }

    /**
     * Get server version
     */
    public String getServerVersion() {
        return Bukkit.getVersion();
    }

    /**
     * Get Bukkit version
     */
    public String getBukkitVersion() {
        return Bukkit.getBukkitVersion();
    }

    /**
     * Get Java version
     */
    public String getJavaVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Get operating system information
     */
    public String getOSInfo() {
        return System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")";
    }

    /**
     * Get number of online players
     */
    public int getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().size();
    }

    /**
     * Get maximum players
     */
    public int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }

    /**
     * Get list of world names
     */
    public List<String> getWorldNames() {
        List<String> worlds = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            worlds.add(world.getName());
        }
        return worlds;
    }

    /**
     * Represents memory usage information.
     */
    public static class MemoryInfo {
        private final long maxMemory;
        private final long usedMemory;
        private final long freeMemory;

        public MemoryInfo(long maxMemory, long usedMemory, long freeMemory) {
            this.maxMemory = maxMemory;
            this.usedMemory = usedMemory;
            this.freeMemory = freeMemory;
        }

        public long getMaxMemory() {
            return maxMemory;
        }

        public long getUsedMemory() {
            return usedMemory;
        }

        public long getFreeMemory() {
            return freeMemory;
        }
    }
}

