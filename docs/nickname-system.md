# Nickname System

Enhanced nicknames with colors and formatting.

## Overview

The nickname system allows players to set custom display names with colors and formatting.

## Features

- **Custom Nicknames**: Set custom display names
- **Color Support**: Use colors in nicknames
- **Formatting Support**: Use formatting codes
- **Nickname Viewing**: View player nicknames

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/nick <nickname>` | Set your nickname | `ecore.nickname` | `true` |
| `/nickname <nickname>` | Alias for nick | `ecore.nickname` | `true` |
| `/nick set <nickname>` | Set nickname (explicit) | `ecore.nickname` | `true` |
| `/nick reset` | Reset your nickname | `ecore.nickname` | `true` |
| `/nick color <color>` | Set nickname color | `ecore.nickname.color` | `false` |
| `/nick format <format>` | Set nickname format | `ecore.nickname.format` | `false` |
| `/nick view [player]` | View nickname | `ecore.nickname`, `ecore.nickname.view.others` | `true` |

## Usage Guide

### Setting Nicknames

- Use `/nick <nickname>` to set your nickname
- Use `/nick reset` to remove nickname
- Nickname appears in chat and tab list

### Colors and Formatting

- Use `/nick color <color>` to set color (requires permission)
- Use `/nick format <format>` to set formatting (requires permission)
- Use color codes: `&a` (green), `&c` (red), etc.

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.nickname` | Set and use nicknames | `true` |
| `ecore.nickname.color` | Use colors in nicknames | `false` |
| `ecore.nickname.format` | Use custom formatting in nicknames | `false` |
| `ecore.nickname.view.others` | View other players' nicknames | `true` |

## Tips

- Use nicknames for roleplay
- Colors require permission
- Reset nickname to remove
- View nicknames to see custom names

