# Changelog - Client-Mod

Änderungen am Fabric-Mod MRwaypoints. Versionen: `mod-mc<Minecraft>-v<Version>`, siehe
[docs/RELEASES.md](../docs/RELEASES.md). Das Plugin hat ein eigenes [CHANGELOG](../plugin/CHANGELOG.md) und eigene
Versionen - welche Plugin-Versionen passen, steht in [docs/COMPATIBILITY.md](../docs/COMPATIBILITY.md).

## [Unveröffentlicht]

### Behoben & geändert
- **Neues Versionsschema:** Der Mod meldet dem Plugin seine volle Version (z.B. `STA-1.1.0-bu001`), damit Update-Hinweis und `mod.minimum-version` auch den Build vergleichen. In Fabric und Mod Menu steht weiter nur die Nummer (`1.1.0`), weil Fabric dort SemVer erwartet. Funktioniert mit allen Plugin-Versionen.

## [0.1.0] - 2026-11-04

### Neu
- **Erste Version:** Zeigt die Server-Wegpunkte von MRwaypoints in Xaero's Minimap und World Map - je Kategorie in deren Farbe (die nächstliegende Xaero-Farbe), mit den Anfangsbuchstaben des Namens. Sie liegen getrennt von deinen eigenen Wegpunkten und werden nicht in deinen Xaero-Dateien gespeichert.
- **Kategorien ein- und ausschalten:** im Menü `/waypoints` unter „Kategorien“ - die Taste `J` und der Knopf „Server-Wegpunkte …“ in Xaeros Wegpunkt-Menü öffnen es. Ein eigener Bildschirm des Mods entfällt, es gibt nur dieses eine Menü. Die Auswahl speichert der Server - sie gilt auf jedem PC und lässt sich auch auf der Spielerseite im Web umschalten.
- Braucht Fabric API und Xaero's Minimap (gebaut für 26.6.2). Auf Servern ohne MRwaypoints tut der Mod nichts.
- **Tasten** (Optionen → Steuerung → MRwaypoints, alle außer `J` frei belegbar): Wegpunkt-Menü (`J`), Wegpunkt hier erstellen (Dialog mit Name und Kategorie vom Server), Server-Wegpunkte in Xaero ein/aus (nur auf diesem PC, die Auswahl pro Kategorie bleibt).
- **Sichtweite und Anzeige** (Protokoll 2, braucht das passende Plugin): Der Mod setzt die Einstellungen aus `/waypoints` → Einstellungen um - Wegpunkte nur bis zur gewählten Zahl Chunks in jede Richtung im Spiel und auf der Minimap, auf Wunsch nur auf der World Map, entfernte trotzdem auf der World Map. Das folgt dir beim Laufen. 
- **Klare Meldung bei unpassendem Mod:** Lehnt der Server den Mod ab (zu alt oder zu neu), sagen die Tasten das jetzt, statt „Dieser Server hat kein MRwaypoints“.
- **Tastenbelegung aus dem Menü:** `/waypoints` → Einstellungen → „Tastenbelegung“ öffnet Minecrafts Steuerung (neue Nachricht `keys` vom Server). Die Taste „Wegweiser beenden“ gibt es nicht mehr - das geht im Menü oder mit `/waypoints stop`.
