# Staff Management

Complete staff moderation tools for server administration and player management.

## Overview

ECore provides a comprehensive staff management system with a beautiful GUI interface and all the tools needed for server moderation, player management, and administrative tasks.

## Features

- **Staff GUI**: Easy access to all moderation tools
- **Player Moderation**: Ban, kick, mute, freeze players
- **Vanish System**: Complete invisibility without potion effects
- **Command Spy**: Monitor player commands
- **Social Spy**: Monitor private messages
- **Inventory Inspection**: View and manage player inventories
- **Report Management**: View and resolve player reports
- **Item Management**: Give items, enchant items, repair items
- **Chat Management**: Control chat slow mode and chat state

## Commands

### Main Staff Command

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/ecore staff` | Open staff GUI | `ecore.staff` | `op` |

### Player Moderation

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/mute <player> [duration]` | Mute a player | `ecore.staff` | `op` |
| `/unmute <player>` | Unmute a player | `ecore.staff` | `op` |
| `/freeze <player>` | Freeze a player | `ecore.staff` | `op` |
| `/unfreeze <player>` | Unfreeze a player | `ecore.staff` | `op` |
| `/ban <player> [reason]` | Ban a player | `ecore.staff` | `op` |
| `/kick <player> [reason]` | Kick a player | `ecore.staff` | `op` |

### Spy Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/commandspy` | Toggle command spy | `ecore.staff` | `op` |
| `/socialspy` | Toggle social spy | `ecore.staff` | `op` |

### Item Management

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/give <player> <item> [amount]` | Give item to player | `ecore.staff` | `op` |
| `/enchant <player> <enchantment> <level>` | Enchant player's item | `ecore.staff` | `op` |
| `/repair [all]` | Repair items | `ecore.staff` | `op` |

### Chat Management

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/chat <on\|off\|clear>` | Manage chat | `ecore.chat.manage` | `op` |
| `/chatslow <seconds>` | Set chat slow mode | `ecore.staff` | `op` |
| `/sc <message>` | Staff chat | `ecore.staff` | `op` |
| `/staffchat <message>` | Alias for sc | `ecore.staff` | `op` |
| `/ac <message>` | Admin chat | `ecore.admin` | `op` |
| `/adminchat <message>` | Alias for ac | `ecore.admin` | `op` |

### Vanish

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/vanish` | Toggle vanish mode | `ecore.vanish` | `op` |

## Staff GUI

The staff GUI (`/ecore staff`) provides easy access to:

- **Player Management**: View online players, teleport, inspect inventories
- **Moderation Tools**: Ban, kick, mute, freeze players
- **Report Management**: View and resolve player reports
- **Item Tools**: Give items, enchant items, repair items
- **Chat Tools**: Manage chat, set slow mode
- **Server Tools**: Server information, performance stats

## Usage Guide

### Accessing Staff Tools

1. Use `/ecore staff` to open the staff GUI
2. Navigate through the GUI to access different tools
3. Click on players or options to perform actions

### Muting Players

1. Use `/mute <player>` to mute a player indefinitely
2. Use `/mute <player> <duration>` to mute for a specific time (e.g., `1h`, `30m`)
3. Use `/unmute <player>` to unmute

### Freezing Players

1. Use `/freeze <player>` to freeze a player (prevents movement)
2. Use `/unfreeze <player>` to unfreeze

### Vanish Mode

1. Use `/vanish` to toggle vanish mode
2. You become invisible to all players
3. No potion effects are shown (unlike invisibility potions)
4. Use `/vanish` again to become visible

### Command Spy

1. Use `/commandspy` to toggle command spy
2. You will see all commands executed by players
3. Useful for monitoring suspicious activity

### Social Spy

1. Use `/socialspy` to toggle social spy
2. You will see all private messages between players
3. Useful for monitoring communication

### Inventory Inspection

1. Open the staff GUI (`/ecore staff`)
2. Click on a player
3. Select "Inspect Inventory"
4. View and manage the player's inventory

### Chat Management

- `/chat on` - Enable chat
- `/chat off` - Disable chat
- `/chat clear` - Clear all chat messages
- `/chatslow <seconds>` - Set chat slow mode (players can only chat once per X seconds)

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.staff` | Access staff commands and GUI | `op` |
| `ecore.vanish` | Use vanish mode | `op` |
| `ecore.chat.manage` | Manage chat | `op` |
| `ecore.admin` | Admin chat access | `op` |

## Tips

- Use the staff GUI for quick access to all tools
- Use vanish mode for invisible monitoring
- Enable command spy to catch rule breakers
- Use social spy to monitor private communications
- Freeze players before investigating suspicious activity
- Use chat slow mode to prevent spam

## Related Systems

- [Staff Mode](staff-mode.md) - For safe monitoring mode
- [Report System](report-system.md) - For handling player reports
- [Discord Integration](discord-integration.md) - For staff action logging

