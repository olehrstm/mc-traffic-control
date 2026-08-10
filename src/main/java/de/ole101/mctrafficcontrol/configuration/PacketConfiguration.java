package de.ole101.mctrafficcontrol.configuration;

import com.moulberry.lattice.WidgetFunction;
import com.moulberry.lattice.annotation.LatticeCategory;
import com.moulberry.lattice.annotation.LatticeOption;
import com.moulberry.lattice.annotation.widget.LatticeWidgetButton;
import com.moulberry.lattice.annotation.widget.LatticeWidgetCustom;
import com.moulberry.lattice.annotation.widget.LatticeWidgetTextField;
import lombok.Data;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.util.Util;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Data
public class PacketConfiguration {

    @LatticeCategory(name = "mtc.category.packet.packet_blocking")
    public PacketBlocking packetBlocking = new PacketBlocking();
    @LatticeCategory(name = "mtc.category.packet.payload_channel_blocking")
    public PayloadChannelBlocking payloadChannelBlocking = new PayloadChannelBlocking();
    @LatticeCategory(name = "mtc.category.packet.brand_override")
    public BrandOverride brandOverride = new BrandOverride();

    @SuppressWarnings({ "rawtypes", "unused" })
    private static WidgetFunction channelSetTextArea(Supplier<Set> getter, Consumer<Set> setter) {
        return WidgetFunction.multilineEditBox(
                () -> ((Set<?>) getter.get()).stream()
                        .map(Object::toString)
                        .collect(Collectors.joining("\n")),
                text -> {
                    Set<String> channels = text.lines()
                            .map(String::trim)
                            .filter(channel -> !channel.isEmpty())
                            .collect(Collectors.toCollection(LinkedHashSet::new));

                    setter.accept(channels);
                },
                80,
                Integer.MAX_VALUE
        );
    }

    @Data
    public static class PacketBlocking {

        @LatticeOption(title = "mtc.option.packet.packet_blocking.hint")
        @LatticeWidgetButton
        private transient Runnable openWiki = () -> {
            Component.empty()
                    .append(Component.translatable("mtc.option.packet.packet_blocking.hint"))
                    .append(Component.literal(" ↗").withStyle(ChatFormatting.AQUA))
                    .withStyle(style -> style
                            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("mtc.option.packet.packet_blocking.hint_hover")))
                            .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://minecraft.wiki/w/Java_Edition_protocol/Packets#List_of_packets"))));

            Util.getPlatform().openUri(URI.create("https://minecraft.wiki/w/Java_Edition_protocol/Packets#List_of_packets"));
        };

        @LatticeOption(title = "mtc.option.packet.packet_blocking_incoming_enabled.label",
                       description = "mtc.option.packet.packet_blocking_incoming_enabled.description")
        @LatticeWidgetButton
        private boolean incomingEnabled = false;

        @LatticeOption(title = "mtc.option.packet.packet_blocking_incoming.label",
                       description = "mtc.option.packet.packet_blocking_incoming.description")
        @LatticeWidgetCustom(function = "channelSetTextArea")
        private Set<String> incomingPackets = new LinkedHashSet<>();

        @LatticeOption(title = "mtc.option.packet.packet_blocking_outgoing_enabled.label",
                       description = "mtc.option.packet.packet_blocking_outgoing_enabled.description")
        @LatticeWidgetButton
        private boolean outgoingEnabled = false;

        @LatticeOption(title = "mtc.option.packet.packet_blocking_outgoing.label",
                       description = "mtc.option.packet.packet_blocking_outgoing.description")
        @LatticeWidgetCustom(function = "channelSetTextArea")
        private Set<String> outgoingPackets = new LinkedHashSet<>();
    }

    @Data
    public static class PayloadChannelBlocking {

        @LatticeOption(title = "mtc.option.packet.payload_channel_blocking_incoming_enabled.label",
                       description = "mtc.option.packet.payload_channel_blocking_incoming_enabled.description")
        @LatticeWidgetButton
        private boolean incomingEnabled = false;

        @LatticeOption(title = "mtc.option.packet.payload_channel_blocking_incoming.label",
                       description = "mtc.option.packet.payload_channel_blocking_incoming.description")
        @LatticeWidgetCustom(function = "channelSetTextArea")
        private Set<String> incomingChannels = new LinkedHashSet<>();

        @LatticeOption(title = "mtc.option.packet.payload_channel_blocking_outgoing_enabled.label",
                       description = "mtc.option.packet.payload_channel_blocking_outgoing_enabled.description")
        @LatticeWidgetButton
        private boolean outgoingEnabled = false;

        @LatticeOption(title = "mtc.option.packet.payload_channel_blocking_outgoing.label",
                       description = "mtc.option.packet.payload_channel_blocking_outgoing.description")
        @LatticeWidgetCustom(function = "channelSetTextArea")
        private Set<String> outgoingChannels = new LinkedHashSet<>();
    }

    @Data
    public static class BrandOverride {

        @LatticeOption(title = "mtc.option.packet.brand_override_enabled.label",
                       description = "mtc.option.packet.brand_override_enabled.description")
        @LatticeWidgetButton
        private boolean enabled = false;

        @LatticeOption(title = "mtc.option.packet.brand_override.label",
                       description = "mtc.option.packet.brand_override.description")
        @LatticeWidgetTextField(characterLimit = 16)
        private String override = "vanilla";
    }
}
