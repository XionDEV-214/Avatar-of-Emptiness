package com.eisiadev.avatarofemptiness.registry;

import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import com.eisiadev.avatarofemptiness.item.DimensionalCrasherItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AvatarOfEmptinessMod.MODID);

    public static final RegistryObject<Item> DIMENSIONAL_CRASHER = ITEMS.register("dimensional_crasher",
            () -> new DimensionalCrasherItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> REALITY_COMET = ITEMS.register("reality_comet",
            () -> new Item(new Item.Properties()));

    public static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(DIMENSIONAL_CRASHER);
        }
    }

    private ModItems() {}
}
