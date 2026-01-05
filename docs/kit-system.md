# Kit System

Create and manage kits with cooldowns.

## Overview

The kit system allows you to create item kits that players can claim, with configurable cooldowns.

## Features

- **Kit Creation**: Create kits from inventory
- **Kit Claiming**: Players can claim kits
- **Kit Cooldowns**: Configurable cooldowns per kit
- **Kit GUI**: Browse and claim kits easily
- **Kit Management**: Create, delete, and give kits

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/kit <name>` | Get a kit | `ecore.kit.<name>` or `ecore.kit.*` | `true` |
| `/kit list` | List all kits or open kit GUI | `ecore.kit` | `true` |
| `/kit create <name>` | Create a kit from inventory | `ecore.kit.create` | `op` |
| `/kit delete <name>` | Delete a kit | `ecore.kit.delete` | `op` |
| `/kit give <player> <kit>` | Give kit to player | `ecore.kit.give` | `op` |

## Usage Guide

### Creating Kits

1. Fill your inventory with items for the kit
2. Use `/kit create <name>` to create the kit
3. Kit is saved with all items

### Claiming Kits

- Use `/kit <name>` to claim a kit
- Items are added to inventory
- Cooldown applies before next claim

### Kit GUI

- Use `/kit list` to open the kit GUI
- Browse available kits
- See cooldowns and requirements
- Claim kits easily

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.kit` | Use kits | `true` |
| `ecore.kit.<name>` | Use specific kit | `true` |
| `ecore.kit.*` | Use all kits | `true` |
| `ecore.kit.create` | Create kits | `op` |
| `ecore.kit.delete` | Delete kits | `op` |
| `ecore.kit.give` | Give kits to others | `op` |

## Tips

- Create starter kits for new players
- Use cooldowns to prevent abuse
- Organize kits by purpose
- Use kit GUI for better experience

