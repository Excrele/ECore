# PlaceholderAPI

Extensive PlaceholderAPI support for use in any PlaceholderAPI-supported plugin.

## Overview

ECore provides extensive PlaceholderAPI support, automatically registering placeholders when PlaceholderAPI is installed.

## Features

- **Automatic Registration**: Placeholders register automatically when PlaceholderAPI is installed
- **Extensive Placeholders**: 20+ placeholders for various statistics
- **No Configuration**: Works out of the box
- **Plugin Integration**: Works with any PlaceholderAPI-supported plugin

## Available Placeholders

| Placeholder | Description |
|-------------|-------------|
| `%ecore_homes%` | Number of homes |
| `%ecore_max_homes%` | Maximum homes allowed |
| `%ecore_balance%` | Player balance |
| `%ecore_playtime%` | Playtime formatted (hours and minutes) |
| `%ecore_kills%` | Number of kills |
| `%ecore_deaths%` | Number of deaths |
| `%ecore_kdr%` | Kill/Death ratio |
| `%ecore_achievements%` | Achievement count |
| `%ecore_distance%` | Distance traveled (meters or kilometers) |
| `%ecore_items_crafted%` | Items crafted |
| `%ecore_experience_gained%` | Experience gained |
| `%ecore_damage_taken%` | Damage taken |
| `%ecore_damage_dealt%` | Damage dealt |
| `%ecore_joins%` | Number of joins |
| `%ecore_mail_count%` | Unread mail count |

## Usage

### In Scoreboards

Use placeholders in `scoreboard.yml`:

```yaml
lines:
  - "&eBalance: &a%ecore_balance%"
  - "&eHomes: &a%ecore_homes%/%ecore_max_homes%"
  - "&eKills: &a%ecore_kills%"
```

### In Tab List

Use placeholders in `tablist.yml`:

```yaml
header:
  - "&6Balance: &a%ecore_balance%"
```

### In Other Plugins

Use placeholders in any PlaceholderAPI-supported plugin:
- Chat plugins
- Hologram plugins
- Boss bar plugins
- And more...

## Example Usage

```
%ecore_balance% - Shows player's balance
%ecore_kdr% - Shows kill/death ratio
%ecore_homes%/%ecore_max_homes% - Shows homes (e.g., "3/5")
```

## Integration

ECore automatically registers PlaceholderAPI expansion when PlaceholderAPI is installed. No configuration needed.

## Tips

- Use placeholders in scoreboards for dynamic content
- Combine with color codes for better visuals
- Use in chat plugins for player info
- Check PlaceholderAPI documentation for more features
- Test placeholders before using in production

## Related Systems

- [Custom Scoreboard & Tab List](custom-scoreboard-tablist.md) - For using placeholders in scoreboards
- [Statistics & Achievements](statistics-achievements.md) - For statistics placeholders

