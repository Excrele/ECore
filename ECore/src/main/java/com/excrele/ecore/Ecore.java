package com.excrele.ecore;

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
    private Map<UUID, String> pendingActions;

    @Override
    public void onEnable() {
        pendingActions = new HashMap<>();

        // Initialize managers
        configManager = new ConfigManager(this);
        discordManager = new DiscordManager(this);
        shopManager = new ShopManager(this);
        shopGUIManager = new ShopGUIManager(this);
        staffManager = new StaffManager(this);
        gameModeManager = new GameModeManager(this);

        // Register commands
        getCommand("shopedit").setExecutor(new ShopEditCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new SignListener(this), this);

        // Send server start notification
        discordManager.sendChatNotification("Server has started!");

        getLogger().info(ChatColor.GREEN + "Ecore plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Shutdown Discord bot
        discordManager.shutdown();

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

    public void registerPendingAction(Player player, String action) {
        pendingActions.put(player.getUniqueId(), action);
    }

    public String getPendingAction(UUID uuid) {
        return pendingActions.get(uuid);
    }

    public void removePendingAction(UUID uuid) {
        pendingActions.remove(uuid);
    }
}