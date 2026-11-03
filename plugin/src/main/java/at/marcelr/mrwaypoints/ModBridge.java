package at.marcelr.mrwaypoints;

import at.marcelr.mrwaypoints.util.Wire;
import at.marcelr.mrwaypoints.waypoint.Category;
import at.marcelr.mrwaypoints.waypoint.Waypoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static at.marcelr.mrwaypoints.Messages.tag;

/**
 * Connection to the MRwaypoints Fabric mod, which shows the server waypoints in Xaero's Minimap and World Map. The mod
 * is optional - menu and guide work without it. See docs/COMPATIBILITY.md.
 * <p>
 * On joining, the mod sends {@code hello} (protocol, mod version); the plugin checks it ({@link Compatibility}) and
 * answers {@code welcome}. After an OK it sends {@code state} (categories with the player's choice, all waypoints) -
 * again after every change - and takes {@code toggle} (switch a category on or off, saved per player). {@code hello}
 * and {@code welcome} never change their format.
 */
public final class ModBridge implements Listener, PluginMessageListener {

    public static final String HELLO = "mrwaypoints:hello";
    public static final String WELCOME = "mrwaypoints:welcome";
    public static final String STATE = "mrwaypoints:state";
    public static final String TOGGLE = "mrwaypoints:toggle";
    public static final String OPEN = "mrwaypoints:open";

    /** What the handshake of a player gave. */
    public record Client(int protocol, String modVersion, Compatibility.Status status) {
    }

    private final MRWaypoints plugin;
    private final Map<UUID, Client> clients = new HashMap<>();
    /**
     * Messages for channels the client has not announced yet, per player in the order they were sent (per channel the
     * newest wins). Bukkit drops plugin messages to unannounced channels silently - and the mod's hello can arrive
     * before its announcement.
     */
    private final Map<UUID, LinkedHashMap<String, byte[]>> waiting = new HashMap<>();
    private boolean resendQueued;

    public ModBridge(MRWaypoints plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, HELLO, this);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, TOGGLE, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, WELCOME);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, STATE);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, OPEN);
    }

    public void stop() {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin);
    }

    /** Whether the player plays with a compatible MRwaypoints mod. */
    public boolean compatible(Player player) {
        Client client = clients.get(player.getUniqueId());
        return client != null && client.status() == Compatibility.Status.OK;
    }

    /** The handshake result of the player, or null if the mod did not say hello (yet). */
    public Client client(Player player) {
        return clients.get(player.getUniqueId());
    }

    /** Waypoints or categories changed: everyone with the mod gets the new state (once per tick). */
    public void resendAll() {
        if (resendQueued || !plugin.isEnabled()) {
            return;
        }
        resendQueued = true;
        Bukkit.getScheduler().runTask(plugin, () -> {
            resendQueued = false;
            for (Player player : Bukkit.getOnlinePlayers()) {
                sendState(player);
            }
        });
    }

    /** Sends the player their state: categories (names in their language, their choice) and all waypoints. */
    public void sendState(Player player) {
        if (!compatible(player) || !player.hasPermission(WaypointPerms.USE)) {
            return;
        }
        String lang = plugin.messages().languageOf(player);
        Wire.Writer out = new Wire.Writer();
        var categories = plugin.store().categories();
        out.varInt(categories.size());
        for (Category c : categories) {
            out.string(c.id()).string(c.name(lang)).intValue(c.color())
                    .bool(plugin.store().visible(player.getUniqueId(), c));
        }
        var waypoints = plugin.store().all().stream().filter(w -> plugin.store().category(w.category()) != null).toList();
        out.varInt(waypoints.size());
        for (Waypoint w : waypoints) {
            World world = w.bukkitWorld();
            // The dimension the client knows the world by; an unloaded world by its old name.
            String dimension = world != null ? world.getKey().asString() : "minecraft:" + w.worldName().toLowerCase(Locale.ROOT);
            out.varInt(w.id()).string(w.category()).string(w.name()).string(dimension)
                    .intValue(w.x()).intValue(w.y()).intValue(w.z());
        }
        send(player, STATE, out.bytes());
    }

    /** Opens the mod's category screen. @return false without a compatible mod */
    public boolean openScreen(Player player) {
        if (!compatible(player)) {
            return false;
        }
        send(player, OPEN, new byte[0]);
        return true;
    }

    /**
     * Sends a message now, or as soon as the client announced the channel. While something is waiting, later messages
     * wait behind it, so they never overtake each other (e.g. the state the welcome).
     */
    private void send(Player player, String channel, byte[] message) {
        LinkedHashMap<String, byte[]> queue = waiting.get(player.getUniqueId());
        if ((queue == null || queue.isEmpty()) && player.getListeningPluginChannels().contains(channel)) {
            player.sendPluginMessage(plugin, channel, message);
            return;
        }
        queue = waiting.computeIfAbsent(player.getUniqueId(), id -> new LinkedHashMap<>());
        queue.remove(channel);
        queue.put(channel, message);
    }

    /** Sends the waiting messages in order, up to the first one whose channel the client has not announced yet. */
    private void flush(Player player) {
        LinkedHashMap<String, byte[]> queue = waiting.get(player.getUniqueId());
        if (queue == null) {
            return;
        }
        var it = queue.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, byte[]> next = it.next();
            if (!player.getListeningPluginChannels().contains(next.getKey())) {
                return;
            }
            player.sendPluginMessage(plugin, next.getKey(), next.getValue());
            it.remove();
        }
        waiting.remove(player.getUniqueId());
    }

    public Component downloadLink() {
        return plugin.messages().get("mod-download-label")
                .clickEvent(ClickEvent.openUrl(plugin.settings().mod().downloadUrl()));
    }

    // ------------------------------------------------------------------ events

    @EventHandler
    public void onRegister(PlayerRegisterChannelEvent event) {
        flush(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clients.remove(event.getPlayer().getUniqueId());
        waiting.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void onPluginMessageReceived(@NonNull String channel, @NonNull Player player, byte @NonNull [] message) {
        plugin.messages().run(player, () -> receive(channel, player, message));
    }

    private void receive(String channel, Player player, byte[] message) {
        try {
            switch (channel) {
                case HELLO -> hello(player, new Wire.Reader(message));
                case TOGGLE -> toggle(player, new Wire.Reader(message));
                default -> {
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Invalid " + channel + " message from " + player.getName() + ": " + e.getMessage());
        }
    }

    /** hello: VarInt protocol, String mod version, String Minecraft version (frozen format). */
    private void hello(Player player, Wire.Reader in) throws IOException {
        int protocol = in.varInt();
        String modVersion = in.string();
        Settings.ModSettings settings = plugin.settings().mod();
        Compatibility.Status status = Compatibility.check(protocol, modVersion, settings.minimumVersion());
        clients.put(player.getUniqueId(), new Client(protocol, modVersion, status));

        // welcome: VarInt status, VarInt min protocol, VarInt max protocol, String plugin version,
        // String minimum mod version, String download URL (frozen format).
        send(player, WELCOME, new Wire.Writer()
                .varInt(status.code())
                .varInt(Compatibility.MIN_PROTOCOL)
                .varInt(Compatibility.MAX_PROTOCOL)
                .string(plugin.updateChecker().currentVersion())
                .string(settings.minimumVersion())
                .string(settings.downloadUrl())
                .bytes());

        if (status == Compatibility.Status.OK) {
            plugin.getLogger().info(player.getName() + " uses the MRwaypoints mod " + modVersion + " (protocol " + protocol + ").");
            sendState(player);
        } else {
            plugin.getLogger().info(player.getName() + " uses an incompatible MRwaypoints mod " + modVersion
                    + " (protocol " + protocol + "): " + status.name().toLowerCase(Locale.ROOT));
            plugin.messages().send(player, status == Compatibility.Status.MOD_TOO_OLD ? "mod-too-old" : "mod-too-new",
                    tag("version", modVersion), tag("download", downloadLink()));
        }
    }

    /** toggle: String category ("*" = all), Boolean visible. */
    private void toggle(Player player, Wire.Reader in) throws IOException {
        String id = in.string();
        boolean visible = in.bool();
        if (!compatible(player)) {
            return;
        }
        Category category = id.equals("*") ? null : plugin.store().category(id);
        if (category == null && !id.equals("*")) {
            return;
        }
        // The store tells its listeners, which send the new state.
        plugin.store().setVisible(player.getUniqueId(), category, visible);
    }
}
