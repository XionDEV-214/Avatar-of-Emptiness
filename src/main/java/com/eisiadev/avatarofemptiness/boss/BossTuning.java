package com.eisiadev.avatarofemptiness.boss;

public final class BossTuning {
    public static final int PHASE12_PATTERN_COOLDOWN = 200;
    public static final int PHASE_TRANSITION_TICKS = 60;
    public static final int INTRO_TICKS = 40;

    public static final float STAR_RADIUS = 7.0f;
    public static final float STAR_DAMAGE = 150f;
    public static final float STAR_DAMAGE_VISCERAL = 225f;
    public static final float STAR_HEIGHT = 1.5f;

    public static final float STAR_COUNT_SCALE = 0.9f;
    public static final double ATTACK_RADIUS = 60.0;

    public static final float THORN_DAMAGE = 300f;
    public static final int THORN_HIT_INTERVAL = 5;
    public static final int THORN_RISE_DELAY = 20;
    public static final int THORN_LIFE = 20 + 30;
    public static final float THORN_PORTAL_RADIUS = 2.3f;
    public static final float THORN_HIT_RADIUS = 2.2f;
    public static final float THORN_HEIGHT = 14.0f;
    public static final float THORN_PEEK = 0.18f;
    public static final int THORN_RETRACT_TICKS = 8;
    public static final int THORN_SOUND_WINDOW = 20;
    public static final double THORN_LINE_SPACING = 4.5;
    public static final double THORN_LINE_RANGE = 64.0;

    public static final float SHOCKWAVE_DAMAGE = 600f;
    public static final float SHOCKWAVE_SPEED = 1.0f;
    public static final float SHOCKWAVE_HEIGHT = 1.0f;
    public static final float COMET_DAMAGE = 200f;
    public static final float COMET_SPEED = 2.0f;
    public static final int COMET_DIRECTIONS = 18;
    public static final float SPIN_RATE = 0.0f;
    public static final float PULL_STRONG = 0.20f;
    public static final float PULL_SHATTER = 0.256f;

    public static final int TRANSMISSION_TICKS = 70;
    public static final float DEATHRAY_LENGTH = 100f;
    public static final float DEATHRAY_HALF_WIDTH = 1.6f;
    public static final float DEATHRAY_HEIGHT = 3.0f;
    public static final float DEATHRAY_SPIN = 0.012f;

    public static final float TORNADO_RADIUS = 5.0f;
    public static final float TORNADO_HEIGHT = 32f;
    public static final int TORNADO_CHUNKS_PER_EMIT = 2;
    public static final float SUBZERO_CHUNK_DAMAGE = 200f;
    public static final float SUBZERO_CHUNK_SPEED = 3.0f;

    public static final float BLACKHOLE_RADIUS = 20.5f;
    public static final float BLACKHOLE_PERCENT = 0.10f;
    public static final int BLACKHOLE_HIT_INTERVAL = 3;
    public static final float BLACKHOLE_HEIGHT = 6.0f;
    public static final float BLACKHOLE_PULL_PLAYER = 0.035f;
    public static final int STAR_HIT_INTERVAL = 10;
    public static final int UA_STARS_PER_BURST = 30;
    public static final double UA_STAR_SPAWN_RADIUS = 100.0;
    public static final float WHITE_DWARF_RADIUS = 2f, WHITE_DWARF_DAMAGE = 150f;
    public static final float BLUE_STAR_RADIUS = 7f, BLUE_STAR_DAMAGE = 300f;
    public static final float RED_GIANT_RADIUS = 15f, RED_GIANT_DAMAGE = 450f;

    public static final int AIR_SLAM_TICKS = 60;
    public static final double AIR_SLAM_SPEED = 3.5;
    public static final int INBOUND_COMETS = 18;

    public static final int PARADISE_TIME_LIMIT = 600;
    public static final float PARADISE_TICK_DAMAGE = 20f;
    public static final float PARADISE_FLOOR_SPEED = 0.05f;
    public static final double PARADISE_FLOOR_START_DEPTH = 30.0;

    private BossTuning() {}
}
