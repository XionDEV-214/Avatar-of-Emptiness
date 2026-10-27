package com.eisiadev.avatarofemptiness.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public final class BbModel {
    public record Part(int texture, VertexBuffer buffer) {}

    private final List<Part> body;
    private final List<Part> head;
    private final ResourceLocation[] textures;
    public final Vector3f headPivot;
    public final float headForward;
    public final Vector3f center;
    public final Vector3f size;
    public final float radius;

    BbModel(List<Part> body, List<Part> head, ResourceLocation[] textures, Vector3f headPivot, float headForward,
            Vector3f center, Vector3f size, float radius) {
        this.body = body;
        this.head = head;
        this.textures = textures;
        this.headPivot = headPivot;
        this.headForward = headForward;
        this.center = center;
        this.size = size;
        this.radius = radius;
    }

    public boolean hasHead() {
        return !head.isEmpty();
    }

    public void drawBody(Matrix4f modelView, Matrix4f projection, ShaderInstance shader) {
        draw(body, modelView, projection, shader);
    }

    public void drawHead(Matrix4f modelView, Matrix4f projection, ShaderInstance shader) {
        draw(head, modelView, projection, shader);
    }

    private void draw(List<Part> parts, Matrix4f modelView, Matrix4f projection, ShaderInstance shader) {
        for (Part p : parts) {
            if (p.texture() >= 0 && p.texture() < textures.length && textures[p.texture()] != null) {
                RenderSystem.setShaderTexture(0, textures[p.texture()]);
            }
            p.buffer().bind();
            p.buffer().drawWithShader(modelView, projection, shader);
        }
        VertexBuffer.unbind();
    }

    public void close() {
        for (Part p : body) p.buffer().close();
        for (Part p : head) p.buffer().close();
        for (ResourceLocation rl : textures) {
            if (rl != null) Minecraft.getInstance().getTextureManager().release(rl);
        }
    }
}
