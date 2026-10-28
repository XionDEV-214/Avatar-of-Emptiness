package com.eisiadev.avatarofemptiness.net;

import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String VERSION = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            AvatarOfEmptinessMod.id("main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private static boolean registered;

    public static void register() {
        if (registered) return;
        registered = true;
        int id = 0;
        CHANNEL.messageBuilder(HazardSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(HazardSyncPacket::encode).decoder(HazardSyncPacket::decode)
                .consumerMainThread(HazardSyncPacket::handle).add();
        CHANNEL.messageBuilder(HazardRemovePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(HazardRemovePacket::encode).decoder(HazardRemovePacket::decode)
                .consumerMainThread(HazardRemovePacket::handle).add();
        CHANNEL.messageBuilder(ArenaFxPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ArenaFxPacket::encode).decoder(ArenaFxPacket::decode)
                .consumerMainThread(ArenaFxPacket::handle).add();
        CHANNEL.messageBuilder(MusicPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(MusicPacket::encode).decoder(MusicPacket::decode)
                .consumerMainThread(MusicPacket::handle).add();
        CHANNEL.messageBuilder(CutscenePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CutscenePacket::encode).decoder(CutscenePacket::decode)
                .consumerMainThread(CutscenePacket::handle).add();
        CHANNEL.messageBuilder(ScreenShatterPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ScreenShatterPacket::encode).decoder(ScreenShatterPacket::decode)
                .consumerMainThread(ScreenShatterPacket::handle).add();
    }

    public static void sendToLevel(ServerLevel level, Object msg) {
        CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension), msg);
    }

    public static void sendTo(ServerPlayer player, Object msg) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    private ModNetwork() {}
}
