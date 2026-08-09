package de.ole101.mctrafficcontrol.configuration;

import com.moulberry.lattice.WidgetFunction;
import com.moulberry.lattice.annotation.LatticeCategory;
import com.moulberry.lattice.annotation.LatticeOption;
import com.moulberry.lattice.annotation.widget.LatticeWidgetButton;
import com.moulberry.lattice.annotation.widget.LatticeWidgetCustom;
import com.moulberry.lattice.annotation.widget.LatticeWidgetTextField;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Data
public class PayloadConfiguration {

    @LatticeCategory(name = "mtc.category.payload.channel_blocking")
    public ChannelBlocking channelBlocking = new ChannelBlocking();
    @LatticeCategory(name = "mtc.category.payload.brand_override")
    public BrandOverride brandOverride = new BrandOverride();

    @Data
    public static class ChannelBlocking {

        @LatticeOption(title = "mtc.option.payload.channel_blocking_incoming_enabled.label",
                       description = "mtc.option.payload.channel_blocking_incoming_enabled.description")
        @LatticeWidgetButton
        private boolean incomingEnabled = false;

        @LatticeOption(title = "mtc.option.payload.channel_blocking_incoming.label",
                       description = "mtc.option.payload.channel_blocking_incoming.description")
        @LatticeWidgetCustom(function = "channelSetTextArea")
        private Set<String> incomingChannels = new LinkedHashSet<>();

        @LatticeOption(title = "mtc.option.payload.channel_blocking_outgoing_enabled.label",
                       description = "mtc.option.payload.channel_blocking_outgoing_enabled.description")
        @LatticeWidgetButton
        private boolean outgoingEnabled = false;

        @LatticeOption(title = "mtc.option.payload.channel_blocking_outgoing.label",
                       description = "mtc.option.payload.channel_blocking_outgoing.description")
        @LatticeWidgetCustom(function = "channelSetTextArea")
        private Set<String> outgoingChannels = new LinkedHashSet<>();

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
    }

    @Data
    public static class BrandOverride {

        @LatticeOption(title = "mtc.option.payload.brand_override_enabled.label",
                       description = "mtc.option.payload.brand_override_enabled.description")
        @LatticeWidgetButton
        private boolean enabled = false;

        @LatticeOption(title = "mtc.option.payload.brand_override.label",
                       description = "mtc.option.payload.brand_override.description")
        @LatticeWidgetTextField(characterLimit = 16)
        private String override = "vanilla";
    }
}
