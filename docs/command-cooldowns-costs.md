# Command Cooldowns & Costs

Configure cooldowns and economy costs for any command.

## Overview

The command cooldowns and costs system allows you to control command usage by setting cooldowns and economy costs for any command.

## Features

- **Per-Command Cooldowns**: Set cooldowns for any command (in seconds)
- **Per-Command Economy Costs**: Charge players for using commands
- **Bypass Permissions**: Configurable bypass permissions per command
- **Easy Configuration**: Setup in `config.yml` under `command-control.commands`
- **User-Friendly Messages**: Clear cooldown and cost notifications

## Configuration

Command cooldowns and costs are configured in `config.yml`:

```yaml
command-control:
  enabled: true
  commands:
    home:
      cooldown: 60               # 60 second cooldown
      cost: 10.0                 # 10 currency cost
      bypass-permission: ecore.home.bypass
    warp:
      cooldown: 30               # 30 second cooldown
      cost: 5.0                  # 5 currency cost
      bypass-permission: ecore.warp.bypass
```

## Usage Guide

### Setting Cooldowns

1. Edit `config.yml`
2. Navigate to `command-control.commands`
3. Add command name (e.g., `home`)
4. Set `cooldown` in seconds
5. Reload config with `/ecore reload`

### Setting Costs

1. Edit `config.yml`
2. Navigate to `command-control.commands`
3. Add command name
4. Set `cost` (economy amount)
5. Reload config with `/ecore reload`

### Bypass Permissions

1. Edit `config.yml`
2. Set `bypass-permission` for each command
3. Players with this permission bypass cooldown and cost
4. Example: `ecore.home.bypass`

### Example Configuration

```yaml
command-control:
  enabled: true
  commands:
    home:
      cooldown: 60
      cost: 10.0
      bypass-permission: ecore.home.bypass
    warp:
      cooldown: 30
      cost: 5.0
      bypass-permission: ecore.warp.bypass
    spawn:
      cooldown: 10
      cost: 0.0
      bypass-permission: ecore.spawn.bypass
```

## How It Works

### Cooldowns

- When a player uses a command with a cooldown, they must wait before using it again
- Cooldown is per-player
- Clear messages show remaining cooldown time
- Bypass permission skips cooldown

### Costs

- When a player uses a command with a cost, money is deducted
- Player must have sufficient balance
- Clear messages show cost and remaining balance
- Bypass permission skips cost

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.command.bypass` | Bypass all command cooldowns and costs | `op` |
| `ecore.home.bypass` | Bypass home command cooldown/cost | `op` |
| `ecore.warp.bypass` | Bypass warp command cooldown/cost | `op` |
| `ecore.tp.bypass` | Bypass teleport command cooldown/cost | `op` |
| `ecore.spawn.bypass` | Bypass spawn command cooldown/cost | `op` |

**Note:** Each command can have its own bypass permission configured.

## Tips

- Set reasonable cooldowns to prevent spam
- Use costs to balance economy
- Give staff bypass permissions
- Test cooldowns and costs before going live
- Use clear bypass permission names
- Monitor command usage to adjust settings

## Related Systems

- [Economy System](economy-system.md) - For command costs
- [Home System](home-system.md) - Example system with cooldowns/costs

