# Block Logging System Implementation

## Overview

A comprehensive block logging and rollback system has been implemented for ECore, providing CoreProtect-like functionality with inventory protection and rollback capabilities. The system includes a full GUI interface for easy access and management.

## Features Implemented

### 1. Block Logging
- ✅ Block break logging
- ✅ Block place logging
- ✅ Container access logging (chests, furnaces, etc.)
- ✅ Player action history tracking
- ✅ Configurable logging options

### 2. Inventory Logging
- ✅ Inventory change tracking
- ✅ Periodic inventory snapshots
- ✅ Inventory rollback functionality
- ✅ Snapshot management

### 3. Rollback System
- ✅ Player-based rollback (rollback all actions by a player)
- ✅ Area-based rollback (rollback blocks in a selected area)
- ✅ Time-based rollback (rollback to a specific time)
- ✅ Inventory rollback to snapshots

### 4. GUI System
- ✅ Main block logging GUI
- ✅ Lookup GUI (browse player logs)
- ✅ Inventory rollback GUI (browse and restore snapshots)
- ✅ Easy-to-use interface for all operations

### 5. Inspector Tool
- ✅ Inspector wand for checking block history
- ✅ Right-click blocks to view their log history

## Files Created

### Core Classes
1. **BlockLogDatabase.java** - Database abstraction layer
   - SQLite and MySQL support
   - Async operations for performance
   - Automatic table creation and indexing
   - Log purging functionality

2. **BlockLogManager.java** - Core logging manager
   - Block action logging
   - Container interaction logging
   - Rollback operations
   - Inspector tool management

3. **InventoryLogManager.java** - Inventory logging manager
   - Inventory change tracking
   - Snapshot creation and management
   - Inventory rollback operations

4. **BlockLogListener.java** - Event listeners
   - Block break/place events
   - Container open/close/click events
   - Inventory change events
   - Item pickup/drop events

5. **BlockLogCommand.java** - Command handler
   - `/blocklog`, `/bl`, `/co` commands
   - Lookup, rollback, restore, inspect subcommands
   - Tab completion support

6. **BlockLogGUIManager.java** - GUI manager
   - Main menu GUI
   - Lookup GUI
   - Inventory rollback GUI
   - Click event handling

## Commands

### Main Commands
- `/blocklog` - Open main GUI (or use subcommands)
- `/bl` - Alias for blocklog
- `/co` - Alias for blocklog (CoreProtect-like)

### Subcommands
- `/blocklog lookup <player> [time]` - View player's block logs
- `/blocklog rollback <player> [time]` - Rollback player's actions
- `/blocklog restore [time]` - Restore selected area
- `/blocklog inspect` - Get inspector wand
- `/blocklog inventory <player> [time]` - Rollback player inventory
- `/blocklog purge [days]` - Purge old logs
- `/blocklog reload` - Reload configuration

### Time Format
Time ranges can be specified with suffixes:
- `s` - seconds (e.g., `30s`)
- `m` - minutes (e.g., `5m`)
- `h` - hours (e.g., `1h`)
- `d` - days (e.g., `7d`)

Default: `1h` (1 hour)

## Permissions

- `ecore.blocklog.use` - Use block logging commands (default: op)
- `ecore.blocklog.lookup` - Lookup block logs (default: op)
- `ecore.blocklog.rollback` - Rollback blocks (default: op)
- `ecore.blocklog.restore` - Restore blocks (default: op)
- `ecore.blocklog.inspect` - Use inspector tool (default: op)
- `ecore.blocklog.inventory` - Rollback inventories (default: op)
- `ecore.blocklog.purge` - Purge old logs (default: op)
- `ecore.blocklog.reload` - Reload configuration (default: op)

## Configuration

Added to `config.yml`:

```yaml
block-logging:
  enabled: true
  log-block-break: true
  log-block-place: true
  log-container-access: true
  log-inventory-changes: true
  inventory-snapshots: true
  inventory-snapshot-interval: 300  # Seconds (5 minutes)
  database-type: sqlite  # sqlite or mysql
  retention-days: 30  # Keep logs for X days (0 = forever)
  mysql:
    host: localhost
    port: 3306
    database: ecore
    username: root
    password: ""
```

## Database Schema

### block_logs
- `id` - Primary key
- `time` - Timestamp (milliseconds)
- `player_uuid` - Player UUID
- `player_name` - Player name
- `action` - Action type (BREAK, PLACE, etc.)
- `world` - World name
- `x, y, z` - Block coordinates
- `material` - Block material
- `data` - Additional block data

### container_logs
- `id` - Primary key
- `time` - Timestamp
- `player_uuid` - Player UUID
- `player_name` - Player name
- `action` - Action type (OPEN, CLOSE, CLICK, etc.)
- `world, x, y, z` - Container location
- `slot` - Inventory slot
- `item_type` - Item material
- `item_amount` - Item amount
- `item_data` - Serialized item data

### inventory_logs
- `id` - Primary key
- `time` - Timestamp
- `player_uuid` - Player UUID
- `player_name` - Player name
- `action` - Action type (CLICK, DROP, PICKUP, etc.)
- `slot` - Inventory slot
- `item_type` - Item material
- `item_amount` - Item amount
- `item_data` - Serialized item data
- `inventory_type` - Inventory type (PLAYER, etc.)

### inventory_snapshots
- `id` - Primary key
- `time` - Timestamp
- `player_uuid` - Player UUID
- `player_name` - Player name
- `inventory_data` - Serialized inventory (Base64)

## Usage Examples

### Lookup Player Actions
```
/blocklog lookup Notch 1h
```
Shows all block actions by Notch in the last hour.

### Rollback Player Actions
```
/blocklog rollback Notch 30m
```
Rolls back all block changes by Notch in the last 30 minutes.

### Inspect a Block
```
/blocklog inspect
```
Gives you an inspector wand. Right-click any block to see its history.

### Rollback Inventory
```
/blocklog inventory Notch 1h
```
Opens GUI to select a snapshot to rollback Notch's inventory to.

### Purge Old Logs
```
/blocklog purge 30
```
Removes all logs older than 30 days.

## GUI Usage

1. **Main Menu**: `/blocklog` opens the main GUI with options:
   - Lookup Logs - Browse player logs
   - Rollback Blocks - Rollback player actions
   - Inspector Tool - Get inspector wand
   - Inventory Rollback - Rollback inventories

2. **Lookup GUI**: Click "Lookup Logs" and enter a player name to see their logs
   - Click any log entry to see details
   - Use back button to return to main menu

3. **Inventory Rollback GUI**: Click "Inventory Rollback" and enter a player name
   - See all available snapshots
   - Click a snapshot to rollback the player's inventory to that state

## Performance Considerations

- All database operations are performed asynchronously
- Indexes are created on frequently queried columns
- Log purging runs automatically every 24 hours
- Inventory snapshots are taken periodically (configurable interval)
- Large rollbacks are processed with delays to prevent server lag

## Future Enhancements

Potential improvements:
- Area selection tool (pos1/pos2) for rollback
- Visual block restoration (particles/effects)
- Enhanced item serialization (enchantments, NBT data)
- Rollback preview before execution
- Export logs to file
- Web interface for log browsing
- Integration with Discord for log notifications

## Notes

- SQLite is used by default (no additional setup required)
- MySQL support is available but requires configuration
- Logs are stored in `blocklog.db` (SQLite) or MySQL database
- Inventory snapshots use Base64 encoding for storage
- All operations respect permissions
- The system is fully integrated with ECore's existing architecture

---

**Implementation Date**: Current
**Version**: 1.0
**Status**: ✅ Complete and Ready for Use

