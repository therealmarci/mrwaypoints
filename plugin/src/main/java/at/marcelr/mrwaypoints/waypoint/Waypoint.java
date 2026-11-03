package at.marcelr.mrwaypoints.waypoint;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * One server waypoint: a named block position in a category.
 *
 * @param worldName name of the world when the waypoint was saved - shown if the world is not loaded
 */
public record Waypoint(int id, String category, String name, UUID world, String worldName, int x, int y, int z,
                       UUID creator, String creatorName, long created) {

    public @Nullable World bukkitWorld() {
        return Bukkit.getWorld(world);
    }

    /** Middle of the block, or null if the world is not loaded. */
    public @Nullable Location location() {
        World w = bukkitWorld();
        return w == null ? null : new Location(w, x + 0.5, y, z + 0.5);
    }

    /** Distance in blocks from {@code from}, or -1 if it is in another world. */
    public double distance(Location from) {
        if (from.getWorld() == null || !from.getWorld().getUID().equals(world)) {
            return -1;
        }
        double dx = x + 0.5 - from.getX();
        double dy = y - from.getY();
        double dz = z + 0.5 - from.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public Waypoint withName(String newName) {
        return new Waypoint(id, category, newName, world, worldName, x, y, z, creator, creatorName, created);
    }

    public Waypoint movedTo(Location to) {
        return new Waypoint(id, category, name, to.getWorld().getUID(), to.getWorld().getName(), to.getBlockX(),
                to.getBlockY(), to.getBlockZ(), creator, creatorName, created);
    }

    public Waypoint inCategory(String newCategory) {
        return new Waypoint(id, newCategory, name, world, worldName, x, y, z, creator, creatorName, created);
    }
}
