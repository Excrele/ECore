# ECore Public API Documentation

## Overview

This document provides comprehensive documentation for developers who want to integrate with ECore or extend its functionality. ECore exposes a rich API through manager classes that can be accessed by other plugins.

**Version:** 1.0.1  
**Minecraft Version:** 1.21+  
**API Stability:** Stable (methods may be added but existing methods will not be removed)

---

## Getting Started

### Adding ECore as a Dependency

#### Maven
```xml
<repositories>
    <repository>
        <id>spigot-repo</id>
        <url>https://hub.spigotmc.org/nexus/content/repositories/snapshots/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.spigotmc</groupId>
        <artifactId>spigot-api</artifactId>
        <version>1.21-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

#### Gradle
```gradle
repositories {
    maven { url = 'https://hub.spigotmc.org/nexus/content/repositories/snapshots/' }
}

dependencies {
    compileOnly 'org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT'
}
```

### Getting the Plugin Instance

```java
import com.excrele.ecore.Ecore;
import org.bukkit.plugin.java.JavaPlugin;

public class YourPlugin extends JavaPlugin {
    private Ecore ecore;
    
    @Override
    public void onEnable() {
        // Get ECore plugin instance
        JavaPlugin ecorePlugin = (JavaPlugin) getServer().getPluginManager().getPlugin("ECore");
        if (ecorePlugin != null && ecorePlugin.isEnabled()) {
            ecore = (Ecore) ecorePlugin;
            getLogger().info("ECore found! API ready to use.");
        } else {
            getLogger().warning("ECore not found! Some features may not work.");
        }
    }
    
    public Ecore getEcore() {
        return ecore;
    }
}
```

---

## Available Managers

ECore provides access to all its managers through getter methods in the main `Ecore` class. All managers are initialized when the plugin enables, so they are safe to use after `onEnable()`.

### Core Managers

#### EconomyManager
Manages the self-contained economy system.

```java
Ecore ecore = // ... get plugin instance
EconomyManager economy = ecore.getEconomyManager();

// Get player balance
double balance = economy.getBalance(player.getUniqueId());

// Add funds
economy.addBalance(player.getUniqueId(), 100.0);

// Remove funds
boolean success = economy.removeBalance(player.getUniqueId(), 50.0);

// Transfer funds
boolean transferred = economy.transferBalance(from.getUniqueId(), to.getUniqueId(), 25.0);

// Check if player has enough
if (economy.getBalance(player.getUniqueId()) >= 100.0) {
    // Player has enough money
}

// Format money
String formatted = economy.format(1000.0); // Returns formatted string
```

**Key Methods:**
- `getBalance(UUID uuid)` - Get player balance
- `setBalance(UUID uuid, double amount)` - Set player balance
- `addBalance(UUID uuid, double amount)` - Add funds
- `removeBalance(UUID uuid, double amount)` - Remove funds (returns false if insufficient)
- `transferBalance(UUID from, UUID to, double amount)` - Transfer funds
- `format(double amount)` - Format money as string
- `getTransactionHistory(UUID uuid, int limit)` - Get transaction history

---

#### HomeManager
Manages player homes and teleportation.

```java
HomeManager homeManager = ecore.getHomeManager();

// Set a home
boolean success = homeManager.setHome(player, "spawn", location);

// Get home location
Location home = homeManager.getHome(player, "spawn");

// Teleport to home
boolean teleported = homeManager.teleportToHome(player, "spawn");

// Delete home
boolean deleted = homeManager.deleteHome(player, "spawn");

// Get all homes for a player
List<String> homes = homeManager.getHomes(player);

// Check if home exists
if (homeManager.hasHome(player, "spawn")) {
    // Home exists
}
```

**Key Methods:**
- `setHome(Player player, String homeName, Location location)` - Create/update home
- `getHome(Player player, String homeName)` - Get home location
- `deleteHome(Player player, String homeName)` - Delete home
- `teleportToHome(Player player, String homeName)` - Teleport to home
- `getHomes(Player player)` - Get list of home names
- `hasHome(Player player, String homeName)` - Check if home exists
- `getHomeCount(Player player)` - Get number of homes

---

#### WarpManager
Manages server warps.

```java
WarpManager warpManager = ecore.getWarpManager();

// Create warp
boolean created = warpManager.createWarp("spawn", location);

// Get warp location
Location warp = warpManager.getWarp("spawn");

// Teleport to warp
boolean teleported = warpManager.teleportToWarp(player, "spawn");

// Delete warp
boolean deleted = warpManager.deleteWarp("spawn");

// Get all warps
Set<String> warps = warpManager.getWarps();
```

**Key Methods:**
- `createWarp(String name, Location location)` - Create warp
- `getWarp(String name)` - Get warp location
- `deleteWarp(String name)` - Delete warp
- `teleportToWarp(Player player, String name)` - Teleport to warp
- `getWarps()` - Get all warp names
- `warpExists(String name)` - Check if warp exists

---

#### ShopManager
Manages player and admin shops.

```java
ShopManager shopManager = ecore.getShopManager();

// Create a shop
boolean created = shopManager.createShop(player, "My Shop", location, ShopType.PLAYER);

// Get shop by ID
Shop shop = shopManager.getShop(shopId);

// Get player's shops
List<Shop> shops = shopManager.getPlayerShops(player.getUniqueId());

// Search shops
List<Shop> results = shopManager.searchShops("diamond");

// Get shops by category
List<Shop> categoryShops = shopManager.getShopsByCategory("weapons");
```

**Key Methods:**
- `createShop(Player owner, String name, Location location, ShopType type)` - Create shop
- `getShop(String shopId)` - Get shop by ID
- `getPlayerShops(UUID owner)` - Get player's shops
- `searchShops(String query)` - Search shops
- `getShopsByCategory(String category)` - Get shops by category
- `deleteShop(String shopId)` - Delete shop

---

#### FriendManager
Manages friend lists and friend requests.

```java
FriendManager friendManager = ecore.getFriendManager();

// Send friend request
boolean sent = friendManager.sendFriendRequest(player, target);

// Accept friend request
boolean accepted = friendManager.acceptFriendRequest(player, requester);

// Remove friend
boolean removed = friendManager.removeFriend(player, friend);

// Get friends list
Set<UUID> friends = friendManager.getFriends(player.getUniqueId());

// Check if friends
if (friendManager.areFriends(player.getUniqueId(), other.getUniqueId())) {
    // They are friends
}
```

**Key Methods:**
- `sendFriendRequest(Player sender, Player target)` - Send friend request
- `acceptFriendRequest(Player target, Player requester)` - Accept request
- `denyFriendRequest(Player target, Player requester)` - Deny request
- `removeFriend(Player player, Player friend)` - Remove friend
- `getFriends(UUID uuid)` - Get friends list
- `areFriends(UUID uuid1, UUID uuid2)` - Check if friends
- `getPendingRequests(UUID uuid)` - Get pending requests

---

#### PartyManager
Manages party/team system.

```java
PartyManager partyManager = ecore.getPartyManager();

// Create party
Party party = partyManager.createParty(leader);

// Invite player
boolean invited = partyManager.invitePlayer(leader, target);

// Accept invite
boolean accepted = partyManager.acceptInvite(target, leader);

// Leave party
boolean left = partyManager.leaveParty(player);

// Get player's party
Party party = partyManager.getPlayerParty(player.getUniqueId());
```

**Key Methods:**
- `createParty(Player leader)` - Create party
- `invitePlayer(Player leader, Player target)` - Invite to party
- `acceptInvite(Player target, Player leader)` - Accept invite
- `leaveParty(Player player)` - Leave party
- `kickPlayer(Player leader, Player target)` - Kick from party
- `getPlayerParty(UUID uuid)` - Get player's party
- `isInParty(UUID uuid)` - Check if in party

---

#### JobManager
Manages the jobs system.

```java
JobManager jobManager = ecore.getJobManager();

// Join a job
boolean joined = jobManager.joinJob(player, "miner");

// Leave job
boolean left = jobManager.leaveJob(player);

// Get player job data
JobManager.PlayerJobData data = jobManager.getPlayerJobData(player.getUniqueId());

// Get job type
JobManager.JobType jobType = jobManager.getJobType("miner");

// Add experience
jobManager.addExperience(player, "miner", 10.0);
```

**Key Methods:**
- `joinJob(Player player, String jobId)` - Join a job
- `leaveJob(Player player)` - Leave current job
- `getPlayerJobData(UUID uuid)` - Get player job data
- `getJobType(String jobId)` - Get job type configuration
- `addExperience(Player player, String jobId, double exp)` - Add experience
- `getJobTypes()` - Get all job types

---

#### QuestManager
Manages the quests system.

```java
QuestManager questManager = ecore.getQuestManager();

// Start a quest
boolean started = questManager.startQuest(player, "quest-id");

// Complete quest
boolean completed = questManager.completeQuest(player, "quest-id");

// Get active quests
List<QuestManager.Quest> active = questManager.getActiveQuests(player);

// Get quest progress
QuestManager.QuestProgress progress = questManager.getQuestProgress(player.getUniqueId(), "quest-id");

// Get all quests
Map<String, QuestManager.Quest> allQuests = questManager.getQuests();
```

**Key Methods:**
- `startQuest(Player player, String questId)` - Start a quest
- `completeQuest(Player player, String questId)` - Complete quest
- `getActiveQuests(Player player)` - Get active quests
- `getQuestProgress(UUID uuid, String questId)` - Get quest progress
- `getQuest(String questId)` - Get quest configuration
- `getQuests()` - Get all quests

---

#### RegionManager
Manages region protection system.

```java
RegionManager regionManager = ecore.getRegionManager();

// Create region
Region region = regionManager.createRegion("spawn", world, min, max, "spawn", player.getUniqueId());

// Get region at location
Region region = regionManager.getRegionAt(location);

// Check if player can perform action
boolean canBuild = regionManager.canPerformAction(player, location, RegionFlag.BUILD);

// Add owner to region
region.addOwner(player.getUniqueId());

// Set region flag
region.setFlag(RegionFlag.PVP, false);
```

**Key Methods:**
- `createRegion(String name, World world, Location min, Location max, String type, UUID creator)` - Create region
- `getRegion(World world, String name)` - Get region by name
- `getRegionAt(Location location)` - Get region at location
- `canPerformAction(Player player, Location location, RegionFlag flag)` - Check permission
- `deleteRegion(World world, String name)` - Delete region
- `visualizeRegion(Player player, Region region)` - Show region borders

---

#### BlockLogManager
Manages block logging and rollback system.

```java
BlockLogManager blockLog = ecore.getBlockLogManager();

// Log block break
blockLog.logBlockBreak(player, block);

// Log block place
blockLog.logBlockPlace(player, block);

// Get block logs
List<BlockLogDatabase.BlockLogEntry> logs = blockLog.getBlockLogs(location, timeRange);

// Rollback player actions
blockLog.rollbackPlayer(player.getUniqueId(), timeRange);
```

**Key Methods:**
- `logBlockBreak(Player player, Block block)` - Log block break
- `logBlockPlace(Player player, Block block)` - Log block place
- `getBlockLogs(Location location, long timeRange)` - Get logs for location
- `getPlayerBlockLogs(UUID uuid, long timeRange, int limit)` - Get player logs
- `rollbackPlayer(UUID uuid, long timeRange)` - Rollback player actions

---

#### PerformanceManager
Manages server performance optimization.

```java
PerformanceManager perf = ecore.getPerformanceManager();

// Perform cleanup
PerformanceManager.CleanupResult result = perf.performCleanup();

// Get performance stats
PerformanceManager.PerformanceStats stats = perf.getPerformanceStats();

// Merge items
int merged = perf.mergeItems(5.0); // radius

// Optimize chunks
int unloaded = perf.optimizeChunks();
```

**Key Methods:**
- `performCleanup()` - Perform entity cleanup
- `getPerformanceStats()` - Get performance statistics
- `mergeItems(double radius)` - Merge nearby items
- `optimizeChunks()` - Unload empty chunks

---

#### TitleManager
Manages titles, subtitles, and action bars.

```java
TitleManager titleManager = ecore.getTitleManager();

// Send title
titleManager.sendTitle(player, "Title", "Subtitle", 10, 70, 20);

// Send action bar
titleManager.sendActionBar(player, "Action bar message");

// Broadcast title
titleManager.broadcastTitle("Title", "Subtitle");

// Clear title
titleManager.clearTitle(player);
```

**Key Methods:**
- `sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut)` - Send title
- `sendActionBar(Player player, String message)` - Send action bar
- `broadcastTitle(String title, String subtitle)` - Broadcast to all
- `broadcastActionBar(String message)` - Broadcast action bar
- `clearTitle(Player player)` - Clear title

---

#### VaultManager
Manages player vaults.

```java
VaultManager vaultManager = ecore.getVaultManager();

// Get vault items
ItemStack[] items = vaultManager.getVaultItems(player, 1);

// Set vault items
vaultManager.setVaultItems(player, 1, items);

// Create vault
boolean created = vaultManager.createVault(player, 1);

// Get player vaults
List<Integer> vaults = vaultManager.getPlayerVaults(player);
```

**Key Methods:**
- `getVaultItems(Player player, int vaultNumber)` - Get vault contents
- `setVaultItems(Player player, int vaultNumber, ItemStack[] items)` - Set vault contents
- `createVault(Player player, int vaultNumber)` - Create vault
- `getPlayerVaults(Player player)` - Get player's vaults
- `getMaxVaults(Player player)` - Get max vaults for player

---

#### CommandControlManager
Manages command cooldowns and costs.

```java
CommandControlManager cmdControl = ecore.getCommandControlManager();

// Check cooldown
if (cmdControl.isOnCooldown(player.getUniqueId(), "home")) {
    long remaining = cmdControl.getRemainingCooldown(player.getUniqueId(), "home");
    // Show cooldown message
}

// Check cost
if (!cmdControl.canAfford(player, "warp")) {
    // Player can't afford
}

// Charge player
cmdControl.chargePlayer(player, "warp");
```

**Key Methods:**
- `isOnCooldown(UUID uuid, String command)` - Check if on cooldown
- `getRemainingCooldown(UUID uuid, String command)` - Get remaining cooldown
- `setCooldown(UUID uuid, String command, int seconds)` - Set cooldown
- `canAfford(Player player, String command)` - Check if can afford
- `chargePlayer(Player player, String command)` - Charge player

---

#### RecipeManager
Manages custom recipes.

```java
RecipeManager recipeManager = ecore.getRecipeManager();

// Create shaped recipe
boolean created = recipeManager.createShapedRecipe("custom_sword", "Custom Sword", 
    result, shape, ingredients, "permission.node");

// Create shapeless recipe
boolean created = recipeManager.createShapelessRecipe("custom_item", "Custom Item",
    result, ingredients, "permission.node");

// Remove recipe
boolean removed = recipeManager.removeRecipe("custom_sword");

// Get custom recipes
Map<String, Recipe> recipes = recipeManager.getCustomRecipes();
```

**Key Methods:**
- `createShapedRecipe(String id, String name, ItemStack result, List<String> shape, Map<Character, Material> ingredients, String permission)` - Create shaped recipe
- `createShapelessRecipe(String id, String name, ItemStack result, List<Material> ingredients, String permission)` - Create shapeless recipe
- `removeRecipe(String id)` - Remove recipe
- `getCustomRecipes()` - Get all custom recipes
- `reload()` - Reload recipes from config

---

#### EnchantmentManager
Manages custom enchantments.

```java
EnchantmentManager enchantManager = ecore.getEnchantmentManager();

// Apply enchantment
boolean applied = enchantManager.applyEnchantment(item, "lifesteal", 3);

// Remove enchantment
boolean removed = enchantManager.removeEnchantment(item, "lifesteal");

// Get enchantment level
int level = enchantManager.getEnchantmentLevel(item, "lifesteal");

// Get all enchantments
Map<String, EnchantmentManager.CustomEnchantment> enchants = enchantManager.getEnchantments();
```

**Key Methods:**
- `applyEnchantment(ItemStack item, String enchantId, int level)` - Apply enchantment
- `removeEnchantment(ItemStack item, String enchantId)` - Remove enchantment
- `getEnchantmentLevel(ItemStack item, String enchantId)` - Get level
- `getEnchantments()` - Get all enchantments
- `getEnchantment(String enchantId)` - Get enchantment config

---

#### ChatChannelManager
Manages chat channels.

```java
ChatChannelManager channelManager = ecore.getChatChannelManager();

// Join channel
boolean joined = channelManager.joinChannel(player, "global", true);

// Leave channel
boolean left = channelManager.leaveChannel(player, "global", true);

// Send message to channel
channelManager.sendMessage(player, "Hello!", "global");

// Get player's channel
String channel = channelManager.getPlayerChannel(player.getUniqueId());
```

**Key Methods:**
- `joinChannel(Player player, String channelId, boolean sendMessage)` - Join channel
- `leaveChannel(Player player, String channelId, boolean sendMessage)` - Leave channel
- `sendMessage(Player sender, String message, String channelId)` - Send message
- `getPlayerChannel(UUID uuid)` - Get player's channel
- `getChannel(String channelId)` - Get channel config

---

#### ScoreboardManager & TabListManager
Manages custom scoreboards and tab lists.

```java
ScoreboardManager scoreboard = ecore.getScoreboardManager();
TabListManager tabList = ecore.getTabListManager();

// Setup scoreboard for player
scoreboard.setupScoreboard(player);

// Update scoreboard
scoreboard.updateScoreboard(player);

// Setup tab list
tabList.setupTabList(player);
```

**Key Methods:**
- `setupScoreboard(Player player)` - Setup scoreboard
- `updateScoreboard(Player player)` - Update scoreboard
- `removeScoreboard(Player player)` - Remove scoreboard
- `setupTabList(Player player)` - Setup tab list
- `updateTabList()` - Update all tab lists

---

#### BackupManager
Manages automatic backups.

```java
BackupManager backup = ecore.getBackupManager();

// Create backup
BackupManager.BackupResult result = backup.createBackup(true); // true = manual

// List backups
List<File> backups = backup.listBackups();

// Restore backup
BackupManager.RestoreResult result = backup.restoreBackup(backupFile);
```

**Key Methods:**
- `createBackup(boolean manual)` - Create backup
- `listBackups()` - List all backups
- `restoreBackup(File backup)` - Restore backup
- `isEnabled()` - Check if enabled

---

#### MobCustomizationManager
Manages custom mob drops, health, and damage.

```java
MobCustomizationManager mobCustom = ecore.getMobCustomizationManager();

// Get mob config
MobCustomizationManager.MobConfig config = mobCustom.getMobConfig("ZOMBIE");

// Reload configs
mobCustom.reload();
```

**Key Methods:**
- `getMobConfig(String mobType)` - Get mob configuration
- `reload()` - Reload configurations

---

## Integration Examples

### Example 1: Economy Integration

```java
public class MyPlugin extends JavaPlugin {
    private Ecore ecore;
    
    @Override
    public void onEnable() {
        ecore = (Ecore) getServer().getPluginManager().getPlugin("ECore");
        if (ecore == null) {
            getLogger().severe("ECore not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }
    
    public void giveReward(Player player, double amount) {
        EconomyManager economy = ecore.getEconomyManager();
        economy.addBalance(player.getUniqueId(), amount);
        player.sendMessage("You received " + economy.format(amount) + "!");
    }
}
```

### Example 2: Home System Integration

```java
public void teleportPlayerToHome(Player player, String homeName) {
    HomeManager homeManager = ecore.getHomeManager();
    
    if (!homeManager.hasHome(player, homeName)) {
        player.sendMessage("Home not found!");
        return;
    }
    
    Location home = homeManager.getHome(player, homeName);
    if (home != null) {
        player.teleport(home);
        player.sendMessage("Teleported to " + homeName + "!");
    }
}
```

### Example 3: Quest Integration

```java
public void onPlayerKillMob(Player player, Entity mob) {
    QuestManager questManager = ecore.getQuestManager();
    
    // Check active quests
    List<QuestManager.Quest> activeQuests = questManager.getActiveQuests(player);
    
    for (QuestManager.Quest quest : activeQuests) {
        if (quest.getType() == QuestManager.QuestType.KILL) {
            if (quest.getTargetEntity() == mob.getType()) {
                // Update quest progress
                QuestManager.QuestProgress progress = questManager.getQuestProgress(
                    player.getUniqueId(), quest.getId());
                progress.addProgress(1);
                
                if (progress.getProgress() >= quest.getRequiredAmount()) {
                    questManager.completeQuest(player, quest.getId());
                }
            }
        }
    }
}
```

---

## Event System

ECore uses Bukkit's standard event system. You can listen to ECore-related events by implementing `Listener` and registering it.

### Example: Listen to Economy Transactions

```java
@EventHandler
public void onEconomyTransaction(EconomyTransactionEvent event) {
    // Handle economy transaction
    UUID player = event.getPlayer();
    double amount = event.getAmount();
    String type = event.getType(); // "DEPOSIT", "WITHDRAW", "TRANSFER"
}
```

---

## Permissions

ECore uses a permission-based system. All permissions follow the pattern `ecore.<feature>.<action>`. Check the main documentation for a complete list of permissions.

---

## Thread Safety

⚠️ **Important:** Most ECore managers are **NOT thread-safe**. Always access managers from the main server thread. If you need to access from async threads, use `Bukkit.getScheduler().runTask()` to schedule a synchronous task.

```java
// ❌ WRONG - Accessing from async thread
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    double balance = ecore.getEconomyManager().getBalance(uuid); // UNSAFE!
});

// ✅ CORRECT - Schedule synchronous task
Bukkit.getScheduler().runTask(plugin, () -> {
    double balance = ecore.getEconomyManager().getBalance(uuid); // SAFE!
});
```

---

## Best Practices

1. **Always check if ECore is enabled** before accessing managers
2. **Handle null values** - Managers may return null if data doesn't exist
3. **Use UUIDs** instead of player names for player identification
4. **Respect permissions** - Check permissions before performing actions
5. **Handle errors gracefully** - Methods may return false or null on failure
6. **Use async operations carefully** - Most managers are not thread-safe

---

## Support

For questions, issues, or feature requests:
- Check the main documentation: `DOCUMENTATION.md`
- Review implementation files in the `ECore/` directory
- Check TODO.md for planned features

---

## Version History

- **1.0.1** - Current version with full API access
- **1.0.0** - Initial release

---

**Last Updated:** Current  
**API Version:** 1.0.1  
**Status:** Stable

