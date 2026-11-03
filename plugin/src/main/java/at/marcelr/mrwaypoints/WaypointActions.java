package at.marcelr.mrwaypoints;

import at.marcelr.mrwaypoints.waypoint.Category;
import at.marcelr.mrwaypoints.waypoint.Waypoint;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * Everything a player does with waypoints - menus and commands use the same checks and messages. Each method tells
 * the player the result itself.
 */
public final class WaypointActions {

    private final MRWaypoints plugin;

    WaypointActions(MRWaypoints plugin) {
        this.plugin = plugin;
    }

    private Messages messages() {
        return plugin.messages();
    }

    /**
     * Why the player may not create a waypoint in the category right now (language key), or null if they may.
     * Without a name check - for buttons.
     */
    public @Nullable String createBlocked(Player player, Category category) {
        if (!plugin.permissions().mayCreate(player, category)) {
            return "create-no-permission";
        }
        if (!plugin.permissions().unlimited(player, category)
                && plugin.store().countBy(player.getUniqueId(), category.id()) >= category.maxPerPlayer()) {
            return "create-limit";
        }
        return null;
    }

    /** Creates a waypoint where the player stands. @return the new waypoint, or null (the player was told why) */
    public @Nullable Waypoint create(Player player, Category category, String rawName) {
        String blocked = createBlocked(player, category);
        if (blocked != null) {
            deny(player, blocked, category);
            return null;
        }
        String name = name(player, category, rawName, null);
        if (name == null) {
            return null;
        }
        Location at = player.getLocation();
        Waypoint waypoint = plugin.store().add(new Waypoint(0, category.id(), name, at.getWorld().getUID(),
                at.getWorld().getName(), at.getBlockX(), at.getBlockY(), at.getBlockZ(), player.getUniqueId(),
                player.getName(), System.currentTimeMillis()));
        messages().send(player, "created", tag("name", name), tag("category", category.name(messages().languageOf(player))),
                tag("x", waypoint.x()), tag("y", waypoint.y()), tag("z", waypoint.z()));
        sound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        return waypoint;
    }

    public void rename(Player player, Waypoint waypoint, String rawName) {
        if (!mayEdit(player, waypoint)) {
            return;
        }
        Category category = plugin.store().category(waypoint.category());
        String name = category == null ? null : name(player, category, rawName, waypoint);
        if (name == null || name.equals(waypoint.name())) {
            return;
        }
        plugin.store().update(waypoint.withName(name));
        messages().send(player, "renamed", tag("old", waypoint.name()), tag("name", name));
        sound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
    }

    public void moveHere(Player player, Waypoint waypoint) {
        if (!mayEdit(player, waypoint)) {
            return;
        }
        Waypoint moved = waypoint.movedTo(player.getLocation());
        plugin.store().update(moved);
        messages().send(player, "moved", tag("name", waypoint.name()), tag("x", moved.x()), tag("y", moved.y()),
                tag("z", moved.z()));
        sound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
    }

    public void delete(Player player, Waypoint waypoint) {
        if (!mayEdit(player, waypoint)) {
            return;
        }
        plugin.store().remove(waypoint.id());
        messages().send(player, "deleted", tag("name", waypoint.name()));
        sound(player, Sound.ENTITY_ITEM_BREAK);
    }

    public void teleport(Player player, Waypoint waypoint) {
        if (!player.hasPermission(WaypointPerms.TELEPORT)) {
            messages().send(player, "no-permission");
            return;
        }
        Location target = waypoint.location();
        if (target == null) {
            messages().send(player, "world-not-loaded", tag("world", waypoint.worldName()));
            return;
        }
        target.setYaw(player.getLocation().getYaw());
        target.setPitch(player.getLocation().getPitch());
        player.teleportAsync(target).thenAccept(ok -> {
            if (ok) {
                messages().run(player, () -> messages().send(player, "teleported", tag("name", waypoint.name())));
            }
        });
    }

    private boolean mayEdit(Player player, Waypoint waypoint) {
        if (plugin.permissions().mayEdit(player, waypoint)) {
            return true;
        }
        messages().send(player, "edit-no-permission");
        return false;
    }

    /**
     * The checked name: trimmed, not empty, not too long, unique in the category ({@code self} is the waypoint being
     * renamed). Null if not ok - the player was told why.
     */
    private @Nullable String name(Player player, Category category, String raw, @Nullable Waypoint self) {
        String name = raw == null ? "" : raw.strip().replaceAll("\\s+", " ");
        int max = plugin.settings().waypoints().maxNameLength();
        if (name.isEmpty()) {
            messages().send(player, "name-empty");
            return null;
        }
        if (name.length() > max) {
            messages().send(player, "name-too-long", tag("max", max));
            return null;
        }
        Waypoint existing = plugin.store().byName(category.id(), name);
        if (existing != null && (self == null || existing.id() != self.id())) {
            messages().send(player, "name-taken", tag("name", name));
            return null;
        }
        return name;
    }

    private void deny(Player player, String key, Category category) {
        messages().send(player, key, tag("category", category.name(messages().languageOf(player))),
                tag("max", category.maxPerPlayer()));
        sound(player, Sound.ENTITY_VILLAGER_NO);
    }

    private void sound(Player player, Sound sound) {
        if (plugin.settings().gui().sounds()) {
            player.playSound(player.getLocation(), sound, 0.6f, 1.2f);
        }
    }
}
