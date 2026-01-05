# WorldEdit

Lightweight WorldEdit implementation with all essential features.

## Overview

ECore includes a complete WorldEdit-like system for building, editing, and managing your world with selection tools, clipboard operations, and more.

## Features

- **Selection Tools**: Wand and position selection
- **Block Operations**: Set, replace, clear blocks
- **Clipboard Operations**: Copy, paste, cut
- **History System**: Undo and redo operations
- **Schematic System**: Save and load builds
- **Brush System**: Sphere and cylinder generation
- **Async Operations**: Large builds don't lag the server
- **Block Limits**: Prevents server crashes

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/wand` | Get selection wand | `ecore.worldedit.wand` | `op` |
| `/pos1` | Set selection position 1 | `ecore.worldedit.use` | `op` |
| `/pos2` | Set selection position 2 | `ecore.worldedit.use` | `op` |
| `/set <block>` | Fill selection with blocks | `ecore.worldedit.set` | `op` |
| `/replace <from> <to>` | Replace blocks in selection | `ecore.worldedit.replace` | `op` |
| `/clear` | Clear selection (set to air) | `ecore.worldedit.clear` | `op` |
| `/walls <block>` | Create walls in selection | `ecore.worldedit.walls` | `op` |
| `/hollow <block>` | Create hollow box in selection | `ecore.worldedit.hollow` | `op` |
| `/copy` | Copy selection to clipboard | `ecore.worldedit.copy` | `op` |
| `/paste` | Paste clipboard at your location | `ecore.worldedit.paste` | `op` |
| `/cut` | Cut selection to clipboard | `ecore.worldedit.copy` | `op` |
| `/undo` | Undo last WorldEdit operation | `ecore.worldedit.undo` | `op` |
| `/redo` | Redo last undone operation | `ecore.worldedit.redo` | `op` |
| `/schematic <save\|load\|list\|delete> [name]` | Manage schematics | `ecore.worldedit.schematic` | `op` |
| `/sphere <radius> <block> [hollow]` | Create a sphere | `ecore.worldedit.sphere` | `op` |
| `/cylinder <radius> <height> <block> [hollow]` | Create a cylinder | `ecore.worldedit.cylinder` | `op` |
| `/sel` | Show selection information | `ecore.worldedit.use` | `op` |
| `/selection` | Alias for sel | `ecore.worldedit.use` | `op` |

## Usage Guide

### Selection

**Using Wand:**
1. Use `/wand` to get the selection wand
2. Left-click to set position 1
3. Right-click to set position 2
4. Selection is shown with particles

**Using Commands:**
1. Stand at first corner
2. Use `/pos1` to set position 1
3. Move to opposite corner
4. Use `/pos2` to set position 2

### Basic Operations

**Fill Selection:**
- Use `/set <block>` to fill selection with blocks
- Example: `/set STONE` - Fills with stone

**Replace Blocks:**
- Use `/replace <from> <to>` to replace blocks
- Example: `/replace DIRT GRASS` - Replaces dirt with grass

**Clear Selection:**
- Use `/clear` to set all blocks to air

**Create Walls:**
- Use `/walls <block>` to create walls around selection

**Create Hollow Box:**
- Use `/hollow <block>` to create hollow box

### Clipboard Operations

**Copy:**
1. Select an area
2. Use `/copy` to copy to clipboard
3. Move to destination
4. Use `/paste` to paste

**Cut:**
1. Select an area
2. Use `/cut` to cut to clipboard (removes original)
3. Move to destination
4. Use `/paste` to paste

### History

- Use `/undo` to undo last operation
- Use `/redo` to redo undone operation
- History is per-player

### Schematics

**Save Schematic:**
1. Select an area
2. Use `/schematic save <name>` to save

**Load Schematic:**
1. Stand where you want to paste
2. Use `/schematic load <name>` to load

**List Schematics:**
- Use `/schematic list` to see all schematics

**Delete Schematic:**
- Use `/schematic delete <name>` to delete

### Brush System

**Sphere:**
- Use `/sphere <radius> <block>` to create sphere
- Use `/sphere <radius> <block> true` for hollow sphere

**Cylinder:**
- Use `/cylinder <radius> <height> <block>` to create cylinder
- Use `/cylinder <radius> <height> <block> true` for hollow cylinder

## Configuration

WorldEdit is configured in `config.yml`:

```yaml
worldedit:
  max-block-changes: 1000000     # Maximum blocks per operation
  blocks-per-tick: 1000          # Blocks changed per tick
  history-size: 20               # Number of operations in history
```

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.worldedit.use` | Use WorldEdit commands | `op` |
| `ecore.worldedit.wand` | Get selection wand | `op` |
| `ecore.worldedit.set` | Use /set command | `op` |
| `ecore.worldedit.replace` | Use /replace command | `op` |
| `ecore.worldedit.clear` | Use /clear command | `op` |
| `ecore.worldedit.walls` | Use /walls command | `op` |
| `ecore.worldedit.hollow` | Use /hollow command | `op` |
| `ecore.worldedit.copy` | Use /copy command | `op` |
| `ecore.worldedit.paste` | Use /paste command | `op` |
| `ecore.worldedit.undo` | Use /undo command | `op` |
| `ecore.worldedit.redo` | Use /redo command | `op` |
| `ecore.worldedit.schematic` | Use schematic commands | `op` |
| `ecore.worldedit.sphere` | Use /sphere command | `op` |
| `ecore.worldedit.cylinder` | Use /cylinder command | `op` |

## Tips

- Use selection tools for precise building
- Use clipboard for copying builds
- Use schematics for saving builds
- Use undo/redo for mistakes
- Use async operations for large builds
- Check block limits before large operations

## Related Systems

- [Region Protection](region-protection.md) - For protecting builds
- [Portal System](portal-system.md) - For creating portals from selections

