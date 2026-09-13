package de.ole101.mctrafficcontrol.mixin;

import com.mojang.blaze3d.font.GlyphProvider;
import de.ole101.mctrafficcontrol.gui.font.CustomGlyphs;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(FontManager.class)
public abstract class FontManagerMixin {

    @Shadow
    @Final
    private Map<Identifier, FontSet> fontSets;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mtc$trackFontSets(CallbackInfo ci) {
        CustomGlyphs.setFontSets(fontSets);
    }

    @Inject(method = "safeLoad", at = @At("RETURN"), cancellable = true)
    private void mtc$markCustomProviders(@Coerce Object builderId,
                                         GlyphProviderDefinition.Loader loader,
                                         ResourceManager manager,
                                         Executor executor,
                                         CallbackInfoReturnable<CompletableFuture<Optional<GlyphProvider>>> cir) {
        if (CustomGlyphs.VANILLA_PACK_ID.equals(((BuilderIdAccessor) builderId).mtc$getPack())) {
            return;
        }

        cir.setReturnValue(cir.getReturnValue().thenApply(provider -> {
            provider.ifPresent(CustomGlyphs::markCustom);
            return provider;
        }));
    }
}
