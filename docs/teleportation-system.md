# Teleportation System

Advanced teleportation system with requests, random teleport, and biome/structure teleportation.

## Overview

ECore's teleportation system provides comprehensive teleportation features including player-to-player teleports, teleport requests, random teleportation, and biome/structure-based teleportation.

## Features

- **Player Teleportation**: Teleport to players directly
- **Teleport Requests**: Request-based teleportation system
- **Random Teleport**: Teleport to random safe locations
- **Biome Teleportation**: Teleport to specific biomes
- **Structure Teleportation**: Teleport to structures (villages, temples, etc.)
- **Return System**: Return to previous location
- **Jump Forward**: Teleport forward in the direction you're looking
- **Top Teleport**: Teleport to highest block above you

## Commands

### Direct Teleportation

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/tp <player>` | Teleport to a player | `ecore.teleport` | `op` |
| `/tp <player1> <player2>` | Teleport player1 to player2 | `ecore.teleport.others` | `op` |
| `/tp <x> <y> <z> [world]` | Teleport to coordinates | `ecore.teleport` | `op` |
| `/teleport <player>` | Alias for tp | `ecore.teleport` | `op` |

### Teleport Requests

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/tpa <player>` | Request to teleport to player | `ecore.teleport` | `op` |
| `/tpahere <player>` | Request player to teleport to you | `ecore.teleport` | `op` |
| `/tpaccept` | Accept teleport request | `ecore.teleport` | `op` |
| `/tpdeny` | Deny teleport request | `ecore.teleport` | `op` |

### Special Teleports

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/back` | Return to previous location | `ecore.teleport` | `op` |
| `/top` | Teleport to highest block | `ecore.teleport` | `op` |
| `/jump` | Teleport forward | `ecore.teleport` | `op` |
| `/rtp` | Random teleport | `ecore.teleport` | `op` |
| `/tpbiome <biome>` | Teleport to a biome | `ecore.teleport` | `op` |
| `/teleportbiome <biome>` | Alias for tpbiome | `ecore.teleport` | `op` |
| `/tpstructure <structure>` | Teleport to a structure | `ecore.teleport` | `op` |
| `/teleportstructure <structure>` | Alias for tpstructure | `ecore.teleport` | `op` |

## Usage Guide

### Direct Teleportation

- Use `/tp <player>` to teleport directly to a player
- Use `/tp <player1> <player2>` to teleport one player to another
- Use `/tp <x> <y> <z>` to teleport to specific coordinates
- Use `/tp <x> <y> <z> <world>` to teleport to coordinates in a specific world

### Teleport Requests

**Requesting to Teleport:**
1. Use `/tpa <player>` to request teleportation to a player
2. The player receives a request notification
3. They can accept with `/tpaccept` or deny with `/tpdeny`

**Requesting Player to Teleport:**
1. Use `/tpahere <player>` to request a player to teleport to you
2. The player receives a request notification
3. They can accept with `/tpaccept` or deny with `/tpdeny`

### Random Teleport

- Use `/rtp` to teleport to a random safe location
- The system finds a safe location (no lava, no void, solid ground)
- Useful for exploration or starting fresh

### Biome Teleportation

- Use `/tpbiome <biome>` to teleport to a specific biome
- Available biomes include: PLAINS, FOREST, DESERT, JUNGLE, OCEAN, etc.
- The system finds the nearest location of that biome type

### Structure Teleportation

- Use `/tpstructure <structure>` to teleport to a structure
- Available structures include: VILLAGE, TEMPLE, MONUMENT, FORTRESS, etc.
- The system finds the nearest structure of that type

### Return to Previous Location

- Use `/back` to return to your previous location
- Useful after teleporting or dying
- Tracks your last location before teleportation

### Jump Forward

- Use `/jump` to teleport forward in the direction you're looking
- Useful for quick movement
- Default distance is configurable

### Top Teleport

- Use `/top` to teleport to the highest block above you
- Useful for escaping caves or getting to the surface

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.teleport` | Use teleport commands | `op` |
| `ecore.teleport.others` | Teleport other players | `op` |

## Tips

- Use teleport requests for player-to-player teleportation (more polite)
- Use `/back` to return after accidental teleports
- Use `/rtp` for exploration or random spawns
- Use `/top` to quickly escape caves
- Use `/jump` for quick movement

## Related Systems

- [Warp & Spawn System](warp-spawn-system.md) - For warps and spawn
- [Home System](home-system.md) - For personal homes
- [Multi-World System](multi-world-system.md) - For world teleportation

