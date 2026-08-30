package de.ole101.mctrafficcontrol.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractTextViewerWidget extends AbstractScrollArea {

    protected static final Supplier<Font> FONT = () -> Minecraft.getInstance().font;

    private static final int PADDING = 10;
    private static final int DEFAULT_WIDTH = 320;
    private static final int DEFAULT_HEIGHT = 240;
    private static final int MIN_WIDTH = 32;
    private static final int MIN_HEIGHT = 24;
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("popup/background");

    private List<FormattedText> lines = List.of();
    private boolean positioned;
    private boolean automaticWidth = true;
    private Drag drag;

    protected AbstractTextViewerWidget(Component title) {
        super(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, title, ScrollbarSettings.NO_SCROLL);
        visible = false;
    }

    public final boolean keyPressed(@NonNull KeyEvent event) {
        if (!visible || !event.isEscape()) {
            return false;
        }

        close();
        return true;
    }

    public final boolean mouseClicked(MouseButtonEvent event) {
        if (!isMouseOver(event.x(), event.y())) {
            return false;
        }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            drag = new Drag(event.button(), getX() - (int) event.x(), getY() - (int) event.y());
        } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            drag = new Drag(event.button(), getRight() - (int) event.x(), getBottom() - (int) event.y());
        }

        return true;
    }

    public final boolean mouseDragged(MouseButtonEvent event) {
        return drag != null;
    }

    public final boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }

        setScrollAmount(scrollAmount() - scrollY * 16);
        return true;
    }

    public final boolean isHovering(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }

    public final void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!visible) {
            return;
        }

        if (!positioned) {
            setX(Math.clamp(mouseX, 0, Math.max(0, graphics.guiWidth() - width)));
            setY(Math.clamp(mouseY, 0, Math.max(0, graphics.guiHeight() - height)));
            positioned = true;
        }

        super.extractRenderState(graphics, mouseX, mouseY, 0);
    }

    public final void close() {
        visible = false;
        positioned = false;
        lines = List.of();
        setRectangle(DEFAULT_WIDTH, DEFAULT_HEIGHT, 0, 0);
        automaticWidth = true;
        drag = null;
        setScrollAmount(0);
    }

    protected final void setLines(List<FormattedText> lines) {
        this.lines = List.copyOf(lines);

        if (automaticWidth) {
            Font font = FONT.get();
            int contentWidth = lines.stream().mapToInt(font::width).max().orElse(MIN_WIDTH);
            setWidth(Math.max(MIN_WIDTH, contentWidth + PADDING * 2));
        }

        setScrollAmount(0);
        visible = true;
    }

    @Override
    protected final void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics,
                                                  int mouseX, int mouseY, float partialTick) {
        Font font = FONT.get();

        if (drag != null && drag.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            setX(Math.clamp(mouseX + drag.offsetX(), 0, Math.max(0, graphics.guiWidth() - width)));
            setY(Math.clamp(mouseY + drag.offsetY(), 0, Math.max(0, graphics.guiHeight() - height)));
        } else if (drag != null && drag.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            setWidth(Math.clamp(mouseX + drag.offsetX() - getX(), MIN_WIDTH,
                    Math.max(MIN_WIDTH, graphics.guiWidth() - getX())));
            setHeight(Math.clamp(mouseY + drag.offsetY() - getY(), MIN_HEIGHT,
                    Math.max(MIN_HEIGHT, graphics.guiHeight() - getY())));
            automaticWidth = false;
            refreshScrollAmount();
        }

        graphics.nextStratum();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, getX(), getY(), width, height);
        graphics.text(font, getMessage(), getX() + PADDING, getY() - font.lineHeight, -1);
        graphics.enableScissor(getX() + 6, getY() + 6, getRight() - 6, getBottom() - 6);

        int firstLine = Math.max(0, (int) Math.floor((scrollAmount() - PADDING) / font.lineHeight));
        int lastLine = Math.min(lines.size() - 1, firstLine + height / font.lineHeight + 1);
        int y = getY() + PADDING - (int) scrollAmount() + font.lineHeight * firstLine;

        for (int i = firstLine; i <= lastLine; i++) {
            FormattedCharSequence text = Language.getInstance().getVisualOrder(lines.get(i));
            graphics.text(font, text, getX() + PADDING, y, -1);
            y += font.lineHeight;
        }

        graphics.disableScissor();
    }

    @Override
    public final boolean mouseReleased(@NonNull MouseButtonEvent event) {
        if (drag == null || event.button() != drag.button()) {
            return false;
        }

        drag = null;
        return true;
    }

    @Override
    protected final void updateWidgetNarration(@NonNull NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @Override
    protected final int contentHeight() {
        return lines.size() * FONT.get().lineHeight + PADDING * 2;
    }

    private record Drag(int button, int offsetX, int offsetY) {}
}
