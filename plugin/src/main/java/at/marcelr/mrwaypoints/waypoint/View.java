package at.marcelr.mrwaypoints.waypoint;

/**
 * How a player wants the server waypoints in Xaero's map (with the mod; set in /waypoints -> settings).
 *
 * @param range        up to this many blocks the waypoints show in the world and on the minimap, 0 = unlimited
 * @param worldMapOnly only on Xaero's World Map, never in the world or on the minimap
 * @param farOnMap     waypoints beyond the range still show on the World Map
 */
public record View(int range, boolean worldMapOnly, boolean farOnMap) {
}
