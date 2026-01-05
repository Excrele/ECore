# Custom Recipes

Create shaped and shapeless custom crafting recipes.

## Overview

The custom recipes system allows you to create custom crafting recipes that players can use in crafting tables, providing unique item recipes for your server.

## Features

- **Shaped Recipes**: Create shaped crafting recipes
- **Shapeless Recipes**: Create shapeless crafting recipes
- **Recipe Management**: Create, remove, and list recipes
- **Recipe Permissions**: Per-recipe permission support
- **Hot-Reload**: Reload recipes without restart

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/recipe list` | List all custom recipes | `ecore.recipe` | `true` |
| `/recipe create <id> <shaped\|shapeless>` | Create a recipe (admin) | `ecore.recipe.admin` | `op` |
| `/recipe remove <id>` | Remove a recipe (admin) | `ecore.recipe.admin` | `op` |
| `/recipe reload` | Reload recipes from config (admin) | `ecore.recipe.admin` | `op` |

## Configuration

Custom recipes are stored in `recipes.yml`:

```yaml
recipes:
  custom-diamond:
    type: SHAPED
    result: DIAMOND
    amount: 1
    shape:
      - "GGG"
      - "GIG"
      - "GGG"
    ingredients:
      G: GOLD_INGOT
      I: IRON_INGOT
    permission: ecore.recipe.custom-diamond
```

## Usage Guide

### Creating Recipes

**Shaped Recipe:**
1. Use `/recipe create <id> shaped` to start creating
2. Define the shape (3x3 grid)
3. Define ingredients for each character
4. Set the result item

**Shapeless Recipe:**
1. Use `/recipe create <id> shapeless` to start creating
2. Define ingredients (list of items)
3. Set the result item

### Recipe Permissions

- Add `permission` to recipes to restrict access
- Players need the permission to craft the recipe
- Leave empty for public recipes

### Listing Recipes

- Use `/recipe list` to see all custom recipes
- Shows recipe ID and result

### Removing Recipes

- Use `/recipe remove <id>` to remove a recipe
- Recipe is immediately removed

### Reloading Recipes

- Use `/recipe reload` to reload recipes from config
- No server restart needed

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.recipe` | View custom recipes | `true` |
| `ecore.recipe.admin` | Manage custom recipes | `op` |
| `ecore.recipe.<id>` | Craft specific recipe | `true` (if recipe has permission) |

## Tips

- Create unique recipes for server-specific items
- Use permissions to restrict powerful recipes
- Test recipes before making them public
- Use shaped recipes for complex patterns
- Use shapeless recipes for simpler recipes
- Reload recipes after editing config

## Related Systems

- [Economy System](economy-system.md) - For recipe costs (if configured)

