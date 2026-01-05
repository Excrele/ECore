# Player Vaults System

Extra storage vaults for players with permission-based limits and trust system.

## Overview

The player vaults system provides additional storage space for players beyond their regular inventory, with permission-based vault limits and a trust system for sharing.

## Features

- **Multiple Vaults**: Permission-based vault limits (1-10 vaults)
- **Vault GUI**: Easy vault selection and management
- **Vault Naming**: Custom names for each vault
- **Trust System**: Share vaults with friends
- **54-Slot Storage**: Each vault has 54 slots (6 rows)

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/vault` | Open vault selection GUI | `ecore.vault` | `true` |
| `/pv` | Alias for vault | `ecore.vault` | `true` |
| `/vault open <number>` | Open specific vault | `ecore.vault` | `true` |
| `/vault create <number>` | Create new vault | `ecore.vault` | `true` |
| `/vault rename <number> <name>` | Rename vault | `ecore.vault` | `true` |
| `/vault trust <number> <player>` | Trust player with vault | `ecore.vault` | `true` |
| `/vault untrust <number> <player>` | Untrust player | `ecore.vault` | `true` |
| `/vault list` | List your vaults | `ecore.vault` | `true` |

## Configuration

Player vaults are configured in `config.yml`:

```yaml
vaults:
  max-vaults: 10                  # Maximum vaults (permission-based)
  vault-size: 54                  # Vault size in slots (6 rows)
```

## Usage Guide

### Creating Vaults

1. Use `/vault` to open the vault GUI
2. Click on an empty vault slot
3. Or use `/vault create <number>` to create a specific vault
4. The vault is created and ready to use

### Opening Vaults

- Use `/vault` to open the vault GUI and select a vault
- Use `/vault open <number>` to open a specific vault directly
- Vaults open as a chest GUI

### Naming Vaults

- Use `/vault rename <number> <name>` to give a vault a custom name
- Example: `/vault rename 1 "Diamonds"`
- Names help organize your vaults

### Trust System

**Trusting Players:**
1. Use `/vault trust <number> <player>` to trust a player
2. The trusted player can now access that vault
3. They can view and modify the vault contents

**Untrusting Players:**
- Use `/vault untrust <number> <player>` to revoke access

### Vault Limits

Vault limits are permission-based:
- `ecore.vault.1` - Allows 1 vault (default: true)
- `ecore.vault.2` - Allows 2 vaults (default: false)
- `ecore.vault.3` - Allows 3 vaults (default: false)
- ... up to `ecore.vault.10` for 10 vaults

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.vault` | Use vault commands | `true` |
| `ecore.vault.1` | Allows having 1 vault | `true` |
| `ecore.vault.2` | Allows having 2 vaults | `false` |
| `ecore.vault.3` | Allows having 3 vaults | `false` |
| ... | ... | ... |
| `ecore.vault.10` | Allows having 10 vaults | `false` |

## Tips

- Organize items by vault (e.g., "Building Blocks", "Ores", "Tools")
- Use descriptive names for easy identification
- Trust friends for shared storage
- Use multiple vaults to organize large collections
- Vaults persist across server restarts

## Related Systems

- [Friends & Party System](friends-party-system.md) - For trusting friends

