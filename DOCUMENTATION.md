# ECore Plugin - Complete Documentation

## 📖 Table of Contents

1. [Plugin Overview](#plugin-overview)
2. [Key Features](#key-features)
3. [Installation](#installation)
4. [Configuration](#configuration)
5. [Commands Reference](#commands-reference)
6. [Permissions Reference](#permissions-reference)
7. [Features Guide](#features-guide)
8. [Discord Integration](#discord-integration)
9. [PlaceholderAPI](#placeholderapi)
10. [Troubleshooting](#troubleshooting)

---

## Plugin Overview

**ECore** (Excrele's Core) is a comprehensive, all-in-one Spigot plugin for Minecraft 1.21+ that provides everything you need to run a modern Minecraft server. Instead of installing dozens of separate plugins, ECore combines essential features into a single, optimized, and well-integrated solution.

### What Makes ECore Stand Out?

- **🎯 All-in-One Solution**: Replaces EssentialsX, WorldEdit, CoreProtect, ClearLagg, Jobs Reborn, Quests plugins, Friends plugins, PlayerVaults, and more with a single plugin
- **⚡ Performance Optimized**: Built-in performance monitoring, automatic cleanup, and lag reduction features
- **🔒 Self-Contained Economy**: No external dependencies required - includes a complete economy system with banks, shops, and auction house
- **🎮 Modern GUI System**: Beautiful, intuitive GUIs for homes, shops, kits, mail, statistics, and more
- **🤖 Advanced Discord Integration**: Full chat bridging, staff logs, slash commands, and server status updates
- **📊 Comprehensive Logging**: CoreProtect-like block logging with rollback capabilities and inventory protection
- **🌍 Multi-World Management**: Complete world creation, management, and portal system built-in
- **🛡️ Region Protection**: WorldGuard-like region system with flags, owners, and members
- **✨ 90+ Custom Enchantments**: Extensive library of unique enchantments for all item types
- **🎯 100+ Predefined Quests**: Ready-to-use quest system with multiple quest types and chains
- **💼 Jobs System**: Complete job system with levels, experience, and rewards
- **👥 Social Features**: Friends and party systems with private chat
- **📺 Visual Customization**: Custom scoreboards, tab lists, titles, and action bars
- **🔧 Highly Configurable**: Every feature can be customized through YAML configuration files

---

## Key Features

### Core Systems
- **Home Management**: Multiple homes with sharing, categories, icons, and descriptions
- **Economy System**: Self-contained economy with banks, shops, and auction house
- **Teleportation**: Advanced teleport system with requests, random teleport, biome/structure teleport
- **Staff Tools**: Complete moderation suite with vanish, freeze, mute, and more
- **Multi-World**: Create, manage, and teleport between multiple worlds
- **Portal System**: Custom portals that automatically teleport players
- **Region Protection**: Protect areas with flags, owners, and members

### Advanced Features
- **Block Logging**: Track and rollback all block changes and inventory modifications
- **Performance Optimization**: Automatic entity cleanup, item stacking, and chunk optimization
- **Discord Integration**: Chat bridging, staff logs, slash commands, and server status
- **Custom Enchantments**: 90+ unique enchantments across all item types
- **Quest System**: 100+ predefined quests with chains, daily/weekly quests
- **Jobs System**: Multiple job types with levels, experience, and rewards
- **Friends & Parties**: Social features with friend lists and party chat
- **Player Vaults**: Multiple extra storage vaults per player
- **Custom Recipes**: Create shaped and shapeless custom recipes
- **Chat Channels**: Multiple chat channels (global, local, trade, help, staff)
- **Scoreboard & Tab List**: Fully customizable with PlaceholderAPI support

### Quality of Life
- **GUI System**: Beautiful GUIs for homes, shops, kits, mail, statistics, achievements, and more
- **Command Cooldowns & Costs**: Configure cooldowns and economy costs for any command
- **Statistics Tracking**: Track kills, deaths, distance, items crafted, and more
- **Achievement System**: Unlockable achievements with rewards
- **Mail System**: Send and receive mail between players
- **Kit System**: Create and manage kits with cooldowns
- **AFK System**: Automatic AFK detection and manual toggle
- **Jail System**: Jail players with configurable locations and durations
- **Report System**: Player reporting with staff management
- **Nickname System**: Enhanced nicknames with colors and formatting

---

## Installation

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
   - Download `Ecore-1.0.jar` from the releases page

2. **Install the Plugin**
   - Place `Ecore-1.0.jar` in your server's `plugins` folder

3. **Start the Server**
   - Start or restart your server
   - The plugin will generate configuration files automatically

4. **Configure the Plugin**
   - Navigate to `plugins/Ecore/`
   - Edit configuration files as needed (see [Configuration](#configuration) section)
   - Restart the server or use `/ecore reload` (requires `ecore.staff` permission)

5. **Set Up Discord Integration (Optional)**
   - See [Discord Integration](#discord-integration) section for detailed setup instructions

### First-Time Setup Checklist

- [ ] Verify plugin loaded successfully (check console for "Ecore plugin has been enabled!")
- [ ] Review `config.yml` and adjust settings as needed
- [ ] Set up Discord integration if desired (see Discord Integration section)
- [ ] Configure permissions using your permission plugin (LuckPerms, PermissionsEx, etc.)
- [ ] Test basic commands (`/home`, `/balance`, `/warp`)
- [ ] Set server spawn with `/setspawn`
- [ ] Create a few warps with `/setwarp <name>`

---

## Configuration

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

See [Discord Integration](#discord-integration) for detailed setup.

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

**Example:**
```yaml
enabled: true
update-interval: 20
header: "&6&lWelcome to Server!"
footer: "&7Visit our website!"
```

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

#### `economy.yml`
Economy data storage (auto-generated, do not edit manually)

#### `playershops.yml`
Player shop data storage (auto-generated)

#### `adminshops.yml`
Admin shop data storage (auto-generated)

#### `worlds.yml`
World data storage (auto-generated)

#### `portals.yml`
Portal data storage (auto-generated)

#### `recipes.yml`
Custom recipes storage (auto-generated)

---

## Commands Reference

### Main Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/ecore [reload\|staff\|home]` | Main plugin command | `ecore.staff` (reload/staff), `ecore.home` (home) | `op` / `true` |

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
| `/cut` | Cut selection to clipboard | `ecore.worldedit.copy` | `op` |
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

### Backup Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/backup create` | Create a backup | `ecore.backup.create` | `op` |
| `/backup list` | List all backups | `ecore.backup.list` | `op` |
| `/backup restore <name>` | Restore a backup | `ecore.backup.restore` | `op` |
| `/backup reload` | Reload backup configuration | `ecore.backup.reload` | `op` |

---

## Permissions Reference

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

### Backup Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.backup.create` | Create backups | `op` |
| `ecore.backup.list` | List backups | `op` |
| `ecore.backup.restore` | Restore backups | `op` |
| `ecore.backup.reload` | Reload backup configuration | `op` |

---

## Features Guide

### Home System

The home system allows players to set multiple named homes and teleport to them. Features include:

- **Setting Homes**: Use `/sethome <name>` to set a home at your current location
- **Teleporting**: Use `/home [name]` to teleport to a home, or `/home` to open the GUI
- **Home Sharing**: Share homes with friends using `/homeshare <home> <player>`
- **Home Categories**: Organize homes into categories with `/homecategory <home> <category>`
- **Home Icons**: Set custom icons for homes with `/homeicon <home> <material>`
- **Home Descriptions**: Add descriptions to homes with `/homedescription <home> <description>`
- **Bed Spawn Integration**: Beds can automatically set homes when enabled in config
- **Teleport Costs**: Configure economy costs for home teleportation
- **Cooldowns**: Set cooldowns between home teleports
- **Warmup**: Add warmup delays that cancel if the player moves

### Economy System

ECore includes a complete, self-contained economy system:

- **Starting Balance**: New players receive a configurable starting balance
- **Balance Commands**: Check balance with `/balance`, `/bal`, or `/money`
- **Payments**: Pay other players with `/pay <player> <amount>`
- **Economy Admin**: Staff can give, take, or set player balances with `/economy`
- **Leaderboards**: View top players with `/baltop`
- **Bank System**: Create multiple bank accounts with interest rates
- **Shop Integration**: Economy is integrated with shops and auction house
- **Vault Integration**: Optional integration with Vault-compatible economy plugins

### Bank System

Players can create multiple bank accounts:

- **Create Accounts**: `/bank create <name>` to create a new account
- **Deposit/Withdraw**: Manage money with `/bank deposit` and `/bank withdraw`
- **Transfers**: Transfer money between accounts with `/bank transfer`
- **Interest Rates**: Accounts can earn interest (configurable per account)
- **Account Management**: List, view balance, and delete accounts

### Shop System

ECore includes three types of shops: GUI Shops, Admin Shops, and Player Shops:

**GUI Shops:**
- Server-controlled GUI-based shop system
- Interactive interface for browsing items by category
- **Dynamic Pricing System**: Automatic price adjustment based on supply and demand
- Prices adjust in real-time as items are bought and sold
- Price indicators show percentage changes from base prices
- Configurable inflation/deflation rates
- Price bounds to prevent extreme values

**Admin Shops:**
- Server-controlled shops with unlimited stock
- Created by placing signs with specific format
- Prices and quantities can be edited with `/shopedit`
- No storage required - items are generated

**Player Shops:**
- Player-owned shops using chest storage
- Players can create shops by placing signs
- Shop categories, favorites, and statistics
- Automatic expiration of inactive shops
- Shop GUI for browsing and searching

### Staff Management

Complete staff moderation tools:

- **Staff GUI**: Access via `/ecore staff` - provides easy access to all staff tools
- **Player Moderation**: Ban, kick, mute, freeze players
- **Vanish System**: Complete invisibility without potion effects
- **Command Spy**: Monitor player commands
- **Social Spy**: Monitor private messages
- **Inventory Inspection**: View and manage player inventories
- **Report Management**: View and resolve player reports
- **Item Management**: Give items, enchant items, repair items
- **Chat Management**: Control chat slow mode and chat state

### Staff Mode

Enter staff mode for safe monitoring:

- **Toggle**: Use `/staffmode` or `/sm` to enter/exit staff mode
- **Auto-Vanish**: Automatically vanish when entering staff mode
- **Auto-Fly**: Automatically enable flight
- **Invincible**: Become invincible to all damage
- **Night Vision**: Automatic night vision effect
- **Game Mode**: Automatically switch to configured game mode (default: SPECTATOR)
- **Block Protection**: Prevent block breaking, placing, and interaction
- **Item Protection**: Prevent item pickup and dropping
- **Inventory Protection**: Prevent inventory editing

### Block Logging System

CoreProtect-like block logging and rollback:

- **Comprehensive Logging**: Logs block breaks, places, container access, and inventory changes
- **Rollback System**: Rollback player actions or specific areas
- **Inventory Protection**: Track and rollback player inventories to snapshots
- **Inspector Tool**: Right-click blocks to view their history
- **Database Support**: SQLite (default) or MySQL for efficient log storage
- **GUI System**: Easy-to-use interfaces for browsing logs and performing rollbacks
- **Automatic Purging**: Configurable log retention with automatic cleanup

### Performance Optimization

Built-in lag reduction and optimization:

- **Automatic Cleanup**: Removes excessive entities (items, mobs, projectiles)
- **TPS-Based Cleanup**: Automatically triggers when TPS drops below threshold
- **Item Stacking**: Merges nearby items of the same type
- **Chunk Optimization**: Unloads empty chunks to reduce memory usage
- **Performance Statistics**: Detailed metrics and entity breakdown
- **Scheduled Maintenance**: Automatic cleanup on configurable intervals

### Multi-World System

Complete world management:

- **Create Worlds**: Create new worlds with custom types, environments, and seeds
- **World Management**: Load, unload, and delete worlds dynamically
- **World Properties**: Configure spawn locations, difficulty, PVP, and more
- **World Teleportation**: Seamlessly teleport players between worlds
- **Safe Spawn**: Automatic safe location finding when teleporting
- **World Information**: View detailed information about any world
- **Auto-Load Configuration**: Configure which worlds load automatically

### Portal System

Custom portals for seamless teleportation:

- **Create Portals**: Create portals from any block selection
- **Automatic Teleportation**: Players automatically teleport when entering portal blocks
- **Multi-World Support**: Portals can bridge players between different worlds
- **Custom Materials**: Use any block material for portals
- **Permission-Based Access**: Control who can use specific portals
- **Custom Messages & Sounds**: Configure portal teleportation messages and sounds

### Region Protection

WorldGuard-like region system:

- **Region Creation**: Create protected regions from selections
- **Region Types**: Multiple region types with different properties
- **Region Flags**: Configure region behavior (PvP, build, interact, etc.)
- **Owners & Members**: Manage region access
- **Region Info**: View detailed region information
- **Region Management**: List, delete, and reload regions

### Friends & Party System

Social features for player interaction:

- **Friend Lists**: Add, remove, and manage friends
- **Friend Requests**: Send, accept, and deny friend requests
- **Friend GUI**: Browse friends and pending requests
- **Party System**: Create and manage parties/teams
- **Party Chat**: Private chat channel for party members
- **Party Management**: Invite, kick, and leave parties
- **Party GUI**: Easy party management interface
- **Online Status**: See which friends/party members are online

### Jobs System

Complete job system with progression:

- **Multiple Job Types**: Miner, Farmer, Hunter, Builder, Fisher, and more
- **Job Levels & Experience**: Level up through job-specific actions
- **Job Rewards**: Earn money and items from completing job actions
- **Job GUI**: Easy-to-use interface for browsing and joining jobs
- **Job Statistics**: Track your progress and earnings
- **Job Leaderboards**: Compare with other players
- **Job Progression**: Exponential leveling system with configurable rewards

### Quest System

Extensive quest system:

- **100+ Predefined Quests**: Extensive quest library included
- **Quest Types**: Kill, Collect, Craft, Break, Place, Fish, Breed, Travel, Eat, Enchant, Trade, Mine, Harvest, and Custom quests
- **Quest Chains**: Quests with prerequisites and chains
- **Quest Rewards**: Money, items, and experience rewards
- **Quest GUI**: Browse quests by category with filtering
- **Daily/Weekly Quests**: Automatically resetting quests
- **Quest Progress Tracking**: Real-time progress updates

### Chat Channels System

Multiple chat channels:

- **Multiple Channels**: Global, Local, Trade, Help, Staff channels
- **Channel Switching**: Easy channel management
- **Channel Permissions**: Per-channel permission support
- **Channel Prefixes**: Color-coded channel prefixes
- **Range-Based Local Chat**: Configurable range for local channels
- **Channel Muting**: Mute specific channels per player
- **Auto-Join**: Auto-join to default channel on login

### Player Vaults System

Extra storage for players:

- **Multiple Vaults**: Permission-based vault limits (1-10 vaults)
- **Vault GUI**: Easy vault selection and management
- **Vault Naming**: Custom names for each vault
- **Trust System**: Share vaults with friends
- **54-Slot Storage**: Each vault has 54 slots (6 rows)

### Custom Enchantments

90+ unique enchantments:

- **Weapons**: 18 enchantments (Lifesteal, Venom, Wither, Lightning, etc.)
- **Armor**: 18 enchantments (Regeneration, Absorption, Thorns Plus, etc.)
- **Tools**: 18 enchantments (Auto Smelt, Vein Miner, Tree Feller, etc.)
- **Bows/Crossbows**: 18 enchantments (Explosive Arrows, Homing, Teleport Arrows, etc.)
- **Fishing Rods**: 18 enchantments (Treasure Hunter, Double Catch, Fish Finder, etc.)
- **Scalable by Level**: All enchantments scale with level (1-5 or 1-10)
- **Item-Specific**: Enchantments can only be applied to applicable items

### Custom Scoreboard & Tab List

Visual customization:

- **Custom Scoreboards**: Fully customizable scoreboard with placeholders
- **Placeholder Support**: ECore and PlaceholderAPI placeholders
- **Per-World Scoreboards**: Different scoreboards for different worlds (optional)
- **Per-Group Scoreboards**: Different scoreboards for permission groups (optional)
- **Custom Tab List**: Customizable header and footer
- **Multiline Support**: Support for multiline headers and footers
- **Separate Config Files**: `scoreboard.yml` and `tablist.yml` for easy editing

### Custom Recipes

Create custom crafting recipes:

- **Shaped Recipes**: Create shaped crafting recipes
- **Shapeless Recipes**: Create shapeless crafting recipes
- **Recipe Management**: Create, remove, and list recipes
- **Recipe Permissions**: Per-recipe permission support
- **Hot-Reload**: Reload recipes without restart

### Command Cooldowns & Costs

Control command usage:

- **Per-Command Cooldowns**: Set cooldowns for any command (in seconds)
- **Per-Command Economy Costs**: Charge players for using commands
- **Bypass Permissions**: Configurable bypass permissions per command
- **Easy Configuration**: Setup in `config.yml` under `command-control.commands`

---

## Discord Integration

ECore includes comprehensive Discord integration for chat bridging, staff logs, and server management.

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
   - Select permissions:
     - Send Messages
     - Read Message History
     - Embed Links
     - Attach Files
     - Use Slash Commands
     - Manage Messages (for filtering)
   - Copy the generated URL and invite the bot to your server

#### Step 2: Get Channel IDs

1. Enable Developer Mode in Discord (Settings → Advanced → Developer Mode)
2. Right-click on the channel you want to use for chat bridging
3. Click "Copy ID" and paste it into `discordconf.yml` as `channel-id`
4. Repeat for punishment logs channel (`punishment-channel-id`)
5. Optionally set up a separate staff logs channel (`staff-logs-channel-id`)

#### Step 3: Configure discordconf.yml

1. Set `discord.enabled: true`
2. Paste your bot token into `discord.bot-token`
3. Paste your channel IDs into the appropriate fields
4. Configure additional features as needed:
   - Enable/disable rich embeds
   - Set up chat filtering
   - Configure role-based permissions
   - Enable scheduled reports
   - Set up status channel updates

#### Step 4: Configure Role Permissions

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

#### Step 5: Account Linking (Optional)

Players can link their Discord accounts to Minecraft:

1. In-game: `/link` - Generates a verification code
2. In Discord: `/link <code>` - Links the accounts
3. Use `/unlink` in Discord to unlink

Linked accounts will show Discord names in embeds and enable enhanced features.

#### Step 6: Restart Server

After configuration, restart your server. The Discord bot will connect automatically if configured correctly.

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

---

## PlaceholderAPI

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

### Integration

ECore automatically registers PlaceholderAPI expansion when PlaceholderAPI is installed. No configuration needed.

---

## Troubleshooting

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

---

## Additional Notes

- All data is stored in YAML files by default (except block logging which uses SQLite/MySQL)
- Discord bot requires valid token and channel IDs
- PlaceholderAPI integration is automatic when PlaceholderAPI is installed
- Vault, WorldGuard, and LuckPerms integrations are optional and use reflection
- Configuration files are automatically validated and migrated on startup
- Expired shops are automatically cleaned up every 6 hours
- Bank interest is calculated automatically
- World data is stored in `worlds.yml` in the plugin data folder
- Portal data is stored in `portals.yml` in the plugin data folder
- Portals automatically teleport players when they step into portal blocks
- World unloading will teleport all players in that world to the default world spawn

---

## Version Information

**Version:** 1.0  
**Minecraft:** 1.21+  
**Java:** 17+  
**API Version:** 1.21

---

**Last Updated:** 2024  
**Documentation Version:** 1.0

