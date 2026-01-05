# Custom Enchantments

90+ unique enchantments across all item types with scalable levels.

## Overview

ECore includes 90+ custom enchantments for weapons, armor, tools, bows/crossbows, and fishing rods, providing unique gameplay enhancements.

## Features

- **90+ Enchantments**: Extensive library of unique enchantments
- **Weapon Enchantments**: 18 enchantments for swords and axes
- **Armor Enchantments**: 18 enchantments for armor pieces
- **Tool Enchantments**: 18 enchantments for tools
- **Bow/Crossbow Enchantments**: 18 enchantments for ranged weapons
- **Fishing Rod Enchantments**: 18 enchantments for fishing rods
- **Scalable Levels**: All enchantments scale with level (1-5 or 1-10)
- **Item-Specific**: Enchantments can only be applied to applicable items

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/enchant list` | List all custom enchantments | `ecore.enchant` | `true` |
| `/customenchant list` | Alias for enchant list | `ecore.enchant` | `true` |
| `/enchant info <id>` | View enchantment info | `ecore.enchant` | `true` |
| `/enchant apply <id> [level]` | Apply enchantment to held item (admin) | `ecore.enchant.admin` | `op` |
| `/enchant remove <id>` | Remove enchantment from held item (admin) | `ecore.enchant.admin` | `op` |

## Configuration

Custom enchantments are configured in `enchantments.yml`:

```yaml
enchantments:
  lifesteal:
    type: WEAPON
    max-level: 5
    description: "Steals health from enemies"
    effects:
      health-steal: 0.1  # 10% health per level
```

## Enchantment Categories

### Weapon Enchantments (18)

Examples:
- **Lifesteal**: Steals health from enemies
- **Venom**: Poisons enemies
- **Wither**: Applies wither effect
- **Lightning**: Strikes lightning on hit
- **Fire Aspect Plus**: Enhanced fire damage
- And 13 more...

### Armor Enchantments (18)

Examples:
- **Regeneration**: Regenerates health over time
- **Absorption**: Provides absorption hearts
- **Thorns Plus**: Enhanced thorns damage
- **Speed Boost**: Increases movement speed
- **Jump Boost**: Increases jump height
- And 13 more...

### Tool Enchantments (18)

Examples:
- **Auto Smelt**: Automatically smelts mined ores
- **Vein Miner**: Mines entire veins at once
- **Tree Feller**: Cuts entire trees
- **Experience Boost**: Increases experience gain
- **Fortune Plus**: Enhanced fortune
- And 13 more...

### Bow/Crossbow Enchantments (18)

Examples:
- **Explosive Arrows**: Arrows explode on impact
- **Homing**: Arrows home in on targets
- **Teleport Arrows**: Teleports you to arrow location
- **Multi Shot**: Shoots multiple arrows
- **Poison Arrows**: Poisons targets
- And 13 more...

### Fishing Rod Enchantments (18)

Examples:
- **Treasure Hunter**: Increases treasure catch rate
- **Double Catch**: Chance to catch two items
- **Fish Finder**: Shows fish locations
- **Lure Plus**: Enhanced lure
- **Luck of the Sea Plus**: Enhanced luck
- And 13 more...

## Usage Guide

### Viewing Enchantments

- Use `/enchant list` to see all available enchantments
- Use `/enchant info <id>` to view detailed information about an enchantment

### Applying Enchantments (Admin)

1. Hold the item you want to enchant
2. Use `/enchant apply <id> [level]` to apply the enchantment
3. Example: `/enchant apply lifesteal 3` - Applies level 3 lifesteal

### Removing Enchantments (Admin)

1. Hold the item with the enchantment
2. Use `/enchant remove <id>` to remove the enchantment

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.enchant` | View custom enchantments | `true` |
| `ecore.enchant.admin` | Apply/remove custom enchantments | `op` |

## Tips

- Check enchantment info before applying
- Higher levels provide better effects
- Some enchantments work better in combination
- Weapon enchantments activate on hit
- Armor enchantments provide passive effects
- Tool enchantments activate while using tools

## Related Systems

- [Economy System](economy-system.md) - For purchasing enchantments (if configured)

