# Excrele's Core (Ecore)

A comprehensive Spigot plugin for Minecraft 1.21+, providing staff moderation tools, a home management system, a player reporting system, a self-contained economy, shop systems, Discord integration, and extensive gameplay enhancements.

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

### 🎮 Server Management
- **Server Info**: `/serverinfo` command with detailed metrics
- **TPS Monitoring**: Real-time TPS tracking
- **Memory Usage**: Monitor server memory
- **Performance Metrics**: Track server performance
- **World Information**: View loaded chunks, entities, world list
- **Chunk Pregeneration**: Pregenerate chunks in a radius from spawn to improve server performance

### 🔗 Discord Integration
- **Chat Bridging**: Two-way chat between Minecraft and Discord
- **Rich Embeds**: Beautiful formatted messages in Discord
- **Staff Logs**: Automatic logging of staff actions
- **Player Notifications**: Join/leave notifications (optional)
- **Server Status**: Server start/stop notifications

### 🛠️ WorldEdit Integration
- **Selection Tools**: Wand, pos1, pos2 for area selection
- **Block Operations**: Set, replace, clear, walls, hollow
- **Clipboard Operations**: Copy, paste, cut selections
- **History**: Undo and redo operations
- **Schematics**: Save, load, list, and delete schematics
- **Brush Tools**: Create spheres and cylinders
- **Selection Info**: View selection details

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
4. For Discord integration, set up the bot token in `discordconf.yml`

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
```

### discordconf.yml
```yaml
discord:
  enabled: false  # Enable Discord bot
  bot-token: "INSERT_TOKEN_HERE"
  channel-id: "INSERT_CHANNEL_ID"
  punishment-channel-id: "INSERT_PUNISHMENT_CHANNEL_ID"
  use-rich-embeds: true  # Use rich embeds for messages
  notify-player-join: false  # Send join notifications
  notify-player-leave: false  # Send leave notifications
```

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

### Shop Commands
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/shopedit <buy\|sell\|quantity> <value>` | Edit shop prices or quantity | `ecore.adminshop.edit` or `ecore.pshop.edit` | `op` |

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

---

## 🐛 Support

For issues, feature requests, or questions, please open an issue on the GitHub repository.

---

## 📄 License

This plugin is provided as-is for use on Minecraft servers.

---

**Version:** 1.0  
**Minecraft:** 1.21+  
**Java:** 17+
