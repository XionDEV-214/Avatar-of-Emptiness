package com.eisiadev.avatarofemptiness.arena;

public enum SphereVariant {
    STANDARD(0, "turbulent_noise",
            0.051f, 0.031f, 0.051f,
            0.145f, 0.004f, 0.020f,
            0.267f, 0.004f, 0.031f,
            0.471f, 0.027f, 0.063f),
    TINT_WHITE(0, "turbulent_noise",
            0.70f, 0.70f, 0.74f,
            0.86f, 0.86f, 0.90f,
            0.96f, 0.96f, 1.00f,
            1.00f, 1.00f, 1.00f),
    TINT_RED(0, "turbulent_noise",
            0.12f, 0.00f, 0.01f,
            0.40f, 0.00f, 0.02f,
            0.70f, 0.03f, 0.05f,
            1.00f, 0.15f, 0.12f),
    VISCERAL(1, "wavy_blotch_noise",
            0.039f, 0.000f, 0.024f,
            0.353f, 0.000f, 0.000f,
            0.392f, 0.067f, 0.067f,
            0.620f, 0.060f, 0.050f),
    SHATTER(2, "cracked_noise_b",
            1.00f, 1.00f, 1.00f,
            0.04f, 0.04f, 0.06f,
            0.60f, 0.88f, 1.00f,
            0.92f, 0.94f, 1.00f),
    SUBZERO(3, "viscous_noise",
            0.02f, 0.10f, 0.28f,
            0.25f, 0.70f, 1.00f,
            0.75f, 0.93f, 1.00f,
            1.00f, 1.00f, 1.00f),
    ANNIHILATION(4, "sharp_noise",
            0.00f, 0.00f, 0.00f,
            1.00f, 1.00f, 1.00f,
            0.29f, 0.70f, 1.00f,
            1.00f, 0.79f, 0.13f),
    PARADISE(5, "sharp_noise",
            0.00f, 0.00f, 0.00f,
            1.00f, 1.00f, 1.00f,
            0.50f, 0.50f, 0.50f,
            1.00f, 1.00f, 1.00f);

    public final int mode;
    public final String noise;
    public final float[] palette;

    SphereVariant(int mode, String noise, float... palette) {
        this.mode = mode;
        this.noise = noise;
        this.palette = palette;
    }

    public static SphereVariant byId(int id) {
        SphereVariant[] v = values();
        return id >= 0 && id < v.length ? v[id] : STANDARD;
    }
}
