package com.eisiadev.avatarofemptiness.client.render;

import com.eisiadev.avatarofemptiness.boss.AvatarEntity;
import com.eisiadev.avatarofemptiness.boss.BossTuning;
import com.eisiadev.avatarofemptiness.boss.VisceraEntity;
import com.eisiadev.avatarofemptiness.client.ModShaders;
import com.eisiadev.avatarofemptiness.client.model.BbModel;
import com.eisiadev.avatarofemptiness.client.model.BbModels;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

final class BossRenderer {
    private static final float RIFT_HEIGHT = 3.6f;
    private static final float RIFT_HALF = 8f;

    static void renderSolid(RenderLevelStageEvent event, ClientLevel level, Camera camera, float pt, float time) {
        for (Entity e : level.entitiesForRendering()) {
            if (!(e instanceof AvatarEntity boss)) continue;
            int visual = boss.getVisual();
            if (visual != AvatarEntity.VISUAL_MODEL && visual != AvatarEntity.VISUAL_STATIC) continue;
            renderModel(event, boss, camera, pt, time, visual == AvatarEntity.VISUAL_STATIC);
        }
    }

    private static void renderModel(RenderLevelStageEvent event, AvatarEntity boss, Camera camera, float pt, float time,
                                     boolean staticMode) {
        BbModel model = BbModels.get(BbModels.AVATAR);
        if (model == null) return;
        Vec3 cam = camera.getPosition();
        float x = (float) (Mth.lerp(pt, boss.xo, boss.getX()) - cam.x);
        float y = (float) (Mth.lerp(pt, boss.yo, boss.getY()) - cam.y);
        float z = (float) (Mth.lerp(pt, boss.zo, boss.getZ()) - cam.z);

        float scale = 1f;
        float glow = 0f;
        int trans = boss.getTransitionTicks();
        if (boss.getPhaseNumber() == 2 && trans > 0) {
            float k = 1f - (trans - pt) / BossTuning.PHASE_TRANSITION_TICKS;
            k = Mth.clamp(k, 0f, 1f);
            scale = 0.2f + 0.8f * (1f - (1f - k) * (1f - k) * (1f - k));
            glow = (1f - k) * 2f;
        }

        ShaderInstance s = WorldFxRenderer.prep(ModShaders.MESH, time);
        s.safeGetUniform("Tint").set(1f, 1f, 1f, 1f);
        s.safeGetUniform("StaticMix").set(staticMode ? 1f : 0f);
        s.safeGetUniform("Glow").set(glow);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        Matrix4f proj = RenderSystem.getProjectionMatrix();
        Vector3f c = model.center;
        float spinDeg = (time * 60f) % 360f + boss.getViewYRot(pt);
        Matrix4f body = new Matrix4f(event.getPoseStack().last().pose())
                .translate(x + c.x * scale, y + c.y * scale, z + c.z * scale)
                .rotateY((float) Math.toRadians(-spinDeg))
                .scale(scale);
        model.drawBody(body, proj, s);

        if (model.hasHead()) {
            Vector3f hp = model.headPivot;
            float hx = x + (c.x + hp.x) * scale, hy = y + (c.y + hp.y) * scale, hz = z + (c.z + hp.z) * scale;
            float camAngle = (float) Math.atan2(-hz, -hx);
            Matrix4f head = new Matrix4f(event.getPoseStack().last().pose())
                    .translate(hx, hy, hz)
                    .rotateY(model.headForward - camAngle)
                    .scale(scale);
            model.drawHead(head, proj, s);
        }
        FxDraw.restoreState();
    }

    static void renderTranslucent(RenderLevelStageEvent event, ClientLevel level, Camera camera, float pt, float time) {
        Vec3 cam = camera.getPosition();
        Vector3f left = camera.getLeftVector();
        Vector3f up = camera.getUpVector();
        boolean began = false;

        for (Entity e : level.entitiesForRendering()) {
            if (e instanceof AvatarEntity boss) {
                float scale = riftScale(boss, pt);
                if (scale <= 0.001f) continue;
                if (!began) {
                    FxDraw.pushView(event.getPoseStack());
                    FxDraw.translucentState();
                    began = true;
                }
                ShaderInstance s = WorldFxRenderer.prep(ModShaders.RIFT, time);
                RenderSystem.setShaderTexture(0, WorldFxRenderer.noise("wavy_blotch_noise"));
                RenderSystem.setShaderTexture(1, WorldFxRenderer.noise("dendritic_noise"));
                FxDraw.blendAlpha();
                float x = (float) (Mth.lerp(pt, e.xo, e.getX()) - cam.x);
                float y = (float) (Mth.lerp(pt, e.yo, e.getY()) - cam.y) + RIFT_HEIGHT;
                float z = (float) (Mth.lerp(pt, e.zo, e.getZ()) - cam.z);
                BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_TEX_COLOR);
                FxDraw.billboard(b, x, y, z, left, up, RIFT_HALF * scale, 1f, 0.04f, 0.08f, Mth.clamp(scale, 0f, 1f));
                FxDraw.end(b);
            } else if (e instanceof VisceraEntity) {
                if (!began) {
                    FxDraw.pushView(event.getPoseStack());
                    FxDraw.translucentState();
                    began = true;
                }
                ShaderInstance s = WorldFxRenderer.prep(ModShaders.VISCERA, time);
                RenderSystem.setShaderTexture(0, WorldFxRenderer.noise("wavy_blotch_noise"));
                FxDraw.blendAlpha();
                float x = (float) (Mth.lerp(pt, e.xo, e.getX()) - cam.x);
                float y = (float) (Mth.lerp(pt, e.yo, e.getY()) - cam.y) + e.getBbHeight() * 0.5f;
                float z = (float) (Mth.lerp(pt, e.zo, e.getZ()) - cam.z);
                float pulse = 1f + 0.06f * (float) Math.sin((e.tickCount + pt) * 0.35f);
                BufferBuilder b = FxDraw.begin(s, DefaultVertexFormat.POSITION_TEX_COLOR);
                FxDraw.billboard(b, x, y, z, left, up, 1.7f * pulse, (e.getId() % 97) / 97f, 1f, 1f, 1f);
                FxDraw.end(b);
            }
        }
        if (began) {
            FxDraw.popView();
            FxDraw.restoreState();
        }
    }

    private static float riftScale(AvatarEntity boss, float pt) {
        int visual = boss.getVisual();
        int phase = boss.getPhaseNumber();
        int trans = boss.getTransitionTicks();
        if (visual == AvatarEntity.VISUAL_RIFT) {
            float k = Mth.clamp((boss.tickCount + pt) / BossTuning.INTRO_TICKS, 0f, 1f);
            return 1f - (1f - k) * (1f - k) * (1f - k);
        }
        if (phase == 2 && trans > 0 && visual == AvatarEntity.VISUAL_MODEL) {
            float k = (trans - pt) / BossTuning.PHASE_TRANSITION_TICKS;
            return Mth.clamp(k, 0f, 1f);
        }
        return 0f;
    }

    private BossRenderer() {}
}
