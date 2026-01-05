# Staff Mode

Staff mode provides a safe monitoring environment for staff members to observe the server without interfering.

## Overview

Staff mode automatically applies various protections and features when enabled, allowing staff to safely monitor the server without accidentally breaking blocks, picking up items, or interfering with gameplay.

## Features

- **Auto-Vanish**: Automatically vanish when entering staff mode
- **Auto-Fly**: Automatically enable flight
- **Invincible**: Become invincible to all damage
- **Night Vision**: Automatic night vision effect
- **Game Mode**: Automatically switch to configured game mode (default: SPECTATOR)
- **Block Protection**: Prevent block breaking, placing, and interaction
- **Item Protection**: Prevent item pickup and dropping
- **Inventory Protection**: Prevent inventory editing

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/staffmode` | Toggle staff mode | `ecore.staffmode` | `op` |
| `/sm` | Alias for staffmode | `ecore.staffmode` | `op` |

## Configuration

Staff mode is configured in `config.yml`:

```yaml
staff-mode:
  auto-vanish: true              # Automatically vanish when entering staff mode
  auto-fly: true                 # Automatically enable flight
  invincible: true               # Become invincible
  night-vision: true             # Apply night vision effect
  game-mode: SPECTATOR           # Game mode to switch to (SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR)
  prevent-block-break: true      # Prevent breaking blocks
  prevent-block-place: true      # Prevent placing blocks
  prevent-interact: true         # Prevent block interaction
  prevent-item-pickup: true      # Prevent picking up items
  prevent-item-drop: true        # Prevent dropping items
  prevent-inventory-edit: true    # Prevent inventory editing
```

## Usage Guide

### Entering Staff Mode

1. Use `/staffmode` or `/sm` to enter staff mode
2. The following happens automatically:
   - You become vanished (invisible to players)
   - Flight is enabled
   - You become invincible
   - Night vision is applied
   - Game mode switches to SPECTATOR (or configured mode)
   - All protections are enabled

### Exiting Staff Mode

1. Use `/staffmode` or `/sm` again to exit staff mode
2. Your previous state is restored:
   - Vanish is disabled
   - Flight is disabled (if you didn't have it before)
   - Invincibility is removed
   - Night vision is removed
   - Game mode returns to previous mode
   - Protections are disabled

### Staff Mode Features

**Protection:**
- Cannot break blocks
- Cannot place blocks
- Cannot interact with blocks (chests, doors, etc.)
- Cannot pick up items
- Cannot drop items
- Cannot edit inventory

**Benefits:**
- Safe monitoring without interference
- Invisible to players (vanish)
- Can fly for easy navigation
- Invincible to damage
- Night vision for visibility
- Spectator mode for easy observation

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.staffmode` | Enter/exit staff mode | `op` |

## Tips

- Use staff mode when monitoring players for rule violations
- Staff mode prevents accidental interference with gameplay
- Spectator mode allows you to pass through blocks for easy observation
- Vanish keeps you hidden from players
- Use flight to quickly navigate the server

## Related Systems

- [Staff Management](staff-management.md) - For other staff tools
- [Vanish System](staff-management.md#vanish) - For manual vanish control

