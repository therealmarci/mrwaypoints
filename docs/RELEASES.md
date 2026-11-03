# Versionen & Releases

Plugin und Mod liegen im selben Repository (Ordner `plugin/` und `mod/`, gemeinsame Branches), werden aber
**getrennt versioniert und veröffentlicht**. Ändert sich nur das Plugin, gibt es nur ein Plugin-Release - Spieler
müssen ihren Mod nicht aktualisieren. Ob Plugin und Mod zusammenpassen, regelt das **Protokoll**, nicht die
Versionsnummer: [COMPATIBILITY.md](COMPATIBILITY.md).

## Versionsschema

```
plugin-mc26.3-v0.2.0        mod-mc26.3-v0.1.0
  │      │      └─ Version (SemVer): MAJOR.MINOR.PATCH
  │      └──────── Minecraft-Version
  └─────────────── Teil: plugin oder mod
```

| Teil | Erhöhen, wenn … | Beispiel |
|---|---|---|
| **MAJOR** | große Releases, inkompatible Änderungen (Config, Befehle, Permissions) | `1.4.2` → `2.0.0` |
| **MINOR** | neue Funktionen | `1.4.2` → `1.5.0` |
| **PATCH** | Hotfixes und Fehlerbehebungen | `1.4.2` → `1.4.3` |

| | Plugin | Mod |
|---|---|---|
| Version in | `plugin/gradle.properties` (`version`, `mcVersion`) | `mod/gradle.properties` (`version`, `minecraft_version`) |
| Changelog | [plugin/CHANGELOG.md](../plugin/CHANGELOG.md) | [mod/CHANGELOG.md](../mod/CHANGELOG.md) |
| Tag | `plugin-mc26.3-v0.2.0` | `mod-mc26.3-v0.1.0` |
| JAR | `MRwaypoints-mc26.3-v0.2.0.jar` | `MRwaypoints-mod-mc26.3-v0.1.0.jar` |
| Workflow | `release-plugin.yml` (GitHub + Modrinth) | `release-mod.yml` (GitHub) |
| Update-Hinweis | Admins beim Join, `/mrwaypoints version` | im Einstellungsbildschirm des Mods |

- **Vorabversionen:** `0.x` oder ein Suffix wie `-rc.1`. Auf GitHub als Pre-Release markiert.
- Als „Latest“ markiert GitHub nur stabile **Plugin**-Releases von `main` (Mod-Releases nie), damit Server-Admins
  auf der Startseite das Plugin finden.

## Branches
| Branch | Zweck | Releases |
|---|---|---|
| `beta` | Entwicklung für die **neueste** Minecraft-Version | Vorabversionen |
| `main` | stabiler Stand für die **neueste** Minecraft-Version | stabile Versionen |
| `mc/<version>` | Wartung einer **älteren** Minecraft-Version, z.B. `mc/26.3` | stabile Versionen (Hotfixes) |

Beide Teile teilen sich die Branches - Doku, Protokoll und Kompatibilitätsregeln gehören zu beiden.
Der Update-Check meldet nur Versionen **derselben** Minecraft-Version.

## Welcher Teil braucht ein Release?

| Änderung | Plugin-Release | Mod-Release |
|---|---|---|
| nur Plugin (Server-Logik, Menüs, Texte, Config) | ja | nein |
| nur Mod (Tasten, Darstellung, Design-Menü) | nein | ja |
| Protokoll (Nachrichten zwischen beiden) | ja | ja - siehe [COMPATIBILITY.md](COMPATIBILITY.md) |

## Release-Ablauf (Checkliste, je Teil)
1. `version` in `plugin/gradle.properties` bzw. `mod/gradle.properties` erhöhen.
2. Protokoll prüfen ([COMPATIBILITY.md](COMPATIBILITY.md)): hat sich eine Nachricht geändert? Dann Protokoll erhöhen,
   Historie dort ergänzen und **beide** Teile veröffentlichen.
3. Nur Plugin: neuer Config-Schlüssel → `ConfigUpdater.CURRENT_VERSION` und `config-version` erhöhen; geänderte
   Texte → `lang-version` und die Liste in `MRWaypoints` (Messages).
4. Abschnitt `## [<version>] - <datum>` im CHANGELOG des Teils anlegen (wird zum Release-Text).
5. Bauen (`./gradlew build` im Ordner), auf dem Testserver mit dem Mod testen.
6. Committen, pushen, Tag setzen und pushen, z.B. `git tag plugin-mc26.3-v0.2.0 && git push origin plugin-mc26.3-v0.2.0`.

Die Workflows prüfen, ob der Tag zur `gradle.properties` des Teils passt, lassen stabile Versionen nur von `main`
oder `mc/<mcVersion>` zu (Vorabversionen auch von `beta`), bauen den Teil und erstellen das GitHub-Release.

## Neue Minecraft-Version unterstützen (z.B. 26.4)
1. **Alte Version sichern** (falls weiter gepflegt): `git branch mc/26.3 main && git push -u origin mc/26.3`
2. Auf `beta` anpassen:
   - `plugin/gradle.properties`: `mcVersion`, `paperApi`, `supportedGameVersions`
   - `mod/gradle.properties`: `minecraft_version`, `loader_version`, `fabric_api_version`, ggf. `loom_version`
     und `modmenu_version` (aktuelle Werte: https://fabricmc.net/develop)
3. Der Mod greift über ein Mixin in `Hud.extractItemHotbar` ein: nach einem Minecraft-Update prüfen, ob die
   Methode und der erste `blitSprite`-Aufruf (Hotbar-Hintergrund) noch so heißen.
4. Beide Teile neu veröffentlichen (eine neue Minecraft-Version braucht immer beide).
