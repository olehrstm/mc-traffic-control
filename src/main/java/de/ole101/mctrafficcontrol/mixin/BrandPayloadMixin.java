package de.ole101.mctrafficcontrol.mixin;

import de.ole101.mctrafficcontrol.configuration.PacketConfiguration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.ole101.mctrafficcontrol.McTrafficControl.LOGGER;
import static de.ole101.mctrafficcontrol.McTrafficControl.configuration;

@Mixin(BrandPayload.class)
public class BrandPayloadMixin {

    @Inject(method = "write(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "HEAD"), cancellable = true)
    public void mtc$overrideBrand(FriendlyByteBuf output, CallbackInfo ci) {
        PacketConfiguration config = configuration.packet();
        String brandOverride = config.brandOverride.getOverride();
        if (!config.brandOverride.isEnabled() || brandOverride == null || brandOverride.isEmpty()) {
            return;
        }
        LOGGER.info("Overriding client brand with {}", brandOverride);
        output.writeUtf(brandOverride);
        ci.cancel();
    }
}
