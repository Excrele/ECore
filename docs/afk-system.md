# AFK System

Automatic AFK detection and manual toggle.

## Overview

The AFK system automatically detects when players are away and allows manual AFK toggling.

## Features

- **Automatic Detection**: Detects inactivity automatically
- **Manual Toggle**: Players can toggle AFK manually
- **AFK Status**: Shows AFK status to other players
- **AFK Commands**: Check AFK status

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/afk` | Toggle AFK status | `ecore.afk` | `true` |
| `/afk <player>` | Check player AFK status | `ecore.afk.check` | `true` |

## Usage Guide

### Toggling AFK

- Use `/afk` to toggle your AFK status
- Use again to remove AFK status

### Checking AFK Status

- Use `/afk <player>` to check if a player is AFK

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.afk` | Use AFK command | `true` |
| `ecore.afk.check` | Check others' AFK status | `true` |

## Tips

- Use AFK when away from keyboard
- Check AFK status before messaging players
- Automatic detection helps identify inactive players

