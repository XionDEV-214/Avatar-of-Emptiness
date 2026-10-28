package com.eisiadev.avatarofemptiness.client.render;

import com.eisiadev.avatarofemptiness.arena.Arena;
import com.eisiadev.avatarofemptiness.arena.SphereVariant;
import com.eisiadev.avatarofemptiness.client.ClientArenaState;
import com.eisiadev.avatarofemptiness.client.ClientCutscene;
import com.eisiadev.avatarofemptiness.client.ModShaders;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

final class DomeRenderer {

    static void renderDome(RenderLevelStageEvent event, Vec3 cam, float time) {
        SphereVariant v = ClientArenaState.variant();
        ShaderInstance s = WorldFxRenderer.prep(ModShaders.ARENA_DOME, time);
        float cx = (float) (Arena.CX - cam.x), cy = (float) (Arena.FLOOR_Y - cam.y), cz = (float) (Arena.CZ - cam.z);
        float[] p = v.palette;
        s.safeGetUniform("CenterRel").set(cx, cy, cz);
        s.safeGetUniform("Mode").set(v.mode);
        s.safeGetUniform("PalA").set(p[0], p[1], p[2]);
        s.safeGetUniform("PalB").set(p[3], p[4], p[5]);
        s.safeGetUniform("PalC").set(p[6], p[7], p[8]);
        s.safeGetUniform("PalD").set(p[9], p[10], p[11]);
        s.safeGetUniform("Intensity").set(0.35f + 0.65f * ClientArenaState.variantFade());
        RenderSystem.setShaderTexture(0, WorldFxRenderer.noise(v.noise));

        FxDraw.pushView(event.getPoseStack());
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_TEX_COLOR);
        FxDraw.uvSphere(b, cx, cy, cz, (float) Arena.RADIUS, 64, 32, 1f, 1f, 1f, 1f);
        FxDraw.end(b);
        FxDraw.popView();
        FxDraw.restoreState();
    }

    static void renderSkyRift(RenderLevelStageEvent event, ClientLevel level, Camera camera, float pt, float time) {
        float scale = ClientCutscene.riftScale(pt);
        double th = level.getTimeOfDay(pt) * Math.PI * 2.0;
        float dx = (float) -Math.sin(th), dy = (float) Math.cos(th);
        if (dy < -0.05f) {
            dx = -dx;
            dy = -dy;
        }
        float dist = 100f;
        float x = dx * dist, y = dy * dist, z = 0f;
        float half = 80f * scale;

        ShaderInstance s = WorldFxRenderer.prep(ModShaders.RIFT, time);
        RenderSystem.setShaderTexture(0, WorldFxRenderer.noise("wavy_blotch_noise"));
        RenderSystem.setShaderTexture(1, WorldFxRenderer.noise("dendritic_noise"));

        Vector3f left = camera.getLeftVector();
        Vector3f up = camera.getUpVector();
        FxDraw.pushView(event.getPoseStack());
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        FxDraw.blendAlpha();
        BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_TEX_COLOR);
        FxDraw.billboard(b, x, y, z, left, up, half, 1f, 0.04f, 0.08f, 1f);
        FxDraw.end(b);
        FxDraw.popView();
        FxDraw.restoreState();
    }

    private DomeRenderer() {}
}
