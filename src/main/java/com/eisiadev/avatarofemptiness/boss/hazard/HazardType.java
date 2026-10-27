package com.eisiadev.avatarofemptiness.boss.hazard;

public enum HazardType {
    DISGUSTING_STAR,
    THORN_PORTAL,
    BIG_PORTAL,
    SHOCKWAVE,
    COMET,
    DANGER_ZONE,
    DEATHRAY_WARN,
    DEATHRAY,
    SUBZERO_PORTAL,
    ICE_CHUNK,
    BLACKHOLE,
    STAR_WHITE,
    STAR_BLUE,
    STAR_RED,
    PARADISE_FLOOR,
    VORTEX,
    RIFT_PULSE,
    RIFT_BURST;

    private static final HazardType[] VALUES = values();

    public static HazardType byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : RIFT_PULSE;
    }

    public boolean isStar() {
        return this == STAR_WHITE || this == STAR_BLUE || this == STAR_RED;
    }

    public boolean isProjectile() {
        return this == COMET || this == ICE_CHUNK;
    }
}
