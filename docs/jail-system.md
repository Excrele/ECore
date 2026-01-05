# Jail System

Jail players with configurable locations and durations.

## Overview

The jail system allows staff to jail players at specific locations for configurable durations.

## Features

- **Jail Locations**: Create multiple jail locations
- **Jail Duration**: Configurable jail durations
- **Jail Reasons**: Optional reasons for jailing
- **Jail Information**: View jail status

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/jail <player> <jail> [time] [reason]` | Jail a player | `ecore.jail` | `op` |
| `/unjail <player>` | Unjail a player | `ecore.jail` | `op` |
| `/setjail <name>` | Create a jail location | `ecore.jail.set` | `op` |
| `/jailinfo <player>` | Check jail information | `ecore.jail` | `op` |

## Usage Guide

### Creating Jails

1. Stand at the location for the jail
2. Use `/setjail <name>` to create the jail
3. Jail is ready to use

### Jailing Players

1. Use `/jail <player> <jail>` to jail a player
2. Use `/jail <player> <jail> <time>` to jail for duration
3. Use `/jail <player> <jail> <time> <reason>` to add reason
4. Example: `/jail Player1 spawn 1h Griefing`

### Unjailing Players

- Use `/unjail <player>` to release from jail

### Jail Information

- Use `/jailinfo <player>` to view:
  - Jail location
  - Time remaining
  - Reason

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.jail` | Jail players | `op` |
| `ecore.jail.set` | Create jails | `op` |

## Tips

- Create multiple jails for different purposes
- Use durations for temporary jails
- Add reasons for record keeping
- Check jail info to see status

