package at.marcelr.mrwaypoints.command;

import at.marcelr.mrwaypoints.MRWaypoints;
import at.marcelr.mrwaypoints.WaypointPerms;
import at.marcelr.mrwaypoints.gui.MainMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;

/** /waypoints - the menu with all server waypoints; /waypoints stop - end the guide. */
public final class WaypointsCommand implements TabExecutor {

    private final MRWaypoints plugin;

    public WaypointsCommand(MRWaypoints plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label,
                             String @NonNull [] args) {
        plugin.messages().run(sender, () -> {
            if (!(sender instanceof Player player)) {
                plugin.messages().send(sender, "players-only");
                return;
            }
            if (!player.hasPermission(WaypointPerms.USE)) {
                plugin.messages().send(player, "no-permission");
                return;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("stop")) {
                plugin.messages().send(player, plugin.guide().stop(player) ? "guide-stopped" : "guide-none");
                return;
            }
            new MainMenu(plugin, player).open();
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias,
                                      String @NonNull [] args) {
        return args.length == 1 && "stop".startsWith(args[0].toLowerCase()) ? List.of("stop") : List.of();
    }
}
