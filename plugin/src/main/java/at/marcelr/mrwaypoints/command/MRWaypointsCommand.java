package at.marcelr.mrwaypoints.command;

import at.marcelr.mrwaypoints.MRWaypoints;
import at.marcelr.mrwaypoints.UpdateChecker;
import at.marcelr.mrwaypoints.WaypointPerms;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static at.marcelr.mrwaypoints.Messages.tag;

/** /mrwaypoints - reload (config, categories, texts), version and help. */
public final class MRWaypointsCommand implements TabExecutor {

    private final MRWaypoints plugin;

    public MRWaypointsCommand(MRWaypoints plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label,
                             String @NonNull [] args) {
        plugin.messages().run(sender, () -> handle(sender, args));
        return true;
    }

    private void handle(CommandSender sender, String[] args) {
        String sub = args.length == 0 ? "help" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> {
                if (check(sender, WaypointPerms.RELOAD)) {
                    plugin.reload();
                    plugin.messages().send(sender, "reloaded", tag("categories", plugin.store().categories().size()),
                            tag("waypoints", plugin.store().all().size()));
                }
            }
            case "version", "ver" -> {
                if (check(sender, WaypointPerms.VERSION)) {
                    version(sender);
                }
            }
            default -> help(sender);
        }
    }

    private void help(CommandSender sender) {
        plugin.messages().send(sender, "help-header");
        plugin.messages().send(sender, "help-waypoints");
        if (sender.hasPermission(WaypointPerms.RELOAD)) {
            plugin.messages().send(sender, "help-reload");
        }
        plugin.messages().send(sender, "help-version");
    }

    private boolean check(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        plugin.messages().send(sender, "no-permission");
        return false;
    }

    private void version(CommandSender sender) {
        UpdateChecker updates = plugin.updateChecker();
        plugin.messages().send(sender, "version",
                tag("version", updates.currentVersion()), tag("mc", updates.minecraftVersion()),
                tag("author", String.join(", ", plugin.getPluginMeta().getAuthors())));
        // Order: version, update check, result, empty line, links.
        Runnable links = () -> {
            sender.sendMessage(Component.empty());
            plugin.messages().send(sender, "version-links",
                    tag("website", link("Website", updates.websitePage())),
                    tag("github", link("GitHub", updates.githubPage())));
        };
        if (plugin.settings().update().enabled()) {
            updates.checkNow(sender, links);
        } else {
            updates.sendStatus(sender);
            links.run();
        }
    }

    private static Component link(String label, String url) {
        return Component.text(label).color(NamedTextColor.AQUA)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(url));
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias,
                                      String @NonNull [] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.add("help");
            options.add("version");
            if (sender.hasPermission(WaypointPerms.RELOAD)) {
                options.add("reload");
            }
        }
        String prefix = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.startsWith(prefix)).toList();
    }
}
