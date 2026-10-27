package com.eisiadev.avatarofemptiness.client;

import com.eisiadev.avatarofemptiness.AoeConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.joml.Matrix4f;

public final class FlashOverlay {
    public static void renderEclipse(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        float e = ClientCutscene.eclipse(partialTick);
        if (e <= 0.001f) return;
        e = e * e * (3f - 2f * e);
        float r = Mth.lerp(e, 1f, 0.92f);
        float g = Mth.lerp(e, 1f, 0.40f);
        float b = Mth.lerp(e, 1f, 0.38f);

        graphics.flush();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f m = graphics.pose().last().pose();
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(m, 0, 0, 0).color(r, g, b, 1f).endVertex();
        buf.vertex(m, 0, height, 0).color(r, g, b, 1f).endVertex();
        buf.vertex(m, width, height, 0).color(r, g, b, 1f).endVertex();
        buf.vertex(m, width, 0, 0).color(r, g, b, 1f).endVertex();
        BufferUploader.drawWithShader(buf.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        float fade = ClientCutscene.fade(partialTick);
        if (fade > 0f) {
            int a = (int) (Math.min(1f, fade) * 255);
            graphics.fill(0, 0, width, height, a << 24);
        }
        int flash = ClientArenaState.flashArgb(partialTick);
        if (flash != 0) {
            float intensity = AoeConfig.FLASH_INTENSITY.get().floatValue();
            int a = (int) (((flash >>> 24) & 0xFF) * intensity);
            if (a > 0) graphics.fill(0, 0, width, height, (a << 24) | (flash & 0xFFFFFF));
        }
    }

    private FlashOverlay() {}
}
