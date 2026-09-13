package de.ole101.mctrafficcontrol.gui.widgets;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ContainerPacketWidget extends AbstractTextViewerWidget {

    private final List<LoggedPacket> packets = new ArrayList<>();
    private final List<NbtTree.Foldable> lineOwners = new ArrayList<>();
    private boolean collapseNewPackets;

    public ContainerPacketWidget() {
        super(Component.translatable("mtc.container_packet_viewer.title"));
    }

    public void toggle() {
        if (!visible) {
            open();
        } else if (isHovered) {
            // require hovering to avoid closing the log while switching screens
            close();
        }
    }

    public void open() {
        if (!visible) {
            setLines(layoutLines());
        }
    }

    public void addPacket(boolean outgoing, Packet<?> packet) {
        if (packet == null) {
            close();
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (!visible || player == null) {
            return;
        }

        List<FormattedText> errors = new ArrayList<>();
        var nbtOps = RegistryOps.create(NbtOps.INSTANCE, player.registryAccess());
        CompoundTag fields = new CompoundTag();

        switch (packet) {
            case ClientboundContainerClosePacket containerClose -> fields.putInt("containerId", containerClose.getContainerId());
            case ClientboundContainerSetContentPacket containerSetContent -> {
                fields.putInt("containerId", containerSetContent.containerId());
                fields.putInt("stateId", containerSetContent.stateId());
                putEncoded(errors, fields, "items", ItemStack.OPTIONAL_CODEC.listOf(), nbtOps, containerSetContent.items());
                putEncoded(errors, fields, "carriedItem", ItemStack.OPTIONAL_CODEC, nbtOps, containerSetContent.carriedItem());
            }
            case ClientboundContainerSetDataPacket containerSetData -> {
                fields.putInt("containerId", containerSetData.getContainerId());
                fields.putInt("id", containerSetData.getId());
                fields.putInt("value", containerSetData.getValue());
            }
            case ClientboundContainerSetSlotPacket containerSetSlot -> {
                fields.putInt("containerId", containerSetSlot.getContainerId());
                fields.putInt("stateId", containerSetSlot.getStateId());
                fields.putInt("slot", containerSetSlot.getSlot());
                putEncoded(errors, fields, "item", ItemStack.OPTIONAL_CODEC, nbtOps, containerSetSlot.getItem());
            }
            case ClientboundOpenScreenPacket openScreen -> {
                fields.putInt("containerId", openScreen.getContainerId());
                fields.putString("type", String.valueOf(BuiltInRegistries.MENU.getKey(openScreen.getType())));
                putEncoded(errors, fields, "title", ComponentSerialization.CODEC, nbtOps, openScreen.getTitle());
            }
            case ServerboundContainerButtonClickPacket buttonClick -> {
                fields.putInt("containerId", buttonClick.containerId());
                fields.putInt("buttonId", buttonClick.buttonId());
            }
            case ServerboundContainerClickPacket containerClick -> {
                fields.putInt("containerId", containerClick.containerId());
                fields.putInt("stateId", containerClick.stateId());
                fields.putShort("slotNum", containerClick.slotNum());
                fields.putByte("buttonNum", containerClick.buttonNum());
                fields.putString("containerInput", String.valueOf(containerClick.containerInput()));
            }
            case ServerboundContainerClosePacket containerClose -> fields.putInt("containerId", containerClose.getContainerId());
            case ServerboundContainerSlotStateChangedPacket slotStateChanged -> {
                fields.putInt("containerId", slotStateChanged.containerId());
                fields.putInt("slotId", slotStateChanged.slotId());
                fields.putBoolean("newState", slotStateChanged.newState());
            }
            default -> {
                return;
            }
        }

        Component header = Component.literal(outgoing ? "=> C2S " : "<= S2C ").withColor(outgoing ? 0x99FF99 : 0xFF9999)
                .append(Component.literal(packet.type().id().toString()).withColor(0xE5C17C));

        LoggedPacket loggedPacket = new LoggedPacket(header, errors,
                NbtTree.children(fields, 1, true, null, this::displayText), collapseNewPackets);
        packets.add(loggedPacket);

        if (packets.size() == 1) {
            replaceLines(layoutLines());
            return;
        }

        List<FormattedText> packetLines = new ArrayList<>();
        appendPacketLines(packetLines, loggedPacket);
        addLines(packetLines);
    }

    @Override
    protected void lineClicked(int index, boolean shiftDown) {
        NbtTree.Foldable clicked = lineOwners.get(index);
        switch (clicked) {
            case null -> {
                return;
            }
            case LoggedPacket packet when shiftDown -> {
                collapseNewPackets = !packet.collapsed;
                packets.forEach(loggedPacket -> loggedPacket.collapsed = collapseNewPackets);
            }
            case NbtTree.Node node when shiftDown -> node.setCollapsedRecursively(!node.collapsed);
            default -> clicked.collapsed = !clicked.collapsed;
        }

        replaceLines(layoutLines());
    }

    @Override
    protected void displayTextsChanged() {
        replaceLines(layoutLines());
    }

    @Override
    protected void onClosed() {
        packets.clear();
        lineOwners.clear();
        collapseNewPackets = false;
    }

    @Override
    protected boolean closesOnEscape() {
        return false;
    }

    private List<FormattedText> layoutLines() {
        List<FormattedText> lines = new ArrayList<>();
        lineOwners.clear();

        if (packets.isEmpty()) {
            addLine(lines, Component.translatable("mtc.container_packet_viewer.waiting").withStyle(ChatFormatting.GRAY), null);
        }

        for (LoggedPacket packet : packets) {
            appendPacketLines(lines, packet);
        }

        return lines;
    }

    private void appendPacketLines(List<FormattedText> lines, LoggedPacket packet) {
        addLine(lines, Component.literal(packet.collapsed ? "▶ " : "▼ ").withStyle(ChatFormatting.GRAY).append(packet.header), packet);

        if (packet.collapsed) {
            return;
        }

        packet.errors.forEach(line -> addLine(lines, line, null));
        packet.nodes.forEach(node -> node.appendLines((line, owner) -> addLine(lines, line, owner)));
        addLine(lines, FormattedText.EMPTY, null);
    }

    private void addLine(List<FormattedText> lines, FormattedText line, NbtTree.@Nullable Foldable owner) {
        lines.add(line);
        lineOwners.add(owner);
    }

    private static <T> void putEncoded(List<FormattedText> errors,
                                       CompoundTag fields,
                                       String name,
                                       Codec<T> codec,
                                       RegistryOps<Tag> nbtOps,
                                       T value) {
        codec.encodeStart(nbtOps, value).resultOrPartial(error -> errors.add(
                Component.translatable("mtc.container_packet_viewer.encoding_error", name, error).withStyle(ChatFormatting.RED)
        )).ifPresent(encoded -> fields.put(name, encoded));
    }

    private static class LoggedPacket extends NbtTree.Foldable {

        private final Component header;
        private final List<FormattedText> errors;
        private final List<NbtTree.Node> nodes;

        private LoggedPacket(Component header, List<FormattedText> errors, List<NbtTree.Node> nodes, boolean collapsed) {
            this.header = header;
            this.errors = errors;
            this.nodes = nodes;
            this.collapsed = collapsed;
        }
    }
}
