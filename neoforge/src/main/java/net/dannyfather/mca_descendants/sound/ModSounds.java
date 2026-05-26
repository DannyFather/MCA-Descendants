package net.dannyfather.mca_descendants.sound;

import net.dannyfather.mca_descendants.MCADescendants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MCADescendants.MOD_ID);

    public static final Supplier<SoundEvent> PHONE_PICKUP = registerSoundEvent("phone_pickup");

    public static final Supplier<SoundEvent> PHONE_HANGUP = registerSoundEvent("phone_hangup");

    public static final Supplier<SoundEvent> PHONE_DIAL_TONE = registerSoundEvent("phone_dial_tone");

    public static final Supplier<SoundEvent> PHONE_RINGING = registerSoundEvent("phone_ringing");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MCADescendants.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}