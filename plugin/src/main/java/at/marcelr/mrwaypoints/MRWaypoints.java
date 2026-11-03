package at.marcelr.mrwaypoints;

import at.marcelr.mrwaypoints.command.MRWaypointsCommand;
import at.marcelr.mrwaypoints.command.WaypointsCommand;
import at.marcelr.mrwaypoints.gui.MenuListener;
import at.marcelr.mrwaypoints.waypoint.WaypointStore;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Objects;

/**
 * Main class of MRwaypoints: server waypoints in categories set by the admin - menu and guide in game for everyone,
 * the categories in Xaero's map with the MRwaypoints mod, on the web map and in the web interface through MRadmin.
 */
public final class MRWaypoints extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private Settings settings;
    private final Messages messages = new Messages(1, Map.of());
    private final WaypointPerms permissions = new WaypointPerms();
    private final WaypointActions actions = new WaypointActions(this);
    private WaypointStore store;
    private Guide guide;
    private UpdateChecker updateChecker;
    private MenuListener menuListener;

    @Override
    public void onEnable() {
        ConfigUpdater.update(this);
        loadConfiguration();

        store = new WaypointStore(this);
        store.load();
        guide = new Guide(this);
        guide.start();
        updateChecker = new UpdateChecker(this);
        updateChecker.start();
        menuListener = new MenuListener(this);
        menuListener.start();

        getServer().getPluginManager().registerEvents(guide, this);
        getServer().getPluginManager().registerEvents(menuListener, this);
        getServer().getPluginManager().registerEvents(this, this);
        register("waypoints", new WaypointsCommand(this));
        register("mrwaypoints", new MRWaypointsCommand(this));
        getLogger().info(store.categories().size() + " categories, " + store.all().size() + " waypoints loaded.");
    }

    @Override
    public void onDisable() {
        if (menuListener != null) {
            menuListener.stop();
            menuListener.closeAll();
        }
        if (guide != null) {
            guide.stopTask();
        }
        if (updateChecker != null) {
            updateChecker.stop();
        }
        if (store != null) {
            store.close();
        }
    }

    /** Reloads config.yml, the texts and categories.yml. */
    public void reload() {
        ConfigUpdater.update(this);
        loadConfiguration();
        store.loadCategories();
        guide.start();
        updateChecker.start();
        menuListener.start();
    }

    private void loadConfiguration() {
        config = ConfigUpdater.load(this);
        settings = Settings.from(config, getLogger());
        messages.load(this, config.getString("language", "auto"), config.getString("fallback-language", "en"),
                config.getString("server-language", "en"));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        // A little later, so the notice is not buried under join messages.
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (player.isOnline()) {
                updateChecker.notifyIfOutdated(player);
            }
        }, 60L);
    }

    /** config.yml is read by {@link ConfigUpdater#load}, which also tolerates the UTF-8 BOM. */
    @Override
    public FileConfiguration getConfig() {
        if (config == null) {
            config = ConfigUpdater.load(this);
        }
        return config;
    }

    @Override
    public void reloadConfig() {
        config = ConfigUpdater.load(this);
    }

    private void register(String name, TabExecutor executor) {
        PluginCommand command = Objects.requireNonNull(getCommand(name), name);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    public Settings settings() {
        return settings;
    }

    public Messages messages() {
        return messages;
    }

    public WaypointPerms permissions() {
        return permissions;
    }

    public WaypointActions actions() {
        return actions;
    }

    public WaypointStore store() {
        return store;
    }

    public Guide guide() {
        return guide;
    }

    public UpdateChecker updateChecker() {
        return updateChecker;
    }
}
