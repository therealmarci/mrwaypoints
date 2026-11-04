# MRwaypoints

**Gemeinsame Wegpunkte für deinen Server:** Das Team legt Kategorien fest (Spawn, Shops, Farmen …), Spieler und Team tragen dort Wegpunkte ein - und jeder findet sie im Spiel über ein Menü mit Wegweiser, in Xaero's Minimap und World Map und auf der Karte der Spielerseite (mit MRadmin).

**[⬇ Download](https://mrtechnology.marcelr.at/download/mrwaypoints/)** · **[📖 Anleitung online](https://mrtechnology.marcelr.at/docs/mrwaypoints/)** · **[🐞 Fehler melden](https://mrtechnology.marcelr.at/report/mrwaypoints/)**

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
- **Kategorien vom Team:** Name pro Sprache, Farbe, Symbol, wer darin Wegpunkte anlegen darf und wie viele pro Spieler.
- **Wegpunkte auf dem Server gespeichert:** Jeder sieht dieselben - anders als private Wegpunkte in Xaero, die nur auf deinem PC liegen.
- **Menü `/waypoints` für alle:** meine Wegpunkte, alle Wegpunkte (mit Filter nach Kategorie, Sortierung und Suche), neue Wegpunkte an der eigenen Position anlegen - immer für die Dimension, in der du bist.
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
**`/waypoints`** (auch `/wegpunkte`) öffnet ein kleines Menü mit drei Knöpfen. Alles darin gilt für die **Welt bzw. Dimension, in der du gerade bist** - Wegpunkte im Nether siehst du im Nether.

| Knopf | Was passiert |
|---|---|
| **Meine Wegpunkte** | deine eigenen Wegpunkte hier |
| **Wegpunkt erstellen** | ein Dialog mit Name und Kategorie - nur Kategorien, in denen du gerade anlegen darfst (mit „genutzt/Limit“). Der Wegpunkt kommt an deine Position. Grau, wenn du nirgends darfst. |
| **Alle Wegpunkte** | alle Server-Wegpunkte hier |
| Mitte: **Wegweiser** | nur wenn einer läuft - Klick beendet ihn |
| unten: **In Xaero anzeigen** · **Einstellungen** · **Schließen** | „In Xaero anzeigen“ nur mit dem Mod; **Einstellungen**: Sichtweite und Anzeige in Xaero (siehe unten) |

In den Listen:

| Aktion | Was passiert |
|---|---|
| **Klick** auf einen Wegpunkt | Wegweiser in der Actionbar: Pfeil in Blickrichtung, Entfernung, ▲/▼ Höhenunterschied. Endet bei Ankunft, mit `/waypoints stop` oder oben im Startmenü. |
| **Rechtsklick** auf einen eigenen Wegpunkt | Bearbeiten: umbenennen, an die eigene Position verschieben, löschen |
| unten **Kategorie** · **Sortierung** · **Suche** | Filter: Klick wechselt (Kategorie: nächste, Sortierung: Entfernung → Name → neueste), Suche per Dialog - **Rechtsklick setzt den Filter zurück**. Oben in der Mitte steht, was gerade gefiltert ist. |

Private Wegpunkte legst du wie gewohnt direkt in Xaero's Minimap an - sie bleiben auf deinem PC.

### In Xaero's Minimap und World Map (mit Mod)
1. **Fabric API**, **Xaero's Minimap** (und gern Xaero's World Map) und den **[MRwaypoints-Mod](https://mrtechnology.marcelr.at/download/mrwaypoints/)** (Zeile „Fabric-Mod (Client)“) in den Ordner `mods/` legen.
2. Auf den Server gehen - die Server-Wegpunkte erscheinen von selbst auf der Minimap, im Spiel und auf der World Map, in der Farbe ihrer Kategorie. Sie liegen getrennt von deinen eigenen Xaero-Wegpunkten.
3. **Kategorien ein- und ausschalten:** Taste **`J`** (änderbar unter Optionen → Steuerung → MRwaypoints), Knopf **„Server-Wegpunkte …“** in Xaeros Wegpunkt-Menü oder **„In Xaero anzeigen“** in `/waypoints`. Deine Auswahl speichert der Server - sie gilt auf jedem PC und auch auf der Spielerseite im Web.
4. **Tasten** (Optionen → Steuerung → MRwaypoints): nur `J` ist vorbelegt, die anderen legst du selbst fest, damit nichts mit Xaero oder anderen Mods kollidiert.

   | Taste | Was passiert |
   |---|---|
   | Kategorien (`J`) | Bildschirm mit allen Kategorien zum Ein- und Ausschalten |
   | Wegpunkt hier erstellen | der Dialog mit Name und Kategorie - wie im Menü |
   | Server-Wegpunkte ein/aus | alle Server-Wegpunkte in Xaero aus- und wieder einblenden (nur auf diesem PC, deine Auswahl pro Kategorie bleibt) |
   | Wegpunkt-Menü öffnen | `/waypoints` |
   | Wegweiser beenden | wie `/waypoints stop` |

5. **Sichtweite und Anzeige:** In `/waypoints` → **Einstellungen** (oder Knopf „Sichtweite und Anzeige …“ im Bildschirm von `J`) legst du fest, wie du die Server-Wegpunkte siehst. Der Server speichert das für dich.

   | Einstellung | Was sie tut |
   |---|---|
   | **Sichtweite** | bis wie viele Blöcke die Wegpunkte im Spiel und auf der Minimap erscheinen (Linksklick weiter, Rechtsklick näher; Stufen legt der Server fest, auch „unbegrenzt“). Xaeros eigene Grenze für Wegpunkte im Spiel gilt zusätzlich. |
   | **Anzeige** | „Welt, Minimap und Weltkarte“ oder „nur Weltkarte“ |
   | **Entfernte auf der Weltkarte** | Wegpunkte außerhalb der Sichtweite trotzdem auf der World Map zeigen (nur bei begrenzter Sichtweite) |

   > [!NOTE]
   > Xaero zeigt einen Wegpunkt im Spiel und auf der Minimap immer gemeinsam - „nur Minimap“ oder „nur im Spiel“ gibt es pro Wegpunkt nicht. Dafür hat Xaero eigene Einstellungen, die dann für alle Wegpunkte gelten.

## Für Server-Admins

### Kategorien
Am bequemsten in der MRadmin-Weboberfläche (Seite **Wegpunkte**, `mrwaypoints.admin.categories`) - ohne MRadmin direkt in der Datei.
In `plugins/MRwaypoints/categories.yml`, danach `/mrwaypoints reload`:

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

Mitgeliefert sind **Spawn & Warps** (nur Team), **Shops** (Spieler, je 3), **Farmen** (Spieler, je 5, nicht auf der Karte) und **Sehenswürdigkeiten** (Spieler, je 3).

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
| `mrwaypoints.player` | `/waypoints` mit Wegweiser und eigenen Einstellungen für Xaero, anlegen in Kategorien mit `players-create: true`, `/mrwaypoints version` |
| `mrwaypoints.groups.support` | Spieler + MRwaypoints im MRadmin-Dashboard und die Seite **Wegpunkte** im Web ansehen |
| `mrwaypoints.groups.staff` | Support + in **jeder** Kategorie anlegen, **alle** Wegpunkte bearbeiten und löschen, ohne Limit |
| `mrwaypoints.groups.admin` | Alles: Staff + zu Wegpunkten teleportieren, Kategorien im Web verwalten, Reload, Update-Hinweis |

<details>
<summary>Alle einzelnen Permissions</summary>

Standard ist `op`, die Rechte pro Kategorie haben standardmäßig niemand (sie kommen über die Gruppen bzw. `players-create`).

| Permission | Wofür |
|---|---|
| `mrwaypoints.use` | `/waypoints` |
| `mrwaypoints.settings` | `/waypoints` → Einstellungen: Sichtweite und Anzeige in Xaero (mit Mod) |
| `mrwaypoints.version` | `/mrwaypoints version` |
| `mrwaypoints.create.<kategorie>` · `mrwaypoints.create.*` | Wegpunkte in einer bzw. allen Kategorien anlegen |
| `mrwaypoints.manage.<kategorie>` · `mrwaypoints.manage.*` | alle Wegpunkte einer bzw. aller Kategorien bearbeiten und löschen, ohne Limit |
| `mrwaypoints.teleport` | Shift-Klick im Menü: teleportieren |
| `mrwaypoints.admin.view` | MRwaypoints im MRadmin-Dashboard, Seite **Wegpunkte** in der Weboberfläche |
| `mrwaypoints.admin.categories` | Kategorien im Web anlegen, ändern, sortieren, löschen |
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
| `mod` | Mod für Xaero: Mindestversion (`minimum-version`), Download-Link |
| `view` | Einstellungen der Spieler für Xaero: wählbare Sichtweiten (`ranges`, `0` = unbegrenzt) und Standards (`default-range`, `default-display` `everywhere`/`world-map`, `default-far-on-world-map`) |
| `update-checker` · `gui` | Update-Hinweise, Menüs |

**Texte:** Alle Texte stehen in `plugins/MRwaypoints/lang/<sprache>.yml`, die Namen der Kategorien in `categories.yml`. Die Konsole ist immer Englisch.

---

## Zusammenarbeit mit anderen Plugins
MRwaypoints läuft **allein**. Zusammen mit anderen MRtechnology-Plugins kann es mehr:

| Funktion | Braucht |
|---|---|
| Seite **Wegpunkte** fürs Team: Kategorien anlegen, ändern, sortieren, löschen (`mrwaypoints.admin.categories`), alle Wegpunkte umbenennen, in eine andere Kategorie verschieben, löschen (`mrwaypoints.manage.*`) | MRadmin |
| Tab **Karte** auf der Spielerseite (`/web`): eine Ebene pro Kategorie mit `map: true`; Rechtsklick auf eine freie Stelle: „Wegpunkt erstellen“ mit Name und Kategorie (Höhe = Boden an der Stelle), auf eigene „Wegpunkt bearbeiten“ (Name, Kategorie) und „Wegpunkt löschen“, auf jeden den Wegweiser im Spiel starten; „in Xaero: an/aus“ pro Kategorie | MRadmin |
| Knopf im MRadmin-Dashboard (öffnet `/waypoints`) | MRadmin |
| Wegpunkte in Xaero's Minimap und World Map, Kategorien ein- und ausblenden | MRwaypoints-Mod + Xaero's Minimap |

## Häufige Fragen
**Ein Spieler kann keinen Wegpunkt anlegen.** In der Kategorie braucht er `mrwaypoints.create.<kategorie>` - oder die Kategorie hat `players-create: true` und er hat `mrwaypoints.player`. Ist das Limit (`max-per-player`) erreicht, wird der Knopf grau.

**Brauchen Spieler einen Mod?** Nein. Menü und Wegweiser funktionieren mit jedem Client. Der Mod bringt die Wegpunkte zusätzlich in Xaero's Karten.

**Mit Mod erscheinen keine Wegpunkte in Xaero.** Ist die Kategorie für dich eingeschaltet (Taste `J`)? Ist Xaero's **Minimap** installiert (die World Map allein reicht nicht)? Im Client-Log steht „Connected to MRwaypoints …“ bzw. bei einer nicht unterstützten Xaero-Version eine Fehlermeldung.

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
| `docs/` | [Technische Doku](docs/DOKUMENTATION.md), [Kompatibilität](docs/COMPATIBILITY.md), [Releases](docs/RELEASES.md) | |
