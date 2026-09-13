package de.ole101.mctrafficcontrol.mixin;

import com.mojang.blaze3d.font.GlyphProvider;
import de.ole101.mctrafficcontrol.gui.font.CustomGlyphs;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BitmapProvider.Definition.class)
public class BitmapProviderDefinitionMixin {

    @Shadow
    @Final
    private Identifier file;

    // Packs can replace a vanilla font's texture without changing its definition
    @Inject(method = "load", at = @At("RETURN"))
    private void mtc$markReplacedTexture(ResourceManager resourceManager, CallbackInfoReturnable<GlyphProvider> cir) {
        resourceManager.getResource(file.withPrefix("textures/"))
                .map(Resource::sourcePackId)
                .filter(packId -> !CustomGlyphs.VANILLA_PACK_ID.equals(packId))
                .ifPresent(packId -> CustomGlyphs.markCustom(cir.getReturnValue()));
    }
}
