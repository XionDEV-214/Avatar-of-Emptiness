package com.eisiadev.avatarofemptiness.client.render;

import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import com.eisiadev.avatarofemptiness.arena.Arena;
import com.eisiadev.avatarofemptiness.client.ClientCutscene;
import com.eisiadev.avatarofemptiness.client.ModShaders;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public final class WorldFxRenderer {
    public static final ResourceLocation TEX_STAR = AvatarOfEmptinessMod.id("textures/fx/disgusting_star.png");
    public static final ResourceLocation TEX_PORTAL = AvatarOfEmptinessMod.id("textures/fx/otherworldly_portal.png");

    public static ResourceLocation noise(String name) {
        return AvatarOfEmptinessMod.id("textures/noise/" + name + ".png");
    }

    public static void render(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || !ModShaders.ready()) return;

        RenderLevelStageEvent.Stage stage = event.getStage();
        float pt = event.getPartialTick();
        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        float time = ((level.getGameTime() % 72000L) + pt) / 20f;
        boolean arena = Arena.isArena(level);

        if (stage == RenderLevelStageEvent.Stage.AFTER_SKY) {
            if (arena) DomeRenderer.renderDome(event, cam, time);
            if (ClientCutscene.riftScale(pt) > 0f) DomeRenderer.renderSkyRift(event, level, camera, pt, time);
        } else if (stage == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            if (arena) {
                BossRenderer.renderSolid(event, level, camera, pt, time);
                HazardRenderer.renderSolid(event, level, cam, pt, time);
            }
        } else if (stage == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            if (arena) {
                ThornRenderer.render(event, cam, pt);
                HazardRenderer.renderTranslucent(event, camera, pt, time);
                BossRenderer.renderTranslucent(event, level, camera, pt, time);
            }
        }
    }

    static ShaderInstance prep(ShaderInstance shader, float time) {
        shader.safeGetUniform("Time").set(time);
        return shader;
    }

    private WorldFxRenderer() {}
}
