# Quest System

Extensive quest system with 100+ predefined quests, chains, and daily/weekly quests.

## Overview

The quest system provides a comprehensive questing experience with multiple quest types, quest chains, prerequisites, and rewards.

## Features

- **100+ Predefined Quests**: Extensive quest library included
- **Quest Types**: Kill, Collect, Craft, Break, Place, Fish, Breed, Travel, Eat, Enchant, Trade, Mine, Harvest, and Custom quests
- **Quest Chains**: Quests with prerequisites and chains
- **Quest Rewards**: Money, items, and experience rewards
- **Quest GUI**: Browse quests by category with filtering
- **Daily/Weekly Quests**: Automatically resetting quests
- **Quest Progress Tracking**: Real-time progress updates

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/quest` | Open quest GUI | `ecore.quests` | `true` |
| `/quests` | Alias for quest | `ecore.quests` | `true` |
| `/quest list [category]` | List available quests | `ecore.quests` | `true` |
| `/quest start <quest-id>` | Start a quest | `ecore.quests` | `true` |
| `/quest active` | View active quests | `ecore.quests` | `true` |
| `/quest completed` | View completed quests | `ecore.quests` | `true` |
| `/quest info <quest-id>` | View quest information | `ecore.quests` | `true` |

## Configuration

Quests are configured in `quests.yml`:

```yaml
quests:
  kill-zombies:
    type: KILL
    target: ZOMBIE
    amount: 10
    rewards:
      money: 100.0
      items:
        - DIAMOND:1
    prerequisites: []
    chain: beginner-quests
```

## Quest Types

- **KILL**: Kill specific mobs
- **COLLECT**: Collect specific items
- **CRAFT**: Craft specific items
- **BREAK**: Break specific blocks
- **PLACE**: Place specific blocks
- **FISH**: Catch fish
- **BREED**: Breed animals
- **TRAVEL**: Travel distance
- **EAT**: Eat specific foods
- **ENCHANT**: Enchant items
- **TRADE**: Trade with villagers
- **MINE**: Mine specific blocks
- **HARVEST**: Harvest crops
- **CUSTOM**: Custom quest objectives

## Usage Guide

### Starting Quests

1. Use `/quest` to open the quest GUI
2. Browse available quests by category
3. Click on a quest to view details
4. Click "Start Quest" or use `/quest start <quest-id>`

### Completing Quests

1. Complete the quest objectives (kill mobs, collect items, etc.)
2. Progress is tracked automatically
3. When complete, you receive rewards
4. Quest is marked as completed

### Quest Chains

Some quests have prerequisites:
- Complete prerequisite quests first
- Quest chains unlock in order
- Follow the chain to complete all quests

### Daily/Weekly Quests

- Daily quests reset every 24 hours
- Weekly quests reset every 7 days
- Complete them again for rewards

### Quest Information

- Use `/quest info <quest-id>` to view:
  - Quest type and objectives
  - Progress
  - Rewards
  - Prerequisites
  - Quest chain

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.quests` | Use quest commands | `true` |
| `ecore.quests.admin` | Manage quests (admin only) | `op` |

## Tips

- Start with beginner quests
- Complete quest chains for better rewards
- Check daily/weekly quests regularly
- Use the quest GUI for easy browsing
- Track progress with `/quest active`

## Related Systems

- [Economy System](economy-system.md) - For quest rewards
- [Statistics System](statistics-achievements.md) - For quest progress tracking

