# Discord-Log

MRwaypoints meldet in einen Discord-Kanal, wenn Wegpunkte angelegt, umbenannt, in eine andere Kategorie verschoben, versetzt oder gelöscht werden (auch mehrere auf einmal im Web), wenn Kategorien angelegt, geändert, gelöscht oder umsortiert werden, wenn sich ein Admin zu einem Wegpunkt teleportiert und wenn jemand neu lädt - egal ob im Spiel, in der MRadmin-Weboberfläche oder auf der Karte der Spielerseite.

## Einrichten
1. In Discord: **Kanal bearbeiten → Integrationen → Webhooks → Neuer Webhook** und die **Webhook-URL kopieren**.
2. In der Config `discord.enabled: true` setzen und die URL bei `webhook-url` eintragen.
3. `/mrwaypoints reload`, dann mit **`/mrwaypoints discord test`** prüfen.

Unter `discord.events` legst du pro Ereignis fest, ob es gesendet wird und in welcher Farbe (z.B. `waypoint-created`, `waypoint-deleted`, `category-changed`, `teleport`). Die Texte stehen in der Sprachdatei unter `discord`, Kategorien heißen dort wie in `general.server-language`.

> [!TIP]
> **Kein eigener Webhook?** Mit MRadmin geht es auch über dessen Discord-Log: `discord.enabled: true`, `webhook-url` leer lassen und `discord.use-mradmin-webhook: true` setzen.
>
> Mit MRadmin steht jedes Ereignis außerdem im **Protokoll** der Weboberfläche (bis zu 14 Tage) - auch wenn Discord aus ist.
