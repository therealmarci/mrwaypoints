package at.marcelr.mrwaypoints.gui;

import at.marcelr.mrwaypoints.MRWaypoints;
import at.marcelr.mrwaypoints.Messages;
import at.marcelr.mrwaypoints.waypoint.Category;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * "Create waypoint": one native dialog with the name and the category - only categories the player may create in
 * right now (permission and limit), each with "used / limit". The waypoint goes where the player stands. Afterwards
 * {@code reopen} runs.
 */
public final class CreateDialog {

    private static final String NAME = "name";
    private static final String CATEGORY = "category";

    private CreateDialog() {
    }

    /** The categories the player may create a waypoint in right now. */
    public static List<Category> allowed(MRWaypoints plugin, Player player) {
        List<Category> allowed = new ArrayList<>();
        for (Category category : plugin.store().categories()) {
            if (plugin.actions().createBlockedHere(player, category) == null) {
                allowed.add(category);
            }
        }
        return allowed;
    }

    public static void open(MRWaypoints plugin, Player viewer, Runnable reopen) {
        Messages messages = plugin.messages();
        List<Category> allowed = allowed(plugin, viewer);
        if (allowed.isEmpty()) {
            messages.send(viewer, "create-none");
            return;
        }
        String lang = messages.languageOf(viewer);
        int max = plugin.settings().waypoints().maxNameLength();
        List<SingleOptionDialogInput.OptionEntry> options = new ArrayList<>();
        for (int i = 0; i < allowed.size(); i++) {
            Category c = allowed.get(i);
            Component label = Component.text(c.name(lang), TextColor.color(c.color()));
            if (!plugin.permissions().unlimited(viewer, c)) {
                label = label.append(Component.text(" (" + plugin.store().countBy(viewer.getUniqueId(), c.id()) + "/"
                        + c.maxPerPlayer() + ")", TextColor.color(0xA0A0A0)));
            }
            options.add(SingleOptionDialogInput.OptionEntry.create(c.id(), label, i == 0));
        }

        DialogAction save = DialogAction.customClick((response, audience) -> {
            String name = response.getText(NAME);
            String categoryId = response.getText(CATEGORY);
            // Run on the main thread: the click may arrive on the network thread.
            Bukkit.getScheduler().runTask(plugin, () -> plugin.messages().run(viewer, () -> {
                Category category = plugin.store().category(categoryId);
                if (viewer.isOnline() && category != null) {
                    plugin.actions().create(viewer, category, name);
                    reopen.run();
                }
            }));
        }, ClickCallback.Options.builder().uses(1).lifetime(Duration.ofMinutes(10)).build());
        DialogAction cancel = DialogAction.customClick((response, audience) -> Bukkit.getScheduler().runTask(plugin,
                () -> plugin.messages().run(viewer, () -> {
                    if (viewer.isOnline()) {
                        reopen.run();
                    }
                })), ClickCallback.Options.builder().uses(1).lifetime(Duration.ofMinutes(10)).build());

        var at = viewer.getLocation();
        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(messages.get("dialog-create-title"))
                        .canCloseWithEscape(true)
                        .body(List.of(DialogBody.plainMessage(messages.get("dialog-create-hint", tag("max", max),
                                tag("world", at.getWorld().getName()), tag("x", at.getBlockX()), tag("y", at.getBlockY()),
                                tag("z", at.getBlockZ())))))
                        .inputs(List.of(
                                DialogInput.text(NAME, messages.get("dialog-name-label")).maxLength(max).width(250).build(),
                                DialogInput.singleOption(CATEGORY, messages.get("dialog-category-label"), options)
                                        .width(250).build()))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.create(messages.get("dialog-save"), null, 120, save),
                        ActionButton.create(messages.get("dialog-cancel"), null, 120, cancel))));
        viewer.closeInventory();
        viewer.showDialog(dialog);
    }
}
