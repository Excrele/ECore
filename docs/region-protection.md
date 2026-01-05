# Region Protection

WorldGuard-like region system for protecting areas with flags, owners, and members.

## Overview

The region protection system allows you to create protected areas with configurable flags, owners, and members, providing WorldGuard-like functionality.

## Features

- **Region Creation**: Create protected regions from selections
- **Region Types**: Multiple region types with different properties
- **Region Flags**: Configure region behavior (PvP, build, interact, etc.)
- **Owners & Members**: Manage region access
- **Region Info**: View detailed region information
- **Region Management**: List, delete, and reload regions

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/region create <name> <type>` | Create a region from selection | `ecore.region.create` | `op` |
| `/region delete <name>` | Delete a region | `ecore.region.delete` | `op` |
| `/region list [world]` | List all regions | `ecore.region.list` | `op` |
| `/region info <name>` | Show region information | `ecore.region.info` | `op` |
| `/region flag <name> <flag> <true\|false>` | Set a region flag | `ecore.region.flag` | `op` |
| `/region flags <name>` | Show all flags for a region | `ecore.region.info` | `op` |
| `/region addowner <name> <player>` | Add an owner to region | `ecore.region.owner` | `op` |
| `/region removeowner <name> <player>` | Remove owner from region | `ecore.region.owner` | `op` |
| `/region addmember <name> <player>` | Add a member to region | `ecore.region.member` | `op` |
| `/region removemember <name> <player>` | Remove member from region | `ecore.region.member` | `op` |
| `/region types` | List available region types | `ecore.region.info` | `op` |
| `/region reload` | Reload regions from file | `ecore.region.reload` | `op` |

## Configuration

Region protection is configured in `config.yml`:

```yaml
regions:
  enabled: true                   # Enable region protection
  auto-save: true                 # Auto-save regions
  max-regions: 10                 # Maximum regions per player
  max-volume: 1000000             # Maximum region volume (blocks)
```

## Usage Guide

### Creating Regions

1. **Select an Area**: Use WorldEdit selection tools (`/pos1` and `/pos2`) to select the area
2. **Create Region**: Use `/region create <name> <type>` to create the region
3. **Set Flags**: Configure region behavior with flags (see below)

**Region Types:**
- `PRIVATE`: Private region (only owners/members can access)
- `PUBLIC`: Public region (everyone can access)
- `SHOP`: Shop region (for shops)
- `SPAWN`: Spawn region (for spawn areas)
- `ARENA`: Arena region (for PvP arenas)

### Region Flags

Region flags control what players can do in the region:

**Common Flags:**
- `pvp`: Allow PvP (true/false)
- `build`: Allow building (true/false)
- `break`: Allow block breaking (true/false)
- `interact`: Allow block interaction (true/false)
- `entry`: Allow entry (true/false)
- `exit`: Allow exit (true/false)
- `mob-spawning`: Allow mob spawning (true/false)
- `explosions`: Allow explosions (true/false)

**Setting Flags:**
- Use `/region flag <name> <flag> <true|false>` to set a flag
- Example: `/region flag spawn pvp false` - Disable PvP in spawn region

### Managing Owners and Members

**Owners:**
- Owners have full control over the region
- Use `/region addowner <name> <player>` to add an owner
- Use `/region removeowner <name> <player>` to remove an owner

**Members:**
- Members can access the region but can't modify it
- Use `/region addmember <name> <player>` to add a member
- Use `/region removemember <name> <player>` to remove a member

### Viewing Region Information

- Use `/region info <name>` to view:
  - Region type
  - Location and size
  - Owners and members
  - All flags and their values

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.region` | Use region commands | `op` |
| `ecore.region.create` | Create regions | `op` |
| `ecore.region.delete` | Delete regions | `op` |
| `ecore.region.list` | List regions | `op` |
| `ecore.region.info` | View region information | `op` |
| `ecore.region.flag` | Set region flags | `op` |
| `ecore.region.owner` | Manage region owners | `op` |
| `ecore.region.member` | Manage region members | `op` |
| `ecore.region.reload` | Reload regions | `op` |
| `ecore.region.bypass` | Bypass all region restrictions | `op` |
| `ecore.region.unlimited` | Create unlimited regions | `op` |

## Tips

- Create regions for spawn areas to prevent griefing
- Use flags to customize region behavior
- Add owners and members for collaborative areas
- Use different region types for different purposes
- Protect important builds with regions
- Use region info to check current settings

## Related Systems

- [WorldEdit](worldedit.md) - For area selection
- [Shop System](shop-system.md) - For shop regions

