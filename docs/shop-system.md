# Shop System

ECore includes both Admin Shops and Player Shops for buying and selling items.

## Overview

The shop system provides two types of shops:
- **Admin Shops**: Server-controlled shops with unlimited stock
- **Player Shops**: Player-owned shops using chest storage

Both shop types support buying and selling items with configurable prices.

## Features

- **Admin Shops**: Unlimited stock, server-controlled
- **Player Shops**: Player-owned with chest storage
- **Shop Categories**: Organize shops by category
- **Shop Favorites**: Bookmark favorite shops
- **Shop Statistics**: Track views, sales, and revenue
- **Shop GUI**: Browse and search shops easily
- **Shop Editing**: Edit prices and quantities
- **Automatic Expiration**: Inactive shops are automatically removed

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/shopedit <buy\|sell\|quantity> <value>` | Edit shop prices or quantity | `ecore.adminshop.edit` or `ecore.pshop.edit` | `op` |

## Configuration

The shop system is configured in `config.yml`:

```yaml
shops:
  max-shops-per-player: 10       # Maximum player shops per player
  expiration-days: 30            # Days before inactive shops expire
  enable-categories: true        # Enable shop categories
  enable-favorites: true         # Enable shop favorites
  enable-statistics: true        # Enable shop statistics
```

Shop data is stored in:
- `adminshops.yml` - Admin shop data (auto-generated)
- `playershops.yml` - Player shop data (auto-generated)

## Creating Shops

### Admin Shops

1. Place a sign and write `[Admin Shop]` on the first line
2. Right-click the sign while holding the item you want to sell
3. Enter quantity (1-64) in chat
4. Enter buy price in chat (price players pay to buy)
5. Enter sell price in chat (price players receive when selling)
6. Shop is created!

**Note:** Admin shops have unlimited stock and don't require storage.

### Player Shops

1. Place a chest
2. Place a sign on or next to the chest with `[PShop]` on the first line
3. Right-click the sign to open item selection GUI
4. Select an item from the GUI
5. Enter quantity (1-64) in chat
6. Enter buy price in chat
7. Enter sell price in chat
8. Shop is created! Stock items in the chest

**Note:** Player shops require items in the chest for selling. Players buy from the chest inventory.

## Using Shops

### Buying Items

- **Right-click** a shop sign to **buy** items
- The item will be added to your inventory
- Money will be deducted from your balance

### Selling Items

- **Left-click** a shop sign while holding the item to **sell** items
- The item will be removed from your hand
- Money will be added to your balance

## Shop Features

### Categories

Shops can be organized into categories for easier browsing. Categories are set when creating the shop.

### Favorites

Players can mark shops as favorites for quick access. Use the shop GUI to manage favorites.

### Statistics

Shops track:
- **Views**: How many times the shop was viewed
- **Sales**: Number of transactions
- **Revenue**: Total money earned

### Shop GUI

Access the shop GUI to:
- Browse all shops
- Search for specific items
- Filter by category
- View shop statistics
- Manage favorites

## Editing Shops

Staff can edit shop prices and quantities:

1. Look at the shop sign
2. Use `/shopedit <buy|sell|quantity> <value>`
3. The shop is updated immediately

**Examples:**
- `/shopedit buy 50` - Set buy price to 50
- `/shopedit sell 25` - Set sell price to 25
- `/shopedit quantity 32` - Set quantity to 32

## Shop Expiration

Player shops that are inactive for the configured number of days are automatically removed. This helps keep the server clean and prevents abandoned shops.

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.adminshop` | Create admin shops | `op` |
| `ecore.adminshop.edit` | Edit admin shop prices | `op` |
| `ecore.pshop` | Create player shops | `true` |
| `ecore.pshop.edit` | Edit player shop prices | `op` |

## Tips

- Set reasonable prices to encourage trading
- Use categories to organize shops by item type
- Keep player shops stocked for better sales
- Use the shop GUI to find the best deals
- Monitor shop statistics to optimize your shop

## Related Systems

- [Economy System](economy-system.md) - For money transactions
- [Bank System](bank-system.md) - For managing shop earnings

