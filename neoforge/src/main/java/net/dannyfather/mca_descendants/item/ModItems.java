package net.dannyfather.mca_descendants.item;


import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.item.custom.EvilVillagerGrabberItem;
import net.dannyfather.mca_descendants.item.custom.GoodVillagerGrabberItem;
import net.dannyfather.mca_descendants.item.custom.VillagerGrabberItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MCADescendants.MOD_ID);

    public static final DeferredItem<Item> VILLAGERGRABBER = ITEMS.register("villager_grabber",
            () -> new VillagerGrabberItem(new Item.Properties()));

    public static final DeferredItem<Item> EVILVILLAGERGRABBER = ITEMS.register("evil_villager_grabber",
            () -> new EvilVillagerGrabberItem(new Item.Properties()));

    public static final DeferredItem<Item> GOODVILLAGERGRABBER = ITEMS.register("good_villager_grabber",
            () -> new GoodVillagerGrabberItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
