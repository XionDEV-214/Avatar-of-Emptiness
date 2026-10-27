package com.eisiadev.avatarofemptiness.boss;

import com.eisiadev.avatarofemptiness.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public enum BossPhase {
    RIFT(1, 1_000_000.0, 2_000f, ModSounds.MUSIC_PHASE1, true),
    AVATAR(2, 2_500_000.0, 2_000f, ModSounds.MUSIC_PHASE2, true),
    WRATH(3, 4_000_000.0, 2_000f, ModSounds.MUSIC_PHASE3, true),
    PARADISE(4, 250_000.0, 100_000f, ModSounds.MUSIC_PARADISE, false);

    public final int number;
    public final double maxHealth;
    public final float contactDamage;
    public final RegistryObject<SoundEvent> music;
    public final boolean loopMusic;

    BossPhase(int number, double maxHealth, float contactDamage, RegistryObject<SoundEvent> music, boolean loopMusic) {
        this.number = number;
        this.maxHealth = maxHealth;
        this.contactDamage = contactDamage;
        this.music = music;
        this.loopMusic = loopMusic;
    }

    public BossPhase next() {
        return switch (this) {
            case RIFT -> AVATAR;
            case AVATAR -> WRATH;
            case WRATH -> PARADISE;
            case PARADISE -> null;
        };
    }

    public static BossPhase byNumber(int n) {
        for (BossPhase p : values()) if (p.number == n) return p;
        return RIFT;
    }

    public Component displayName() {
        return switch (this) {
            case RIFT -> Component.literal("균열").withStyle(ChatFormatting.DARK_PURPLE);
            case AVATAR -> Component.literal("완벽한 세계의 가장 빛나는 별, 공허함의 화신").withStyle(ChatFormatting.DARK_PURPLE);
            case WRATH -> Component.literal("완벽한 세계의 가장 빛나는 별, 공허함의 화신").withStyle(ChatFormatting.DARK_RED);
            case PARADISE -> paradiseName();
        };
    }

    private static Component paradiseName() {
        String word = "PARADISE";
        MutableComponent c = Component.empty();
        for (int i = 0; i < word.length(); i++) {
            if (i > 0) c.append(Component.literal(" "));
            c.append(Component.literal(String.valueOf(word.charAt(i)))
                    .withStyle(i % 2 == 0 ? ChatFormatting.BLACK : ChatFormatting.WHITE));
        }
        return c;
    }
}
