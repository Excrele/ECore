# Warp & Spawn System

Warp and spawn system for server-wide teleportation points.

## Overview

The warp and spawn system allows server administrators to create public teleportation points (warps) and manage the server spawn location.

## Features

- **Warps**: Create public teleportation points
- **Warp GUI**: Browse and select warps visually
- **Spawn System**: Server spawn point management
- **Warp Management**: Create, delete, and list warps
- **Permission-Based Access**: Control who can use warps

## Commands

### Warp Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/warp [name]` | Teleport to a warp or open warp GUI | `ecore.warp` | `true` |
| `/setwarp <name>` | Create a warp | `ecore.warp.set` | `op` |
| `/delwarp <name>` | Delete a warp | `ecore.warp.delete` | `op` |
| `/deletewarp <name>` | Alias for delwarp | `ecore.warp.delete` | `op` |
| `/warps` | List all warps or open warp GUI | `ecore.warp` | `true` |

### Spawn Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/spawn [player]` | Teleport to spawn | `ecore.spawn`, `ecore.spawn.others` | `true` / `op` |
| `/setspawn` | Set spawn point | `ecore.spawn.set` | `op` |

## Usage Guide

### Creating Warps

1. Stand at the location where you want to create a warp
2. Use `/setwarp <name>` where `<name>` is a unique name for the warp
3. The warp is created and players can now teleport to it

### Teleporting to Warps

- Use `/warp <name>` to teleport to a specific warp
- Use `/warp` to open the warp GUI and select a warp visually
- Use `/warps` to list all available warps

### Deleting Warps

1. Use `/delwarp <name>` to delete a warp
2. The warp is permanently removed

### Setting Spawn

1. Stand at the location where you want the server spawn
2. Use `/setspawn` to set the spawn point
3. Players will spawn here when joining the server or using `/spawn`

### Teleporting to Spawn

- Use `/spawn` to teleport to the server spawn
- Use `/spawn <player>` to teleport another player to spawn (requires permission)

## Warp GUI

The warp GUI provides:
- Visual list of all warps
- Easy selection by clicking
- Warp information display
- Organized by categories (if configured)

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.warp` | Use warps | `true` |
| `ecore.warp.set` | Create warps | `op` |
| `ecore.warp.delete` | Delete warps | `op` |
| `ecore.spawn` | Teleport to spawn | `true` |
| `ecore.spawn.set` | Set spawn | `op` |
| `ecore.spawn.others` | Teleport others to spawn | `op` |

## Tips

- Create warps for important locations (shops, spawn, events)
- Use descriptive names for warps
- Use the warp GUI for better player experience
- Set spawn in a safe, welcoming location
- Create warps for different areas of your server

## Related Systems

- [Teleportation System](teleportation-system.md) - For other teleportation features
- [Home System](home-system.md) - For personal teleportation points
- [Multi-World System](multi-world-system.md) - For world-specific warps

