# Mob Customization

Customize mob behavior, drops, health, and damage.

## Overview

The mob customization system allows you to customize mob properties including drops, health, damage, and spawn rates.

## Features

- **Custom Mob Drops**: Configurable drops per mob type
- **Custom Mob Health**: Override default health
- **Custom Mob Damage**: Override default damage
- **Custom Spawn Rates**: Multiplier for spawn frequency
- **Replace Default Drops**: Option to replace default drops
- **Drop Chance System**: Percentage-based drop chances

## Configuration

Mob customization is configured in `mob-customization.yml`:

```yaml
mobs:
  ZOMBIE:
    health: 30.0                  # Custom health (default: 20.0)
    damage: 5.0                  # Custom damage (default: 2.0)
    spawn-rate: 1.5              # Spawn rate multiplier
    replace-default-drops: false  # Replace default drops
    drops:
      - type: DIAMOND
        amount: 1
        chance: 0.01              # 1% chance
      - type: GOLD_INGOT
        amount: 1-3
        chance: 0.1               # 10% chance
```

## Usage Guide

### Customizing Mobs

1. Edit `mob-customization.yml`
2. Add mob type (e.g., `ZOMBIE`, `SKELETON`, `CREEPER`)
3. Configure health, damage, spawn rate, and drops
4. Reload config with `/ecore reload`

### Drop Configuration

**Basic Drop:**
```yaml
drops:
  - type: DIAMOND
    amount: 1
    chance: 0.01
```

**Range Drop:**
```yaml
drops:
  - type: GOLD_INGOT
    amount: 1-3
    chance: 0.1
```

**Replace Default Drops:**
- Set `replace-default-drops: true` to replace default drops
- Set `replace-default-drops: false` to add to default drops

### Health and Damage

- Set `health` to override mob health
- Set `damage` to override mob damage
- Values are applied on mob spawn

### Spawn Rates

- Set `spawn-rate` to multiply spawn frequency
- `1.0` = normal spawn rate
- `2.0` = double spawn rate
- `0.5` = half spawn rate

## Permissions

No special permissions needed - configured in `mob-customization.yml`.

## Tips

- Customize mobs for server balance
- Use drop chances for rare items
- Adjust health/damage for difficulty
- Test spawn rates for performance
- Use range amounts for variety
- Replace default drops for custom loot tables

## Related Systems

- [Economy System](economy-system.md) - For economy-based drops
- [Quest System](quest-system.md) - For quest-related mobs

