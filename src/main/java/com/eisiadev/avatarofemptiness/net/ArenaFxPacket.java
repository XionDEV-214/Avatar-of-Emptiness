package com.eisiadev.avatarofemptiness.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ArenaFxPacket(int variant, int flashColor, int flashTicks, float shake, int flashHold) {

    public ArenaFxPacket(int variant, int flashColor, int flashTicks, float shake) {
        this(variant, flashColor, flashTicks, shake, 0);
    }

    public static ArenaFxPacket variantOnly(int variant) {
        return new ArenaFxPacket(variant, 0, 0, 0f);
    }

    public static void encode(ArenaFxPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.variant + 1);
        buf.writeInt(msg.flashColor);
        buf.writeVarInt(msg.flashTicks);
        buf.writeFloat(msg.shake);
        buf.writeVarInt(msg.flashHold);
    }

    public static ArenaFxPacket decode(FriendlyByteBuf buf) {
        return new ArenaFxPacket(buf.readVarInt() - 1, buf.readInt(), buf.readVarInt(), buf.readFloat(), buf.readVarInt());
    }

    public static void handle(ArenaFxPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketBridge.arenaFx(msg));
    }
}
