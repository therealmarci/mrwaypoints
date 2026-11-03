package at.marcelr.mrwaypoints.gui;

import at.marcelr.mrwaypoints.MRWaypoints;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** "Really?" - confirm left, cancel right, symmetric around the centre. */
final class ConfirmMenu extends Menu {

    private final Runnable confirm;
    private final Menu back;

    ConfirmMenu(MRWaypoints plugin, Player viewer, Component title, Runnable confirm, Menu back) {
        super(plugin, viewer, 3, title);
        this.confirm = confirm;
        this.back = back;
    }

    @Override
    protected void draw() {
        set(11, Items.of(Material.RED_CONCRETE).name(plugin.messages().item("gui-confirm")).build(), click -> confirm.run());
        set(15, Items.of(Material.LIGHT_GRAY_CONCRETE).name(plugin.messages().item("gui-cancel")).build(), click -> {
            clickSound();
            back.open();
        });
    }
}
