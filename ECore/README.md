# Excrele's Core (Ecore)

A comprehensive Spigot plugin for Minecraft 1.21+, providing staff moderation tools, a home management system, a player reporting system, a self-contained economy, shop systems, multi-world management, portal system, Discord integration, and extensive gameplay enhancements.

---

## ✨ Features

### 🏠 Home System
- **Multiple Homes**: Set and manage multiple homes with custom names
- **Bed Spawn Integration**: Automatically link beds to home system
- **Home Sharing**: Share homes with other players
- **Home Categories**: Organize homes into categories
- **Home Icons & Descriptions**: Customize home appearance with icons and descriptions
- **Teleport Costs & Cooldowns**: Configurable teleportation restrictions
- **Warmup System**: Prevent teleportation abuse with movement checks
- **Home GUI**: Browse and manage homes through an intuitive GUI

### 💰 Economy System
- **Self-Contained**: No Vault dependency required (optional Vault support available)
- **Starting Balance**: Configurable starting balance for new players
- **Balance Management**: Query, deposit, withdraw, and transfer funds
- **Bank System**: Multiple bank accounts with interest rates
- **Economy Statistics**: Track total money, average balance, transactions
- **Leaderboards**: View top players by balance (`/baltop`)
- **API**: EcoreEconomy API for other plugins

### 🏦 Bank System
- **Multiple Accounts**: Create multiple bank accounts per player
- **Interest Rates**: Configurable interest rates per account
- **Deposits & Withdrawals**: Easy money management
- **Transfers**: Transfer funds between accounts
- **Account Management**: Create, delete, and list accounts

### 🛒 Shop System
- **Admin Shops**: Server-controlled shops with unlimited stock
- **Player Shops**: Player-owned shops with chest storage
- **Shop Categories**: Organize shops into categories
- **Shop Search**: Search shops by item name
- **Shop Favorites**: Bookmark favorite shops
- **Shop Statistics**: Track views, sales, and revenue
- **Shop Limits**: Configurable maximum shops per player
- **Auto-Expiration**: Remove inactive shops after configurable days
- **Shop GUI**: Browse and manage shops through GUI

### 👮 Staff Management
- **Staff GUI**: Easy-to-use GUI for staff actions
- **Player Moderation**: Ban, kick, mute, freeze players
- **Vanish System**: Complete invisibility without potion effects
- **Command Spy**: Monitor player commands
- **Social Spy**: Monitor private messages
- **Inventory Inspection**: View and manage player inventories
- **Report Management**: View and resolve player reports
- **Item Management**: Give items, enchant items, repair items
- **Chat Management**: Control chat slow mode and chat state

### 💬 Chat System
- **Chat Slow Mode**: Limit message frequency (staff only)
- **Chat Cooldowns**: Configurable cooldown between messages
- **Mute System**: Temporary and permanent mutes
- **Private Messaging**: `/msg` and `/reply` commands
- **Staff Chat**: Separate chat channel for staff (`/sc`)
- **Admin Chat**: Separate chat channel for admins (`/ac`)
- **Color Support**: Use color codes in chat (with permission)
- **Chat Control**: Enable/disable chat, clear chat

### 📊 Statistics & Achievements
- **Player Statistics**: Track kills, deaths, distance traveled, items crafted, blocks broken, and more
- **Achievement System**: Unlockable achievements with rewards
- **Statistics GUI**: View your statistics in a GUI
- **Leaderboards**: Compare statistics with other players
- **Stat Reset**: Staff can reset player statistics

### 📧 Mail System
- **Player Mail**: Send and receive mail between players
- **Mail GUI**: Easy-to-use mail interface
- **Mail Notifications**: Get notified of new mail on join
- **Bulk Mail**: Staff can send mail to all players
- **Mail Management**: Read, clear, and manage mail

### 🎁 Kit System
- **Kit Management**: Create and manage kits
- **Kit GUI**: Browse and claim kits
- **Kit Cooldowns**: Prevent kit abuse
- **Kit Permissions**: Per-kit permission support
- **Kit Creation**: Create kits from inventory

### 🚀 Teleportation System
- **Teleport Requests**: Request to teleport to players (`/tpa`, `/tpahere`)
- **Random Teleport**: Teleport to random locations (`/rtp`)
- **Biome Teleport**: Teleport to specific biomes (`/tpbiome`)
- **Structure Teleport**: Teleport to structures (`/tpstructure`)
- **Back Command**: Return to previous location or death location (`/back`)
- **Top Command**: Teleport to highest block (`/top`)
- **Jump Command**: Teleport forward (`/jump`)
- **Coordinate Teleport**: Teleport to specific coordinates
- **Teleport History**: Multiple back locations supported

### 📍 Warp & Spawn System
- **Warps**: Create and manage server warps
- **Spawn System**: Set server spawn point per world
- **Warp GUI**: Browse warps in a GUI
- **Warp Management**: Create, delete, and list warps

### 🌍 Multi-World System
- **World Creation**: Create new worlds with custom types, environments, and seeds
- **World Management**: Load, unload, and delete worlds dynamically
- **World Properties**: Configure spawn locations, difficulty, PVP, and more
- **World Teleportation**: Seamlessly teleport players between worlds
- **Safe Spawn**: Automatic safe location finding when teleporting
- **World Information**: View detailed information about any world
- **Auto-Load Configuration**: Configure which worlds load automatically
- **World Persistence**: All world data saved to `worlds.yml`

### 🌀 Portal System
- **Custom Portals**: Create portals from any block selection
- **Seamless Teleportation**: Players automatically teleport when entering portal blocks
- **Multi-World Support**: Portals can bridge players between different worlds
- **Custom Materials**: Use any block material for portals (NETHER_PORTAL, END_PORTAL, etc.)
- **Permission-Based Access**: Control who can use specific portals
- **Custom Messages & Sounds**: Configure portal teleportation messages and sounds
- **Portal Management**: Create, delete, list, and modify portals easily
- **Portal Persistence**: All portal data saved to `portals.yml`

### 🎮 Server Management
- **Server Info**: `/serverinfo` command with detailed metrics
- **TPS Monitoring**: Real-time TPS tracking
- **Memory Usage**: Monitor server memory
- **Performance Metrics**: Track server performance
- **World Information**: View loaded chunks, entities, world list
- **Chunk Pregeneration**: Pregenerate chunks in a radius from spawn to improve server performance

### 🔗 Discord Integration
- **Chat Bridging**: Two-way chat between Minecraft and Discord with advanced features
  - Message filtering and word filtering
  - Rate limiting to prevent spam
  - @player mention support
  - Rich formatting support
- **Discord Slash Commands**: Full server management from Discord
  - `/serverinfo` - View server status, TPS, memory, uptime
  - `/online` - List all online players
  - `/playerinfo <player>` - Get detailed player information
  - `/report <player> <reason>` - Report players from Discord
  - `/link <code>` - Link Discord account to Minecraft
  - `/unlink` - Unlink Discord account
  - `/staff <action> <player> [reason]` - Execute staff actions (ban, kick, mute, etc.)
  - `/execute <command>` - Execute console commands (admin only)
- **Rich Embeds**: Beautiful formatted messages for all notifications
  - Color-coded by log type
  - Detailed information fields
  - Timestamps and footers
- **Staff Logs**: Comprehensive logging system
  - Separate channels for punishments and staff actions
  - Automatic logging of all staff actions
  - Economy transaction logs
  - Achievement notifications
  - Command execution logs
- **Account Linking**: Link Discord accounts to Minecraft players
  - Verification code system
  - Show linked Discord names in embeds
  - Enhanced player identification
- **Player Notifications**: Enhanced join/leave notifications
  - Optional player statistics in notifications
  - Account link information
  - Rich embeds with detailed info
- **Server Status**: Enhanced server status notifications
  - Detailed server information embeds
  - Live status channel updates (optional)
  - TPS, memory, and player count monitoring
- **Scheduled Reports**: Automatic server statistics reports
  - Daily or weekly summaries
  - Server performance metrics
  - Player activity statistics
- **Message Queue System**: Reliable message delivery
  - Queues messages during Discord outages
  - Automatic retry with exponential backoff
  - No message loss during downtime
- **Role-Based Permissions**: Secure command access
  - Configurable role requirements
  - Separate permissions for staff and admin commands
- **Webhook Support**: Optional webhook integration for better rate limit handling

### 🛠️ WorldEdit Integration
- **Selection Tools**: Wand, pos1, pos2 for area selection
- **Block Operations**: Set, replace, clear, walls, hollow
- **Clipboard Operations**: Copy, paste, cut selections
- **History**: Undo and redo operations
- **Schematics**: Save, load, list, and delete schematics
- **Brush Tools**: Create spheres and cylinders
- **Selection Info**: View selection details
- **Portal Integration**: Use selections to create portals

### 🗺️ Region System
- **Region Creation**: Create protected regions from selections
- **Region Types**: Multiple region types with different properties
- **Region Flags**: Configure region behavior (PvP, build, interact, etc.)
- **Owners & Members**: Manage region access
- **Region Info**: View detailed region information
- **Region Management**: List, delete, and reload regions

### 📝 Additional Features
- **AFK System**: Automatic AFK detection and manual toggle
- **Jail System**: Jail players with configurable locations and durations
- **Time & Weather**: Control server time and weather
- **Player Info**: Detailed player information commands (`/whois`, `/seen`, `/ping`, `/near`)
- **Auction House**: Buy and sell items via auctions
- **GameMode GUI**: Easy gamemode switching through GUI
- **Report System**: Submit and manage player reports
- **Sit System**: Sit on stairs and slabs
- **Sign Colors**: Use color codes on signs
- **Enhanced Nickname System**: Custom nicknames with colors and formatting support

### 🔍 Block Logging System (CoreProtect-like)
- **Comprehensive Logging**: Logs block breaks, places, container access, and inventory changes
- **Rollback System**: Rollback player actions or specific areas to previous states
- **Inventory Protection**: Track and rollback player inventories to snapshots
- **Inspector Tool**: Right-click blocks to view their history
- **Database Support**: SQLite (default) or MySQL for efficient log storage
- **GUI System**: Easy-to-use interfaces for browsing logs and performing rollbacks
- **Automatic Purging**: Configurable log retention with automatic cleanup

### ⚡ Performance Optimization (ClearLagg-like)
- **Automatic Cleanup**: Removes excessive entities (items, mobs, projectiles)
- **TPS-Based Cleanup**: Automatically triggers when TPS drops below threshold
- **Item Stacking**: Merges nearby items of the same type
- **Chunk Optimization**: Unloads empty chunks to reduce memory usage
- **Performance Statistics**: Detailed metrics and entity breakdown
- **Scheduled Maintenance**: Automatic cleanup on configurable intervals
- **Integrated with ServerInfo**: Uses existing TPS monitoring

### 👥 Friends & Party System
- **Friend Lists**: Add, remove, and manage friends
- **Friend Requests**: Send, accept, and deny friend requests
- **Friend GUI**: Browse friends and pending requests
- **Party System**: Create and manage parties/teams
- **Party Chat**: Private chat channel for party members
- **Party Management**: Invite, kick, and leave parties
- **Party GUI**: Easy party management interface
- **Online Status**: See which friends/party members are online

### 📊 Custom Scoreboard & Tab List
- **Custom Scoreboards**: Fully customizable scoreboard with placeholders
- **Placeholder Support**: ECore and PlaceholderAPI placeholders
- **Per-World Scoreboards**: Different scoreboards for different worlds (optional)
- **Per-Group Scoreboards**: Different scoreboards for permission groups (optional)
- **Custom Tab List**: Customizable header and footer
- **Multiline Support**: Support for multiline headers and footers
- **Separate Config Files**: `scoreboard.yml` and `tablist.yml` for easy editing
- **Auto-Updates**: Configurable update intervals

### 💼 Jobs System
- **Multiple Job Types**: Miner, Farmer, Hunter, Builder, Fisher, and more
- **Job Levels & Experience**: Level up through job-specific actions
- **Job Rewards**: Earn money and items from completing job actions
- **Job GUI**: Easy-to-use interface for browsing and joining jobs
- **Job Statistics**: Track your progress and earnings
- **Job Leaderboards**: Compare with other players
- **Job Progression**: Exponential leveling system with configurable rewards

### 🎯 Quests System
- **100+ Predefined Quests**: Extensive quest library included
- **Quest Types**: Kill, Collect, Craft, Break, Place, Fish, Breed, Travel, Eat, Enchant, Trade, Mine, Harvest, and Custom quests
- **Quest Chains**: Quests with prerequisites and chains
- **Quest Rewards**: Money, items, and experience rewards
- **Quest GUI**: Browse quests by category with filtering
- **Daily/Weekly Quests**: Automatically resetting quests
- **Quest Progress Tracking**: Real-time progress updates
- **Quest Completion Notifications**: Get notified when completing quests

### 💬 Chat Channels System
- **Multiple Channels**: Global, Local, Trade, Help, Staff channels
- **Channel Switching**: Easy channel management
- **Channel Permissions**: Per-channel permission support
- **Channel Prefixes**: Color-coded channel prefixes
- **Range-Based Local Chat**: Configurable range for local channels
- **Channel Muting**: Mute specific channels per player
- **Auto-Join**: Auto-join to default channel on login
- **Admin Channel Management**: Create and delete channels

### 🗄️ Player Vaults System
- **Multiple Vaults**: Permission-based vault limits (1-10 vaults)
- **Vault GUI**: Easy vault selection and management
- **Vault Naming**: Custom names for each vault
- **Trust System**: Share vaults with friends
- **54-Slot Storage**: Each vault has 54 slots (6 rows)
- **Vault Management**: Create, rename, and manage vaults easily

### 📺 Title, Subtitle & Action Bar System
- **Titles & Subtitles**: Send custom titles to players
- **Action Bar Messages**: Send messages to action bar
- **Broadcast Support**: Broadcast to all players
- **Customizable Timings**: Configure fade in, stay, and fade out times
- **Color Support**: Full color code support

### ⏱️ Command Cooldowns & Costs
- **Per-Command Cooldowns**: Set cooldowns for any command (in seconds)
- **Per-Command Economy Costs**: Charge players for using commands
- **Bypass Permissions**: Configurable bypass permissions per command
- **Cooldown Messages**: User-friendly cooldown notifications
- **Cost Notifications**: Inform players of charges
- **Configuration**: Easy setup in `config.yml`

### 🍳 Custom Recipes System
- **Shaped Recipes**: Create shaped crafting recipes
- **Shapeless Recipes**: Create shapeless crafting recipes
- **Recipe Management**: Create, remove, and list recipes
- **Recipe Permissions**: Per-recipe permission support
- **Recipe Storage**: Recipes saved in `recipes.yml`
- **Recipe Reload**: Hot-reload recipes without restart

### ✨ Custom Enchantments System
- **90+ Unique Enchantments**: Extensive enchantment library
  - **Weapons**: 18 enchantments (Lifesteal, Venom, Wither, Lightning, etc.)
  - **Armor**: 18 enchantments (Regeneration, Absorption, Thorns Plus, etc.)
  - **Tools**: 18 enchantments (Auto Smelt, Vein Miner, Tree Feller, etc.)
  - **Bows/Crossbows**: 18 enchantments (Explosive Arrows, Homing, Teleport Arrows, etc.)
  - **Fishing Rods**: 18 enchantments (Treasure Hunter, Double Catch, Fish Finder, etc.)
- **Scalable by Level**: All enchantments scale with level (1-5 or 1-10)
- **Item-Specific**: Enchantments can only be applied to applicable items
- **Event Handlers**: Automatic effect application on attack, defend, and fishing
- **Documentation**: Comprehensive guide in `enchantments.yml` for creating new enchantments

### 🔌 Plugin Integrations
- **PlaceholderAPI**: Extensive placeholder support (see PlaceholderAPI section)
- **Vault**: Optional economy integration
- **WorldGuard**: Region protection compatibility
- **LuckPerms**: Permission and group integration

---

## 📦 Installation

1. Place `Ecore.jar` in your server's `plugins` folder
2. Restart the server to generate configuration files
3. Configure settings in `plugins/Ecore/config.yml` and other config files
4. For Discord integration, see the [Discord Setup](#-discord-setup) section below

---

## ⚙️ Configuration

### config.yml
```yaml
# Chat System
chat:
  slow-mode: 0  # Chat slow mode in seconds (0 = disabled)
  cooldown: 0  # General chat cooldown in seconds

# Home System
home:
  max-homes: 5  # Maximum number of homes per player
  teleport-cost: 0.0  # Cost to teleport to a home
  cooldown: 0  # Cooldown between home teleports
  warmup: 0  # Warmup delay before teleport
  bed-spawn-enabled: true  # Enable bed spawn integration
  auto-set-home-on-bed: true  # Auto-set home when sleeping

# Economy
economy:
  starting-balance: 100.0  # Starting balance for new players

# Shops
shops:
  max-shops-per-player: 10  # Maximum player shops
  expiration-days: 30  # Remove inactive shops after X days
  enable-categories: true  # Enable shop categories
  enable-favorites: true  # Enable shop favorites
  enable-statistics: true  # Enable shop statistics

# Server Info
server-info:
  enabled: true  # Enable /serverinfo command
  tps-monitoring: true  # Enable TPS monitoring

# Regions
regions:
  enabled: true  # Enable region system

# Player Vaults
vaults:
  max-vaults: 1  # Default maximum vaults per player
  vault-size: 54  # Vault size in slots (54 = 6 rows)

# Command Cooldowns & Costs
command-control:
  enabled: true  # Enable command control system
  commands:
    home:
      cooldown: 60  # Cooldown in seconds (0 to disable)
      cost: 10.0  # Economy cost (0.0 to disable)
      bypass-permission: ecore.home.bypass
    warp:
      cooldown: 30
      cost: 5.0
      bypass-permission: ecore.warp.bypass
```

### scoreboard.yml
```yaml
enabled: true
title: "&6&lYour Server"
update-interval: 20
lines:
  - "&7&m-------------------"
  - "&eBalance: &a%ecore_balance%"
  - "&eHomes: &a%ecore_homes%/%ecore_max_homes%"
  - "&eKills: &a%ecore_kills%"
  - "&7&m-------------------"
```

### tablist.yml
```yaml
enabled: true
update-interval: 20
header: "&6&lWelcome to Server!"
footer: "&7Visit our website!"
```

### discordconf.yml
```yaml
discord:
  enabled: false  # Enable Discord bot
  bot-token: "INSERT_TOKEN_HERE"  # Bot token from Discord Developer Portal
  channel-id: "INSERT_CHANNEL_ID"  # Chat bridge channel
  punishment-channel-id: "INSERT_PUNISHMENT_CHANNEL_ID"  # Punishment logs channel
  staff-logs-channel-id: "INSERT_STAFF_LOGS_CHANNEL_ID"  # Staff action logs (optional)
  use-rich-embeds: true  # Use rich embeds for messages
  use-webhooks: false  # Use webhooks instead of bot messages
  notify-player-join: false  # Send join notifications
  notify-player-leave: false  # Send leave notifications
  join-leave-show-stats: true  # Show player stats in join/leave notifications
  notify-achievements: false  # Send achievement notifications
  log-economy-transactions: false  # Log economy transactions
  economy-transaction-threshold: 1000.0  # Minimum amount to log
  
  # Chat filtering and moderation
  chat-filter:
    enabled: false
    filtered-words: []  # List of filtered words
    delete-filtered: true
    notify-staff: true
    rate-limit-cooldown: 1000  # Rate limit in milliseconds
  
  # Advanced chat features
  chat-features:
    mention-support: true  # Enable @player mentions
  
  # Role-based permissions
  permissions:
    staff: []  # Role IDs/names for staff commands
    admin: []  # Role IDs/names for admin commands
  
  # Server status channel
  status-channel:
    enabled: false
    channel-id: "INSERT_STATUS_CHANNEL_ID"
    update-interval: 60  # Update interval in seconds
    topic-format: "🟢 Online: %online%/%max% | TPS: %tps%"
  
  # Scheduled reports
  scheduled-reports:
    enabled: false
    schedule: "daily"  # "daily" or "weekly"
    channel-id: "INSERT_REPORTS_CHANNEL_ID"
```

### jobs.yml
Jobs configuration file. See the file for detailed job setup with actions, experience, and rewards.

### quests.yml
Quests configuration file with 100+ predefined quests. See the file for detailed quest setup with types, chains, and rewards.

### enchantments.yml
Custom enchantments configuration file with 90+ unique enchantments. Includes comprehensive documentation on creating new enchantments.

### recipes.yml
Custom recipes storage file. Recipes are created via commands and stored here.
```

---

## 🔗 Discord Setup

### Step 1: Create a Discord Bot

1. Go to [Discord Developer Portal](https://discord.com/developers/applications) and create a new application
2. Navigate to the "Bot" tab and click "Add Bot"
3. Copy the bot token (you'll need this for `discordconf.yml`)
4. Enable the following **Privileged Gateway Intents**:
   - ✅ Server Members Intent
   - ✅ Message Content Intent
5. Under "OAuth2" → "URL Generator":
   - Select scope: `bot`
   - Select permissions:
     - Send Messages
     - Read Message History
     - Embed Links
     - Attach Files
     - Use Slash Commands
     - Manage Messages (for filtering)
   - Copy the generated URL and invite the bot to your server

### Step 2: Get Channel IDs

1. Enable Developer Mode in Discord (Settings → Advanced → Developer Mode)
2. Right-click on the channel you want to use for chat bridging
3. Click "Copy ID" and paste it into `discordconf.yml` as `channel-id`
4. Repeat for punishment logs channel (`punishment-channel-id`)
5. Optionally set up a separate staff logs channel (`staff-logs-channel-id`)

### Step 3: Configure discordconf.yml

1. Set `discord.enabled: true`
2. Paste your bot token into `discord.bot-token`
3. Paste your channel IDs into the appropriate fields
4. Configure additional features as needed:
   - Enable/disable rich embeds
   - Set up chat filtering
   - Configure role-based permissions
   - Enable scheduled reports
   - Set up status channel updates

### Step 4: Configure Role Permissions

To use Discord slash commands with role-based permissions:

1. Get your role IDs (right-click role → Copy ID with Developer Mode enabled)
2. Add role IDs or names to `discord.permissions.staff` for staff commands
3. Add role IDs or names to `discord.permissions.admin` for admin commands

Example:
```yaml
permissions:
  staff:
    - "123456789012345678"  # Role ID
    - "Moderator"  # Or role name
  admin:
    - "987654321098765432"
    - "Admin"
```

### Step 5: Account Linking (Optional)

Players can link their Discord accounts to Minecraft:

1. In-game: `/link` - Generates a verification code
2. In Discord: `/link <code>` - Links the accounts
3. Use `/unlink` in Discord to unlink

Linked accounts will show Discord names in embeds and enable enhanced features.

### Step 6: Restart Server

After configuration, restart your server. The Discord bot will connect automatically if configured correctly.

### Troubleshooting

- **Bot not connecting**: Check that the bot token is correct and the bot is invited to your server
- **Messages not sending**: Verify channel IDs are correct and the bot has permission to send messages
- **Slash commands not working**: Ensure the bot has "Use Slash Commands" permission and wait a few minutes for commands to register
- **Rate limiting**: Enable webhook support or increase rate limit cooldown in config

---

## 🎮 Commands

### Main Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/ecore [reload\|staff\|home]` | Main plugin command | `ecore.staff` (for reload/staff), `ecore.home` (for home) | `op` / `true` |

### Home Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/home [name]` | Teleport to a home or open home GUI | `ecore.home` | `true` |
| `/sethome <name>` | Set a home at your location | `ecore.home` | `true` |
| `/listhomes` | List all your homes | `ecore.home` | `true` |
| `/homeshare <home> <player>` | Share a home with a player | `ecore.home` | `true` |
| `/homeunshare <home> <player>` | Unshare a home with a player | `ecore.home` | `true` |
| `/homecategory <home> <category>` | Set home category | `ecore.home` | `true` |
| `/homeicon <home> <material>` | Set home icon | `ecore.home` | `true` |
| `/homedescription <home> <description>` | Set home description | `ecore.home` | `true` |

### Economy Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/balance [player]` | Check balance (yours or others) | `ecore.economy`, `ecore.economy.balance.others` | `true` / `op` |
| `/bal [player]` | Alias for balance | `ecore.economy`, `ecore.economy.balance.others` | `true` / `op` |
| `/money [player]` | Alias for balance | `ecore.economy`, `ecore.economy.balance.others` | `true` / `op` |
| `/pay <player> <amount>` | Pay a player | `ecore.economy` | `true` |
| `/economy <give\|take\|set\|stats> <player> [amount]` | Economy administration | `ecore.economy.admin` | `op` |
| `/eco <give\|take\|set\|stats> <player> [amount]` | Alias for economy | `ecore.economy.admin` | `op` |
| `/baltop [limit]` | View economy leaderboard | `ecore.economy` | `true` |
| `/balancetop [limit]` | Alias for baltop | `ecore.economy` | `true` |

### Bank Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/bank create <name>` | Create a bank account | `ecore.economy` | `true` |
| `/bank delete <name>` | Delete a bank account | `ecore.economy` | `true` |
| `/bank list` | List your bank accounts | `ecore.economy` | `true` |
| `/bank balance [account]` | Check account balance | `ecore.economy` | `true` |
| `/bank deposit <account> <amount>` | Deposit money | `ecore.economy` | `true` |
| `/bank withdraw <account> <amount>` | Withdraw money | `ecore.economy` | `true` |
| `/bank transfer <from> <to> <amount>` | Transfer between accounts | `ecore.economy` | `true` |
| `/bank interest <account> [rate]` | View or set interest rate | `ecore.economy`, `ecore.bank.admin` (to set) | `true` / `op` |

### Teleportation Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/tp <player>` | Teleport to a player | `ecore.teleport` | `op` |
| `/tp <player1> <player2>` | Teleport player1 to player2 | `ecore.teleport.others` | `op` |
| `/tp <x> <y> <z> [world]` | Teleport to coordinates | `ecore.teleport` | `op` |
| `/teleport <player>` | Alias for tp | `ecore.teleport` | `op` |
| `/tpa <player>` | Request to teleport to player | `ecore.teleport` | `op` |
| `/tpahere <player>` | Request player to teleport to you | `ecore.teleport` | `op` |
| `/tpaccept` | Accept teleport request | `ecore.teleport` | `op` |
| `/tpdeny` | Deny teleport request | `ecore.teleport` | `op` |
| `/back` | Return to previous location | `ecore.teleport` | `op` |
| `/top` | Teleport to highest block | `ecore.teleport` | `op` |
| `/jump` | Teleport forward | `ecore.teleport` | `op` |
| `/rtp` | Random teleport | `ecore.teleport` | `op` |
| `/tpbiome <biome>` | Teleport to a biome | `ecore.teleport` | `op` |
| `/teleportbiome <biome>` | Alias for tpbiome | `ecore.teleport` | `op` |
| `/tpstructure <structure>` | Teleport to a structure | `ecore.teleport` | `op` |
| `/teleportstructure <structure>` | Alias for tpstructure | `ecore.teleport` | `op` |

### Warp & Spawn Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/warp [name]` | Teleport to a warp or open warp GUI | `ecore.warp` | `true` |
| `/setwarp <name>` | Create a warp | `ecore.warp.set` | `op` |
| `/delwarp <name>` | Delete a warp | `ecore.warp.delete` | `op` |
| `/deletewarp <name>` | Alias for delwarp | `ecore.warp.delete` | `op` |
| `/warps` | List all warps or open warp GUI | `ecore.warp` | `true` |
| `/spawn [player]` | Teleport to spawn | `ecore.spawn`, `ecore.spawn.others` | `true` / `op` |
| `/setspawn` | Set spawn point | `ecore.spawn.set` | `op` |

### Chat Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/msg <player> <message>` | Send private message | `ecore.economy` | `true` |
| `/message <player> <message>` | Alias for msg | `ecore.economy` | `true` |
| `/tell <player> <message>` | Alias for msg | `ecore.economy` | `true` |
| `/whisper <player> <message>` | Alias for msg | `ecore.economy` | `true` |
| `/reply <message>` | Reply to last message | `ecore.economy` | `true` |
| `/r <message>` | Alias for reply | `ecore.economy` | `true` |
| `/chat <on\|off\|clear>` | Manage chat | `ecore.chat.manage` | `op` |
| `/sc <message>` | Staff chat | `ecore.staff` | `op` |
| `/staffchat <message>` | Alias for sc | `ecore.staff` | `op` |
| `/ac <message>` | Admin chat | `ecore.admin` | `op` |
| `/adminchat <message>` | Alias for ac | `ecore.admin` | `op` |

### Staff Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/mute <player> [duration]` | Mute a player | `ecore.staff` | `op` |
| `/unmute <player>` | Unmute a player | `ecore.staff` | `op` |
| `/freeze <player>` | Freeze a player | `ecore.staff` | `op` |
| `/unfreeze <player>` | Unfreeze a player | `ecore.staff` | `op` |
| `/commandspy` | Toggle command spy | `ecore.staff` | `op` |
| `/socialspy` | Toggle social spy | `ecore.staff` | `op` |
| `/give <player> <item> [amount]` | Give item to player | `ecore.staff` | `op` |
| `/enchant <player> <enchantment> <level>` | Enchant player's item | `ecore.staff` | `op` |
| `/repair [all]` | Repair items | `ecore.staff` | `op` |
| `/chatslow <seconds>` | Set chat slow mode | `ecore.staff` | `op` |

### Kit Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/kit <name>` | Get a kit | `ecore.kit.<name>` or `ecore.kit.*` | `true` |
| `/kit list` | List all kits or open kit GUI | `ecore.kit` | `true` |
| `/kit create <name>` | Create a kit from inventory | `ecore.kit.create` | `op` |
| `/kit delete <name>` | Delete a kit | `ecore.kit.delete` | `op` |
| `/kit give <player> <kit>` | Give kit to player | `ecore.kit.give` | `op` |

### Mail Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/mail send <player> <message>` | Send mail to player | `ecore.mail` | `true` |
| `/mail read` | Read mail (opens GUI) | `ecore.mail` | `true` |
| `/mail clear` | Clear all mail | `ecore.mail` | `true` |
| `/mail sendall <message>` | Send mail to all players | `ecore.mail.sendall` | `op` |

### Statistics Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/stats` | View your statistics (opens GUI) | `ecore.economy` | `true` |
| `/statistics` | Alias for stats | `ecore.economy` | `true` |
| `/leaderboard [stat]` | View leaderboards | `ecore.economy` | `true` |
| `/lb [stat]` | Alias for leaderboard | `ecore.economy` | `true` |
| `/statsreset <player> [stat]` | Reset player statistics | `ecore.stats.reset` | `op` |
| `/resetstats <player> [stat]` | Alias for statsreset | `ecore.stats.reset` | `op` |

### Achievement Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/achievements` | View your achievements | `ecore.economy` | `true` |
| `/achievement` | Alias for achievements | `ecore.economy` | `true` |
| `/achievement list` | List all achievements | `ecore.economy` | `true` |
| `/achievement give <player> <id>` | Give achievement to player | `ecore.achievement.give` | `op` |
| `/achievement check` | Check for new achievements | `ecore.economy` | `true` |

### Auction House Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/ah` | Open auction house GUI | `ecore.economy` | `true` |
| `/auctionhouse` | Alias for ah | `ecore.economy` | `true` |
| `/auction` | Alias for ah | `ecore.economy` | `true` |
| `/ah create <starting-bid> <buyout> <duration>` | Create auction | `ecore.economy` | `true` |
| `/ah bid <id> <amount>` | Place bid on auction | `ecore.economy` | `true` |
| `/ah buyout <id>` | Buyout auction | `ecore.economy` | `true` |
| `/ah cancel <id>` | Cancel your auction | `ecore.economy` | `true` |
| `/ah list` | List auctions (opens GUI) | `ecore.economy` | `true` |
| `/ah my` | View your auctions | `ecore.economy` | `true` |

### Player Info Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/whois <player>` | Get detailed player information | `ecore.economy` | `true` |
| `/seen <player>` | Check when player was last seen | `ecore.economy` | `true` |
| `/list` | List online players | `ecore.economy` | `true` |
| `/who` | Alias for list | `ecore.economy` | `true` |
| `/ping [player]` | Check ping | `ecore.economy` | `true` |
| `/near [radius]` | Show nearby players | `ecore.economy` | `true` |

### Time & Weather Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/time <set\|add> <value>` | Manage time | `ecore.time` | `op` |
| `/day` | Set time to day | `ecore.time` | `op` |
| `/night` | Set time to night | `ecore.time` | `op` |
| `/weather <clear\|rain\|storm>` | Manage weather | `ecore.weather` | `op` |
| `/sun` | Clear weather | `ecore.weather` | `op` |
| `/clear` | Clear weather | `ecore.weather` | `op` |
| `/rain` | Set rain | `ecore.weather` | `op` |
| `/storm` | Set storm | `ecore.weather` | `op` |
| `/thunder` | Set storm | `ecore.weather` | `op` |

### AFK Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/afk` | Toggle AFK status | `ecore.afk` | `true` |
| `/afk <player>` | Check player AFK status | `ecore.afk.check` | `true` |

### Jail Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/jail <player> <jail> [time] [reason]` | Jail a player | `ecore.jail` | `op` |
| `/unjail <player>` | Unjail a player | `ecore.jail` | `op` |
| `/setjail <name>` | Create a jail location | `ecore.jail.set` | `op` |
| `/jailinfo <player>` | Check jail information | `ecore.jail` | `op` |

### Report Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/report <player> <reason>` | Report a player | `ecore.report` | `true` |

### GameMode Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/gm` | Open GameMode GUI | `ecore.gamemode` | `op` |

### Server Info Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/serverinfo` | Display server information | `ecore.serverinfo` | `op` |
| `/serverinfo clear` | Perform entity cleanup | `ecore.serverinfo.cleanup` | `op` |
| `/serverinfo stats` | Show performance statistics | `ecore.serverinfo.stats` | `op` |
| `/serverinfo merge [radius]` | Merge nearby items | `ecore.serverinfo.merge` | `op` |
| `/serverinfo chunks` | Optimize chunks | `ecore.serverinfo.chunks` | `op` |

### Block Logging Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/blocklog` | Open block logging GUI | `ecore.blocklog.use` | `op` |
| `/blocklog lookup <player> [time]` | View player logs | `ecore.blocklog.lookup` | `op` |
| `/blocklog rollback <player> [time]` | Rollback player actions | `ecore.blocklog.rollback` | `op` |
| `/blocklog restore [time]` | Restore selected area | `ecore.blocklog.restore` | `op` |
| `/blocklog inspect` | Get inspector wand | `ecore.blocklog.inspect` | `op` |
| `/blocklog inventory <player> [time]` | Rollback inventory | `ecore.blocklog.inventory` | `op` |
| `/blocklog purge [days]` | Purge old logs | `ecore.blocklog.purge` | `op` |
| `/bl` | Alias for blocklog | `ecore.blocklog.use` | `op` |
| `/co` | Alias for blocklog (CoreProtect-like) | `ecore.blocklog.use` | `op` |

### Friends & Party Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/friend` | Open friend GUI | `ecore.friend` | `true` |
| `/friend add <player>` | Send friend request | `ecore.friend` | `true` |
| `/friend remove <player>` | Remove friend | `ecore.friend` | `true` |
| `/friend list` | List friends | `ecore.friend` | `true` |
| `/friend accept <player>` | Accept friend request | `ecore.friend` | `true` |
| `/friend deny <player>` | Deny friend request | `ecore.friend` | `true` |
| `/friend requests` | View pending requests | `ecore.friend` | `true` |
| `/party` | Open party GUI | `ecore.party` | `true` |
| `/party create` | Create a party | `ecore.party` | `true` |
| `/party invite <player>` | Invite player to party | `ecore.party` | `true` |
| `/party accept <leader>` | Accept party invite | `ecore.party` | `true` |
| `/party leave` | Leave party | `ecore.party` | `true` |
| `/party kick <player>` | Kick player (leader only) | `ecore.party` | `true` |
| `/party list` | Show party info | `ecore.party` | `true` |
| `/party chat <message>` | Send party message | `ecore.party` | `true` |
| `/p` | Alias for party | `ecore.party` | `true` |

### Scoreboard & Tab List Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/scoreboard toggle` | Toggle your scoreboard | `ecore.scoreboard.use` | `true` |
| `/scoreboard reload` | Reload scoreboard config | `ecore.scoreboard.reload` | `op` |
| `/scoreboard tablist reload` | Reload tab list config | `ecore.tablist.reload` | `op` |
| `/sb` | Alias for scoreboard | `ecore.scoreboard.use` | `true` |

### Chunk Pregeneration Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/chunks generate <radius>` | Generate chunks in radius from spawn | `ecore.chunks.generate` | `op` |
| `/chunks cancel` | Cancel your active chunk generation | `ecore.chunks.generate` | `op` |
| `/chunks status` | Check if you have active chunk generation | `ecore.chunks.generate` | `op` |

### WorldEdit Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/wand` | Get selection wand | `ecore.worldedit.wand` | `op` |
| `/pos1` | Set selection position 1 | `ecore.worldedit.use` | `op` |
| `/pos2` | Set selection position 2 | `ecore.worldedit.use` | `op` |
| `/set <block>` | Fill selection with blocks | `ecore.worldedit.set` | `op` |
| `/replace <from> <to>` | Replace blocks in selection | `ecore.worldedit.replace` | `op` |
| `/clear` | Clear selection (set to air) | `ecore.worldedit.clear` | `op` |
| `/walls <block>` | Create walls in selection | `ecore.worldedit.walls` | `op` |
| `/hollow <block>` | Create hollow box in selection | `ecore.worldedit.hollow` | `op` |
| `/copy` | Copy selection to clipboard | `ecore.worldedit.copy` | `op` |
| `/paste` | Paste clipboard at your location | `ecore.worldedit.paste` | `op` |
| `/cut` | Cut selection to clipboard | `ecore.worldedit.cut` | `op` |
| `/undo` | Undo last WorldEdit operation | `ecore.worldedit.undo` | `op` |
| `/redo` | Redo last undone operation | `ecore.worldedit.redo` | `op` |
| `/schematic <save\|load\|list\|delete> [name]` | Manage schematics | `ecore.worldedit.schematic` | `op` |
| `/sphere <radius> <block> [hollow]` | Create a sphere | `ecore.worldedit.sphere` | `op` |
| `/cylinder <radius> <height> <block> [hollow]` | Create a cylinder | `ecore.worldedit.cylinder` | `op` |
| `/sel` | Show selection information | `ecore.worldedit.use` | `op` |
| `/selection` | Alias for sel | `ecore.worldedit.use` | `op` |

### Region Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/region create <name> <type>` | Create a region from selection | `ecore.region.create` | `op` |
| `/region delete <name>` | Delete a region | `ecore.region.delete` | `op` |
| `/region list [world]` | List all regions | `ecore.region.list` | `op` |
| `/region info <name>` | Show region information | `ecore.region.info` | `op` |
| `/region flag <name> <flag> <true\|false>` | Set a region flag | `ecore.region.flag` | `op` |
| `/region flags <name>` | Show all flags for a region | `ecore.region.info` | `op` |
| `/region addowner <name> <player>` | Add an owner to region | `ecore.region.owner` | `op` |
| `/region removeowner <name> <player>` | Remove owner from region | `ecore.region.owner` | `op` |
| `/region addmember <name> <player>` | Add a member to region | `ecore.region.member` | `op` |
| `/region removemember <name> <player>` | Remove member from region | `ecore.region.member` | `op` |
| `/region types` | List available region types | `ecore.region.info` | `op` |
| `/region reload` | Reload regions from file | `ecore.region.reload` | `op` |

### World Management Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/mv create <name> [type] [environment] [seed]` | Create a new world | `ecore.world.create` | `op` |
| `/multiverse create <name> [type] [environment] [seed]` | Alias for mv create | `ecore.world.create` | `op` |
| `/mv load <name>` | Load an existing world | `ecore.world.load` | `op` |
| `/mv unload <name> [save]` | Unload a world | `ecore.world.unload` | `op` |
| `/mv delete <name>` | Delete a world (WARNING: Permanent!) | `ecore.world.delete` | `op` |
| `/mv list` | List all worlds | `ecore.world.list` | `op` |
| `/mv tp <world> [player]` | Teleport to a world | `ecore.world.teleport`, `ecore.world.teleport.others` | `op` |
| `/mv spawn <world>` | Teleport to world spawn | `ecore.world.spawn` | `op` |
| `/mv setspawn [world]` | Set world spawn to your location | `ecore.world.setspawn` | `op` |
| `/mv info <world>` | View world information | `ecore.world.info` | `op` |
| `/mv reload` | Reload world configuration | `ecore.world.reload` | `op` |

**World Types:** `NORMAL`, `FLAT`, `LARGE_BIOMES`, `AMPLIFIED`, `CUSTOMIZED`  
**Environments:** `NORMAL`, `NETHER`, `THE_END`

### Portal Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/portal create <name> [material]` | Create a portal from selection | `ecore.portal.create` | `op` |
| `/portal delete <name>` | Delete a portal | `ecore.portal.delete` | `op` |
| `/portal list` | List all portals | `ecore.portal.list` | `op` |
| `/portal info <name>` | View portal information | `ecore.portal.info` | `op` |
| `/portal setdest <name>` | Set portal destination to your location | `ecore.portal.setdest` | `op` |
| `/portal wand` | Get portal creation instructions | `ecore.portal.wand` | `op` |

**Note:** To create a portal, first select an area using `/pos1` and `/pos2`, then use `/portal create <name>`. The destination is set to your current location. Use `/portal setdest <name>` to change the destination later.

### Shop Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/shopedit <buy\|sell\|quantity> <value>` | Edit shop prices or quantity | `ecore.adminshop.edit` or `ecore.pshop.edit` | `op` |

### Nickname Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/nick <nickname>` | Set your nickname | `ecore.nickname` | `true` |
| `/nickname <nickname>` | Alias for nick | `ecore.nickname` | `true` |
| `/nick set <nickname>` | Set nickname (explicit) | `ecore.nickname` | `true` |
| `/nick reset` | Reset your nickname | `ecore.nickname` | `true` |
| `/nick color <color>` | Set nickname color | `ecore.nickname.color` | `false` |
| `/nick format <format>` | Set nickname format | `ecore.nickname.format` | `false` |
| `/nick view [player]` | View nickname | `ecore.nickname`, `ecore.nickname.view.others` | `true` |

### Jobs Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/jobs` | Open jobs GUI | `ecore.jobs` | `true` |
| `/jobs join <job>` | Join a job | `ecore.jobs` | `true` |
| `/jobs leave` | Leave current job | `ecore.jobs` | `true` |
| `/jobs info` | View job info | `ecore.jobs` | `true` |
| `/jobs top [job]` | View job leaderboard | `ecore.jobs` | `true` |
| `/jobs list` | List available jobs | `ecore.jobs` | `true` |

### Quest Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/quest` | Open quest GUI | `ecore.quests` | `true` |
| `/quests` | Alias for quest | `ecore.quests` | `true` |
| `/quest list [category]` | List available quests | `ecore.quests` | `true` |
| `/quest start <quest-id>` | Start a quest | `ecore.quests` | `true` |
| `/quest active` | View active quests | `ecore.quests` | `true` |
| `/quest completed` | View completed quests | `ecore.quests` | `true` |
| `/quest info <quest-id>` | View quest information | `ecore.quests` | `true` |

### Chat Channel Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/channel` | Show channel help | `ecore.channel` | `true` |
| `/channel join <channel>` | Join a channel | `ecore.channel` | `true` |
| `/channel leave <channel>` | Leave a channel | `ecore.channel` | `true` |
| `/channel list` | List available channels | `ecore.channel` | `true` |
| `/channel current` | View current channel info | `ecore.channel` | `true` |
| `/channel mute <channel>` | Mute a channel | `ecore.channel` | `true` |
| `/channel unmute <channel>` | Unmute a channel | `ecore.channel` | `true` |
| `/channel create <id> [name] [prefix]` | Create channel (admin) | `ecore.channel.admin` | `op` |
| `/channel delete <id>` | Delete channel (admin) | `ecore.channel.admin` | `op` |
| `/ch <message>` | Chat in current channel | `ecore.channel` | `true` |

### Player Vault Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/vault` | Open vault selection GUI | `ecore.vault` | `true` |
| `/pv` | Alias for vault | `ecore.vault` | `true` |
| `/vault open <number>` | Open specific vault | `ecore.vault` | `true` |
| `/vault create <number>` | Create new vault | `ecore.vault` | `true` |
| `/vault rename <number> <name>` | Rename vault | `ecore.vault` | `true` |
| `/vault trust <number> <player>` | Trust player with vault | `ecore.vault` | `true` |
| `/vault untrust <number> <player>` | Untrust player | `ecore.vault` | `true` |
| `/vault list` | List your vaults | `ecore.vault` | `true` |

### Title & Action Bar Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/title <player> <title> [subtitle] [fadeIn] [stay] [fadeOut]` | Send title to player | `ecore.title` | `op` |
| `/titleall <title> [subtitle] [fadeIn] [stay] [fadeOut]` | Broadcast title to all | `ecore.title.all` | `op` |
| `/actionbar <player> <message>` | Send action bar message | `ecore.actionbar` | `op` |
| `/actionbarall <message>` | Broadcast action bar to all | `ecore.actionbar.all` | `op` |
| `/cleartitle [player\|all]` | Clear title | `ecore.title.clear`, `ecore.title.clear.all` | `op` |

### Custom Recipe Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/recipe list` | List all custom recipes | `ecore.recipe` | `true` |
| `/recipe create <id> <shaped\|shapeless>` | Create a recipe (admin) | `ecore.recipe.admin` | `op` |
| `/recipe remove <id>` | Remove a recipe (admin) | `ecore.recipe.admin` | `op` |
| `/recipe reload` | Reload recipes from config (admin) | `ecore.recipe.admin` | `op` |

### Custom Enchantment Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/enchant list` | List all custom enchantments | `ecore.enchant` | `true` |
| `/customenchant list` | Alias for enchant list | `ecore.enchant` | `true` |
| `/enchant info <id>` | View enchantment info | `ecore.enchant` | `true` |
| `/enchant apply <id> [level]` | Apply enchantment to held item (admin) | `ecore.enchant.admin` | `op` |
| `/enchant remove <id>` | Remove enchantment from held item (admin) | `ecore.enchant.admin` | `op` |

### Staff Mode Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/staffmode` | Toggle staff mode | `ecore.staffmode` | `op` |
| `/sm` | Alias for staffmode | `ecore.staffmode` | `op` |

---

## 🔐 Permissions

### Basic Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.home` | Access home system | `true` |
| `ecore.report` | Submit reports | `true` |
| `ecore.chat.bypass` | Bypass chat restrictions | `op` |
| `ecore.nickname` | Set nicknames | `true` |
| `ecore.colorchat` | Use color codes in chat | `true` |
| `ecore.colorsign` | Use color codes on signs | `true` |
| `ecore.sit` | Sit on stairs | `true` |

### Staff Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.staff` | Access staff commands and GUI | `op` |
| `ecore.vanish` | Use vanish mode | `op` |
| `ecore.teleport` | Teleport to players | `op` |
| `ecore.teleport.others` | Teleport other players | `op` |
| `ecore.serverinfo` | View server info | `op` |
| `ecore.chat.manage` | Manage chat | `op` |
| `ecore.admin` | Admin chat access | `op` |

### Economy Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.economy` | Use economy commands | `true` |
| `ecore.economy.balance.others` | Check others' balance | `op` |
| `ecore.economy.admin` | Economy administration | `op` |
| `ecore.bank.admin` | Set bank interest rates | `op` |

### Shop Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.adminshop` | Create admin shops | `op` |
| `ecore.adminshop.edit` | Edit admin shop prices | `op` |
| `ecore.pshop` | Create player shops | `true` |
| `ecore.pshop.edit` | Edit player shop prices | `op` |

### GameMode Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.gamemode` | Access GameMode GUI | `op` |
| `ecore.gamemode.survival` | Switch to Survival mode | `op` |
| `ecore.gamemode.creative` | Switch to Creative mode | `op` |
| `ecore.gamemode.adventure` | Switch to Adventure mode | `op` |
| `ecore.gamemode.spectator` | Switch to Spectator mode | `op` |

### Warp & Spawn Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.warp` | Use warps | `true` |
| `ecore.warp.set` | Create warps | `op` |
| `ecore.warp.delete` | Delete warps | `op` |
| `ecore.spawn` | Teleport to spawn | `true` |
| `ecore.spawn.set` | Set spawn | `op` |
| `ecore.spawn.others` | Teleport others to spawn | `op` |

### Kit Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.kit` | Use kits | `true` |
| `ecore.kit.<name>` | Use specific kit | `true` |
| `ecore.kit.*` | Use all kits | `true` |
| `ecore.kit.create` | Create kits | `op` |
| `ecore.kit.delete` | Delete kits | `op` |
| `ecore.kit.give` | Give kits to others | `op` |

### Mail Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.mail` | Use mail | `true` |
| `ecore.mail.sendall` | Send mail to all players | `op` |

### Time & Weather Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.time` | Manage time | `op` |
| `ecore.weather` | Manage weather | `op` |

### AFK Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.afk` | Use AFK command | `true` |
| `ecore.afk.check` | Check others' AFK status | `true` |

### Jail Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.jail` | Jail players | `op` |
| `ecore.jail.set` | Create jails | `op` |

### Statistics Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.stats.reset` | Reset player statistics | `op` |

### Achievement Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.achievement.give` | Give achievements to players | `op` |

### WorldEdit Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.worldedit.use` | Use WorldEdit commands | `op` |
| `ecore.worldedit.wand` | Get selection wand | `op` |
| `ecore.worldedit.set` | Use /set command | `op` |
| `ecore.worldedit.replace` | Use /replace command | `op` |
| `ecore.worldedit.clear` | Use /clear command | `op` |
| `ecore.worldedit.walls` | Use /walls command | `op` |
| `ecore.worldedit.hollow` | Use /hollow command | `op` |
| `ecore.worldedit.copy` | Use /copy command | `op` |
| `ecore.worldedit.paste` | Use /paste command | `op` |
| `ecore.worldedit.cut` | Use /cut command | `op` |
| `ecore.worldedit.undo` | Use /undo command | `op` |
| `ecore.worldedit.redo` | Use /redo command | `op` |
| `ecore.worldedit.schematic` | Use schematic commands | `op` |
| `ecore.worldedit.schematic.delete` | Delete schematics | `op` |
| `ecore.worldedit.sphere` | Use /sphere command | `op` |
| `ecore.worldedit.cylinder` | Use /cylinder command | `op` |

### Chunk Pregeneration Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.chunks.generate` | Generate chunks | `op` |

### Region Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.region` | Use region commands | `op` |
| `ecore.region.create` | Create regions | `op` |
| `ecore.region.delete` | Delete regions | `op` |
| `ecore.region.list` | List regions | `op` |
| `ecore.region.info` | View region information | `op` |
| `ecore.region.flag` | Set region flags | `op` |
| `ecore.region.owner` | Manage region owners | `op` |
| `ecore.region.member` | Manage region members | `op` |
| `ecore.region.reload` | Reload regions | `op` |
| `ecore.region.bypass` | Bypass all region restrictions | `op` |
| `ecore.region.unlimited` | Create unlimited regions | `op` |

### World Management Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.world.create` | Create new worlds | `op` |
| `ecore.world.load` | Load worlds | `op` |
| `ecore.world.unload` | Unload worlds | `op` |
| `ecore.world.delete` | Delete worlds | `op` |
| `ecore.world.list` | List worlds | `op` |
| `ecore.world.teleport` | Teleport to worlds | `op` |
| `ecore.world.teleport.others` | Teleport other players to worlds | `op` |
| `ecore.world.spawn` | Teleport to world spawns | `op` |
| `ecore.world.setspawn` | Set world spawns | `op` |
| `ecore.world.info` | View world information | `op` |
| `ecore.world.reload` | Reload world configuration | `op` |

### Portal Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.portal.create` | Create portals | `op` |
| `ecore.portal.delete` | Delete portals | `op` |
| `ecore.portal.list` | List portals | `op` |
| `ecore.portal.info` | View portal information | `op` |
| `ecore.portal.setdest` | Set portal destinations | `op` |
| `ecore.portal.wand` | Use portal creation wand | `op` |
| `ecore.portal.use` | Use portals (enter portal blocks) | `true` |

### Block Logging Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.blocklog.use` | Use block logging commands | `op` |
| `ecore.blocklog.lookup` | Lookup block logs | `op` |
| `ecore.blocklog.rollback` | Rollback blocks | `op` |
| `ecore.blocklog.restore` | Restore blocks | `op` |
| `ecore.blocklog.inspect` | Use inspector tool | `op` |
| `ecore.blocklog.inventory` | Rollback inventories | `op` |
| `ecore.blocklog.purge` | Purge old logs | `op` |
| `ecore.blocklog.reload` | Reload block logging config | `op` |

### Performance Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.serverinfo.cleanup` | Perform server cleanup | `op` |
| `ecore.serverinfo.stats` | View performance statistics | `op` |
| `ecore.serverinfo.merge` | Merge items | `op` |
| `ecore.serverinfo.chunks` | Optimize chunks | `op` |

### Friends & Party Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.friend` | Use friend commands | `true` |
| `ecore.party` | Use party commands | `true` |

### Scoreboard & Tab List Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.scoreboard.use` | Use scoreboard | `true` |
| `ecore.scoreboard.reload` | Reload scoreboard config | `op` |
| `ecore.tablist.use` | Use custom tab list | `true` |
| `ecore.tablist.reload` | Reload tab list config | `op` |

### Jobs Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.jobs` | Use jobs commands | `true` |
| `ecore.jobs.admin` | Manage jobs (admin only) | `op` |

### Quest Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.quests` | Use quest commands | `true` |
| `ecore.quests.admin` | Manage quests (admin only) | `op` |

### Chat Channel Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.channel` | Use chat channel commands | `true` |
| `ecore.channel.admin` | Manage channels (admin only) | `op` |
| `ecore.chat.global` | Use global channel | `true` |
| `ecore.chat.local` | Use local channel | `true` |
| `ecore.chat.trade` | Use trade channel | `true` |
| `ecore.chat.help` | Use help channel | `true` |
| `ecore.chat.staff` | Use staff channel | `op` |

### Player Vault Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.vault` | Use vault commands | `true` |
| `ecore.vault.1` | Allows having 1 vault | `true` |
| `ecore.vault.2` | Allows having 2 vaults | `false` |
| `ecore.vault.3` | Allows having 3 vaults | `false` |
| `ecore.vault.4` | Allows having 4 vaults | `false` |
| `ecore.vault.5` | Allows having 5 vaults | `false` |
| `ecore.vault.6` | Allows having 6 vaults | `false` |
| `ecore.vault.7` | Allows having 7 vaults | `false` |
| `ecore.vault.8` | Allows having 8 vaults | `false` |
| `ecore.vault.9` | Allows having 9 vaults | `false` |
| `ecore.vault.10` | Allows having 10 vaults | `false` |

### Title & Action Bar Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.title` | Use /title command | `op` |
| `ecore.title.all` | Use /titleall command | `op` |
| `ecore.actionbar` | Use /actionbar command | `op` |
| `ecore.actionbar.all` | Use /actionbarall command | `op` |
| `ecore.title.clear` | Use /cleartitle command | `op` |
| `ecore.title.clear.all` | Clear all titles | `op` |

### Custom Recipe Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.recipe` | View custom recipes | `true` |
| `ecore.recipe.admin` | Manage custom recipes | `op` |

### Custom Enchantment Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.enchant` | View custom enchantments | `true` |
| `ecore.enchant.admin` | Apply/remove custom enchantments | `op` |

### Staff Mode Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.staffmode` | Enter/exiting staff mode | `op` |

### Nickname Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.nickname` | Set and use nicknames | `true` |
| `ecore.nickname.color` | Use colors in nicknames | `false` |
| `ecore.nickname.format` | Use custom formatting in nicknames | `false` |
| `ecore.nickname.view.others` | View other players' nicknames | `true` |

### Command Control Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.command.bypass` | Bypass all command cooldowns and costs | `op` |
| `ecore.home.bypass` | Bypass home command cooldown/cost | `op` |
| `ecore.warp.bypass` | Bypass warp command cooldown/cost | `op` |
| `ecore.tp.bypass` | Bypass teleport command cooldown/cost | `op` |
| `ecore.spawn.bypass` | Bypass spawn command cooldown/cost | `op` |

**Note:** Command cooldowns and costs are configured in `config.yml` under `command-control.commands`. Each command can have its own bypass permission configured.

---

## 🔌 PlaceholderAPI

Ecore provides extensive PlaceholderAPI support. Use `%ecore_<placeholder>%` in any PlaceholderAPI-supported plugin.

### Available Placeholders

| Placeholder | Description |
|-------------|-------------|
| `%ecore_homes%` | Number of homes |
| `%ecore_max_homes%` | Maximum homes allowed |
| `%ecore_balance%` | Player balance |
| `%ecore_playtime%` | Playtime formatted (hours and minutes) |
| `%ecore_kills%` | Number of kills |
| `%ecore_deaths%` | Number of deaths |
| `%ecore_kdr%` | Kill/Death ratio |
| `%ecore_achievements%` | Achievement count |
| `%ecore_distance%` | Distance traveled (meters or kilometers) |
| `%ecore_items_crafted%` | Items crafted |
| `%ecore_experience_gained%` | Experience gained |
| `%ecore_damage_taken%` | Damage taken |
| `%ecore_damage_dealt%` | Damage dealt |
| `%ecore_joins%` | Number of joins |
| `%ecore_mail_count%` | Unread mail count |

### Example Usage
```
%ecore_balance% - Shows player's balance
%ecore_kdr% - Shows kill/death ratio
%ecore_homes%/%ecore_max_homes% - Shows homes (e.g., "3/5")
```

---

## 🔌 Integrations

### PlaceholderAPI
Ecore automatically registers PlaceholderAPI expansion when PlaceholderAPI is installed. No configuration needed.

### Vault (Optional)
Ecore can optionally use Vault-compatible economy plugins. Set `economy.use-vault: true` in config.yml.

### WorldGuard (Optional)
Ecore integrates with WorldGuard for region protection compatibility. Uses reflection for optional dependency.

### LuckPerms (Optional)
Ecore integrates with LuckPerms for enhanced permission and group support. Uses reflection for optional dependency.

---

## 📚 Development

### Building
```bash
mvn clean package
```

### Dependencies
- Spigot API 1.21.8+
- JDA 5.0.2 (for Discord integration)
- PlaceholderAPI (optional, soft dependency)
- Vault (optional, for economy integration)
- WorldGuard (optional, for region protection)
- LuckPerms (optional, for permissions)

### API Usage
```java
Ecore plugin = (Ecore) Bukkit.getPluginManager().getPlugin("Ecore");
EconomyManager economy = plugin.getEconomyManager();
HomeManager homes = plugin.getHomeManager();
TeleportManager teleport = plugin.getTeleportManager();
WorldManager worlds = plugin.getWorldManager();
PortalManager portals = plugin.getPortalManager();
// ... etc
```

---

## 📝 Notes

- All data is stored in YAML files by default
- Discord bot requires valid token and channel IDs
- PlaceholderAPI integration is automatic when PlaceholderAPI is installed
- Vault, WorldGuard, and LuckPerms integrations are optional and use reflection
- Configuration files are automatically validated and migrated on startup
- Expired shops are automatically cleaned up every 6 hours
- Bank interest is calculated automatically (implementation depends on BankManager)
- World data is stored in `worlds.yml` in the plugin data folder
- Portal data is stored in `portals.yml` in the plugin data folder
- Portals automatically teleport players when they step into portal blocks
- World unloading will teleport all players in that world to the default world spawn

---

## 🐛 Support

For issues, feature requests, or questions, please open an issue on the GitHub repository.

---

## 📄 License

This plugin is provided as-is for use on Minecraft servers.

---

## 🆕 Recent Updates (Version 1.0)

### New Modules Added

#### 🔍 Block Logging System
- Complete CoreProtect-like functionality with block and inventory logging
- Rollback system for blocks and player inventories
- Comprehensive GUI system for easy log browsing
- SQLite/MySQL database support with automatic purging

#### ⚡ Performance Optimization
- Automatic entity cleanup (items, mobs, projectiles)
- TPS-based emergency cleanup when server performance drops
- Item stacking and chunk optimization
- Fully integrated with ServerInfoManager

#### 👥 Friends & Party System
- Complete friend list management with requests
- Party/team system with private party chat
- Beautiful GUI interfaces for both systems
- Online status tracking for friends and party members

#### 📊 Custom Scoreboard & Tab List
- Fully customizable scoreboards with placeholders
- Custom tab list headers and footers
- Separate configuration files (`scoreboard.yml` and `tablist.yml`)
- PlaceholderAPI and ECore placeholder support
- Per-world and per-group customization options

#### 💼 Jobs System
- Multiple job types with levels, experience, and rewards
- Job GUI for easy management
- Job leaderboards and statistics
- Configurable job actions and rewards

#### 🎯 Quests System
- 100+ predefined quests with multiple quest types
- Quest chains with prerequisites
- Daily and weekly quests with automatic resets
- Quest GUI with category filtering
- Comprehensive quest configuration

#### 💬 Chat Channels System
- Multiple chat channels (global, local, trade, help, staff)
- Range-based local chat
- Channel permissions and muting
- Easy channel switching

#### 🗄️ Player Vaults System
- Multiple vaults per player (permission-based)
- Vault GUI for easy management
- Vault naming and trust system
- 54-slot storage per vault

#### 📺 Title, Subtitle & Action Bar System
- Send titles and subtitles to players
- Action bar messages
- Broadcast capabilities
- Customizable timings

#### ⏱️ Command Cooldowns & Costs
- Per-command cooldowns and economy costs
- Bypass permissions
- Easy configuration

#### 🍳 Custom Recipes System
- Shaped and shapeless recipe creation
- Recipe management and permissions
- Hot-reload support

#### ✨ Custom Enchantments System
- 90+ unique enchantments across all item types
- Scalable by level
- Comprehensive documentation for creating new enchantments

---

**Version:** 1.0  
**Minecraft:** 1.21+  
**Java:** 17+
