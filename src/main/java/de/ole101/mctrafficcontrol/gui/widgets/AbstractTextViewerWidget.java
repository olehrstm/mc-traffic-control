package de.ole101.mctrafficcontrol.gui.widgets;

import de.ole101.mctrafficcontrol.gui.font.CustomGlyphs;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractTextViewerWidget extends AbstractScrollArea {

    protected static final Supplier<Font> FONT = () -> Minecraft.getInstance().font;

    private static final int PADDING = 10;
    private static final int CLIP_INSET = 6;
    private static final int DEFAULT_WIDTH = 320;
    private static final int DEFAULT_HEIGHT = 240;
    private static final int MIN_WIDTH = 32;
    private static final int MIN_HEIGHT = 24;
    private static final double CLICK_TOLERANCE = 2;
    private static final float MIN_GLYPH_HIT_WIDTH = 4;
    private static final String GLYPH_INSERTION_PREFIX = "mtc$glyph:";
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("popup/background");

    private final Map<Integer, DisplayText> displayTexts = new HashMap<>();
    private int nextDisplayTextId;
    private List<FormattedText> lines = new ArrayList<>();
    private boolean positioned;
    private boolean automaticWidth = true;
    private Drag drag;

    protected AbstractTextViewerWidget(Component title) {
        super(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, title, ScrollbarSettings.NO_SCROLL);
        visible = false;
    }

    public final boolean keyPressed(@NonNull KeyEvent event) {
        if (!visible || !event.isEscape() || !closesOnEscape()) {
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
            drag = new Drag(event.button(), getX() - (int) event.x(), getY() - (int) event.y(), event.x(), event.y());
        } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            drag = new Drag(event.button(), getRight() - (int) event.x(), getBottom() - (int) event.y(), event.x(), event.y());
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
        lines = new ArrayList<>();
        setRectangle(DEFAULT_WIDTH, DEFAULT_HEIGHT, 0, 0);
        automaticWidth = true;
        drag = null;
        setScrollAmount(0);
        clearDisplayTexts();
        onClosed();
    }

    protected final void setLines(List<FormattedText> lines) {
        this.lines = new ArrayList<>(lines);

        updateAutomaticWidth(lines, MIN_WIDTH);

        setScrollAmount(0);
        visible = true;
    }

    protected final void replaceLines(List<FormattedText> lines) {
        this.lines = new ArrayList<>(lines);

        updateAutomaticWidth(lines, width);

        refreshScrollAmount();
    }

    protected final void addLines(List<FormattedText> lines) {
        this.lines.addAll(lines);

        updateAutomaticWidth(lines, width);

        visible = true;
    }

    protected final DisplayText displayText(FormattedText source, int maxWidth, String continuationIndent) {
        DisplayText text = new DisplayText(nextDisplayTextId++, source, maxWidth, Component.literal(continuationIndent));
        displayTexts.put(text.id, text);
        return text;
    }

    protected final void clearDisplayTexts() {
        displayTexts.clear();
    }

    protected void lineClicked(int index, boolean shiftDown) {}

    protected void displayTextsChanged() {}

    protected void onClosed() {}

    protected boolean closesOnEscape() {
        return true;
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
        graphics.enableScissor(getX() + CLIP_INSET, getY() + CLIP_INSET, getRight() - CLIP_INSET, getBottom() - CLIP_INSET);

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

        Drag released = drag;
        drag = null;

        if (released.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && Math.abs(event.x() - released.startX()) < CLICK_TOLERANCE
                && Math.abs(event.y() - released.startY()) < CLICK_TOLERANCE) {
            int index = lineAt(event.y());
            if (index >= 0 && !toggleGlyphAt(lines.get(index), event.x() - getX() - PADDING)) {
                lineClicked(index, event.hasShiftDown());
            }
        }

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

    private void updateAutomaticWidth(List<FormattedText> lines, int minimumWidth) {
        if (automaticWidth) {
            Font font = FONT.get();
            int contentWidth = lines.stream().mapToInt(font::width).max().orElse(MIN_WIDTH);
            setWidth(Math.max(minimumWidth, contentWidth + PADDING * 2));
        }
    }

    private int lineAt(double mouseY) {
        if (mouseY < getY() + CLIP_INSET || mouseY >= getBottom() - CLIP_INSET) {
            return -1;
        }

        int index = (int) Math.floor((mouseY - getY() - PADDING + scrollAmount()) / FONT.get().lineHeight);
        return index >= 0 && index < lines.size() ? index : -1;
    }

    private boolean toggleGlyphAt(FormattedText line, double x) {
        String insertion = insertionAt(line, x);
        if (insertion == null || !insertion.startsWith(GLYPH_INSERTION_PREFIX)) {
            return false;
        }

        String[] parts = insertion.substring(GLYPH_INSERTION_PREFIX.length()).split(":");
        DisplayText text = displayTexts.get(Integer.parseInt(parts[0]));
        if (text == null) {
            return false;
        }

        text.toggle(Integer.parseInt(parts[1]));
        displayTextsChanged();
        return true;
    }

    private static String unicodeEscape(int codepoint) {
        return Character.isBmpCodePoint(codepoint) ? "\\u%04X".formatted(codepoint) : "\\U%08X".formatted(codepoint);
    }

    // negative spacing can overlap glyphs
    private static @Nullable String insertionAt(FormattedText line, double x) {
        StringSplitter splitter = FONT.get().getSplitter();
        float[] offset = { 0 };
        String[] hit = { null };

        line.visit((style, content) -> {
            float min = offset[0];
            float max = offset[0];

            for (int i = 0; i < content.length(); i = content.offsetByCodePoints(i, 1)) {
                offset[0] += splitter.stringWidth(FormattedText.of(Character.toString(content.codePointAt(i)), style));
                min = Math.min(min, offset[0]);
                max = Math.max(max, offset[0]);
            }

            if (style.getInsertion() != null && x >= min && x < Math.max(max, min + MIN_GLYPH_HIT_WIDTH)) {
                hit[0] = style.getInsertion();
            }
            return Optional.empty();
        }, Style.EMPTY);

        return hit[0];
    }

    private record Drag(int button, int offsetX, int offsetY, double startX, double startY) {}

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    protected static class DisplayText {

        private final int id;
        private final FormattedText source;
        private final int maxWidth;
        private final Component continuationIndent;
        // source positions keep toggle state stable when text wraps differently
        private final Set<Integer> renderedRuns = new HashSet<>();
        private List<FormattedText> lines;

        public List<FormattedText> lines() {
            if (lines == null) {
                List<FormattedText> split = FONT.get().getSplitter().splitLines(render(), maxWidth, Style.EMPTY);
                lines = new ArrayList<>(split.size());
                for (int i = 0; i < split.size(); i++) {
                    lines.add(i == 0 ? split.get(i) : FormattedText.composite(continuationIndent, split.get(i)));
                }
            }

            return lines;
        }

        private void toggle(int position) {
            if (!renderedRuns.add(position)) {
                renderedRuns.remove(position);
            }
            lines = null;
        }

        private FormattedText render() {
            List<FormattedText> parts = new ArrayList<>();
            int[] position = { 0 };

            source.visit((style, content) -> {
                Style plainStyle = style.withFont(FontDescription.DEFAULT);
                StringBuilder plain = new StringBuilder();
                StringBuilder run = new StringBuilder();
                int runStart = -1;
                FontDescription runFont = null;

                for (int codepoint : content.codePoints().toArray()) {
                    int index = position[0]++;
                    FontDescription textureFont = codepoint == '\n' ? null : CustomGlyphs.customTextureFont(codepoint, style.getFont());

                    // keep adjacent glyphs together
                    if (runStart >= 0 && !Objects.equals(textureFont, runFont)) {
                        addRun(parts, run, runStart, runFont, plainStyle);
                        runStart = -1;
                    }

                    if (textureFont == null) {
                        if (codepoint == '\n' || CustomGlyphs.hasGlyph(Minecraft.DEFAULT_FONT, codepoint)) {
                            plain.appendCodePoint(codepoint);
                        } else {
                            plain.append(unicodeEscape(codepoint));
                        }
                        continue;
                    }

                    if (runStart < 0) {
                        flush(parts, plain, plainStyle);
                        runStart = index;
                        runFont = textureFont;
                    }
                    run.appendCodePoint(codepoint);
                }

                if (runStart >= 0) {
                    addRun(parts, run, runStart, runFont, plainStyle);
                }
                flush(parts, plain, plainStyle);
                return Optional.empty();
            }, Style.EMPTY);

            return FormattedText.composite(parts);
        }

        private void addRun(List<FormattedText> parts,
                            StringBuilder run,
                            int runStart,
                            FontDescription textureFont,
                            Style plainStyle) {
            Style glyphStyle = plainStyle.withUnderlined(true)
                    .withInsertion(GLYPH_INSERTION_PREFIX + id + ":" + runStart);
            String characters = run.toString();

            parts.add(renderedRuns.contains(runStart)
                    ? FormattedText.of(characters, glyphStyle.withFont(textureFont))
                    : FormattedText.of(characters.codePoints()
                    .mapToObj(AbstractTextViewerWidget::unicodeEscape)
                    .collect(Collectors.joining()), glyphStyle));
            run.setLength(0);
        }

        private static void flush(List<FormattedText> parts, StringBuilder plain, Style style) {
            if (!plain.isEmpty()) {
                parts.add(FormattedText.of(plain.toString(), style));
                plain.setLength(0);
            }
        }
    }
}
