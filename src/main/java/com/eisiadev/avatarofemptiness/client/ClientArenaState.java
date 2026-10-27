package com.eisiadev.avatarofemptiness.client;

import com.eisiadev.avatarofemptiness.arena.SphereVariant;

public final class ClientArenaState {
    private static SphereVariant variant = SphereVariant.STANDARD;
    private static float variantFade = 1f;
    private static int flashColor;
    private static int flashTicks;
    private static int flashMax = 1;
    private static float shake;

    public static void apply(int variantId, int flashArgb, int ticks, float shakeAmount) {
        if (variantId >= 0) {
            SphereVariant v = SphereVariant.byId(variantId);
            if (v != variant) variantFade = 0f;
            variant = v;
        }
        if ((flashArgb >>> 24) != 0 && ticks > 0) {
            flashColor = flashArgb;
            flashTicks = ticks;
            flashMax = ticks;
        }
        if (shakeAmount > 0) shake = Math.max(shake, shakeAmount);
    }

    public static void addShake(float amount) {
        shake = Math.max(shake, amount);
    }

    public static void tick() {
        if (flashTicks > 0) flashTicks--;
        shake *= 0.88f;
        if (shake < 0.01f) shake = 0f;
        variantFade = Math.min(1f, variantFade + 0.08f);
    }

    public static SphereVariant variant() {
        return variant;
    }

    public static float variantFade() {
        return variantFade;
    }

    public static float shake() {
        return shake;
    }

    public static int flashArgb(float partialTick) {
        if (flashTicks <= 0) return 0;
        float k = Math.max(0f, (flashTicks - partialTick) / flashMax);
        int a = (int) (((flashColor >>> 24) & 0xFF) * k * k);
        return (a << 24) | (flashColor & 0xFFFFFF);
    }

    public static void reset() {
        variant = SphereVariant.STANDARD;
        variantFade = 1f;
        flashTicks = 0;
        shake = 0f;
    }

    private ClientArenaState() {}
}
