package com.eisiadev.avatarofemptiness.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ScreenShatter {
    private static final int CRACK = 6;
    private static final int DURATION = 50;
    private static final int FADE = 16;
    private static final int GRID_X = 11;
    private static final int GRID_Y = 7;

    @Nullable
    private static TextureTarget capture;
    @Nullable
    private static List<Shard> shards;
    private static boolean pending;
    private static boolean white;
    private static int age = -1;

    private record Shard(float[] xs, float[] ys, float cx, float cy,
                         float vx, float vy, float spin, float grow) {}

    public static void trigger(boolean whiteShards) {
        white = whiteShards;
        pending = true;
        age = 0;
        shards = null;
    }

    public static void reset() {
        pending = false;
        age = -1;
        shards = null;
    }

    public static void tick() {
        if (age < 0 || pending) return;
        age++;
        if (age > DURATION) reset();
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        if (age < 0) return;
        if (pending) {
            graphics.flush();
            if (!white) captureFrame();
            shards = build(width, height, new Random());
            pending = false;
        }
        if (shards == null || (!white && capture == null)) return;

        float t = age + partialTick;
        float s = Math.max(0f, t - CRACK);
        float alpha = 1f - Mth.clamp((t - (DURATION - FADE)) / FADE, 0f, 1f);
        float scaleRef = width / 480f;
        float gravity = 0.55f * scaleRef;

        graphics.flush();
        Matrix4f m = graphics.pose().last().pose();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        BufferBuilder b = Tesselator.getInstance().getBuilder();
        if (white) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            b.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, capture.getColorTextureId());
            b.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
        }
        for (Shard sh : shards) {
            float ox = sh.vx * s;
            float oy = sh.vy * s + 0.5f * gravity * s * s;
            float ang = sh.spin * s;
            float sc = 1f + sh.grow * s;
            float cos = Mth.cos(ang) * sc, sin = Mth.sin(ang) * sc;
            float shade = 1f - (white ? 0.12f : 0.25f) * Mth.clamp(s / 20f, 0f, 1f);
            for (int i = 0; i < 3; i++) {
                float px = sh.xs[i] - sh.cx, py = sh.ys[i] - sh.cy;
                float x = sh.cx + ox + px * cos - py * sin;
                float y = sh.cy + oy + px * sin + py * cos;
                float u = sh.xs[i] / width;
                float v = 1f - sh.ys[i] / height;
                if (white) {
                    b.vertex(m, x, y, 0f).color(shade, shade, Math.min(1f, shade + 0.05f), alpha).endVertex();
                } else {
                    b.vertex(m, x, y, 0f).uv(u, v).color(shade, shade, shade, alpha).endVertex();
                }
            }
        }
        BufferUploader.drawWithShader(b.end());

        float crack = 1f - Mth.clamp((t - CRACK) / 8f, 0f, 1f);
        if (crack > 0f) {
            float ca = Mth.clamp(t / 2f, 0f, 1f) * crack;
            float lc = white ? 0.62f : 1f;
            float lb = white ? 0.72f : 1f;
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.lineWidth(Math.max(1.5f, scaleRef * 1.5f));
            b.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            for (Shard sh : shards) {
                for (int i = 0; i < 3; i++) {
                    int j = (i + 1) % 3;
                    float ox = sh.vx * s;
                    float oy = sh.vy * s + 0.5f * gravity * s * s;
                    b.vertex(m, sh.xs[i] + ox, sh.ys[i] + oy, 0f).color(lc, lc, lb, ca).endVertex();
                    b.vertex(m, sh.xs[j] + ox, sh.ys[j] + oy, 0f).color(lc, lc, lb, ca).endVertex();
                }
            }
            BufferUploader.drawWithShader(b.end());
            RenderSystem.lineWidth(1f);
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void captureFrame() {
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        if (capture == null) {
            capture = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
        } else if (capture.width != main.width || capture.height != main.height) {
            capture.resize(main.width, main.height, Minecraft.ON_OSX);
        }
        capture.setFilterMode(GL11.GL_LINEAR);
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, capture.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, main.width, main.height, 0, 0, capture.width, capture.height,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        main.bindWrite(true);
    }

    private static List<Shard> build(int width, int height, Random rng) {
        float[][] gx = new float[GRID_X + 1][GRID_Y + 1];
        float[][] gy = new float[GRID_X + 1][GRID_Y + 1];
        float cw = width / (float) GRID_X, ch = height / (float) GRID_Y;
        for (int i = 0; i <= GRID_X; i++) {
            for (int j = 0; j <= GRID_Y; j++) {
                float jx = (i == 0 || i == GRID_X) ? 0f : (rng.nextFloat() - 0.5f) * cw * 0.7f;
                float jy = (j == 0 || j == GRID_Y) ? 0f : (rng.nextFloat() - 0.5f) * ch * 0.7f;
                gx[i][j] = i * cw + jx;
                gy[i][j] = j * ch + jy;
            }
        }
        float mx = width * 0.5f, my = height * 0.5f;
        float scaleRef = width / 480f;
        List<Shard> out = new ArrayList<>();
        for (int i = 0; i < GRID_X; i++) {
            for (int j = 0; j < GRID_Y; j++) {
                float[] ax = {gx[i][j], gx[i + 1][j], gx[i + 1][j + 1], gx[i][j + 1]};
                float[] ay = {gy[i][j], gy[i + 1][j], gy[i + 1][j + 1], gy[i][j + 1]};
                if (rng.nextBoolean()) {
                    out.add(shard(new float[]{ax[0], ax[1], ax[2]}, new float[]{ay[0], ay[1], ay[2]}, mx, my, scaleRef, rng));
                    out.add(shard(new float[]{ax[0], ax[2], ax[3]}, new float[]{ay[0], ay[2], ay[3]}, mx, my, scaleRef, rng));
                } else {
                    out.add(shard(new float[]{ax[0], ax[1], ax[3]}, new float[]{ay[0], ay[1], ay[3]}, mx, my, scaleRef, rng));
                    out.add(shard(new float[]{ax[1], ax[2], ax[3]}, new float[]{ay[1], ay[2], ay[3]}, mx, my, scaleRef, rng));
                }
            }
        }
        return out;
    }

    private static Shard shard(float[] xs, float[] ys, float mx, float my, float scaleRef, Random rng) {
        float cx = (xs[0] + xs[1] + xs[2]) / 3f;
        float cy = (ys[0] + ys[1] + ys[2]) / 3f;
        float dx = cx - mx, dy = cy - my;
        float len = Math.max(1f, Mth.sqrt(dx * dx + dy * dy));
        float speed = (2.5f + rng.nextFloat() * 5.5f) * scaleRef;
        float vx = dx / len * speed + (rng.nextFloat() - 0.5f) * 2f * scaleRef;
        float vy = dy / len * speed - (2f + rng.nextFloat() * 3f) * scaleRef;
        float spin = (rng.nextFloat() - 0.5f) * 0.22f;
        float grow = 0.01f + rng.nextFloat() * 0.03f;
        return new Shard(xs, ys, cx, cy, vx, vy, spin, grow);
    }

    private ScreenShatter() {}
}
