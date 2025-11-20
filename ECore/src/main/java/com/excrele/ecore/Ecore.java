package com.excrele.ecore;

import com.excrele.ecore.commands.*;
import com.excrele.ecore.listeners.ChatListener;
import com.excrele.ecore.listeners.PlayerJoinListener;
import com.excrele.ecore.listeners.PlayerBedEnterListener;
import com.excrele.ecore.listeners.SignListener;
import com.excrele.ecore.listeners.SitListener;
import com.excrele.ecore.managers.*;
import com.excrele.ecore.managers.AccountLinkManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Main plugin class for Ecore - Excrele's Core plugin.
 * 
 * <p>Ecore is a comprehensive Spigot plugin for Minecraft 1.21+ that provides:
 * <ul>
 *   <li>Staff moderation tools and GUI</li>
 *   <li>Home management system with bed spawn integration</li>
 *   <li>Player reporting system</li>
 *   <li>Self-contained economy system</li>
 *   <li>Admin and Player shop systems</li>
 *   <li>Discord integration for server status and staff logs</li>
 *   <li>Teleportation and warp systems</li>
 *   <li>Kit management</li>
 *   <li>Mail system</li>
 *   <li>Statistics tracking</li>
 *   <li>Achievement system</li>
 *   <li>Auction house</li>
 *   <li>Bank system</li>
 *   <li>And much more...</li>
 * </ul>
 * 
 * @author Excrele
 * @version 1.0
 */
public class Ecore extends JavaPlugin {
    private ConfigManager configManager;
    private DiscordManager discordManager;
    private ShopManager shopManager;
    private ShopGUIManager shopGUIManager;
    private StaffManager staffManager;
    private GameModeManager gameModeManager;
    private ReportManager reportManager;
    private HomeManager homeManager;
    private HomeGUIManager homeGUIManager;
    private EconomyManager economyManager;
    private TeleportManager teleportManager;
    private WarpManager warpManager;
    private SpawnManager spawnManager;
    private KitManager kitManager;
    private MailManager mailManager;
    private PlayerInfoManager playerInfoManager;
    private TimeWeatherManager timeWeatherManager;
    private AFKManager afkManager;
    private JailManager jailManager;
    private StatisticsManager statisticsManager;
    private StatisticsGUIManager statisticsGUIManager;
    private MailGUIManager mailGUIManager;
    private KitGUIManager kitGUIManager;
    private WarpGUIManager warpGUIManager;
    private ChatManager chatManager;
    private BankManager bankManager;
    private AchievementManager achievementManager;
    private AuctionHouseManager auctionHouseManager;
    private AuctionHouseGUIManager auctionHouseGUIManager;
    private ServerInfoManager serverInfoManager;
    private WorldEditManager worldEditManager;
    private com.excrele.ecore.managers.RegionManager regionManager;
    private ChunkManager chunkManager;
    private com.excrele.ecore.managers.StaffModeManager staffModeManager;
    private com.excrele.ecore.managers.WorldManager worldManager;
    private com.excrele.ecore.managers.PortalManager portalManager;
    private com.excrele.ecore.managers.BlockLogManager blockLogManager;
    private com.excrele.ecore.managers.InventoryLogManager inventoryLogManager;
    private com.excrele.ecore.managers.BlockLogGUIManager blockLogGUIManager;
    private com.excrele.ecore.managers.PerformanceManager performanceManager;
    private com.excrele.ecore.integrations.VaultIntegration vaultIntegration;
    private com.excrele.ecore.integrations.WorldGuardIntegration worldGuardIntegration;
    private com.excrele.ecore.integrations.LuckPermsIntegration luckPermsIntegration;
    private AccountLinkManager accountLinkManager;
    private final Map<UUID, String> pendingActions;

    /**
     * Creates a new Ecore plugin instance.
     */
    public Ecore() {
        this.pendingActions = new HashMap<>();
    }

    /**
     * Called when the plugin is enabled.
     * Initializes all managers, registers commands and listeners.
     */
    @Override
    public void onEnable() {
        // Initialize managers
        configManager = new ConfigManager(this);
        economyManager = new EconomyManager(this);
        discordManager = new DiscordManager(this);
        shopManager = new ShopManager(this);
        shopGUIManager = new ShopGUIManager(this);
        staffManager = new StaffManager(this);
        gameModeManager = new GameModeManager(this);
        reportManager = new ReportManager(this);
        homeManager = new HomeManager(this);
        homeGUIManager = new HomeGUIManager(this);
        teleportManager = new TeleportManager(this);
        warpManager = new WarpManager(this);
        spawnManager = new SpawnManager(this);
        kitManager = new KitManager(this);
        mailManager = new MailManager(this);
        playerInfoManager = new PlayerInfoManager(this);
        timeWeatherManager = new TimeWeatherManager(this);
        afkManager = new AFKManager(this);
        jailManager = new JailManager(this);
        statisticsManager = new StatisticsManager(this);
        statisticsGUIManager = new StatisticsGUIManager(this);
        mailGUIManager = new MailGUIManager(this);
        kitGUIManager = new KitGUIManager(this);
        warpGUIManager = new WarpGUIManager(this);
        chatManager = new ChatManager(this);
        bankManager = new BankManager(this);
        achievementManager = new AchievementManager(this);
        auctionHouseManager = new AuctionHouseManager(this);
        auctionHouseGUIManager = new AuctionHouseGUIManager(this);
        serverInfoManager = new ServerInfoManager(this);
        worldEditManager = new WorldEditManager(this);
        regionManager = new com.excrele.ecore.managers.RegionManager(this);
        chunkManager = new ChunkManager(this);
        staffModeManager = new com.excrele.ecore.managers.StaffModeManager(this);
        worldManager = new com.excrele.ecore.managers.WorldManager(this);
        portalManager = new com.excrele.ecore.managers.PortalManager(this);
        accountLinkManager = new AccountLinkManager(this);
        blockLogManager = new com.excrele.ecore.managers.BlockLogManager(this);
        inventoryLogManager = new com.excrele.ecore.managers.InventoryLogManager(this);
        blockLogGUIManager = new com.excrele.ecore.managers.BlockLogGUIManager(this);
        performanceManager = new com.excrele.ecore.managers.PerformanceManager(this);
        
        // Initialize integrations
        vaultIntegration = new com.excrele.ecore.integrations.VaultIntegration(this);
        worldGuardIntegration = new com.excrele.ecore.integrations.WorldGuardIntegration(this);
        luckPermsIntegration = new com.excrele.ecore.integrations.LuckPermsIntegration(this);

        // Register commands
        registerCommand("ecore", new EcoreCommand(this));
        registerCommand("shopedit", new ShopEditCommand(this));
        registerCommand("report", new ReportCommand(this));
        registerCommand("sethome", new HomeCommand(this));
        registerCommand("home", new HomeCommand(this));
        registerCommand("listhomes", new HomeCommand(this));
        registerCommand("homeshare", new HomeCommand(this));
        registerCommand("sharehome", new HomeCommand(this));
        registerCommand("homeunshare", new HomeCommand(this));
        registerCommand("unsharehome", new HomeCommand(this));
        registerCommand("homecategory", new HomeCommand(this));
        registerCommand("sethomecategory", new HomeCommand(this));
        registerCommand("homeicon", new HomeCommand(this));
        registerCommand("sethomeicon", new HomeCommand(this));
        registerCommand("homedescription", new HomeCommand(this));
        registerCommand("sethomedescription", new HomeCommand(this));
        registerCommand("gm", new GameModeCommand(this));
        registerCommand("tp", new TeleportCommand(this));
        registerCommand("teleport", new TeleportCommand(this));
        registerCommand("tpa", new TeleportCommand(this));
        registerCommand("tpahere", new TeleportCommand(this));
        registerCommand("tpaccept", new TeleportCommand(this));
        registerCommand("tpdeny", new TeleportCommand(this));
        registerCommand("back", new TeleportCommand(this));
        registerCommand("warp", new WarpCommand(this));
        registerCommand("setwarp", new WarpCommand(this));
        registerCommand("delwarp", new WarpCommand(this));
        registerCommand("deletewarp", new WarpCommand(this));
        registerCommand("warps", new WarpCommand(this));
        registerCommand("spawn", new SpawnCommand(this));
        registerCommand("setspawn", new SpawnCommand(this));
        registerCommand("kit", new KitCommand(this));
        registerCommand("mail", new MailCommand(this));
        registerCommand("balance", new EconomyCommand(this));
        registerCommand("bal", new EconomyCommand(this));
        registerCommand("money", new EconomyCommand(this));
        registerCommand("pay", new EconomyCommand(this));
        registerCommand("economy", new EconomyCommand(this));
        registerCommand("eco", new EconomyCommand(this));
        registerCommand("baltop", new EconomyCommand(this));
        registerCommand("balancetop", new EconomyCommand(this));
        registerCommand("whois", new PlayerInfoCommand(this));
        registerCommand("seen", new PlayerInfoCommand(this));
        registerCommand("list", new PlayerInfoCommand(this));
        registerCommand("who", new PlayerInfoCommand(this));
        registerCommand("ping", new PlayerInfoCommand(this));
        registerCommand("time", new TimeWeatherCommand(this));
        registerCommand("day", new TimeWeatherCommand(this));
        registerCommand("night", new TimeWeatherCommand(this));
        registerCommand("weather", new TimeWeatherCommand(this));
        registerCommand("sun", new TimeWeatherCommand(this));
        registerCommand("clear", new TimeWeatherCommand(this));
        registerCommand("rain", new TimeWeatherCommand(this));
        registerCommand("storm", new TimeWeatherCommand(this));
        registerCommand("thunder", new TimeWeatherCommand(this));
        registerCommand("afk", new AFKCommand(this));
        registerCommand("jail", new JailCommand(this));
        registerCommand("unjail", new JailCommand(this));
        registerCommand("setjail", new JailCommand(this));
        registerCommand("jailinfo", new JailCommand(this));
        registerCommand("msg", new ChatCommand(this));
        registerCommand("message", new ChatCommand(this));
        registerCommand("tell", new ChatCommand(this));
        registerCommand("whisper", new ChatCommand(this));
        registerCommand("reply", new ChatCommand(this));
        registerCommand("r", new ChatCommand(this));
        registerCommand("chat", new ChatCommand(this));
        registerCommand("sc", new ChatCommand(this));
        registerCommand("staffchat", new ChatCommand(this));
        registerCommand("ac", new ChatCommand(this));
        registerCommand("adminchat", new ChatCommand(this));
        registerCommand("mute", new StaffCommand(this));
        registerCommand("unmute", new StaffCommand(this));
        registerCommand("freeze", new StaffCommand(this));
        registerCommand("unfreeze", new StaffCommand(this));
        registerCommand("commandspy", new StaffCommand(this));
        registerCommand("socialspy", new StaffCommand(this));
        registerCommand("give", new StaffCommand(this));
        registerCommand("enchant", new StaffCommand(this));
        registerCommand("repair", new StaffCommand(this));
        registerCommand("chatslow", new StaffCommand(this));
        registerCommand("top", new TeleportCommand(this));
        registerCommand("jump", new TeleportCommand(this));
        registerCommand("rtp", new TeleportCommand(this));
        registerCommand("tpbiome", new TeleportCommand(this));
        registerCommand("teleportbiome", new TeleportCommand(this));
        registerCommand("tpstructure", new TeleportCommand(this));
        registerCommand("teleportstructure", new TeleportCommand(this));
        registerCommand("bank", new BankCommand(this));
        registerCommand("stats", new StatisticsCommand(this));
        registerCommand("statistics", new StatisticsCommand(this));
        registerCommand("leaderboard", new StatisticsCommand(this));
        registerCommand("lb", new StatisticsCommand(this));
        registerCommand("statsreset", new StatisticsCommand(this));
        registerCommand("resetstats", new StatisticsCommand(this));
        registerCommand("achievements", new AchievementCommand(this));
        registerCommand("achievement", new AchievementCommand(this));
        registerCommand("ah", new AuctionHouseCommand(this));
        registerCommand("auctionhouse", new AuctionHouseCommand(this));
        registerCommand("auction", new AuctionHouseCommand(this));
        registerCommand("near", new PlayerInfoCommand(this));
        registerCommand("serverinfo", new ServerInfoCommand(this));
        registerCommand("wand", new WorldEditCommand(this));
        registerCommand("pos1", new WorldEditCommand(this));
        registerCommand("pos2", new WorldEditCommand(this));
        registerCommand("set", new WorldEditCommand(this));
        registerCommand("replace", new WorldEditCommand(this));
        registerCommand("clear", new WorldEditCommand(this));
        registerCommand("walls", new WorldEditCommand(this));
        registerCommand("hollow", new WorldEditCommand(this));
        registerCommand("copy", new WorldEditCommand(this));
        registerCommand("paste", new WorldEditCommand(this));
        registerCommand("cut", new WorldEditCommand(this));
        registerCommand("undo", new WorldEditCommand(this));
        registerCommand("redo", new WorldEditCommand(this));
        registerCommand("schematic", new WorldEditCommand(this));
        registerCommand("sphere", new WorldEditCommand(this));
        registerCommand("cylinder", new WorldEditCommand(this));
        registerCommand("sel", new WorldEditCommand(this));
        registerCommand("selection", new WorldEditCommand(this));
        registerCommand("region", new com.excrele.ecore.commands.RegionCommand(this));
        registerCommand("chunks", new ChunksCommand(this));
        registerCommand("staffmode", new com.excrele.ecore.commands.StaffModeCommand(this));
        registerCommand("sm", new com.excrele.ecore.commands.StaffModeCommand(this));
        registerCommand("mv", new com.excrele.ecore.commands.WorldCommand(this));
        registerCommand("multiverse", new com.excrele.ecore.commands.WorldCommand(this));
        registerCommand("portal", new com.excrele.ecore.commands.PortalCommand(this));
        registerCommand("blocklog", new com.excrele.ecore.commands.BlockLogCommand(this));
        registerCommand("bl", new com.excrele.ecore.commands.BlockLogCommand(this));
        registerCommand("co", new com.excrele.ecore.commands.BlockLogCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new SignListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new SitListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerBedEnterListener(this), this);
        getServer().getPluginManager().registerEvents(new com.excrele.ecore.listeners.PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new com.excrele.ecore.listeners.CommandSpyListener(this), this);
        getServer().getPluginManager().registerEvents(new com.excrele.ecore.listeners.WorldEditListener(this), this);
        getServer().getPluginManager().registerEvents(new com.excrele.ecore.listeners.StaffModeListener(this), this);
        getServer().getPluginManager().registerEvents(new com.excrele.ecore.listeners.PortalListener(this), this);
        
        // Register block logging listener if enabled
        if (configManager.getConfig().getBoolean("block-logging.enabled", true)) {
            getServer().getPluginManager().registerEvents(new com.excrele.ecore.listeners.BlockLogListener(this), this);
        }
        
        // Register region listener if regions are enabled
        if (configManager.getConfig().getBoolean("regions.enabled", true)) {
            getServer().getPluginManager().registerEvents(new com.excrele.ecore.listeners.RegionListener(this), this);
        }

        // Register PlaceholderAPI expansion if available
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new com.excrele.ecore.placeholders.EcorePlaceholders(this).register();
            getLogger().info("PlaceholderAPI expansion registered!");
        }

        // Schedule expired shop cleanup (every 6 hours)
        getServer().getScheduler().runTaskTimer(this, () -> {
            shopManager.checkExpiredShops();
        }, 0L, 432000L); // 6 hours = 432000 ticks

        // Send server start notification
        discordManager.sendServerStartNotification();

        getLogger().info(ChatColor.GREEN + "Ecore plugin has been enabled!");
    }

    /**
     * Called when the plugin is disabled.
     * Performs cleanup operations like shutting down Discord bot and saving data.
     */
    @Override
    public void onDisable() {
        if (bankManager != null) {
            bankManager.shutdown();
        }
        if (blockLogManager != null) {
            blockLogManager.shutdown();
        }
        if (performanceManager != null) {
            performanceManager.shutdown();
        }
        // Shutdown Discord bot
        discordManager.shutdownBot();

        getLogger().info(ChatColor.RED + "Ecore plugin has been disabled!");
    }

    /**
     * Registers a command with its executor.
     * 
     * @param name The command name as defined in plugin.yml
     * @param executor The CommandExecutor to handle the command
     */
    private void registerCommand(String name, CommandExecutor executor) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
        } else {
            getLogger().warning("Command '" + name + "' not found in plugin.yml!");
        }
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

    public HomeGUIManager getHomeGUIManager() {
        return homeGUIManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public MailManager getMailManager() {
        return mailManager;
    }

    public PlayerInfoManager getPlayerInfoManager() {
        return playerInfoManager;
    }

    public TimeWeatherManager getTimeWeatherManager() {
        return timeWeatherManager;
    }

    public AFKManager getAFKManager() {
        return afkManager;
    }

    public JailManager getJailManager() {
        return jailManager;
    }

    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public BankManager getBankManager() {
        return bankManager;
    }

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    public AuctionHouseManager getAuctionHouseManager() {
        return auctionHouseManager;
    }

    public AuctionHouseGUIManager getAuctionHouseGUIManager() {
        return auctionHouseGUIManager;
    }

    public ServerInfoManager getServerInfoManager() {
        return serverInfoManager;
    }

    public WorldEditManager getWorldEditManager() {
        return worldEditManager;
    }

    public com.excrele.ecore.managers.RegionManager getRegionManager() {
        return regionManager;
    }

    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public com.excrele.ecore.managers.StaffModeManager getStaffModeManager() {
        return staffModeManager;
    }

    public com.excrele.ecore.integrations.VaultIntegration getVaultIntegration() {
        return vaultIntegration;
    }

    public com.excrele.ecore.integrations.WorldGuardIntegration getWorldGuardIntegration() {
        return worldGuardIntegration;
    }

    public com.excrele.ecore.integrations.LuckPermsIntegration getLuckPermsIntegration() {
        return luckPermsIntegration;
    }

    public AccountLinkManager getAccountLinkManager() {
        return accountLinkManager;
    }

    public com.excrele.ecore.managers.WorldManager getWorldManager() {
        return worldManager;
    }

    public com.excrele.ecore.managers.PortalManager getPortalManager() {
        return portalManager;
    }

    public com.excrele.ecore.managers.BlockLogManager getBlockLogManager() {
        return blockLogManager;
    }

    public com.excrele.ecore.managers.InventoryLogManager getInventoryLogManager() {
        return inventoryLogManager;
    }

    public com.excrele.ecore.managers.BlockLogGUIManager getBlockLogGUIManager() {
        return blockLogGUIManager;
    }

    public com.excrele.ecore.managers.PerformanceManager getPerformanceManager() {
        return performanceManager;
    }

    public StatisticsGUIManager getStatisticsGUIManager() {
        return statisticsGUIManager;
    }

    public MailGUIManager getMailGUIManager() {
        return mailGUIManager;
    }

    public KitGUIManager getKitGUIManager() {
        return kitGUIManager;
    }

    public WarpGUIManager getWarpGUIManager() {
        return warpGUIManager;
    }

    /**
     * Registers a pending action for a player (used for chat-based input).
     * 
     * @param player The player to register the action for
     * @param action The action identifier (e.g., "shopgui:admin:quantity")
     */
    public void registerPendingAction(Player player, String action) {
        if (player != null) {
            pendingActions.put(player.getUniqueId(), action);
        }
    }

    /**
     * Gets a pending action for a player.
     * 
     * @param uuid The player's UUID
     * @return The pending action string, or null if none exists
     */
    public String getPendingAction(UUID uuid) {
        return pendingActions.get(uuid);
    }

    /**
     * Removes a pending action for a player.
     * 
     * @param uuid The player's UUID
     */
    public void removePendingAction(UUID uuid) {
        pendingActions.remove(uuid);
    }
}