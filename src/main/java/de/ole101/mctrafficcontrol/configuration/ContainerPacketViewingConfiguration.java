package de.ole101.mctrafficcontrol.configuration;

import com.moulberry.lattice.annotation.LatticeOption;
import com.moulberry.lattice.annotation.widget.LatticeWidgetKeybind;
import lombok.Data;
import net.minecraft.client.KeyMapping;

@Data
public class ContainerPacketViewingConfiguration {

    @LatticeOption(title = "mtc.option.container_packet_viewing.keybind.label",
                   description = "mtc.option.container_packet_viewing.keybind.description")
    @LatticeWidgetKeybind
    private transient KeyMapping keybind;
}
