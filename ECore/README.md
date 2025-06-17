Excrele's Core (Ecore)
A modular Spigot plugin for Minecraft 1.21.5, providing staff moderation tools, a home management system, a player reporting system, a self-contained economy, shop systems, Discord integration, and basic gameplay enhancements.
Features
Economy System

Self-Contained: No Vault dependency, with player balances stored in economy.yml.
Functionalities:
Players start with a configurable balance (default: 100).
Supports balance queries, deposits, withdrawals, and transfers.
API for other plugins (EcoreEconomy) to access economy functions.


Configuration: Set starting balance in config.yml (economy.starting-balance).
Usage: Integrated with Admin Shops and Player Shops for transactions.

Admin Shops

Creation (requires ecore.adminshop permission, default: op):
Place a sign with [Admin Shop] on the first line.
Right-click the sign with the item to sell.
Enter quantity (1–64), buy price (cost to players), and sell price (paid to players) via chat.
Sign updates to show item, quantity, and prices.


Usage:
Right-click sign to buy (deducts buy price, gives items).
Left-click sign to sell (deducts items, adds sell price).


Storage: Shop data stored in adminshops.yml.
Logging: Creation logged to Discord punishment channel.

Player Shops

Creation (requires ecore.pshop permission, default: true):
Place a chest, then a sign on it with [PShop] on the first line.
Drop the item into the chest to set the shop’s item.
Enter quantity (1–64), buy price (cost to players), and sell price (paid to players) via chat.
Sign updates to show item, quantity, and prices.


Usage:
Right-click sign to buy (deducts buy price, takes items from chest).
Left-click sign to sell (deducts items, adds sell price, stores items in chest).


Storage: Shop data stored in playershops.yml.
Logging: Creation logged to Discord punishment channel.

Staff Management

GUI: Access via /ecore staff (requires ecore.staff permission).
Functionalities:
Ban players (prompts for target player name in chat).
Kick players (prompts for target player name in chat).
Inspect player inventories (prompts for target player name in chat).
View and resolve player reports.
Vanish: Toggle complete invisibility (no potion effects) with fake disconnect/join messages (requires ecore.vanish).
Teleport: Teleport to a player by entering their name in chat (requires ecore.teleport).


Punishment Logging: Sends notifications of bans, kicks, inventory inspections, vanishes, teleports, and shop creations to a configurable Discord channel.
Permission: Requires ecore.staff (default: op), with specific permissions for vanish (ecore.vanish), teleport (ecore.teleport), admin shops (ecore.adminshop), and player shops (ecore.pshop).

Home System

Commands:
/sethome: Prompts for a custom home name in chat.
/home [name]: Teleports to a named home.
/ecore home: Opens the home management GUI.


GUI Features:
List and teleport to homes.
Set new homes with custom names (prompts via chat).
Delete homes via submenu.
Rename homes via submenu (prompts for new name via chat).


Configuration: Set maximum homes in config.yml.
Permission: Requires ecore.home (default: true).

Report System

Command: /report <player> <reason> (requires ecore.report permission).
Functionalities:
Players can report others with a reason.
Staff can view and resolve reports via the staff GUI.


Configuration: Max reports and cooldown in config.yml.
Permission: Requires ecore.report (default: true).

Discord Integration

Features:
Bridges Minecraft chat with a specified Discord channel.
Sends Minecraft chat messages to Discord.
Relays Discord channel messages to Minecraft chat.
Logs staff actions (bans, kicks, inventory inspections, vanishes, teleports, shop creations) to a separate Discord channel.


Configuration: Bot token, chat channel ID, punishment channel ID, and message formats in discordconf.yml.
Setup:
Create a Discord bot at Discord Developer Portal.
Copy the bot token and paste it into discordconf.yml (discord.bot-token).
Enable the bot and set the chat channel ID (discord.channel-id) for chat bridging.
Set the punishment channel ID (discord.punishment-channel-id) for action logs.
Invite the bot to your server with permissions to send/read messages.


Requirements: JDA library (included via Maven dependency).

Basic Gameplay Enhancements

Nicknames: Set with /nick <nickname> (requires ecore.nickname, default: true).
Colored Chat: Use & color codes (requires ecore.colorchat, default: true).
Colored Signs: Use & color codes (requires ecore.colorsign, default: true).
Stair Sitting: Right-click stairs to sit (requires ecore.sit, default: true).

Installation

Place Ecore.jar in your server's plugins folder.
Restart the server to generate configuration files.
Configure settings in plugins/Ecore/config.yml, discordconf.yml, economy.yml, adminshops.yml, and playershops.yml.
For Discord integration, set up the bot token, chat channel ID, and punishment channel ID in discordconf.yml.

Configuration
config.yml
home:
max-homes: 3  # Maximum number of homes per player
report:
max-reports: 5  # Maximum number of reports a player can submit
report-cooldown: 300  # Cooldown in seconds between reports
economy:
starting-balance: 100.0  # Starting balance for new players

discordconf.yml
discord:
enabled: false  # Enable/disable Discord bot integration
bot-token: "INSERT_TOKEN_HERE"  # Discord bot token
channel-id: "INSERT_CHANNEL_ID"  # Discord text channel ID for chat
punishment-channel-id: "INSERT_PUNISHMENT_CHANNEL_ID"  # Discord text channel ID for action logs
message-formats:
minecraft-to-discord: "[Minecraft] %player%: %message%"  # Minecraft to Discord format
discord-to-minecraft: "&7[Discord] &f%user%: %message%"  # Discord to Minecraft format
punishment-log: "[Punishment] %staff% %action% %target%: %reason%"  # Punishment log format
vanish-log: "[Vanish] %staff% %action%: %reason%"  # Vanish/unvanish log format
teleport-log: "[Teleport] %staff% teleported to %target%"  # Teleport log format
adminshop-log: "[AdminShop] %staff% created admin shop for %target%: %reason%"  # Admin shop creation
playershop-log: "[PlayerShop] %staff% created player shop for %target%: %reason%"  # Player shop creation

economy.yml
economy:
starting-balance: 100.0  # Default starting balance for new players
players: {}

adminshops.yml
adminshops: {}

playershops.yml
playershops: {}

Commands

/ecore reload: Reloads configuration files (requires ecore.staff).
/ecore staff: Opens staff GUI (requires ecore.staff).
/ecore home: Opens home GUI (requires ecore.home).
/sethome: Prompts for home name in chat (requires ecore.home).
/home [name]: Teleports to a home (requires ecore.home).
/report <player> <reason>: Reports a player (requires ecore.report).

Permissions

ecore.staff: Access to staff commands and GUI (default: op).
ecore.home: Access to home system (default: true).
ecore.report: Submit player reports (default: true).
ecore.nickname: Set nicknames (default: true).
ecore.colorchat: Use color codes in chat (default: true).
ecore.colorsign: Use color codes on signs (default: true).
ecore.sit: Sit on stairs (default: true).
ecore.vanish: Toggle vanish mode (default: op).
ecore.teleport: Teleport to players (default: op).
ecore.adminshop: Create and manage Admin Shops (default: op).
ecore.pshop: Create and manage Player Shops (default: true).

Development

Modular Design: Organized into managers (ConfigManager, HomeManager, StaffManager, ReportManager, DiscordManager, EconomyManager, ShopManager) and listeners.
Dependencies: JDA for Discord integration (included via Maven).
Economy API: Access via Ecore.getInstance().getEconomyManager().new EcoreEconomy().
Future Extensions: Add shop management commands, economy commands, or enhanced shop GUIs.

Setup Discord Bot

Go to Discord Developer Portal and create a new application.
In the "Bot" tab, create a bot and copy its token.
In discordconf.yml, set discord.bot-token to the copied token.
Enable "Server Members Intent" and "Message Content Intent" in the Bot tab.
In the "OAuth2" tab, select "bot" scope, grant "Send Messages" and "Read Message History" permissions, and invite the bot to your server.
Right-click a text channel for chat bridging, copy its ID (enable Developer Mode in Discord settings), and set discord.channel-id in discordconf.yml.
Right-click a text channel for action logs, copy its ID, and set discord.punishment-channel-id in discordconf.yml.
Set discord.enabled to true and restart the server.

Notes

Reports are stored in reports.yml.
Homes are stored in homes.yml.
Economy data stored in economy.yml.
Admin Shops stored in adminshops.yml.
Player Shops stored in playershops.yml.
Chat-based inputs (home naming, punishments, teleports, shop creation) cancel normal chat.
Discord bot requires a valid token and channel IDs to function.
Vanish makes staff invisible without potion effects and sends fake disconnect/join messages.
Ensure Minecraft 1.21.5 and Java 8+ for compatibility.
JDA library is required; include via Maven or manually add to build path.

Dependencies
Add the following to your pom.xml for JDA:
<dependencies>
<dependency>
<groupId>net.dv8tion</groupId>
<artifactId>JDA</artifactId>
<version>5.0.0-beta.24</version>
<scope>compile</scope>
</dependency>
</dependencies>
<repositories>
<repository>
<id>jcenter</id>
<url>https://jcenter.bintray.com</url>
</repository>
</repositories>

