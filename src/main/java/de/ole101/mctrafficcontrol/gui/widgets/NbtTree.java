package de.ole101.mctrafficcontrol.gui.widgets;

import de.ole101.mctrafficcontrol.gui.widgets.AbstractTextViewerWidget.DisplayText;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

class NbtTree {

    private static final int MAX_LINE_WIDTH = 512;
    private static final String INDENT = "  ";
    private static final Pattern SIMPLE_KEY = Pattern.compile("[A-Za-z0-9._+-]+");

    static List<Node> children(Tag tag, int depth, boolean foldable, @Nullable FontDescription font, DisplayTextFactory factory) {
        List<Node> nodes = new ArrayList<>();

        switch (tag) {
            case CompoundTag compound -> {
                FontDescription childFont = fontOf(compound, font);
                List<String> keys = new ArrayList<>(compound.keySet());
                Collections.sort(keys);

                for (int i = 0; i < keys.size(); i++) {
                    String key = keys.get(i);
                    nodes.add(node(formatKey(key), compound.get(key), depth, i < keys.size() - 1, foldable,
                            key.equals("font") ? font : childFont, factory));
                }
            }
            case ListTag list -> {
                for (int i = 0; i < list.size(); i++) {
                    nodes.add(node(null, list.get(i), depth, i < list.size() - 1, foldable, font, factory));
                }
            }
            default -> {
            }
        }

        return nodes;
    }

    static Node node(@Nullable Component key,
                     Tag tag,
                     int depth,
                     boolean trailingComma,
                     boolean foldable,
                     @Nullable FontDescription font,
                     DisplayTextFactory factory) {
        String indent = INDENT.repeat(depth);
        String arrowPadding = foldable ? arrowPadding() : "";
        String continuationIndent = INDENT.repeat(depth + 1) + arrowPadding;
        String separator = trailingComma ? "," : "";

        MutableComponent keyPart = Component.empty();
        if (key != null) {
            keyPart.append(key).append(": ");
        }

        int size = switch (tag) {
            case CompoundTag compound -> compound.size();
            case ListTag list -> list.size();
            default -> 0;
        };

        if (size == 0) {
            MutableComponent value = Component.empty();
            if (font != null) {
                value.withStyle(Style.EMPTY.withFont(font));
            }
            value.append(new TextComponentTagVisitor("").visit(tag));

            MutableComponent line = Component.literal(indent + arrowPadding).append(keyPart).append(value).append(separator);
            return new Node(factory.create(line, MAX_LINE_WIDTH, continuationIndent), null, null, List.of());
        }

        String open = tag instanceof CompoundTag ? "{" : "[";
        String close = tag instanceof CompoundTag ? "}" : "]";

        MutableComponent openPrefix = Component.literal(indent);
        MutableComponent collapsedPrefix = Component.literal(indent);
        if (foldable) {
            openPrefix.append(Component.literal("▼ ").withStyle(ChatFormatting.GRAY));
            collapsedPrefix.append(Component.literal("▶ ").withStyle(ChatFormatting.GRAY));
        }

        MutableComponent openLine = keyPart.copy().append(open);
        MutableComponent collapsedLine = keyPart.copy()
                .append(open)
                .append(Component.literal("...").withStyle(ChatFormatting.GRAY))
                .append(close).append(separator).append(" ")
                .append(Component.translatable("mtc.container_packet_viewer.entries", size).withStyle(ChatFormatting.GRAY));

        return new Node(
                factory.create(openPrefix, openLine, MAX_LINE_WIDTH, continuationIndent),
                foldable ? factory.create(collapsedPrefix, collapsedLine, MAX_LINE_WIDTH, continuationIndent) : null,
                factory.create(Component.literal(indent + arrowPadding + close + separator), MAX_LINE_WIDTH, continuationIndent),
                children(tag, depth + 1, foldable, font, factory)
        );
    }

    private static @Nullable FontDescription fontOf(CompoundTag compound, @Nullable FontDescription inherited) {
        if (compound.get("font") instanceof StringTag(String value)) {
            Identifier fontId = Identifier.tryParse(value);
            if (fontId != null) {
                return new FontDescription.Resource(fontId);
            }
        }

        return inherited;
    }

    private static String arrowPadding() {
        Font font = AbstractTextViewerWidget.FONT.get();
        return " ".repeat(Math.max(1, Math.round((float) font.width("▼ ") / font.width(" "))));
    }

    private static Component formatKey(String key) {
        if (SIMPLE_KEY.matcher(key).matches()) {
            return Component.literal(key).withStyle(ChatFormatting.AQUA);
        }

        String quoted = StringTag.quoteAndEscape(key);
        String quote = quoted.substring(0, 1);
        return Component.literal(quote)
                .append(Component.literal(quoted.substring(1, quoted.length() - 1)).withStyle(ChatFormatting.AQUA))
                .append(quote);
    }

    @FunctionalInterface
    interface DisplayTextFactory {

        DisplayText create(FormattedText prefix, FormattedText source, int maxWidth, String continuationIndent);

        default DisplayText create(FormattedText source, int maxWidth, String continuationIndent) {
            return create(FormattedText.EMPTY, source, maxWidth, continuationIndent);
        }
    }

    @FunctionalInterface
    interface LineConsumer {

        void accept(FormattedText line, @Nullable Foldable owner);
    }

    abstract static class Foldable {

        boolean collapsed;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static class Node extends Foldable {

        private final DisplayText lines;
        private final @Nullable DisplayText collapsedLines;
        private final @Nullable DisplayText closeLines;
        private final List<Node> children;

        void setCollapsedRecursively(boolean collapsed) {
            if (isFoldable()) {
                this.collapsed = collapsed;
            }
            children.forEach(child -> child.setCollapsedRecursively(collapsed));
        }

        void appendLines(LineConsumer consumer) {
            if (collapsed && collapsedLines != null) {
                collapsedLines.lines().forEach(line -> consumer.accept(line, this));
                return;
            }

            Foldable owner = isFoldable() ? this : null;
            lines.lines().forEach(line -> consumer.accept(line, owner));
            children.forEach(child -> child.appendLines(consumer));
            if (closeLines != null) {
                closeLines.lines().forEach(line -> consumer.accept(line, owner));
            }
        }

        private boolean isFoldable() {
            return collapsedLines != null;
        }
    }
}
