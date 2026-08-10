package de.ole101.mctrafficcontrol.mixin;

import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.ole101.mctrafficcontrol.McTrafficControl.LOGGER;
import static de.ole101.mctrafficcontrol.McTrafficControl.configuration;

@Mixin(ClientboundCustomPayloadPacket.class)
public class ClientboundCustomPayloadPacketMixin {

    @Final
    @Shadow
    private CustomPacketPayload payload;

    @Inject(
            method = "handle(Lnet/minecraft/network/protocol/common/ClientCommonPacketListener;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mtc$blockIncomingPayload(ClientCommonPacketListener listener, CallbackInfo ci) {
        if (configuration.packet().payloadChannelBlocking.isIncomingEnabled() && configuration.packet().payloadChannelBlocking.getIncomingChannels().contains(payload.type().id().toString())) {
            LOGGER.info("Blocking incoming payload {}", payload.type().id());
            ci.cancel();
        }
    }
}
