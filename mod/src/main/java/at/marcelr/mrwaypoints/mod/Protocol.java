package at.marcelr.mrwaypoints.mod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Messages between mod and plugin - must match {@code ModBridge} and {@code Compatibility} in the plugin.
 * See docs/COMPATIBILITY.md.
 * <p>
 * {@code hello} and {@code welcome} never change their format (fields may only be added at the end), so every mod
 * version can find out whether it fits a plugin version. Everything else belongs to {@link #VERSION}.
 */
public final class Protocol {

    /** Protocol this mod speaks. Raise it (here and in the plugin) whenever state or toggle change. */
    public static final int VERSION = 2;

    private Protocol() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("mrwaypoints", path);
    }

    /** Result codes of {@link WelcomePayload} (never reused or reordered). */
    public enum Status {
        OK, MOD_TOO_OLD, MOD_TOO_NEW, UNKNOWN;

        static Status of(int code) {
            return code >= 0 && code < UNKNOWN.ordinal() ? values()[code] : UNKNOWN;
        }
    }

    /** Client to server on joining: VarInt protocol, String mod version, String Minecraft version. Frozen format. */
    public record HelloPayload(int protocol, String modVersion, String minecraftVersion) implements CustomPacketPayload {

        public static final Type<HelloPayload> TYPE = new Type<>(id("hello"));
        public static final StreamCodec<FriendlyByteBuf, HelloPayload> CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeVarInt(payload.protocol);
                    buf.writeUtf(payload.modVersion);
                    buf.writeUtf(payload.minecraftVersion);
                },
                buf -> new HelloPayload(buf.readVarInt(), buf.readUtf(), buf.readUtf()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Server's answer: VarInt status, VarInt min protocol, VarInt max protocol, String plugin version, String minimum
     * mod version, String download URL. Frozen format.
     */
    public record WelcomePayload(Status status, int minProtocol, int maxProtocol, String pluginVersion,
                                 String minimumModVersion, String downloadUrl) implements CustomPacketPayload {

        public static final Type<WelcomePayload> TYPE = new Type<>(id("welcome"));
        public static final StreamCodec<FriendlyByteBuf, WelcomePayload> CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeVarInt(payload.status.ordinal());
                    buf.writeVarInt(payload.minProtocol);
                    buf.writeVarInt(payload.maxProtocol);
                    buf.writeUtf(payload.pluginVersion);
                    buf.writeUtf(payload.minimumModVersion);
                    buf.writeUtf(payload.downloadUrl);
                },
                buf -> {
                    WelcomePayload payload = new WelcomePayload(Status.of(buf.readVarInt()), buf.readVarInt(),
                            buf.readVarInt(), buf.readUtf(), buf.readUtf(), buf.readUtf());
                    // Fields added by newer plugins are skipped.
                    buf.skipBytes(buf.readableBytes());
                    return payload;
                });

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** A category as the player sees it: name in their language, colour 0xRRGGBB, switched on for them. */
    public record Category(String id, String name, int color, boolean visible) {
    }

    /** A waypoint; {@code dimension} is the key of its world, e.g. "minecraft:overworld". */
    public record Waypoint(int id, String category, String name, String dimension, int x, int y, int z) {
    }

    /**
     * How the player wants the waypoints (set in /waypoints -> settings): in the world and on the minimap only up to
     * {@code range} chunks to every side of them (0 = unlimited), only on the World Map, the ones
     * beyond the range still on the World Map.
     */
    public record View(int range, boolean worldMapOnly, boolean farOnMap) {
    }

    /**
     * Everything for this player: VarInt n, n × (String id, String name, Int colour, Boolean visible), VarInt m,
     * m × (VarInt id, String category, String name, String dimension, Int x, Int y, Int z),
     * VarInt range, Boolean world map only, Boolean far ones on the World Map.
     */
    public record StatePayload(List<Category> categories, List<Waypoint> waypoints, View view) implements CustomPacketPayload {

        public static final Type<StatePayload> TYPE = new Type<>(id("state"));
        public static final StreamCodec<FriendlyByteBuf, StatePayload> CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeVarInt(payload.categories.size());
                    for (Category c : payload.categories) {
                        buf.writeUtf(c.id());
                        buf.writeUtf(c.name());
                        buf.writeInt(c.color());
                        buf.writeBoolean(c.visible());
                    }
                    buf.writeVarInt(payload.waypoints.size());
                    for (Waypoint w : payload.waypoints) {
                        buf.writeVarInt(w.id());
                        buf.writeUtf(w.category());
                        buf.writeUtf(w.name());
                        buf.writeUtf(w.dimension());
                        buf.writeInt(w.x());
                        buf.writeInt(w.y());
                        buf.writeInt(w.z());
                    }
                    buf.writeVarInt(payload.view.range());
                    buf.writeBoolean(payload.view.worldMapOnly());
                    buf.writeBoolean(payload.view.farOnMap());
                },
                buf -> {
                    int categories = buf.readVarInt();
                    List<Category> categoryList = new ArrayList<>(Math.min(categories, 256));
                    for (int i = 0; i < categories; i++) {
                        categoryList.add(new Category(buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readBoolean()));
                    }
                    int waypoints = buf.readVarInt();
                    List<Waypoint> waypointList = new ArrayList<>(Math.min(waypoints, 4096));
                    for (int i = 0; i < waypoints; i++) {
                        waypointList.add(new Waypoint(buf.readVarInt(), buf.readUtf(), buf.readUtf(), buf.readUtf(),
                                buf.readInt(), buf.readInt(), buf.readInt()));
                    }
                    View view = new View(buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
                    return new StatePayload(List.copyOf(categoryList), List.copyOf(waypointList), view);
                });

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Switch a category on or off for this player: String category ("*" = all), Boolean visible. */
    public record TogglePayload(String category, boolean visible) implements CustomPacketPayload {

        public static final Type<TogglePayload> TYPE = new Type<>(id("toggle"));
        public static final StreamCodec<FriendlyByteBuf, TogglePayload> CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeUtf(payload.category);
                    buf.writeBoolean(payload.visible);
                },
                buf -> new TogglePayload(buf.readUtf(), buf.readBoolean()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * A key was pressed, sent as the ordinal - new actions only at the end. Plugins ignore codes they do not know, so
     * a new action needs no new protocol (it just does nothing on older servers).
     */
    public enum Action {
        /** Open /waypoints. */
        MENU,
        /** Create a waypoint here (the server shows its dialog with name and category). */
        CREATE,
        /** End the guide. */
        GUIDE_STOP,
        /** Open the settings (range, display) - button in the category screen. */
        SETTINGS
    }

    /** Client to server: byte action. */
    public record ActionPayload(Action action) implements CustomPacketPayload {

        public static final Type<ActionPayload> TYPE = new Type<>(id("action"));
        public static final StreamCodec<FriendlyByteBuf, ActionPayload> CODEC = StreamCodec.of(
                (buf, payload) -> buf.writeByte(payload.action.ordinal()),
                buf -> new ActionPayload(Action.values()[Math.min(buf.readUnsignedByte(), Action.values().length - 1)]));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** The server asks to open the category screen (button in /waypoints). No content. */
    public record OpenPayload() implements CustomPacketPayload {

        public static final Type<OpenPayload> TYPE = new Type<>(id("open"));
        public static final StreamCodec<FriendlyByteBuf, OpenPayload> CODEC = StreamCodec.of(
                (buf, payload) -> {
                },
                buf -> new OpenPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
