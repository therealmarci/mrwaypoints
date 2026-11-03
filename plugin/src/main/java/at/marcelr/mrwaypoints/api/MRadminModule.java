package at.marcelr.mrwaypoints.api;

import at.marcelr.mradmin.api.AdminModule;
import at.marcelr.mradmin.api.MRadminService;
import at.marcelr.mradmin.api.SelfBlock;
import at.marcelr.mradmin.api.SelfMapLayer;
import at.marcelr.mradmin.api.SelfViewer;
import at.marcelr.mradmin.api.WebModule;
import at.marcelr.mradmin.api.WebUser;
import at.marcelr.mrwaypoints.MRWaypoints;
import at.marcelr.mrwaypoints.Messages;
import at.marcelr.mrwaypoints.WaypointActions;
import at.marcelr.mrwaypoints.WaypointPerms;
import at.marcelr.mrwaypoints.gui.MainMenu;
import at.marcelr.mrwaypoints.waypoint.Category;
import at.marcelr.mrwaypoints.waypoint.Waypoint;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * MRwaypoints as a module of MRadmin (module API, see MRadmin's docs/MODULE-API.md): a dashboard button that opens
 * /waypoints, the waypoints page of the team and one layer per category (with {@code map: true}) on the map of the
 * players' own page. <b>Only loaded while MRadmin runs</b> - everything else goes
 * through {@link MRadminLink}.
 */
final class MRadminModule implements AdminModule, WebModule {

    static final String ID = "mrwaypoints";

    private final MRWaypoints plugin;

    private MRadminModule(MRWaypoints plugin) {
        this.plugin = plugin;
    }

    static boolean register(MRWaypoints plugin) {
        MRadminService service = MRadminService.get();
        if (service == null) {
            return false;
        }
        service.register(new MRadminModule(plugin));
        return true;
    }

    static void changed() {
        MRadminService service = MRadminService.get();
        if (service == null) {
            return;
        }
        service.webChanged(ID, "waypoints", "");
    }

    // -------------------------------------------------------------- module

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Plugin plugin() {
        return plugin;
    }

    @Override
    public String viewPermission() {
        return WaypointPerms.VIEW;
    }

    @Override
    public Material icon() {
        return Material.RECOVERY_COMPASS;
    }

    @Override
    public List<Component> summary(Player viewer) {
        return plugin.messages().as(viewer, () -> plugin.messages().lore("admin-summary",
                tag("waypoints", plugin.store().all().size()), tag("categories", plugin.store().categories().size())));
    }

    @Override
    public boolean openMenu(Player viewer, Runnable back) {
        plugin.messages().run(viewer, () -> new MainMenu(plugin, viewer).open());
        return true;
    }

    @Override
    public @Nullable WebModule web() {
        return this;
    }

    // ---------------------------------------------------------- web module

    /** may.mrwaypointsManage: edit all waypoints; may.mrwaypointsCategories: manage the categories. */
    @Override
    public Map<String, String> permissions() {
        return Map.of("manage", WaypointPerms.MANAGE + "*", "categories", WaypointPerms.CATEGORIES);
    }

    @Override
    public Map<String, String> actions() {
        return Map.of("rename", "manage", "delete", "manage", "recategorize", "manage",
                "categorySave", "categories", "categoryDelete", "categories", "categoryOrder", "categories");
    }

    /** "overview": all categories (with counts) and all waypoints - the waypoints page of the team. */
    @Override
    public @Nullable Object data(String request, Map<String, String> args) {
        if (!request.equals("overview")) {
            return null;
        }
        List<Map<String, Object>> categories = new ArrayList<>();
        for (Category c : plugin.store().categories()) {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", c.id());
            map.put("names", c.names());
            map.put("color", c.hexColor());
            map.put("icon", c.icon().getKey().getKey());
            map.put("playersCreate", c.playersCreate());
            map.put("maxPerPlayer", c.maxPerPlayer());
            map.put("visible", c.visible());
            map.put("map", c.onMap());
            map.put("count", plugin.store().inCategory(c.id()).size());
            categories.add(map);
        }
        List<Map<String, Object>> waypoints = new ArrayList<>();
        for (Waypoint w : plugin.store().all()) {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", w.id());
            map.put("category", w.category());
            map.put("name", w.name());
            map.put("world", w.worldName());
            map.put("x", w.x());
            map.put("y", w.y());
            map.put("z", w.z());
            map.put("creator", w.creator().toString());
            map.put("creatorName", w.creatorName());
            map.put("created", w.created());
            waypoints.add(map);
        }
        return Map.of("categories", categories, "waypoints", waypoints,
                "maxNameLength", plugin.settings().waypoints().maxNameLength());
    }

    @Override
    public @Nullable String action(String action, Map<String, Object> args, WebUser user) {
        return switch (action) {
            case "rename", "delete", "recategorize" -> waypointAction(action, args);
            case "categorySave" -> saveCategory(args);
            case "categoryDelete" -> {
                Category c = plugin.store().category(text(args, "id"));
                if (c == null) {
                    yield "not-found";
                }
                plugin.store().removeCategory(c.id());
                yield null;
            }
            case "categoryOrder" -> {
                plugin.store().reorder(List.of(text(args, "ids").split(",")));
                yield null;
            }
            default -> "unknown-action";
        };
    }

    private @Nullable String waypointAction(String action, Map<String, Object> args) {
        Waypoint w;
        try {
            w = plugin.store().byId(Integer.parseInt(text(args, "id")));
        } catch (NumberFormatException e) {
            w = null;
        }
        if (w == null) {
            return "not-found";
        }
        switch (action) {
            case "rename" -> {
                Category category = plugin.store().category(w.category());
                String name = WaypointActions.clean(text(args, "name"));
                String problem = category == null ? "not-found" : plugin.actions().nameProblem(category, name, w);
                if (problem != null) {
                    return problem.equals("not-found") ? problem : "wp-" + problem;
                }
                plugin.store().update(w.withName(name));
            }
            case "recategorize" -> {
                Category target = plugin.store().category(text(args, "category"));
                if (target == null) {
                    return "wp-invalid-category";
                }
                if (plugin.store().byName(target.id(), w.name()) != null) {
                    return "wp-name-taken";
                }
                plugin.store().update(w.inCategory(target.id()));
            }
            default -> plugin.store().remove(w.id());
        }
        return null;
    }

    /** New or changed category; "isNew" decides whether the id may already exist. */
    private @Nullable String saveCategory(Map<String, Object> args) {
        String id = text(args, "id").toLowerCase(java.util.Locale.ROOT);
        boolean isNew = Boolean.parseBoolean(text(args, "isNew"));
        if (!Category.validId(id)) {
            return "wp-invalid-id";
        }
        if (isNew == (plugin.store().category(id) != null)) {
            return isNew ? "wp-id-taken" : "not-found";
        }
        String color = text(args, "color").replace("#", "");
        if (!color.matches("[0-9a-fA-F]{6}")) {
            return "wp-invalid-color";
        }
        Material icon = Material.matchMaterial(text(args, "icon"));
        if (icon == null || !icon.isItem() || icon.isAir()) {
            return "wp-invalid-icon";
        }
        int max;
        try {
            max = Integer.parseInt(text(args, "maxPerPlayer"));
        } catch (NumberFormatException e) {
            return "wp-invalid-limit";
        }
        if (max < 0 || max > 1000) {
            return "wp-invalid-limit";
        }
        Map<String, String> names = new java.util.LinkedHashMap<>();
        for (String lang : List.of("de", "en")) {
            String name = text(args, "name" + Character.toUpperCase(lang.charAt(0)) + lang.substring(1)).strip();
            if (name.length() > 48) {
                return "wp-name-too-long";
            }
            if (!name.isEmpty()) {
                names.put(lang, name);
            }
        }
        if (names.isEmpty()) {
            return "wp-name-empty";
        }
        plugin.store().putCategory(new Category(id, names, Integer.parseInt(color, 16), icon,
                Boolean.parseBoolean(text(args, "playersCreate")), max, Boolean.parseBoolean(text(args, "visible")),
                Boolean.parseBoolean(text(args, "map"))));
        return null;
    }

    private static String text(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    // ------------------------------------------------------ player's page

    @Override
    public Set<String> selfPermissions() {
        return Set.of(WaypointPerms.USE);
    }

    /**
     * The map of the player's page (MRadmin's tab "Map"): one layer per category with all its waypoints. On their own
     * ones rename and delete, on every one the guide (while the player is online and in that world); a right-click on a
     * free spot creates one there (categories the player may use); the switch "in Xaero" per category.
     */
    @Override
    public List<SelfMapLayer> selfMap(SelfViewer viewer) {
        if (!viewer.has(WaypointPerms.USE)) {
            return List.of();
        }
        return plugin.messages().in(viewer.lang(), () -> {
            Messages m = plugin.messages();
            String lang = viewer.lang();
            Player online = Bukkit.getPlayer(viewer.player());
            int max = plugin.settings().waypoints().maxNameLength();
            List<SelfMapLayer> layers = new ArrayList<>();
            for (Category category : plugin.store().categories()) {
                if (!category.onMap()) {
                    continue;
                }
                List<SelfMapLayer.Marker> markers = new ArrayList<>();
                for (Waypoint w : plugin.store().inCategory(category.id())) {
                    World world = w.bukkitWorld();
                    if (world == null) {
                        continue;
                    }
                    boolean own = w.creator().equals(viewer.player());
                    List<SelfMapLayer.Action> actions = new ArrayList<>();
                    if (online != null && online.getWorld().equals(world)) {
                        actions.add(new SelfMapLayer.Action("guide", m.plain("map-guide"), "", null, null));
                    }
                    if (own) {
                        actions.add(new SelfMapLayer.Action("rename", m.plain("map-rename"), "", null,
                                new SelfBlock.Input(m.plain("map-rename-label"), w.name(), max)));
                        actions.add(new SelfMapLayer.Action("delete", m.plain("map-delete"), "bad",
                                m.plain("map-delete-confirm", "name", w.name()), null));
                    }
                    markers.add(new SelfMapLayer.Marker(String.valueOf(w.id()), world.getKey().asString(), w.x(), w.z(),
                            0, 0, w.name(), "", List.of(category.name(lang), w.x() + " " + w.y() + " " + w.z(),
                            m.plain(own ? "map-own" : "map-by", "player", w.creatorName())), actions));
                }
                List<SelfMapLayer.Action> here = mayCreate(viewer, category, online) == null
                        ? List.of(new SelfMapLayer.Action("create", m.plain("map-create", "category", category.name(lang)),
                        "good", null, new SelfBlock.Input(m.plain("map-create-label"), "", max)))
                        : List.of();
                boolean shown = plugin.store().visible(viewer.player(), category);
                layers.add(new SelfMapLayer(category.id(), category.name(lang), category.hexColor(), markers, here,
                        new SelfMapLayer.Toggle(m.plain(shown ? "map-xaero-on" : "map-xaero-off"), shown, "xaero")));
            }
            return layers;
        });
    }

    /**
     * Why the player may not create in the category from the web (language key), or null. Online: the same check as in
     * game. Offline their permissions are unknown - only categories every player may use, within the limit.
     */
    private @Nullable String mayCreate(SelfViewer viewer, Category category, @Nullable Player online) {
        if (online != null) {
            return plugin.actions().createBlocked(online, category);
        }
        if (!category.playersCreate()) {
            return "create-no-permission";
        }
        return category.maxPerPlayer() > 0 && plugin.store().countBy(viewer.player(), category.id()) >= category.maxPerPlayer()
                ? "create-limit" : null;
    }

    @Override
    public @Nullable String selfMapAction(SelfViewer viewer, String layer, String action, Map<String, String> args) {
        return plugin.messages().in(viewer.lang(), () -> {
            Messages m = plugin.messages();
            Category category = plugin.store().category(layer);
            if (!viewer.has(WaypointPerms.USE) || category == null) {
                return m.plain("self-not-found");
            }
            Player online = Bukkit.getPlayer(viewer.player());
            switch (action) {
                case "xaero" -> {
                    plugin.store().setVisible(viewer.player(), category, Boolean.parseBoolean(args.get("on")));
                    return null;
                }
                case "create" -> {
                    return create(viewer, category, online, args);
                }
                default -> {
                }
            }
            Waypoint w;
            try {
                w = plugin.store().byId(Integer.parseInt(args.getOrDefault("marker", "")));
            } catch (NumberFormatException e) {
                w = null;
            }
            if (w == null || !w.category().equals(category.id())) {
                return m.plain("self-not-found");
            }
            if (action.equals("guide")) {
                if (online == null || !online.getWorld().getUID().equals(w.world())) {
                    return m.plain("map-guide-offline");
                }
                Waypoint target = w;
                plugin.messages().run(online, () -> plugin.guide().guide(online, target));
                return null;
            }
            if (!w.creator().equals(viewer.player())) {
                return m.plain("self-not-yours");
            }
            switch (action) {
                case "rename" -> {
                    String name = WaypointActions.clean(args.get("value"));
                    String problem = plugin.actions().nameProblem(category, name, w);
                    if (problem != null) {
                        return m.plain("self-" + problem, "max", plugin.settings().waypoints().maxNameLength(), "name", name);
                    }
                    plugin.store().update(w.withName(name));
                    return null;
                }
                case "delete" -> {
                    plugin.store().remove(w.id());
                    return null;
                }
                default -> {
                    return "?";
                }
            }
        });
    }

    /** A waypoint at the spot of the map: the ground there (highest block), the same name rules and limits as in game. */
    private @Nullable String create(SelfViewer viewer, Category category, @Nullable Player online, Map<String, String> args) {
        Messages m = plugin.messages();
        String blocked = mayCreate(viewer, category, online);
        if (blocked != null) {
            return m.plain("self-" + blocked, "max", category.maxPerPlayer());
        }
        NamespacedKey key = NamespacedKey.fromString(args.getOrDefault("world", ""));
        World world = key == null ? null : Bukkit.getWorld(key);
        int x;
        int z;
        try {
            x = Integer.parseInt(args.getOrDefault("x", ""));
            z = Integer.parseInt(args.getOrDefault("z", ""));
        } catch (NumberFormatException e) {
            return m.plain("self-not-found");
        }
        if (world == null || !world.getWorldBorder().isInside(new org.bukkit.Location(world, x + 0.5, 64, z + 0.5))) {
            return m.plain("map-outside");
        }
        String name = WaypointActions.clean(args.get("value"));
        String problem = plugin.actions().nameProblem(category, name, null);
        if (problem != null) {
            return m.plain("self-" + problem, "max", plugin.settings().waypoints().maxNameLength(), "name", name);
        }
        int y = world.getHighestBlockYAt(x, z) + 1;
        plugin.store().add(new Waypoint(0, category.id(), name, world.getUID(), world.getName(), x, y, z,
                viewer.player(), viewer.name(), System.currentTimeMillis()));
        return null;
    }
}
