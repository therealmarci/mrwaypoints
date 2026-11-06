# In Xaero's Minimap (mit Mod)

## Einrichten
1. **Fabric API**, **Xaero's Minimap** (und gern Xaero's World Map) und den **[MRwaypoints-Mod](https://mrtechnology.marcelr.at/download/mrwaypoints/)** (Zeile „Fabric-Mod (Client)“) in den Ordner `mods/` legen.
2. Auf den Server gehen - die Server-Wegpunkte erscheinen von selbst auf der Minimap, im Spiel und auf der World Map, in der Farbe ihrer Kategorie. Sie liegen getrennt von deinen eigenen Xaero-Wegpunkten.

## Kategorien ein- und ausblenden
Die Taste **`J`** (änderbar unter Optionen → Steuerung → MRwaypoints) und der Knopf **„Server-Wegpunkte …“** in Xaeros Wegpunkt-Menü öffnen `/waypoints`. Dort schaltest du unter **Kategorien** ein und aus, welche Kategorien in Xaero erscheinen (Klick pro Kategorie, „Alle an“ / „Alle aus“). Deine Auswahl speichert der Server - sie gilt auf jedem PC und auch auf der Spielerseite im Web.

## Tasten
Optionen → Steuerung → MRwaypoints, schneller über `/waypoints` → Einstellungen → **Tastenbelegung**. Nur `J` ist vorbelegt, die anderen legst du selbst fest, damit nichts mit Xaero oder anderen Mods kollidiert.

| Taste | Was passiert |
|---|---|
| Wegpunkt-Menü (`J`) | `/waypoints` |
| Wegpunkt hier erstellen | der Dialog mit Name und Kategorie - wie im Menü |
| Server-Wegpunkte ein/aus | alle Server-Wegpunkte in Xaero aus- und wieder einblenden (nur auf diesem PC, deine Auswahl pro Kategorie bleibt) |

## Sichtweite und Anzeige
In `/waypoints` → **Einstellungen** legst du fest, wie du die Server-Wegpunkte siehst. Der Server speichert das für dich.

| Einstellung | Was sie tut |
|---|---|
| **Sichtweite** | wie weit um dich die Wegpunkte im Spiel und auf der Minimap erscheinen: 10, 20, 50 oder 100 Chunks in jede Richtung oder unbegrenzt (Linksklick weiter, Rechtsklick näher; die Stufen legt der Server fest). Xaeros eigene Grenze für Wegpunkte im Spiel gilt zusätzlich. |
| **Anzeige** | „Welt, Minimap und Weltkarte“ oder „nur Weltkarte“ |
| **Entfernte auf der Weltkarte** | Wegpunkte außerhalb der Sichtweite trotzdem auf der World Map zeigen (nur bei begrenzter Sichtweite) |

> [!NOTE]
> Xaero zeigt einen Wegpunkt im Spiel und auf der Minimap immer gemeinsam - „nur Minimap“ oder „nur im Spiel“ gibt es pro Wegpunkt nicht. Dafür hat Xaero eigene Einstellungen, die dann für alle Wegpunkte gelten.

> [!TIP]
> Keine Wegpunkte in Xaero? Prüf, ob die Kategorie für dich eingeschaltet ist (`/waypoints` → Kategorien) und ob Xaero's **Minimap** installiert ist - die World Map allein reicht nicht.
