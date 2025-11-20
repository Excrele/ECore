# Performance Optimization Module Implementation

## Overview

A comprehensive performance optimization system has been integrated into ECore, providing ClearLagg-like functionality with automatic entity cleanup, item stacking, chunk optimization, and TPS-based auto-cleanup. The system is fully integrated with the existing ServerInfoManager and ServerInfoCommand.

## Features Implemented

### 1. Automatic Entity Cleanup
- ✅ Item cleanup (removes excessive dropped items)
- ✅ Mob cleanup (removes excessive mobs per chunk)
- ✅ Projectile cleanup (removes old projectiles)
- ✅ Configurable thresholds per chunk
- ✅ Scheduled automatic cleanup

### 2. TPS-Based Auto-Cleanup
- ✅ Monitors TPS using ServerInfoManager
- ✅ Automatically triggers cleanup when TPS drops below threshold
- ✅ Emergency cleanup for performance issues
- ✅ Configurable TPS threshold

### 3. Item Stacking
- ✅ Merges nearby items of the same type
- ✅ Configurable merge radius
- ✅ Prevents item spam
- ✅ Reduces entity count

### 4. Chunk Optimization
- ✅ Unloads empty chunks (no players/entities)
- ✅ Reduces memory usage
- ✅ Improves server performance

### 5. Performance Statistics
- ✅ Detailed performance metrics
- ✅ Entity breakdown (items, mobs, other)
- ✅ Memory statistics
- ✅ TPS monitoring
- ✅ Integration with ServerInfoCommand

## Files Created/Modified

### New Files
1. **PerformanceManager.java** - Core performance optimization manager
   - Entity cleanup operations
   - Item stacking functionality
   - Chunk optimization
   - Performance statistics
   - Auto-cleanup scheduling

### Modified Files
1. **ServerInfoCommand.java** - Enhanced with performance commands
   - Added subcommands: clear, stats, merge, chunks
   - Integrated with PerformanceManager
   - Tab completion support

2. **Ecore.java** - Registered PerformanceManager
   - Initialization on plugin enable
   - Shutdown on plugin disable

3. **config.yml** - Added performance configuration section

4. **plugin.yml** - Added performance-related permissions

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

### Overhead
- Minimal - cleanup runs on schedule (not every tick)
- Async-friendly operations
- Efficient entity scanning
- Smart chunk checking

## Integration with ServerInfoManager

The PerformanceManager integrates seamlessly with ServerInfoManager:
- Uses ServerInfoManager.getTPS() for TPS monitoring
- Uses ServerInfoManager.getMemoryInfo() for memory stats
- No duplicate code or calculations
- Shared performance metrics

## Future Enhancements

Potential improvements:
- Per-world cleanup settings
- Whitelist/blacklist for entity types
- More granular cleanup options
- Performance GUI dashboard
- Cleanup history/logging
- Discord notifications for low TPS
- Advanced chunk optimization strategies

## Notes

- All cleanup operations are safe (doesn't remove player items in use)
- Projectiles are only removed if older than 30 seconds
- Chunk optimization only unloads truly empty chunks
- Item stacking respects item metadata (won't merge different items)
- Auto-cleanup respects configuration settings
- Manual commands require appropriate permissions

---

**Implementation Date**: Current
**Version**: 1.0
**Status**: ✅ Complete and Ready for Use
**Integration**: Fully integrated with ServerInfoManager

