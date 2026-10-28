package com.eisiadev.avatarofemptiness.client.render;

import com.eisiadev.avatarofemptiness.arena.Arena;
import com.eisiadev.avatarofemptiness.boss.BossTuning;
import com.eisiadev.avatarofemptiness.boss.hazard.Hazard;
import com.eisiadev.avatarofemptiness.boss.hazard.HazardType;
import com.eisiadev.avatarofemptiness.client.ClientHazards;
import com.eisiadev.avatarofemptiness.client.ModShaders;
import com.eisiadev.avatarofemptiness.client.model.BbModel;
import com.eisiadev.avatarofemptiness.client.model.BbModels;
import com.eisiadev.avatarofemptiness.registry.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

final class HazardRenderer {
    private static ItemStack cometStack;

    static void renderSolid(RenderLevelStageEvent event, ClientLevel level, Vec3 cam, float pt, float time) {
        if (ClientHazards.isEmpty()) return;
        renderStarMeshes(event, cam, pt, time);
        renderComets(event, level, cam, pt);
    }

    private static void renderStarMeshes(RenderLevelStageEvent event, Vec3 cam, float pt, float time) {
        ShaderInstance s = WorldFxRenderer.prep(ModShaders.MESH, time);
        boolean any = false;
        Matrix4f proj = RenderSystem.getProjectionMatrix();
        for (Hazard h : ClientHazards.all()) {
            if (!h.type.isStar()) continue;
            BbModel model = BbModels.get(switch (h.type) {
                case STAR_BLUE -> BbModels.BLUE_STAR;
                case STAR_RED -> BbModels.RED_GIANT;
                default -> BbModels.WHITE_DWARF;
            });
            if (model == null || model.radius <= 0f) continue;
            if (!any) {
                any = true;
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
                s.safeGetUniform("StaticMix").set(0f);
            }
            float age = h.age + pt;
            float appear = Mth.clamp(age / 6f, 0f, 1f);
            float scale = h.size / model.radius * appear;
            float x = (float) (h.lerpX(pt) - cam.x), y = (float) (h.lerpY(pt) - cam.y), z = (float) (h.lerpZ(pt) - cam.z);
            Matrix4f mv = new Matrix4f(event.getPoseStack().last().pose())
                    .translate(x, y, z)
                    .rotateY((float) Math.toRadians(h.a + age * 6f))
                    .scale(scale);
            s.safeGetUniform("Glow").set(0.6f * (1f - appear));
            s.safeGetUniform("Tint").set(1f, 1f, 1f, 1f);
            model.drawBody(mv, proj, s);
        }
        if (any) FxDraw.restoreState();
    }

    private static void renderComets(RenderLevelStageEvent event, ClientLevel level, Vec3 cam, float pt) {
        Minecraft mc = Minecraft.getInstance();
        if (cometStack == null) cometStack = new ItemStack(ModItems.REALITY_COMET.get());
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        PoseStack ps = event.getPoseStack();
        boolean any = false;
        for (Hazard h : ClientHazards.all()) {
            if (h.type != HazardType.COMET) continue;
            any = true;
            ps.pushPose();
            ps.translate(h.lerpX(pt) - cam.x, h.lerpY(pt) - cam.y, h.lerpZ(pt) - cam.z);
            ps.mulPose(Axis.YP.rotation(-h.a));
            ps.mulPose(Axis.XP.rotationDegrees((h.age + pt) * 25f));
            ps.scale(1.3f, 1.3f, 1.3f);
            mc.getItemRenderer().renderStatic(cometStack, ItemDisplayContext.NONE, LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY, ps, buffers, level, h.id);
            ps.popPose();
        }
        if (any) buffers.endBatch();
    }

    static void renderTranslucent(RenderLevelStageEvent event, Camera camera, float pt, float time) {
        if (ClientHazards.isEmpty()) return;
        Vec3 cam = camera.getPosition();
        Vector3f left = camera.getLeftVector();
        Vector3f up = camera.getUpVector();

        FxDraw.pushView(event.getPoseStack());
        FxDraw.translucentState();

        drawRings(cam, pt, time);
        drawPortals(cam, pt, time);
        drawParadiseFloor(cam, pt, time);
        drawShockwaves(cam, pt, time);
        drawDangerSpheres(cam, pt, time);
        drawBeams(cam, pt, time);
        drawTornadoes(cam, pt, time);
        drawStarSprites(cam, left, up, pt, time);
        drawIceChunks(cam, left, up, pt, time);
        drawBlackHoles(cam, left, up, pt, time);

        FxDraw.popView();
        FxDraw.restoreState();
    }

    private static float rx(Hazard h, Vec3 cam, float pt) { return (float) (h.lerpX(pt) - cam.x); }
    private static float ry(Hazard h, Vec3 cam, float pt) { return (float) (h.lerpY(pt) - cam.y); }
    private static float rz(Hazard h, Vec3 cam, float pt) { return (float) (h.lerpZ(pt) - cam.z); }

    private static boolean has(HazardType... types) {
        for (Hazard h : ClientHazards.all()) for (HazardType t : types) if (h.type == t) return true;
        return false;
    }

    private static void drawRings(Vec3 cam, float pt, float time) {
        if (!has(HazardType.RIFT_PULSE, HazardType.RIFT_BURST, HazardType.VORTEX)) return;
        ShaderInstance s = WorldFxRenderer.prep(ModShaders.RING, time);
        RenderSystem.setShaderTexture(0, WorldFxRenderer.noise("turbulent_noise"));
        FxDraw.blendAlpha();
        BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (Hazard h : ClientHazards.all()) {
            float age = h.age + pt;
            float k = Mth.clamp(age / Math.max(1, h.life), 0f, 1f);
            float x = rx(h, cam, pt), y = ry(h, cam, pt), z = rz(h, cam, pt);
            switch (h.type) {
                case RIFT_PULSE -> {
                    float r = h.size * (1f - k * k);
                    FxDraw.ring(b, x, y, z, Math.max(0f, r - 2.5f), r + 1.0f, 64, 0.06f, 0.0f, 0.08f, (float) Math.sin(k * Math.PI) * 0.85f);
                }
                case RIFT_BURST -> {
                    float e = 1f - (1f - k) * (1f - k);
                    float r = h.size * e;
                    FxDraw.ring(b, x, y, z, Math.max(0f, r - 4f * (1f - k) - 1f), r, 72, 0.55f, 0.02f, 0.05f, (1f - k) * 0.9f);
                }
                case VORTEX -> {
                    for (int i = 0; i < 4; i++) {
                        float kk = (age / 25f + i / 4f) % 1f;
                        float r = h.size * (1f - kk);
                        float a = (float) Math.sin(kk * Math.PI) * 0.8f * Mth.clamp((h.life - age) / 10f, 0f, 1f);
                        FxDraw.ring(b, x, y + i * 0.2f, z, Math.max(0f, r - 3f), r, 64, 0.08f, 0.02f, 0.1f, a);
                    }
                }
                default -> { }
            }
        }
        FxDraw.end(b);
    }

    private static void drawPortals(Vec3 cam, float pt, float time) {
        if (!has(HazardType.THORN_PORTAL, HazardType.BIG_PORTAL, HazardType.SUBZERO_PORTAL)) return;
        ShaderInstance s = WorldFxRenderer.prep(ModShaders.RIFT, time);
        RenderSystem.setShaderTexture(0, WorldFxRenderer.noise("wavy_blotch_noise"));
        RenderSystem.setShaderTexture(1, WorldFxRenderer.noise("dendritic_noise"));
        FxDraw.blendAlpha();
        BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (Hazard h : ClientHazards.all()) {
            boolean subzero = h.type == HazardType.SUBZERO_PORTAL;
            if (h.type != HazardType.THORN_PORTAL && h.type != HazardType.BIG_PORTAL && !subzero) continue;
            float age = h.age + pt;
            float open = Mth.clamp(age / 7f, 0f, 1f) * Mth.clamp((h.life - age) / 7f, 0f, 1f);
            open = 1f - (1f - open) * (1f - open);
            if (open <= 0.001f) continue;
            float half = h.size / 0.45f * (0.55f + 0.45f * open);
            float r = 1f, g = 0.04f, bl = 0.08f;
            if (subzero) {
                r = 0.3f;
                g = 0.85f;
                bl = 1f;
            }
            FxDraw.ground(b, rx(h, cam, pt), ry(h, cam, pt), rz(h, cam, pt), half, h.a, r, g, bl, open);
        }
        FxDraw.end(b);
    }

    private static void drawShockwaves(Vec3 cam, float pt, float time) {
        if (!has(HazardType.SHOCKWAVE)) return;
        ShaderInstance s = WorldFxRenderer.prep(ModShaders.SHOCKWAVE, time);
        FxDraw.blendAdditive();
        BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (Hazard h : ClientHazards.all()) {
            if (h.type != HazardType.SHOCKWAVE) continue;
            float age = h.age + pt;
            float radius = h.a * age;
            float fade = Mth.clamp((h.life - age) / 8f, 0f, 1f) * Mth.clamp(age / 2f, 0f, 1f);
            FxDraw.cylinderWall(b, rx(h, cam, pt), ry(h, cam, pt), rz(h, cam, pt), radius, 1.5f,
                    Math.max(32, (int) (radius * 3)), 1f, 1f, 1f, fade);
        }
        FxDraw.end(b);
    }

    private static void drawDangerSpheres(Vec3 cam, float pt, float time) {
        if (!has(HazardType.DISGUSTING_STAR, HazardType.DANGER_ZONE)) return;
        ShaderInstance s = WorldFxRenderer.prep(ModShaders.DANGER_SPHERE, time);
        FxDraw.blendAlpha();
        BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        for (Hazard h : ClientHazards.all()) {
            float age = h.age + pt;
            float x = rx(h, cam, pt), y = ry(h, cam, pt), z = rz(h, cam, pt);
            if (h.type == HazardType.DISGUSTING_STAR) {
                if (age < h.trigger) {
                    float appear = Mth.clamp(age / 6f, 0f, 1f);
                    float warn = Mth.clamp(age / h.trigger, 0f, 1f);
                    FxDraw.normalSphere(b, x, y, z, h.size * appear, 1f, 0.35f, 0.35f, 0.10f + 0.12f * warn);
                } else {
                    float k = Mth.clamp((age - h.trigger) / 8f, 0f, 1f);
                    FxDraw.normalSphere(b, x, y, z, h.size * (0.7f + 0.4f * k), 1f, 0.25f, 0.2f, 0.55f * (1f - k));
                }
            } else if (h.type == HazardType.DANGER_ZONE) {
                if (age < h.trigger) {
                    float progress = age / h.trigger;
                    int period = progress > 0.75f ? 2 : 4;
                    boolean red = ((int) (age / period)) % 2 == 0;
                    float a = 0.18f + 0.12f * progress;
                    if (red) FxDraw.normalSphere(b, x, y, z, h.size, 1f, 0.1f, 0.12f, a);
                    else FxDraw.normalSphere(b, x, y, z, h.size, 0.25f, 0.9f, 1f, a);
                } else {
                    float k = Mth.clamp((age - h.trigger) / 12f, 0f, 1f);
                    FxDraw.normalSphere(b, x, y, z, h.size * (1f + 0.15f * k), 1f, 1f, 1f, 0.7f * (1f - k));
                }
            }
        }
        FxDraw.end(b);
    }

    private static void drawBeams(Vec3 cam, float pt, float time) {
        boolean warn = has(HazardType.DISGUSTING_STAR, HazardType.DEATHRAY_WARN);
        boolean ray = has(HazardType.DEATHRAY);
        if (!warn && !ray) return;
        ShaderInstance s = WorldFxRenderer.prep(ModShaders.BEAM, time);
        RenderSystem.setShaderTexture(0, WorldFxRenderer.noise("turbulent_noise"));
        FxDraw.blendAlpha();

        if (warn) {
            s.safeGetUniform("Mode").set(1);
            BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (Hazard h : ClientHazards.all()) {
                float age = h.age + pt;
                if (h.type == HazardType.DISGUSTING_STAR && age < Math.min(h.trigger, 14)) {
                    float a = 1f - age / 14f;
                    FxDraw.beam(b, (float) (h.tx - cam.x), (float) (h.ty - cam.y), (float) (h.tz - cam.z),
                            rx(h, cam, pt), ry(h, cam, pt), rz(h, cam, pt), 0.12f, 1f, 1f, 0f, 0f, a);
                } else if (h.type == HazardType.DEATHRAY_WARN) {
                    float x = rx(h, cam, pt), y = ry(h, cam, pt), z = rz(h, cam, pt);
                    float dx = (float) Math.cos(h.a), dz = (float) Math.sin(h.a);
                    float a = Mth.clamp(age / 6f, 0f, 1f) * (0.55f + 0.45f * (float) Math.sin(age * 0.6f));
                    FxDraw.beam(b, x, y, z, x + dx * h.size, y, z + dz * h.size, 0.6f, 1f, 1f, 0f, 0f, a);
                }
            }
            FxDraw.end(b);
        }
        if (ray) {
            s.safeGetUniform("Mode").set(0);
            BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (Hazard h : ClientHazards.all()) {
                if (h.type != HazardType.DEATHRAY) continue;
                float age = h.age + pt;
                float ang = h.a + h.b * age;
                float grow = Mth.clamp(age / 4f, 0f, 1f) * Mth.clamp((h.life - age) / 4f, 0f, 1f);
                float x = rx(h, cam, pt), y = ry(h, cam, pt), z = rz(h, cam, pt);
                float dx = (float) Math.cos(ang), dz = (float) Math.sin(ang);
                FxDraw.beam(b, x, y, z, x + dx * h.size, y, z + dz * h.size, 1.8f * grow, 6f, 1f, 1f, 1f, 1f);
            }
            FxDraw.end(b);
        }
    }

    private static void drawTornadoes(Vec3 cam, float pt, float time) {
        boolean any = false;
        for (Hazard h : ClientHazards.all()) {
            if (h.type == HazardType.SUBZERO_PORTAL && h.age >= h.trigger) {
                any = true;
                break;
            }
        }
        if (!any) return;
        ShaderInstance s = WorldFxRenderer.prep(ModShaders.TORNADO, time);
        RenderSystem.setShaderTexture(0, WorldFxRenderer.noise("viscous_noise"));
        RenderSystem.setShaderTexture(1, WorldFxRenderer.noise("wavy_blotch_noise"));
        FxDraw.blendAlpha();
        BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_TEX_COLOR);
        final int segs = 16;
        final float height = BossTuning.TORNADO_HEIGHT;
        for (Hazard h : ClientHazards.all()) {
            if (h.type != HazardType.SUBZERO_PORTAL || h.age < h.trigger) continue;
            float age = h.age + pt;
            float grow = Mth.clamp((age - h.trigger) / 10f, 0f, 1f) * Mth.clamp((h.life - age) / 8f, 0f, 1f);
            float x = rx(h, cam, pt), y = ry(h, cam, pt), z = rz(h, cam, pt);
            float spin = h.a >= 0 ? 1f : 0f;
            for (int i = 0; i < segs; i++) {
                float t0 = (float) i / segs, t1 = (float) (i + 1) / segs;
                float w0 = h.size * (0.35f + 0.9f * (float) Math.pow(t0, 1.3)) * grow;
                float w1 = h.size * (0.35f + 0.9f * (float) Math.pow(t1, 1.3)) * grow;
                FxDraw.verticalStrip(b, x, y + height * t0 * grow, z, y + height * t1 * grow, w0, w1, t0, t1,
                        -1f, 1f, spin, 1f, 1f, grow);
            }
        }
        FxDraw.end(b);
    }

    private static void drawStarSprites(Vec3 cam, Vector3f left, Vector3f up, float pt, float time) {
        if (!has(HazardType.DISGUSTING_STAR)) return;
        ShaderInstance s = WorldFxRenderer.prep(ModShaders.STAR_SPRITE, time);
        RenderSystem.setShaderTexture(0, WorldFxRenderer.TEX_STAR);
        FxDraw.blendAdditive();
        BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (Hazard h : ClientHazards.all()) {
            if (h.type != HazardType.DISGUSTING_STAR) continue;
            float age = h.age + pt;
            float x = rx(h, cam, pt), y = ry(h, cam, pt), z = rz(h, cam, pt);
            if (age < h.trigger) {
                float appear = Mth.clamp(age / 4f, 0f, 1f);
                float urgency = Mth.clamp(age / h.trigger, 0f, 1f);
                float pulse = 1f + 0.18f * (float) Math.sin(age * (0.6f + urgency * 1.6f));
                FxDraw.billboard(b, x, y, z, left, up, h.size * 0.5f * appear * pulse, 1f, 1f, 1f, 0.9f);
            } else {
                float k = Mth.clamp((age - h.trigger) / 8f, 0f, 1f);
                FxDraw.billboard(b, x, y, z, left, up, 2f + h.size * 1.2f * k, 1f, 0.8f, 0.8f, 1f - k);
            }
        }
        FxDraw.end(b);
    }

    private static void drawIceChunks(Vec3 cam, Vector3f left, Vector3f up, float pt, float time) {
        if (!has(HazardType.ICE_CHUNK)) return;
        ShaderInstance s = WorldFxRenderer.prep(ModShaders.ICE_CHUNK, time);
        FxDraw.blendAlpha();
        BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (Hazard h : ClientHazards.all()) {
            if (h.type != HazardType.ICE_CHUNK) continue;
            float seed = (h.seed & 0xFFFF) / 65535f;
            FxDraw.billboard(b, rx(h, cam, pt), ry(h, cam, pt), rz(h, cam, pt), left, up, 0.9f, seed, 1f, 1f, 1f);
        }
        FxDraw.end(b);
    }

    private static void drawBlackHoles(Vec3 cam, Vector3f left, Vector3f up, float pt, float time) {
        if (!has(HazardType.BLACKHOLE)) return;
        ShaderInstance s = WorldFxRenderer.prep(ModShaders.BLACKHOLE, time);
        RenderSystem.setShaderTexture(0, WorldFxRenderer.noise("turbulent_noise"));
        FxDraw.blendAlpha();
        for (Hazard h : ClientHazards.all()) {
            if (h.type != HazardType.BLACKHOLE) continue;
            float age = h.age + pt;
            float form = Mth.clamp(age / 20f, 0f, 1f) * Mth.clamp((h.life - age) / 20f, 0f, 1f);
            form = 1f - (1f - form) * (1f - form);
            float x = rx(h, cam, pt), y = ry(h, cam, pt), z = rz(h, cam, pt);
            float horiz = (float) Math.sqrt(x * x + z * z);
            float elevation = (float) Math.atan2(-y, Math.max(0.001f, horiz));
            s.safeGetUniform("CamAngle").set(elevation);
            BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_TEX_COLOR);
            FxDraw.billboard(b, x, y, z, left, up, h.size / 0.387f * form, 1f, 1f, 1f, form);
            FxDraw.end(b);
        }
    }

    private static void drawParadiseFloor(Vec3 cam, float pt, float time) {
        if (!has(HazardType.PARADISE_FLOOR)) return;
        ShaderInstance s = WorldFxRenderer.prep(ModShaders.PARADISE_STATIC, time);
        FxDraw.blendAlpha();
        BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (Hazard h : ClientHazards.all()) {
            if (h.type != HazardType.PARADISE_FLOOR) continue;
            float y = ry(h, cam, pt);
            float half = (float) Arena.RADIUS + 16f;
            float x0 = (float) (Arena.CX - half - cam.x), x1 = (float) (Arena.CX + half - cam.x);
            float z0 = (float) (Arena.CZ - half - cam.z), z1 = (float) (Arena.CZ + half - cam.z);
            float uv = half * 2f * 3f;
            FxDraw.vtx(b, x0, y, z0, 0, 0, 1, 1, 1, 1);
            FxDraw.vtx(b, x0, y, z1, 0, uv, 1, 1, 1, 1);
            FxDraw.vtx(b, x1, y, z1, uv, uv, 1, 1, 1, 1);
            FxDraw.vtx(b, x1, y, z0, uv, 0, 1, 1, 1, 1);
        }
        FxDraw.end(b);
    }

    private HazardRenderer() {}
}
