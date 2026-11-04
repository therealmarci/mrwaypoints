package at.marcelr.mrwaypoints.gui;

import at.marcelr.mrwaypoints.MRWaypoints;
import at.marcelr.mrwaypoints.Messages;
import at.marcelr.mrwaypoints.util.Layout;
import at.marcelr.mrwaypoints.waypoint.View;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * /waypoints -> settings: how far and where the player sees the server waypoints in Xaero's map (only with the mod).
 * Range, display, "far ones on the World Map" and (with the mod) the key bindings in the middle row, back at the
 * bottom centre. Saved on the server per player, the mod gets the new state at once.
 */
public final class SettingsMenu extends Menu {

    private final Menu back;

    public SettingsMenu(MRWaypoints plugin, Player viewer, Menu back) {
        super(plugin, viewer, 3, plugin.messages().get("gui-settings-title"));
        this.back = back;
    }

    @Override
    protected void draw() {
        Messages messages = plugin.messages();
        View view = plugin.store().view(viewer.getUniqueId());
        List<Integer> ranges = plugin.settings().view().ranges();

        boolean mod = plugin.bridge().compatible(viewer);
        // With the mod a fourth button (key bindings) - the row stays symmetric: 10 12 14 16, otherwise 11 13 15.
        int[] slots = Layout.row(mod ? 4 : 3);
        if (!mod) {
            set(4, Items.of(Material.BOOK).name(messages.item("gui-settings-nomod-name"))
                    .lore(messages.lore("gui-settings-nomod-lore")).build());
        }

        set(slots[0], Items.of(Material.SPYGLASS).name(messages.item("gui-range-name", tag("range", range(view.range()))))
                .lore(messages.lore("gui-range-lore")).build(), click -> {
            clickSound();
            save(new View(Layout.step(ranges, view.range(), click != ClickType.RIGHT && click != ClickType.SHIFT_RIGHT),
                    view.worldMapOnly(), view.farOnMap()));
        });

        set(slots[1], Items.of(view.worldMapOnly() ? Material.FILLED_MAP : Material.ENDER_EYE)
                .name(messages.item("gui-display-name", tag("display",
                        messages.get(view.worldMapOnly() ? "gui-display-world-map" : "gui-display-everywhere"))))
                .lore(messages.lore("gui-display-lore")).build(), click -> {
            clickSound();
            save(new View(view.range(), !view.worldMapOnly(), view.farOnMap()));
        });

        // Only matters with a limited range - with "unlimited" everything is in range.
        if (view.range() == 0) {
            set(slots[2], Items.of(Material.GRAY_DYE).name(messages.item("gui-far-name", tag("state", state(view.farOnMap()))))
                    .lore(messages.lore("gui-far-unlimited-lore")).build());
        } else {
            set(slots[2], Items.of(view.farOnMap() ? Material.LIME_DYE : Material.GRAY_DYE)
                    .name(messages.item("gui-far-name", tag("state", state(view.farOnMap()))))
                    .lore(messages.lore("gui-far-lore")).build(), click -> {
                clickSound();
                save(new View(view.range(), view.worldMapOnly(), !view.farOnMap()));
            });
        }

        if (mod) {
            set(slots[3], Items.of(Material.KNOWLEDGE_BOOK).name(messages.item("gui-keys-name"))
                    .lore(messages.lore("gui-keys-lore")).build(), click -> {
                clickSound();
                viewer.closeInventory();
                plugin.bridge().openKeys(viewer);
            });
        }

        set(22, Items.of(Material.ARROW).name(messages.item("gui-back")).build(), click -> {
            clickSound();
            back.open();
        });
    }

    private void save(View view) {
        plugin.store().setView(viewer.getUniqueId(), view);
        render();
    }

    private Component range(int chunks) {
        return chunks == 0 ? plugin.messages().get("gui-range-unlimited")
                : plugin.messages().get("gui-range-chunks", tag("count", chunks));
    }

    private Component state(boolean on) {
        return plugin.messages().get(on ? "gui-state-on" : "gui-state-off");
    }
}
