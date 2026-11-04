package at.marcelr.mrwaypoints.gui;

import at.marcelr.mrwaypoints.MRWaypoints;
import at.marcelr.mrwaypoints.Messages;
import at.marcelr.mrwaypoints.waypoint.Waypoint;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * Native Minecraft dialog with one text field: rename a waypoint, or the search term of a list. "Save" applies it,
 * "Cancel" closes it; afterwards {@code reopen} runs (the menu it came from).
 */
public final class NameDialog {

    private static final String INPUT = "text";

    private NameDialog() {
    }

    public static void rename(MRWaypoints plugin, Player viewer, Waypoint waypoint, Runnable reopen) {
        int id = waypoint.id();
        int max = plugin.settings().waypoints().maxNameLength();
        open(plugin, viewer, "rename", waypoint.name(), max, tag("name", waypoint.name()), text -> {
            Waypoint current = plugin.store().byId(id);
            if (current != null) {
                plugin.actions().rename(viewer, current, text);
            }
        }, reopen);
    }

    /** The search term of a list ("" = no search). */
    public static void search(MRWaypoints plugin, Player viewer, String current, Consumer<String> apply, Runnable reopen) {
        open(plugin, viewer, "search", current, 32, TagResolver.empty(), apply, reopen);
    }

    /** @param kind "rename" or "search" - the texts are dialog-&lt;kind&gt;-title / -hint / -label */
    private static void open(MRWaypoints plugin, Player viewer, String kind, String initial, int max, TagResolver tags,
                             Consumer<String> apply, Runnable reopen) {
        Messages messages = plugin.messages();
        TagResolver all = TagResolver.resolver(tags, tag("max", max));

        DialogAction save = DialogAction.customClick((response, audience) -> {
            String text = response.getText(INPUT);
            // Run on the main thread: the click may arrive on the network thread.
            Bukkit.getScheduler().runTask(plugin, () -> plugin.messages().run(viewer, () -> {
                if (viewer.isOnline()) {
                    apply.accept(text == null ? "" : text);
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

        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(messages.get("dialog-" + kind + "-title", all))
                        .canCloseWithEscape(true)
                        .body(List.of(DialogBody.plainMessage(messages.get("dialog-" + kind + "-hint", all))))
                        .inputs(List.of(DialogInput.text(INPUT, messages.get("dialog-" + kind + "-label", all))
                                .initial(initial)
                                .maxLength(max)
                                .width(250)
                                .build()))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.create(messages.get("dialog-save", all), null, 120, save),
                        ActionButton.create(messages.get("dialog-cancel", all), null, 120, cancel))));
        viewer.closeInventory();
        viewer.showDialog(dialog);
    }
}
