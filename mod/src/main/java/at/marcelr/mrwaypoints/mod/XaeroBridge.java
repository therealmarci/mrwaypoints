package at.marcelr.mrwaypoints.mod;

import at.marcelr.mrwaypoints.mod.Protocol.Category;
import at.marcelr.mrwaypoints.mod.Protocol.StatePayload;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.WaypointVisibilityType;
import xaero.hud.minimap.waypoint.thirdparty.ThirdPartyWaypointManager;
import xaero.hud.minimap.waypoint.thirdparty.ThirdPartyWaypoints;
import xaero.hud.minimap.world.container.MinimapWorldContainer;
import xaero.hud.minimap.world.container.MinimapWorldRootContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The only class that touches Xaero's Minimap. Xaero has no official API for this; it keeps waypoints of other mods
 * apart from the player's own ones ("third-party waypoints", per origin - Xaero uses it itself for Waystones). They are
 * not saved in the player's waypoint files and show on the minimap, in the world and on Xaero's World Map.
 * <p>
 * Every visible category is its own origin {@code mrwaypoints:<category>} in the container of each dimension that has
 * waypoints of it. On every change all origins are cleared and filled again - a few hundred waypoints at most.
 * <p>
 * If Xaero changes its internals, this class fails: it then logs once and does nothing any more (the game goes on).
 */
final class XaeroBridge {

    /** Containers and origins filled last time, to clear them again. */
    private static final List<MinimapWorldContainer> USED = new ArrayList<>();
    private static final Set<Identifier> ORIGINS = new HashSet<>();
    /** The waypoints put into Xaero with where they are - their visibility follows the player (range). */
    private static final List<Shown> SHOWN = new ArrayList<>();
    /** The player's view of the last state, and where the visibility was set last (null = set it again). */
    private static Protocol.View view;
    private static String lastDimension;
    private static double lastX;
    private static double lastZ;
    /** The root container the waypoints went into - a new one (other server, world switch) needs them again. */
    private static Object appliedRoot;
    private static boolean broken;

    private record Shown(Waypoint waypoint, String dimension, int x, int z) {
    }

    private XaeroBridge() {
    }

    /** Whether Xaero has a new world session since the last apply (the waypoints are only in the old one). */
    static boolean needsApply() {
        if (broken) {
            return false;
        }
        try {
            MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
            return session != null && session.getWorldManager().getAutoRootContainer() != appliedRoot;
        } catch (LinkageError | RuntimeException e) {
            fail(e);
            return false;
        }
    }

    /**
     * Puts the waypoints of the visible categories into Xaero ({@code hiddenAll}: none - the key "server waypoints
     * on/off"). @return false if Xaero is not ready yet (try later)
     */
    static boolean apply(StatePayload state, boolean hiddenAll) {
        if (broken) {
            return true;
        }
        try {
            MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
            MinimapWorldRootContainer root = session == null ? null : session.getWorldManager().getAutoRootContainer();
            if (root == null) {
                return false;
            }
            clearNow();
            Map<String, Category> visible = new HashMap<>();
            for (Category category : state.categories()) {
                if (category.visible() && !hiddenAll) {
                    visible.put(category.id(), category);
                }
            }
            Map<String, MinimapWorldContainer> containers = new HashMap<>();
            int count = 0;
            for (Protocol.Waypoint w : state.waypoints()) {
                Category category = visible.get(w.category());
                if (category == null) {
                    continue;
                }
                MinimapWorldContainer container = containers.computeIfAbsent(w.dimension(), dimension -> {
                    ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimension));
                    String directory = session.getDimensionHelper().getDimensionDirectoryName(key);
                    MinimapWorldContainer c = root.addSubContainer(root.getPath().resolve(directory));
                    USED.add(c);
                    return c;
                });
                Identifier origin = Identifier.fromNamespaceAndPath("mrwaypoints", category.id());
                ORIGINS.add(origin);
                ThirdPartyWaypoints waypoints = container.getThirdPartyWaypointManager().get(origin);
                Waypoint waypoint = new Waypoint(w.x(), w.y(), w.z(), w.name(), initials(w.name()),
                        nearest(category.color()), WaypointPurpose.NORMAL);
                waypoints.add(String.valueOf(w.id()), waypoint);
                SHOWN.add(new Shown(waypoint, w.dimension(), w.x(), w.z()));
                count++;
            }
            view = state.view();
            lastDimension = null;
            appliedRoot = root;
            MRwaypointsClient.LOG.info("{} server waypoints in {} categories shown in Xaero's Minimap.", count,
                    visible.size());
            return true;
        } catch (LinkageError | RuntimeException e) {
            fail(e);
            return true;
        }
    }

    /**
     * Sets every waypoint's visibility from the player's view and position: in range in the world and on the minimap
     * (or only on the World Map), beyond the range on the World Map or nowhere. Only does something after a new state
     * or when the player moved a few blocks - cheap enough for every tick.
     */
    static void follow(String dimension, double x, double z) {
        if (broken || view == null) {
            return;
        }
        if (dimension.equals(lastDimension) && Math.abs(x - lastX) < 8 && Math.abs(z - lastZ) < 8) {
            return;
        }
        lastDimension = dimension;
        lastX = x;
        lastZ = z;
        try {
            // The range is a square of chunks around the player: 10 = 10 x 10 chunks, i.e. 80 blocks to every side.
            double half = view.range() * 16 / 2.0;
            for (Shown shown : SHOWN) {
                double dx = shown.x() + 0.5 - x;
                double dz = shown.z() + 0.5 - z;
                // Other dimensions are out of range (Xaero shows them only on the World Map of that dimension).
                boolean near = view.range() == 0
                        || shown.dimension().equals(dimension) && Math.abs(dx) <= half && Math.abs(dz) <= half;
                Waypoint waypoint = shown.waypoint();
                if (near || view.farOnMap()) {
                    waypoint.setDisabled(false);
                    waypoint.setVisibility(near && !view.worldMapOnly()
                            ? WaypointVisibilityType.LOCAL : WaypointVisibilityType.WORLD_MAP_LOCAL);
                } else {
                    waypoint.setDisabled(true);
                }
            }
        } catch (LinkageError | RuntimeException e) {
            fail(e);
        }
    }

    /** Removes all MRwaypoints waypoints from Xaero (leaving the server). */
    static void clear() {
        if (broken) {
            return;
        }
        try {
            clearNow();
        } catch (LinkageError | RuntimeException e) {
            fail(e);
        }
        appliedRoot = null;
        view = null;
    }

    private static void clearNow() {
        for (MinimapWorldContainer container : USED) {
            ThirdPartyWaypointManager manager = container.getThirdPartyWaypointManager();
            for (Identifier origin : ORIGINS) {
                manager.clearOrigin(origin);
            }
        }
        USED.clear();
        ORIGINS.clear();
        SHOWN.clear();
    }

    private static void fail(Throwable e) {
        broken = true;
        MRwaypointsClient.LOG.error("This version of Xaero's Minimap is not supported - server waypoints are not shown "
                + "in it. Please report it with the Xaero version.", e);
    }

    /** One or two letters on the waypoint: the first letters of the first two words ("Test Shop" -> "TS"). */
    static String initials(String name) {
        String[] words = name.trim().split("\\s+");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty() && out.length() < 2) {
                out.appendCodePoint(word.codePointAt(0));
            }
        }
        return out.isEmpty() ? "?" : out.toString().toUpperCase();
    }

    /** Xaero knows only its own colours - the one closest to the category's. */
    private static WaypointColor nearest(int rgb) {
        WaypointColor best = WaypointColor.WHITE;
        double bestDistance = Double.MAX_VALUE;
        for (WaypointColor color : WaypointColor.values()) {
            int hex = color.getHex();
            double dr = ((hex >> 16) & 0xFF) - ((rgb >> 16) & 0xFF);
            double dg = ((hex >> 8) & 0xFF) - ((rgb >> 8) & 0xFF);
            double db = (hex & 0xFF) - (rgb & 0xFF);
            // Weighted like the eye sees it, so a yellow does not end up grey.
            double distance = 2 * dr * dr + 4 * dg * dg + 3 * db * db;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = color;
            }
        }
        return best;
    }
}
