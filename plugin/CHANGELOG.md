# Changelog - Plugin

Änderungen am Paper-Plugin MRwaypoints. Versionen: `plugin-mc<Minecraft>-v<Version>`, siehe
[docs/RELEASES.md](../docs/RELEASES.md).

## [Unveröffentlicht]

### Neu
- **Erste Version:** Server-Wegpunkte in Kategorien. Kategorien in `categories.yml` (Name pro Sprache, Farbe, Symbol, `players-create`, `max-per-player`, `visible`, `map`), vier Beispiel-Kategorien beim ersten Start.
- **Menü `/waypoints`** (`mrwaypoints.use`, in `mrwaypoints.player`): Kategorien, Wegpunkte nach Entfernung, anlegen an der eigenen Position mit Namensdialog, eigene umbenennen, verschieben, löschen. Teleport per Shift-Klick nur mit `mrwaypoints.teleport` (`mrwaypoints.groups.admin`).
- **Wegweiser** in der Actionbar: Pfeil, Entfernung, Höhenunterschied; Ende bei Ankunft (`guide.arrive-distance`) oder mit `/waypoints stop`.
- **Permissions pro Kategorie:** `mrwaypoints.create.<kategorie>` und `mrwaypoints.manage.<kategorie>` (bzw. `.*`), Gruppen `mrwaypoints.player` und `mrwaypoints.groups.support|staff|admin`.
- `/mrwaypoints reload|version`, Update-Prüfung, Deutsch und Englisch pro Spieler.
- **Mit MRadmin:** Kategorien mit `map: true` als eigene Ebenen auf der Webkarte (`mrwaypoints:<kategorie>`, Pin in der Kategoriefarbe, Klick zeigt Name, Ort und Ersteller; Text `map-tooltip`), Knopf im Dashboard (`mrwaypoints.admin.view`, öffnet `/waypoints`) und Bereich **Wegpunkte** auf der Spielerseite (`/web`): eigene Wegpunkte umbenennen und löschen mit Kartenausschnitt, alle Wegpunkte nach Kategorie.
