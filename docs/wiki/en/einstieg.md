# Overview

**Shared waypoints for your server:** the team sets up categories (spawn, shops, farms …), players and team add waypoints to them - and everyone finds them in game in a menu with a guide, in Xaero's Minimap and World Map and on the map of the player page (with MRadmin).

> [!NOTE]
> MRwaypoints is a **pre-release**. Feedback is welcome via [Report a bug](https://mrtechnology.marcelr.at/report/mrwaypoints/).

MRwaypoints consists of **two parts** - both are on the [download page](https://mrtechnology.marcelr.at/download/mrwaypoints/):

| Part | Who needs it | Where |
|---|---|---|
| **Plugin** (`MRwaypoints-mc26.3-v….jar`) | the server | Paper 26.3, Java 25 → folder `plugins/` |
| **Client mod** (`MRwaypoints-mod-mc26.3-v….jar`) | players who want to see the waypoints in Xaero's maps | Fabric 26.3 with [Fabric API](https://modrinth.com/mod/fabric-api) and [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap) → folder `mods/` |

Players **without** the mod use the menu and the guide normally. Plugin and mod have their own versions.

| At a glance | |
|---|---|
| Server | Paper 26.3 (or a fork like Purpur), Java 25 |
| Client | nothing needed - for Xaero's maps the MRwaypoints mod (Fabric Loader + Fabric API + Xaero's Minimap) |
| Requires | nothing - LuckPerms recommended, MRadmin optional |
| Languages | German, English - every player automatically in the language of their Minecraft client |
| Status | pre-release (0.x) |

## What can MRwaypoints do?
- **Categories from the team:** name per language, color, icon, worlds, who may add waypoints and how many per player. → [Categories](kategorien.md)
- **Waypoints stored on the server:** everyone sees the same ones - unlike private waypoints in Xaero, which only live on your PC.
- **Menu `/waypoints` for everyone:** my waypoints, all waypoints (with filters for category, sorting and search), create new waypoints at your own position - always for the dimension you are in. → [Waypoint menu & guide](menue.md)
- **Guide:** a click on a waypoint shows an arrow, the distance and the height difference in the action bar - without the mod.
- **In Xaero's Minimap and World Map** with the mod, categories can be shown and hidden one by one. → [In Xaero's Minimap](xaero.md)
- **Teleport** only for the team (admins).

## Where to start?
| You are … | Read first |
|---|---|
| **a player** | [Waypoint menu & guide](menue.md), then [In Xaero's Minimap](xaero.md) |
| **a server owner** | [Quick start](schnellstart.md), then [Categories](kategorien.md) and [Permissions](permissions.md) |
