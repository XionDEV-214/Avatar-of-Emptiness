package com.eisiadev.avatarofemptiness.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record MusicPacket(@Nullable ResourceLocation sound, boolean loop) {

    public static MusicPacket stop() {
        return new MusicPacket(null, false);
    }

    public static void encode(MusicPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.sound != null);
        if (msg.sound != null) buf.writeResourceLocation(msg.sound);
        buf.writeBoolean(msg.loop);
    }

    public static MusicPacket decode(FriendlyByteBuf buf) {
        ResourceLocation rl = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new MusicPacket(rl, buf.readBoolean());
    }

    public static void handle(MusicPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketBridge.music(msg));
    }
}
