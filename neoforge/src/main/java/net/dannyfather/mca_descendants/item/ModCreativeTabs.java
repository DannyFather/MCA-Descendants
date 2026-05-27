package net.dannyfather.mca_descendants.item;

import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MCADescendants.MOD_ID);

    public static final Supplier<CreativeModeTab> MCA_DESCENDANTS_TAB =
            CREATIVE_MODE_TABS.register("mca_descendants_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.mca_descendants_tab"))
                    .icon(() -> new ItemStack(ModBlocks.PHONE.get()))

                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.DECAYED_GRASS.get());
                        output.accept(ModBlocks.DECAYED_DIRT.get());
                        output.accept(ModBlocks.PHONE.get());
                    })

                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}