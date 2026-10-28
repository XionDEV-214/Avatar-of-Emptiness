package com.eisiadev.avatarofemptiness.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ScreenShatterPacket(boolean white) {

    public static void encode(ScreenShatterPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.white);
    }

    public static ScreenShatterPacket decode(FriendlyByteBuf buf) {
        return new ScreenShatterPacket(buf.readBoolean());
    }

    public static void handle(ScreenShatterPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketBridge.screenShatter(msg.white()));
    }
}
