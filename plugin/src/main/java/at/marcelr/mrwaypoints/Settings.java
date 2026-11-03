package at.marcelr.mrwaypoints;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Locale;
import java.util.logging.Logger;

/** Values from config.yml, read once on load/reload. Categories are not in here - they are in categories.yml. */
public record Settings(
        GuideSettings guide,
        WaypointSettings waypoints,
        ModSettings mod,
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
        return new Settings(guide, waypoints, mod, update, gui);
    }
}
