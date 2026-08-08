package de.ole101.mctrafficcontrol.mixin;

import de.ole101.mctrafficcontrol.configuration.MiscellaneousConfiguration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.ole101.mctrafficcontrol.McTrafficControl.configuration;

@Mixin(BrandPayload.class)
public class BrandPayloadMixin {

    @Inject(method = "write(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "HEAD"), cancellable = true)
    public void mtc$write(FriendlyByteBuf output, CallbackInfo ci) {
        MiscellaneousConfiguration config = configuration.miscellaneous();
        String brandOverride = config.getBrandOverride();
        if (!config.isBrandOverrideEnabled() || brandOverride == null || brandOverride.isEmpty()) {
            return;
        }
        output.writeUtf(brandOverride);
        ci.cancel();
    }
}
