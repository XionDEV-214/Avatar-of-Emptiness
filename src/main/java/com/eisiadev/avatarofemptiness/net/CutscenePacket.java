package com.eisiadev.avatarofemptiness.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CutscenePacket(int duration) {

    public static void encode(CutscenePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.duration);
    }

    public static CutscenePacket decode(FriendlyByteBuf buf) {
        return new CutscenePacket(buf.readVarInt());
    }

    public static void handle(CutscenePacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketBridge.cutscene(msg));
    }
}
