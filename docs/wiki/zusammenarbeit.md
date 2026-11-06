# Zusammenarbeit mit anderen Plugins

MRwaypoints läuft **allein**. Zusammen mit anderen MRtechnology-Plugins kann es mehr ([wie das funktioniert](https://mrtechnology.marcelr.at/wiki/allgemein/zusammenarbeit/)):

| Funktion | Braucht |
|---|---|
| Seite **Karte** fürs Team: Kategorien anlegen, ändern, sortieren, löschen (`mrwaypoints.admin.categories`), alle Wegpunkte umbenennen, in eine andere Kategorie verschieben, löschen - auch mehrere auf einmal (`mrwaypoints.manage.*`) | MRadmin |
| Tab **Karte** auf der Spielerseite (`/web`): eine Ebene pro Kategorie mit `map: true`; Rechtsklick auf eine freie Stelle: „Wegpunkt erstellen“ mit Name und Kategorie (Höhe = Boden an der Stelle), auf eigene „Wegpunkt bearbeiten“ (Name, Kategorie) und „Wegpunkt löschen“, auf jeden den Wegweiser im Spiel starten; „Ingame: an/aus“ pro Kategorie | MRadmin |
| MRwaypoints im Browser einstellen (Plugins → MRwaypoints → Konfiguration) | MRadmin |
| Knopf im MRadmin-Dashboard (öffnet `/waypoints`) | MRadmin |
| Ereignisse im **Protokoll** der MRadmin-Weboberfläche, Discord-Log über MRadmins Webhook (`discord.use-mradmin-webhook`) | MRadmin |
| Wegpunkte in Xaero's Minimap und World Map, Kategorien ein- und ausblenden | MRwaypoints-Mod + Xaero's Minimap |
