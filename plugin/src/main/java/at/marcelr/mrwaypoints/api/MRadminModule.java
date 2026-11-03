package at.marcelr.mrwaypoints.api;

import at.marcelr.mradmin.api.AdminModule;
import at.marcelr.mradmin.api.MRadminService;
import at.marcelr.mradmin.api.MapLayer;
import at.marcelr.mradmin.api.MapMarker;
import at.marcelr.mradmin.api.SelfArea;
import at.marcelr.mradmin.api.SelfBlock;
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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * MRwaypoints as a module of MRadmin (module API, see MRadmin's docs/MODULE-API.md): the categories as layers on the
 * web map (those with {@code map: true}), a dashboard button that opens /waypoints and the area "Waypoints" on the
 * players' own page. <b>Only loaded while MRadmin runs</b> - everything else goes through {@link MRadminLink}.
 */
final class MRadminModule implements AdminModule, WebModule {

    static final String ID = "mrwaypoints";
    /** The web map since API 7, the building blocks of the player page (Cards, Group) since API 5. */
    private static final int MAP_API = 7;
    private static final int SELF_API = 5;
    private static final int ICON_SIZE = 16;

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
        if (service.apiVersion() < MAP_API) {
            plugin.getLogger().info("MRadmin is older than module API " + MAP_API + " - no waypoints on its web map.");
        }
        return true;
    }

    static void changed() {
        MRadminService service = MRadminService.get();
        if (service == null) {
            return;
        }
        service.webChanged(ID, "waypoints", "");
        if (service.apiVersion() >= MAP_API) {
            service.mapChanged(ID);
        }
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

    // ------------------------------------------------------------ web map

    /** One layer per category with map: true, a pin in the category's colour per waypoint. */
    @Override
    public List<MapLayer> mapLayers() {
        return plugin.messages().server(() -> {
            Messages m = plugin.messages();
            String lang = m.serverLanguage();
            String template = m.optional("map-tooltip");
            List<MapLayer> layers = new ArrayList<>();
            int priority = 10;
            for (Category category : plugin.store().categories()) {
                if (!category.onMap()) {
                    continue;
                }
                List<MapMarker> markers = new ArrayList<>();
                for (Waypoint w : plugin.store().inCategory(category.id())) {
                    String click = template.replace("{name}", html(w.name()))
                            .replace("{category}", html(category.name(lang)))
                            .replace("{x}", String.valueOf(w.x())).replace("{y}", String.valueOf(w.y()))
                            .replace("{z}", String.valueOf(w.z())).replace("{player}", html(w.creatorName()));
                    markers.add(new MapMarker.Icon("waypoint_" + w.id(), w.world(), w.x() + 0.5, w.z() + 0.5,
                            iconKey(category), ICON_SIZE, html(w.name()), click));
                }
                layers.add(new MapLayer(category.id(), category.name(lang), false, priority++, markers));
            }
            return layers;
        });
    }

    /** A pin per category; the key carries the colour, so a changed colour is a new picture for MRadmin. */
    @Override
    public Map<String, BufferedImage> mapIcons() {
        Map<String, BufferedImage> icons = new HashMap<>();
        for (Category category : plugin.store().categories()) {
            icons.put(iconKey(category), pin(category.color()));
        }
        return icons;
    }

    private static String iconKey(Category category) {
        return category.id() + "_" + String.format("%06x", category.color() & 0xFFFFFF);
    }

    /** A map pin: round head in the colour with a light dot, tip pointing to the spot. Drawn in code, no image file. */
    static BufferedImage pin(int rgb) {
        BufferedImage image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Path2D.Double shape = new Path2D.Double();
        shape.append(new java.awt.geom.Ellipse2D.Double(2.5, 0.5, 11, 11), false);
        shape.moveTo(4, 9);
        shape.lineTo(8, 15.5);
        shape.lineTo(12, 9);
        shape.closePath();
        g.setColor(new Color(rgb));
        g.fill(shape);
        g.setColor(new Color(0, 0, 0, 170));
        g.setStroke(new BasicStroke(1f));
        g.draw(shape);
        g.setColor(new Color(255, 255, 255, 230));
        g.fill(new java.awt.geom.Ellipse2D.Double(5.5, 3.5, 5, 5));
        g.dispose();
        return image;
    }

    private static String html(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
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

    @Override
    public List<SelfArea> selfAreas(SelfViewer viewer) {
        MRadminService service = MRadminService.get();
        if (!viewer.has(WaypointPerms.USE) || service == null || service.apiVersion() < SELF_API) {
            return List.of();
        }
        return plugin.messages().in(viewer.lang(), () -> List.of(area(viewer, service)));
    }

    private SelfArea area(SelfViewer viewer, MRadminService service) {
        Messages m = plugin.messages();
        String lang = viewer.lang();
        List<Waypoint> own = plugin.store().all().stream().filter(w -> w.creator().equals(viewer.player()))
                .sorted(java.util.Comparator.comparing(Waypoint::name, String.CASE_INSENSITIVE_ORDER)).toList();
        SelfBlock.Stats stats = new SelfBlock.Stats(List.of(
                new SelfBlock.Stat(m.plain("self-total"), String.valueOf(plugin.store().all().size()), ""),
                new SelfBlock.Stat(m.plain("self-own"), String.valueOf(own.size()), ""),
                new SelfBlock.Stat(m.plain("self-categories"), String.valueOf(plugin.store().categories().size()), "")));

        List<SelfBlock> blocks = new ArrayList<>();
        blocks.add(stats);
        blocks.add(new SelfBlock.Text(m.plain("self-hint"), "info"));
        List<SelfBlock.Card> cards = new ArrayList<>();
        for (Waypoint w : own) {
            cards.add(card(w, lang, service));
        }
        blocks.add(new SelfBlock.Cards(m.plain("self-own-title"), cards, m.plain("self-own-empty")));
        // Which categories the mod shows in Xaero - the same switches as in game (key J).
        List<SelfBlock.Button> toggles = new ArrayList<>();
        for (Category category : plugin.store().categories()) {
            boolean shown = plugin.store().visible(viewer.player(), category);
            toggles.add(new SelfBlock.Button(m.plain(shown ? "self-xaero-on" : "self-xaero-off", "name", category.name(lang)),
                    "xaero", Map.of("category", category.id(), "visible", String.valueOf(!shown)), shown ? "good" : "",
                    null, null));
        }
        if (!toggles.isEmpty()) {
            blocks.add(new SelfBlock.Group(m.plain("self-xaero-title"), m.plain("self-xaero-text"),
                    List.of(new SelfBlock.Buttons(toggles))));
        }
        for (Category category : plugin.store().categories()) {
            List<SelfBlock.Row> rows = new ArrayList<>();
            for (Waypoint w : plugin.store().inCategory(category.id())) {
                rows.add(new SelfBlock.Row(List.of(w.name(), place(w), w.creatorName()),
                        w.creator().equals(viewer.player()) ? "info" : "", List.of()));
            }
            blocks.add(new SelfBlock.Table(category.name(lang), List.of(m.plain("self-col-name"), m.plain("self-col-place"),
                    m.plain("self-col-creator")), rows, m.plain("self-category-empty")));
        }
        return new SelfArea("waypoints", m.plain("self-title"), "📍", List.of(stats), blocks);
    }

    private SelfBlock.Card card(Waypoint w, String lang, MRadminService service) {
        Messages m = plugin.messages();
        Category category = plugin.store().category(w.category());
        String categoryName = category == null ? w.category() : category.name(lang);
        Map<String, String> args = Map.of("id", String.valueOf(w.id()));
        List<SelfBlock.Button> buttons = List.of(
                new SelfBlock.Button(m.plain("self-rename"), "rename", args, "", null,
                        new SelfBlock.Input(m.plain("self-rename-label"), w.name(), plugin.settings().waypoints().maxNameLength())),
                new SelfBlock.Button(m.plain("self-delete"), "delete", args, "bad",
                        m.plain("self-delete-confirm", "name", w.name()), null));
        List<SelfBlock> details = new ArrayList<>();
        World world = w.bukkitWorld();
        String url = world == null || service.apiVersion() < MAP_API || category == null || !category.onMap()
                ? null : service.mapUrl(world, w.x(), w.z());
        if (url != null) {
            details.add(new SelfBlock.Embed(m.plain("self-map"), url, 320, m.plain("self-map-open")));
        }
        return new SelfBlock.Card("waypoint-" + w.id(), w.name(), categoryName + " · " + place(w), "", "",
                List.of(), List.of(), null, buttons, details);
    }

    private static String place(Waypoint w) {
        return w.worldName() + " " + w.x() + " " + w.y() + " " + w.z();
    }

    /** Only the player's own waypoints - the same name rules as in game. */
    @Override
    public @Nullable String selfAction(SelfViewer viewer, String area, String action, Map<String, String> args) {
        return plugin.messages().in(viewer.lang(), () -> {
            Messages m = plugin.messages();
            if (!viewer.has(WaypointPerms.USE)) {
                return m.plain("self-no-permission");
            }
            if (action.equals("xaero")) {
                Category category = plugin.store().category(args.getOrDefault("category", ""));
                if (category == null) {
                    return m.plain("self-not-found");
                }
                plugin.store().setVisible(viewer.player(), category, Boolean.parseBoolean(args.get("visible")));
                return null;
            }
            Waypoint w;
            try {
                w = plugin.store().byId(Integer.parseInt(args.getOrDefault("id", "")));
            } catch (NumberFormatException e) {
                w = null;
            }
            if (w == null) {
                return m.plain("self-not-found");
            }
            if (!w.creator().equals(viewer.player())) {
                return m.plain("self-not-yours");
            }
            switch (action) {
                case "rename" -> {
                    Category category = plugin.store().category(w.category());
                    String name = WaypointActions.clean(args.get("value"));
                    String problem = category == null ? "self-not-found"
                            : plugin.actions().nameProblem(category, name, w);
                    if (problem != null) {
                        return m.plain(problem.startsWith("self-") ? problem : "self-" + problem,
                                "max", plugin.settings().waypoints().maxNameLength(), "name", name);
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
}
