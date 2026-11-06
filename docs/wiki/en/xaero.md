# In Xaero's Minimap (with mod)

## Setup
1. Put **Fabric API**, **Xaero's Minimap** (and ideally Xaero's World Map) and the **[MRwaypoints mod](https://mrtechnology.marcelr.at/download/mrwaypoints/)** (row "Fabric mod (client)") into the `mods/` folder.
2. Join the server - the server waypoints appear by themselves on the minimap, in the world and on the World Map, in the color of their category. They are kept apart from your own Xaero waypoints.

## Show and hide categories
The key **`J`** (changeable under Options → Controls → MRwaypoints) and the button **"Server waypoints …"** in Xaero's waypoint menu open `/waypoints`. There, under **Categories**, you switch which categories appear in Xaero (a click per category, "All on" / "All off"). The server saves your choice - it applies on every PC and also on the player page in the web.

## Keys
Options → Controls → MRwaypoints, quicker via `/waypoints` → Settings → **Key bindings**. Only `J` is bound by default, you set the others yourself so nothing clashes with Xaero or other mods.

| Key | What happens |
|---|---|
| Waypoint menu (`J`) | `/waypoints` |
| Create waypoint here | the dialog with name and category - like in the menu |
| Server waypoints on/off | hide and show all server waypoints in Xaero (only on this PC, your choice per category stays) |

## View distance and display
In `/waypoints` → **Settings** you set how you see the server waypoints. The server saves it for you.

| Setting | What it does |
|---|---|
| **View distance** | how far around you the waypoints appear in the world and on the minimap: 10, 20, 50 or 100 chunks in every direction or unlimited (left click further, right click closer; the server sets the steps). Xaero's own limit for in-world waypoints applies as well. |
| **Display** | "World, minimap and world map" or "world map only" |
| **Distant ones on the world map** | show waypoints outside the view distance on the World Map anyway (only with a limited view distance) |

> [!NOTE]
> Xaero always shows a waypoint in the world and on the minimap together - "minimap only" or "world only" does not exist per waypoint. Xaero has its own settings for that, which then apply to all waypoints.

> [!TIP]
> No waypoints in Xaero? Check whether the category is switched on for you (`/waypoints` → Categories) and whether Xaero's **Minimap** is installed - the World Map alone is not enough.
