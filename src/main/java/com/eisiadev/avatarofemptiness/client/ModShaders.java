package com.eisiadev.avatarofemptiness.client;

import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.client.event.RegisterShadersEvent;

import java.io.IOException;
import java.util.function.Consumer;

public final class ModShaders {
    public static ShaderInstance ARENA_DOME;
    public static ShaderInstance RIFT;
    public static ShaderInstance STAR_SPRITE;
    public static ShaderInstance DANGER_SPHERE;
    public static ShaderInstance PORTAL;
    public static ShaderInstance THORN;
    public static ShaderInstance THORN_OVERLAY;
    public static ShaderInstance SHOCKWAVE;
    public static ShaderInstance BEAM;
    public static ShaderInstance TORNADO;
    public static ShaderInstance ICE_CHUNK;
    public static ShaderInstance BLACKHOLE;
    public static ShaderInstance PARADISE_STATIC;
    public static ShaderInstance MESH;
    public static ShaderInstance RING;
    public static ShaderInstance VISCERA;

    public static void register(RegisterShadersEvent event) throws IOException {
        VertexFormat ptc = DefaultVertexFormat.POSITION_TEX_COLOR;
        reg(event, "arena_dome", ptc, s -> ARENA_DOME = s);
        reg(event, "rift", ptc, s -> RIFT = s);
        reg(event, "star_sprite", ptc, s -> STAR_SPRITE = s);
        reg(event, "danger_sphere", DefaultVertexFormat.POSITION_COLOR_NORMAL, s -> DANGER_SPHERE = s);
        reg(event, "portal", ptc, s -> PORTAL = s);
        reg(event, "thorn", DefaultVertexFormat.POSITION, s -> THORN = s);
        reg(event, "thorn_overlay", DefaultVertexFormat.POSITION, s -> THORN_OVERLAY = s);
        reg(event, "shockwave", ptc, s -> SHOCKWAVE = s);
        reg(event, "beam", ptc, s -> BEAM = s);
        reg(event, "tornado", ptc, s -> TORNADO = s);
        reg(event, "ice_chunk", ptc, s -> ICE_CHUNK = s);
        reg(event, "blackhole", ptc, s -> BLACKHOLE = s);
        reg(event, "paradise_static", ptc, s -> PARADISE_STATIC = s);
        reg(event, "mesh", ptc, s -> MESH = s);
        reg(event, "ring", ptc, s -> RING = s);
        reg(event, "viscera", ptc, s -> VISCERA = s);
    }

    private static void reg(RegisterShadersEvent event, String name, VertexFormat format,
                            Consumer<ShaderInstance> onLoad) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), AvatarOfEmptinessMod.id(name), format), onLoad);
    }

    public static boolean ready() {
        return ARENA_DOME != null && MESH != null && VISCERA != null;
    }

    private ModShaders() {}
}
