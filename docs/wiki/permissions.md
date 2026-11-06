# Permissions

Am einfachsten vergibst du die **Gruppen** - sie sind in allen MRtechnology-Plugins gleich aufgebaut, jede enthält die darunter ([mehr dazu](https://mrtechnology.marcelr.at/wiki/allgemein/rechte/)). Standardmäßig haben sie nur OPs.

## Gruppen
| Gruppe | Enthält |
|---|---|
| `mrwaypoints.player` | `/waypoints` mit Wegweiser und eigenen Einstellungen für Xaero, anlegen in Kategorien mit `players-create: true`, `/mrwaypoints version` |
| `mrwaypoints.groups.support` | Spieler + MRwaypoints im MRadmin-Dashboard und die Wegpunkte im Web ansehen |
| `mrwaypoints.groups.staff` | Support + in **jeder** Kategorie anlegen, **alle** Wegpunkte bearbeiten und löschen, ohne Limit |
| `mrwaypoints.groups.admin` | Alles: Staff + zu Wegpunkten teleportieren, Kategorien im Web verwalten, Reload, Discord-Log, Update-Hinweis |

```text
/lp group default permission set mrwaypoints.player true
/lp group supporter permission set mrwaypoints.groups.support true
/lp group staff permission set mrwaypoints.groups.staff true
/lp group admin permission set mrwaypoints.groups.admin true
```

## Einzeln
Standard ist `op`, die Rechte pro Kategorie haben standardmäßig niemand (sie kommen über die Gruppen bzw. `players-create`).

| Permission | Wofür |
|---|---|
| `mrwaypoints.use` | `/waypoints` |
| `mrwaypoints.settings` | `/waypoints` → Einstellungen: Sichtweite und Anzeige in Xaero (mit Mod) |
| `mrwaypoints.version` | `/mrwaypoints version` |
| `mrwaypoints.create.<kategorie>` · `mrwaypoints.create.*` | Wegpunkte in einer bzw. allen Kategorien anlegen |
| `mrwaypoints.manage.<kategorie>` · `mrwaypoints.manage.*` | alle Wegpunkte einer bzw. aller Kategorien bearbeiten und löschen, ohne Limit |
| `mrwaypoints.teleport` | Shift-Klick im Menü: teleportieren |
| `mrwaypoints.admin.view` | MRwaypoints im MRadmin-Dashboard, Wegpunkte in der Weboberfläche ansehen |
| `mrwaypoints.admin.categories` | Kategorien im Web anlegen, ändern, sortieren, löschen |
| `mrwaypoints.admin.reload` | `/mrwaypoints reload` |
| `mrwaypoints.admin.discord` | `/mrwaypoints discord [test]` |
| `mrwaypoints.admin.update` | Update-Hinweis beim Betreten des Servers |

Eigene Wegpunkte darf jeder Ersteller immer umbenennen, verschieben und löschen.
