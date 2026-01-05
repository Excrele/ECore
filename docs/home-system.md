# Home System

The home system allows players to set multiple named homes and teleport to them with ease.

## Overview

ECore's home system provides a comprehensive solution for player home management, replacing EssentialsX home functionality with enhanced features including home sharing, categories, icons, and descriptions.

## Features

- **Multiple Homes**: Set multiple homes with custom names
- **Home Sharing**: Share homes with other players
- **Home Categories**: Organize homes into categories
- **Home Icons**: Set custom icons for homes
- **Home Descriptions**: Add descriptions to homes
- **Bed Spawn Integration**: Beds can automatically set homes when enabled
- **Teleport Costs**: Configure economy costs for home teleportation
- **Cooldowns**: Set cooldowns between home teleports
- **Warmup**: Add warmup delays that cancel if the player moves
- **Beautiful GUI**: Easy-to-use home management interface

## Commands

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

## Configuration

The home system is configured in `config.yml`:

```yaml
home:
  max-homes: 5                    # Maximum homes per player
  teleport-cost: 0.0             # Cost to teleport (0.0 = free)
  cooldown: 0                    # Cooldown in seconds (0 = no cooldown)
  warmup: 0                      # Warmup delay in seconds (0 = instant)
  bed-spawn-enabled: true        # Enable bed spawn integration
  auto-set-home-on-bed: true     # Auto-set home when sleeping
```

## Usage Guide

### Setting a Home

1. Stand at the location where you want to set your home
2. Use `/sethome <name>` where `<name>` is a unique name for your home
3. Your home is now saved and you can teleport to it anytime

### Teleporting to a Home

- Use `/home <name>` to teleport to a specific home
- Use `/home` to open the home GUI and select a home visually
- If you only have one home, `/home` will teleport you directly

### Sharing Homes

1. Use `/homeshare <home> <player>` to share a home with another player
2. The shared player can now teleport to that home
3. Use `/homeunshare <home> <player>` to revoke access

### Organizing Homes

- **Categories**: Use `/homecategory <home> <category>` to organize homes
- **Icons**: Use `/homeicon <home> <material>` to set a visual icon (e.g., `DIAMOND`, `CHEST`)
- **Descriptions**: Use `/homedescription <home> <description>` to add a description

### Bed Spawn Integration

When enabled, sleeping in a bed will automatically set a home at that location. This integrates with Minecraft's bed spawn system.

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.home` | Access home system | `true` |
| `ecore.home.bypass` | Bypass home cooldowns and costs | `op` |

## Tips

- Use descriptive names for your homes (e.g., "Mining Base", "Nether Portal")
- Organize homes by category for easier management
- Share frequently visited locations with friends
- Use the GUI (`/home`) for a visual overview of all your homes
- Set icons to quickly identify homes in the GUI

## Related Systems

- [Economy System](economy-system.md) - For teleport costs
- [Command Cooldowns & Costs](command-cooldowns-costs.md) - For cooldown configuration
- [Teleportation System](teleportation-system.md) - For general teleportation features

