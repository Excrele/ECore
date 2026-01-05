# Report System

Player reporting with staff management.

## Overview

The report system allows players to report other players for rule violations, with staff management tools.

## Features

- **Player Reports**: Submit reports on players
- **Report Management**: Staff can view and manage reports
- **Report GUI**: Easy report viewing and management
- **Report Tracking**: Track report status

## Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/report <player> <reason>` | Report a player | `ecore.report` | `true` |

## Usage Guide

### Submitting Reports

1. Use `/report <player> <reason>` to submit a report
2. Report is sent to staff
3. Staff can view and manage reports

### Managing Reports (Staff)

- Use `/ecore staff` to open staff GUI
- Navigate to reports section
- View and resolve reports

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `ecore.report` | Submit reports | `true` |

## Tips

- Report rule violations promptly
- Provide clear reasons
- Staff should check reports regularly
- Resolve reports to keep system clean

## Related Systems

- [Staff Management](staff-management.md) - For managing reports

