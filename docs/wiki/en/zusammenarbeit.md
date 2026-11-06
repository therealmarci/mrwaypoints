# Working with other plugins

MRwaypoints runs **on its own**. Together with other MRtechnology plugins it can do more ([how that works](https://mrtechnology.marcelr.at/wiki/allgemein/zusammenarbeit/)):

| Feature | Needs |
|---|---|
| Page **Map** for the team: create, change, reorder and delete categories (`mrwaypoints.admin.categories`), rename all waypoints, move them to another category, delete them - also several at once (`mrwaypoints.manage.*`) | MRadmin |
| Tab **Map** on the player page (`/web`): one layer per category with `map: true`; right click on an empty spot: "Create waypoint" with name and category (height = ground at that spot), on your own ones "Edit waypoint" (name, category) and "Delete waypoint", on any one start the guide in game; "In game: on/off" per category | MRadmin |
| Configure MRwaypoints in the browser (Plugins → MRwaypoints → Configuration) | MRadmin |
| Button in the MRadmin dashboard (opens `/waypoints`) | MRadmin |
| Events in the **log** of the MRadmin web interface, Discord log via MRadmin's webhook (`discord.use-mradmin-webhook`) | MRadmin |
| Waypoints in Xaero's Minimap and World Map, show and hide categories | MRwaypoints mod + Xaero's Minimap |
