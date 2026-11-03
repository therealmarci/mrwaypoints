package at.marcelr.mrwaypoints;

import at.marcelr.mrwaypoints.waypoint.Category;
import at.marcelr.mrwaypoints.waypoint.Waypoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Permissions of MRwaypoints. The per-category ones (mrwaypoints.create.&lt;id&gt;, mrwaypoints.manage.&lt;id&gt;) are
 * registered at runtime, because the categories come from categories.yml: as children of mrwaypoints.create.* and
 * mrwaypoints.manage.* (so the groups contain them), and create.&lt;id&gt; also of mrwaypoints.player for categories with
 * players-create. LuckPerms sees them like the ones in plugin.yml.
 */
public final class WaypointPerms {

    public static final String PLAYER = "mrwaypoints.player";
    public static final String USE = "mrwaypoints.use";
    public static final String CREATE = "mrwaypoints.create.";
    public static final String MANAGE = "mrwaypoints.manage.";
    public static final String TELEPORT = "mrwaypoints.teleport";
    public static final String VIEW = "mrwaypoints.admin.view";
    public static final String CATEGORIES = "mrwaypoints.admin.categories";
    public static final String RELOAD = "mrwaypoints.admin.reload";
    public static final String VERSION = "mrwaypoints.version";

    /** Category ids registered so far (to remove the ones of deleted categories). */
    private final Set<String> registered = new HashSet<>();

    /** Brings the per-category permissions in line with the categories. */
    public void register(Collection<Category> categories) {
        PluginManager pm = Bukkit.getPluginManager();
        Permission player = pm.getPermission(PLAYER);
        Permission createAll = pm.getPermission(CREATE + "*");
        Permission manageAll = pm.getPermission(MANAGE + "*");
        Set<String> now = new HashSet<>();
        for (Category c : categories) {
            now.add(c.id());
            Permission create = permission(pm, CREATE + c.id(), "Create waypoints in the category " + c.id() + ".");
            Permission manage = permission(pm, MANAGE + c.id(), "Edit and delete all waypoints of the category "
                    + c.id() + ", no limit there.");
            link(createAll, create, true);
            link(manageAll, manage, true);
            link(player, create, c.playersCreate());
        }
        for (String old : registered) {
            if (!now.contains(old)) {
                link(createAll, pm.getPermission(CREATE + old), false);
                link(manageAll, pm.getPermission(MANAGE + old), false);
                link(player, pm.getPermission(CREATE + old), false);
                pm.removePermission(CREATE + old);
                pm.removePermission(MANAGE + old);
            }
        }
        registered.clear();
        registered.addAll(now);
        Bukkit.getOnlinePlayers().forEach(Player::recalculatePermissions);
    }

    private static Permission permission(PluginManager pm, String name, String description) {
        Permission permission = pm.getPermission(name);
        if (permission == null) {
            permission = new Permission(name, description, PermissionDefault.FALSE);
            pm.addPermission(permission);
        }
        return permission;
    }

    /** Makes {@code child} a child of {@code parent} (or removes it). */
    private static void link(Permission parent, Permission child, boolean on) {
        if (parent == null || child == null) {
            return;
        }
        boolean has = parent.getChildren().containsKey(child.getName());
        if (on && !has) {
            parent.getChildren().put(child.getName(), true);
            parent.recalculatePermissibles();
        } else if (!on && has) {
            parent.getChildren().remove(child.getName());
            parent.recalculatePermissibles();
        }
    }

    // ------------------------------------------------------------ checks

    public boolean mayCreate(Player player, Category category) {
        return player.hasPermission(CREATE + category.id());
    }

    public boolean mayManage(Player player, Category category) {
        return player.hasPermission(MANAGE + category.id());
    }

    /** Rename, move and delete: the creator, or the team with manage for the category. */
    public boolean mayEdit(Player player, Waypoint waypoint) {
        return waypoint.creator().equals(player.getUniqueId()) || player.hasPermission(MANAGE + waypoint.category());
    }

    /** The limit of the category does not apply (team with manage). */
    public boolean unlimited(Player player, Category category) {
        return category.maxPerPlayer() <= 0 || mayManage(player, category);
    }
}
