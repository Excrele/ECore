# Shop System

ECore includes three types of shops: GUI Shops, Admin Shops, and Player Shops for buying and selling items.

## Overview

The shop system provides three types of shops:
- **GUI Shops**: Server-controlled GUI-based shops with dynamic pricing and inflation adjustment
- **Admin Shops**: Server-controlled shops with unlimited stock
- **Player Shops**: Player-owned shops using chest storage

All shop types support buying and selling items with configurable prices.

## Features

- **GUI Shops**: Fully featured GUI shop system with dynamic pricing
- **Admin Shops**: Unlimited stock, server-controlled
- **Player Shops**: Player-owned with chest storage
- **Dynamic Pricing**: Automatic price adjustment based on supply and demand
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
  gui-shop-enabled: true         # Enable GUI Shop system
  max-shops-per-player: 10       # Maximum player shops per player
  expiration-days: 30            # Days before inactive shops expire
  enable-categories: true        # Enable shop categories
  enable-favorites: true         # Enable shop favorites
  enable-statistics: true        # Enable shop statistics
  
  # Dynamic Pricing System (Inflation Adjustment)
  dynamic-pricing:
    enabled: true                # Enable/disable dynamic pricing system
    adjustment-rate: 0.05         # Price adjustment rate per transaction (5%)
    min-price-multiplier: 0.1    # Minimum price as percentage of base price (10%)
    max-price-multiplier: 10.0   # Maximum price as percentage of base price (1000%)
    buy-inflation-rate: 0.02     # Price increase rate when items are bought (2% per buy)
    sell-deflation-rate: 0.02    # Price decrease rate when items are sold (2% per sell)
    transaction-threshold: 10    # Number of transactions before significant price changes
```

Shop data is stored in:
- `gui-shops.yml` - GUI shop data with dynamic pricing information (auto-generated)
- `adminshops.yml` - Admin shop data (auto-generated)
- `playershops.yml` - Player shop data (auto-generated)

## GUI Shop System

The GUI Shop system provides a modern, user-friendly interface for buying and selling items with automatic price adjustment based on supply and demand.

### Features

- **Interactive GUI**: Browse items by category in an easy-to-use interface
- **Dynamic Pricing**: Prices automatically adjust based on buy/sell transactions
- **Price Indicators**: See price changes relative to base prices
- **Category Organization**: Items organized by categories for easy browsing
- **Real-time Updates**: Prices update in real-time as transactions occur

### Accessing GUI Shops

GUI Shops can be accessed through:
- NPCs configured to open the shop GUI
- Server commands (if implemented)
- Integration with other systems

### Using GUI Shops

1. Open the shop GUI
2. Browse items by category or view all items
3. **Left-click** an item to **buy** it
4. **Right-click** an item to **sell** it from your inventory

### Dynamic Pricing System

The GUI Shop system features an advanced dynamic pricing system that automatically adjusts prices based on market activity:

**How It Works:**
- **Buy Transactions**: When items are bought, demand increases, causing prices to rise (inflation)
- **Sell Transactions**: When items are sold, supply increases, causing prices to fall (deflation)
- **Net Demand**: The system calculates net demand (buys - sells) to determine price adjustments
- **Price Bounds**: Prices are constrained between minimum and maximum multipliers to prevent extreme values

**Price Calculation:**
- Prices use square root scaling to provide diminishing returns, preventing extreme price swings
- Buy prices respond more strongly to demand changes
- Sell prices follow buy prices but with reduced volatility
- Significant transaction imbalances trigger additional price adjustments

**Configuration Options:**
- `enabled`: Toggle dynamic pricing on/off
- `adjustment-rate`: Base adjustment rate for significant transaction imbalances
- `min-price-multiplier`: Minimum price as a percentage of base price (default: 10%)
- `max-price-multiplier`: Maximum price as a percentage of base price (default: 1000%)
- `buy-inflation-rate`: Price increase rate per buy transaction (default: 2%)
- `sell-deflation-rate`: Price decrease rate per sell transaction (default: 2%)
- `transaction-threshold`: Number of transactions before significant price changes apply

**Price Display:**
- Items show current buy and sell prices
- Price change indicators show percentage change from base price
- Green indicators show price decreases (deflation)
- Red indicators show price increases (inflation)

**Price Management:**
- Base prices are stored separately from current prices
- Transaction counts are tracked per item
- Prices can be reset to base prices by administrators
- The system automatically initializes base prices for existing items

### Tips for Dynamic Pricing

- **High Demand Items**: Items that are frequently bought will see price increases
- **High Supply Items**: Items that are frequently sold will see price decreases
- **Market Balance**: Prices naturally adjust to find equilibrium between supply and demand
- **Price Stability**: The system uses mathematical scaling to prevent extreme price swings
- **Monitoring**: Watch price indicators to understand market trends

## Creating Shops

### GUI Shops

GUI Shops are configured by server administrators in `gui-shops.yml`. Items are automatically organized by category and support dynamic pricing.

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

- Set reasonable base prices to encourage trading
- Use categories to organize shops by item type
- Keep player shops stocked for better sales
- Use the shop GUI to find the best deals
- Monitor shop statistics to optimize your shop
- Watch price indicators in GUI shops to understand market trends
- Take advantage of price fluctuations in dynamic pricing system
- Consider timing your purchases when prices are lower

## Related Systems

- [Economy System](economy-system.md) - For money transactions
- [Bank System](bank-system.md) - For managing shop earnings

