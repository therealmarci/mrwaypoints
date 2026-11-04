# MRwaypoints - Technische Dokumentation

## 1. Überblick
| | |
|---|---|
| Plattform | Paper 26.3 (`paper-api:26.3.build.200-stable`), Java 25 |
| Build | Gradle (Wrapper in `plugin/`), JUnit 6, Minotaur für Modrinth |
| Paket | `at.marcelr.mrwaypoints` |
| Abhängigkeiten | keine Pflicht. Optional MRadmin (Modul-API `api-1.9.0`, `compileOnly`, `softdepend`) |

## 2. Projektstruktur
```text
plugin/src/main/java/at/marcelr/mrwaypoints/
├─ MRWaypoints.java       Hauptklasse: Config, Texte, Store, Wegweiser, Befehle
├─ Settings.java          Werte aus config.yml
├─ ConfigUpdater.java     config.yml aktuell halten (UTF-8 mit BOM, Sicherung)
├─ Messages.java          Sprachdateien, Sprache pro Spieler (wie in allen MR-Plugins)
├─ WaypointPerms.java     Permissions, auch die pro Kategorie (zur Laufzeit registriert)
├─ WaypointActions.java   anlegen, umbenennen, verschieben, löschen, teleportieren - mit Prüfungen und Meldungen
├─ Guide.java             Wegweiser in der Actionbar
├─ ModBridge.java         Verbindung zum Mod (Handshake, Zustand, Aktionen, Tastenbelegung), Compatibility (Protokoll)
├─ UpdateChecker.java     Update-Prüfung (GitHub/Modrinth)
├─ command/               /waypoints, /mrwaypoints
├─ gui/                   Menu (gemeinsamer Glas-Rahmen), MenuListener, Items, MainMenu, ListMenu, WaypointMenu, CategoryMenu,
│                         SettingsMenu, ConfirmMenu, CreateDialog, NameDialog
├─ api/                   MRadminLink (ohne MRadmin-Typen), MRadminModule (Dashboard, Team-Seite, Karte der Spielerseite)
├─ util/                  Layout (Menü-Plätze, Pfeil), Wire (Nachrichtenformat) - mit Tests; LanguageChoice, TitleWidth, Versions
└─ waypoint/              Category, Waypoint, View, WaypointStore (categories.yml, waypoints.yml, players.yml)
plugin/src/main/resources/
├─ config.yml             UTF-8 mit BOM! Aufbau wie alle MR-Plugins (general, Funktionen, updates, gui)
├─ web-config.yml         Formular der config.yml für MRadmins Browser-Konfiguration (Test: WebConfigFormTest)
├─ categories.yml         Beispiel-Kategorien (beim ersten Start kopiert)
├─ plugin.yml, build-info.properties
└─ lang/de.yml, lang/en.yml
```

## 3. Daten
- **`categories.yml`** (Server-Admin oder MRadmin-Web): Abschnitt `categories.<id>` mit `name.<sprache>`, `color`, `icon`, `players-create`, `max-per-player`, `visible`, `map`, `worlds` (Weltnamen, leer = alle; `Category#allows`, `WaypointStore#shown` blendet Wegpunkte in anderen Welten überall aus). Die Reihenfolge der Datei ist die Reihenfolge in Menüs und auf den Karten. Wird aus dem Web gespeichert, schreibt `WaypointStore#saveCategories` die Datei neu und übernimmt den Kommentarblock aus der mitgelieferten Datei.
- **`waypoints.yml`**: `next-id` und `waypoints.<id>` mit `category`, `name`, `world` (UUID), `world-name`, `x`/`y`/`z` (Block), `creator`, `creator-name`, `created`.
- Geschrieben wird im Hintergrund über einen eigenen Thread (Reihenfolge bleibt), mit Temp-Datei und Umbenennen. `close()` beim Abschalten wartet darauf.
- Jede Änderung ruft die `onChange`-Listener auf (MRadmin-Seiten, Mod-Abgleich).

## 4. Permissions pro Kategorie
`WaypointPerms#register` legt für jede Kategorie `mrwaypoints.create.<id>` und `mrwaypoints.manage.<id>` an (Default `false`) und hängt sie als Kinder an `mrwaypoints.create.*` / `mrwaypoints.manage.*` (aus der plugin.yml, in `groups.staff`). Bei `players-create: true` wird `create.<id>` zusätzlich Kind von `mrwaypoints.player`. Gelöschte Kategorien verlieren ihre Permissions wieder. Danach `recalculatePermissions()` für alle Online-Spieler. LuckPerms liest Kinder von registrierten Permissions wie die aus der plugin.yml.

Bearbeiten darf der Ersteller oder wer `manage.<kategorie>` hat; `manage` hebt auch das Limit auf.

## 5. Wegweiser
`Guide` merkt pro Spieler die Wegpunkt-ID und zeigt alle `guide.update-ticks` in der Actionbar: Pfeil (`Layout.arrowIndex`: Winkel zum Ziel minus Blickrichtung, acht Richtungen; Minecraft-Yaw 0 = Süden, 90 = Westen, rechts drehen erhöht den Yaw), Entfernung, ab 4 Blöcken Höhenunterschied ▲/▼. Ende bei Ankunft, `/waypoints stop`, gelöschtem Wegpunkt oder Quit.

## 6. Menüs
Alle symmetrisch. `MainMenu` (3 Reihen): oben Meine (2) · Erstellen (4) · Alle (6), Wegweiser in der Mitte (13), unten Schließen (22) bzw. mit Mod „In Xaero“ (21) + Schließen (23). `ListMenu` (6 Reihen, „meine“ oder „alle“, nur die Welt des Spielers): 28 Plätze, oben (4) die aktiven Filter, unten Zurück (45) · Kategorie (46) · ◀ (48) · Sortierung (49) · ▶ (50) · Suche (52) · Schließen (53); Rechtsklick auf einen Filter setzt ihn zurück, die Filter leben nur im offenen Menü. Aktionen eines Wegpunkts (`WaypointMenu`) um Platz 13 (`util/Layout`). Dialoge: `CreateDialog` (Name + Kategorie als Auswahlliste, nur erlaubte), `NameDialog` (umbenennen, suchen).

## 7. MRadmin (`api/MRadminLink`, `api/MRadminModule`)
- `MRadminLink` hat keine MRadmin-Typen und verbindet sich am Ende von `onEnable` (bzw. wenn MRadmin später startet). `WaypointStore#onChange` → `changed()` → `webChanged`.
- `MRadminModule` (nur mit MRadmin geladen): Dashboard-Knopf mit `viewPermission` `mrwaypoints.admin.view`, `openMenu` öffnet `/waypoints`.
- **Karte der Spielerseite (API 8):** `selfMap` = eine `SelfMapLayer` pro Kategorie mit `map: true` (alle Welten, MRadmin zeigt die gewählte), Marker = Wegpunkte mit Aktionen `guide` (nur online in der Welt), `rename`/`delete` (nur eigene); `here` = `create` (Name per Eingabe, Höhe `getHighestBlockYAt + 1`, Weltgrenze geprüft). Anlegen offline nur mit `players-create` (Rechte offline unbekannt), online wie im Spiel (`createBlocked`). Schalter `xaero` = `WaypointStore#setVisible`. Der frühere Bereich „Wegpunkte“ der Spielerseite ist entfallen.
- **Konfiguration im Browser (API 12):** `webConfig()` gibt `config.yml`, `web-config.yml` und `plugin.reload()` an MRadmin. Neue oder umbenannte Config-Schlüssel immer auch in `web-config.yml` eintragen (Namen und Hilfe DE/EN) - `WebConfigFormTest` prüft das. Umbenennungen in `ConfigUpdater.RENAMED_KEYS` (v7: `general.*`, `updates.*`).

## 8. Mod für Xaero (`ModBridge`, `mod/`)
- Protokoll, Handshake und Nachrichten: [COMPATIBILITY.md](COMPATIBILITY.md). `ModBridge` (Plugin) wie in MRtoolbar: wartet mit Nachrichten, bis der Client den Kanal angemeldet hat, schickt nach OK den Zustand und bei jeder Änderung (`WaypointStore#onChange`, einmal pro Tick) neu an alle mit Mod.
- **Auswahl pro Spieler:** `WaypointStore#visible/setVisible`, gespeichert in `players.yml` (nur Abweichungen von `visible` der Kategorie). Geändert im Menü `/waypoints` → Kategorien (`gui/CategoryMenu`) oder über `selfMapAction` `xaero` von der Karte der Spielerseite; die Einstellungen für Xaero (`View`: Sichtweite in Chunks je Richtung, nur Weltkarte, Entfernte auf der Weltkarte) unter `view.<uuid>` in `players.yml`, Standards aus `view` in der config.yml; danach bekommt der Spieler den neuen Zustand und MRadmin lädt die Seite neu.
- **Mod:** `MRwaypointsClient` (Handshake, Taste `J` und Knopf in `xaero.common.gui.GuiWaypoints` per `ScreenEvents.AFTER_INIT` - Klasse per Name, ohne Mixin - schicken die Aktion „Menü“, der Server öffnet `/waypoints`; Nachricht `keys` öffnet `KeyBindsScreen`; kein eigener Bildschirm), `XaeroBridge` (einzige Klasse mit Xaero-Typen). `XaeroBridge.apply` leert die eigenen Herkünfte und füllt sie neu, sobald ein Zustand kommt oder Xaero eine neue Welt-Sitzung hat (`getAutoRootContainer` hat sich geändert); Fehler → einmal loggen, danach still.

## 9. Testen
| # | Schritt | Erwartung |
|---|---|---|
| 1 | Erster Start | `categories.yml` mit 4 Kategorien, Log „4 categories, 0 waypoints loaded.“ |
| 2 | `/waypoints` → Wegpunkt erstellen | ein Dialog mit Name und Kategorie, danach Wegpunkt unter „Meine Wegpunkte“, Meldung mit Koordinaten im Hover |
| 3 | 4. Shop als Spieler (Limit 3) | Shops fehlt in der Auswahl des Dialogs |
| 4 | Klick auf Wegpunkt, weglaufen und drehen | Pfeil dreht mit, Entfernung zählt, bei ≤ 5 Blöcken „Angekommen“ |
| 5 | Rechtsklick auf fremden Wegpunkt ohne manage | nichts (kein Bearbeiten-Hinweis in der Beschreibung) |
| 6 | Kategorie in `categories.yml` auf `players-create: false`, reload | Spieler ohne `create.<id>` sehen „Neuer Wegpunkt hier“ nicht mehr |
