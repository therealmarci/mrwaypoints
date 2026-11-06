# Schnellstart

1. **[MRwaypoints herunterladen](https://mrtechnology.marcelr.at/download/mrwaypoints/)** (Zeile „Plugin (Server)“) und die JAR in den Ordner `plugins/` legen.
2. Den Server starten. Dabei entstehen `plugins/MRwaypoints/config.yml`, `categories.yml` mit vier Beispiel-Kategorien und die Sprachdateien in `plugins/MRwaypoints/lang/`.
3. **Rechte geben** - ohne sie dürfen nur OPs:
   ```text
   /lp group default permission set mrwaypoints.player true
   /lp group supporter permission set mrwaypoints.groups.support true
   /lp group staff permission set mrwaypoints.groups.staff true
   /lp group admin permission set mrwaypoints.groups.admin true
   ```
4. Die Kategorien anpassen - in der MRadmin-Weboberfläche oder in `plugins/MRwaypoints/categories.yml` und `/mrwaypoints reload` (siehe [Kategorien](kategorien.md)).
5. Optional deinen Spielern den Mod empfehlen: [In Xaero's Minimap](xaero.md).

## Update
Die alte JAR durch die neue ersetzen und den Server neu starten. Die Config wird automatisch angepasst: Deine Werte bleiben, neue Optionen kommen dazu, und die alte Datei wird als `config.v<N>.backup.yml` gesichert. Plugin und Mod haben eigene Versionen.
