# Economy System

ECore includes a complete, self-contained economy system that requires no external dependencies.

## Overview

The economy system is fully integrated into ECore and provides all the functionality you need for a server economy, including player balances, payments, banks, shops, and auction houses.

## Features

- **Self-Contained**: No Vault dependency required (optional Vault support available)
- **Starting Balance**: Configurable starting balance for new players
- **Bank System**: Multiple bank accounts with interest rates
- **Economy Statistics**: Track economy metrics and leaderboards
- **Complete API**: Full API for other plugins to integrate
- **Payment System**: Easy payments between players
- **Economy Administration**: Staff commands for economy management

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/balance [player]` | Check balance (yours or others) | `ecore.economy`, `ecore.economy.balance.others` | `true` / `op` |
| `/bal [player]` | Alias for balance | `ecore.economy`, `ecore.economy.balance.others` | `true` / `op` |
| `/money [player]` | Alias for balance | `ecore.economy`, `ecore.economy.balance.others` | `true` / `op` |
| `/pay <player> <amount>` | Pay a player | `ecore.economy` | `true` |
| `/economy <give\|take\|set\|stats> <player> [amount]` | Economy administration | `ecore.economy.admin` | `op` |
| `/eco <give\|take\|set\|stats> <player> [amount]` | Alias for economy | `ecore.economy.admin` | `op` |
| `/baltop [limit]` | View economy leaderboard | `ecore.economy` | `true` |
| `/balancetop [limit]` | Alias for baltop | `ecore.economy` | `true` |

## Configuration

The economy system is configured in `config.yml`:

```yaml
economy:
  starting-balance: 100.0        # Starting balance for new players
```

Economy data is stored in `economy.yml` (auto-generated, do not edit manually).

## Usage Guide

### Checking Your Balance

- Use `/balance` or `/bal` to check your own balance
- Use `/balance <player>` to check another player's balance (requires permission)

### Paying Players

1. Use `/pay <player> <amount>` to send money to another player
2. The amount will be deducted from your balance and added to theirs
3. You must have sufficient funds to complete the payment

### Economy Administration

Staff members can manage player balances:

- `/economy give <player> <amount>` - Give money to a player
- `/economy take <player> <amount>` - Take money from a player
- `/economy set <player> <amount>` - Set a player's balance
- `/economy stats <player>` - View economy statistics for a player

### Economy Leaderboards

- Use `/baltop` to view the top players by balance
- Use `/baltop [limit]` to specify how many players to show (default: 10)

## Bank System

Players can create multiple bank accounts for better money management. See [Bank System](bank-system.md) for detailed information.

## Integration

The economy system integrates with:

- **Shops**: Both admin and player shops use the economy
- **Auction House**: All transactions use the economy
- **Command Costs**: Commands can charge economy costs
- **Home/Warp Teleports**: Teleportation can cost money
- **Vault**: Optional integration with Vault-compatible economy plugins

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.economy` | Use economy commands | `true` |
| `ecore.economy.balance.others` | Check others' balance | `op` |
| `ecore.economy.admin` | Economy administration | `op` |

## API Usage

Other plugins can integrate with ECore's economy:

```java
Ecore plugin = Ecore.getInstance();
EconomyManager economyManager = plugin.getEconomyManager();

// Get player balance
double balance = economyManager.getBalance(player);

// Give money
economyManager.depositPlayer(player, 100.0);

// Take money
economyManager.withdrawPlayer(player, 50.0);

// Check if player has enough
if (economyManager.has(player, 25.0)) {
    economyManager.withdrawPlayer(player, 25.0);
}
```

## Tips

- Set a reasonable starting balance for new players
- Use `/baltop` to encourage competition
- Integrate economy costs with commands to create a balanced economy
- Monitor economy statistics to track server economy health

## Related Systems

- [Bank System](bank-system.md) - For bank accounts and interest
- [Shop System](shop-system.md) - For buying and selling items
- [Command Cooldowns & Costs](command-cooldowns-costs.md) - For command economy costs

