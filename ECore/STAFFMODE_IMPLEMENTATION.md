# Staff Mode Module Implementation

## Overview

This document describes the staff mode module implementation, inspired by the Staff++ plugin. The staff mode module provides a comprehensive system for staff members to enter a specialized moderation mode with enhanced capabilities and restrictions.

## Features Implemented

### Core Functionality

1. **State Management**
   - Saves player inventory, location, game mode, and other state when entering staff mode
   - Restores all saved state when exiting staff mode
   - Handles player disconnections gracefully

2. **Automatic Features**
   - **Vanish**: Automatically vanishes staff members (configurable)
   - **Flight**: Automatically enables flight for easier navigation (configurable)
   - **Invincibility**: Makes staff invincible to all damage (configurable)
   - **Night Vision**: Provides night vision effect (configurable)
   - **Game Mode**: Sets staff to spectator mode by default (configurable)

3. **Restrictions**
   - Prevents block breaking/placing (configurable)
   - Prevents item pickup/drop (configurable)
   - Prevents inventory editing (configurable)
   - Prevents entity/block interaction (configurable)
   - All restrictions are configurable via config.yml

4. **Staff Tools**
   - Default staff mode items:
     - Compass: Teleport tool
     - Book: View reports
     - Chest: Inspect player inventory
     - Redstone Block: Ban player
     - Iron Boots: Kick player
     - Barrier: Exit staff mode
   - Custom items can be configured in config.yml

5. **Command Integration**
   - Commands can be executed automatically when entering/exiting staff mode
   - Supports %player% placeholder

## Files Created

1. **StaffModeManager.java**
   - Core manager class handling all staff mode logic
   - Manages state saving/restoration
   - Applies/removes staff mode features
   - Handles staff mode items

2. **StaffModeCommand.java**
   - Command handler for `/staffmode` and `/sm` commands
   - Toggles staff mode on/off

3. **StaffModeListener.java**
   - Event listener for staff mode restrictions
   - Prevents unauthorized actions while in staff mode
   - Handles staff tool interactions

## Configuration

The staff mode configuration is located in `config.yml` under the `staffmode` section:

```yaml
staffmode:
  auto-vanish: true              # Auto-vanish on enter
  auto-fly: true                 # Auto-fly on enter
  invincible: true               # Invincibility
  night-vision: true              # Night vision effect
  game-mode: SPECTATOR           # Game mode (SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR)
  restore-location: false        # Restore location on exit
  allow-block-break: false       # Allow block breaking
  allow-block-place: false       # Allow block placing
  allow-item-pickup: false       # Allow item pickup
  allow-item-drop: false         # Allow item dropping
  allow-inventory-edit: false    # Allow inventory editing
  allow-entity-interact: false   # Allow entity interaction
  allow-block-interact: false    # Allow block interaction
  enter-commands: []            # Commands to run on enter
  exit-commands: []              # Commands to run on exit
  items:                         # Custom staff mode items (optional)
    # Example configuration
```

## Commands

- `/staffmode` - Toggle staff mode on/off
- `/sm` - Alias for `/staffmode`

## Permissions

- `ecore.staffmode` - Permission to use staff mode (default: op)

## Integration

The staff mode module is fully integrated into the Ecore plugin:

1. **Main Class Integration**
   - StaffModeManager initialized in `onEnable()`
   - Commands registered
   - Listeners registered

2. **Existing System Integration**
   - Uses existing `StaffManager` for vanish functionality
   - Uses existing `DiscordManager` for logging
   - Uses existing `ConfigManager` for configuration

## Usage

1. Staff members with `ecore.staffmode` permission can use `/staffmode` or `/sm`
2. When entering staff mode:
   - Current state is saved
   - Inventory is cleared and staff tools are given
   - Staff mode features are applied (vanish, flight, etc.)
   - Restrictions are enforced
3. When exiting staff mode:
   - Staff mode features are removed
   - Previous state is restored
   - Player returns to normal gameplay

## Staff Mode Tools

### Default Tools

- **Compass** (Slot 0): Teleport tool - Right-click players to teleport to them
- **Book** (Slot 1): View reports - Opens the reports GUI
- **Chest** (Slot 2): Inspect inventory - Right-click players to inspect their inventory
- **Redstone Block** (Slot 3): Ban player - Use staff GUI or `/ban` command
- **Iron Boots** (Slot 4): Kick player - Use staff GUI or `/kick` command
- **Barrier** (Slot 8): Exit staff mode - Click to exit staff mode

### Custom Tools

Custom tools can be configured in `config.yml` under `staffmode.items`:

```yaml
staffmode:
  items:
    custom-tool:
      material: DIAMOND_SWORD
      amount: 1
      slot: 5
      display-name: "&cCustom Tool"
```

## Event Handling

The StaffModeListener handles the following events:

- `BlockBreakEvent` - Prevents block breaking (if disabled)
- `BlockPlaceEvent` - Prevents block placing (if disabled)
- `EntityPickupItemEvent` - Prevents item pickup (if disabled)
- `PlayerDropItemEvent` - Prevents item dropping (if disabled)
- `EntityDamageEvent` - Prevents damage (if invincible)
- `InventoryClickEvent` - Prevents inventory editing (if disabled)
- `PlayerInteractEvent` - Handles staff tool interactions
- `PlayerInteractEntityEvent` - Handles player interactions (teleport/inspect)
- `PlayerQuitEvent` - Cleans up staff mode on quit

## Comparison with Staff++

This implementation includes the core features of Staff++:

✅ Multiple staff modes (extensible architecture)
✅ Customizable permissions and restrictions
✅ Vanish mode integration
✅ Flight and invincibility
✅ Command execution on toggle
✅ Inventory state saving/restoration
✅ Configurable staff tools
✅ Event-based restrictions

## Future Enhancements

Potential future enhancements:

1. Multiple staff mode types (moderator, admin, etc.)
2. Staff mode GUI for easier tool access
3. Staff mode statistics tracking
4. Staff mode notifications to other staff
5. Staff mode cooldowns
6. Staff mode time tracking

## Notes

- Staff mode state is stored in memory and lost on server restart
- Players in staff mode who disconnect will have their state cleaned up
- Staff mode integrates seamlessly with existing vanish system
- All restrictions can be bypassed by configuring them in config.yml


