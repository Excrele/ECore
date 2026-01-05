# Chat Channels System

Multiple chat channels for different types of communication.

## Overview

The chat channels system provides multiple channels for different types of communication, including global, local, trade, help, and staff channels.

## Features

- **Multiple Channels**: Global, Local, Trade, Help, Staff channels
- **Channel Switching**: Easy channel management
- **Channel Permissions**: Per-channel permission support
- **Channel Prefixes**: Color-coded channel prefixes
- **Range-Based Local Chat**: Configurable range for local channels
- **Channel Muting**: Mute specific channels per player
- **Auto-Join**: Auto-join to default channel on login

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/channel` | Show channel help | `ecore.channel` | `true` |
| `/channel join <channel>` | Join a channel | `ecore.channel` | `true` |
| `/channel leave <channel>` | Leave a channel | `ecore.channel` | `true` |
| `/channel list` | List available channels | `ecore.channel` | `true` |
| `/channel current` | View current channel info | `ecore.channel` | `true` |
| `/channel mute <channel>` | Mute a channel | `ecore.channel` | `true` |
| `/channel unmute <channel>` | Unmute a channel | `ecore.channel` | `true` |
| `/channel create <id> [name] [prefix]` | Create channel (admin) | `ecore.channel.admin` | `op` |
| `/channel delete <id>` | Delete channel (admin) | `ecore.channel.admin` | `op` |
| `/ch <message>` | Chat in current channel | `ecore.channel` | `true` |

## Default Channels

- **Global**: Server-wide chat (default)
- **Local**: Range-based local chat
- **Trade**: Trading channel
- **Help**: Help and support channel
- **Staff**: Staff-only channel

## Usage Guide

### Joining Channels

1. Use `/channel list` to see available channels
2. Use `/channel join <channel>` to join a channel
3. You are now in that channel and messages go there

### Chatting in Channels

- Type normally to chat in your current channel
- Use `/ch <message>` to explicitly chat in current channel
- Channel prefix shows which channel you're in

### Switching Channels

- Use `/channel join <channel>` to switch channels
- Your current channel is shown in chat prefix

### Local Chat

- Local chat has a configurable range (default: 100 blocks)
- Only players within range can see local chat
- Useful for nearby communication

### Muting Channels

- Use `/channel mute <channel>` to mute a channel
- You won't see messages from that channel
- Use `/channel unmute <channel>` to unmute

### Creating Custom Channels

Staff can create custom channels:
1. Use `/channel create <id> <name> <prefix>`
2. Example: `/channel create events Events [Events]`
3. Players can now join this channel

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.channel` | Use chat channel commands | `true` |
| `ecore.channel.admin` | Manage channels (admin only) | `op` |
| `ecore.chat.global` | Use global channel | `true` |
| `ecore.chat.local` | Use local channel | `true` |
| `ecore.chat.trade` | Use trade channel | `true` |
| `ecore.chat.help` | Use help channel | `true` |
| `ecore.chat.staff` | Use staff channel | `op` |

## Tips

- Use local chat for nearby communication
- Use trade channel for trading
- Use help channel for questions
- Mute channels you don't want to see
- Create custom channels for events or groups

## Related Systems

- [Staff Management](staff-management.md) - For staff chat
- [Friends & Party System](friends-party-system.md) - For private communication

