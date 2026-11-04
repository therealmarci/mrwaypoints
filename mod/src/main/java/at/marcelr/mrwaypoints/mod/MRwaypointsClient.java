package at.marcelr.mrwaypoints.mod;

import at.marcelr.mrwaypoints.mod.Protocol.Action;
import at.marcelr.mrwaypoints.mod.Protocol.ActionPayload;
import at.marcelr.mrwaypoints.mod.Protocol.HelloPayload;
import at.marcelr.mrwaypoints.mod.Protocol.KeysPayload;
import at.marcelr.mrwaypoints.mod.Protocol.StatePayload;
import at.marcelr.mrwaypoints.mod.Protocol.WelcomePayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ServerboundPlayChannelEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MRwaypoints on the client: says hello to the plugin, receives the server waypoints and hands the visible ones to
 * Xaero's Minimap ({@link XaeroBridge}). There is one menu only - the server's /waypoints (categories for Xaero,
 * settings, lists): the key J and a button in Xaero's waypoint menu open it. The categories are saved on the server per
 * player and can also be switched on the player page in the web interface.
 */
public final class MRwaypointsClient implements ClientModInitializer {

    public static final Logger LOG = LoggerFactory.getLogger("MRwaypoints");
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("mrwaypoints", "main"));
    /** Class of Xaero's waypoint menu - by name, so the button just does not appear if Xaero renames it. */
    private static final String XAERO_WAYPOINTS_SCREEN = "xaero.common.gui.GuiWaypoints";

    private static KeyMapping createKey;
    private static KeyMapping toggleAllKey;
    private static KeyMapping menuKey;
    /** All server waypoints hidden in Xaero with the key (only on this PC; the choice per category stays). */
    private static volatile boolean hiddenAll;
    private static volatile StatePayload state;
    /** The server answered the hello with OK. */
    private static volatile boolean compatible;
    private static boolean helloSent;
    /** Ticks since the hello without an answer - it is repeated now and then (the server may have missed it). */
    private static int helloTicks;
    /** Status of the last welcome, or null if none arrived yet. */
    private static volatile Protocol.Status status;
    /** The state changed and is not yet in Xaero (applied on the client thread). */
    private static volatile boolean dirty;

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.serverboundPlay().register(HelloPayload.TYPE, HelloPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ActionPayload.TYPE, ActionPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WelcomePayload.TYPE, WelcomePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StatePayload.TYPE, StatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(KeysPayload.TYPE, KeysPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(WelcomePayload.TYPE, (payload, context) -> welcome(payload));
        ClientPlayNetworking.registerGlobalReceiver(StatePayload.TYPE, (payload, context) -> {
            state = payload;
            dirty = true;
        });
        // Settings in /waypoints: open Minecraft's key bindings (the MRwaypoints keys are listed there).
        ClientPlayNetworking.registerGlobalReceiver(KeysPayload.TYPE, (payload, context) ->
                context.client().gui.setScreen(new KeyBindsScreen(null, context.client().options)));

        // Say hello as soon as the server can receive it (it announces its channels after joining).
        ServerboundPlayChannelEvents.REGISTER.register((handler, sender, client, channels) -> {
            if (channels.contains(HelloPayload.TYPE.id())) {
                sayHello();
            }
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (ClientPlayNetworking.canSend(HelloPayload.TYPE)) {
                sayHello();
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            XaeroBridge.clear();
            state = null;
            compatible = false;
            helloSent = false;
            helloTicks = 0;
            status = null;
            dirty = false;
        });

        // Only J has a default; the others are free to bind (Options -> Controls -> MRwaypoints), so nothing clashes
        // with Xaero or other mods. Fixed order there, the ids stay the same.
        menuKey = key("menu", InputConstants.KEY_J, 0);
        createKey = key("create", InputConstants.UNKNOWN.getValue(), 1);
        toggleAllKey = key("toggle-all", InputConstants.UNKNOWN.getValue(), 2);

        // A button in Xaero's waypoint menu opens /waypoints (only on servers with MRwaypoints).
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (state() != null && screen.getClass().getName().equals(XAERO_WAYPOINTS_SCREEN)) {
                Screens.getWidgets(screen).add(Button.builder(Component.translatable("mrwaypoints.button.xaero"),
                        button -> {
                            client.gui.setScreen(null);
                            send(client, Action.MENU);
                        })
                        .bounds(width - 125, 5, 120, 20).build());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (helloSent && status == null && client.getConnection() != null && ++helloTicks % 60 == 0
                    && helloTicks <= 600) {
                LOG.info("No answer from MRwaypoints on the server yet, saying hello again.");
                ClientPlayNetworking.send(hello());
            }
            while (createKey.consumeClick()) {
                send(client, Action.CREATE);
            }
            while (menuKey.consumeClick()) {
                send(client, Action.MENU);
            }
            while (toggleAllKey.consumeClick()) {
                if (connected(client)) {
                    hiddenAll = !hiddenAll;
                    dirty = true;
                    client.player.sendSystemMessage(Component.translatable(hiddenAll
                            ? "mrwaypoints.message.all-hidden" : "mrwaypoints.message.all-shown"));
                }
            }
            // Xaero's session can come later than our state, and it changes on every world/server switch.
            StatePayload current = state();
            if (current != null && (dirty || XaeroBridge.needsApply())) {
                dirty = !XaeroBridge.apply(current, hiddenAll);
            }
            // The range follows the player.
            if (current != null && client.player != null) {
                XaeroBridge.follow(client.player.level().dimension().identifier().toString(), client.player.getX(),
                        client.player.getZ());
            }
        });
    }

    private static KeyMapping key(String name, int defaultKey, int order) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping("key.mrwaypoints." + name,
                InputConstants.Type.KEYBOARD, defaultKey, CATEGORY, order));
    }

    /** Whether MRwaypoints answered on this server - otherwise a short hint in the chat. */
    private static boolean connected(Minecraft client) {
        if (state() != null) {
            return true;
        }
        if (client.player != null) {
            // The server has MRwaypoints but refused this mod: say why instead of "no MRwaypoints".
            String key = status == Protocol.Status.MOD_TOO_OLD ? "mrwaypoints.message.too-old"
                    : status == Protocol.Status.MOD_TOO_NEW ? "mrwaypoints.message.too-new"
                    : status == Protocol.Status.UNKNOWN ? "mrwaypoints.message.refused"
                    : "mrwaypoints.message.no-server";
            client.player.sendSystemMessage(Component.translatable(key));
        }
        return false;
    }

    /** Asks the server to do something (only with a compatible MRwaypoints on the server). */
    private static void send(Minecraft client, Action action) {
        if (connected(client) && ClientPlayNetworking.canSend(ActionPayload.TYPE)) {
            ClientPlayNetworking.send(new ActionPayload(action));
        }
    }

    private static void sayHello() {
        if (helloSent) {
            return;
        }
        helloSent = true;
        helloTicks = 0;
        ClientPlayNetworking.send(hello());
    }

    private static HelloPayload hello() {
        return new HelloPayload(Protocol.VERSION, version("mrwaypoints"), version("minecraft"));
    }

    private static String version(String mod) {
        return FabricLoader.getInstance().getModContainer(mod)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("?");
    }

    private static void welcome(WelcomePayload payload) {
        compatible = payload.status() == Protocol.Status.OK;
        status = payload.status();
        if (compatible) {
            LOG.info("Connected to MRwaypoints {} on the server (protocol {}-{}).", payload.pluginVersion(),
                    payload.minProtocol(), payload.maxProtocol());
            dirty = true;
            return;
        }
        state = null;
        XaeroBridge.clear();
        LOG.warn("MRwaypoints on the server ({}, protocol {}-{}) does not accept this mod (protocol {}): {}",
                payload.pluginVersion(), payload.minProtocol(), payload.maxProtocol(), Protocol.VERSION,
                payload.status());
        // Too old / too new: the server explains it in the chat with the download link. Only unknown answers here.
        if (payload.status() == Protocol.Status.UNKNOWN) {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                client.player.sendSystemMessage(Component.translatable("mrwaypoints.message.incompatible",
                        payload.pluginVersion()));
            }
        }
    }

    /** The server waypoints, or null without (compatible) MRwaypoints on the server. */
    public static StatePayload state() {
        return compatible ? state : null;
    }
}
