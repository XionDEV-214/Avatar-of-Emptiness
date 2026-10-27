package com.eisiadev.avatarofemptiness;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AoeConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final ForgeConfigSpec.DoubleValue DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue CONTACT_DAMAGE_INTERVAL;

    public static final ForgeConfigSpec.DoubleValue FLASH_INTENSITY;
    public static final ForgeConfigSpec.DoubleValue SHAKE_INTENSITY;

    static {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        common.push("balance");
        DAMAGE_MULTIPLIER = common
                .comment("Multiplier applied to flat damage values from the spec (150, 300, 2000, ...).",
                        "Percent-of-max-health and instant-kill attacks ignore this.")
                .defineInRange("damageMultiplier", 1.0, 0.0, 1000.0);
        CONTACT_DAMAGE_INTERVAL = common
                .comment("Ticks between boss contact-damage hits on the same player.")
                .defineInRange("contactDamageInterval", 10, 1, 200);
        common.pop();
        COMMON_SPEC = common.build();

        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        client.push("visuals");
        FLASH_INTENSITY = client
                .comment("Screen flash strength. 0 disables flashes (photosensitivity).")
                .defineInRange("flashIntensity", 1.0, 0.0, 1.0);
        SHAKE_INTENSITY = client
                .comment("Screen shake strength. 0 disables shake.")
                .defineInRange("shakeIntensity", 1.0, 0.0, 2.0);
        client.pop();
        CLIENT_SPEC = client.build();
    }

    private AoeConfig() {}
}
