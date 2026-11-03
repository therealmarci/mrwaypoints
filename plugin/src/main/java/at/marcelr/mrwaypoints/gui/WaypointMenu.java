package at.marcelr.mrwaypoints.gui;

import at.marcelr.mrwaypoints.MRWaypoints;
import at.marcelr.mrwaypoints.Messages;
import at.marcelr.mrwaypoints.WaypointPerms;
import at.marcelr.mrwaypoints.util.Layout;
import at.marcelr.mrwaypoints.waypoint.Category;
import at.marcelr.mrwaypoints.waypoint.Waypoint;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * One waypoint: info on top, the actions the viewer may use centred in the middle row (guide, teleport, rename, move
 * here, delete), back at the bottom centre.
 */
public final class WaypointMenu extends Menu {

    private record Action(ItemStack item, Consumer<ClickType> click) {
    }

    private final int waypointId;
    private final Menu back;

    public WaypointMenu(MRWaypoints plugin, Player viewer, int waypointId, Menu back) {
        super(plugin, viewer, 3, plugin.messages().get("gui-waypoint-title",
                tag("name", nameOf(plugin, waypointId))));
        this.waypointId = waypointId;
        this.back = back;
    }

    private static String nameOf(MRWaypoints plugin, int id) {
        Waypoint waypoint = plugin.store().byId(id);
        return waypoint == null ? "?" : waypoint.name();
    }

    @Override
    protected void draw() {
        Messages messages = plugin.messages();
        Waypoint waypoint = plugin.store().byId(waypointId);
        Category category = waypoint == null ? null : plugin.store().category(waypoint.category());
        if (waypoint == null || category == null) {
            back.open();
            return;
        }
        set(4, Items.of(category.icon()).name(messages.item("gui-waypoint-info", tag("name", waypoint.name())))
                .lore(messages.lore("gui-waypoint-info-lore", tag("category", category.name(messages.languageOf(viewer))),
                        tag("world", waypoint.worldName()), tag("x", waypoint.x()), tag("y", waypoint.y()),
                        tag("z", waypoint.z()), tag("player", waypoint.creatorName())))
                .build());

        List<Action> actions = new ArrayList<>();
        actions.add(new Action(Items.of(Material.COMPASS).name(messages.item("gui-action-guide")).build(), click -> {
            viewer.closeInventory();
            plugin.guide().guide(viewer, waypoint);
        }));
        if (viewer.hasPermission(WaypointPerms.TELEPORT)) {
            actions.add(new Action(Items.of(Material.ENDER_PEARL).name(messages.item("gui-action-teleport")).build(), click -> {
                viewer.closeInventory();
                plugin.actions().teleport(viewer, waypoint);
            }));
        }
        if (plugin.permissions().mayEdit(viewer, waypoint)) {
            actions.add(new Action(Items.of(Material.NAME_TAG).name(messages.item("gui-action-rename")).build(), click -> {
                clickSound();
                NameDialog.rename(plugin, viewer, waypoint, this::open);
            }));
            actions.add(new Action(Items.of(Material.RECOVERY_COMPASS).name(messages.item("gui-action-move"))
                    .lore(messages.lore("gui-action-move-lore")).build(), click -> {
                plugin.actions().moveHere(viewer, waypoint);
                render();
            }));
            actions.add(new Action(Items.of(Material.LAVA_BUCKET).name(messages.item("gui-action-delete")).build(), click -> {
                clickSound();
                new ConfirmMenu(plugin, viewer, messages.get("gui-delete-title", tag("name", waypoint.name())), () -> {
                    plugin.actions().delete(viewer, waypoint);
                    back.open();
                }, this).open();
            }));
        }
        int[] slots = Layout.row(actions.size());
        for (int i = 0; i < slots.length; i++) {
            set(slots[i], actions.get(i).item(), actions.get(i).click());
        }
        set(22, Items.of(Material.ARROW).name(messages.item("gui-back")).build(), click -> {
            clickSound();
            back.open();
        });
    }
}
