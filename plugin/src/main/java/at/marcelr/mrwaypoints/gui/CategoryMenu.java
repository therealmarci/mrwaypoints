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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * The waypoints of one category, nearest first (other worlds after them, by name), 28 per page. Click = guide,
 * shift-click = teleport (mrwaypoints.teleport), right-click = edit (creator or team). Bottom row: back, previous page,
 * new waypoint here (centre), next page, close.
 */
public final class CategoryMenu extends Menu {

    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};

    private final String categoryId;
    private final Menu back;
    private int page;

    public CategoryMenu(MRWaypoints plugin, Player viewer, String categoryId, Menu back) {
        super(plugin, viewer, 6, title(plugin, viewer, categoryId));
        this.categoryId = categoryId;
        this.back = back;
    }

    private static Component title(MRWaypoints plugin, Player viewer, String categoryId) {
        Category category = plugin.store().category(categoryId);
        String name = category == null ? categoryId : category.name(plugin.messages().languageOf(viewer));
        return plugin.messages().get("gui-category-title", tag("category", name));
    }

    @Override
    protected void draw() {
        Messages messages = plugin.messages();
        Category category = plugin.store().category(categoryId);
        if (category == null) {
            viewer.closeInventory();
            return;
        }
        List<Waypoint> list = sorted(plugin.store().inCategory(categoryId), viewer.getLocation());
        int pages = Math.max(1, (list.size() + SLOTS.length - 1) / SLOTS.length);
        page = Math.min(page, pages - 1);
        int first = page * SLOTS.length;
        for (int i = 0; i < SLOTS.length && first + i < list.size(); i++) {
            Waypoint waypoint = list.get(first + i);
            set(SLOTS[i], item(category, waypoint), click -> click(waypoint, click));
        }
        if (list.isEmpty()) {
            set(22, Items.of(Material.PAPER).name(messages.item("gui-category-empty"))
                    .lore(messages.lore("gui-category-empty-lore")).build());
        }

        set(45, Items.of(Material.ARROW).name(messages.item("gui-back")).build(), click -> {
            clickSound();
            back.open();
        });
        if (page > 0) {
            set(48, Items.of(Material.SPECTRAL_ARROW).name(messages.item("gui-previous", tag("page", page),
                    tag("pages", pages))).build(), click -> {
                clickSound();
                page--;
                render();
            });
        }
        createButton(category);
        if (page < pages - 1) {
            set(50, Items.of(Material.SPECTRAL_ARROW).name(messages.item("gui-next", tag("page", page + 2),
                    tag("pages", pages))).build(), click -> {
                clickSound();
                page++;
                render();
            });
        }
        set(53, Items.of(Material.OAK_DOOR).name(messages.item("gui-close")).build(), click -> viewer.closeInventory());
    }

    /** "New waypoint here" - only for those who may create in the category; grey with the reason at the limit. */
    private void createButton(Category category) {
        Messages messages = plugin.messages();
        if (!plugin.permissions().mayCreate(viewer, category)) {
            return;
        }
        String blocked = plugin.actions().createBlocked(viewer, category);
        if (blocked != null) {
            set(49, Items.of(Material.GRAY_DYE).name(messages.item("gui-create-full"))
                    .lore(messages.lore("gui-create-full-lore", tag("max", category.maxPerPlayer()))).build());
            return;
        }
        int used = plugin.store().countBy(viewer.getUniqueId(), category.id());
        List<Component> lore = new ArrayList<>(messages.lore("gui-create-lore"));
        if (!plugin.permissions().unlimited(viewer, category)) {
            lore.addAll(messages.lore("gui-create-limit-lore", tag("used", used), tag("max", category.maxPerPlayer())));
        }
        set(49, Items.of(Material.LIME_DYE).name(messages.item("gui-create")).lore(lore).build(), click -> {
            clickSound();
            NameDialog.create(plugin, viewer, category, this::open);
        });
    }

    private org.bukkit.inventory.ItemStack item(Category category, Waypoint waypoint) {
        Messages messages = plugin.messages();
        double distance = waypoint.distance(viewer.getLocation());
        List<Component> lore = new ArrayList<>();
        lore.addAll(distance >= 0
                ? messages.lore("gui-waypoint-near", tag("distance", Math.round(distance)), tag("x", waypoint.x()),
                tag("y", waypoint.y()), tag("z", waypoint.z()))
                : messages.lore("gui-waypoint-far", tag("world", waypoint.worldName()), tag("x", waypoint.x()),
                tag("y", waypoint.y()), tag("z", waypoint.z())));
        lore.addAll(messages.lore("gui-waypoint-creator", tag("player", waypoint.creatorName())));
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

    /** Nearest first in the viewer's world, then the other worlds by name. */
    static List<Waypoint> sorted(List<Waypoint> waypoints, Location from) {
        List<Waypoint> list = new ArrayList<>(waypoints);
        list.sort(Comparator.comparingDouble((Waypoint w) -> {
            double d = w.distance(from);
            return d < 0 ? Double.MAX_VALUE : d;
        }).thenComparing(w -> w.name().toLowerCase(Locale.ROOT)));
        return list;
    }
}
