package de.ole101.mctrafficcontrol.mixin;

import de.ole101.mctrafficcontrol.configuration.PacketBlockingConfiguration;
import de.ole101.mctrafficcontrol.configuration.PayloadConfiguration;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.ole101.mctrafficcontrol.McTrafficControl.LOGGER;
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
        PayloadConfiguration config = configuration.payload();
        if (packet instanceof ServerboundCustomPayloadPacket(
                CustomPacketPayload payload
        ) && config.isOutgoingEnabled() && config.getOutgoingChannels().contains(payload.type().id().toString())) {
            LOGGER.info("Blocking outgoing payload {}", payload.type().id());
            ci.cancel();
        }
    }

    @Inject(
            method = "sendPacket",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mtc$blockOutgoingPacket(
            Packet<?> packet,
            ChannelFutureListener listener,
            boolean flush,
            CallbackInfo ci
    ) {
        PacketBlockingConfiguration config = configuration.packetBlocking();
        if (packet.type().flow() == PacketFlow.SERVERBOUND
                && config.isOutgoingEnabled()
                && config.getOutgoingPackets().contains(packet.type().id().toString())
        ) {
            LOGGER.info("Blocking outgoing packet {}", packet.type().id());
            ci.cancel();
        }
    }

    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mtc$blockIncomingPacket(
            ChannelHandlerContext ctx,
            Packet<?> packet,
            CallbackInfo ci
    ) {
        PacketBlockingConfiguration config = configuration.packetBlocking();
        if (packet.type().flow() == PacketFlow.CLIENTBOUND
                && config.isIncomingEnabled()
                && config.getIncomingPackets().contains(packet.type().id().toString())
        ) {
            LOGGER.info("Blocking incoming packet {}", packet.type().id());
            ci.cancel();
        }
    }
}
