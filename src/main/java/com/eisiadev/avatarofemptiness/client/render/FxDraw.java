package com.eisiadev.avatarofemptiness.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Vector3f;

public final class FxDraw {
    private static final int SPHERE_SLICES = 24;
    private static final int SPHERE_STACKS = 14;
    private static final float[][][] UNIT_SPHERE = new float[SPHERE_STACKS + 1][SPHERE_SLICES + 1][3];

    static {
        for (int i = 0; i <= SPHERE_STACKS; i++) {
            double phi = Math.PI * i / SPHERE_STACKS;
            for (int j = 0; j <= SPHERE_SLICES; j++) {
                double th = Math.PI * 2.0 * j / SPHERE_SLICES;
                UNIT_SPHERE[i][j][0] = (float) (Math.sin(phi) * Math.cos(th));
                UNIT_SPHERE[i][j][1] = (float) Math.cos(phi);
                UNIT_SPHERE[i][j][2] = (float) (Math.sin(phi) * Math.sin(th));
            }
        }
    }

    public static void pushView(PoseStack levelPose) {
        PoseStack mv = RenderSystem.getModelViewStack();
        mv.pushPose();
        mv.mulPoseMatrix(levelPose.last().pose());
        RenderSystem.applyModelViewMatrix();
    }

    public static void popView() {
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static void blendAlpha() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    public static void blendAdditive() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
    }

    public static void translucentState() {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
    }

    public static void restoreState() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public static BufferBuilder begin(ShaderInstance shader, VertexFormat format) {
        RenderSystem.setShader(() -> shader);
        BufferBuilder b = Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.QUADS, format);
        return b;
    }

    public static void end(BufferBuilder b) {
        BufferUploader.drawWithShader(b.end());
    }

    public static void vtx(BufferBuilder b, float x, float y, float z, float u, float v, float r, float g, float bl, float a) {
        b.vertex(x, y, z).uv(u, v).color(r, g, bl, a).endVertex();
    }

    public static void billboard(BufferBuilder b, float x, float y, float z, Vector3f left, Vector3f up,
                                 float half, float r, float g, float bl, float a) {
        billboard(b, x, y, z, left, up, half, half, r, g, bl, a);
    }

    public static void billboard(BufferBuilder b, float x, float y, float z, Vector3f left, Vector3f up,
                                 float hw, float hh, float r, float g, float bl, float a) {
        float lx = left.x() * hw, ly = left.y() * hw, lz = left.z() * hw;
        float ux = up.x() * hh, uy = up.y() * hh, uz = up.z() * hh;
        vtx(b, x + lx + ux, y + ly + uy, z + lz + uz, 0, 0, r, g, bl, a);
        vtx(b, x + lx - ux, y + ly - uy, z + lz - uz, 0, 1, r, g, bl, a);
        vtx(b, x - lx - ux, y - ly - uy, z - lz - uz, 1, 1, r, g, bl, a);
        vtx(b, x - lx + ux, y - ly + uy, z - lz + uz, 1, 0, r, g, bl, a);
    }

    public static void ground(BufferBuilder b, float x, float y, float z, float half, float rotation,
                              float r, float g, float bl, float a) {
        float c = (float) Math.cos(rotation) * half, s = (float) Math.sin(rotation) * half;
        vtx(b, x - c + s, y, z - s - c, 0, 0, r, g, bl, a);
        vtx(b, x - c - s, y, z - s + c, 0, 1, r, g, bl, a);
        vtx(b, x + c - s, y, z + s + c, 1, 1, r, g, bl, a);
        vtx(b, x + c + s, y, z + s - c, 1, 0, r, g, bl, a);
    }

    public static void verticalStrip(BufferBuilder b, float x, float yBottom, float z, float yTop,
                                     float halfWidthBottom, float halfWidthTop, float vBottom, float vTop,
                                     float uLeft, float uRight, float r, float g, float bl, float a) {
        float len = (float) Math.sqrt(x * x + z * z);
        float sx, sz;
        if (len < 1.0e-4f) {
            sx = 1f;
            sz = 0f;
        } else {
            sx = -z / len;
            sz = x / len;
        }
        vtx(b, x - sx * halfWidthTop, yTop, z - sz * halfWidthTop, uLeft, vTop, r, g, bl, a);
        vtx(b, x - sx * halfWidthBottom, yBottom, z - sz * halfWidthBottom, uLeft, vBottom, r, g, bl, a);
        vtx(b, x + sx * halfWidthBottom, yBottom, z + sz * halfWidthBottom, uRight, vBottom, r, g, bl, a);
        vtx(b, x + sx * halfWidthTop, yTop, z + sz * halfWidthTop, uRight, vTop, r, g, bl, a);
    }

    public static void beam(BufferBuilder b, float x0, float y0, float z0, float x1, float y1, float z1,
                            float halfWidth, float uScale, float r, float g, float bl, float a) {
        float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        float mx = -(x0 + x1) * 0.5f, my = -(y0 + y1) * 0.5f, mz = -(z0 + z1) * 0.5f;
        float sx = dy * mz - dz * my, sy = dz * mx - dx * mz, sz = dx * my - dy * mx;
        float sl = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
        if (sl < 1.0e-5f) return;
        sx = sx / sl * halfWidth;
        sy = sy / sl * halfWidth;
        sz = sz / sl * halfWidth;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) / uScale;
        vtx(b, x0 + sx, y0 + sy, z0 + sz, 0, 0, r, g, bl, a);
        vtx(b, x0 - sx, y0 - sy, z0 - sz, 0, 1, r, g, bl, a);
        vtx(b, x1 - sx, y1 - sy, z1 - sz, len, 1, r, g, bl, a);
        vtx(b, x1 + sx, y1 + sy, z1 + sz, len, 0, r, g, bl, a);
    }

    public static void ring(BufferBuilder b, float cx, float cy, float cz, float inner, float outer, int segments,
                            float r, float g, float bl, float a) {
        for (int i = 0; i < segments; i++) {
            double t0 = Math.PI * 2.0 * i / segments, t1 = Math.PI * 2.0 * (i + 1) / segments;
            float c0 = (float) Math.cos(t0), s0 = (float) Math.sin(t0), c1 = (float) Math.cos(t1), s1 = (float) Math.sin(t1);
            float v0 = (float) i / segments, v1 = (float) (i + 1) / segments;
            vtx(b, cx + c0 * inner, cy, cz + s0 * inner, 0, v0, r, g, bl, a);
            vtx(b, cx + c0 * outer, cy, cz + s0 * outer, 1, v0, r, g, bl, a);
            vtx(b, cx + c1 * outer, cy, cz + s1 * outer, 1, v1, r, g, bl, a);
            vtx(b, cx + c1 * inner, cy, cz + s1 * inner, 0, v1, r, g, bl, a);
        }
    }

    public static void cylinderWall(BufferBuilder b, float cx, float y, float cz, float radius, float height, int segments,
                                    float r, float g, float bl, float a) {
        for (int i = 0; i < segments; i++) {
            double t0 = Math.PI * 2.0 * i / segments, t1 = Math.PI * 2.0 * (i + 1) / segments;
            float x0 = cx + (float) Math.cos(t0) * radius, z0 = cz + (float) Math.sin(t0) * radius;
            float x1 = cx + (float) Math.cos(t1) * radius, z1 = cz + (float) Math.sin(t1) * radius;
            float u0 = (float) i / segments, u1 = (float) (i + 1) / segments;
            vtx(b, x0, y + height, z0, u0, 1, r, g, bl, a);
            vtx(b, x0, y, z0, u0, 0, r, g, bl, a);
            vtx(b, x1, y, z1, u1, 0, r, g, bl, a);
            vtx(b, x1, y + height, z1, u1, 1, r, g, bl, a);
        }
    }

    public static void uvSphere(BufferBuilder b, float cx, float cy, float cz, float radius, int slices, int stacks,
                                float r, float g, float bl, float a) {
        for (int i = 0; i < stacks; i++) {
            double p0 = Math.PI * i / stacks, p1 = Math.PI * (i + 1) / stacks;
            for (int j = 0; j < slices; j++) {
                double t0 = Math.PI * 2.0 * j / slices, t1 = Math.PI * 2.0 * (j + 1) / slices;
                sphereVtx(b, cx, cy, cz, radius, p0, t0, (float) j / slices, (float) i / stacks, r, g, bl, a);
                sphereVtx(b, cx, cy, cz, radius, p1, t0, (float) j / slices, (float) (i + 1) / stacks, r, g, bl, a);
                sphereVtx(b, cx, cy, cz, radius, p1, t1, (float) (j + 1) / slices, (float) (i + 1) / stacks, r, g, bl, a);
                sphereVtx(b, cx, cy, cz, radius, p0, t1, (float) (j + 1) / slices, (float) i / stacks, r, g, bl, a);
            }
        }
    }

    private static void sphereVtx(BufferBuilder b, float cx, float cy, float cz, float radius, double phi, double th,
                                  float u, float v, float r, float g, float bl, float a) {
        float sx = (float) (Math.sin(phi) * Math.cos(th)), sy = (float) Math.cos(phi), sz = (float) (Math.sin(phi) * Math.sin(th));
        vtx(b, cx + sx * radius, cy + sy * radius, cz + sz * radius, u, v, r, g, bl, a);
    }

    public static void normalSphere(BufferBuilder b, float cx, float cy, float cz, float radius,
                                    float r, float g, float bl, float a) {
        for (int i = 0; i < SPHERE_STACKS; i++) {
            for (int j = 0; j < SPHERE_SLICES; j++) {
                nVtx(b, cx, cy, cz, radius, UNIT_SPHERE[i][j], r, g, bl, a);
                nVtx(b, cx, cy, cz, radius, UNIT_SPHERE[i + 1][j], r, g, bl, a);
                nVtx(b, cx, cy, cz, radius, UNIT_SPHERE[i + 1][j + 1], r, g, bl, a);
                nVtx(b, cx, cy, cz, radius, UNIT_SPHERE[i][j + 1], r, g, bl, a);
            }
        }
    }

    private static void nVtx(BufferBuilder b, float cx, float cy, float cz, float radius, float[] n,
                             float r, float g, float bl, float a) {
        b.vertex(cx + n[0] * radius, cy + n[1] * radius, cz + n[2] * radius).color(r, g, bl, a).normal(n[0], n[1], n[2]).endVertex();
    }

    private FxDraw() {}
}
