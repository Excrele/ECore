# Jobs System

Complete job system with levels, experience, and rewards.

## Overview

The jobs system allows players to join jobs, complete job-specific actions, earn experience, level up, and receive rewards.

## Features

- **Multiple Job Types**: Miner, Farmer, Hunter, Builder, Fisher, and more
- **Job Levels & Experience**: Level up through job-specific actions
- **Job Rewards**: Earn money and items from completing job actions
- **Job GUI**: Easy-to-use interface for browsing and joining jobs
- **Job Statistics**: Track your progress and earnings
- **Job Leaderboards**: Compare with other players
- **Job Progression**: Exponential leveling system with configurable rewards

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/jobs` | Open jobs GUI | `ecore.jobs` | `true` |
| `/jobs join <job>` | Join a job | `ecore.jobs` | `true` |
| `/jobs leave` | Leave current job | `ecore.jobs` | `true` |
| `/jobs info` | View job info | `ecore.jobs` | `true` |
| `/jobs top [job]` | View job leaderboard | `ecore.jobs` | `true` |
| `/jobs list` | List available jobs | `ecore.jobs` | `true` |

## Configuration

Jobs are configured in `jobs.yml`:

```yaml
jobs:
  miner:
    actions:
      break-stone:
        experience: 10
        money: 1.0
      break-ore:
        experience: 50
        money: 5.0
    levels:
      1:
        experience-required: 100
        rewards:
          money: 100.0
          items:
            - DIAMOND:1
```

## Usage Guide

### Joining a Job

1. Use `/jobs` to open the jobs GUI
2. Browse available jobs
3. Click on a job to join, or use `/jobs join <job>`

### Completing Job Actions

Jobs track specific actions:
- **Miner**: Breaking stone, ores, etc.
- **Farmer**: Harvesting crops, breeding animals, etc.
- **Hunter**: Killing mobs
- **Builder**: Placing blocks
- **Fisher**: Fishing

Perform these actions while having the job active to earn experience and rewards.

### Leveling Up

1. Complete job actions to earn experience
2. When you reach the required experience, you level up
3. Receive level-up rewards (money, items)
4. Higher levels require more experience (exponential system)

### Job Information

- Use `/jobs info` to view:
  - Current job
  - Current level
  - Experience progress
  - Next level requirements

### Job Leaderboards

- Use `/jobs top` to view overall job leaderboard
- Use `/jobs top <job>` to view leaderboard for specific job
- See top players by level and experience

## Job Types

Common job types include:
- **Miner**: Mining ores and stone
- **Farmer**: Farming crops and animals
- **Hunter**: Hunting mobs
- **Builder**: Building structures
- **Fisher**: Fishing
- **Lumberjack**: Cutting trees
- And more (configurable)

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.jobs` | Use jobs commands | `true` |
| `ecore.jobs.admin` | Manage jobs (admin only) | `op` |

## Tips

- Choose a job that matches your playstyle
- Focus on job actions to level up quickly
- Check leaderboards for competition
- Switch jobs if you want variety
- Level up to earn better rewards

## Related Systems

- [Economy System](economy-system.md) - For job rewards
- [Statistics System](statistics-achievements.md) - For job statistics

