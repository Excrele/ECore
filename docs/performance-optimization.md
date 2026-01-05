# Performance Optimization

Built-in lag reduction and optimization features to keep your server running smoothly.

## Overview

ECore includes comprehensive performance optimization features to automatically reduce lag, clean up entities, and optimize server performance.

## Features

- **Automatic Cleanup**: Removes excessive entities (items, mobs, projectiles)
- **TPS-Based Cleanup**: Automatically triggers when TPS drops below threshold
- **Item Stacking**: Merges nearby items of the same type
- **Chunk Optimization**: Unloads empty chunks to reduce memory usage
- **Performance Statistics**: Detailed metrics and entity breakdown
- **Scheduled Maintenance**: Automatic cleanup on configurable intervals

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/serverinfo` | Display server information | `ecore.serverinfo` | `op` |
| `/serverinfo clear` | Perform entity cleanup | `ecore.serverinfo.cleanup` | `op` |
| `/serverinfo stats` | Show performance statistics | `ecore.serverinfo.stats` | `op` |
| `/serverinfo merge [radius]` | Merge nearby items | `ecore.serverinfo.merge` | `op` |
| `/serverinfo chunks` | Optimize chunks | `ecore.serverinfo.chunks` | `op` |

## Configuration

Performance optimization is configured in `config.yml`:

```yaml
performance:
  auto-cleanup:
    enabled: true                 # Enable automatic cleanup
    interval: 300                # Cleanup every 5 minutes (in seconds)
    tps-threshold: 15.0          # Emergency cleanup if TPS < 15
    max-items-per-chunk: 100     # Maximum items per chunk
    max-entities-per-chunk: 50    # Maximum entities per chunk
  item-stacking:
    enabled: true                 # Enable item stacking
    radius: 2.0                   # Radius to search for items to merge
    merge-delay: 5                # Seconds before items merge
  chunk-optimization:
    enabled: true                 # Enable chunk optimization
    unload-empty-chunks: true    # Unload empty chunks
    unload-delay: 300            # Seconds before unloading empty chunks
```

## Usage Guide

### Automatic Cleanup

The automatic cleanup system runs on a schedule and when TPS drops:

1. **Scheduled Cleanup**: Runs every configured interval (default: 5 minutes)
2. **Emergency Cleanup**: Triggers when TPS drops below threshold (default: 15 TPS)
3. **Entity Limits**: Removes entities when limits are exceeded per chunk

**What Gets Cleaned:**
- Dropped items (beyond limit)
- Hostile mobs (beyond limit)
- Projectiles (arrows, etc.)
- Other entities (beyond limit)

### Manual Cleanup

1. Use `/serverinfo clear` to manually trigger cleanup
2. All excessive entities are removed immediately
3. Useful before events or when lag is noticed

### Item Stacking

1. Items of the same type within radius merge together
2. Reduces entity count and improves performance
3. Use `/serverinfo merge` to manually merge items
4. Use `/serverinfo merge <radius>` to specify merge radius

### Chunk Optimization

1. Empty chunks are automatically unloaded
2. Reduces memory usage
3. Use `/serverinfo chunks` to manually optimize chunks

### Performance Statistics

1. Use `/serverinfo stats` to view detailed performance metrics
2. See entity counts, TPS, memory usage, and more
3. Helps identify performance issues

## Server Information

Use `/serverinfo` to view:
- **TPS**: Server ticks per second
- **Memory**: RAM usage
- **Uptime**: Server uptime
- **Online Players**: Number of online players
- **Entities**: Entity counts

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.serverinfo` | View server info | `op` |
| `ecore.serverinfo.cleanup` | Perform cleanup | `op` |
| `ecore.serverinfo.stats` | View statistics | `op` |
| `ecore.serverinfo.merge` | Merge items | `op` |
| `ecore.serverinfo.chunks` | Optimize chunks | `op` |

## Tips

- Enable automatic cleanup for best results
- Set appropriate entity limits for your server size
- Use emergency cleanup threshold to prevent severe lag
- Monitor TPS regularly with `/serverinfo`
- Use item stacking to reduce entity count
- Optimize chunks on large servers
- Check performance statistics to identify issues

## Related Systems

- [Server Info Commands](staff-management.md#server-info-commands) - For server monitoring

