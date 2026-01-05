# Performance Optimization Module Implementation

## Overview

A comprehensive performance optimization system has been integrated into ECore, providing ClearLagg-like functionality with automatic entity cleanup, item stacking, chunk optimization, and TPS-based auto-cleanup. The system is fully integrated with the existing ServerInfoManager and ServerInfoCommand.

**Current Status**: ✅ **Fully Implemented and Active**

## Features Implemented

### 1. Automatic Entity Cleanup
- ✅ Item cleanup (removes excessive dropped items)
- ✅ Mob cleanup (removes excessive mobs per chunk)
- ✅ Projectile cleanup (removes old projectiles > 30 seconds)
- ✅ Configurable thresholds per chunk
- ✅ Scheduled automatic cleanup (runs every configured interval)
- ⚠️ **Note**: Cleanup operations run synchronously on the main thread. For very large servers, consider async optimization.

### 2. TPS-Based Auto-Cleanup
- ✅ Monitors TPS using ServerInfoManager (tracks last 100 ticks)
- ✅ Automatically triggers cleanup when TPS drops below threshold
- ✅ Emergency cleanup for performance issues
- ✅ Configurable TPS threshold (default: 15.0)
- ✅ Regular scheduled cleanup when TPS is healthy

### 3. Item Stacking
- ✅ Merges nearby items of the same type
- ✅ Configurable merge radius (default: 5.0 blocks)
- ✅ Prevents item spam
- ✅ Reduces entity count
- ✅ Respects max stack sizes and item metadata

### 4. Chunk Optimization
- ✅ Unloads empty chunks (no players/entities)
- ✅ Reduces memory usage
- ✅ Improves server performance
- ✅ Safe chunk unloading (only truly empty chunks)

### 5. Performance Statistics
- ✅ Detailed performance metrics
- ✅ Entity breakdown (items, mobs, other, players)
- ✅ Memory statistics (used/max with percentage)
- ✅ TPS monitoring (real-time, color-coded)
- ✅ Integration with ServerInfoCommand
- ✅ Chunk and entity counts

## Files Created/Modified

### New Files
1. **PerformanceManager.java** (`src/main/java/com/excrele/ecore/managers/PerformanceManager.java`)
   - Entity cleanup operations (`performCleanup()`)
   - Item stacking functionality (`mergeItems()`)
   - Chunk optimization (`optimizeChunks()`)
   - Performance statistics (`getPerformanceStats()`)
   - Auto-cleanup scheduling (`scheduleAutoCleanup()`)
   - Cleanup result tracking (`CleanupResult` class)
   - Performance stats data structure (`PerformanceStats` class)

### Modified Files
1. **ServerInfoCommand.java** (`src/main/java/com/excrele/ecore/commands/ServerInfoCommand.java`)
   - Enhanced with performance subcommands: `clear`, `stats`, `merge`, `chunks`, `help`
   - Integrated with PerformanceManager
   - Tab completion support for subcommands
   - Permission checks for each subcommand
   - Formatted output with color coding

2. **Ecore.java** (`src/main/java/com/excrele/ecore/Ecore.java`)
   - Registered PerformanceManager on plugin enable (line 168)
   - PerformanceManager getter method (line 622)
   - Proper initialization order

3. **config.yml** (`src/main/resources/config.yml`)
   - Added comprehensive performance configuration section (lines 237-259)
   - All settings are configurable with sensible defaults

4. **plugin.yml** (`src/main/resources/plugin.yml`)
   - Added performance-related permissions (lines 606-617):
     - `ecore.serverinfo.cleanup`
     - `ecore.serverinfo.stats`
     - `ecore.serverinfo.merge`
     - `ecore.serverinfo.chunks`

## Commands

### Main Command
- `/serverinfo` - Shows server information (existing, enhanced)

### New Subcommands
- `/serverinfo clear` or `/serverinfo cleanup` - Perform entity cleanup
- `/serverinfo stats` or `/serverinfo lag` - Show detailed performance statistics
- `/serverinfo merge [radius]` - Merge nearby items (default radius: 5.0)
- `/serverinfo chunks` - Optimize chunks (unload empty chunks)
- `/serverinfo help` - Show command help

## Permissions

- `ecore.serverinfo` - View server information (default: op)
- `ecore.serverinfo.cleanup` - Perform cleanup (default: op)
- `ecore.serverinfo.stats` - View performance stats (default: op)
- `ecore.serverinfo.merge` - Merge items (default: op)
- `ecore.serverinfo.chunks` - Optimize chunks (default: op)

## Configuration

Added to `config.yml`:

```yaml
performance:
  auto-cleanup:
    enabled: true
    interval: 300  # Seconds (5 minutes)
    tps-threshold: 15.0  # TPS threshold for emergency cleanup
    max-items-per-chunk: 100
    max-entities-per-chunk: 50
    clean-items: true
    clean-mobs: true
    clean-projectiles: true
  
  item-stacking:
    enabled: true
    radius: 5.0  # Blocks
  
  chunk-optimization:
    enabled: true
    unload-empty-chunks: true
```

## Usage Examples

### View Server Information
```
/serverinfo
```
Shows basic server information including TPS, memory, players, etc.

### Perform Manual Cleanup
```
/serverinfo clear
```
Manually triggers entity cleanup and shows results.

### View Performance Statistics
```
/serverinfo stats
```
Shows detailed performance metrics including entity breakdown.

### Merge Items
```
/serverinfo merge 5.0
```
Merges nearby items within 5 blocks radius.

### Optimize Chunks
```
/serverinfo chunks
```
Unloads empty chunks to free memory.

## How It Works

### Automatic Cleanup
1. Scheduled cleanup runs every X seconds (configurable)
2. Checks TPS using ServerInfoManager
3. If TPS < threshold, performs emergency cleanup
4. Otherwise, performs regular scheduled cleanup
5. Removes excessive entities based on per-chunk limits

### Item Stacking
1. Scans all worlds for dropped items
2. Finds items of the same type within radius
3. Merges them into single stacks
4. Respects max stack sizes

### Chunk Optimization
1. Scans all loaded chunks
2. Checks for players and entities
3. Unloads chunks with no activity
4. Reduces memory footprint

### TPS Monitoring
- Uses existing ServerInfoManager TPS tracking
- No duplicate TPS calculation
- Seamless integration

## Performance Impact

### Benefits
- Reduces entity count (improves TPS)
- Lowers memory usage (chunk optimization)
- Prevents item spam
- Automatic maintenance
- Configurable thresholds
- TPS-based emergency cleanup prevents server crashes

### Overhead
- Minimal - cleanup runs on schedule (not every tick)
- Cleanup interval is configurable (default: 300 seconds / 5 minutes)
- Efficient entity scanning (per-chunk processing)
- Smart chunk checking (only processes loaded chunks)
- ⚠️ **Current Limitation**: Cleanup operations run synchronously on the main thread. For servers with thousands of entities, this may cause brief lag spikes during cleanup. Consider async optimization for future enhancement.

## Integration with ServerInfoManager

The PerformanceManager integrates seamlessly with ServerInfoManager:
- Uses ServerInfoManager.getTPS() for TPS monitoring
- Uses ServerInfoManager.getMemoryInfo() for memory stats
- No duplicate code or calculations
- Shared performance metrics

## Additional Performance Optimizations in ECore

Beyond the PerformanceManager, ECore implements several other performance optimizations:

### 1. Async Database Operations
- **BlockLogDatabase**: All database queries run asynchronously
  - Block logging operations
  - Container access logging
  - Inventory snapshots
  - Log purging
- **Database Indexes**: Optimized indexes on frequently queried columns (time, player_uuid, location)

### 2. Async File I/O Operations
- **BackupManager**: Backup creation runs asynchronously
  - ZIP file creation on async thread
  - Old backup cleanup on async thread
  - Scheduled backups use async tasks

### 3. Batched Operations
- **WorldEditManager**: Large block operations are batched
  - Configurable `blocks-per-tick` setting (default: 1000)
  - Progress tracking and updates
  - Prevents server freezes during large operations
- **ChunkManager**: Chunk generation is batched
  - Generates 5 chunks per tick to avoid lag
  - Progress updates every 5 seconds

### 4. Caching
- **StatisticsManager**: Caches player distance calculations
- **ServerInfoManager**: TPS calculation uses rolling window (last 100 ticks)

### 5. Scheduled Tasks Optimization
- Most recurring tasks use appropriate intervals
- Scoreboard/TabList updates are configurable
- AFK detection runs efficiently
- Quest/Job progress tracking is optimized

## Future Enhancements

Potential improvements:
- ⚠️ **High Priority**: Make cleanup operations async to prevent main thread blocking
- Per-world cleanup settings
- Whitelist/blacklist for entity types
- More granular cleanup options
- Performance GUI dashboard
- Cleanup history/logging
- Discord notifications for low TPS
- Advanced chunk optimization strategies
- Batch entity removal for better performance
- Configurable cleanup batch sizes

## Implementation Details

### Cleanup Logic
- **Item Cleanup**: Removes items when chunk exceeds `max-items-per-chunk` threshold
- **Mob Cleanup**: Removes living entities (excluding players) when chunk exceeds `max-entities-per-chunk` threshold
- **Projectile Cleanup**: Removes projectiles older than 30 seconds (calculated from `getTicksLived()`)
- **Safety**: All cleanup operations are safe - doesn't remove player items in use or players themselves

### Item Stacking Logic
- Scans all worlds for dropped items
- Compares items by type and metadata
- Merges items within configured radius
- Respects max stack sizes (splits if needed)
- Removes merged item entities

### Chunk Optimization Logic
- Scans all loaded chunks in all worlds
- Checks for players and entities in each chunk
- Only unloads chunks with no players AND no entities
- Safe operation - doesn't affect active gameplay

### TPS Monitoring
- Uses `ServerInfoManager` which tracks last 100 tick times
- Calculates average tick time to determine TPS
- Capped at 20.0 TPS (Minecraft's maximum)
- Color-coded display: Green (≥19), Yellow (≥15), Orange (≥10), Red (<10)

## Notes

- All cleanup operations are safe (doesn't remove player items in use)
- Projectiles are only removed if older than 30 seconds
- Chunk optimization only unloads truly empty chunks
- Item stacking respects item metadata (won't merge different items)
- Auto-cleanup respects configuration settings
- Manual commands require appropriate permissions
- Cleanup task runs every `interval` seconds (default: 300 seconds = 5 minutes)
- Emergency cleanup triggers when TPS < `tps-threshold` (default: 15.0)
- Regular cleanup runs even when TPS is healthy (preventive maintenance)

## Code Quality

- ✅ Proper null checks throughout
- ✅ Configuration validation with defaults
- ✅ Clean shutdown handling (cancels tasks on disable)
- ✅ Integration with existing managers
- ✅ Well-documented code with JavaDoc comments
- ✅ Error handling for edge cases

---

**Implementation Date**: Current
**Version**: 1.0.1
**Status**: ✅ Complete and Ready for Use
**Integration**: Fully integrated with ServerInfoManager and Ecore plugin lifecycle
**Last Updated**: Based on current codebase analysis

