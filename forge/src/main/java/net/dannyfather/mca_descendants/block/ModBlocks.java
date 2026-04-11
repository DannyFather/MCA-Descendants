package net.dannyfather.mca_descendants.block;

import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.block.custom.DecayedGrassBlock;
import net.dannyfather.mca_descendants.block.custom.PhoneBlock;
import net.dannyfather.mca_descendants.items.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MCADescendants.MODID);

    public static final RegistryObject<Block> DECAYED_DIRT = registerBlock("decayed_dirt",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.COARSE_DIRT).strength(0.6F)));

    public static final RegistryObject<Block> DECAYED_GRASS = registerBlock("decayed_grass",
            () -> new DecayedGrassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(0.6F).sound(SoundType.GRASS)));

    public static final RegistryObject<Block> PHONE = registerBlock("phone",
            () -> new PhoneBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK).noOcclusion()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block){
        RegistryObject<T> toReturn = BLOCKS.register(name,block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
