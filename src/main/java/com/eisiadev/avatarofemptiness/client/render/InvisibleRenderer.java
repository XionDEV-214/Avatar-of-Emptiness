package com.eisiadev.avatarofemptiness.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class InvisibleRenderer<T extends Entity> extends EntityRenderer<T> {
    private static final ResourceLocation NONE = new ResourceLocation("minecraft", "textures/misc/white.png");

    public InvisibleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0f;
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        return false;
    }

    @Override
    public void render(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return NONE;
    }
}
