package com.excrele.ecore;

import com.excrele.ecore.commands.HomeCommand;
import com.excrele.ecore.commands.ReportCommand;
import com.excrele.ecore.commands.ShopEditCommand;
import com.excrele.ecore.listeners.SignListener;
import com.excrele.ecore.managers.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Ecore extends JavaPlugin {
    private ConfigManager configManager;
    private DiscordManager discordManager;
    private ShopManager shopManager;
    private ShopGUIManager shopGUIManager;
    private StaffManager staffManager;
    private GameModeManager gameModeManager;
    private ReportManager reportManager;
    private HomeManager homeManager;
    private final Map<UUID, String> pendingActions;

    public Ecore() {
        this.pendingActions = new HashMap<>();
    }

    @Override
    public void onEnable() {
        // Initialize managers
        configManager = new ConfigManager(this);
        discordManager = new DiscordManager(this);
        shopManager = new ShopManager(this);
        shopGUIManager = new ShopGUIManager(this);
        staffManager = new StaffManager(this);
        gameModeManager = new GameModeManager(this);
        reportManager = new ReportManager(this);
        homeManager = new HomeManager(this);

        // Register commands
        if (getCommand("shopedit") != null) {
            getCommand("shopedit").setExecutor(new ShopEditCommand(this));
        } else {
            getLogger().warning("Command 'shopedit' not found in plugin.yml!");
        }
        if (getCommand("report") != null) {
            getCommand("report").setExecutor(new ReportCommand(this));
        } else {
            getLogger().warning("Command 'report' not found in plugin.yml!");
        }
        if (getCommand("sethome") != null) {
            getCommand("sethome").setExecutor(new HomeCommand(this));
        } else {
            getLogger().warning("Command 'sethome' not found in plugin.yml!");
        }
        if (getCommand("home") != null) {
            getCommand("home").setExecutor(new HomeCommand(this));
        } else {
            getLogger().warning("Command 'home' not found in plugin.yml!");
        }
        if (getCommand("listhomes") != null) {
            getCommand("listhomes").setExecutor(new HomeCommand(this));
        } else {
            getLogger().warning("Command 'listhomes' not found in plugin.yml!");
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new SignListener(this), this);

        // Send server start notification
        discordManager.sendServerStartNotification();

        getLogger().info(ChatColor.GREEN + "Ecore plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Shutdown Discord bot
        discordManager.shutdownBot();

        getLogger().info(ChatColor.RED + "Ecore plugin has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public ShopGUIManager getShopGUIManager() {
        return shopGUIManager;
    }

    public StaffManager getStaffManager() {
        return staffManager;
    }

    public GameModeManager getGameModeManager() {
        return gameModeManager;
    }

    public ReportManager getReportManager() {
        return reportManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public void registerPendingAction(Player player, String action) {
        if (player != null) {
            pendingActions.put(player.getUniqueId(), action);
        }
    }

    public String getPendingAction(UUID uuid) {
        return pendingActions.get(uuid);
    }

    public void removePendingAction(UUID uuid) {
        pendingActions.remove(uuid);
    }
}