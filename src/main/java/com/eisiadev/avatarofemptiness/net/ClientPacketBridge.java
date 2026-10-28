package com.eisiadev.avatarofemptiness.net;

import com.eisiadev.avatarofemptiness.client.BossMusicPlayer;
import com.eisiadev.avatarofemptiness.client.ClientArenaState;
import com.eisiadev.avatarofemptiness.client.ClientCutscene;
import com.eisiadev.avatarofemptiness.client.ClientHazards;
import com.eisiadev.avatarofemptiness.client.ScreenShatter;

public final class ClientPacketBridge {

    static void hazardSync(HazardSyncPacket msg) {
        ClientHazards.sync(msg.clear(), msg.hazards());
    }

    static void hazardRemove(HazardRemovePacket msg) {
        ClientHazards.remove(msg.ids());
    }

    static void arenaFx(ArenaFxPacket msg) {
        ClientArenaState.apply(msg.variant(), msg.flashColor(), msg.flashTicks(), msg.shake());
    }

    static void music(MusicPacket msg) {
        if (msg.sound() == null) {
            BossMusicPlayer.stop();
        } else {
            BossMusicPlayer.play(msg.sound(), msg.loop());
        }
    }

    static void cutscene(CutscenePacket msg) {
        if (msg.duration() <= 0) {
            ClientCutscene.reset();
        } else {
            ClientCutscene.start(msg.duration());
        }
    }

    static void screenShatter() {
        ScreenShatter.trigger();
    }

    private ClientPacketBridge() {}
}
