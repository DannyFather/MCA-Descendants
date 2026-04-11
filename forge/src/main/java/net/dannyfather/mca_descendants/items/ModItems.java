package net.dannyfather.mca_descendants.items;

import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.items.custom.GoodVillagerGrabberItem;
import net.dannyfather.mca_descendants.items.custom.EvilVillagerGrabberItem;
import net.dannyfather.mca_descendants.items.custom.VillagerGrabberItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MCADescendants.MODID);

    public static final RegistryObject<Item> VILLAGERGRABBER = ITEMS.register("villager_grabber",
            () -> new VillagerGrabberItem(new Item.Properties()));

    public static final RegistryObject<Item> EVILVILLAGERGRABBER = ITEMS.register("evil_villager_grabber",
            () -> new EvilVillagerGrabberItem(new Item.Properties()));

    public static final RegistryObject<Item> GOODVILLAGEGRABBER = ITEMS.register("good_villager_grabber",
            () -> new GoodVillagerGrabberItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {ITEMS.register(eventBus);}
}
