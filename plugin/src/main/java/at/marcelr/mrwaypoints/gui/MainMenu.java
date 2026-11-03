package at.marcelr.mrwaypoints.gui;

import at.marcelr.mrwaypoints.MRWaypoints;
import at.marcelr.mrwaypoints.Messages;
import at.marcelr.mrwaypoints.waypoint.Category;
import at.marcelr.mrwaypoints.waypoint.Waypoint;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * /waypoints: the start - "my waypoints", "create waypoint" and "all waypoints" in the top row, the running guide
 * in the middle (click ends it), "show in Xaero" (with the mod) and close at the bottom. Everything only for the
 * dimension the player is in.
 */
public final class MainMenu extends Menu {

    public MainMenu(MRWaypoints plugin, Player viewer) {
        super(plugin, viewer, 3, plugin.messages().get("gui-main-title"));
    }

    @Override
    protected void draw() {
        Messages messages = plugin.messages();
        String world = viewer.getWorld().getName();
        long mine = plugin.store().all().stream()
                .filter(w -> w.creator().equals(viewer.getUniqueId()) && inWorld(w)).count();
        long all = plugin.store().all().stream().filter(this::inWorld).count();

        set(2, Items.of(Material.PLAYER_HEAD).name(messages.item("gui-mine-name"))
                .lore(messages.lore("gui-mine-lore", tag("count", mine), tag("world", world))).build(), click -> {
            clickSound();
            new ListMenu(plugin, viewer, true, this).open();
        });
        createButton();
        set(6, Items.of(Material.FILLED_MAP).name(messages.item("gui-all-name"))
                .lore(messages.lore("gui-all-lore", tag("count", all), tag("world", world))).build(), click -> {
            clickSound();
            new ListMenu(plugin, viewer, false, this).open();
        });

        Integer target = plugin.guide().target(viewer);
        Waypoint guided = target == null ? null : plugin.store().byId(target);
        if (guided != null) {
            set(13, Items.of(Material.COMPASS).name(messages.item("gui-guide-name", tag("name", guided.name())))
                    .lore(messages.lore("gui-guide-lore")).glint(true).build(), click -> {
                clickSound();
                plugin.guide().stop(viewer);
                plugin.messages().send(viewer, "guide-stopped");
                render();
            });
        }
        // With the mod: "show in Xaero" and close side by side, otherwise close in the middle.
        boolean mod = plugin.bridge().compatible(viewer);
        if (mod) {
            set(21, Items.of(Material.MAP).name(messages.item("gui-xaero-name"))
                    .lore(messages.lore("gui-xaero-lore")).build(), click -> {
                clickSound();
                viewer.closeInventory();
                plugin.bridge().openScreen(viewer);
            });
        }
        set(mod ? 23 : 22, Items.of(Material.OAK_DOOR).name(messages.item("gui-close")).build(), click -> viewer.closeInventory());
    }

    /** "Create waypoint" - grey with the reason if the player may create in no category right now. */
    private void createButton() {
        Messages messages = plugin.messages();
        List<Category> allowed = CreateDialog.allowed(plugin, viewer);
        if (allowed.isEmpty()) {
            set(4, Items.of(Material.GRAY_DYE).name(messages.item("gui-create-name"))
                    .lore(messages.lore("gui-create-none-lore")).build());
            return;
        }
        set(4, Items.of(Material.LIME_DYE).name(messages.item("gui-create-name"))
                .lore(messages.lore("gui-create-here-lore", tag("count", allowed.size()))).build(), click -> {
            clickSound();
            CreateDialog.open(plugin, viewer, this::open);
        });
    }

    private boolean inWorld(Waypoint w) {
        return w.world().equals(viewer.getWorld().getUID()) && plugin.store().category(w.category()) != null;
    }
}
