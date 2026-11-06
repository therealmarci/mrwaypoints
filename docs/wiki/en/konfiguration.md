# Configuration

Easiest in the browser: with **MRadmin** under **Plugins → MRwaypoints → Configuration** - every setting with an explanation, saving reloads MRwaypoints right away (permission `mradmin.config`). Everything is also commented in `plugins/MRwaypoints/config.yml`; you apply changes by hand with `/mrwaypoints reload`. The categories are kept separately in `categories.yml` (see [Categories](kategorien.md)).

| Section | What for |
|---|---|
| `general` | Languages (`language`, `fallback-language`, `server-language`) - every player automatically in theirs |
| `waypoints` | Longest name (`max-name-length`) |
| `guide` | Guide: how often the arrow updates (`update-ticks`), from how many blocks counts as "arrived" (`arrive-distance`), sound |
| `mod` | Mod for Xaero: minimum version (`minimum-version`), download link |
| `view` | The players' settings for Xaero: selectable view distances in chunks (`ranges`: chunks in every direction around the player, `0` = unlimited) and defaults (`default-range`, `default-display` `everywhere`/`world-map`, `default-far-on-world-map`) |
| `discord` | Discord log - see [Discord log](discord.md) |
| `updates` · `gui` | Update notices, menus |

## Files
| File | Content |
|---|---|
| `config.yml` | Settings (above) |
| `categories.yml` | The categories |
| `waypoints.yml` | All waypoints |
| `players.yml` | The players' settings (categories in Xaero, view distance, display) - only differences from the default |
| `lang/<language>.yml` | All messages, more under [Languages & messages](https://mrtechnology.marcelr.at/wiki/allgemein/sprachen/) |

The console is always English.

## All settings
Every setting of `config.yml` with its default - the same fields as in MRadmin's browser form. The list is generated automatically from the plugin and therefore always matches this version.

<!-- auto:config -->
