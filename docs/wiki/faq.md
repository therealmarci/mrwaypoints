# Häufige Fragen

### Ein Spieler kann keinen Wegpunkt anlegen.
In der Kategorie braucht er `mrwaypoints.create.<kategorie>` - oder die Kategorie hat `players-create: true` und er hat `mrwaypoints.player`. Ist das Limit (`max-per-player`) erreicht, wird der Knopf grau. Gilt die Kategorie nur in bestimmten Welten (`worlds`), kann er in anderen Welten dort nichts anlegen.

### Brauchen Spieler einen Mod?
Nein. Menü und Wegweiser funktionieren mit jedem Client. Der Mod bringt die Wegpunkte zusätzlich in Xaero's Karten.

### Mit Mod erscheinen keine Wegpunkte in Xaero.
Ist die Kategorie für dich eingeschaltet (`/waypoints` → Kategorien)? Ist Xaero's **Minimap** installiert (die World Map allein reicht nicht)? Im Client-Log steht „Connected to MRwaypoints …“ bzw. bei einer nicht unterstützten Xaero-Version eine Fehlermeldung.

### Kann ich Server-Wegpunkte in meine eigenen Xaero-Wegpunkte übernehmen?
Nein, mit Absicht: Server-Wegpunkte gibt es nur auf dem Server, damit es keine veralteten Kopien gibt. Xaero zeigt sie nur an.

### Ich habe einen Fehler gefunden oder einen Wunsch.
Gern über das **[Formular auf der Website](https://mrtechnology.marcelr.at/report/mrwaypoints/)** - am besten mit der Ausgabe von `/mrwaypoints version` und deiner Mod-Version.
