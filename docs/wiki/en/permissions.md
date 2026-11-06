# Permissions

The easiest way is to give the **groups** - they are built the same in all MRtechnology plugins, every one contains the one below ([more](https://mrtechnology.marcelr.at/wiki/allgemein/rechte/)). By default only OPs have them.

## Groups
Every group contains the one below, the team groups also contain `mrwaypoints.player` - best give `mrwaypoints.player` to the default group:

<!-- auto:permission-groups -->

```text
/lp group default permission set mrwaypoints.player true
/lp group supporter permission set mrwaypoints.groups.support true
/lp group staff permission set mrwaypoints.groups.staff true
/lp group admin permission set mrwaypoints.groups.admin true
```

## Single permissions
"Included in" names the smallest group that brings the permission; "–" means: in no group, only given on its own.

<!-- auto:permissions -->

Plus the permissions per category (you create the categories yourself, so they are not in the list above):

| Permission | What for |
|---|---|
| `mrwaypoints.create.<category>` | Create waypoints in this category (players may do it anyway in categories with `players-create: true`) |
| `mrwaypoints.manage.<category>` | Edit and delete all waypoints of this category, without the limit |

Every creator may always rename, move and delete their own waypoints.
