package net.dannyfather.mca_descendants.entity;

import net.dannyfather.mca_descendants.MCADescendants;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MCADescendants.MODID);

    public static final RegistryObject<EntityType<BabySittingEntity>> BABY_SEAT = ENTITY_TYPES.register("baby_seat" , () ->
            EntityType.Builder.<BabySittingEntity>of(
                    BabySittingEntity::new,
                    MobCategory.MISC
            ).sized(0.1f,0.23f).clientTrackingRange(1).updateInterval(1).build("baby_seat")
    );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
