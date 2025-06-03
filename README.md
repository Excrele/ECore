Excrele's Core (Ecore)
A modular Spigot plugin for Minecraft 1.21.5 providing staff moderation tools, a home system, and basic gameplay enhancements.
Features
Staff Commands

GUI: Access via /ecore staff (requires ecore.staff permission)
Functionalities:
Ban players
Kick players
Inspect player inventories
View and resolve player reports


All actions are permission-based (ecore.staff)

Home System

Commands:
/sethome [name]: Set a home location
/home [name]: Teleport to a home
/ecore home: Open home GUI for teleporting, deleting, or renaming homes


Configurable: Set maximum homes in config.yml
GUI Features:
List and teleport to homes
Set new homes
Delete homes via submenu
Rename homes via submenu


Permission: ecore.home (default: true)

Basic Functions

Nicknames: Set with /nick (requires ecore.nickname, default: true)
Colored Chat: Use & color codes (requires ecore.colorchat, default: true)
Colored Signs: Use & color codes (requires ecore.colorsign, default: true)
Stair Sitting: Right-click stairs to sit (requires ecore.sit, default: true)

Installation

Place Ecore.jar in your server's plugins folder.
Restart the server.
Configure settings in plugins/Ecore/config.yml.

Configuration

config.yml:home:
max-homes: 3 # Maximum number of homes per player
report:
max-reports: 5 # Maximum number of reports a player can submit
report-cooldown: 300 # Cooldown in seconds between reports



Commands

/ecore reload: Reload configuration (requires ecore.staff)
/ecore staff: Open staff GUI (requires ecore.staff)
/ecore home: Open home GUI (requires ecore.home)
/sethome [name]: Set a home
/home [name]: Teleport to a home

Permissions

ecore.staff: Access to staff commands and GUI
ecore.home: Access to home system
ecore.nickname: Set nicknames
ecore.colorchat: Use color codes in chat
ecore.colorsign: Use color codes on signs
ecore.sit: Sit on stairs

Development

Modular Design: Organized into managers (ConfigManager, HomeManager, StaffManager, ReportManager) and listeners for easy extension.
Future Extensions: Add new commands, listeners, or managers in respective packages.

Notes

Reports are stored in reports.yml and managed via the staff GUI.
Homes are stored in homes.yml in the plugin's data folder.
Ensure Minecraft 1.21.5 is used for compatibility.

