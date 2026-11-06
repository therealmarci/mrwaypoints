# Kategorien

Am bequemsten in der MRadmin-Weboberfläche (Seite **Karte**, Reiter Kategorien, `mrwaypoints.admin.categories`) - ohne MRadmin direkt in `plugins/MRwaypoints/categories.yml`, danach `/mrwaypoints reload`.

| Schlüssel | Bedeutung |
|---|---|
| *id* (der Name des Eintrags) | kurz, klein, Buchstaben/Ziffern/`-`/`_` - später nicht mehr ändern, Wegpunkte und Permissions hängen daran |
| `name.de` · `name.en` | Name pro Sprache |
| `color` | Farbe auf den Karten und in den Menüs, `"#RRGGBB"` |
| `icon` | Item im Menü, z.B. `EMERALD` |
| `players-create` | `true` = jeder Spieler (`mrwaypoints.player`) darf hier anlegen, `false` = nur mit `mrwaypoints.create.<id>` |
| `max-per-player` | so viele eigene Wegpunkte pro Spieler, `0` = unbegrenzt (wer `mrwaypoints.manage.<id>` hat, ist nie begrenzt) |
| `visible` | in Xaero's Karte standardmäßig eingeblendet - jeder Spieler schaltet es für sich um (Mod oder Karte auf der Spielerseite) |
| `map` | als eigene Ebene auf der Karte der Spielerseite zeigen (Tab „Karte“ in MRadmin, nur für angemeldete Spieler) |
| `worlds` | nur in diesen Welten, z.B. `[world_nether]` für einen Nether-Highway (Namen wie im Server-Ordner bzw. `/mv list`). Weglassen = alle Welten. In anderen Welten kann man dort nichts anlegen und die Kategorie erscheint weder im Menü noch in Xaero noch auf der Karte. |

Mitgeliefert sind **Spawn & Warps** (nur Team), **Shops** (Spieler, je 3), **Farmen** (Spieler, je 5, nicht auf der Karte) und **Sehenswürdigkeiten** (Spieler, je 3).

> [!WARNING]
> Löschst du eine Kategorie aus der Datei, verschwinden ihre Wegpunkte aus den Menüs, bleiben aber in `waypoints.yml`. Mit derselben id kommen sie zurück.

> [!TIP]
> Shops nur für VIPs? `players-create: false` setzen und `/lp group vip permission set mrwaypoints.create.shops true`.

## Wegpunkte verwalten (MRadmin)
Auf der Seite **Karte** der MRadmin-Weboberfläche (Reiter Wegpunkte) siehst du alle Wegpunkte mit Filtern nach Kategorie, Welt, Ersteller und Suche. Mit `mrwaypoints.manage.*` benennst du sie um, verschiebst sie in eine andere Kategorie oder löschst sie - auch mehrere auf einmal.
