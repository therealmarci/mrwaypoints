# Konfiguration

Am einfachsten im Browser: Mit **MRadmin** unter **Plugins → MRwaypoints → Konfiguration** - jede Einstellung mit Erklärung, Speichern lädt MRwaypoints sofort neu (Permission `mradmin.config`). Alles steht außerdem kommentiert in `plugins/MRwaypoints/config.yml`; Änderungen von Hand übernimmst du mit `/mrwaypoints reload`. Die Kategorien stehen getrennt in `categories.yml` (siehe [Kategorien](kategorien.md)).

| Abschnitt | Wofür |
|---|---|
| `general` | Sprachen (`language`, `fallback-language`, `server-language`) - jeder Spieler automatisch in seiner |
| `waypoints` | längster Name (`max-name-length`) |
| `guide` | Wegweiser: wie oft der Pfeil aktualisiert (`update-ticks`), ab wie vielen Blöcken „angekommen“ (`arrive-distance`), Ton |
| `mod` | Mod für Xaero: Mindestversion (`minimum-version`), Download-Link |
| `view` | Einstellungen der Spieler für Xaero: wählbare Sichtweiten in Chunks (`ranges`: Chunks in jede Richtung um den Spieler, `0` = unbegrenzt) und Standards (`default-range`, `default-display` `everywhere`/`world-map`, `default-far-on-world-map`) |
| `discord` | Discord-Log - siehe [Discord-Log](discord.md) |
| `updates` · `gui` | Update-Hinweise, Menüs |

## Dateien
| Datei | Inhalt |
|---|---|
| `config.yml` | Einstellungen (oben) |
| `categories.yml` | die Kategorien |
| `waypoints.yml` | alle Wegpunkte |
| `players.yml` | Einstellungen der Spieler (Kategorien in Xaero, Sichtweite, Anzeige) - nur Abweichungen vom Standard |
| `lang/<sprache>.yml` | alle Texte, mehr unter [Sprachen & Texte](https://mrtechnology.marcelr.at/wiki/allgemein/sprachen/) |

Die Konsole ist immer Englisch.

## Alle Einstellungen
Jede Einstellung der `config.yml` mit Standardwert - dieselben Felder wie im Browser-Formular von MRadmin. Die Liste entsteht automatisch aus dem Plugin und ist damit immer auf dem Stand dieser Version.

<!-- auto:config -->
