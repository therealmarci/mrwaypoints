package at.marcelr.mrwaypoints.gui;

import at.marcelr.mrwaypoints.MRWaypoints;
import at.marcelr.mrwaypoints.Messages;
import at.marcelr.mrwaypoints.util.Layout;
import at.marcelr.mrwaypoints.waypoint.Category;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * /waypoints -> categories (with the mod): which categories show in Xaero's map. One button per category (click
 * switches it, saved on the server per player), up to seven per row and symmetric; "all on", back and "all off" at
 * the bottom. Replaces the mod's own screen - the key J opens /waypoints.
 */
public final class CategoryMenu extends Menu {

    /** At most four rows of categories (28) - more do not fit between the header and the bottom row. */
    private static final int MAX_ROWS = 4;

    private final Menu back;

    public CategoryMenu(MRWaypoints plugin, Player viewer, Menu back) {
        super(plugin, viewer, rows(plugin), plugin.messages().get("gui-categories-title"));
        this.back = back;
    }

    private static int rows(MRWaypoints plugin) {
        int categories = plugin.store().categories().size();
        return Math.clamp((categories + 6) / 7, 1, MAX_ROWS) + 2;
    }

    @Override
    protected void draw() {
        Messages messages = plugin.messages();
        List<Category> categories = plugin.store().categories().stream().limit(MAX_ROWS * 7L).toList();
        int rows = getInventory().getSize() / 9;

        for (int row = 0; row * 7 < categories.size(); row++) {
            List<Category> part = categories.subList(row * 7, Math.min(categories.size(), row * 7 + 7));
            int[] columns = Layout.columns(part.size());
            for (int i = 0; i < part.size(); i++) {
                Category category = part.get(i);
                boolean on = plugin.store().visible(viewer.getUniqueId(), category);
                long count = plugin.store().all().stream().filter(w -> w.category().equals(category.id())).count();
                set((row + 1) * 9 + columns[i], Items.of(category.icon())
                        .name(Component.text(category.name(messages.languageOf(viewer)), TextColor.color(category.color()))
                                .decoration(TextDecoration.ITALIC, false))
                        .lore(messages.lore("gui-category-lore", tag("state", messages.get(on ? "gui-state-on" : "gui-state-off")),
                                tag("count", count)))
                        .lore(category.worlds().isEmpty() ? List.of() : List.of(messages.item("gui-category-only",
                                tag("worlds", String.join(", ", category.worlds())))))
                        .glint(on).build(), click -> {
                    clickSound();
                    plugin.store().setVisible(viewer.getUniqueId(), category, !on);
                    render();
                });
            }
        }

        int bottom = (rows - 1) * 9;
        set(bottom + 2, Items.of(Material.LIME_DYE).name(messages.item("gui-categories-all-on")).build(), click -> {
            clickSound();
            plugin.store().setVisible(viewer.getUniqueId(), null, true);
            render();
        });
        set(bottom + 4, Items.of(Material.ARROW).name(messages.item("gui-back")).build(), click -> {
            clickSound();
            back.open();
        });
        set(bottom + 6, Items.of(Material.GRAY_DYE).name(messages.item("gui-categories-all-off")).build(), click -> {
            clickSound();
            plugin.store().setVisible(viewer.getUniqueId(), null, false);
            render();
        });
    }
}
