# MRwaypoints

**Gemeinsame Wegpunkte für deinen Server:** Das Team legt Kategorien fest (Spawn, Shops, Farmen …), Spieler und Team tragen dort Wegpunkte ein - und jeder findet sie im Spiel über ein Menü mit Wegweiser, in Xaero's Minimap und World Map und auf der Karte der Spielerseite (mit MRadmin).

**[⬇ Download](https://mrtechnology.marcelr.at/download/mrwaypoints/)** · **[📖 Wiki](https://mrtechnology.marcelr.at/wiki/mrwaypoints/)** · **[🐞 Fehler melden](https://mrtechnology.marcelr.at/report/mrwaypoints/)**

MRwaypoints besteht aus **zwei Teilen** - beide gibt es auf der [Download-Seite](https://mrtechnology.marcelr.at/download/mrwaypoints/):

| Teil | Wer braucht ihn | Wohin |
|---|---|---|
| **Plugin** (`MRwaypoints-mc26.3-v….jar`) | der Server | Paper 26.3, Java 25 → Ordner `plugins/` |
| **Client-Mod** (`MRwaypoints-mod-mc26.3-v….jar`) | Spieler, die die Wegpunkte in Xaero's Karten sehen wollen | Fabric 26.3 mit [Fabric API](https://modrinth.com/mod/fabric-api) und [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap) → Ordner `mods/` |

| Auf einen Blick | |
|---|---|
| Server | Paper 26.3 (oder ein Fork wie Purpur), Java 25 |
| Client | nichts nötig - für Xaero's Karten der MRwaypoints-Mod (Fabric Loader + Fabric API + Xaero's Minimap) |
| Benötigt | nichts - LuckPerms empfohlen, MRadmin optional |
| Sprachen | Deutsch, Englisch - jeder Spieler automatisch in der Sprache seines Minecraft-Clients |
| Status | Vorabversion (0.x) |

## Was kann MRwaypoints?
- **Kategorien vom Team** mit Farbe, Symbol, Welten und Limits pro Spieler.
- **Wegpunkte auf dem Server:** jeder sieht dieselben, Menü `/waypoints` mit Filtern und Suche.
- **Wegweiser** in der Actionbar - ohne Mod.
- **In Xaero's Minimap und World Map** mit dem Mod, Kategorien einzeln ein- und ausblendbar.
- Mit MRadmin: Verwaltung im Browser und Wegpunkte auf der Karte der Spielerseite.

## Schnellstart
1. **[MRwaypoints herunterladen](https://mrtechnology.marcelr.at/download/mrwaypoints/)** und die JAR in den Ordner `plugins/` legen.
2. Den Server starten - vier Beispiel-Kategorien entstehen von selbst.
3. Rechte geben:
   ```text
   /lp group default permission set mrwaypoints.player true
   /lp group supporter permission set mrwaypoints.groups.support true
   /lp group staff permission set mrwaypoints.groups.staff true
   /lp group admin permission set mrwaypoints.groups.admin true
   ```
4. Kategorien anpassen (MRadmin-Weboberfläche oder `categories.yml` + `/mrwaypoints reload`).

**Update:** alte JAR durch die neue ersetzen, Server neu starten - die Config passt sich selbst an. Plugin und Mod haben eigene Versionen.

## Ganze Anleitung im Wiki
| Für Spieler | Für Admins |
|---|---|
| [Wegpunkte-Menü & Wegweiser](https://mrtechnology.marcelr.at/wiki/mrwaypoints/menue/) · [In Xaero's Minimap](https://mrtechnology.marcelr.at/wiki/mrwaypoints/xaero/) | [Kategorien](https://mrtechnology.marcelr.at/wiki/mrwaypoints/kategorien/) · [Befehle](https://mrtechnology.marcelr.at/wiki/mrwaypoints/befehle/) · [Permissions](https://mrtechnology.marcelr.at/wiki/mrwaypoints/permissions/) · [Konfiguration](https://mrtechnology.marcelr.at/wiki/mrwaypoints/konfiguration/) · [Häufige Fragen](https://mrtechnology.marcelr.at/wiki/mrwaypoints/faq/) |

Die Seiten liegen als Markdown in [`docs/wiki/`](docs/wiki/) (Seitenbaum: [`docs/wiki/index.yml`](docs/wiki/index.yml)).

## Lizenz
MRwaypoints steht unter der **[MRtechnology Lizenz](LICENSE)** (source-available, nicht Open Source). Kurz gesagt:

| ✅ Erlaubt | ❌ Nicht erlaubt |
|---|---|
| Nutzen - privat und gewerblich, auch auf Servern mit Spenden, Rängen oder Shops | Weiterveröffentlichen (auch verändert), z.B. auf Modrinth, CurseForge, in Modpacks |
| Den Mod aus der offiziellen Quelle auf deinem PC nutzen, Spielern den Link geben | Verkaufen oder gegen Geld anbieten |
| Verändern und neu kompilieren, an dein Team weitergeben | Als eigenes Werk ausgeben, Lizenzhinweis entfernen |
| Verbesserungen per Pull Request einreichen | |

Die Nutzung erfolgt auf eigenes Risiko - leg vor Updates eine Sicherung an. Maßgeblich ist der vollständige Text in [LICENSE](LICENSE).

## Für Entwickler
| Ordner | Inhalt | Bauen |
|---|---|---|
| `plugin/` | Paper-Plugin (Java 25, Gradle) - [CHANGELOG](plugin/CHANGELOG.md) | `cd plugin && ./gradlew build` |
| `mod/` | Fabric-Mod (Java 25, Fabric Loom) - [CHANGELOG](mod/CHANGELOG.md) | `cd mod && ./gradlew build` |
| `docs/` | [Wiki-Seiten](docs/wiki/), [Technische Doku](docs/DOKUMENTATION.md), [Kompatibilität](docs/COMPATIBILITY.md), [Releases](docs/RELEASES.md) | |
