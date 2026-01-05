# Statistics & Achievements

Track player statistics and unlockable achievements with rewards.

## Overview

The statistics and achievements system tracks player progress and provides unlockable achievements with rewards.

## Features

- **Statistics Tracking**: Track kills, deaths, distance, items crafted, and more
- **Achievement System**: Unlockable achievements with rewards
- **Statistics GUI**: View your statistics easily
- **Achievement GUI**: Browse and track achievements
- **Leaderboards**: Compare with other players
- **Progress Tracking**: Real-time progress updates

## Commands

### Statistics Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/stats` | View your statistics (opens GUI) | `ecore.economy` | `true` |
| `/statistics` | Alias for stats | `ecore.economy` | `true` |
| `/leaderboard [stat]` | View leaderboards | `ecore.economy` | `true` |
| `/lb [stat]` | Alias for leaderboard | `ecore.economy` | `true` |
| `/statsreset <player> [stat]` | Reset player statistics | `ecore.stats.reset` | `op` |
| `/resetstats <player> [stat]` | Alias for statsreset | `ecore.stats.reset` | `op` |

### Achievement Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/achievements` | View your achievements | `ecore.economy` | `true` |
| `/achievement` | Alias for achievements | `ecore.economy` | `true` |
| `/achievement list` | List all achievements | `ecore.economy` | `true` |
| `/achievement give <player> <id>` | Give achievement to player | `ecore.achievement.give` | `op` |
| `/achievement check` | Check for new achievements | `ecore.economy` | `true` |

## Tracked Statistics

- **Kills**: Number of mob/player kills
- **Deaths**: Number of deaths
- **Distance**: Distance traveled
- **Items Crafted**: Items crafted
- **Experience Gained**: Total experience gained
- **Damage Taken**: Total damage taken
- **Damage Dealt**: Total damage dealt
- **Joins**: Number of server joins
- **Playtime**: Total playtime
- And more...

## Usage Guide

### Viewing Statistics

1. Use `/stats` to open the statistics GUI
2. Browse your statistics
3. See progress and rankings

### Leaderboards

- Use `/leaderboard` to view overall leaderboard
- Use `/leaderboard <stat>` to view leaderboard for specific stat
- Example: `/leaderboard kills` - Top players by kills

### Achievements

**Viewing Achievements:**
- Use `/achievements` to view your achievements
- Use `/achievement list` to see all available achievements
- Completed achievements show rewards

**Unlocking Achievements:**
- Achievements unlock automatically when conditions are met
- Use `/achievement check` to check for new achievements
- Receive rewards when unlocked

**Giving Achievements (Admin):**
- Use `/achievement give <player> <id>` to give achievement
- Useful for events or rewards

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.economy` | Use statistics commands | `true` |
| `ecore.stats.reset` | Reset player statistics | `op` |
| `ecore.achievement.give` | Give achievements to players | `op` |

## Tips

- Check statistics regularly to track progress
- Complete achievements for rewards
- Compete on leaderboards
- Use statistics for server events
- Reset statistics for seasonal competitions

## Related Systems

- [Economy System](economy-system.md) - For achievement rewards
- [PlaceholderAPI](placeholderapi.md) - For statistics placeholders

