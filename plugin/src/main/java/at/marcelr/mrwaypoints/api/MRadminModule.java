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

    @Override
    public Map<String, String> permissions() {
        return Map.of();
    }

    @Override
    public @Nullable Object data(String request, Map<String, String> args) {
        return null;
    }

    @Override
    public Map<String, String> actions() {
        return Map.of();
    }

    @Override
    public @Nullable String action(String action, Map<String, Object> args, WebUser user) {
        return "unknown-action";
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
