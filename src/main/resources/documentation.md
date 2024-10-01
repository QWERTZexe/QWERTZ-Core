# QWERTZcore Documentation

## Overview

QWERTZcore is an all-in-one event management plugin for Minecraft servers. It provides a comprehensive set of tools for hosts and administrators to manage events, player statuses, and game mechanics.

**Version:** 1.0
**Author:** QWERTZ_EXE
**Website:** https://qwertz.app

## Features

- Event countdown and timer management
- Player revival system with tokens
- Gamemode management
- Inventory management for alive/dead players
- Player visibility controls
- Configuration management
- Database for tracking wins and revive tokens

## Commands

### General Commands

- `/core` - Displays information about QWERTZ Core
- `/config <key> <value>` - Edit configuration values
- `/spawn` - Teleport to spawn location
- `/discord` - Displays the discord invite set in the config
- `/mutechat` - Mute the chat
- `/unmutechat` - Unmute the chat

### Event Management

- `/timer <seconds|cancel>` - Starts or cancels a countdown timer
- `/eventcountdown <time|cancel>` - Start or cancel a countdown for the event

### Gamemode Commands

- `/gmc` - Set gamemode to creative
- `/gms` - Set gamemode to survival
- `/gmsp` - Set gamemode to spectator
- `/gma` - Set gamemode to adventure
- `/gm <creative|survival|adventure|spectator>` - Set gamemode

### Revival System

- `/chatrevive <math|typer|guess> [max]` - Start a chat revival game
- `/revive <player>` - Revive a player
- `/unrevive <player>` - Mark a player as dead
- `/reviveall` - Revive all players
- `/revivelast [seconds]` - Revives all players who died in the last specified number of seconds
- `/userevive` - Request to use a revive token
- `/reviveaccept <player>` - Accept a player's revive request
- `/revivedeny <player>` - Deny a player's revive request
- `/addrevive <player>` - Add a revive token to a player
- `/removerevive <player>` - Remove a revive token from a player

### Player Management

- `/listalive` - List all alive players
- `/listdead` - List all dead players
- `/givedead <item> [amount] [data]` - Give an item to all dead players
- `/givealive <item> [amount] [data]` - Give an item to all alive players
- `/tpalive` - Teleport all alive players to you
- `/tpdead` - Teleport all dead players to you
- `/clearalive` - Clears the inventory of all alive players
- `/cleardead` - Clears the inventory of all dead players
- `/healalive` - Heals all alive players
- `/healdead` - Heals all dead players
- `/hide <host|staff|all|off>` - Hide other players based on their rank

### Database Management

- `/addwin <player>` - Add a win to a player's record

## Permissions

### Basic Permissions

- `qwertzcore.host.*` - All host permissions
- `qwertzcore.staff.*` - All staff permissions
- `qwertzcore.player.*` - All player permissions
- `qwertzcore.gamemode.*` - All gamemode permissions

### Specific Permissions

#### Host Permissions

- `qwertzcore.host.timer` - Manage timers
- `qwertzcore.host.revival` - Manage chat revivals
- `qwertzcore.host.revive` - Revive players
- `qwertzcore.host.unrevive` - Unrevive players
- `qwertzcore.host.reviveall` - Revive all players
- `qwertzcore.host.listalive` - List alive players
- `qwertzcore.host.listdead` - List dead players
- `qwertzcore.host.givedead` - Give items to dead players
- `qwertzcore.host.givealive` - Give items to alive players
- `qwertzcore.host.tpalive` - Teleport alive players
- `qwertzcore.host.tpdead` - Teleport dead players
- `qwertzcore.host.clearalive` - Clear alive players' inventories
- `qwertzcore.host.cleardead` - Clear dead players' inventories
- `qwertzcore.host.healalive` - Heals alive players
- `qwertzcore.host.healdead` - Heals dead players
- `qwertzcore.host.revivelast` - Revive recently dead players
- `qwertzcore.host.eventcountdown` - Manage event countdowns
- `qwertzcore.host.reviveaccept` - Accept revive requests
- `qwertzcore.host.revivedeny` - Deny revive requests
- `qwertzcore.host.addrevive` - Add revive tokens
- `qwertzcore.host.removerevive` - Remove revive tokens
- `qwertzcore.host.mutechat` - Mute the chat
- `qwertzcore.host.unmutechat` - Unmute the chat

#### Player Permissions

- `qwertzcore.player.spawn` - Use spawn command
- `qwertzcore.player.hide` - Use hide command
- `qwertzcore.player.discord` - Use discord command
- `qwertzcore.player.userevive` - Use revive tokens

#### Gamemode Permissions

- `qwertzcore.gamemode.creative` - Use creative mode
- `qwertzcore.gamemode.survival` - Use survival mode
- `qwertzcore.gamemode.spectator` - Use spectator mode
- `qwertzcore.gamemode.adventure` - Use adventure mode
- `qwertzcore.gamemode` - Use general gamemode command

#### Other Permissions

- `qwertzcore.config` - Edit plugin configuration
- `qwertzcore.chat.bypass` - Bypass a muted chat
- `qwertzcore.database.addwin` - Add wins to players' records