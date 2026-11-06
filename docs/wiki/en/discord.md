# Discord log

MRwaypoints reports to a Discord channel when waypoints are created, renamed, moved to another category, relocated or deleted (also several at once in the web), when categories are created, changed, deleted or reordered, when an admin teleports to a waypoint and when someone reloads - no matter whether in game, in the MRadmin web interface or on the map of the player page.

## Setup
1. In Discord: **Edit channel → Integrations → Webhooks → New webhook** and **copy the webhook URL**.
2. In the config set `discord.enabled: true` and enter the URL at `webhook-url`.
3. `/mrwaypoints reload`, then check with **`/mrwaypoints discord test`**.

Under `discord.events` you set per event whether it is sent and in which color (e.g. `waypoint-created`, `waypoint-deleted`, `category-changed`, `teleport`). The texts are in the language file under `discord`, categories are named there as in `general.server-language`.

> [!TIP]
> **No own webhook?** With MRadmin it also works through its Discord log: `discord.enabled: true`, leave `webhook-url` empty and set `discord.use-mradmin-webhook: true`.
>
> With MRadmin every event is also in the **log** of the web interface (up to 14 days) - even when Discord is off.
