# Portal System

Custom portals that automatically teleport players when they enter portal blocks.

## Overview

The portal system allows you to create custom portals from any block selection that automatically teleport players when they step into the portal blocks.

## Features

- **Custom Portals**: Create portals from any block selection
- **Automatic Teleportation**: Players automatically teleport when entering portal blocks
- **Multi-World Support**: Portals can bridge players between different worlds
- **Custom Materials**: Use any block material for portals
- **Permission-Based Access**: Control who can use specific portals
- **Custom Messages & Sounds**: Configure portal teleportation messages and sounds

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/portal create <name> [material]` | Create a portal from selection | `ecore.portal.create` | `op` |
| `/portal delete <name>` | Delete a portal | `ecore.portal.delete` | `op` |
| `/portal list` | List all portals | `ecore.portal.list` | `op` |
| `/portal info <name>` | View portal information | `ecore.portal.info` | `op` |
| `/portal setdest <name>` | Set portal destination to your location | `ecore.portal.setdest` | `op` |
| `/portal wand` | Get portal creation instructions | `ecore.portal.wand` | `op` |

## Usage Guide

### Creating Portals

1. **Select an Area**: Use WorldEdit selection tools (`/pos1` and `/pos2`) to select the portal area
2. **Set Destination**: Stand at the location where players should teleport to
3. **Create Portal**: Use `/portal create <name>` to create the portal
4. **Set Destination**: Use `/portal setdest <name>` to set the destination (if not already set)

**Optional:**
- Use `/portal create <name> <material>` to specify the portal block material (default: NETHER_PORTAL)

**Example:**
```
/pos1
/pos2
/portal create spawn_portal
/portal setdest spawn_portal
```

### Using Portals

1. Players simply walk into the portal blocks
2. They are automatically teleported to the destination
3. Custom messages and sounds can be configured

### Managing Portals

**List Portals:**
- Use `/portal list` to see all portals

**View Portal Info:**
- Use `/portal info <name>` to view portal details:
  - Location
  - Destination
  - Material
  - Permissions

**Delete Portals:**
- Use `/portal delete <name>` to remove a portal

**Change Destination:**
- Stand at new destination
- Use `/portal setdest <name>` to update destination

## Portal Materials

You can use any block material for portals. Common choices:
- **NETHER_PORTAL**: Default nether portal blocks
- **END_PORTAL**: End portal blocks
- **WATER**: Water blocks
- **LAVA**: Lava blocks
- **AIR**: Invisible portals (air blocks)
- Any other block type

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.portal.create` | Create portals | `op` |
| `ecore.portal.delete` | Delete portals | `op` |
| `ecore.portal.list` | List portals | `op` |
| `ecore.portal.info` | View portal information | `op` |
| `ecore.portal.setdest` | Set portal destinations | `op` |
| `ecore.portal.wand` | Use portal creation wand | `op` |
| `ecore.portal.use` | Use portals (enter portal blocks) | `true` |

## Tips

- Create portals for easy navigation between areas
- Use portals to connect different worlds
- Set clear destinations for better player experience
- Use custom materials for themed portals
- Create invisible portals (air) for hidden teleportation
- Use portals for spawn areas or event locations

## Related Systems

- [WorldEdit](worldedit.md) - For area selection
- [Multi-World System](multi-world-system.md) - For world portals
- [Teleportation System](teleportation-system.md) - For other teleportation features

