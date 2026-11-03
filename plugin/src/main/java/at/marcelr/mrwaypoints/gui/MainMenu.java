package at.marcelr.mrwaypoints.gui;

import at.marcelr.mrwaypoints.MRWaypoints;
import at.marcelr.mrwaypoints.Messages;
import at.marcelr.mrwaypoints.util.Layout;
import at.marcelr.mrwaypoints.waypoint.Category;
import at.marcelr.mrwaypoints.waypoint.Waypoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * /waypoints: the categories (up to 21, three rows of seven, centred), the running guide on top (click stops it) and
 * close at the bottom.
 */
public final class MainMenu extends Menu {

    public MainMenu(MRWaypoints plugin, Player viewer) {
        super(plugin, viewer, 5, plugin.messages().get("gui-main-title"));
    }

    @Override
    protected void draw() {
        Messages messages = plugin.messages();
        List<Category> categories = new ArrayList<>(plugin.store().categories());
        int[] slots = Layout.centered(categories.size());
        for (int i = 0; i < slots.length; i++) {
            Category category = categories.get(i);
            int count = plugin.store().inCategory(category.id()).size();
            set(slots[i], Items.of(category.icon())
                    .name(Component.text(category.name(messages.languageOf(viewer)), TextColor.color(category.color()))
                            .decoration(TextDecoration.ITALIC, false))
                    .lore(messages.lore("gui-category-lore", tag("count", count)))
                    .build(), click -> {
                clickSound();
                new CategoryMenu(plugin, viewer, category.id(), this).open();
            });
        }
        if (categories.isEmpty()) {
            set(22, Items.of(Material.BARRIER).name(messages.item("gui-no-categories"))
                    .lore(messages.lore("gui-no-categories-lore")).build());
        }
        Integer target = plugin.guide().target(viewer);
        Waypoint guided = target == null ? null : plugin.store().byId(target);
        if (guided != null) {
            set(4, Items.of(Material.COMPASS).name(messages.item("gui-guide-name", tag("name", guided.name())))
                    .lore(messages.lore("gui-guide-lore")).glint(true).build(), click -> {
                clickSound();
                plugin.guide().stop(viewer);
                plugin.messages().send(viewer, "guide-stopped");
                render();
            });
        }
        set(40, Items.of(Material.OAK_DOOR).name(messages.item("gui-close")).build(), click -> viewer.closeInventory());
    }
}
