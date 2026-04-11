package net.dannyfather.mca_descendants.items;

import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MCADescendants.MODID);

    public static final RegistryObject<CreativeModeTab> MCA_REBORN_TAB = CREATIVE_MODE_TABS.register("mca_descendants",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.DECAYED_DIRT.get()))
                    .title(Component.translatable("creativetab.mca_descendants_tab"))
                    .displayItems(((pParameters, pOutput) -> {
                        pOutput.accept(ModBlocks.DECAYED_DIRT.get());
                        pOutput.accept(ModBlocks.DECAYED_GRASS.get());
                        pOutput.accept(ModBlocks.PHONE.get());
                        pOutput.accept(ModItems.EVILVILLAGERGRABBER.get());
                        pOutput.accept(ModItems.GOODVILLAGEGRABBER.get());
                        pOutput.accept(ModItems.VILLAGERGRABBER.get());
                    })).build());

    public static void register(IEventBus eventBus) {CREATIVE_MODE_TABS.register(eventBus);}

}
