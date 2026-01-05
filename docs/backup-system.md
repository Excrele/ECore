# Backup System

Automatic and scheduled backups with restoration capabilities.

## Overview

The backup system provides automatic backups, scheduled backups, and backup restoration to protect your server data.

## Features

- **Automatic Backups**: Automatic backups on configurable intervals
- **Scheduled Backups**: Configurable backup schedule
- **Backup Restoration**: Restore backups easily
- **Backup Compression**: ZIP format for efficient storage
- **Configurable Retention**: Max backups to keep
- **Manual Backups**: Create backups on demand

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/backup create` | Create a backup | `ecore.backup.create` | `op` |
| `/backup list` | List all backups | `ecore.backup.list` | `op` |
| `/backup restore <name>` | Restore a backup | `ecore.backup.restore` | `op` |
| `/backup reload` | Reload backup configuration | `ecore.backup.reload` | `op` |

## Configuration

Backup system is configured in `config.yml`:

```yaml
backup:
  enabled: true                    # Enable backup system
  directory: "backups"            # Backup directory
  interval: 3600                  # Backup interval in seconds (1 hour)
  max-backups: 10                 # Maximum backups to keep
  compress: true                   # Compress backups (ZIP)
```

## Usage Guide

### Creating Backups

**Manual Backup:**
1. Use `/backup create` to create a backup immediately
2. Backup is saved with timestamp
3. Includes all plugin data

**Automatic Backups:**
- Backups are created automatically at configured intervals
- No action needed
- Old backups are automatically deleted when limit is reached

### Listing Backups

- Use `/backup list` to see all backups
- Shows backup names, dates, and sizes
- Helps identify which backup to restore

### Restoring Backups

1. Use `/backup list` to see available backups
2. Use `/backup restore <name>` to restore a backup
3. **Warning:** This will overwrite current data
4. Server may need restart after restoration

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.backup.create` | Create backups | `op` |
| `ecore.backup.list` | List backups | `op` |
| `ecore.backup.restore` | Restore backups | `op` |
| `ecore.backup.reload` | Reload backup configuration | `op` |

## Tips

- Enable automatic backups for safety
- Set reasonable backup intervals
- Keep multiple backups for redundancy
- Test backup restoration regularly
- Store backups in safe location
- Compress backups to save space

## Related Systems

- All systems benefit from backups

