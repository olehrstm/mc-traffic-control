package de.ole101.mctrafficcontrol.configuration;

import com.moulberry.lattice.annotation.LatticeOption;
import com.moulberry.lattice.annotation.widget.LatticeWidgetButton;
import com.moulberry.lattice.annotation.widget.LatticeWidgetTextField;
import lombok.Data;

@Data
public class ClientIdentityConfiguration {

    @LatticeOption(title = "mtc.option.client_identity.brand_override_enabled.label",
                   description = "mtc.option.client_identity.brand_override_enabled.description")
    @LatticeWidgetButton
    private boolean brandOverrideEnabled = false;

    @LatticeOption(title = "mtc.option.client_identity.brand_override.label",
                   description = "mtc.option.client_identity.brand_override.description")
    @LatticeWidgetTextField(characterLimit = 16)
    private String brand = "vanilla";
}
