package de.ole101.mctrafficcontrol.configuration;

import com.moulberry.lattice.WidgetFunction;
import com.moulberry.lattice.annotation.LatticeOption;
import com.moulberry.lattice.annotation.widget.LatticeWidgetButton;
import com.moulberry.lattice.annotation.widget.LatticeWidgetCustom;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Data
public class PayloadConfiguration {

    @LatticeOption(title = "mtc.option.payload.incoming_enabled.label",
                   description = "mtc.option.payload.incoming_enabled.description")
    @LatticeWidgetButton
    private boolean incomingEnabled = false;

    @LatticeOption(title = "mtc.option.payload.incoming.label",
                   description = "mtc.option.payload.incoming.description")
    @LatticeWidgetCustom(function = "identifierSetTextArea")
    private Set<String> incomingChannels = new LinkedHashSet<>();

    @LatticeOption(title = "mtc.option.payload.outgoing_enabled.label",
                   description = "mtc.option.payload.outgoing_enabled.description")
    @LatticeWidgetButton
    private boolean outgoingEnabled = false;

    @LatticeOption(title = "mtc.option.payload.outgoing.label",
                   description = "mtc.option.payload.outgoing.description")
    @LatticeWidgetCustom(function = "identifierSetTextArea")
    private Set<String> outgoingChannels = new LinkedHashSet<>();

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
