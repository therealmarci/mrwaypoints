# MRwaypoints

**Gemeinsame Wegpunkte für deinen Server:** Das Team legt Kategorien fest (Spawn, Shops, Farmen …), Spieler und Team tragen dort Wegpunkte ein - und jeder findet sie im Spiel über ein Menü mit Wegweiser, in Xaero's Minimap und World Map und auf der Webkarte.

**[⬇ Download](https://mrtechnology.marcelr.at/download/mrwaypoints/)** · **[📖 Anleitung online](https://mrtechnology.marcelr.at/docs/mrwaypoints/)** · **[🐞 Fehler melden](https://mrtechnology.marcelr.at/report/mrwaypoints/)**

> [!NOTE]
> MRwaypoints ist **in Entwicklung**. Fertig ist das Plugin mit Menü und Wegweiser. Der Client-Mod für Xaero's Karten, die Seiten in der MRadmin-Weboberfläche und die Ebene auf der Webkarte folgen.

| Auf einen Blick | |
|---|---|
| Server | Paper 26.3 (oder ein Fork wie Purpur), Java 25 |
| Client | nichts nötig - für Xaero's Karten später der MRwaypoints-Mod (Fabric) |
| Benötigt | nichts - LuckPerms empfohlen, MRadmin optional |
| Sprachen | Deutsch, Englisch - jeder Spieler automatisch in der Sprache seines Minecraft-Clients |
| Status | Vorabversion (0.x) |

## Was kann MRwaypoints?
- **Kategorien vom Team:** Name pro Sprache, Farbe, Symbol, wer darin Wegpunkte anlegen darf und wie viele pro Spieler.
- **Wegpunkte auf dem Server gespeichert:** Jeder sieht dieselben - anders als private Wegpunkte in Xaero, die nur auf deinem PC liegen.
- **Menü `/waypoints` für alle:** Kategorien, Wegpunkte nach Entfernung sortiert, neue Wegpunkte an der eigenen Position anlegen.
- **Wegweiser:** Ein Klick auf einen Wegpunkt zeigt in der Actionbar einen Pfeil, die Entfernung und den Höhenunterschied - ohne Mod.
- **Teleport** nur fürs Team (Admins).

## Schnellstart
1. **[MRwaypoints herunterladen](https://mrtechnology.marcelr.at/download/mrwaypoints/)** und die JAR in den Ordner `plugins/` legen.
2. Den Server starten. Dabei entstehen `plugins/MRwaypoints/config.yml`, `categories.yml` mit vier Beispiel-Kategorien und die Sprachdateien in `plugins/MRwaypoints/lang/`.
3. **Rechte geben** - ohne sie dürfen nur OPs:
   ```text
   /lp group default permission set mrwaypoints.player true
   /lp group supporter permission set mrwaypoints.groups.support true
   /lp group staff permission set mrwaypoints.groups.staff true
   /lp group admin permission set mrwaypoints.groups.admin true
   ```
4. Die Kategorien in `plugins/MRwaypoints/categories.yml` anpassen und `/mrwaypoints reload`.

**Update:** Die alte JAR durch die neue ersetzen und den Server neu starten. Die Config wird automatisch angepasst: Deine Werte bleiben, neue Optionen kommen dazu, und die alte Datei wird als `config.v<N>.backup.yml` gesichert.

---

## Für Spieler
**`/waypoints`** (auch `/wegpunkte`) öffnet die Kategorien. Darin:

| Aktion | Was passiert |
|---|---|
| **Klick** auf einen Wegpunkt | Wegweiser in der Actionbar: Pfeil in Blickrichtung, Entfernung, ▲/▼ Höhenunterschied. Endet bei Ankunft, mit `/waypoints stop` oder oben im Menü. |
| **Rechtsklick** auf einen eigenen Wegpunkt | Bearbeiten: umbenennen, an die eigene Position verschieben, löschen |
| **Neuer Wegpunkt hier** (unten in der Mitte) | Legt einen Wegpunkt an deiner Position an, den Namen gibst du im Dialog ein. Nur in Kategorien, in denen du darfst - mit Anzeige, wie viele du dort schon hast. |

Liegt ein Wegpunkt in einer anderen Welt, zeigt der Wegweiser, in welcher. Private Wegpunkte legst du wie gewohnt direkt in Xaero's Minimap an - sie bleiben auf deinem PC.

## Für Server-Admins

### Kategorien
In `plugins/MRwaypoints/categories.yml`, danach `/mrwaypoints reload`:

| Schlüssel | Bedeutung |
|---|---|
| *id* (der Name des Eintrags) | kurz, klein, Buchstaben/Ziffern/`-`/`_` - später nicht mehr ändern, Wegpunkte und Permissions hängen daran |
| `name.de` · `name.en` | Name pro Sprache |
| `color` | Farbe auf den Karten und in den Menüs, `"#RRGGBB"` |
| `icon` | Item im Menü, z.B. `EMERALD` |
| `players-create` | `true` = jeder Spieler (`mrwaypoints.player`) darf hier anlegen, `false` = nur mit `mrwaypoints.create.<id>` |
| `max-per-player` | so viele eigene Wegpunkte pro Spieler, `0` = unbegrenzt (wer `mrwaypoints.manage.<id>` hat, ist nie begrenzt) |
| `visible` | in Xaero's Karte standardmäßig eingeblendet (Spieler schalten Kategorien mit dem Mod um) - *kommt mit dem Mod* |
| `map` | auf der Webkarte zeigen (die ist öffentlich) - *kommt mit der MRadmin-Anbindung* |

Mitgeliefert sind **Spawn & Warps** (nur Team), **Shops** (Spieler, je 3), **Farmen** (Spieler, je 5, nicht auf der Webkarte) und **Sehenswürdigkeiten** (Spieler, je 3).

> [!WARNING]
> Löschst du eine Kategorie aus der Datei, verschwinden ihre Wegpunkte aus den Menüs, bleiben aber in `waypoints.yml`. Mit derselben id kommen sie zurück.

### Befehle

| Befehl | Was er tut | Permission |
|---|---|---|
| `/waypoints` (`/wegpunkte`, `/wps`) | Menü mit allen Wegpunkten | `mrwaypoints.use` |
| `/waypoints stop` | Wegweiser beenden | `mrwaypoints.use` |
| `/mrwaypoints reload` | Config, Kategorien und Texte neu laden | `mrwaypoints.admin.reload` |
| `/mrwaypoints version` | Version und Update-Status | `mrwaypoints.version` |

### Permissions
Am einfachsten vergibst du die **Gruppen** - sie sind in allen MRtechnology-Plugins gleich aufgebaut, jede enthält die darunter. Standardmäßig haben sie nur OPs.

| Gruppe | Enthält |
|---|---|
| `mrwaypoints.player` | `/waypoints` mit Wegweiser, anlegen in Kategorien mit `players-create: true`, `/mrwaypoints version` |
| `mrwaypoints.groups.support` | Spieler + die Wegpunkte-Seite in MRadmin ansehen *(kommt)* |
| `mrwaypoints.groups.staff` | Support + in **jeder** Kategorie anlegen, **alle** Wegpunkte bearbeiten und löschen, ohne Limit |
| `mrwaypoints.groups.admin` | Alles: Staff + zu Wegpunkten teleportieren, Kategorien im Web verwalten *(kommt)*, Reload, Update-Hinweis |

<details>
<summary>Alle einzelnen Permissions</summary>

Standard ist `op`, die Rechte pro Kategorie haben standardmäßig niemand (sie kommen über die Gruppen bzw. `players-create`).

| Permission | Wofür |
|---|---|
| `mrwaypoints.use` | `/waypoints` |
| `mrwaypoints.version` | `/mrwaypoints version` |
| `mrwaypoints.create.<kategorie>` · `mrwaypoints.create.*` | Wegpunkte in einer bzw. allen Kategorien anlegen |
| `mrwaypoints.manage.<kategorie>` · `mrwaypoints.manage.*` | alle Wegpunkte einer bzw. aller Kategorien bearbeiten und löschen, ohne Limit |
| `mrwaypoints.teleport` | Shift-Klick im Menü: teleportieren |
| `mrwaypoints.admin.view` | Wegpunkte-Seite in der MRadmin-Weboberfläche *(kommt)* |
| `mrwaypoints.admin.categories` | Kategorien im Web anlegen, ändern, löschen *(kommt)* |
| `mrwaypoints.admin.reload` | `/mrwaypoints reload` |
| `mrwaypoints.admin.update` | Update-Hinweis beim Betreten des Servers |

Eigene Wegpunkte darf jeder Ersteller immer umbenennen, verschieben und löschen.
</details>

> [!TIP]
> Shops nur für VIPs? `players-create: false` setzen und `/lp group vip permission set mrwaypoints.create.shops true`.

### Konfiguration
Alles steht kommentiert in `plugins/MRwaypoints/config.yml`. Änderungen übernimmst du mit `/mrwaypoints reload`.

| Abschnitt | Wofür |
|---|---|
| `language` · `fallback-language` · `server-language` | Sprachen - jeder Spieler automatisch in seiner |
| `guide` | Wegweiser: wie oft der Pfeil aktualisiert (`update-ticks`), ab wie vielen Blöcken „angekommen“ (`arrive-distance`), Ton |
| `waypoints` | längster Name (`max-name-length`) |
| `update-checker` · `gui` | Update-Hinweise, Menüs |

**Texte:** Alle Texte stehen in `plugins/MRwaypoints/lang/<sprache>.yml`, die Namen der Kategorien in `categories.yml`. Die Konsole ist immer Englisch.

---

## Zusammenarbeit mit anderen Plugins
MRwaypoints läuft **allein**. Zusammen mit anderen MRtechnology-Plugins kann es mehr:

| Funktion | Braucht |
|---|---|
| Wegpunkte-Seite fürs Team und Kategorien im Web verwalten *(kommt)* | MRadmin |
| Wegpunkte auf der Spielerseite (`/web`) *(kommt)* | MRadmin |
| Kategorien als Ebenen auf der Webkarte *(kommt)* | MRadmin + squaremap |
| Kategorien in Xaero's Minimap und World Map ein- und ausblenden *(kommt)* | MRwaypoints-Mod + Xaero's Minimap |

## Häufige Fragen
**Ein Spieler kann keinen Wegpunkt anlegen.** In der Kategorie braucht er `mrwaypoints.create.<kategorie>` - oder die Kategorie hat `players-create: true` und er hat `mrwaypoints.player`. Ist das Limit (`max-per-player`) erreicht, wird der Knopf grau.

**Brauchen Spieler einen Mod?** Nein. Menü und Wegweiser funktionieren mit jedem Client. Der Mod (kommt) bringt die Wegpunkte zusätzlich in Xaero's Karten.

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
| `docs/` | [Technische Doku](docs/DOKUMENTATION.md), [Releases](docs/RELEASES.md) | |
