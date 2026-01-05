# Custom Scoreboard & Tab List

Fully customizable scoreboards and tab lists with PlaceholderAPI support.

## Overview

The custom scoreboard and tab list system allows you to create beautiful, dynamic displays with placeholders, per-world support, and per-group customization.

## Features

- **Custom Scoreboards**: Fully customizable scoreboard with placeholders
- **Placeholder Support**: ECore and PlaceholderAPI placeholders
- **Per-World Scoreboards**: Different scoreboards for different worlds (optional)
- **Per-Group Scoreboards**: Different scoreboards for permission groups (optional)
- **Custom Tab List**: Customizable header and footer
- **Multiline Support**: Support for multiline headers and footers
- **Separate Config Files**: `scoreboard.yml` and `tablist.yml` for easy editing

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/scoreboard toggle` | Toggle your scoreboard | `ecore.scoreboard.use` | `true` |
| `/scoreboard reload` | Reload scoreboard config | `ecore.scoreboard.reload` | `op` |
| `/scoreboard tablist reload` | Reload tab list config | `ecore.tablist.reload` | `op` |
| `/sb` | Alias for scoreboard | `ecore.scoreboard.use` | `true` |

## Configuration

### Scoreboard Configuration (`scoreboard.yml`)

```yaml
enabled: true
title: "&6&lYour Server"
update-interval: 20
lines:
  - "&7&m-------------------"
  - "&eBalance: &a%ecore_balance%"
  - "&eHomes: &a%ecore_homes%/%ecore_max_homes%"
  - "&eKills: &a%ecore_kills%"
  - "&7&m-------------------"
```

### Tab List Configuration (`tablist.yml`)

```yaml
enabled: true
update-interval: 20
header:
  - "&6&lWelcome to Server!"
  - "&7Visit our website!"
footer:
  - "&7Players Online: &a%ecore_online%"
```

## Usage Guide

### Configuring Scoreboard

1. Edit `scoreboard.yml` in `plugins/Ecore/`
2. Set `enabled: true` to enable the scoreboard
3. Configure `title` with color codes
4. Add `lines` with placeholders
5. Set `update-interval` (in ticks, 20 = 1 second)
6. Use `/scoreboard reload` to apply changes

### Configuring Tab List

1. Edit `tablist.yml` in `plugins/Ecore/`
2. Set `enabled: true` to enable the tab list
3. Configure `header` (multiline supported)
4. Configure `footer` (multiline supported)
5. Set `update-interval`
6. Use `/scoreboard tablist reload` to apply changes

### Placeholders

Use ECore placeholders:
- `%ecore_balance%` - Player balance
- `%ecore_homes%` - Number of homes
- `%ecore_kills%` - Kill count
- And many more...

Use PlaceholderAPI placeholders (if installed):
- `%player_name%` - Player name
- `%server_tps%` - Server TPS
- And all other PlaceholderAPI placeholders

### Color Codes

Use `&` for color codes:
- `&a` - Green
- `&c` - Red
- `&e` - Yellow
- `&6` - Gold
- `&b` - Aqua
- `&l` - Bold
- `&m` - Strikethrough
- And more...

### Toggling Scoreboard

- Players can use `/scoreboard toggle` to hide/show their scoreboard
- Useful for screenshots or personal preference

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.scoreboard.use` | Use scoreboard | `true` |
| `ecore.scoreboard.reload` | Reload scoreboard config | `op` |
| `ecore.tablist.use` | Use custom tab list | `true` |
| `ecore.tablist.reload` | Reload tab list config | `op` |

## Tips

- Use placeholders for dynamic content
- Update intervals affect performance (lower = more updates)
- Use color codes for better visuals
- Test scoreboard with `/scoreboard reload`
- Use multiline for tab list headers/footers
- Check PlaceholderAPI for more placeholders

## Related Systems

- [PlaceholderAPI](placeholderapi.md) - For additional placeholders

