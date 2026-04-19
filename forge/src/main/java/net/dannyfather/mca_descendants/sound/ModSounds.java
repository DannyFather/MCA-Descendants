package net.dannyfather.mca_descendants.sound;

import net.dannyfather.mca_descendants.MCADescendants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MCADescendants.MODID);

    public static final RegistryObject<SoundEvent> PHONE_PICKUP =
            SOUND_EVENTS.register("phone_pickup",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(MCADescendants.MODID, "phone_pickup")));

    public static final RegistryObject<SoundEvent> PHONE_HANGUP =
            SOUND_EVENTS.register("phone_hangup",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(MCADescendants.MODID, "phone_hangup")));

    public static final RegistryObject<SoundEvent> PHONE_DIAL_TONE =
            SOUND_EVENTS.register("phone_dial_tone",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(MCADescendants.MODID, "phone_dial_tone")));

    public static final RegistryObject<SoundEvent> PHONE_RINGING =
            SOUND_EVENTS.register("phone_ringing",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(MCADescendants.MODID, "phone_ringing")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}