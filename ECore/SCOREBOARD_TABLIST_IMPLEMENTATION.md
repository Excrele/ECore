# Custom Scoreboard & Tab List Implementation

## Overview

A comprehensive custom scoreboard and tab list system has been implemented for ECore, providing fully customizable displays with placeholder support and separate configuration files for easy editing.

## Features Implemented

### Scoreboard System
- ✅ Fully customizable scoreboard
- ✅ Placeholder support (ECore and PlaceholderAPI)
- ✅ Per-world scoreboards (optional)
- ✅ Per-group scoreboards (optional, requires LuckPerms)
- ✅ Configurable update intervals
- ✅ Toggle command for players
- ✅ Automatic setup on player join

### Tab List System
- ✅ Custom header and footer
- ✅ Multiline support
- ✅ Placeholder support
- ✅ Per-world tab lists (optional)
- ✅ Per-group tab lists (optional)
- ✅ Configurable update intervals
- ✅ Automatic setup on player join

## Files Created

1. **ScoreboardManager.java** - Scoreboard manager
   - Scoreboard creation and updates
   - Placeholder processing
   - Per-world/per-group support
   - Automatic updates

2. **TabListManager.java** - Tab list manager
   - Header/footer management
   - Placeholder processing
   - Per-world/per-group support
   - Automatic updates

3. **ScoreboardCommand.java** - Command handler
   - Toggle scoreboard
   - Reload configurations
   - Tab list reload

4. **scoreboard.yml** - Scoreboard configuration
   - Separate file for easy editing
   - Comprehensive configuration options

5. **tablist.yml** - Tab list configuration
   - Separate file for easy editing
   - Multiline support

## Commands

- `/scoreboard toggle` - Toggle your scoreboard on/off
- `/scoreboard reload` - Reload scoreboard config (admin)
- `/scoreboard tablist reload` - Reload tab list config (admin)
- `/sb` - Alias for scoreboard

## Permissions

- `ecore.scoreboard.use` - Use scoreboard (default: true)
- `ecore.scoreboard.reload` - Reload scoreboard config (default: op)
- `ecore.tablist.use` - Use custom tab list (default: true)
- `ecore.tablist.reload` - Reload tab list config (default: op)

## Configuration Files

### scoreboard.yml
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

### tablist.yml
```yaml
enabled: true
update-interval: 20
header: "&6&lWelcome to Server!"
footer: "&7Visit our website!"
```

## Placeholder Support

### ECore Placeholders
- `%player%` - Player name
- `%balance%` - Player balance
- `%homes%` - Number of homes
- `%kills%` - Number of kills
- `%deaths%` - Number of deaths
- `%online%` - Online players
- `%max%` - Max players
- `%tps%` - Server TPS

### PlaceholderAPI
All PlaceholderAPI placeholders are supported if PlaceholderAPI is installed.

## Usage

### For Players
- Scoreboards are automatically enabled on join
- Use `/scoreboard toggle` to turn it on/off
- Tab list is always active (if enabled)

### For Admins
- Edit `scoreboard.yml` to customize scoreboard
- Edit `tablist.yml` to customize tab list
- Use `/scoreboard reload` to apply changes without restart
- Use `/scoreboard tablist reload` to reload tab list

## Advanced Features

### Per-World Scoreboards
Configure different scoreboards for different worlds:
```yaml
per-world:
  enabled: true
  worlds:
    world:
      title: "&6&lOverworld"
      lines: [...]
```

### Per-Group Scoreboards
Configure different scoreboards for permission groups:
```yaml
per-group:
  enabled: true
  groups:
    vip:
      title: "&6&lVIP Scoreboard"
      lines: [...]
```

## Notes

- Scoreboards update automatically on configurable intervals
- Tab lists update automatically on configurable intervals
- Both systems support PlaceholderAPI if installed
- Separate config files make editing easier
- Players can toggle scoreboards on/off
- Tab lists are always active (if enabled)

---

**Implementation Date**: Current
**Version**: 1.0
**Status**: ✅ Complete and Ready for Use

