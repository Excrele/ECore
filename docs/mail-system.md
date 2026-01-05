# Mail System

Send and receive mail between players.

## Overview

The mail system allows players to send messages and items to other players, even when they're offline.

## Features

- **Send Mail**: Send messages to players
- **Receive Mail**: Receive mail when online or offline
- **Mail GUI**: Easy-to-use mail interface
- **Mail Storage**: Mail persists until read
- **Clear Mail**: Clear all mail

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/mail send <player> <message>` | Send mail to player | `ecore.mail` | `true` |
| `/mail read` | Read mail (opens GUI) | `ecore.mail` | `true` |
| `/mail clear` | Clear all mail | `ecore.mail` | `true` |
| `/mail sendall <message>` | Send mail to all players | `ecore.mail.sendall` | `op` |

## Usage Guide

### Sending Mail

1. Use `/mail send <player> <message>` to send mail
2. Mail is delivered immediately if player is online
3. Mail is stored if player is offline

### Reading Mail

1. Use `/mail read` to open the mail GUI
2. Browse all received mail
3. Read messages from other players

### Clearing Mail

- Use `/mail clear` to delete all your mail
- Useful for cleaning up old messages

### Sending to All Players (Admin)

- Use `/mail sendall <message>` to send mail to all players
- Useful for announcements

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.mail` | Use mail | `true` |
| `ecore.mail.sendall` | Send mail to all players | `op` |

## Tips

- Use mail for important messages
- Check mail regularly
- Clear old mail to stay organized
- Use sendall for server announcements

