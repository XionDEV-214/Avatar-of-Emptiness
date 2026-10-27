package com.eisiadev.avatarofemptiness.registry;

import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AvatarOfEmptinessMod.MODID);

    public static final RegistryObject<SoundEvent> HURT_RIFT = reg("aoe_hurt_rift");
    public static final RegistryObject<SoundEvent> HURT = reg("aoe_hurt");

    public static final RegistryObject<SoundEvent> RIFT_ABSORB = reg("aoe_rift_absorb");
    public static final RegistryObject<SoundEvent> RIFT_EJECT = reg("aoe_rift_eject");
    public static final RegistryObject<SoundEvent> RIFT_OPEN = reg("aoe_rift_open");

    public static final RegistryObject<SoundEvent> BLOOD_CRY = reg("aoe_blood_cry");
    public static final RegistryObject<SoundEvent> BLOOD_FOUNTAIN = reg("aoe_blood_fountain");
    public static final RegistryObject<SoundEvent> BLOOD_EX = reg("aoe_blood_ex");
    public static final RegistryObject<SoundEvent> BLOOD_STAR_SEVER = reg("aoe_blood_star_sever");
    public static final RegistryObject<SoundEvent> BLOOD_STAR_SUMMON = reg("aoe_blood_star_summon");
    public static final RegistryObject<SoundEvent> VISCERAL_LOOP = reg("aoe_visceral_loop");
    public static final RegistryObject<SoundEvent> VISCERAL_ACTIVE = reg("aoe_visceral_active");
    public static final RegistryObject<SoundEvent> VISCERAL_START = reg("aoe_visceral_start");
    public static final RegistryObject<SoundEvent> THORN_CHARGE = reg("aoe_visceral_thorn_charge");
    public static final RegistryObject<SoundEvent> THORN_SHOOT = reg("aoe_visceral_thorn_shoot");
    public static final RegistryObject<SoundEvent> THORN_RETRACT = reg("aoe_visceral_thorn_retract");

    public static final RegistryObject<SoundEvent> ABSOLUTE_ZERO_CHARGE = reg("aoe_absolute_zero_charge");
    public static final RegistryObject<SoundEvent> ABSOLUTE_ZERO_IMPACT = reg("aoe_absolute_zero_impact");
    public static final RegistryObject<SoundEvent> SUBZERO_CHARGE = reg("aoe_subzero_charge");
    public static final RegistryObject<SoundEvent> SUBZERO_IMPACT = reg("aoe_subzero_impact");
    public static final RegistryObject<SoundEvent> SUBZERO_SHOOT = reg("aoe_subzero_shoot");
    public static final RegistryObject<SoundEvent> SUBZERO_VORTEX_BURST = reg("aoe_subzero_vortex_burst");
    public static final RegistryObject<SoundEvent> SUBZERO_VORTEX_CHARGE = reg("aoe_subzero_vortex_charge");

    public static final RegistryObject<SoundEvent> UA_CHARGE = reg("aoe_ua_charge");
    public static final RegistryObject<SoundEvent> UA_BLAST = reg("aoe_ua_blast");
    public static final RegistryObject<SoundEvent> UA_LOOP = reg("aoe_ua_loop");
    public static final RegistryObject<SoundEvent> UA_VORTEX = reg("aoe_ua_vortex");

    public static final RegistryObject<SoundEvent> MUSIC_PHASE1 = reg("music_phase1");
    public static final RegistryObject<SoundEvent> MUSIC_PHASE2 = reg("music_phase2");
    public static final RegistryObject<SoundEvent> MUSIC_PHASE3 = reg("music_phase3");
    public static final RegistryObject<SoundEvent> MUSIC_PARADISE = reg("music_paradise");
    public static final RegistryObject<SoundEvent> MUSIC_WINTER = reg("music_winter");

    private static RegistryObject<SoundEvent> reg(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(AvatarOfEmptinessMod.id(name)));
    }

    public static SoundEvent vanilla(String path) {
        SoundEvent e = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("minecraft", path));
        return e != null ? e : SoundEvent.createVariableRangeEvent(new ResourceLocation("minecraft", path));
    }

    private ModSounds() {}
}
