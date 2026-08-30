package de.ole101.mctrafficcontrol;

import com.moulberry.lattice.Lattice;
import com.moulberry.lattice.element.LatticeElements;
import de.ole101.mctrafficcontrol.configuration.Configuration;
import de.ole101.mctrafficcontrol.gui.widgets.ComponentViewerWidget;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM;
import static net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping;
import static net.minecraft.client.KeyMapping.Category.register;
import static net.minecraft.network.chat.CommonComponents.SPACE;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;
import static net.minecraft.resources.Identifier.fromNamespaceAndPath;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

public class McTrafficControl implements ModInitializer {

    public static final String MOD_ID = "mc-traffic-control";
    public static final String MOD_NAME = "mc-traffic-control";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Configuration configuration = new Configuration().loadFromFile();
    public static final MutableComponent CONFIG_TITLE = empty()
            .append(literal(MOD_NAME))
            .append(SPACE)
            .append(translatable("options.title"));

    public static final KeyMapping.Category KEY_CATEGORY = register(id("name"));
    public static final KeyMapping COMPONENT_VIEWER_KEY = registerKeyMapping(new KeyMapping("mtc.key.view_components", KEYSYM, GLFW_KEY_UNKNOWN, KEY_CATEGORY));

    public static final ComponentViewerWidget COMPONENT_VIEWER_WIDGET = new ComponentViewerWidget();

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        configuration.loadKeybinds();

        Minecraft.getInstance().submit(() -> {
            LatticeElements elements = LatticeElements.fromAnnotations(CONFIG_TITLE, configuration);
            Lattice.performTest(elements);
        });

        LOGGER.info("Hello Fabric world!");
    }

    public static Identifier id(String path) {
        return fromNamespaceAndPath(MOD_ID, path);
    }
}
