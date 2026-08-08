package de.ole101.mctrafficcontrol.integrations;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.network.chat.MutableComponent;

import static de.ole101.mctrafficcontrol.McTrafficControl.MOD_NAME;
import static de.ole101.mctrafficcontrol.McTrafficControl.configuration;
import static net.minecraft.network.chat.CommonComponents.SPACE;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        MutableComponent title = empty()
                .append(literal(MOD_NAME))
                .append(SPACE)
                .append(translatable("options.title"));

        return parentScreen -> YetAnotherConfigLib.createBuilder()
                .title(title)
                .save(configuration::saveToFile)
                .build()
                .generateScreen(parentScreen);
    }
}
