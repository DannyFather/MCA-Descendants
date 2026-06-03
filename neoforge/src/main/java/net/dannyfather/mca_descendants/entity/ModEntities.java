package net.dannyfather.mca_descendants.entity;

import net.dannyfather.mca_descendants.MCADescendants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MCADescendants.MOD_ID);

    public static final DeferredHolder<EntityType<?>,EntityType<BabySittingEntity>> BABY_SEAT = ENTITY_TYPES.register("baby_seat" , () ->
            EntityType.Builder.<BabySittingEntity>of(
                    BabySittingEntity::new,
                    MobCategory.MISC
            ).sized(0.1f,0.39f).clientTrackingRange(1).updateInterval(1).build("baby_seat")
    );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
