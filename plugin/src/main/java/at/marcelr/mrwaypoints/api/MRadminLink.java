package at.marcelr.mrwaypoints.api;

import at.marcelr.mrwaypoints.MRWaypoints;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * The optional connection to MRadmin. MRwaypoints works the same without it; while MRadmin runs, MRwaypoints is one of
 * its modules ({@link MRadminModule}): a button on the dashboard, the team page and a layer
 * per category on the map of the players' own page. This class uses no MRadmin types, so it is safe to load without MRadmin - only
 * {@link MRadminModule} touches them, and only after MRadmin is known to be enabled.
 */
public final class MRadminLink implements Listener {

    private static final String MRADMIN = "MRadmin";

    private final MRWaypoints plugin;
    private boolean connected;

    public MRadminLink(MRWaypoints plugin) {
        this.plugin = plugin;
    }

    /** Connects if MRadmin runs (on enable; MRadmin enables first, MRwaypoints lists it under softdepend). */
    public void connect() {
        connected = false;
        if (!Bukkit.getPluginManager().isPluginEnabled(MRADMIN)) {
            return;
        }
        try {
            connected = MRadminModule.register(plugin);
            if (!connected) {
                plugin.getLogger().info("MRadmin found, but it is too old for the module API - update MRadmin to connect.");
            }
        } catch (LinkageError | RuntimeException e) {
            plugin.getLogger().warning("Could not connect to MRadmin (versions do not fit?): " + e);
        }
    }

    /** Waypoints or categories changed - open pages in MRadmin reload. */
    public void changed() {
        if (connected) {
            try {
                MRadminModule.changed();
            } catch (LinkageError | RuntimeException e) {
                plugin.getLogger().warning("Could not update MRadmin: " + e);
            }
        }
    }

    /** An event of the Discord log also into MRadmin's log (web interface, API 13). */
    public void log(String event, String text, int color) {
        if (connected) {
            try {
                MRadminModule.log(event, text, color);
            } catch (LinkageError | RuntimeException e) {
                plugin.getLogger().warning("Could not write into MRadmin's log: " + e);
            }
        }
    }

    /**
     * Whether this team member wants messages of this kind in the game - their choice in MRadmin (account ->
     * notifications) and not muted with /mradmin mute. Always true without MRadmin (API 15).
     */
    public boolean teamInGame(org.bukkit.entity.Player player, String kind) {
        if (connected) {
            try {
                return MRadminModule.teamInGame(player.getUniqueId(), kind);
            } catch (LinkageError | RuntimeException e) {
                return true;
            }
        }
        return true;
    }

    /** Into MRadmin's Discord admin log. @return false without MRadmin */
    public boolean adminLog(String text) {
        if (connected) {
            try {
                MRadminModule.adminLog(text);
                return true;
            } catch (LinkageError | RuntimeException e) {
                plugin.getLogger().warning("Could not write into MRadmin's Discord log: " + e);
            }
        }
        return false;
    }

    /** MRadmin enabled later (e.g. with a plugin manager): connect now. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals(MRADMIN)) {
            connect();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDisable(PluginDisableEvent event) {
        if (event.getPlugin().getName().equals(MRADMIN)) {
            connected = false;
        }
    }
}
