package at.marcelr.mrwaypoints.mod;

import at.marcelr.mrwaypoints.mod.Protocol.HelloPayload;
import at.marcelr.mrwaypoints.mod.Protocol.OpenPayload;
import at.marcelr.mrwaypoints.mod.Protocol.StatePayload;
import at.marcelr.mrwaypoints.mod.Protocol.TogglePayload;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MRwaypoints on the client: says hello to the plugin, receives the server waypoints and hands the visible ones to
 * Xaero's Minimap ({@link XaeroBridge}). The categories are switched on and off on the server (saved per player), from
 * the own screen ({@link CategoryScreen}: key, button in Xaero's waypoint menu, button in /waypoints) or on the player
 * page in the web interface.
 */
public final class MRwaypointsClient implements ClientModInitializer {

    public static final Logger LOG = LoggerFactory.getLogger("MRwaypoints");
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("mrwaypoints", "main"));
    /** Class of Xaero's waypoint menu - by name, so the button just does not appear if Xaero renames it. */
    private static final String XAERO_WAYPOINTS_SCREEN = "xaero.common.gui.GuiWaypoints";

    private static KeyMapping screenKey;
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
        PayloadTypeRegistry.serverboundPlay().register(TogglePayload.TYPE, TogglePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WelcomePayload.TYPE, WelcomePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StatePayload.TYPE, StatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenPayload.TYPE, OpenPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(WelcomePayload.TYPE, (payload, context) -> welcome(payload));
        ClientPlayNetworking.registerGlobalReceiver(StatePayload.TYPE, (payload, context) -> {
            state = payload;
            dirty = true;
        });
        ClientPlayNetworking.registerGlobalReceiver(OpenPayload.TYPE,
                (payload, context) -> context.client().gui.setScreen(new CategoryScreen(null)));

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

        screenKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.mrwaypoints.categories",
                InputConstants.Type.KEYBOARD, InputConstants.KEY_J, CATEGORY, 0));

        // A button in Xaero's waypoint menu opens the category screen (only on servers with MRwaypoints).
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (state() != null && screen.getClass().getName().equals(XAERO_WAYPOINTS_SCREEN)) {
                Screens.getWidgets(screen).add(Button.builder(Component.translatable("mrwaypoints.button.xaero"),
                        button -> client.gui.setScreen(new CategoryScreen(screen)))
                        .bounds(width - 125, 5, 120, 20).build());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (helloSent && status == null && client.getConnection() != null && ++helloTicks % 60 == 0
                    && helloTicks <= 600) {
                LOG.info("No answer from MRwaypoints on the server yet, saying hello again.");
                ClientPlayNetworking.send(hello());
            }
            while (screenKey.consumeClick()) {
                if (state() != null) {
                    client.gui.setScreen(new CategoryScreen(null));
                } else if (client.player != null) {
                    client.player.sendSystemMessage(Component.translatable("mrwaypoints.message.no-server"));
                }
            }
            // Xaero's session can come later than our state, and it changes on every world/server switch.
            StatePayload current = state();
            if (current != null && (dirty || XaeroBridge.needsApply())) {
                dirty = !XaeroBridge.apply(current);
            }
        });
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

    /** Switches a category on or off ("*" = all) - the server saves it and sends the new state. */
    public static void toggle(String category, boolean visible) {
        if (compatible && ClientPlayNetworking.canSend(TogglePayload.TYPE)) {
            ClientPlayNetworking.send(new TogglePayload(category, visible));
        }
    }

    /** The server waypoints, or null without (compatible) MRwaypoints on the server. */
    public static StatePayload state() {
        return compatible ? state : null;
    }
}
