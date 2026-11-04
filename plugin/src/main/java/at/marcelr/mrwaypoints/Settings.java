package at.marcelr.mrwaypoints;

import at.marcelr.mrwaypoints.waypoint.View;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/** Values from config.yml, read once on load/reload. Categories are not in here - they are in categories.yml. */
public record Settings(
        GuideSettings guide,
        WaypointSettings waypoints,
        ModSettings mod,
        ViewSettings view,
        UpdateSettings update,
        GuiSettings gui
) {

    /**
     * The guide in the action bar (/waypoints, click on a waypoint).
     *
     * @param updateTicks    how often the arrow and the distance are refreshed
     * @param arriveDistance within this many blocks the guide ends ("arrived")
     */
    public record GuideSettings(int updateTicks, int arriveDistance, boolean sound) {
    }

    /** @param maxNameLength longest name of a waypoint */
    public record WaypointSettings(int maxNameLength) {
    }

    /**
     * The MRwaypoints mod (Xaero's map).
     *
     * @param minimumVersion oldest mod version allowed ("" = any version that speaks a supported protocol)
     */
    public record ModSettings(String minimumVersion, String downloadUrl) {
    }

    /**
     * What players can pick in /waypoints -> settings (only with the mod) and what applies until they do.
     *
     * @param ranges   the ranges to choose from: chunks to every side of the player, 0 = unlimited
     * @param defaults for players who did not change anything
     */
    public record ViewSettings(List<Integer> ranges, View defaults) {
    }

    public record UpdateSettings(boolean enabled, String source, boolean includePrereleases,
                                 int intervalHours, boolean notifyOnJoin) {
    }

    public record GuiSettings(int refreshTicks, boolean sounds) {
    }

    public static Settings from(FileConfiguration c, Logger log) {
        GuideSettings guide = new GuideSettings(
                Math.clamp(c.getInt("guide.update-ticks", 5), 2, 40),
                Math.clamp(c.getInt("guide.arrive-distance", 5), 1, 64),
                c.getBoolean("guide.sound", true));
        WaypointSettings waypoints = new WaypointSettings(
                Math.clamp(c.getInt("waypoints.max-name-length", 32), 3, 64));

        String minimum = c.getString("mod.minimum-version", "").trim();
        if (!minimum.isEmpty() && !minimum.matches("\\d+(\\.\\d+){0,2}.*")) {
            log.warning("Invalid mod.minimum-version '" + minimum + "' (expected e.g. 0.2.0), ignoring it.");
            minimum = "";
        }
        ModSettings mod = new ModSettings(minimum,
                c.getString("mod.download-url", "https://mrtechnology.marcelr.at/download/mrwaypoints/").trim());

        List<Integer> ranges = new ArrayList<>();
        for (int range : c.getIntegerList("view.ranges")) {
            if (range >= 0 && !ranges.contains(range)) {
                ranges.add(range);
            }
        }
        if (ranges.isEmpty()) {
            ranges.add(0);
        }
        String display = c.getString("view.default-display", "everywhere").toLowerCase(Locale.ROOT);
        if (!display.equals("everywhere") && !display.equals("world-map")) {
            log.warning("Invalid view.default-display '" + display + "', using everywhere.");
            display = "everywhere";
        }
        ViewSettings view = new ViewSettings(List.copyOf(ranges), new View(Math.max(0, c.getInt("view.default-range", 0)),
                display.equals("world-map"), c.getBoolean("view.default-far-on-world-map", true)));

        String source = c.getString("update-checker.source", "github").toLowerCase(Locale.ROOT);
        if (!source.equals("modrinth") && !source.equals("github")) {
            log.warning("Invalid update-checker.source '" + source + "', using github.");
            source = "github";
        }
        UpdateSettings update = new UpdateSettings(
                c.getBoolean("update-checker.enabled", true),
                source,
                c.getBoolean("update-checker.include-prereleases", false),
                Math.max(1, c.getInt("update-checker.interval-hours", 6)),
                c.getBoolean("update-checker.notify-on-join", true));
        GuiSettings gui = new GuiSettings(
                Math.max(5, c.getInt("gui.refresh-ticks", 20)),
                c.getBoolean("gui.sounds", true));
        return new Settings(guide, waypoints, mod, view, update, gui);
    }
}
