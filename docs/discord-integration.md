# Discord Integration

Comprehensive Discord integration for chat bridging, staff logs, and server management.

## Overview

ECore includes advanced Discord integration for two-way chat bridging, staff action logging, Discord slash commands, and server status updates.

## Features

- **Chat Bridging**: Two-way chat between Minecraft and Discord
- **Message Filtering**: Word filtering and rate limiting
- **@player Mentions**: Mention support in Discord
- **Rich Embeds**: Beautiful formatted messages
- **Staff Logs**: Comprehensive logging system
- **Slash Commands**: Full server management from Discord
- **Account Linking**: Link Discord accounts to Minecraft players
- **Server Status**: Live status channel updates
- **Scheduled Reports**: Automatic server statistics reports

## Setup Instructions

### Step 1: Create a Discord Bot

1. Go to [Discord Developer Portal](https://discord.com/developers/applications) and create a new application
2. Navigate to the "Bot" tab and click "Add Bot"
3. Copy the bot token (you'll need this for `discordconf.yml`)
4. Enable the following **Privileged Gateway Intents**:
   - ✅ Server Members Intent
   - ✅ Message Content Intent
5. Under "OAuth2" → "URL Generator":
   - Select scope: `bot`
   - Select permissions:
     - Send Messages
     - Read Message History
     - Embed Links
     - Attach Files
     - Use Slash Commands
     - Manage Messages (for filtering)
   - Copy the generated URL and invite the bot to your server

### Step 2: Get Channel IDs

1. Enable Developer Mode in Discord (Settings → Advanced → Developer Mode)
2. Right-click on the channel you want to use for chat bridging
3. Click "Copy ID" and paste it into `discordconf.yml` as `channel-id`
4. Repeat for punishment logs channel (`punishment-channel-id`)
5. Optionally set up a separate staff logs channel (`staff-logs-channel-id`)

### Step 3: Configure discordconf.yml

1. Set `discord.enabled: true`
2. Paste your bot token into `discord.bot-token`
3. Paste your channel IDs into the appropriate fields
4. Configure additional features as needed

### Step 4: Configure Role Permissions

To use Discord slash commands with role-based permissions:

1. Get your role IDs (right-click role → Copy ID with Developer Mode enabled)
2. Add role IDs or names to `discord.permissions.staff` for staff commands
3. Add role IDs or names to `discord.permissions.admin` for admin commands

Example:
```yaml
permissions:
  staff:
    - "123456789012345678"  # Role ID
    - "Moderator"  # Or role name
  admin:
    - "987654321098765432"
    - "Admin"
```

### Step 5: Account Linking (Optional)

Players can link their Discord accounts to Minecraft:

1. In-game: `/link` - Generates a verification code
2. In Discord: `/link <code>` - Links the accounts
3. Use `/unlink` in Discord to unlink

Linked accounts will show Discord names in embeds and enable enhanced features.

### Step 6: Restart Server

After configuration, restart your server. The Discord bot will connect automatically if configured correctly.

## Discord Features

### Chat Bridging

- Messages from Minecraft appear in Discord
- Messages from Discord appear in Minecraft
- Player names and formatting preserved
- @mentions work in Discord

### Staff Logs

- All staff actions are logged to Discord
- Includes bans, kicks, mutes, and more
- Rich embeds with detailed information
- Separate channel for staff logs

### Slash Commands

Discord slash commands for server management:
- `/serverinfo` - View server status, TPS, memory, uptime
- `/online` - List all online players
- `/playerinfo <player>` - Get detailed player information
- `/report <player> <reason>` - Report players from Discord
- `/link <code>` - Link Discord account to Minecraft
- `/unlink` - Unlink Discord account
- `/staff <action> <player> [reason]` - Execute staff actions (ban, kick, mute, etc.)
- `/execute <command>` - Execute console commands (admin only)

### Server Status

- Live status updates in Discord channel
- Shows online players, TPS, memory usage
- Automatic updates on configurable interval

### Scheduled Reports

- Automatic server statistics reports
- Configurable schedule (daily, weekly, etc.)
- Includes player counts, economy stats, and more

## Configuration

Discord integration is configured in `discordconf.yml`:

```yaml
discord:
  enabled: true
  bot-token: "YOUR_BOT_TOKEN"
  channel-id: "CHANNEL_ID"
  punishment-channel-id: "CHANNEL_ID"
  staff-logs-channel-id: "CHANNEL_ID"
  rich-embeds: true
  chat-filtering: true
  scheduled-reports:
    enabled: true
    interval: 86400  # 24 hours
```

## Permissions

Discord permissions are role-based:
- Staff roles can use staff commands
- Admin roles can use admin commands
- Configure in `discordconf.yml`

## Tips

- Keep bot token secure (never share it)
- Use separate channels for different log types
- Enable rich embeds for better formatting
- Use chat filtering to prevent spam
- Link accounts for enhanced features
- Monitor Discord for server activity

## Related Systems

- [Staff Management](staff-management.md) - For staff action logging
- [Economy System](economy-system.md) - For economy statistics in reports

