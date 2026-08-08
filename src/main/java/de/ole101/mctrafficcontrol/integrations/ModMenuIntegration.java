package de.ole101.mctrafficcontrol.integrations;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
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
                .category(ConfigCategory.createBuilder()
                        .name(translatable("mtc.option.miscellaneous.label"))
                        .tooltip(translatable("mtc.option.miscellaneous.tooltip"))
                        .group(OptionGroup.createBuilder()
                                .name(translatable("mtc.option.miscellaneous.brand_override.section_title"))
                                .description(OptionDescription.of(translatable("mtc.option.miscellaneous.brand_override.section_tooltip")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(translatable("mtc.option.miscellaneous.brand_override_enabled.label"))
                                        .description(OptionDescription.of(translatable("mtc.option.miscellaneous.brand_override_enabled.tooltip")))
                                        .binding(false, () -> configuration.miscellaneous().isBrandOverrideEnabled(), value -> configuration.miscellaneous().setBrandOverrideEnabled(value))
                                        .controller(BooleanControllerBuilder::create)
                                        .build())
                                .option(Option.<String>createBuilder()
                                        .name(translatable("mtc.option.miscellaneous.brand_override.label"))
                                        .description(OptionDescription.of(translatable("mtc.option.miscellaneous.brand_override.tooltip")))
                                        .binding("fabric", () -> configuration.miscellaneous().getBrandOverride(), value -> configuration.miscellaneous().setBrandOverride(value))
                                        .controller(StringControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .save(configuration::saveToFile)
                .build()
                .generateScreen(parentScreen);
    }
}
