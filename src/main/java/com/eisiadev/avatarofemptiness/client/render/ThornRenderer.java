package com.eisiadev.avatarofemptiness.client.render;

import com.eisiadev.avatarofemptiness.arena.Arena;
import com.eisiadev.avatarofemptiness.boss.BossTuning;
import com.eisiadev.avatarofemptiness.boss.hazard.Hazard;
import com.eisiadev.avatarofemptiness.boss.hazard.HazardType;
import com.eisiadev.avatarofemptiness.client.ClientHazards;
import com.eisiadev.avatarofemptiness.client.ModShaders;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.Random;

final class ThornRenderer {
    private static final int SEGS = 18;
    private static final int RINGS = 48;
    private static final float BASE_RADIUS = 1.15f;
    private static final float SIDE_LENGTH = 0.14f;
    private static final float RATE_PEEK = 1f - (float) Math.pow(0.93, 3);
    private static final float RATE_SHOOT = 1f - (float) Math.pow(0.81, 3);

    @Nullable
    private static VertexBuffer cone;
    @Nullable
    private static TextureTarget target;

    static void render(RenderLevelStageEvent event, Vec3 cam, float pt) {
        if (ModShaders.THORN == null || ModShaders.THORN_OVERLAY == null) return;
        boolean any = false;
        for (Hazard h : ClientHazards.all()) {
            if (h.type == HazardType.THORN_PORTAL) {
                any = true;
                break;
            }
        }
        if (!any) return;

        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        TextureTarget t = target(main);

        t.setClearColor(0f, 0f, 0f, 0f);
        t.clear(Minecraft.ON_OSX);
        t.copyDepthFrom(main);
        t.bindWrite(true);

        ShaderInstance s = ModShaders.THORN;
        s.safeGetUniform("ClipY").set((float) (Arena.FLOOR_Y - cam.y));
        s.safeGetUniform("GroundFade").set(0.9f);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.disableBlend();

        Matrix4f view = new Matrix4f(event.getPoseStack().last().pose());
        Matrix4f proj = RenderSystem.getProjectionMatrix();
        VertexBuffer vb = cone();
        vb.bind();
        for (Hazard h : ClientHazards.all()) {
            if (h.type != HazardType.THORN_PORTAL) continue;
            drawThorn(vb, s, view, proj, h, cam, pt);
        }
        VertexBuffer.unbind();

        main.bindWrite(true);
        ShaderInstance o = ModShaders.THORN_OVERLAY;
        o.safeGetUniform("OutlinePx").set((float) Math.max(2, Math.round(main.height / 540f)));
        o.safeGetUniform("EdgeColor").set(1f, 0f, 0f, 1f);
        RenderSystem.setShaderTexture(0, t.getColorTextureId());
        RenderSystem.setShaderTexture(1, t.getDepthTextureId());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.depthMask(true);
        RenderSystem.setShader(() -> o);
        BufferBuilder b = Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        b.vertex(-1, -1, 0).endVertex();
        b.vertex(1, -1, 0).endVertex();
        b.vertex(1, 1, 0).endVertex();
        b.vertex(-1, 1, 0).endVertex();
        BufferUploader.drawWithShader(b.end());
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        FxDraw.restoreState();
    }

    private static void drawThorn(VertexBuffer vb, ShaderInstance s, Matrix4f view, Matrix4f proj,
                                  Hazard h, Vec3 cam, float pt) {
        Random rng = new Random(h.seed);
        float maxReach = BossTuning.THORN_HEIGHT * (0.9f + 0.2f * rng.nextFloat());
        float reach = reach(h, maxReach, pt);
        if (reach <= 0.01f) return;

        float x = (float) (h.lerpX(pt) - cam.x);
        float y = (float) (Arena.FLOOR_Y - cam.y);
        float z = (float) (h.lerpZ(pt) - cam.z);
        float sink = maxReach - reach;
        float spin = h.id * 0.7f;

        Matrix4f m = new Matrix4f()
                .translate(x, y - sink, z)
                .rotateY(spin)
                .scale(BASE_RADIUS, maxReach, BASE_RADIUS);
        draw(vb, s, view, proj, m);

        int count = (int) Mth.lerp(rng.nextFloat(), 7f, 16f);
        for (int i = 1; i < count; i++) {
            float side = rng.nextBoolean() ? 1f : -1f;
            float zRot = side * ((float) Math.PI / 2f - 0.86f) + Mth.lerp(rng.nextFloat(), -0.4f, 0.4f);
            float f = i / (float) (count - 1);
            float shrivel = 1f - f;
            float offsetY = maxReach * f * 0.84f;
            float w = BASE_RADIUS * shrivel * 0.9f;
            if (w <= 0.01f) continue;
            Matrix4f sm = new Matrix4f()
                    .translate(x, y + offsetY - sink, z)
                    .rotateY(spin)
                    .rotateY(i * 1.3f)
                    .rotateZ(zRot)
                    .scale(w, reach * SIDE_LENGTH, w);
            draw(vb, s, view, proj, sm);
        }
    }

    private static void draw(VertexBuffer vb, ShaderInstance s, Matrix4f view, Matrix4f proj, Matrix4f model) {
        s.safeGetUniform("ModelMat").set(model);
        vb.drawWithShader(view, proj, s);
    }

    private static float reach(Hazard h, float max, float pt) {
        float r = 0f;
        for (int k = 0; k < h.age; k++) r = stepReach(h, k, r, max);
        float next = stepReach(h, h.age, r, max);
        return Mth.lerp(pt, r, next);
    }

    private static float stepReach(Hazard h, int tick, float r, float max) {
        float ideal;
        float rate;
        if (tick < h.trigger) {
            ideal = max * BossTuning.THORN_PEEK;
            rate = RATE_PEEK;
        } else {
            ideal = tick >= h.life - BossTuning.THORN_RETRACT_TICKS ? 0f : max;
            rate = RATE_SHOOT;
        }
        return r + (ideal - r) * rate;
    }

    private static TextureTarget target(RenderTarget main) {
        if (target == null) {
            target = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
        } else if (target.width != main.width || target.height != main.height) {
            target.resize(main.width, main.height, Minecraft.ON_OSX);
        }
        if (main.isStencilEnabled() && !target.isStencilEnabled()) target.enableStencil();
        return target;
    }

    private static VertexBuffer cone() {
        if (cone != null) return cone;
        BufferBuilder b = Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int j = 0; j < RINGS; j++) {
            float v0 = j / (float) RINGS, v1 = (j + 1) / (float) RINGS;
            float r0 = radius(v0), r1 = radius(v1);
            for (int i = 0; i < SEGS; i++) {
                double a0 = Math.PI * 2.0 * i / SEGS, a1 = Math.PI * 2.0 * (i + 1) / SEGS;
                float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0);
                float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
                b.vertex(c0 * r0, v0, s0 * r0).endVertex();
                b.vertex(c1 * r0, v0, s1 * r0).endVertex();
                b.vertex(c1 * r1, v1, s1 * r1).endVertex();
                b.vertex(c0 * r1, v1, s0 * r1).endVertex();
            }
        }
        cone = new VertexBuffer(VertexBuffer.Usage.STATIC);
        cone.bind();
        cone.upload(b.end());
        VertexBuffer.unbind();
        return cone;
    }

    private static float radius(float v) {
        return (1f - v) * Mth.lerp((float) Math.sin(v * 20f), 0.89f, 1f);
    }

    static void release() {
        if (cone != null) {
            cone.close();
            cone = null;
        }
        if (target != null) {
            target.destroyBuffers();
            target = null;
        }
    }

    private ThornRenderer() {}
}
