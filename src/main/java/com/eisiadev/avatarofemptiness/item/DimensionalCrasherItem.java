package com.eisiadev.avatarofemptiness.item;

import com.eisiadev.avatarofemptiness.arena.Arena;
import com.eisiadev.avatarofemptiness.arena.CutsceneManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DimensionalCrasherItem extends Item {

    public DimensionalCrasherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (Arena.isArena(level)) {
                sp.displayClientMessage(Component.translatable("item.avatarofemptiness.dimensional_crasher.in_arena")
                        .withStyle(ChatFormatting.DARK_RED), true);
                return InteractionResultHolder.fail(stack);
            }
            if (CutsceneManager.isActive(sp)) {
                return InteractionResultHolder.fail(stack);
            }
            CutsceneManager.start(sp);
            sp.getCooldowns().addCooldown(this, CutsceneManager.DURATION + 40);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.avatarofemptiness.dimensional_crasher.desc").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
