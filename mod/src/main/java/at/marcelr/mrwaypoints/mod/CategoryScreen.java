package at.marcelr.mrwaypoints.mod;

import at.marcelr.mrwaypoints.mod.Protocol.Category;
import at.marcelr.mrwaypoints.mod.Protocol.StatePayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * The server's categories with a switch each (shown in Xaero or not), "all on" / "all off" and done. The switches go
 * to the server, which saves them for the player and answers with the new state - the screen rebuilds then.
 */
final class CategoryScreen extends Screen {

    private static final int BUTTON_WIDTH = 220;
    private static final int ROW = 24;

    private final Screen parent;
    /** The state the buttons were built from - a newer one rebuilds them. */
    private StatePayload shown;

    CategoryScreen(Screen parent) {
        super(Component.translatable("mrwaypoints.screen.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        shown = MRwaypointsClient.state();
        if (shown == null) {
            onClose();
            return;
        }
        List<Category> categories = shown.categories();
        int left = width / 2 - BUTTON_WIDTH / 2;
        int top = Math.max(40, height / 2 - (categories.size() * ROW + 2 * ROW) / 2);
        int y = top;
        for (Category category : categories) {
            long count = shown.waypoints().stream().filter(w -> w.category().equals(category.id())).count();
            Component label = Component.literal("■ ").withColor(category.color())
                    .append(Component.literal(category.name()).withColor(0xFFFFFF))
                    .append(Component.literal(" (" + count + ")").withColor(0xA0A0A0))
                    .append(Component.literal(": "))
                    .append(Component.translatable(category.visible() ? "options.on" : "options.off")
                            .withColor(category.visible() ? 0x55FF55 : 0xFF5555));
            addRenderableWidget(Button.builder(label,
                    button -> MRwaypointsClient.toggle(category.id(), !category.visible()))
                    .bounds(left, y, BUTTON_WIDTH, 20).build());
            y += ROW;
        }
        y += 6;
        int half = (BUTTON_WIDTH - 4) / 2;
        addRenderableWidget(Button.builder(Component.translatable("mrwaypoints.screen.all-on"),
                button -> MRwaypointsClient.toggle("*", true)).bounds(left, y, half, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("mrwaypoints.screen.all-off"),
                button -> MRwaypointsClient.toggle("*", false)).bounds(left + half + 4, y, half, 20).build());
        y += ROW;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left, y, BUTTON_WIDTH, 20).build());
    }

    @Override
    public void tick() {
        // The server answered a switch: show the new state.
        StatePayload current = MRwaypointsClient.state();
        if (current != shown) {
            rebuildWidgets();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 15, 0xFFFFFFFF);
        graphics.centeredText(font, Component.translatable("mrwaypoints.screen.hint"), width / 2, 27, 0xFFA0A0A0);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}
