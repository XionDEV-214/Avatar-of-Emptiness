package com.eisiadev.avatarofemptiness.client;

import com.eisiadev.avatarofemptiness.arena.Arena;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public final class BossMusicPlayer {
    @Nullable
    private static Track current;
    @Nullable
    private static ResourceLocation currentId;
    private static boolean currentLoop;
    private static int silentTicks;

    public static void play(ResourceLocation sound, boolean loop) {
        Minecraft mc = Minecraft.getInstance();
        if (sound.equals(currentId) && current != null && mc.getSoundManager().isActive(current)) return;
        stop();
        silenceOthers(mc);
        current = new Track(sound, loop);
        currentId = sound;
        currentLoop = loop;
        silentTicks = 0;
        mc.getSoundManager().play(current);
    }

    public static void setVolume(ResourceLocation track, float v) {
        if (current != null && track.equals(currentId)) current.setVolume(v);
    }

    public static void stop() {
        if (current != null) {
            Minecraft.getInstance().getSoundManager().stop(current);
        }
        current = null;
        currentId = null;
    }

    public static boolean blocking() {
        Minecraft mc = Minecraft.getInstance();
        return ClientCutscene.active() || (mc.level != null && Arena.isArena(mc.level));
    }

    public static boolean isOurs(SoundInstance sound) {
        return sound instanceof Track;
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (!blocking()) {
            if (current != null) stop();
            return;
        }
        mc.getMusicManager().stopPlaying();
        if (current != null && currentLoop && !mc.getSoundManager().isActive(current)) {
            if (++silentTicks > 20) {
                ResourceLocation id = currentId;
                current = null;
                currentId = null;
                play(id, true);
            }
        } else {
            silentTicks = 0;
        }
    }

    private static void silenceOthers(Minecraft mc) {
        mc.getMusicManager().stopPlaying();
        mc.getSoundManager().stop(null, SoundSource.MUSIC);
        mc.getSoundManager().stop(null, SoundSource.RECORDS);
    }

    private static final class Track extends AbstractTickableSoundInstance {
        Track(ResourceLocation id, boolean loop) {
            super(SoundEvent.createVariableRangeEvent(id), SoundSource.MUSIC, RandomSource.create());
            this.looping = loop;
            this.delay = 0;
            this.volume = 1.0f;
            this.pitch = 1.0f;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.relative = true;
            this.x = this.y = this.z = 0.0;
        }

        void setVolume(float v) {
            this.volume = Math.max(0.0001f, Math.min(1f, v));
        }

        @Override
        public void tick() {
        }
    }

    private BossMusicPlayer() {}
}
