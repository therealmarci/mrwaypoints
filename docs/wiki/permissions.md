# Permissions

Am einfachsten vergibst du die **Gruppen** - sie sind in allen MRtechnology-Plugins gleich aufgebaut, jede enthält die darunter ([mehr dazu](https://mrtechnology.marcelr.at/wiki/allgemein/rechte/)). Standardmäßig haben sie nur OPs.

## Gruppen
Jede Gruppe enthält die darunter, die Team-Gruppen auch `mrwaypoints.player` - am besten `mrwaypoints.player` der Default-Gruppe geben:

<!-- auto:permission-groups -->

```text
/lp group default permission set mrwaypoints.player true
/lp group supporter permission set mrwaypoints.groups.support true
/lp group staff permission set mrwaypoints.groups.staff true
/lp group admin permission set mrwaypoints.groups.admin true
```

## Einzeln
„Enthalten in“ nennt die kleinste Gruppe, die die Permission mitbringt; „–“ heißt: in keiner Gruppe, nur einzeln vergeben.

<!-- auto:permissions -->

Dazu die Rechte pro Kategorie (die Kategorien legst du selbst an, darum stehen sie nicht in der Liste oben):

| Permission | Wofür |
|---|---|
| `mrwaypoints.create.<kategorie>` | Wegpunkte in dieser Kategorie anlegen (Spieler dürfen es in Kategorien mit `players-create: true` ohnehin) |
| `mrwaypoints.manage.<kategorie>` | alle Wegpunkte dieser Kategorie bearbeiten und löschen, ohne Limit |

Eigene Wegpunkte darf jeder Ersteller immer umbenennen, verschieben und löschen.
