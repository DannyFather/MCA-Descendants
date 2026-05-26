package net.dannyfather.mca_descendants.block;

import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.block.custom.DecayedGrassBlock;
import net.dannyfather.mca_descendants.block.custom.PhoneBlock;
import net.dannyfather.mca_descendants.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(MCADescendants.MOD_ID);


    public static final DeferredBlock<Block> DECAYED_DIRT = registerBlock("decayed_dirt",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COARSE_DIRT).strength(0.6F)));

    public static final DeferredBlock<Block> DECAYED_GRASS = registerBlock("decayed_grass",
            () -> new DecayedGrassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(0.6F).sound(SoundType.GRASS)));

    public static final DeferredBlock<Block> PHONE = registerBlock("phone",
            () -> new PhoneBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noOcclusion()));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
