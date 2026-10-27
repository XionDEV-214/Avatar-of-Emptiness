package com.eisiadev.avatarofemptiness.net;

import com.eisiadev.avatarofemptiness.boss.hazard.Hazard;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record HazardSyncPacket(boolean clear, List<Hazard> hazards) {

    public static void encode(HazardSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.clear);
        buf.writeVarInt(msg.hazards.size());
        for (Hazard h : msg.hazards) h.write(buf);
    }

    public static HazardSyncPacket decode(FriendlyByteBuf buf) {
        boolean clear = buf.readBoolean();
        int n = buf.readVarInt();
        List<Hazard> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(Hazard.read(buf));
        return new HazardSyncPacket(clear, list);
    }

    public static void handle(HazardSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketBridge.hazardSync(msg));
    }
}
