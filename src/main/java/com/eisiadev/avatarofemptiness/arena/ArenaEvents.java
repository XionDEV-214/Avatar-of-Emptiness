package com.eisiadev.avatarofemptiness.arena;

import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import com.eisiadev.avatarofemptiness.boss.AvatarEntity;
import com.eisiadev.avatarofemptiness.boss.BossPhase;
import com.eisiadev.avatarofemptiness.boss.pattern.Patterns;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = AvatarOfEmptinessMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArenaEvents {

    private static boolean isArena(LevelAccessor level) {
        return level instanceof Level l && Arena.isArena(l);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player p = event.player;
        if (p.isSpectator() || !Arena.isArena(p.level())) return;

        Vec3 pos = p.position();
        double margin = 0.6;
        if (Arena.inside(pos.x, pos.y, pos.z, margin)) return;

        if (p.level().isClientSide) {
            Vec3 c = Arena.clampInside(pos, margin);
            p.setPos(c.x, c.y, c.z);
            Vec3 n = pos.subtract(Arena.CENTER).normalize();
            Vec3 v = p.getDeltaMovement();
            double out = v.dot(n);
            if (out > 0) p.setDeltaMovement(v.subtract(n.scale(out)));
        } else if (p instanceof ServerPlayer sp && !Arena.inside(pos.x, pos.y, pos.z, -2.0)) {
            Vec3 c = Arena.clampInside(pos, margin + 0.5);
            sp.teleportTo(c.x, c.y, c.z);
        }
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (isArena(event.getLevel()) && !event.getPlayer().isCreative()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!isArena(event.getLevel())) return;
        if (event.getEntity() instanceof Player p && p.isCreative()) return;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (Arena.isArena(event.getLevel()) && !event.getEntity().isCreative()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!Arena.isArena(event.getLevel()) || event.getEntity().isCreative()) return;
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof BucketItem || stack.getItem() instanceof BlockItem) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp && Arena.isArena(sp.level())) {
            ServerLevel arena = (ServerLevel) sp.level();
            if (ArenaFight.findBoss(arena) == null) ArenaFight.sendHome(sp);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) CutsceneManager.tick(server);
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("aoe")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("start").executes(ctx -> {
                    ArenaFight.startFor(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("cutscene").executes(ctx -> {
                    CutsceneManager.start(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("stop").executes(ctx -> {
                    AvatarEntity boss = boss(ctx.getSource());
                    if (boss != null) boss.endFight(true);
                    return 1;
                }))
                .then(Commands.literal("phase")
                        .then(Commands.argument("n", IntegerArgumentType.integer(1, 4)).executes(ctx -> {
                            AvatarEntity boss = boss(ctx.getSource());
                            if (boss != null) boss.forcePhase(BossPhase.byNumber(IntegerArgumentType.getInteger(ctx, "n")));
                            return 1;
                        })))
                .then(Commands.literal("pattern")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                        Patterns.ALL.stream().map(Patterns.Entry::id), b))
                                .executes(ctx -> {
                                    AvatarEntity boss = boss(ctx.getSource());
                                    Patterns.Entry e = Patterns.byId(StringArgumentType.getString(ctx, "id"));
                                    if (boss == null || e == null) return 0;
                                    if (e.phase() != boss.phase()) boss.forcePhase(e.phase());
                                    boss.startPattern(e);
                                    return 1;
                                })))
        );
    }

    private static AvatarEntity boss(CommandSourceStack source) {
        ServerLevel arena = ArenaFight.arena(source.getServer());
        AvatarEntity boss = arena == null ? null : ArenaFight.findBoss(arena);
        if (boss == null) source.sendFailure(Component.literal("No Avatar of Emptiness is active."));
        return boss;
    }

    private ArenaEvents() {}
}
