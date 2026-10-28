package com.eisiadev.avatarofemptiness.boss;

import com.eisiadev.avatarofemptiness.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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

    public static final String KEY_RIFT = "avatarofemptiness.boss.rift";
    public static final String KEY_AVATAR = "avatarofemptiness.boss.avatar";
    public static final String KEY_WRATH = "avatarofemptiness.boss.wrath";
    public static final String KEY_TITLE = "avatarofemptiness.boss.title";
    public static final String KEY_NAME = "avatarofemptiness.boss.name";
    public static final String KEY_PARADISE = "avatarofemptiness.boss.paradise";

    public Component displayName() {
        return switch (this) {
            case RIFT -> Component.translatable(KEY_RIFT).withStyle(Style.EMPTY.withColor(0xC23B9E));
            case AVATAR -> Component.translatable(KEY_AVATAR,
                    Component.translatable(KEY_TITLE).withStyle(Style.EMPTY.withColor(0xC9B8E8)),
                    Component.translatable(KEY_NAME).withStyle(Style.EMPTY.withColor(0xFF3355)));
            case WRATH -> Component.translatable(KEY_WRATH,
                    Component.translatable(KEY_TITLE).withStyle(Style.EMPTY.withColor(0xE0A0A0)),
                    Component.translatable(KEY_NAME).withStyle(Style.EMPTY.withColor(0xD0102A)));
            case PARADISE -> Component.translatable(KEY_PARADISE).withStyle(ChatFormatting.WHITE);
        };
    }
}
