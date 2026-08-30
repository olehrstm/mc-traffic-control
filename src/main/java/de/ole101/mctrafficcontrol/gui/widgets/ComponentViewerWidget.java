package de.ole101.mctrafficcontrol.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ComponentViewerWidget extends AbstractTextViewerWidget {

    private static final int MAX_LINE_WIDTH = 512;

    public ComponentViewerWidget() {
        super(Component.translatable("mtc.component_viewer.title"));
    }

    public void setItemStack(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            close();
            return;
        }

        List<FormattedText> componentLines = new ArrayList<>();
        var registryAccess = Minecraft.getInstance().player.registryAccess();
        var nbtOps = RegistryOps.create(NbtOps.INSTANCE, registryAccess);
        var patch = itemStack.getComponentsPatch();
        Set<DataComponentType<?>> patchedComponents = new HashSet<>();
        boolean addPatchedHeader = true;

        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
            DataComponentType<?> componentType = entry.getKey();

            if (addPatchedHeader) {
                addPatchedHeader = false;
                addHeader(componentLines, "mtc.component_viewer.patched");
            }

            if (entry.getValue().isEmpty()) {
                componentLines.add(Component.literal(componentType.toString()).withColor(0xE5C17C));
                componentLines.add(Component.translatable("mtc.component_viewer.removed").withStyle(ChatFormatting.RED));
                componentLines.add(FormattedText.EMPTY);
                continue;
            }

            TypedDataComponent<?> component = itemStack.getTyped(componentType);
            if (component != null) {
                addLinesForDataComponent(componentLines, component, nbtOps);
                patchedComponents.add(componentType);
            }
        }

        boolean addDefaultHeader = !patch.isEmpty();
        for (TypedDataComponent<?> component : itemStack.getComponents()) {
            if (patchedComponents.contains(component.type())) {
                continue;
            }

            if (addDefaultHeader) {
                addDefaultHeader = false;
                addHeader(componentLines, "mtc.component_viewer.default");
            }

            addLinesForDataComponent(componentLines, component, nbtOps);
        }

        setLines(componentLines);
    }

    private void addHeader(List<FormattedText> componentLines, String translationKey) {
        componentLines.add(Component.translatable(translationKey).withColor(0xFFFFFF).withStyle(ChatFormatting.BOLD));
        componentLines.add(FormattedText.EMPTY);
    }

    private void addLinesForDataComponent(List<FormattedText> componentLines,
                                          TypedDataComponent<?> component,
                                          RegistryOps<Tag> nbtOps) {
        componentLines.add(Component.literal(component.type().toString()).withColor(0xE5C17C));

        component.encodeValue(nbtOps).resultOrPartial(error -> componentLines.add(
                Component.translatable("mtc.component_viewer.encoding_error", error).withStyle(ChatFormatting.RED)
        )).ifPresent(encoded -> {
            Component formattedData = new TextComponentTagVisitor("  ").visit(encoded);
            componentLines.addAll(FONT.get().getSplitter().splitLines(formattedData, MAX_LINE_WIDTH, Style.EMPTY));
        });
        componentLines.add(FormattedText.EMPTY);
    }
}
