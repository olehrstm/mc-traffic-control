package de.ole101.mctrafficcontrol.integrations;

import com.moulberry.lattice.Lattice;
import com.moulberry.lattice.element.LatticeElements;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import static de.ole101.mctrafficcontrol.McTrafficControl.CONFIG_TITLE;
import static de.ole101.mctrafficcontrol.McTrafficControl.configuration;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        configuration.loadKeybinds();
        LatticeElements elements = LatticeElements.fromAnnotations(CONFIG_TITLE, configuration);
        return parent -> Lattice.createConfigScreen(elements, configuration::saveToFile, parent);
    }
}
