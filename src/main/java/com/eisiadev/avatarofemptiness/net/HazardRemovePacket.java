package com.eisiadev.avatarofemptiness.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record HazardRemovePacket(int[] ids) {

    public static void encode(HazardRemovePacket msg, FriendlyByteBuf buf) {
        buf.writeVarIntArray(msg.ids);
    }

    public static HazardRemovePacket decode(FriendlyByteBuf buf) {
        return new HazardRemovePacket(buf.readVarIntArray());
    }

    public static void handle(HazardRemovePacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketBridge.hazardRemove(msg));
    }
}
