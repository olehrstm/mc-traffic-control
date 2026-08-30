package de.ole101.mctrafficcontrol.configuration;

import com.moulberry.lattice.annotation.LatticeOption;
import com.moulberry.lattice.annotation.widget.LatticeWidgetKeybind;
import lombok.Data;
import net.minecraft.client.KeyMapping;

@Data
public class ComponentViewingConfiguration {

    @LatticeOption(title = "mtc.option.component_viewing.keybind.label",
                   description = "mtc.option.component_viewing.keybind.description")
    @LatticeWidgetKeybind
    private transient KeyMapping keybind;
}
