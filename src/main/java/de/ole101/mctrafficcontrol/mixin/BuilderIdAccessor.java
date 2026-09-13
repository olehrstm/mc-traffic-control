package de.ole101.mctrafficcontrol.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.font.FontManager$BuilderId")
public interface BuilderIdAccessor {

    @Accessor("pack")
    String mtc$getPack();
}
