package com.excrele.ecore;

import com.excrele.ecore.commands.EcoreCommand;
import com.excrele.ecore.commands.HomeCommand;
import com.excrele.ecore.commands.ReportCommand;
import com.excrele.ecore.commands.SetHomeCommand;
import com.excrele.ecore.listeners.ChatListener;
import com.excrele.ecore.listeners.SignListener;
import com.excrele.ecore.listeners.SitListener;
import com.excrele.ecore.managers.ConfigManager;
import com.excrele.ecore.managers.DiscordManager;
import com.excrele.ecore.managers.HomeManager;
import com.excrele.ecore.managers.ReportManager;
import com.excrele.ecore.managers.StaffManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Ecore extends JavaPlugin implements Listener {
    private ConfigManager configManager;
    private HomeManager homeManager;
    private StaffManager staffManager;
    private ReportManager reportManager;
    private DiscordManager discordManager;
    private final Map<UUID, String> pendingActions;

    public Ecore() {
        this.pendingActions = new HashMap<>();
    }

    @Override
    public void onEnable() {
        // Initialize managers
        configManager = new ConfigManager(this);
        homeManager = new HomeManager(this);
        staffManager = new StaffManager(this);
        reportManager = new ReportManager(this);
        discordManager = new DiscordManager(this);

        // Save default config and discord config
        saveDefaultConfig();
        configManager.saveDefaultDiscordConfig();

        // Register commands
        getCommand("ecore").setExecutor(new EcoreCommand(this));
        getCommand("sethome").setExecutor(new SetHomeCommand(this));
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("report").setExecutor(new ReportCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new SignListener(), this);
        getServer().getPluginManager().registerEvents(new SitListener(), this);
        getServer().getPluginManager().registerEvents(this, this);

        // Load report data
        reportManager.loadReports();

        getLogger().info("Ecore plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Save report data
        reportManager.saveReports();
        // Shut down Discord bot
        discordManager.shutdownBot();
        getLogger().info("Ecore plugin disabled!");
    }

    // Handle chat inputs for home naming, punishment targets, and teleport
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!pendingActions.containsKey(uuid)) return;

        String action = pendingActions.remove(uuid);
        String message = event.getMessage().trim();
        event.setCancelled(true);

        if (action.startsWith("sethome") || action.startsWith("renamehome")) {
            String[] parts = action.split(":");
            if (parts.length == 2 && parts[0].equals("sethome")) {
                if (homeManager.setHome(player, message)) {
                    player.sendMessage(ChatColor.GREEN + "Home '" + message + "' set!");
                } else {
                    player.sendMessage(ChatColor.RED + "You have reached the maximum number of homes!");
                }
            } else if (parts.length == 3 && parts[0].equals("renamehome")) {
                String oldName = parts[1];
                Map<String, Location> homes = homeManager.getPlayerHomes(player);
                if (homes.containsKey(oldName)) {
                    Location loc = homes.remove(oldName);
                    homes.put(message, loc);
                    homeManager.saveHomes();
                    player.sendMessage(ChatColor.GREEN + "Renamed home '" + oldName + "' to '" + message + "'!");
                }
            }
        } else if (action.startsWith("punish")) {
            String[] parts = action.split(":");
            if (parts.length == 2) {
                String type = parts[1];
                switch (type) {
                    case "ban":
                        staffManager.banPlayer(player, message, "Staff action via GUI");
                        break;
                    case "kick":
                        staffManager.kickPlayer(player, message, "Staff action via GUI");
                        break;
                    case "inspect":
                        staffManager.openPlayerInventory(player, message);
                        break;
                }
            }
        } else if (action.equals("teleport")) {
            staffManager.teleportToPlayer(player, message);
        }
    }

    // Register a pending action for chat input
    public void registerPendingAction(Player player, String action) {
        pendingActions.put(player.getUniqueId(), action);
        player.sendMessage(ChatColor.YELLOW + "Please type the input in chat.");
    }

    // Getter methods for managers
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public StaffManager getStaffManager() {
        return staffManager;
    }

    public ReportManager getReportManager() {
        return reportManager;
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }
}