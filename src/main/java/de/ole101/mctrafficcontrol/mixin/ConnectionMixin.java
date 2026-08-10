package de.ole101.mctrafficcontrol.mixin;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.ole101.mctrafficcontrol.McTrafficControl.configuration;

@Mixin(Connection.class)
public class ConnectionMixin {

    @Inject(
            method = "sendPacket",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mtc$blockOutgoingPayload(
            Packet<?> packet,
            ChannelFutureListener listener,
            boolean flush,
            CallbackInfo ci
    ) {
        if (packet instanceof ServerboundCustomPayloadPacket(
                CustomPacketPayload payload
        ) && configuration.payload().channelBlocking.isOutgoingEnabled() && configuration.payload().channelBlocking.getOutgoingChannels().contains(payload.type().id().toString())) {
            ci.cancel();
        }
    }
}
