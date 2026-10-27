package com.eisiadev.avatarofemptiness.client.model;

import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class BbModels {
    public static final String AVATAR = "avatar_of_emptiness_false";
    public static final String WHITE_DWARF = "white_dwarf";
    public static final String BLUE_STAR = "blue_star";
    public static final String RED_GIANT = "red_giant";

    private static final Map<String, Optional<BbModel>> CACHE = new HashMap<>();

    public static final ResourceManagerReloadListener RELOAD_LISTENER = BbModels::onReload;

    @Nullable
    public static BbModel get(String name) {
        RenderSystem.assertOnRenderThread();
        return CACHE.computeIfAbsent(name, n -> BbModelLoader.load(
                Minecraft.getInstance().getResourceManager(),
                AvatarOfEmptinessMod.id("bbmodel/" + n + ".bbmodel"), n)).orElse(null);
    }

    private static void onReload(ResourceManager manager) {
        RenderSystem.recordRenderCall(() -> {
            CACHE.values().forEach(o -> o.ifPresent(BbModel::close));
            CACHE.clear();
        });
    }

    private BbModels() {}
}
