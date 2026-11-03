# Kompatibilität Plugin ↔ Mod

MRwaypoints besteht aus zwei Teilen:

| Teil | Ordner | Läuft auf | Aufgabe |
|---|---|---|---|
| **Plugin** (Paper) | `plugin/` | Server | Kategorien, Wegpunkte, Rechte, Menü `/waypoints`, Wegweiser, Webkarte und Web über MRadmin, welche Kategorien ein Spieler in Xaero sehen will (`players.yml`) |
| **Mod** (Fabric) | `mod/` | Client | Zeigt die Server-Wegpunkte in Xaero's Minimap und World Map, Bildschirm zum Ein- und Ausschalten der Kategorien (Taste `J`, Knopf in Xaeros Wegpunkt-Menü) |

Der Mod ist **optional**: Ohne ihn funktionieren Menü und Wegweiser ganz normal. Er braucht **Xaero's Minimap** (die World Map zeigt die Wegpunkte der Minimap mit an).

## Zwei Nummern: Version und Protokoll
- **Version** (`plugin-mc26.3-v0.1.0`, `mod-mc26.3-v0.1.0`): eigene Versionen und Releases ([RELEASES.md](RELEASES.md)).
- **Protokoll** (ganze Zahl): Format der Nachrichten.
  - Mod: `Protocol.VERSION` (`mod/.../Protocol.java`)
  - Plugin: `Compatibility.MIN_PROTOCOL` .. `MAX_PROTOCOL` (`plugin/.../Compatibility.java`)

Kompatibel ist, was ein gemeinsames Protokoll spricht - nicht, was dieselbe Version hat.

## Handshake
1. Der Mod schickt nach dem Betreten `mrwaypoints:hello` (Protokoll, Mod-Version, Minecraft-Version).
2. Das Plugin prüft (`Compatibility.check`): Protokoll zu klein → zu alt, zu groß → zu neu, Mod-Version unter `mod.minimum-version` → zu alt.
3. Antwort `mrwaypoints:welcome` (Status, Protokollbereich, Plugin-Version, Mindestversion, Download-Link), bei „zu alt / zu neu“ zusätzlich eine Chatzeile mit Link.
4. Bei **OK** schickt das Plugin `mrwaypoints:state` - danach nach jeder Änderung neu.

Die Formate von `hello` und `welcome` ändern sich **nie** (Felder nur hinten anhängen), damit jede Mod-Version jeder Plugin-Version sagen kann, ob sie passen.

## Nachrichten (Protokoll 1)
| Kanal | Richtung | Inhalt |
|---|---|---|
| `mrwaypoints:hello` | Mod → Plugin | VarInt Protokoll, String Mod-Version, String Minecraft-Version (eingefroren) |
| `mrwaypoints:welcome` | Plugin → Mod | VarInt Status (0 OK, 1 zu alt, 2 zu neu), VarInt min, VarInt max, String Plugin-Version, String Mindestversion, String Download-Link (eingefroren) |
| `mrwaypoints:state` | Plugin → Mod | VarInt n, n × (String id, String Name in der Sprache des Spielers, Int Farbe, Boolean an); VarInt m, m × (VarInt id, String Kategorie, String Name, String Dimension wie `minecraft:overworld`, Int x, Int y, Int z) |
| `mrwaypoints:toggle` | Mod → Plugin | String Kategorie (`*` = alle), Boolean an |
| `mrwaypoints:open` | Plugin → Mod | leer - öffnet den Kategorien-Bildschirm (Knopf in `/waypoints`) |

Strings sind VarInt-Länge + UTF-8, Zahlen Big-Endian - wie Minecrafts `FriendlyByteBuf` (Plugin: `util/Wire`, getestet in `WireTest`).

## Xaero
Xaero hat keine offizielle API. Der Mod nutzt Xaeros Ablage für Wegpunkte anderer Mods (`ThirdPartyWaypoints`, wie Xaero selbst für Waystones): pro sichtbarer Kategorie eine Herkunft `mrwaypoints:<kategorie>` im Ordner jeder Dimension. Diese Wegpunkte landen nicht in den Wegpunkt-Dateien des Spielers. Alles steckt in `XaeroBridge`; passt eine Xaero-Version nicht, schreibt der Mod einmal einen Fehler ins Log und zeigt nichts an - das Spiel läuft weiter. Gebaut und getestet gegen Xaero's Minimap **26.6.2** (MC 26.3).

## Bei Änderungen
- Neue Nachricht oder geändertes Format von `state`/`toggle`/`open`: `Protocol.VERSION` im Mod **und** `MAX_PROTOCOL` im Plugin erhöhen. Alte Mods weiter unterstützen, solange es geht (`MIN_PROTOCOL` erst anheben, wenn es nicht mehr anders geht).
- Neue Xaero-Version: `xaero_minimap_version` in `mod/gradle.properties` anheben, bauen, im Spiel prüfen.
