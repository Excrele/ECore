# Bank System

The bank system allows players to create multiple bank accounts for better money management and earn interest on their savings.

## Overview

Players can create multiple bank accounts to organize their money, earn interest, and transfer funds between accounts. This system integrates seamlessly with the economy system.

## Features

- **Multiple Accounts**: Create multiple bank accounts per player
- **Interest Rates**: Accounts can earn interest over time
- **Account Management**: Easy deposit, withdraw, and transfer operations
- **Account Limits**: Configurable maximum accounts per player
- **Interest Calculation**: Automatic interest calculation

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/bank create <name>` | Create a bank account | `ecore.economy` | `true` |
| `/bank delete <name>` | Delete a bank account | `ecore.economy` | `true` |
| `/bank list` | List your bank accounts | `ecore.economy` | `true` |
| `/bank balance [account]` | Check account balance | `ecore.economy` | `true` |
| `/bank deposit <account> <amount>` | Deposit money | `ecore.economy` | `true` |
| `/bank withdraw <account> <amount>` | Withdraw money | `ecore.economy` | `true` |
| `/bank transfer <from> <to> <amount>` | Transfer between accounts | `ecore.economy` | `true` |
| `/bank interest <account> [rate]` | View or set interest rate | `ecore.economy`, `ecore.bank.admin` (to set) | `true` / `op` |

## Configuration

The bank system is configured in `config.yml`:

```yaml
bank:
  max-accounts: 5                 # Maximum bank accounts per player
  interest-rate: 0.01           # Default interest rate (1% per calculation)
  interest-interval: 86400     # Interest calculation interval in seconds (24 hours)
```

## Usage Guide

### Creating a Bank Account

1. Use `/bank create <name>` where `<name>` is a unique name for your account
2. The account is created with a balance of 0
3. You can now deposit money into it

### Depositing Money

1. Use `/bank deposit <account> <amount>` to deposit money
2. The amount is deducted from your main balance and added to the account
3. You must have sufficient funds in your main balance

### Withdrawing Money

1. Use `/bank withdraw <account> <amount>` to withdraw money
2. The amount is deducted from the account and added to your main balance
3. You must have sufficient funds in the account

### Transferring Between Accounts

1. Use `/bank transfer <from> <to> <amount>` to transfer money
2. Money is moved from one account to another
3. Both accounts must exist and the source account must have sufficient funds

### Interest Rates

- Interest is calculated automatically at configured intervals
- Use `/bank interest <account>` to view the current interest rate
- Staff can set interest rates with `/bank interest <account> <rate>` (requires `ecore.bank.admin`)

### Listing Accounts

- Use `/bank list` to see all your bank accounts and their balances

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.economy` | Use bank commands | `true` |
| `ecore.bank.admin` | Set bank interest rates | `op` |

## Tips

- Create separate accounts for different purposes (e.g., "Savings", "Shopping", "Trading")
- Use interest-earning accounts to grow your money over time
- Transfer money between accounts to organize your finances
- Keep some money in your main balance for quick transactions

## Related Systems

- [Economy System](economy-system.md) - For main balance and economy features
- [Shop System](shop-system.md) - For spending money

