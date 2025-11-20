# Friends & Party System Implementation

## Overview

A comprehensive social system has been implemented for ECore, providing friend list management and party/team functionality with beautiful GUI interfaces.

## Features Implemented

### Friends System
- ✅ Friend list management
- ✅ Friend requests (send, accept, deny)
- ✅ Friend GUI with online status
- ✅ Pending requests management
- ✅ Persistent storage (friends.yml)

### Party System
- ✅ Party creation and management
- ✅ Party invitations
- ✅ Party chat (private channel)
- ✅ Party member management (kick, leave)
- ✅ Party GUI
- ✅ Online status for party members

## Files Created

1. **FriendManager.java** - Core friend system manager
   - Friend list storage and retrieval
   - Friend request handling
   - Friend relationship management

2. **PartyManager.java** - Core party system manager
   - Party creation and management
   - Invitation system
   - Party chat functionality

3. **FriendCommand.java** - Friend command handler
   - All friend-related commands
   - Tab completion support

4. **PartyCommand.java** - Party command handler
   - All party-related commands
   - Tab completion support

5. **FriendGUIManager.java** - Friend GUI system
   - Browse friends and requests
   - Accept/deny requests from GUI
   - Online status display

6. **PartyGUIManager.java** - Party GUI system
   - View party members
   - Invite/kick from GUI
   - Party management interface

## Commands

### Friend Commands
- `/friend` - Open friend GUI
- `/friend add <player>` - Send friend request
- `/friend remove <player>` - Remove friend
- `/friend list` - List all friends
- `/friend accept <player>` - Accept friend request
- `/friend deny <player>` - Deny friend request
- `/friend requests` - View pending requests
- `/friends` - Alias for friend

### Party Commands
- `/party` - Open party GUI
- `/party create` - Create a party
- `/party invite <player>` - Invite player to party
- `/party accept <leader>` - Accept party invite
- `/party leave` - Leave party
- `/party kick <player>` - Kick player (leader only)
- `/party list` - Show party information
- `/party chat <message>` - Send party message
- `/party c <message>` - Alias for party chat
- `/p` - Alias for party

## Permissions

- `ecore.friend` - Use friend commands (default: true)
- `ecore.party` - Use party commands (default: true)

## Data Storage

- **friends.yml** - Stores friend relationships
- **Parties** - Stored in memory (can be enhanced to persist)

## GUI Features

### Friend GUI
- Shows pending friend requests at the top
- Displays all friends with online/offline status
- Click to accept requests
- Right-click to deny requests
- "Add Friend" button for easy friend adding

### Party GUI
- Shows all party members with online status
- Leader indicators
- Invite button (leader only)
- Kick button (leader only)
- Leave party button

## Integration

- Integrated with ChatManager for chat input handling
- GUI click events properly handled
- Online status tracking
- Player join/leave notifications

---

**Implementation Date**: Current
**Version**: 1.0
**Status**: ✅ Complete and Ready for Use

