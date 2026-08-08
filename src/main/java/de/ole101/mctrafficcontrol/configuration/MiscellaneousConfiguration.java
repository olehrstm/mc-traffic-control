package de.ole101.mctrafficcontrol.configuration;

import lombok.Data;

@Data
public class MiscellaneousConfiguration {

    private boolean brandOverrideEnabled = false;
    private String brandOverride = "fabric";
}
