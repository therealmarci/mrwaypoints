# Überblick

**Gemeinsame Wegpunkte für deinen Server:** Das Team legt Kategorien fest (Spawn, Shops, Farmen …), Spieler und Team tragen dort Wegpunkte ein - und jeder findet sie im Spiel über ein Menü mit Wegweiser, in Xaero's Minimap und World Map und auf der Karte der Spielerseite (mit MRadmin).

> [!NOTE]
> MRwaypoints ist eine **Vorabversion**. Rückmeldungen gern über [Fehler melden](https://mrtechnology.marcelr.at/report/mrwaypoints/).

MRwaypoints besteht aus **zwei Teilen** - beide gibt es auf der [Download-Seite](https://mrtechnology.marcelr.at/download/mrwaypoints/):

| Teil | Wer braucht ihn | Wohin |
|---|---|---|
| **Plugin** (`MRwaypoints-mc26.3-v….jar`) | der Server | Paper 26.3, Java 25 → Ordner `plugins/` |
| **Client-Mod** (`MRwaypoints-mod-mc26.3-v….jar`) | Spieler, die die Wegpunkte in Xaero's Karten sehen wollen | Fabric 26.3 mit [Fabric API](https://modrinth.com/mod/fabric-api) und [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap) → Ordner `mods/` |

Spieler **ohne** Mod nutzen Menü und Wegweiser ganz normal. Plugin und Mod haben eigene Versionen.

| Auf einen Blick | |
|---|---|
| Server | Paper 26.3 (oder ein Fork wie Purpur), Java 25 |
| Client | nichts nötig - für Xaero's Karten der MRwaypoints-Mod (Fabric Loader + Fabric API + Xaero's Minimap) |
| Benötigt | nichts - LuckPerms empfohlen, MRadmin optional |
| Sprachen | Deutsch, Englisch - jeder Spieler automatisch in der Sprache seines Minecraft-Clients |
| Status | Vorabversion (0.x) |

## Was kann MRwaypoints?
- **Kategorien vom Team:** Name pro Sprache, Farbe, Symbol, Welten, wer darin Wegpunkte anlegen darf und wie viele pro Spieler. → [Kategorien](kategorien.md)
- **Wegpunkte auf dem Server gespeichert:** Jeder sieht dieselben - anders als private Wegpunkte in Xaero, die nur auf deinem PC liegen.
- **Menü `/waypoints` für alle:** meine Wegpunkte, alle Wegpunkte (mit Filter nach Kategorie, Sortierung und Suche), neue Wegpunkte an der eigenen Position anlegen - immer für die Dimension, in der du bist. → [Wegpunkte-Menü & Wegweiser](menue.md)
- **Wegweiser:** Ein Klick auf einen Wegpunkt zeigt in der Actionbar einen Pfeil, die Entfernung und den Höhenunterschied - ohne Mod.
- **In Xaero's Minimap und World Map** mit dem Mod, Kategorien einzeln ein- und ausblendbar. → [In Xaero's Minimap](xaero.md)
- **Teleport** nur fürs Team (Admins).

## Wo anfangen?
| Du bist … | Lies zuerst |
|---|---|
| **Spieler** | [Wegpunkte-Menü & Wegweiser](menue.md), dann [In Xaero's Minimap](xaero.md) |
| **Server-Betreiber** | [Schnellstart](schnellstart.md), dann [Kategorien](kategorien.md) und [Permissions](permissions.md) |
