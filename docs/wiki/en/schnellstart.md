# Quick start

1. **[Download MRwaypoints](https://mrtechnology.marcelr.at/download/mrwaypoints/)** (row "Plugin (server)") and put the JAR into the `plugins/` folder.
2. Start the server. This creates `plugins/MRwaypoints/config.yml`, `categories.yml` with four example categories and the language files in `plugins/MRwaypoints/lang/`.
3. **Give permissions** - without them only OPs may:
   ```text
   /lp group default permission set mrwaypoints.player true
   /lp group supporter permission set mrwaypoints.groups.support true
   /lp group staff permission set mrwaypoints.groups.staff true
   /lp group admin permission set mrwaypoints.groups.admin true
   ```
4. Adjust the categories - in the MRadmin web interface or in `plugins/MRwaypoints/categories.yml` and `/mrwaypoints reload` (see [Categories](kategorien.md)).
5. Optionally recommend the mod to your players: [In Xaero's Minimap](xaero.md).

## Update
Replace the old JAR with the new one and restart the server. The config is adapted automatically: your values stay, new options are added, and the old file is backed up as `config.v<N>.backup.yml`. Plugin and mod have their own versions.
