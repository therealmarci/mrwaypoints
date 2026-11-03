package at.marcelr.mrwaypoints.gui;

import at.marcelr.mrwaypoints.MRWaypoints;
import at.marcelr.mrwaypoints.Messages;
import at.marcelr.mrwaypoints.WaypointPerms;
import at.marcelr.mrwaypoints.waypoint.Category;
import at.marcelr.mrwaypoints.waypoint.Waypoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * "My waypoints" or "all waypoints" of the dimension the viewer is in, 28 per page, with filters: category (click =
 * next, right-click = all), sorting (distance, name, newest) and a search. Click = guide, shift-click = teleport
 * (mrwaypoints.teleport), right-click = edit (creator or team). The filters stay while the menu is open.
 * Bottom row, symmetric: back | category | previous | sorting | next | search | close.
 */
public final class ListMenu extends Menu {

    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};

    /** Order of the list; the language key of each is gui-sort-&lt;key&gt;. */
    enum Sort {
        DISTANCE("distance"), NAME("name"), NEWEST("newest");

        final String key;

        Sort(String key) {
            this.key = key;
        }

        Sort next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private final boolean mine;
    private final Menu back;
    private int page;
    /** null = all categories */
    private String category;
    private Sort sort = Sort.DISTANCE;
    private String search = "";

    public ListMenu(MRWaypoints plugin, Player viewer, boolean mine, Menu back) {
        super(plugin, viewer, 6, plugin.messages().get(mine ? "gui-mine-title" : "gui-all-title",
                tag("world", viewer.getWorld().getName())));
        this.mine = mine;
        this.back = back;
    }

    @Override
    protected void draw() {
        Messages messages = plugin.messages();
        List<Waypoint> list = filtered();
        int pages = Math.max(1, (list.size() + SLOTS.length - 1) / SLOTS.length);
        page = Math.min(page, pages - 1);
        int first = page * SLOTS.length;
        for (int i = 0; i < SLOTS.length && first + i < list.size(); i++) {
            Waypoint waypoint = list.get(first + i);
            set(SLOTS[i], item(waypoint), click -> click(waypoint, click));
        }
        if (list.isEmpty()) {
            set(22, Items.of(Material.PAPER).name(messages.item("gui-list-empty"))
                    .lore(messages.lore(filtersActive() ? "gui-list-empty-filtered-lore"
                            : mine ? "gui-list-empty-mine-lore" : "gui-list-empty-lore")).build());
        }
        set(4, Items.of(Material.OAK_SIGN).name(messages.item("gui-list-info", tag("world", viewer.getWorld().getName()),
                        tag("count", list.size())))
                .lore(messages.lore("gui-list-info-lore", tag("category", categoryLabel()),
                        tag("sort", messages.plain("gui-sort-" + sort.key)),
                        tag("search", search.isEmpty() ? messages.plain("gui-search-none") : search)))
                .build());

        set(45, Items.of(Material.ARROW).name(messages.item("gui-back")).build(), click -> {
            clickSound();
            back.open();
        });
        Category selected = category == null ? null : plugin.store().category(category);
        set(46, Items.of(selected == null ? Material.CHEST : selected.icon())
                .name(messages.item("gui-filter-category", tag("category", categoryLabel())))
                .lore(messages.lore("gui-filter-category-lore")).glint(selected != null).build(), click -> {
            clickSound();
            category = click.isRightClick() ? null : nextCategory();
            page = 0;
            render();
        });
        if (page > 0) {
            set(48, Items.of(Material.SPECTRAL_ARROW).name(messages.item("gui-previous", tag("page", page),
                    tag("pages", pages))).build(), click -> {
                clickSound();
                page--;
                render();
            });
        }
        set(49, Items.of(Material.HOPPER).name(messages.item("gui-sort", tag("sort", messages.plain("gui-sort-" + sort.key))))
                .lore(messages.lore("gui-sort-lore")).build(), click -> {
            clickSound();
            sort = click.isRightClick() ? Sort.DISTANCE : sort.next();
            page = 0;
            render();
        });
        if (page < pages - 1) {
            set(50, Items.of(Material.SPECTRAL_ARROW).name(messages.item("gui-next", tag("page", page + 2),
                    tag("pages", pages))).build(), click -> {
                clickSound();
                page++;
                render();
            });
        }
        set(52, Items.of(Material.NAME_TAG).name(messages.item("gui-search",
                        tag("search", search.isEmpty() ? messages.plain("gui-search-none") : search)))
                .lore(messages.lore("gui-search-lore")).glint(!search.isEmpty()).build(), click -> {
            clickSound();
            if (click.isRightClick()) {
                search = "";
                page = 0;
                render();
                return;
            }
            NameDialog.search(plugin, viewer, search, text -> {
                search = text.strip();
                page = 0;
            }, this::open);
        });
        set(53, Items.of(Material.OAK_DOOR).name(messages.item("gui-close")).build(), click -> viewer.closeInventory());
    }

    private boolean filtersActive() {
        return category != null || !search.isEmpty();
    }

    /** The waypoints after the filters: this dimension, mine or all, category, search - in the chosen order. */
    private List<Waypoint> filtered() {
        Location from = viewer.getLocation();
        String query = search.toLowerCase(Locale.ROOT);
        List<Waypoint> list = new ArrayList<>();
        for (Waypoint w : plugin.store().all()) {
            if (!w.world().equals(viewer.getWorld().getUID()) || plugin.store().category(w.category()) == null) {
                continue;
            }
            if ((mine && !w.creator().equals(viewer.getUniqueId())) || (category != null && !w.category().equals(category))
                    || (!query.isEmpty() && !w.name().toLowerCase(Locale.ROOT).contains(query))) {
                continue;
            }
            list.add(w);
        }
        Comparator<Waypoint> byName = Comparator.comparing(w -> w.name().toLowerCase(Locale.ROOT));
        list.sort(switch (sort) {
            case DISTANCE -> Comparator.comparingDouble((Waypoint w) -> w.distance(from)).thenComparing(byName);
            case NAME -> byName;
            case NEWEST -> Comparator.comparingLong(Waypoint::created).reversed();
        });
        return list;
    }

    /** The next category after the current one (all → first → … → last → all). */
    private String nextCategory() {
        List<String> ids = plugin.store().categories().stream().map(Category::id).toList();
        if (ids.isEmpty()) {
            return null;
        }
        int index = category == null ? -1 : ids.indexOf(category);
        return index + 1 >= ids.size() ? null : ids.get(index + 1);
    }

    private String categoryLabel() {
        Category c = category == null ? null : plugin.store().category(category);
        return c == null ? plugin.messages().plain("gui-filter-all") : c.name(plugin.messages().languageOf(viewer));
    }

    private ItemStack item(Waypoint waypoint) {
        Messages messages = plugin.messages();
        Category category = plugin.store().category(waypoint.category());
        double distance = waypoint.distance(viewer.getLocation());
        List<Component> lore = new ArrayList<>(messages.lore("gui-waypoint-near", tag("distance", Math.round(distance)),
                tag("x", waypoint.x()), tag("y", waypoint.y()), tag("z", waypoint.z())));
        lore.addAll(messages.lore(mine ? "gui-waypoint-category" : "gui-waypoint-category-creator",
                tag("category", category.name(messages.languageOf(viewer))), tag("player", waypoint.creatorName())));
        lore.addAll(messages.lore("gui-waypoint-guide"));
        if (viewer.hasPermission(WaypointPerms.TELEPORT)) {
            lore.addAll(messages.lore("gui-waypoint-teleport"));
        }
        if (plugin.permissions().mayEdit(viewer, waypoint)) {
            lore.addAll(messages.lore("gui-waypoint-edit"));
        }
        Integer target = plugin.guide().target(viewer);
        return Items.of(category.icon())
                .name(Component.text(waypoint.name(), TextColor.color(category.color())).decoration(TextDecoration.ITALIC, false))
                .lore(lore)
                .glint(target != null && target == waypoint.id())
                .build();
    }

    private void click(Waypoint waypoint, ClickType type) {
        if (type.isRightClick()) {
            if (plugin.permissions().mayEdit(viewer, waypoint)) {
                clickSound();
                new WaypointMenu(plugin, viewer, waypoint.id(), this).open();
            }
            return;
        }
        if (type.isShiftClick() && viewer.hasPermission(WaypointPerms.TELEPORT)) {
            viewer.closeInventory();
            plugin.actions().teleport(viewer, waypoint);
            return;
        }
        clickSound();
        viewer.closeInventory();
        plugin.guide().guide(viewer, waypoint);
    }
}
