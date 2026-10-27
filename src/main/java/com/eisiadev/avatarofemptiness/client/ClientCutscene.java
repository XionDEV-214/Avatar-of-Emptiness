package com.eisiadev.avatarofemptiness.client;

import com.eisiadev.avatarofemptiness.arena.Arena;
import com.eisiadev.avatarofemptiness.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class ClientCutscene {
    private static final int MUSIC_FADE = 120;
    private static final int BLACK_FADE = 50;

    private static int duration;
    private static int t = -1;

    public static void start(int durationTicks) {
        duration = durationTicks;
        t = 0;
        BossMusicPlayer.play(ModSounds.MUSIC_WINTER.getId(), false);
    }

    public static void reset() {
        t = -1;
    }

    public static boolean active() {
        return t >= 0;
    }

    public static void tick() {
        if (t < 0) return;
        t++;
        if (t <= duration) {
            float k = Mth.clamp((duration - t) / (float) MUSIC_FADE, 0f, 1f);
            BossMusicPlayer.setVolume(k * k);
        }
        if (t > duration + 40) t = -1;
    }

    private static float time(float pt) {
        return t + pt;
    }

    public static float riftScale(float pt) {
        if (t < 0) return 0f;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && Arena.isArena(mc.level)) return 0f;
        float x = time(pt);
        if (x < 40) return 0f;
        if (x < 100) return 0.05f * (x - 40) / 60f;
        if (x < 470) {
            float k = (x - 100) / 370f;
            float eased = k < 0.5f ? 4 * k * k * k : 1 - (float) Math.pow(-2 * k + 2, 3) / 2;
            return Mth.lerp(eased, 0.05f, 1f);
        }
        return 1f;
    }

    public static float eclipse(float pt) {
        float s = riftScale(pt);
        return Mth.clamp((s - 0.05f) / 0.95f, 0f, 1f);
    }

    public static float fade(float pt) {
        if (t < 0) return 0f;
        float x = time(pt);
        if (x < duration - BLACK_FADE) return 0f;
        if (x < duration) {
            float k = (x - (duration - BLACK_FADE)) / BLACK_FADE;
            return k * k;
        }
        return Math.max(0f, 1f - (x - duration) / 40f);
    }

    private ClientCutscene() {}
}
