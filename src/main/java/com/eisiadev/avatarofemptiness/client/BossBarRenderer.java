package com.eisiadev.avatarofemptiness.client;

import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import com.eisiadev.avatarofemptiness.boss.BossPhase;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = AvatarOfEmptinessMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BossBarRenderer {
    private static final int BAR_W = 260;
    private static final int BAR_H = 8;
    private static final int MARGIN = 4;
    private static final ResourceLocation NOISE = AvatarOfEmptinessMod.id("textures/noise/turbulent_noise.png");

    private static UUID trackedId;
    private static int trackedMode = -1;
    private static float trail = 1f;
    private static float lastProgress = 1f;
    private static float flash;
    private static float holdTimer;
    private static long lastNanos;

    @SubscribeEvent
    public static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        LerpingBossEvent boss = event.getBossEvent();
        int mode = modeOf(boss.getName());
        if (mode < 0 || ModShaders.BOSS_BAR == null) return;
        event.setCanceled(true);

        GuiGraphics g = event.getGuiGraphics();
        Font font = Minecraft.getInstance().font;
        int cx = g.guiWidth() / 2;
        int top = event.getY() - 9;
        float time = (Util.getMillis() % 3_600_000L) / 1000f;

        int barY;
        if (mode == 1 || mode == 2) {
            drawTitle(g, font, cx, top, mode, time);
            drawName(g, font, cx, top + 8, mode, time);
            barY = top + 19;
        } else {
            drawName(g, font, cx, top, mode, time);
            barY = top + 11;
        }

        float progress = Mth.clamp(boss.getProgress(), 0f, 1f);
        updateState(boss.getId(), mode, progress);
        drawBar(g, cx - BAR_W / 2, barY, progress, time, mode);

        event.setIncrement(barY + BAR_H + 8 - event.getY());
    }

    private static int modeOf(Component name) {
        if (!(name.getContents() instanceof TranslatableContents tc)) return -1;
        return switch (tc.getKey()) {
            case BossPhase.KEY_RIFT -> 0;
            case BossPhase.KEY_AVATAR -> 1;
            case BossPhase.KEY_WRATH -> 2;
            case BossPhase.KEY_PARADISE -> 3;
            default -> -1;
        };
    }

    private static void updateState(UUID id, int mode, float progress) {
        long now = System.nanoTime();
        float dt = lastNanos == 0 ? 0f : Math.min(0.1f, (now - lastNanos) / 1.0e9f);
        lastNanos = now;
        if (!id.equals(trackedId) || mode != trackedMode || progress > trail + 0.001f) {
            trackedId = id;
            trackedMode = mode;
            trail = progress;
            lastProgress = progress;
            flash = 0f;
            holdTimer = 0f;
            return;
        }
        if (progress < lastProgress - 1.0e-5f) {
            flash = Math.min(1f, flash + 0.6f);
            holdTimer = 0.45f;
        }
        lastProgress = progress;
        if (holdTimer > 0f) {
            holdTimer -= dt;
        } else if (trail > progress) {
            trail = Math.max(progress, trail - (trail - progress) * Math.min(1f, dt * 5f) - dt * 0.02f);
        }
        flash = Math.max(0f, flash - dt * 4f);
    }

    private static void drawBar(GuiGraphics g, int x, int y, float progress, float time, int mode) {
        g.flush();
        ShaderInstance s = ModShaders.BOSS_BAR;
        float w = BAR_W + MARGIN * 2, h = BAR_H + MARGIN * 2;
        s.safeGetUniform("Time").set(time);
        s.safeGetUniform("Progress").set(progress);
        s.safeGetUniform("Trail").set(trail);
        s.safeGetUniform("Flash").set(flash);
        s.safeGetUniform("Margin").set((float) MARGIN);
        s.safeGetUniform("Size").set(w, h);
        s.safeGetUniform("Mode").set(mode);
        RenderSystem.setShader(() -> s);
        RenderSystem.setShaderTexture(0, NOISE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        Matrix4f m = g.pose().last().pose();
        float x0 = x - MARGIN, y0 = y - MARGIN, x1 = x0 + w, y1 = y0 + h;
        BufferBuilder b = Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        b.vertex(m, x0, y0, 0f).uv(0f, 0f).color(1f, 1f, 1f, 1f).endVertex();
        b.vertex(m, x0, y1, 0f).uv(0f, 1f).color(1f, 1f, 1f, 1f).endVertex();
        b.vertex(m, x1, y1, 0f).uv(1f, 1f).color(1f, 1f, 1f, 1f).endVertex();
        b.vertex(m, x1, y0, 0f).uv(1f, 0f).color(1f, 1f, 1f, 1f).endVertex();
        BufferUploader.drawWithShader(b.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void drawTitle(GuiGraphics g, Font font, int cx, int y, int mode, float time) {
        String text = Component.translatable(BossPhase.KEY_TITLE).getString();
        int a = mode == 2 ? 0xE0A0A0 : 0xC9B8E8;
        int b = mode == 2 ? 0xFFD6C2 : 0xF2EAFF;
        MutableComponent c = gradient(text, a, b, time * 0.15f, false);
        float scale = 0.75f;
        g.pose().pushPose();
        g.pose().translate(cx, y, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, c, -font.width(c) / 2, 0, 0xFFFFFF, true);
        g.pose().popPose();
    }

    private static void drawName(GuiGraphics g, Font font, int cx, int y, int mode, float time) {
        MutableComponent c = switch (mode) {
            case 0 -> gradient(Component.translatable(BossPhase.KEY_RIFT).getString(), 0x9B30FF, 0xFF3D7F, time * 0.3f, false);
            case 1 -> gradient(Component.translatable(BossPhase.KEY_NAME).getString(), 0xFF2E4D, 0xFFB3C1, time * 0.3f, true);
            case 2 -> gradient(Component.translatable(BossPhase.KEY_NAME).getString(), 0xB00018, 0xFF5A36, time * 0.45f, true);
            default -> paradise(Component.translatable(BossPhase.KEY_PARADISE).getString(), time);
        };
        g.drawString(font, c, cx - font.width(c) / 2, y, 0xFFFFFF, true);
    }

    private static MutableComponent gradient(String text, int from, int to, float phase, boolean bold) {
        MutableComponent out = Component.empty();
        int len = Math.max(1, text.length());
        for (int i = 0; i < text.length(); i++) {
            float t = (i / (float) len - phase) % 1f;
            if (t < 0f) t += 1f;
            float k = t < 0.5f ? t * 2f : 2f - t * 2f;
            k = k * k * (3f - 2f * k);
            out.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(lerp(from, to, k))).withBold(bold)));
        }
        return out;
    }

    private static MutableComponent paradise(String text, float time) {
        MutableComponent out = Component.empty();
        int frame = (int) (time * 12f);
        for (int i = 0; i < text.length(); i++) {
            int h = (i * 73856093) ^ (frame * 19349663);
            h ^= h >>> 13;
            boolean black = ((h * 0x5bd1e995) >>> 28) % 2 == 0;
            int color = black ? 0x101010 : 0xFFFFFF;
            out.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)).withBold(true)));
        }
        return out;
    }

    private static int lerp(int a, int b, float f) {
        int r = (int) Mth.lerp(f, (a >> 16) & 0xFF, (b >> 16) & 0xFF);
        int gr = (int) Mth.lerp(f, (a >> 8) & 0xFF, (b >> 8) & 0xFF);
        int bl = (int) Mth.lerp(f, a & 0xFF, b & 0xFF);
        return (r << 16) | (gr << 8) | bl;
    }

    private BossBarRenderer() {}
}
