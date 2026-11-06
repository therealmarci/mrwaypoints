# FAQ

### A player cannot create a waypoint.
In the category they need `mrwaypoints.create.<category>` - or the category has `players-create: true` and they have `mrwaypoints.player`. When the limit (`max-per-player`) is reached, the button turns gray. If the category only applies in certain worlds (`worlds`), nobody can create waypoints there in other worlds.

### Do players need a mod?
No. Menu and guide work with every client. The mod additionally brings the waypoints into Xaero's maps.

### With the mod, no waypoints appear in Xaero.
Is the category switched on for you (`/waypoints` → Categories)? Is Xaero's **Minimap** installed (the World Map alone is not enough)? The client log says "Connected to MRwaypoints …" or, with an unsupported Xaero version, shows an error.

### Can I copy server waypoints into my own Xaero waypoints?
No, on purpose: server waypoints only exist on the server, so there are no outdated copies. Xaero only displays them.

### I found a bug or have a wish.
Gladly via the **[form on the website](https://mrtechnology.marcelr.at/report/mrwaypoints/)** - ideally with the output of `/mrwaypoints version` and your mod version.
