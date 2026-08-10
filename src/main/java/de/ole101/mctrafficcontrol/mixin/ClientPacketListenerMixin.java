package de.ole101.mctrafficcontrol.mixin;

import de.ole101.mctrafficcontrol.configuration.PacketBlockingConfiguration;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static de.ole101.mctrafficcontrol.McTrafficControl.LOGGER;
import static de.ole101.mctrafficcontrol.McTrafficControl.configuration;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Redirect(
            method = "handleBundlePacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V"
            )
    )
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void mtc$handleBundledPacket(Packet packet, PacketListener listener) {
        PacketBlockingConfiguration config = configuration.packetBlocking();
        boolean blocked = packet.type().flow() == PacketFlow.CLIENTBOUND
                && config.isIncomingEnabled()
                && config.getIncomingPackets().contains(packet.type().id().toString());

        if (blocked) {
            LOGGER.info("Blocking incoming bundled packet {}", packet.type().id());
            return;
        }

        packet.handle(listener);
    }
}
