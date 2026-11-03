package at.marcelr.mrwaypoints;

import at.marcelr.mrwaypoints.util.Layout;
import at.marcelr.mrwaypoints.waypoint.Category;
import at.marcelr.mrwaypoints.waypoint.Waypoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * The guide to a waypoint: an arrow relative to where the player looks and the distance, in the action bar. Ends on
 * arrival, with /waypoints stop, a new target, when the waypoint is deleted or the player leaves. Works without the mod.
 */
public final class Guide implements Listener {


    private final MRWaypoints plugin;
    /** player -> waypoint id */
    private final Map<UUID, Integer> targets = new HashMap<>();
    private BukkitTask task;

    public Guide(MRWaypoints plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stopTask();
        long interval = plugin.settings().guide().updateTicks();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, interval, interval);
    }

    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void guide(Player player, Waypoint waypoint) {
        targets.put(player.getUniqueId(), waypoint.id());
        plugin.messages().send(player, "guide-started", tag("name", waypoint.name()));
        show(player, waypoint);
    }

    /** @return whether a guide was running */
    public boolean stop(Player player) {
        boolean had = targets.remove(player.getUniqueId()) != null;
        if (had) {
            player.sendActionBar(Component.empty());
        }
        return had;
    }

    public @Nullable Integer target(Player player) {
        return targets.get(player.getUniqueId());
    }

    private void tick() {
        for (var it = targets.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                it.remove();
                continue;
            }
            Waypoint waypoint = plugin.store().byId(entry.getValue());
            if (waypoint == null) {
                it.remove();
                plugin.messages().run(player, () -> plugin.messages().send(player, "guide-gone"));
                continue;
            }
            double distance = waypoint.distance(player.getLocation());
            if (distance >= 0 && distance <= plugin.settings().guide().arriveDistance()) {
                it.remove();
                plugin.messages().run(player, () -> {
                    player.sendActionBar(Component.empty());
                    plugin.messages().send(player, "guide-arrived", tag("name", waypoint.name()));
                });
                if (plugin.settings().guide().sound()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.4f);
                }
                continue;
            }
            plugin.messages().run(player, () -> show(player, waypoint));
        }
    }

    private void show(Player player, Waypoint waypoint) {
        Category category = plugin.store().category(waypoint.category());
        Component name = Component.text(waypoint.name(), TextColor.color(category == null ? 0xFFFFFF : category.color()));
        Location from = player.getLocation();
        double distance = waypoint.distance(from);
        if (distance < 0) {
            player.sendActionBar(plugin.messages().get("guide-other-world", tag("name", name),
                    tag("world", waypoint.worldName())));
            return;
        }
        int dy = waypoint.y() - from.getBlockY();
        Component height = Math.abs(dy) < 4 ? Component.empty()
                : plugin.messages().get(dy > 0 ? "guide-up" : "guide-down", tag("blocks", Math.abs(dy)));
        String arrow = Layout.arrow(from.getYaw(), waypoint.x() + 0.5 - from.getX(), waypoint.z() + 0.5 - from.getZ());
        player.sendActionBar(plugin.messages().get("guide-actionbar", tag("arrow", arrow),
                tag("name", name), tag("distance", Math.round(distance)), tag("height", height)));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        targets.remove(event.getPlayer().getUniqueId());
    }
}
