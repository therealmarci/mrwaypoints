# Changelog - Plugin

Änderungen am Paper-Plugin MRwaypoints. Versionen: `plugin-mc<Minecraft>-v<Version>`, siehe
[docs/RELEASES.md](../docs/RELEASES.md).

## [Unveröffentlicht]

### Neu
- **Erste Version:** Server-Wegpunkte in Kategorien. Kategorien in `categories.yml` (Name pro Sprache, Farbe, Symbol, `players-create`, `max-per-player`, `visible`, `map`), vier Beispiel-Kategorien beim ersten Start.
- **Menü `/waypoints`** (`mrwaypoints.use`, in `mrwaypoints.player`): Startmenü mit „Meine Wegpunkte“, „Wegpunkt erstellen“ und „Alle Wegpunkte“ - immer für die Welt bzw. Dimension, in der man gerade ist. Die Listen filtern nach Kategorie, sortieren nach Entfernung, Name oder neueste und suchen nach Namen (Rechtsklick setzt einen Filter zurück). Erstellen ist ein Dialog mit Name und Kategorie (nur Kategorien, in denen man gerade darf), eigene Wegpunkte lassen sich umbenennen, verschieben und löschen. Teleport per Shift-Klick nur mit `mrwaypoints.teleport` (`mrwaypoints.groups.admin`).
- **Wegweiser** in der Actionbar: Pfeil, Entfernung, Höhenunterschied; Ende bei Ankunft (`guide.arrive-distance`) oder mit `/waypoints stop`.
- **Permissions pro Kategorie:** `mrwaypoints.create.<kategorie>` und `mrwaypoints.manage.<kategorie>` (bzw. `.*`), Gruppen `mrwaypoints.player` und `mrwaypoints.groups.support|staff|admin`.
- `/mrwaypoints reload|version`, Update-Prüfung, Deutsch und Englisch pro Spieler.
- **Mit MRadmin:** Kategorien mit `map: true` als eigene Ebenen auf der Webkarte (`mrwaypoints:<kategorie>`, Pin in der Kategoriefarbe, Klick zeigt Name, Ort und Ersteller; Text `map-tooltip`), Knopf im Dashboard (`mrwaypoints.admin.view`, öffnet `/waypoints`) und Ebenen auf der **Karte der Spielerseite** (MRadmin-API 8, Tab „Karte“): Wegpunkte per Rechtsklick anlegen (Höhe = Boden an der Stelle; offline nur in Kategorien mit `players-create`), eigene umbenennen und löschen, Wegweiser im Spiel starten, „in Xaero“ pro Kategorie.
- **Seite „Wegpunkte“ in der MRadmin-Weboberfläche:** Kategorien anlegen, ändern, sortieren und löschen (`mrwaypoints.admin.categories`), alle Wegpunkte durchsuchen, umbenennen, in eine andere Kategorie verschieben und löschen (`mrwaypoints.manage.*`). `categories.yml` wird dabei neu geschrieben (Kommentarblock bleibt).
- **Mod für Xaero's Karte** (Protokoll 1, siehe `docs/COMPATIBILITY.md`): Spieler mit dem MRwaypoints-Mod sehen die Wegpunkte in Xaero's Minimap und World Map und schalten Kategorien ein und aus. Die Auswahl speichert der Server pro Spieler (`players.yml`); Standard ist `visible` der Kategorie. In `/waypoints` erscheint dann der Knopf „In Xaero anzeigen“, auf der Karte der Spielerseite der Schalter „in Xaero“ pro Kategorie. Neue Config `mod.minimum-version` und `mod.download-url` (config-version 2).
