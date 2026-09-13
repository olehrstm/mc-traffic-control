package de.ole101.mctrafficcontrol.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class ComponentViewerWidget extends AbstractTextViewerWidget {

    private final List<Consumer<List<FormattedText>>> blocks = new ArrayList<>();

    public ComponentViewerWidget() {
        super(Component.translatable("mtc.component_viewer.title"));
    }

    public void setItemStack(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            close();
            return;
        }

        clearDisplayTexts();
        blocks.clear();

        var registryAccess = Minecraft.getInstance().player.registryAccess();
        var nbtOps = RegistryOps.create(NbtOps.INSTANCE, registryAccess);
        var patch = itemStack.getComponentsPatch();
        Set<DataComponentType<?>> patchedComponents = new HashSet<>();
        if (!patch.isEmpty()) {
            addHeader("mtc.component_viewer.patched");
        }

        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
            DataComponentType<?> componentType = entry.getKey();

            if (entry.getValue().isEmpty()) {
                addText(Component.literal(componentType.toString()).withColor(0xE5C17C));
                addText(Component.translatable("mtc.component_viewer.removed").withStyle(ChatFormatting.RED));
                addText(FormattedText.EMPTY);
                continue;
            }

            TypedDataComponent<?> component = itemStack.getTyped(componentType);
            if (component != null) {
                addLinesForDataComponent(component, nbtOps);
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
                addHeader("mtc.component_viewer.default");
            }

            addLinesForDataComponent(component, nbtOps);
        }

        setLines(layoutLines());
    }

    @Override
    protected void displayTextsChanged() {
        replaceLines(layoutLines());
    }

    @Override
    protected void onClosed() {
        blocks.clear();
    }

    private List<FormattedText> layoutLines() {
        List<FormattedText> lines = new ArrayList<>();
        blocks.forEach(block -> block.accept(lines));
        return lines;
    }

    private void addText(FormattedText text) {
        blocks.add(lines -> lines.add(text));
    }

    private void addHeader(String translationKey) {
        addText(Component.translatable(translationKey).withColor(0xFFFFFF).withStyle(ChatFormatting.BOLD));
        addText(FormattedText.EMPTY);
    }

    private void addLinesForDataComponent(TypedDataComponent<?> component, RegistryOps<Tag> nbtOps) {
        addText(Component.literal(component.type().toString()).withColor(0xE5C17C));

        component.encodeValue(nbtOps).resultOrPartial(error -> addText(
                Component.translatable("mtc.component_viewer.encoding_error", error).withStyle(ChatFormatting.RED)
        )).ifPresent(encoded -> {
            NbtTree.Node node = NbtTree.node(null, encoded, 0, false, false, null, this::displayText);
            blocks.add(lines -> node.appendLines((line, owner) -> lines.add(line)));
        });
        addText(FormattedText.EMPTY);
    }
}
