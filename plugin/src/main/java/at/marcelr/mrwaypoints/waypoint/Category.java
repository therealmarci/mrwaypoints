package at.marcelr.mrwaypoints.waypoint;

import org.bukkit.Material;

import java.util.Locale;
import java.util.Map;

/**
 * A category of server waypoints, set by the admin (categories.yml or MRadmin's web interface).
 *
 * @param id             short, stable, lowercase id - part of the permissions mrwaypoints.create.&lt;id&gt; and
 *                       mrwaypoints.manage.&lt;id&gt; and of the map layer mrwaypoints:&lt;id&gt;
 * @param names          name per language ("de", "en" ...)
 * @param color          colour as 0xRRGGBB (map, Xaero, menus)
 * @param icon           item in the menus
 * @param playersCreate  every player (mrwaypoints.player) may create waypoints here - otherwise only with
 *                       mrwaypoints.create.&lt;id&gt;
 * @param maxPerPlayer   waypoints one player may create here, 0 = no limit (the team with manage is never limited)
 * @param visible        shown by default in Xaero's map (players switch categories on and off with the mod)
 * @param onMap          shown on the map of the player page in MRadmin
 */
public record Category(String id, Map<String, String> names, int color, Material icon, boolean playersCreate,
                       int maxPerPlayer, boolean visible, boolean onMap) {

    /** Ids allowed for categories (they become part of permissions and file keys). */
    public static boolean validId(String id) {
        return id != null && id.matches("[a-z0-9_-]{1,32}");
    }

    /** The name in {@code lang}, else English, else any name, else the id. */
    public String name(String lang) {
        String name = names.get(lang.toLowerCase(Locale.ROOT));
        if (name == null || name.isBlank()) {
            name = names.get("en");
        }
        if (name == null || name.isBlank()) {
            name = names.values().stream().filter(n -> n != null && !n.isBlank()).findFirst().orElse(id);
        }
        return name;
    }

    /** "#RRGGBB" */
    public String hexColor() {
        return String.format("#%06X", color & 0xFFFFFF);
    }
}
