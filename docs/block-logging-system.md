# Block Logging System

CoreProtect-like block logging with rollback capabilities and inventory protection.

## Overview

The block logging system tracks all block changes, container access, and inventory modifications, allowing administrators to investigate griefing, rollback changes, and restore player inventories.

## Features

- **Comprehensive Logging**: Logs block breaks, places, container access, and inventory changes
- **Rollback System**: Rollback player actions or specific areas
- **Inventory Protection**: Track and rollback player inventories to snapshots
- **Inspector Tool**: Right-click blocks to view their history
- **Database Support**: SQLite (default) or MySQL for efficient log storage
- **GUI System**: Easy-to-use interfaces for browsing logs and performing rollbacks
- **Automatic Purging**: Configurable log retention with automatic cleanup

## Commands

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

## Configuration

Block logging is configured in `config.yml`:

```yaml
block-logging:
  enabled: true                   # Enable block logging
  log-block-break: true          # Log block breaks
  log-block-place: true          # Log block places
  log-container-access: true     # Log container access (chests, etc.)
  log-inventory-changes: true    # Log inventory changes
  database:
    type: SQLITE                  # SQLITE or MYSQL
    host: localhost               # MySQL host (if using MySQL)
    port: 3306                   # MySQL port
    database: ecore_blocklog     # Database name
    username: root                # MySQL username
    password: password            # MySQL password
  retention-days: 30             # Days to keep logs (0 = forever)
```

## Usage Guide

### Viewing Logs

**Player Logs:**
1. Use `/blocklog lookup <player>` to view all actions by a player
2. Use `/blocklog lookup <player> <time>` to view actions within a time period
3. Time format examples: `1h` (1 hour), `2d` (2 days), `1w` (1 week)

**Area Logs:**
1. Use the inspector tool (see below) to view block history
2. Right-click a block to see its change history

### Rollback Operations

**Player Rollback:**
1. Use `/blocklog rollback <player>` to rollback all actions by a player
2. Use `/blocklog rollback <player> <time>` to rollback actions within a time period
3. All blocks changed by the player will be restored

**Area Rollback:**
1. Select an area using WorldEdit selection tools
2. Use `/blocklog restore <time>` to restore the area
3. All blocks in the selection will be restored to their state at that time

**Inventory Rollback:**
1. Use `/blocklog inventory <player>` to rollback a player's inventory
2. Use `/blocklog inventory <player> <time>` to restore inventory from a specific time
3. The player's inventory will be restored to a snapshot

### Inspector Tool

1. Use `/blocklog inspect` to get the inspector wand
2. Right-click any block to view its history
3. See who placed/broke it and when
4. View container access history

### Purging Logs

1. Use `/blocklog purge` to purge logs older than the retention period
2. Use `/blocklog purge <days>` to purge logs older than specified days
3. This helps manage database size

## Block Logging GUI

The block logging GUI provides:
- Easy log browsing
- Filter by player, time, or action type
- Visual rollback interface
- Quick access to common operations

## Database

### SQLite (Default)

- No configuration needed
- Database file: `plugins/Ecore/blocklog.db`
- Suitable for small to medium servers

### MySQL

- Configure database connection in `config.yml`
- Better performance for large servers
- Supports multiple servers sharing logs

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.blocklog.use` | Use block logging commands | `op` |
| `ecore.blocklog.lookup` | Lookup block logs | `op` |
| `ecore.blocklog.rollback` | Rollback blocks | `op` |
| `ecore.blocklog.restore` | Restore blocks | `op` |
| `ecore.blocklog.inspect` | Use inspector tool | `op` |
| `ecore.blocklog.inventory` | Rollback inventories | `op` |
| `ecore.blocklog.purge` | Purge old logs | `op` |

## Tips

- Enable block logging for grief protection
- Use inspector tool to investigate suspicious blocks
- Rollback player actions to undo griefing
- Use inventory rollback to restore stolen items
- Purge old logs regularly to manage database size
- Use MySQL for better performance on large servers

## Related Systems

- [WorldEdit](worldedit.md) - For area selection
- [Staff Management](staff-management.md) - For moderation tools

