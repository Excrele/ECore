# Excrele's Core (Ecore)

A comprehensive, all-in-one Spigot plugin for Minecraft 1.21+ that provides everything you need to run a modern Minecraft server. Instead of installing dozens of separate plugins, ECore combines essential features into a single, optimized, and well-integrated solution.

## 📋 Table of Contents

- [Overview](#overview)
- [Documentation](#-documentation)
- [Key Features](#-key-features)
- [Installation](#-installation)
- [Quick Start](#-quick-start)
- [Commands](#-commands)
- [Permissions](#-permissions)
- [Configuration](#-configuration)
- [Discord Integration](#-discord-integration)
- [PlaceholderAPI](#placeholderapi)
- [Features Guide](#-features-guide)
- [Troubleshooting](#-troubleshooting)
- [Support](#-support)

---

## 📚 Documentation

For detailed documentation on each system, see the [Documentation Index](docs/index.md) or browse individual system documentation:

### Core Systems
- [Home System](docs/home-system.md) - Multiple homes with sharing and customization
- [Economy System](docs/economy-system.md) - Self-contained economy with banks
- [Bank System](docs/bank-system.md) - Multiple bank accounts with interest
- [Shop System](docs/shop-system.md) - Admin and player shops

### Staff & Administration
- [Staff Management](docs/staff-management.md) - Complete moderation tools
- [Staff Mode](docs/staff-mode.md) - Safe monitoring mode
- [Block Logging System](docs/block-logging-system.md) - Track and rollback changes
- [Report System](docs/report-system.md) - Player reporting

### Teleportation & Worlds
- [Teleportation System](docs/teleportation-system.md) - Advanced teleportation
- [Warp & Spawn System](docs/warp-spawn-system.md) - Public warps and spawn
- [Multi-World System](docs/multi-world-system.md) - World management
- [Portal System](docs/portal-system.md) - Custom portals

### Protection & Building
- [Region Protection](docs/region-protection.md) - WorldGuard-like regions
- [WorldEdit](docs/worldedit.md) - Complete WorldEdit functionality

### Social Features
- [Friends & Party System](docs/friends-party-system.md) - Friend lists and parties
- [Chat Channels System](docs/chat-channels-system.md) - Multiple chat channels
- [Mail System](docs/mail-system.md) - Send and receive mail

### Progression Systems
- [Jobs System](docs/jobs-system.md) - Job system with rewards
- [Quest System](docs/quest-system.md) - 100+ quests with chains
- [Statistics & Achievements](docs/statistics-achievements.md) - Track progress

### Customization
- [Custom Enchantments](docs/custom-enchantments.md) - 90+ unique enchantments
- [Custom Recipes](docs/custom-recipes.md) - Custom crafting recipes
- [Custom Scoreboard & Tab List](docs/custom-scoreboard-tablist.md) - Custom displays
- [Nickname System](docs/nickname-system.md) - Enhanced nicknames

### Player Features
- [Player Vaults System](docs/player-vaults-system.md) - Extra storage
- [Kit System](docs/kit-system.md) - Create and manage kits
- [AFK System](docs/afk-system.md) - AFK detection
- [Jail System](docs/jail-system.md) - Jail players

### Server Management
- [Performance Optimization](docs/performance-optimization.md) - Lag reduction
- [Backup System](docs/backup-system.md) - Automatic backups
- [Mob Customization](docs/mob-customization.md) - Customize mobs
- [Command Cooldowns & Costs](docs/command-cooldowns-costs.md) - Control commands

### Integration
- [Discord Integration](docs/discord-integration.md) - Chat bridging and management
- [PlaceholderAPI](docs/placeholderapi.md) - Placeholder support

**📖 [View Full Documentation Index](docs/index.md)**

---

## Overview

**ECore** (Excrele's Core) is a comprehensive, all-in-one Spigot plugin that replaces multiple popular plugins with a single, well-integrated solution. It's designed for modern Minecraft servers running version 1.21 or higher.

### What Makes ECore Stand Out?

- **🎯 All-in-One Solution**: Replaces EssentialsX, WorldEdit, CoreProtect, ClearLagg, Jobs Reborn, Quests plugins, Friends plugins, PlayerVaults, and more
- **⚡ Performance Optimized**: Built-in performance monitoring, automatic cleanup, and lag reduction features
- **🔒 Self-Contained Economy**: No external dependencies required - includes complete economy system with banks, shops, and auction house
- **🎮 Modern GUI System**: Beautiful, intuitive GUIs for homes, shops, kits, mail, statistics, achievements, and more
- **🤖 Advanced Discord Integration**: Full chat bridging, staff logs, slash commands, and server status updates
- **📊 Comprehensive Logging**: CoreProtect-like block logging with rollback capabilities and inventory protection
- **🌍 Multi-World Management**: Complete world creation, management, and portal system built-in
- **🛡️ Region Protection**: WorldGuard-like region system with flags, owners, and members
- **✨ 90+ Custom Enchantments**: Extensive library of unique enchantments for all item types
- **🎯 100+ Predefined Quests**: Ready-to-use quest system with multiple quest types and chains
- **💼 Jobs System**: Complete job system with levels, experience, and rewards
- **👥 Social Features**: Friends and party systems with private chat
- **📺 Visual Customization**: Custom scoreboards, tab lists, titles, and action bars

---

## ✨ Key Features

### 🏠 Home System
- Multiple homes with custom names, categories, icons, and descriptions
- Home sharing with other players
- Bed spawn integration
- Teleport costs, cooldowns, and warmup system
- Beautiful home management GUI
- Configurable maximum homes per player

### 💰 Economy System
- **Self-Contained**: No Vault dependency required (optional Vault support available)
- Starting balance configuration
- Bank system with multiple accounts and interest rates
- Economy statistics and leaderboards
- Complete API for other plugins
- Payment system between players
- Economy administration commands

### 🛒 Shop System
- **Admin Shops**: Server-controlled shops with unlimited stock
- **Player Shops**: Player-owned shops with chest storage
- Right-click to buy, left-click to sell
- Shop categories, favorites, and statistics
- Automatic expiration of inactive shops
- Configurable sign formats
- Shop GUI for browsing and searching
- Shop editing capabilities

### 👮 Staff Management
- Complete staff GUI with all moderation tools
- Player moderation (ban, kick, mute, freeze)
- Vanish system without potion effects
- Command spy and social spy
- Inventory inspection and management
- Report management system
- Staff mode with auto-vanish, fly, and invincibility
- Chat management (slow mode, clear, toggle)

### 🎮 Teleportation System
- Advanced TP system with requests
- Random teleport (RTP)
- Biome and structure teleportation
- Teleport requests (tpa, tpahere, tpaccept, tpdeny)
- Return to previous location (/back)
- Teleport to highest block (/top)
- Jump forward (/jump)
- Warp system with GUI
- Spawn system

### 🌍 Multi-World System
- Create, manage, and teleport between worlds
- World types: NORMAL, FLAT, LARGE_BIOMES, AMPLIFIED, CUSTOMIZED
- Environments: NORMAL, NETHER, THE_END
- World properties configuration
- Safe spawn location finding
- World information and statistics

### 🚪 Portal System
- Custom portals that automatically teleport players
- Multi-world portal support
- Custom block materials
- Permission-based access
- Custom messages and sounds
- Easy creation with selection tool

### 🛡️ Region Protection
- Protect areas with flags, owners, and members
- Multiple region types
- Configurable region flags (PvP, build, interact, etc.)
- Region management GUI
- WorldGuard-like functionality

### 📊 Block Logging System
- CoreProtect-like logging with rollback capabilities
- Track block breaks, places, and container access
- Inventory protection and rollback
- Inspector tool for block history
- SQLite/MySQL database support
- GUI system for easy management
- Automatic log purging

### ⚡ Performance Optimization
- Automatic cleanup of entities (items, mobs, projectiles)
- TPS-based emergency cleanup
- Item stacking (merges nearby items)
- Chunk optimization
- Performance statistics
- Scheduled maintenance

### 🤖 Discord Integration
- Two-way chat bridging
- Staff action logging
- Discord slash commands for server management
- Server status updates
- Account linking system
- Rich embeds and webhooks
- Chat filtering and moderation

### ✨ Custom Enchantments
- 90+ unique enchantments
- Categories: Weapons, Armor, Tools, Bows/Crossbows, Fishing Rods
- Scalable by level (1-5 or 1-10)
- Item-specific enchantments
- Easy application and removal

### 🎯 Quest System
- 100+ predefined quests
- Quest types: Kill, Collect, Craft, Break, Place, Fish, Breed, Travel, Eat, Enchant, Trade, Mine, Harvest, Custom
- Quest chains and prerequisites
- Daily/weekly quests
- Quest GUI with filtering
- Progress tracking

### 💼 Jobs System
- Multiple job types (Miner, Farmer, Hunter, Builder, Fisher, etc.)
- Job levels and experience
- Rewards (money and items)
- Job GUI
- Job statistics and leaderboards
- Exponential leveling system

### 👥 Friends & Parties
- Friend lists and requests
- Party system with private chat
- Friend and party GUIs
- Online status tracking
- Easy management commands

### 📦 Player Vaults
- Multiple extra storage vaults per player
- Permission-based vault limits (1-10 vaults)
- Vault naming and organization
- Trust system (share vaults with friends)
- 54-slot storage per vault

### 📺 Visual Customization
- Custom scoreboards with PlaceholderAPI support
- Custom tab list (header and footer)
- Title and subtitle system
- Action bar messages
- Per-world and per-group scoreboards

### 🎨 Additional Features
- **Custom Recipes**: Create shaped and shapeless recipes
- **Chat Channels**: Multiple channels (global, local, trade, help, staff)
- **Statistics Tracking**: Track kills, deaths, distance, items crafted, and more
- **Achievement System**: Unlockable achievements with rewards
- **Mail System**: Send and receive mail between players
- **Kit System**: Create and manage kits with cooldowns
- **AFK System**: Automatic AFK detection and manual toggle
- **Jail System**: Jail players with configurable locations and durations
- **Report System**: Player reporting with staff management
- **Nickname System**: Enhanced nicknames with colors and formatting
- **WorldEdit Commands**: Complete WorldEdit-like functionality
- **Chunk Pregeneration**: Generate chunks for better performance
- **Backup System**: Automatic backup functionality
- **Command Cooldowns & Costs**: Configure cooldowns and economy costs for commands
- **Mob Customization**: Customize mob behavior and properties

---

## 📦 Installation

### Requirements
- **Minecraft Server**: Spigot/Paper 1.21 or higher
- **Java**: Java 17 or higher
- **Optional Dependencies**:
  - PlaceholderAPI (for placeholder support)
  - Vault (for economy integration with other plugins)
  - WorldGuard (for region compatibility)
  - LuckPerms (for enhanced permissions)

### Installation Steps

1. **Download the Plugin**
   - Download `Ecore-1.0.1.jar` from the releases page

2. **Install the Plugin**
   - Place `Ecore-1.0.1.jar` in your server's `plugins` folder

3. **Start the Server**
   - Start or restart your server
   - The plugin will generate configuration files automatically

4. **Configure the Plugin**
   - Navigate to `plugins/Ecore/`
   - Edit configuration files as needed (see [Configuration](#-configuration) section)
   - Restart the server or use `/ecore reload` (requires `ecore.staff` permission)

5. **Set Up Discord Integration (Optional)**
   - See [Discord Integration](#-discord-integration) section for detailed setup instructions

### First-Time Setup Checklist

- [ ] Verify plugin loaded successfully (check console for "Ecore plugin has been enabled!")
- [ ] Review `config.yml` and adjust settings as needed
- [ ] Set up Discord integration if desired
- [ ] Configure permissions using your permission plugin (LuckPerms, PermissionsEx, etc.)
- [ ] Test basic commands (`/home`, `/balance`, `/warp`)
- [ ] Set server spawn with `/setspawn`
- [ ] Create a few warps with `/setwarp <name>`

---

## 🚀 Quick Start

### Basic Configuration (`config.yml`)

```yaml
# Home System
home:
  max-homes: 5
  teleport-cost: 0.0
  cooldown: 0
  warmup: 0
  bed-spawn-enabled: true

# Economy
economy:
  starting-balance: 100.0

# Shops
shops:
  max-shops-per-player: 10
  expiration-days: 30
  enable-categories: true
  enable-favorites: true
  enable-statistics: true
```

### Shop Sign Setup

**Admin Shops:**
1. Place a sign and write `[Admin Shop]` on the first line
2. Right-click the sign while holding the item you want to sell
3. Enter quantity (1-64) in chat
4. Enter buy price in chat
5. Enter sell price in chat
6. Shop is created!

**Player Shops:**
1. Place a chest
2. Place a sign on or next to the chest with `[PShop]` on the first line
3. Right-click the sign to open item selection GUI
4. Select an item from the GUI
5. Enter quantity (1-64) in chat
6. Enter buy price in chat
7. Enter sell price in chat
8. Shop is created! Stock items in the chest

**Using Shops:**
- **Right-click** a shop sign to **buy** items
- **Left-click** a shop sign to **sell** items (hold the item in your hand)

---

## 🎮 Commands

### Main Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/ecore [reload\|staff\|home\|tutorial]` | Main plugin command | `ecore.staff` (reload/staff), `ecore.home` (home) | `op` / `true` |

### Home Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/home [name]` | Teleport to a home or open home GUI | `ecore.home` | `true` |
| `/sethome <name>` | Set a home at your location | `ecore.home` | `true` |
| `/listhomes` | List all your homes | `ecore.home` | `true` |
| `/homeshare <home> <player>` | Share a home with a player | `ecore.home` | `true` |
| `/homeunshare <home> <player>` | Unshare a home | `ecore.home` | `true` |
| `/homecategory <home> <category>` | Set home category | `ecore.home` | `true` |
| `/homeicon <home> <material>` | Set home icon | `ecore.home` | `true` |
| `/homedescription <home> <description>` | Set home description | `ecore.home` | `true` |

### Economy Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/balance [player]` | Check balance | `ecore.economy`, `ecore.economy.balance.others` | `true` / `op` |
| `/bal [player]` | Alias for balance | `ecore.economy`, `ecore.economy.balance.others` | `true` / `op` |
| `/money [player]` | Alias for balance | `ecore.economy`, `ecore.economy.balance.others` | `true` / `op` |
| `/pay <player> <amount>` | Pay a player | `ecore.economy` | `true` |
| `/economy <give\|take\|set> <player> <amount>` | Economy administration | `ecore.economy.admin` | `op` |
| `/baltop [limit]` | View economy leaderboard | `ecore.economy` | `true` |

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

### Teleportation Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/tp <player>` | Teleport to a player | `ecore.teleport` | `op` |
| `/tpa <player>` | Request to teleport to player | `ecore.teleport` | `op` |
| `/tpahere <player>` | Request player to teleport to you | `ecore.teleport` | `op` |
| `/tpaccept` | Accept teleport request | `ecore.teleport` | `op` |
| `/tpdeny` | Deny teleport request | `ecore.teleport` | `op` |
| `/back` | Return to previous location | `ecore.teleport` | `op` |
| `/rtp` | Random teleport | `ecore.teleport` | `op` |
| `/tpbiome <biome>` | Teleport to a biome | `ecore.teleport` | `op` |
| `/tpstructure <structure>` | Teleport to a structure | `ecore.teleport` | `op` |

### Warp & Spawn Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/warp [name]` | Teleport to a warp or open warp GUI | `ecore.warp` | `true` |
| `/setwarp <name>` | Create a warp | `ecore.warp.set` | `op` |
| `/delwarp <name>` | Delete a warp | `ecore.warp.delete` | `op` |
| `/warps` | List all warps | `ecore.warp` | `true` |
| `/spawn [player]` | Teleport to spawn | `ecore.spawn`, `ecore.spawn.others` | `true` / `op` |
| `/setspawn` | Set spawn point | `ecore.spawn.set` | `op` |

### Staff Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/ecore staff` | Open staff GUI | `ecore.staff` | `op` |
| `/vanish` | Toggle vanish mode | `ecore.vanish` | `op` |
| `/mute <player> [duration]` | Mute a player | `ecore.staff` | `op` |
| `/unmute <player>` | Unmute a player | `ecore.staff` | `op` |
| `/freeze <player>` | Freeze a player | `ecore.staff` | `op` |
| `/commandspy` | Toggle command spy | `ecore.staff` | `op` |
| `/socialspy` | Toggle social spy | `ecore.staff` | `op` |
| `/staffmode` or `/sm` | Toggle staff mode | `ecore.staffmode` | `op` |

### Shop Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/shopedit <buy\|sell\|quantity> <value>` | Edit shop prices/quantity (look at shop sign) | `ecore.adminshop.edit` or `ecore.pshop.edit` | `op` |

### Additional Commands

**Kit Commands:**
- `/kit <name>` - Get a kit
- `/kit list` - List all kits or open kit GUI
- `/kit create <name>` - Create a kit from inventory (staff)
- `/kit delete <name>` - Delete a kit (staff)

**Mail Commands:**
- `/mail send <player> <message>` - Send mail to player
- `/mail read` - Read mail (opens GUI)
- `/mail clear` - Clear all mail

**Statistics Commands:**
- `/stats` - View your statistics (opens GUI)
- `/leaderboard [stat]` - View leaderboards
- `/lb [stat]` - Alias for leaderboard

**Achievement Commands:**
- `/achievements` - View your achievements
- `/achievement list` - List all achievements

**Auction House Commands:**
- `/ah` - Open auction house GUI
- `/ah create <starting-bid> <buyout> <duration>` - Create auction
- `/ah bid <id> <amount>` - Place bid
- `/ah buyout <id>` - Buyout auction

**WorldEdit Commands:**
- `/wand` - Get selection wand
- `/pos1` - Set selection position 1
- `/pos2` - Set selection position 2
- `/set <block>` - Fill selection with blocks
- `/copy`, `/paste`, `/cut` - Clipboard operations
- `/undo`, `/redo` - Undo/redo operations
- `/schematic <save\|load\|list\|delete> [name]` - Manage schematics

**World Management Commands:**
- `/mv create <name> [type] [environment] [seed]` - Create a new world
- `/mv list` - List all worlds
- `/mv tp <world> [player]` - Teleport to a world
- `/mv delete <name>` - Delete a world

**Portal Commands:**
- `/portal create <name> [material]` - Create a portal from selection
- `/portal list` - List all portals
- `/portal setdest <name>` - Set portal destination

**Block Logging Commands:**
- `/blocklog` or `/bl` or `/co` - Open block logging GUI
- `/blocklog lookup <player> [time]` - View player logs
- `/blocklog rollback <player> [time]` - Rollback player actions
- `/blocklog inspect` - Get inspector wand

**Friends & Party Commands:**
- `/friend` - Open friend GUI
- `/friend add <player>` - Send friend request
- `/party` or `/p` - Open party GUI
- `/party create` - Create a party
- `/party invite <player>` - Invite player to party

**Jobs & Quests Commands:**
- `/jobs` - Open jobs GUI
- `/jobs join <job>` - Join a job
- `/quest` - Open quest GUI
- `/quest start <quest-id>` - Start a quest

**Chat Channel Commands:**
- `/channel` - Show channel help
- `/channel join <channel>` - Join a channel
- `/ch <message>` - Chat in current channel

**Player Vault Commands:**
- `/vault` or `/pv` - Open vault selection GUI
- `/vault open <number>` - Open specific vault
- `/vault create <number>` - Create new vault

**Title & Action Bar Commands:**
- `/title <player> <title> [subtitle]` - Send title to player
- `/titleall <title> [subtitle]` - Broadcast title
- `/actionbar <player> <message>` - Send action bar message

**Custom Recipe & Enchantment Commands:**
- `/recipe list` - List all custom recipes
- `/enchant list` - List all custom enchantments
- `/enchant apply <id> [level]` - Apply enchantment (staff)

**Backup Commands:**
- `/backup create` - Create a backup
- `/backup list` - List all backups
- `/backup restore <name>` - Restore a backup

For detailed command documentation, see the [Documentation Index](docs/index.md) or individual system documentation files.

---

## 🔐 Permissions

### Basic Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.home` | Access home system | `true` |
| `ecore.economy` | Use economy commands | `true` |
| `ecore.pshop` | Create player shops | `true` |
| `ecore.adminshop` | Create admin shops | `op` |
| `ecore.staff` | Access staff commands | `op` |
| `ecore.warp` | Use warps | `true` |
| `ecore.spawn` | Teleport to spawn | `true` |
| `ecore.report` | Submit reports | `true` |
| `ecore.nickname` | Set nicknames | `true` |
| `ecore.friend` | Use friend commands | `true` |
| `ecore.party` | Use party commands | `true` |
| `ecore.jobs` | Use jobs commands | `true` |
| `ecore.quests` | Use quest commands | `true` |
| `ecore.vault` | Use vault commands | `true` |
| `ecore.channel` | Use chat channel commands | `true` |

### Staff Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.staff` | Access staff commands and GUI | `op` |
| `ecore.vanish` | Use vanish mode | `op` |
| `ecore.teleport` | Teleport to players | `op` |
| `ecore.teleport.others` | Teleport other players | `op` |
| `ecore.staffmode` | Enter/exiting staff mode | `op` |
| `ecore.serverinfo` | View server info | `op` |
| `ecore.chat.manage` | Manage chat | `op` |

### Economy Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.economy` | Use economy commands | `true` |
| `ecore.economy.balance.others` | Check others' balance | `op` |
| `ecore.economy.admin` | Economy administration | `op` |

### WorldEdit Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.worldedit.use` | Use WorldEdit commands | `op` |
| `ecore.worldedit.wand` | Get selection wand | `op` |
| `ecore.worldedit.set` | Use /set command | `op` |
| `ecore.worldedit.copy` | Use /copy command | `op` |
| `ecore.worldedit.paste` | Use /paste command | `op` |
| `ecore.worldedit.undo` | Use /undo command | `op` |

### Block Logging Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.blocklog.use` | Use block logging commands | `op` |
| `ecore.blocklog.lookup` | Lookup block logs | `op` |
| `ecore.blocklog.rollback` | Rollback blocks | `op` |
| `ecore.blocklog.restore` | Restore blocks | `op` |

### Player Vault Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.vault.1` | Allows having 1 vault | `true` |
| `ecore.vault.2` | Allows having 2 vaults | `false` |
| `ecore.vault.3` | Allows having 3 vaults | `false` |
| ... | ... | ... |
| `ecore.vault.10` | Allows having 10 vaults | `false` |

For detailed permission documentation, see the [Documentation Index](docs/index.md) or individual system documentation files.

---

## ⚙️ Configuration

ECore uses multiple YAML configuration files for easy organization. All files are located in `plugins/Ecore/`.

### Main Configuration Files

#### `config.yml`
Main configuration file containing settings for:
- Chat system (slow mode, cooldowns)
- Home system (max homes, teleport costs, cooldowns, warmup, bed spawn)
- Economy (starting balance)
- Bank system (max accounts, interest rates)
- Shop system (max shops, expiration, categories, favorites, statistics)
- Server info (TPS monitoring, update intervals)
- Performance optimization (auto-cleanup, item stacking, chunk optimization)
- WorldEdit (max block changes, blocks per tick, history size)
- Region system (enabled, auto-save, max regions, max volume)
- Staff mode (auto-vanish, auto-fly, invincible, night vision, game mode)
- Block logging (enabled, what to log, database settings, retention)
- Player vaults (max vaults, vault size)
- Command control (cooldowns and costs per command)
- Backup system (enabled, directory, interval, max backups)
- Mob customization (enabled)

**Key Configuration Options:**

```yaml
# Home System
home:
  max-homes: 5                    # Maximum homes per player
  teleport-cost: 0.0             # Cost to teleport (0.0 = free)
  cooldown: 0                    # Cooldown in seconds (0 = no cooldown)
  warmup: 0                      # Warmup delay in seconds (0 = instant)
  bed-spawn-enabled: true        # Enable bed spawn integration
  auto-set-home-on-bed: true     # Auto-set home when sleeping

# Economy
economy:
  starting-balance: 100.0        # Starting balance for new players

# Performance Optimization
performance:
  auto-cleanup:
    enabled: true
    interval: 300                # Cleanup every 5 minutes
    tps-threshold: 15.0          # Emergency cleanup if TPS < 15
    max-items-per-chunk: 100
    max-entities-per-chunk: 50

# Command Cooldowns & Costs
command-control:
  enabled: true
  commands:
    home:
      cooldown: 60               # 60 second cooldown
      cost: 10.0                 # 10 currency cost
      bypass-permission: ecore.home.bypass
```

#### `discordconf.yml`
Discord bot integration configuration:
- Bot token and channel IDs
- Rich embeds and webhook settings
- Chat filtering and moderation
- Role-based permissions
- Server status channel
- Scheduled reports

#### `scoreboard.yml`
Custom scoreboard configuration:
- Enable/disable scoreboard
- Title and lines
- Update interval
- Placeholder support

**Example:**
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

#### `tablist.yml`
Custom tab list configuration:
- Enable/disable tab list
- Header and footer
- Update interval
- Multiline support

#### `jobs.yml`
Jobs system configuration:
- Job types (Miner, Farmer, Hunter, Builder, Fisher, etc.)
- Actions and experience per action
- Rewards (money and items)
- Level requirements

#### `quests.yml`
Quests system configuration:
- 100+ predefined quests
- Quest types (Kill, Collect, Craft, Break, Place, Fish, Breed, Travel, Eat, Enchant, Trade, Mine, Harvest, Custom)
- Quest chains and prerequisites
- Rewards (money, items, experience)
- Daily/weekly quests

#### `enchantments.yml`
Custom enchantments configuration:
- 90+ unique enchantments
- Enchantment types (Weapons, Armor, Tools, Bows/Crossbows, Fishing Rods)
- Effect configurations
- Level scaling

For detailed configuration options, see the [Documentation Index](docs/index.md) or individual system documentation files.

---

## 🔗 Discord Integration

ECore includes advanced Discord integration for chat bridging, staff logs, and server management.

### Setup Instructions

#### Step 1: Create a Discord Bot
1. Go to [Discord Developer Portal](https://discord.com/developers/applications) and create a new application
2. Navigate to the "Bot" tab and click "Add Bot"
3. Copy the bot token (you'll need this for `discordconf.yml`)
4. Enable the following **Privileged Gateway Intents**:
   - ✅ Server Members Intent
   - ✅ Message Content Intent
5. Under "OAuth2" → "URL Generator":
   - Select scope: `bot`
   - Select permissions: Send Messages, Read Message History, Embed Links, Attach Files, Use Slash Commands, Manage Messages
   - Copy the generated URL and invite the bot to your server

#### Step 2: Get Channel IDs
1. Enable Developer Mode in Discord (Settings → Advanced → Developer Mode)
2. Right-click on the channel you want to use for chat bridging
3. Click "Copy ID" and paste it into `discordconf.yml` as `channel-id`
4. Repeat for punishment logs channel and staff logs channel

#### Step 3: Configure discordconf.yml
1. Set `discord.enabled: true`
2. Paste your bot token into `discord.bot-token`
3. Paste your channel IDs into the appropriate fields
4. Configure additional features as needed

### Discord Features

- **Chat Bridging**: Two-way chat between Minecraft and Discord
- **Message Filtering**: Word filtering and rate limiting
- **@player Mentions**: Mention support in Discord
- **Rich Embeds**: Beautiful formatted messages
- **Staff Logs**: Comprehensive logging system
- **Slash Commands**: Full server management from Discord
- **Account Linking**: Link Discord accounts to Minecraft players
- **Server Status**: Live status channel updates
- **Scheduled Reports**: Automatic server statistics reports

### Discord Slash Commands

- `/serverinfo` - View server status, TPS, memory, uptime
- `/online` - List all online players
- `/playerinfo <player>` - Get detailed player information
- `/report <player> <reason>` - Report players from Discord
- `/link <code>` - Link Discord account to Minecraft
- `/unlink` - Unlink Discord account
- `/staff <action> <player> [reason]` - Execute staff actions (ban, kick, mute, etc.)
- `/execute <command>` - Execute console commands (admin only)

For detailed Discord setup instructions, see the [Discord Integration Documentation](docs/discord-integration.md).

---

## 📊 PlaceholderAPI

ECore provides extensive PlaceholderAPI support. Use `%ecore_<placeholder>%` in any PlaceholderAPI-supported plugin.

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

ECore automatically registers PlaceholderAPI expansion when PlaceholderAPI is installed. No configuration needed.

---

## 📚 Features Guide

### Home System

The home system allows players to set multiple named homes and teleport to them. Features include:
- Setting homes with `/sethome <name>`
- Teleporting with `/home [name]` or GUI
- Home sharing with friends
- Home categories, icons, and descriptions
- Bed spawn integration
- Teleport costs, cooldowns, and warmup

### Economy System

ECore includes a complete, self-contained economy system:
- Starting balance for new players
- Balance commands (`/balance`, `/bal`, `/money`)
- Payments between players (`/pay`)
- Economy administration (`/economy`)
- Leaderboards (`/baltop`)
- Bank system with multiple accounts and interest rates
- Shop and auction house integration

### Shop System

ECore includes both Admin Shops and Player Shops:
- **Admin Shops**: Server-controlled shops with unlimited stock
- **Player Shops**: Player-owned shops using chest storage
- Shop categories, favorites, and statistics
- Automatic expiration of inactive shops
- Shop GUI for browsing and searching
- Right-click to buy, left-click to sell

### Staff Management

Complete staff moderation tools:
- Staff GUI (`/ecore staff`) with all moderation tools
- Player moderation (ban, kick, mute, freeze)
- Vanish system without potion effects
- Command spy and social spy
- Inventory inspection and management
- Report management system
- Item management (give, enchant, repair)
- Chat management (slow mode, clear, toggle)

### Staff Mode

Enter staff mode for safe monitoring:
- Toggle with `/staffmode` or `/sm`
- Auto-vanish, auto-fly, and invincibility
- Night vision effect
- Automatic game mode switch (default: SPECTATOR)
- Block, item, and inventory protection

### Block Logging System

CoreProtect-like block logging and rollback:
- Comprehensive logging of block breaks, places, and container access
- Rollback system for player actions or specific areas
- Inventory protection and rollback
- Inspector tool for block history
- SQLite/MySQL database support
- GUI system for easy management

### Performance Optimization

Built-in lag reduction and optimization:
- Automatic cleanup of excessive entities
- TPS-based emergency cleanup
- Item stacking (merges nearby items)
- Chunk optimization
- Performance statistics
- Scheduled maintenance

### Multi-World System

Complete world management:
- Create new worlds with custom types, environments, and seeds
- World management (load, unload, delete)
- World properties configuration
- World teleportation
- Safe spawn location finding
- World information and statistics

### Portal System

Custom portals for seamless teleportation:
- Create portals from any block selection
- Automatic teleportation when entering portal blocks
- Multi-world portal support
- Custom block materials
- Permission-based access

### Region Protection

WorldGuard-like region system:
- Create protected regions from selections
- Multiple region types
- Configurable region flags (PvP, build, interact, etc.)
- Owners and members management
- Region information and management

### Friends & Party System

Social features for player interaction:
- Friend lists and requests
- Party system with private chat
- Friend and party GUIs
- Online status tracking
- Easy management commands

### Jobs System

Complete job system with progression:
- Multiple job types (Miner, Farmer, Hunter, Builder, Fisher, etc.)
- Job levels and experience
- Rewards (money and items)
- Job GUI, statistics, and leaderboards

### Quest System

Extensive quest system:
- 100+ predefined quests
- Multiple quest types (Kill, Collect, Craft, Break, Place, Fish, Breed, Travel, Eat, Enchant, Trade, Mine, Harvest, Custom)
- Quest chains and prerequisites
- Quest GUI with filtering
- Daily/weekly quests

### Custom Enchantments

90+ unique enchantments:
- Categories: Weapons, Armor, Tools, Bows/Crossbows, Fishing Rods
- Scalable by level (1-5 or 1-10)
- Item-specific enchantments
- Easy application and removal

### Player Vaults

Extra storage for players:
- Multiple vaults per player (permission-based limits)
- Vault naming and organization
- Trust system (share vaults with friends)
- 54-slot storage per vault

### Custom Scoreboard & Tab List

Visual customization:
- Custom scoreboards with PlaceholderAPI support
- Per-world and per-group scoreboards
- Custom tab list (header and footer)
- Multiline support

For detailed feature guides, see the [Documentation Index](docs/index.md) or individual system documentation files.

---

## 🐛 Troubleshooting

### Common Issues

**Plugin not loading:**
- Check that you're using Spigot/Paper 1.21 or higher
- Verify Java 17 or higher is installed
- Check server console for error messages

**Commands not working:**
- Verify permissions are set correctly
- Check that commands are registered in `plugin.yml`
- Use `/ecore reload` to reload configuration

**Discord bot not connecting:**
- Verify bot token is correct
- Ensure bot is invited to your server
- Check that required intents are enabled
- Verify channel IDs are correct

**Economy not working:**
- Check `economy.yml` exists and is valid
- Verify starting balance is set in config
- Check console for economy-related errors

**Homes not saving:**
- Check file permissions in `plugins/Ecore/`
- Verify home data file is writable
- Check console for file I/O errors

**Performance issues:**
- Enable performance optimization in config
- Adjust cleanup intervals
- Check TPS with `/serverinfo`
- Use `/serverinfo clear` to manually cleanup

**Block logging not working:**
- Verify block logging is enabled in config
- Check database connection (if using MySQL)
- Ensure database file is writable (if using SQLite)

### Getting Help

If you encounter issues not covered here:

1. Check the console for error messages
2. Review configuration files for syntax errors
3. Verify all required permissions are set
4. Check that all dependencies are installed (if using optional ones)
5. Review the GitHub repository for known issues
6. Open an issue on the GitHub repository with:
   - Server version
   - Plugin version
   - Error messages from console
   - Steps to reproduce the issue

For more troubleshooting information, see individual system documentation files in the [Documentation Index](docs/index.md).

---

## 📝 Notes

- All data is stored in YAML files by default (except block logging which uses SQLite/MySQL)
- Configuration files are automatically generated on first run
- Use `/ecore reload` to reload configuration (requires `ecore.staff`)
- PlaceholderAPI integration is automatic when PlaceholderAPI is installed
- Shop signs support color codes using `&` prefix
- Expired shops are automatically cleaned up every 6 hours
- Bank interest is calculated automatically
- World data is stored in `worlds.yml` in the plugin data folder
- Portal data is stored in `portals.yml` in the plugin data folder
- Portals automatically teleport players when they step into portal blocks
- World unloading will teleport all players in that world to the default world spawn

---

## 🆕 Recent Updates

### Version 1.0.1
- ✅ Complete shop system with buying/selling functionality
- ✅ Shop creation via signs with GUI support
- ✅ Shop statistics, categories, and favorites
- ✅ Automatic shop expiration
- ✅ Full shop editing capabilities
- ✅ Block logging system (CoreProtect-like)
- ✅ Performance optimization features
- ✅ Friends & Party system
- ✅ Custom scoreboard & tab list
- ✅ Jobs system
- ✅ Quests system (100+ quests)
- ✅ Player vaults
- ✅ Custom enchantments (90+ enchantments)
- ✅ Chat channels system
- ✅ Command cooldowns & costs
- ✅ Custom recipes
- ✅ Enhanced nickname system
- ✅ Backup system

---

## 📄 License

This plugin is provided as-is for use on Minecraft servers.

---

## 🔗 Links

- **Documentation Index**: See [docs/index.md](docs/index.md) for complete system documentation
- **API Documentation**: See [ECore/README_API.md](ECore/README_API.md) for developer API reference
- **Implementation Status**: See [ECore/STATUS.md](ECore/STATUS.md) for implementation details

---

**Version:** 1.0.1  
**Minecraft:** 1.21+  
**Java:** 17+  
**API Version:** 1.21

---

## 💬 Support

For issues, feature requests, or questions, please open an issue on the GitHub repository.

---

*Last Updated: 2026
