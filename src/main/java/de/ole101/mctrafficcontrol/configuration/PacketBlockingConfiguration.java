package de.ole101.mctrafficcontrol.configuration;

import com.moulberry.lattice.WidgetFunction;
import com.moulberry.lattice.annotation.LatticeOption;
import com.moulberry.lattice.annotation.widget.LatticeWidgetButton;
import com.moulberry.lattice.annotation.widget.LatticeWidgetCustom;
import lombok.Data;
import net.minecraft.util.Util;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Data
public class PacketBlockingConfiguration {

    private static final URI PACKET_LIST_URI = URI.create(
            "https://minecraft.wiki/w/Java_Edition_protocol/Packets#List_of_packets"
    );

    @LatticeOption(title = "mtc.option.packet_blocking.hint")
    @LatticeWidgetButton
    private transient Runnable openPacketList = () -> Util.getPlatform().openUri(PACKET_LIST_URI);

    @LatticeOption(title = "mtc.option.packet_blocking.incoming_enabled.label",
                   description = "mtc.option.packet_blocking.incoming_enabled.description")
    @LatticeWidgetButton
    private boolean incomingEnabled = false;

    @LatticeOption(title = "mtc.option.packet_blocking.incoming.label",
                   description = "mtc.option.packet_blocking.incoming.description")
    @LatticeWidgetCustom(function = "identifierSetTextArea")
    private Set<String> incomingPackets = new LinkedHashSet<>();

    @LatticeOption(title = "mtc.option.packet_blocking.outgoing_enabled.label",
                   description = "mtc.option.packet_blocking.outgoing_enabled.description")
    @LatticeWidgetButton
    private boolean outgoingEnabled = false;

    @LatticeOption(title = "mtc.option.packet_blocking.outgoing.label",
                   description = "mtc.option.packet_blocking.outgoing.description")
    @LatticeWidgetCustom(function = "identifierSetTextArea")
    private Set<String> outgoingPackets = new LinkedHashSet<>();

    @SuppressWarnings({ "rawtypes", "unused" })
    private static WidgetFunction identifierSetTextArea(Supplier<Set> getter, Consumer<Set> setter) {
        return WidgetFunction.multilineEditBox(
                () -> ((Set<?>) getter.get()).stream()
                        .map(Object::toString)
                        .collect(Collectors.joining("\n")),
                text -> {
                    Set<String> identifiers = text.lines()
                            .map(String::trim)
                            .filter(identifier -> !identifier.isEmpty())
                            .collect(Collectors.toCollection(LinkedHashSet::new));

                    setter.accept(identifiers);
                },
                80,
                Integer.MAX_VALUE
        );
    }
}
