# Friends & Party System

Social features for player interaction including friend lists and party chat.

## Overview

The friends and party system provides social features for players to connect, form parties, and communicate privately.

## Features

- **Friend Lists**: Add, remove, and manage friends
- **Friend Requests**: Send, accept, and deny friend requests
- **Friend GUI**: Browse friends and pending requests
- **Party System**: Create and manage parties/teams
- **Party Chat**: Private chat channel for party members
- **Party Management**: Invite, kick, and leave parties
- **Party GUI**: Easy party management interface
- **Online Status**: See which friends/party members are online

## Commands

### Friend Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/friend` | Open friend GUI | `ecore.friend` | `true` |
| `/friend add <player>` | Send friend request | `ecore.friend` | `true` |
| `/friend remove <player>` | Remove friend | `ecore.friend` | `true` |
| `/friend list` | List friends | `ecore.friend` | `true` |
| `/friend accept <player>` | Accept friend request | `ecore.friend` | `true` |
| `/friend deny <player>` | Deny friend request | `ecore.friend` | `true` |
| `/friend requests` | View pending requests | `ecore.friend` | `true` |

### Party Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/party` | Open party GUI | `ecore.party` | `true` |
| `/party create` | Create a party | `ecore.party` | `true` |
| `/party invite <player>` | Invite player to party | `ecore.party` | `true` |
| `/party accept <leader>` | Accept party invite | `ecore.party` | `true` |
| `/party leave` | Leave party | `ecore.party` | `true` |
| `/party kick <player>` | Kick player (leader only) | `ecore.party` | `true` |
| `/party list` | Show party info | `ecore.party` | `true` |
| `/party chat <message>` | Send party message | `ecore.party` | `true` |
| `/p` | Alias for party | `ecore.party` | `true` |

## Usage Guide

### Friends

**Adding Friends:**
1. Use `/friend add <player>` to send a friend request
2. The player receives a notification
3. They can accept with `/friend accept <player>` or deny with `/friend deny <player>`

**Managing Friends:**
- Use `/friend list` to see all your friends
- Use `/friend remove <player>` to remove a friend
- Use `/friend requests` to see pending requests
- Use `/friend` to open the friend GUI

**Friend GUI:**
- Browse all friends
- See online/offline status
- View pending requests
- Accept/deny requests easily

### Parties

**Creating a Party:**
1. Use `/party create` to create a party
2. You become the party leader
3. Invite players with `/party invite <player>`

**Party Management:**
- **Invite**: Use `/party invite <player>` to invite players
- **Accept**: Players use `/party accept <leader>` to join
- **Kick**: Leaders use `/party kick <player>` to remove members
- **Leave**: Use `/party leave` to leave the party
- **List**: Use `/party list` to see party members

**Party Chat:**
- Use `/party chat <message>` or `/p <message>` to send party messages
- Only party members can see party chat
- Private communication for your team

**Party GUI:**
- Browse party members
- See online/offline status
- Manage party easily

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.friend` | Use friend commands | `true` |
| `ecore.party` | Use party commands | `true` |

## Tips

- Add friends to keep in touch
- Create parties for group activities
- Use party chat for team coordination
- Use the GUIs for easier management
- Check online status before inviting

## Related Systems

- [Chat Channels System](chat-channels-system.md) - For other chat features

