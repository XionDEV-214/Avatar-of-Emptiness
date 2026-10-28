package com.eisiadev.avatarofemptiness.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ScreenShatterPacket() {

    public static void encode(ScreenShatterPacket msg, FriendlyByteBuf buf) {
    }

    public static ScreenShatterPacket decode(FriendlyByteBuf buf) {
        return new ScreenShatterPacket();
    }

    public static void handle(ScreenShatterPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketBridge::screenShatter);
    }
}
