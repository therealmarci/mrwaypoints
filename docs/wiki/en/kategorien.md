# Categories

Most convenient in the MRadmin web interface (page **Map**, tab Categories, `mrwaypoints.admin.categories`) - without MRadmin directly in `plugins/MRwaypoints/categories.yml`, then `/mrwaypoints reload`.

| Key | Meaning |
|---|---|
| *id* (the name of the entry) | short, lower case, letters/digits/`-`/`_` - do not change it later, waypoints and permissions depend on it |
| `name.de` · `name.en` | Name per language |
| `color` | Color on the maps and in the menus, `"#RRGGBB"` |
| `icon` | Item in the menu, e.g. `EMERALD` |
| `players-create` | `true` = every player (`mrwaypoints.player`) may create here, `false` = only with `mrwaypoints.create.<id>` |
| `max-per-player` | this many own waypoints per player, `0` = unlimited (whoever has `mrwaypoints.manage.<id>` is never limited) |
| `visible` | shown in Xaero's map by default - every player switches it for themselves (mod or map on the player page) |
| `map` | show as its own layer on the map of the player page (tab "Map" in MRadmin, only for signed-in players) |
| `worlds` | only in these worlds, e.g. `[world_nether]` for a Nether highway (names as in the server folder or `/mv list`). Leave out = all worlds. In other worlds nobody can create waypoints there and the category appears neither in the menu nor in Xaero nor on the map. |

Included are **Spawn & warps** (team only), **Shops** (players, 3 each), **Farms** (players, 5 each, not on the map) and **Sights** (players, 3 each).

> [!WARNING]
> If you delete a category from the file, its waypoints disappear from the menus but stay in `waypoints.yml`. With the same id they come back.

> [!TIP]
> Shops only for VIPs? Set `players-create: false` and `/lp group vip permission set mrwaypoints.create.shops true`.

## Manage waypoints (MRadmin)
On the page **Map** of the MRadmin web interface (tab Waypoints) you see all waypoints with filters for category, world, creator and search. With `mrwaypoints.manage.*` you rename them, move them to another category or delete them - also several at once.
