# Multi-World System

Complete world management system for creating, managing, and teleporting between multiple worlds.

## Overview

The multi-world system allows server administrators to create, manage, and teleport between multiple worlds with different types, environments, and properties.

## Features

- **Create Worlds**: Create new worlds with custom types, environments, and seeds
- **World Management**: Load, unload, and delete worlds dynamically
- **World Properties**: Configure spawn locations, difficulty, PVP, and more
- **World Teleportation**: Seamlessly teleport players between worlds
- **Safe Spawn**: Automatic safe location finding when teleporting
- **World Information**: View detailed information about any world
- **Auto-Load Configuration**: Configure which worlds load automatically

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/mv create <name> [type] [environment] [seed]` | Create a new world | `ecore.world.create` | `op` |
| `/multiverse create <name> [type] [environment] [seed]` | Alias for mv create | `ecore.world.create` | `op` |
| `/mv load <name>` | Load an existing world | `ecore.world.load` | `op` |
| `/mv unload <name> [save]` | Unload a world | `ecore.world.unload` | `op` |
| `/mv delete <name>` | Delete a world (WARNING: Permanent!) | `ecore.world.delete` | `op` |
| `/mv list` | List all worlds | `ecore.world.list` | `op` |
| `/mv tp <world> [player]` | Teleport to a world | `ecore.world.teleport`, `ecore.world.teleport.others` | `op` |
| `/mv spawn <world>` | Teleport to world spawn | `ecore.world.spawn` | `op` |
| `/mv setspawn [world]` | Set world spawn to your location | `ecore.world.setspawn` | `op` |
| `/mv info <world>` | View world information | `ecore.world.info` | `op` |
| `/mv reload` | Reload world configuration | `ecore.world.reload` | `op` |

## World Types

- **NORMAL**: Standard world generation
- **FLAT**: Superflat world
- **LARGE_BIOMES**: Large biome sizes
- **AMPLIFIED**: Amplified terrain generation
- **CUSTOMIZED**: Custom world generation settings

## Environments

- **NORMAL**: Overworld
- **NETHER**: Nether dimension
- **THE_END**: End dimension

## Usage Guide

### Creating Worlds

1. Use `/mv create <name>` to create a world with default settings
2. Use `/mv create <name> <type>` to specify world type
3. Use `/mv create <name> <type> <environment>` to specify environment
4. Use `/mv create <name> <type> <environment> <seed>` to specify seed

**Examples:**
- `/mv create creative FLAT NORMAL` - Create flat creative world
- `/mv create nether NORMAL NETHER` - Create nether world
- `/mv create custom NORMAL NORMAL 12345` - Create world with seed

### Managing Worlds

**Loading Worlds:**
- Use `/mv load <name>` to load an existing world

**Unloading Worlds:**
- Use `/mv unload <name>` to unload a world
- Use `/mv unload <name> false` to unload without saving
- **Warning:** Players in unloaded worlds will be teleported to default world spawn

**Deleting Worlds:**
- Use `/mv delete <name>` to permanently delete a world
- **WARNING:** This action cannot be undone!

### Teleporting to Worlds

- Use `/mv tp <world>` to teleport to a world
- Use `/mv tp <world> <player>` to teleport another player
- Use `/mv spawn <world>` to teleport to world spawn

### Setting World Spawn

1. Stand at the location where you want the world spawn
2. Use `/mv setspawn` to set spawn for current world
3. Use `/mv setspawn <world>` to set spawn for specific world

### World Information

- Use `/mv info <world>` to view detailed world information:
  - World type and environment
  - Seed
  - Spawn location
  - Difficulty
  - PVP status
  - And more

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.world.create` | Create new worlds | `op` |
| `ecore.world.load` | Load worlds | `op` |
| `ecore.world.unload` | Unload worlds | `op` |
| `ecore.world.delete` | Delete worlds | `op` |
| `ecore.world.list` | List worlds | `op` |
| `ecore.world.teleport` | Teleport to worlds | `op` |
| `ecore.world.teleport.others` | Teleport other players to worlds | `op` |
| `ecore.world.spawn` | Teleport to world spawns | `op` |
| `ecore.world.setspawn` | Set world spawns | `op` |
| `ecore.world.info` | View world information | `op` |
| `ecore.world.reload` | Reload world configuration | `op` |

## Tips

- Create separate worlds for different purposes (creative, survival, events)
- Use flat worlds for building or testing
- Unload unused worlds to save memory
- Set safe spawn locations for each world
- Use world-specific spawns for better player experience
- Be careful when deleting worlds - it's permanent!

## Related Systems

- [Portal System](portal-system.md) - For portals between worlds
- [Teleportation System](teleportation-system.md) - For general teleportation

