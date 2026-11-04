package at.marcelr.mrwaypoints.gui;

import at.marcelr.mrwaypoints.MRWaypoints;
import at.marcelr.mrwaypoints.Messages;
import at.marcelr.mrwaypoints.util.Layout;
import at.marcelr.mrwaypoints.waypoint.Category;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * /waypoints -> categories (with the mod): which categories show in Xaero's map. On top how many are on; then per
 * category its symbol and right below a green or red pane with its state - a click on either switches it (saved on
 * the server per player). Up to seven side by side, two such rows per page; at the bottom "all on", back, "all off"
 * and the page arrows. Replaces the mod's own screen - the key J opens /waypoints.
 */
public final class CategoryMenu extends Menu {

    /** Categories per page: two rows of seven. */
    private static final int PER_PAGE = 14;

    private final Menu back;
    private int page;

    public CategoryMenu(MRWaypoints plugin, Player viewer, Menu back) {
        super(plugin, viewer, rows(plugin), plugin.messages().get("gui-categories-title"));
        this.back = back;
    }

    /** A header row, two rows (symbol + state) per row of categories, the bottom row. */
    private static int rows(MRWaypoints plugin) {
        int categories = Math.min(plugin.store().categories().size(), PER_PAGE);
        return Math.clamp((categories + 6) / 7, 1, 2) * 2 + 2;
    }

    @Override
    protected void draw() {
        Messages messages = plugin.messages();
        List<Category> all = List.copyOf(plugin.store().categories());
        int pages = Math.max(1, (all.size() + PER_PAGE - 1) / PER_PAGE);
        page = Math.min(page, pages - 1);
        List<Category> categories = all.subList(page * PER_PAGE, Math.min(all.size(), (page + 1) * PER_PAGE));
        long on = all.stream().filter(c -> plugin.store().visible(viewer.getUniqueId(), c)).count();

        set(4, Items.of(Material.FILLED_MAP).name(messages.item("gui-categories-info", tag("on", on), tag("total", all.size())))
                .lore(messages.lore("gui-categories-info-lore")).build());

        for (int row = 0; row * 7 < categories.size(); row++) {
            List<Category> part = categories.subList(row * 7, Math.min(categories.size(), row * 7 + 7));
            int[] columns = Layout.columns(part.size());
            for (int i = 0; i < part.size(); i++) {
                Category category = part.get(i);
                boolean shown = plugin.store().visible(viewer.getUniqueId(), category);
                long count = plugin.store().all().stream().filter(w -> w.category().equals(category.id())).count();
                List<Component> lore = new java.util.ArrayList<>(messages.lore("gui-category-lore",
                        tag("state", messages.get(shown ? "gui-state-on" : "gui-state-off")), tag("count", count)));
                if (!category.worlds().isEmpty()) {
                    lore.add(messages.item("gui-category-only", tag("worlds", String.join(", ", category.worlds()))));
                }
                Component name = Component.text(category.name(messages.languageOf(viewer)), TextColor.color(category.color()))
                        .decoration(TextDecoration.ITALIC, false);
                int symbol = (1 + row * 2) * 9 + columns[i];
                set(symbol, Items.of(category.icon()).name(name).lore(lore).glint(shown).build(), click -> toggle(category, shown));
                // The state right under the symbol: green = in Xaero, red = hidden.
                set(symbol + 9, Items.of(shown ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE)
                        .name(messages.item(shown ? "gui-category-shown" : "gui-category-hidden", tag("name", name)))
                        .lore(messages.lore("gui-category-switch-lore")).build(), click -> toggle(category, shown));
            }
        }

        int bottom = getInventory().getSize() - 9;
        set(bottom + 2, Items.of(Material.LIME_DYE).name(messages.item("gui-categories-all-on")).build(), click -> {
            switched(true);
            plugin.store().setVisible(viewer.getUniqueId(), null, true);
            render();
        });
        set(bottom + 4, Items.of(Material.ARROW).name(messages.item("gui-back")).build(), click -> {
            clickSound();
            back.open();
        });
        set(bottom + 6, Items.of(Material.GRAY_DYE).name(messages.item("gui-categories-all-off")).build(), click -> {
            switched(false);
            plugin.store().setVisible(viewer.getUniqueId(), null, false);
            render();
        });
        if (page > 0) {
            set(bottom, Items.of(Material.SPECTRAL_ARROW).name(messages.item("gui-previous", tag("page", page),
                    tag("pages", pages))).build(), click -> {
                clickSound();
                page--;
                render();
            });
        }
        if (page < pages - 1) {
            set(bottom + 8, Items.of(Material.SPECTRAL_ARROW).name(messages.item("gui-next", tag("page", page + 2),
                    tag("pages", pages))).build(), click -> {
                clickSound();
                page++;
                render();
            });
        }
    }

    private void toggle(Category category, boolean shown) {
        switched(!shown);
        plugin.store().setVisible(viewer.getUniqueId(), category, !shown);
        render();
    }

    /** A clear sound for on and for off - the click shows at once. */
    private void switched(boolean on) {
        sound(on ? Sound.BLOCK_NOTE_BLOCK_PLING : Sound.BLOCK_NOTE_BLOCK_BASS);
    }
}
